package de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.dto;

import com.google.common.collect.Iterables;
import de.adorsys.datasafe.business.impl.e2e.randomactions.framework.fixture.generator.RandomUsers;
import lombok.Data;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Represents users' file tree (virtual tree).
 */
@Data
@Slf4j
public class TestFileTreeOper {

    private final Consumer<Operation> publishTo;
    private final TestUser testUser;
    private final StorageType storageType;
    private final Random random;
    private final Map<String, ContentId> files = new HashMap<>();

    public boolean isEmpty() {
        return files.isEmpty();
    }

    /**
     * Perform write operation with some content on virtual tree.
     */
    @Synchronized
    public void write(String path, ContentId id) {
        if (null == id) {
            throw new IllegalArgumentException("No content");
        }

        log.info(">PUT [{}]:{} to {}", storageTag(), id, path);
        files.put(path, id);
        publishTo.accept(
                Operation.builder()
                        .userId(testUser.getUsername())
                        .contentId(id)
                        .location(path)
                        .storageType(storageType)
                        .type(OperationType.WRITE)
                        .build()
        );
    }

    /**
     * Perform share operation with some content.
     */
    @Synchronized
    public List<UserFileSystem> share(String path, ContentId id, List<UserFileSystem> possibleUsersToShareWith,
                                      RandomUsers randomUsers) {
        if (null == id) {
            throw new IllegalArgumentException("No content");
        }

        if (possibleUsersToShareWith.isEmpty()) {
            throw new IllegalArgumentException("No recipients");
        }

        Collections.shuffle(possibleUsersToShareWith);
        List<UserFileSystem> shareWith = possibleUsersToShareWith.subList(
                0,
                randomUsers.randomUserCount(1, possibleUsersToShareWith.size())
        );

        log.info(">SHARE [{}]:{} to {} for {}", storageTag(), id, path, shareWith);
        shareWith.forEach(it -> it.getInboxOper().getFiles().put(path, id));
        publishTo.accept(
                Operation.builder()
                        .userId(testUser.getUsername())
                        .contentId(id)
                        .location(path)
                        .recipients(shareWith
                                .stream()
                                .map(it -> it.getUser().getUsername())
                                .collect(Collectors.toSet())
                        )
                        .storageType(storageType)
                        .type(OperationType.SHARE)
                        .build()
        );

        return shareWith;
    }

    /**
     * Perform read operation with some content on virtual tree.
     */
    @Synchronized
    public ContentId read(String path) {
        ContentId value = files.get(path);
        if (null == value) {
            throw new IllegalArgumentException("Reading null path");
        }

        log.info(">GET [{}]:{}:{}", storageTag(), path, value);
        publishTo.accept(
                Operation.builder()
                        .userId(testUser.getUsername())
                        .location(path)
                        .storageType(storageType)
                        .type(OperationType.READ)
                        .result(OperationResult.builder().content(value).build())
                        .build()
        );
        return value;
    }

    /**
     * Perform list operation on virtual tree.
     */
    @Synchronized
    public Set<String> list(String path) {
        log.info(">LIST [{}]:{}", storageTag(), path);
        Set<String> result = files.keySet().stream()
                .filter(it -> it.startsWith(path)).collect(Collectors.toSet());
        publishTo.accept(
                Operation.builder()
                        .userId(testUser.getUsername())
                        .location(path)
                        .storageType(storageType)
                        .type(OperationType.LIST)
                        .result(OperationResult.builder().dirContent(result).build())
                        .build()
        );
        return result;
    }

    /**
     * Perform delete operation on virtual tree.
     */
    @Synchronized
    public void delete(String path) {
        Set<String> toRemove = files.keySet().stream()
                .filter(it -> it.startsWith(path))
                .collect(Collectors.toSet());

        log.info(">DELETE [{}]:{}:{}", storageTag(), path, toRemove);
        toRemove.forEach(files::remove);

        publishTo.accept(
                Operation.builder()
                        .userId(testUser.getUsername())
                        .location(path)
                        .storageType(storageType)
                        .type(OperationType.DELETE)
                        .build()
        );
    }

    /**
     * Select random path from virtual tree
     */
    public String getPathRandomly() {
        log.info("GET RANDOMLY [{}]", storageTag());
        return Iterables.get(files.keySet(), random.nextInt(files.size()));
    }

    public String getDirPathRandomly() {
        log.info("GET RANDOMLY [{}]", storageTag());
        String path = Iterables.get(files.keySet(), random.nextInt(files.size()));
        if (!path.contains("/")) {
            return "";
        }

        return path.replaceAll("([^/]*)$", "");
    }

    private String storageTag() {
        return testUser.getUsername() + "/" + storageType;
    }
}
