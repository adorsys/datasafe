package de.adorsys.datasafe.business.impl.serde;

import com.google.gson.*;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import de.adorsys.datasafe.business.impl.types.DefaultPrivateResource;
import de.adorsys.datasafe.business.impl.types.DefaultPublicResource;
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
                        (elem, type, ctx) -> new DefaultPublicResource(URI.create(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonDeserializer<PrivateResource>)
                        (elem, type, ctx) -> new DefaultPrivateResource(URI.create(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonSerializer<PublicResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.locationWithAccess().toString())
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonSerializer<PrivateResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.locationWithAccess().toString())
        );

        this.gson = gsonBuilder.create();
    }
}
