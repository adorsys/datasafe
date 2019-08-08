package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.services;

import com.google.common.collect.Streams;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.dto.UserSpec;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto.*;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.profile.operations.ProfileRegistrationService;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.directory.impl.profile.exceptions.UserNotFoundException;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.teststorage.WithStorageProvider;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import de.adorsys.datasafe.types.api.shared.ContentGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Performs {@link Operation} on DFS storage and calculates performance statistics.
 */
@Slf4j
@RequiredArgsConstructor
public class MultiStorageOperationExecutor {

    private final Map<OperationType, Consumer<Operation>> handlers = ImmutableMap.<OperationType, Consumer<Operation>>builder()
            .put(OperationType.CREATE_USER, this::doCreate)
            .put(OperationType.WRITE, this::doWrite)
            .put(OperationType.SHARE, this::doShare)
            .put(OperationType.READ, this::doRead)
            .put(OperationType.LIST, this::doList)
            .put(OperationType.DELETE, this::doDelete)
            .build();

    private final AtomicLong counter = new AtomicLong();

    private final int fileContentSize;
    private final Map<String, UserSpec> users;
    private final StatisticService statisticService;
    private final List<WithStorageProvider.StorageDescriptor> descriptors;
    private static ProfileRegistrationService registrationService;
    private static PrivateSpaceService privateSpace;
    private static InboxService inboxService;

    public void execute(Operation oper) {
        initUserSelectedStorage(oper.getUserId());
        long cnt = counter.incrementAndGet();

        log.trace("[{}] [{} {}/{}/{}] Executing {}",
                cnt, oper.getType(), oper.getUserId(), oper.getStorageType(), oper.getLocation(), oper);

        long start = System.currentTimeMillis();
        handlers.get(oper.getType()).accept(oper);
        long end = System.currentTimeMillis();
        statisticService.reportOperationPerformance(oper, (int) (end - start));

        if (0 == cnt % 100) {
            log.info("[{}] Done operations", cnt);
        }
    }

    private void initUserSelectedStorage(String userId) {
        if(null != descriptors){
            WithStorageProvider.StorageDescriptor descriptor = descriptors.get(Integer.parseInt(userId.split("-")[1]) % descriptors.size());
            log.info("User no: {}, bucket no: {}", userId, Integer.parseInt(userId.split("-")[1]) % descriptors.size());
            DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                    .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                    .storage(descriptor.getStorageService().get())
                    .build();
            registrationService = datasafeServices.userProfile();
            privateSpace = datasafeServices.privateService();
            inboxService = datasafeServices.inboxService();
        }
    }

    public void validateUsersStorageContent(
            String execId,
            Map<String, Map<String, ContentId>> userIdToPrivateSpace,
            Map<String, Map<String, ContentId>> userIdToInboxSpace) {

        userIdToPrivateSpace.forEach((user, storage) ->
                generateValidatingOperations(execId, user, storage, StorageType.PRIVATE)
                        .forEach(this::executeWithoutReport)
        );

        userIdToInboxSpace.forEach((user, storage) ->
                generateValidatingOperations(execId, user, storage, StorageType.INBOX)
                        .forEach(this::executeWithoutReport)
        );
    }

    private void executeWithoutReport(Operation oper) {
        log.trace("[N/A] [{} {}/{}/{}] Executing {} (no report)",
                oper.getType(), oper.getUserId(), oper.getStorageType(), oper.getLocation(), oper);
        handlers.get(oper.getType()).accept(oper);
    }

    private Stream<Operation> generateValidatingOperations(String execId,
                                                           String userId,
                                                           Map<String, ContentId> storage,
                                                           StorageType type) {
        return Streams.concat(
                Stream.of(Operation.builder()
                        .type(OperationType.LIST)
                        .userId(userId)
                        .location(execId)
                        .storageType(type)
                        .result(
                                OperationResult.builder()
                                        .dirContent(
                                                storage.keySet().stream()
                                                        .map(it -> execId + "/" + it)
                                                        .collect(Collectors.toSet())
                                        ).build()
                        )
                        .build()
                ),
                storage.entrySet().stream().map(it ->
                        Operation.builder()
                                .type(OperationType.READ)
                                .userId(userId)
                                .location(execId + "/" + it.getKey())
                                .storageType(type)
                                .result(OperationResult.builder().content(it.getValue()).build())
                                .build()
                )
        );
    }

    @SneakyThrows
    private void doCreate(Operation oper) {
        UserIDAuth auth = new UserIDAuth(oper.getUserId(), oper.getUserId());
        registrationService.registerUsingDefaults(auth);
        users.put(auth.getUserID().getValue(), new UserSpec(auth, new ContentGenerator(fileContentSize)));
    }

    @SneakyThrows
    private void doWrite(Operation oper) {
        UserSpec user = requireUser(oper);

        try (OutputStream os = openWriteStream(user, oper)) {
            ByteStreams.copy(user.getGenerator().generate(oper.getContentId().getId()), os);
        }
    }

