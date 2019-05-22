package de.adorsys.datasafe.business.impl.e2e.performance;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.api.privatespace.PrivateSpaceService;
import de.adorsys.datasafe.business.api.types.actions.ListRequest;
import de.adorsys.datasafe.business.api.types.actions.ReadRequest;
import de.adorsys.datasafe.business.api.types.actions.RemoveRequest;
import de.adorsys.datasafe.business.api.types.actions.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.ResolvedResource;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.Operation;
import de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto.OperationType;
import de.adorsys.datasafe.business.impl.profile.exceptions.UserNotFoundException;
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
    private final Map<String, UserSpec> users;

    public void execute(Operation oper) {
        long cnt = counter.incrementAndGet();
        
        log.trace("[{}] Executing {}", cnt, oper);
        handlers.get(oper.getType()).accept(oper);
        if (0 == cnt % 100) {
            log.info("[{}] Done operations", cnt);
        }
    }

    @SneakyThrows
    public void doWrite(Operation oper) {
        UserSpec user = requireUser(oper);

        try (OutputStream os = privateSpace.write(WriteRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()))) {
            ByteStreams.copy(user.getGenerator().generate(oper.getContentId().getId()), os);
        }
    }

    @SneakyThrows
    public void doRead(Operation oper) {
        UserSpec user = requireUser(oper);

        try (InputStream is = privateSpace.read(ReadRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()))) {
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

        List<AbsoluteLocation<ResolvedResource>> resources =
                privateSpace.list(ListRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()))
                        .collect(Collectors.toList());

        if (resources.isEmpty()) {
            log.info("Empty bucket");
        }
    }

    public void doDelete(Operation oper) {
        UserSpec user = requireUser(oper);

        privateSpace.remove(RemoveRequest.forDefaultPrivate(user.getAuth(), oper.getLocation()));
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
