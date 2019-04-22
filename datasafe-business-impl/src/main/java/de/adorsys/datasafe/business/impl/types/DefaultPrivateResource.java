package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@RequiredArgsConstructor
public class DefaultPrivateResource implements PrivateResource {

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }
}