    @SneakyThrows
    private void doShare(Operation oper) {
        UserSpec user = requireUser(oper);

        for (String userId : oper.getRecipients()){
            if(null != descriptors){
                UserSpec userNew = requireUser(userId);
                WithStorageProvider.StorageDescriptor descriptor = descriptors.get(Integer.parseInt(userId.split("-")[1]) % descriptors.size());
                DefaultDatasafeServices datasafeServices = DaggerDefaultDatasafeServices.builder()
                        .config(new DefaultDFSConfig(descriptor.getLocation(), "PAZZWORT"))
                        .storage(descriptor.getStorageService().get())
                        .build();
                Set<UserID> userIDS = new HashSet<>();
                userIDS.add(requireUser(userId).getAuth().getUserID());

                if (StorageType.INBOX.equals(oper.getStorageType())) {
                    ByteStreams.copy(user.getGenerator().generate(oper.getContentId().getId()), datasafeServices.inboxService().write(WriteRequest.forDefaultPublic(userIDS, oper.getLocation())));
                }else if(!StorageType.INBOX.equals(oper.getStorageType())){
                    ByteStreams.copy(user.getGenerator().generate(oper.getContentId().getId()), datasafeServices.privateService().write(WriteRequest.forDefaultPrivate(userNew.getAuth(), oper.getLocation())));
                }
            }
        }
    }

    @SneakyThrows
    private void doRead(Operation oper) {
        UserSpec user = requireUser(oper);

        try (InputStream is = openReadStream(user, oper)) {
            byte[] users = digest(is);
            byte[] expected = digest(user.getGenerator().generate(oper.getResult().getContent().getId()));

            if (!Arrays.equals(users, expected)) {
                log.error("Checksum mismatch for {} - found {} / expected {}",
                        oper,
                        Base64.getEncoder().encodeToString(users),
                        Base64.getEncoder().encodeToString(expected)
                );

                throw new IllegalArgumentException("Failed reading - checksum mismatch");
            }
        }
    }

    private void doList(Operation oper) {
        UserSpec user = requireUser(oper);

        List<AbsoluteLocation<ResolvedResource>> resources = listResources(user, oper).collect(Collectors.toList());
        Set<String> paths = resources.stream()
                .map(it -> it.getResource().asPrivate().decryptedPath().getPath())
                .collect(Collectors.toSet());
        if (!paths.equals(oper.getResult().getDirContent())) {
            log.error("Directory content mismatch for {} - found {} / expected {}",
                    oper,
                    paths,
                    oper.getResult().getDirContent()
            );
            throw new IllegalArgumentException("Directory content mismatch");
        }
    }

    private void doDelete(Operation oper) {
        UserSpec user = requireUser(oper);

        RemoveRequest<UserIDAuth, PrivateResource> request =
                RemoveRequest.forDefaultPrivate(user.getAuth(), new Uri(oper.getLocation()));

        if (StorageType.INBOX.equals(oper.getStorageType())) {
            inboxService.remove(request);
            return;
        }

        privateSpace.remove(request);
    }

    private OutputStream openWriteStream(UserSpec user, Operation oper) {
        if (StorageType.INBOX.equals(oper.getStorageType())) {
            return inboxService.write(WriteRequest.forDefaultPublic(
                    oper.getRecipients().stream()
                            .map(it -> requireUser(it).getAuth().getUserID())
                            .collect(Collectors.toSet()),
                    oper.getLocation())
            );
        }

        return privateSpace.write(WriteRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()));
    }

    private InputStream openReadStream(UserSpec user, Operation oper) {
        ReadRequest<UserIDAuth, PrivateResource> request = ReadRequest.forDefaultPrivate(
                user.getAuth(), oper.getLocation()
        );

        if (StorageType.INBOX.equals(oper.getStorageType())) {
            return inboxService.read(request);
        }

        return privateSpace.read(ReadRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()));
    }

    private Stream<AbsoluteLocation<ResolvedResource>> listResources(UserSpec user, Operation oper) {
        ListRequest<UserIDAuth, PrivateResource> request = ListRequest.forDefaultPrivate(
                user.getAuth(), oper.getLocation()
        );

        if (StorageType.INBOX.equals(oper.getStorageType())) {
            return inboxService.list(request);
        }

        return privateSpace.list(request);
    }

    @SneakyThrows
    private byte[] digest(InputStream is) {
        MessageDigest digest = getDigest();
        try (DigestInputStream dis = new DigestInputStream(is, digest)) {
            ByteStreams.copy(dis, ByteStreams.nullOutputStream());
        }

        return digest.digest();
    }

    private UserSpec requireUser(String userId) {
        UserSpec user = users.get(userId);
        if (null == user) {
            log.error("No such user {}", userId);
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    private UserSpec requireUser(Operation oper) {
        return requireUser(oper.getUserId());
    }

    @SneakyThrows
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("MD5");
    }
}
