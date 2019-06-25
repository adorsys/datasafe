package de.adorsys.datasafe.business.impl.e2e.performance.fixture.dto;

import com.google.common.collect.Iterables;
import lombok.Data;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.HashMap;
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
public class TestFileTree {

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
        publishTo.accept(new Operation(
                testUser.getUsername(), OperationType.WRITE, storageType, id, URI.create(path), null));
    }

    /**
     * Perform share operation with some content.
     */
    @Synchronized
    public void share(String path, ContentId id, Set<UserFileSystem> usersToShareWith) {
        if (null == id) {
            throw new IllegalArgumentException("No content");
        }

        if (usersToShareWith.isEmpty()) {
            throw new IllegalArgumentException("No recipients");
        }

        log.info(">SHARE [{}]:{} to {} for {}", storageTag(), id, path, usersToShareWith);
        usersToShareWith.forEach(it -> it.getInboxFiles().getFiles().put(path, id));
        publishTo.accept(new Opaeration(
                testUser.getUsername(), OperationType.SHARE, storageType, id, URI.create(path), null));
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
        publishTo.accept(new Operation(
                testUser.getUsername(), OperationType.READ, storageType, value, URI.create(path), null));
        return value;
    }

    /**
     * Perform list operation on virtual tree.
     */
    @Synchronized
    public Set<String> list(String path) {
        log.info(">LIST [{}]:{}", storageTag(), path);
        publishTo.accept(new Operation(
                testUser.getUsername(), OperationType.LIST, storageType, null, URI.create(path), null));
        return files.keySet().stream().filter(it -> it.startsWith(path)).collect(Collectors.toSet());
    }

    /**
     * Perform delete operation on virtual tree.
     */
    @Synchronized
    public void delete(String path) {
        ContentId value = files.get(path);
        if (null == value) {
            throw new IllegalArgumentException("Deleting non-existing path");
        }

        log.info(">DELETE [{}]:{}:{}", storageTag(), path, value);
        files.remove(path);
        publishTo.accept(new Operation(
                testUser.getUsername(), OperationType.DELETE, storageType, null, URI.create(path), value));
    }

    /**
     * Select random path from virtual tree
     */
    public String getPathRandomly() {
        log.info("GET RANDOMLY [{}]", storageTag());
        return Iterables.get(files.keySet(), random.nextInt(files.size()));
    }

    private String storageTag() {
        return testUser.getUsername() + "/" + storageType;
    }
}
