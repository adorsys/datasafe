package de.adorsys.datasafe.directory.impl.profile.serde;

import com.google.gson.*;
import de.adorsys.datasafe.directory.impl.profile.operations.DFSSystem;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.types.api.resource.*;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import java.security.PublicKey;

/**
 * User profile to json serializer/deserializer.
 * @implNote By default, is used to store profiles beneath {@link DFSSystem#dfsRoot()} as json files.
 */
public class GsonSerde {

    @Delegate
    private final Gson gson;

    @Inject
    public GsonSerde(PublicKeySerde pubSerde) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonDeserializer<PublicResource>)
                        (elem, type, ctx) -> new BasePublicResource(new Uri(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PublicKey.class,
                (JsonDeserializer<PublicKey>) (elem, type, ctx) -> pubSerde.readPubKey(elem.getAsString())
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonDeserializer<PrivateResource>)
                        (elem, type, ctx) -> new BasePrivateResource(new Uri(elem.getAsString()))
        );

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonSerializer<PublicResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.location().toASCIIString())
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonSerializer<PrivateResource>)
                        (elem, type, ctx) -> new JsonPrimitive(elem.location().toASCIIString())
        );

        gsonBuilder.registerTypeAdapter(
                PublicKey.class,
                (JsonSerializer<PublicKey>) (elem, type, ctx) -> new JsonPrimitive(pubSerde.writePubKey(elem))
        );

        this.gson = gsonBuilder.create();
    }
}
