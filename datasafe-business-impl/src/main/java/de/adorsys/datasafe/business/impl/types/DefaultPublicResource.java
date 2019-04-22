package de.adorsys.datasafe.business.impl.types;

import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import lombok.RequiredArgsConstructor;

import java.net.URI;

@RequiredArgsConstructor
public class DefaultPublicResource implements PublicResource {

    private final URI uri;

    @Override
    public URI locationWithAccess() {
        return uri;
    }
}
