package de.adorsys.datasafe.types.api.callback;

/**
 * Called when file is persisted on storage that supports versioning natively
 * to tell callee what was the version id assigned to the file (happens when OutputStream is closed and
 * response from storage is received).
 * NOTE: It will get called only if version was assigned.
 */
@FunctionalInterface
public interface PhysicalVersionCallback extends ResourceWriteCallback {

    /**
     * Handles assigned version to the file
     * @param version Non-null, version assigned by storage
     */
    void handleVersionAssigned(String version);
}
