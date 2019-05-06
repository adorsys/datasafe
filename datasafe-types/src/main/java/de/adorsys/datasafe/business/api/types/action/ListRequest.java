package de.adorsys.datasafe.business.api.types.action;

import de.adorsys.datasafe.business.api.types.resource.DefaultPrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class ListRequest<T> {

    //TODO: Add ROOT bucket path constant
    @NonNull
    private final T owner;

    @NonNull
    private final PrivateResource location;

    public ListRequest(T owner, String path) {
        this.owner = owner;
        this.location = DefaultPrivateResource.forPrivate(URI.create(path));
    }
}
