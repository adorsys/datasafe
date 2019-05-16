package de.adorsys.datasafe.business.impl.version.latest;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.action.RemoveRequest;
import de.adorsys.datasafe.business.api.types.action.WriteRequest;
import de.adorsys.datasafe.business.api.types.resource.AbsoluteLocation;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.Version;
import de.adorsys.datasafe.business.api.types.resource.Versioned;
import de.adorsys.datasafe.business.api.version.VersionedPrivateSpaceService;
import de.adorsys.datasafe.business.api.version.actions.VersionedList;
import de.adorsys.datasafe.business.api.version.actions.VersionedRead;
import de.adorsys.datasafe.business.api.version.actions.VersionedRemove;
import de.adorsys.datasafe.business.api.version.actions.VersionedWrite;
import de.adorsys.datasafe.business.impl.version.types.LatestDFSVersion;
import lombok.Getter;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Stream;

/**
 * Each operation will be applied to latest file version.
 *
 * @param <V>
 */
public class LatestPrivateSpaceImpl<V extends LatestDFSVersion> implements VersionedPrivateSpaceService<V> {

    @Getter
    private final V strategy;

    private final VersionedList<V> listService;
    private final VersionedRead<V> readService;
    private final VersionedRemove<V> removeService;
    private final VersionedWrite<V> writeService;

    @Inject
    public LatestPrivateSpaceImpl(V strategy, VersionedList<V> listService, VersionedRead<V> readService,
                                  VersionedRemove<V> removeService, VersionedWrite<V> writeService) {
        this.strategy = strategy;
        this.listService = listService;
        this.readService = readService;
        this.removeService = removeService;
        this.writeService = writeService;
    }

    // Delegate didn't work
    @Override
    public Stream<AbsoluteLocation<PrivateResource>> list(ListRequest<UserIDAuth, PrivateResource> request) {
        return listService.list(request);
    }

    // Delegate didn't work
    @Override
    public Stream<Versioned<AbsoluteLocation<PrivateResource>, PrivateResource, Version>> listWithDetails(
            ListRequest<UserIDAuth, PrivateResource> request) {
        return listService.listVersioned(request);
    }

    // Delegate didn't work
    @Override
    public InputStream read(ReadRequest<UserIDAuth, PrivateResource> request) {
        return readService.read(request);
    }

    // Delegate didn't work
    @Override
    public void remove(RemoveRequest<UserIDAuth, PrivateResource> request) {
        removeService.remove(request);
    }

    // Delegate didn't work
    @Override
    public OutputStream write(WriteRequest<UserIDAuth, PrivateResource> request) {
        return writeService.write(request);
    }
}
