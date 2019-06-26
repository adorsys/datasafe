package de.adorsys.datasafe.types.api.callback;

/**
 * Called when file is persisted using software-versioning
 * to tell callee what was the version id assigned to the file (happens when OutputStream is closed and
 * response from storage is received).
 * NOTE: It will get called only if version was assigned and only by software-versioning layer.
 */
@FunctionalInterface
public interface SoftwareVersionCallback extends ResourceWriteCallback {

    /**
     * Handles assigned version to the file
     * @param version Non-null, version assigned by software versioning
     */
    void handleVersionAssigned(String version);
}
