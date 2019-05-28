package de.adorsys.datasafe.business.impl.e2e.performance.services;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.privatestore.api.PrivateSpaceService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import de.adorsys.datasafe.business.impl.e2e.performance.dto.UserSpec;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.StorageType;
import de.adorsys.datasafe.directory.impl.profile.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class OperationExecutor {

    private final Map<OperationType, Consumer<Operation>> handlers = ImmutableMap.of(
            OperationType.WRITE, this::doWrite,
            OperationType.READ, this::doRead,
            OperationType.LIST, this::doList,
            OperationType.DELETE, this::doDelete
    );

    private final AtomicLong counter = new AtomicLong();

    private final PrivateSpaceService privateSpace;
    private final InboxService inboxService;
    private final Map<String, UserSpec> users;
    private final StatisticService statisticService;

    public void execute(Operation oper) {
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

    @SneakyThrows
    public void doWrite(Operation oper) {
        UserSpec user = requireUser(oper);

        try (OutputStream os = openWriteStream(user, oper)) {
            ByteStreams.copy(user.getGenerator().generate(oper.getContentId().getId()), os);
        }
    }

    @SneakyThrows
    public void doRead(Operation oper) {
        UserSpec user = requireUser(oper);

        try (InputStream is = openReadStream(user, oper)) {
            byte[] users = digest(is);
            byte[] expected = digest(user.getGenerator().generate(oper.getExpected().getId()));

            if (!Arrays.equals(users, expected)) {
                log.error("Checksum mismatch for {}", oper);
                throw new IllegalArgumentException("Failed reading - checksum mismatch");
            }
        }
    }

    public void doList(Operation oper) {
        UserSpec user = requireUser(oper);

        List<AbsoluteLocation<ResolvedResource>> resources = listResources(user, oper).collect(Collectors.toList());

        if (resources.isEmpty()) {
            log.info("Empty bucket");
        }
    }

    public void doDelete(Operation oper) {
        UserSpec user = requireUser(oper);

        RemoveRequest<UserIDAuth, PrivateResource> request =
                RemoveRequest.forDefaultPrivate(user.getAuth(), oper.getLocation());

        if (StorageType.INBOX.equals(oper.getStorageType())) {
            inboxService.remove(request);
            return;
        }

        privateSpace.remove(request);
    }

    private OutputStream openWriteStream(UserSpec user, Operation oper) {
        if (StorageType.INBOX.equals(oper.getStorageType())) {
            return inboxService.write(WriteRequest.forDefaultPublic(user.getAuth().getUserID(), oper.getLocation()));
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

    private UserSpec requireUser(Operation oper) {
        UserSpec user = users.get(oper.getUserId());
        if (null == user) {
            log.error("No such user for {}", oper);
            throw new UserNotFoundException(oper.getUserId());
        }
        return user;
    }

    @SneakyThrows
    private static MessageDigest getDigest() {
        return MessageDigest.getInstance("MD5");
    }
}
