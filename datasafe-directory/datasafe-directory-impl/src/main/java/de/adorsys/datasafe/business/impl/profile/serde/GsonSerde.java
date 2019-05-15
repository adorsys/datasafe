package de.adorsys.datasafe.business.impl.profile.serde;

import com.google.gson.*;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.BasePublicResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import java.net.URI;

public class GsonSerde {

    @Delegate
    private final Gson gson;

    @Inject
    public GsonSerde() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonDeserializer<PublicResource>)
                        (elem, type, ctx) -> new BasePublicResource(URI.create(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonDeserializer<PrivateResource>)
                        (elem, type, ctx) -> new BasePrivateResource(URI.create(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonSerializer<PublicResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.location().toString())
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonSerializer<PrivateResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.location().toString())
        );

        this.gson = gsonBuilder.create();
    }
}
