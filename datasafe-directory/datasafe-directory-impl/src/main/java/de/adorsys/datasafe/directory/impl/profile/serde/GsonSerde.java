package de.adorsys.datasafe.directory.impl.profile.serde;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import de.adorsys.datasafe.encrypiton.api.keystore.PublicKeySerde;
import de.adorsys.datasafe.types.api.context.annotations.RuntimeDelegate;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import java.net.URI;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User profile to json serializer/deserializer.
 * @implNote By default, is used to store profiles as json files.
 */
@RuntimeDelegate
public class GsonSerde {

    @Delegate
    private final Gson gson;

    @Inject
    public GsonSerde(PublicKeySerde pubSerde) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(
                PublicResource.class,
                (JsonDeserializer<PublicResource>)
                        (elem, type, ctx) -> new BasePublicResource(new Uri(URI.create(elem.getAsString())))
        );

        gsonBuilder.registerTypeAdapter(
                PublicKey.class,
                (JsonDeserializer<PublicKey>) (elem, type, ctx) -> pubSerde.readPubKey(elem.getAsString())
        );

        gsonBuilder.registerTypeAdapter(
                PrivateResource.class,
                (JsonDeserializer<PrivateResource>)
                        (elem, type, ctx) -> new BasePrivateResource(new Uri(URI.create(elem.getAsString())))
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

        Gson basic = gsonBuilder.enableComplexMapKeySerialization().create();

        // Allow support for old-style profiles that have 1 single private storage
        gsonBuilder.registerTypeAdapter(
            new TypeToken<Map<StorageIdentifier, AbsoluteLocation<PrivateResource>>>() {}.getType(),
            (JsonDeserializer<Map<StorageIdentifier, AbsoluteLocation<PrivateResource>>>)
                (elem, type, ctx) -> {
                    if (!(elem instanceof JsonObject)) {
                        return basic.fromJson(elem, type);
                    }

                    JsonObject object = (JsonObject) elem;

                    if (null != object.get("resource") && object.entrySet().size() == 1) {
                        return new HashMap<>(
                            Collections.singletonMap(
                                StorageIdentifier.DEFAULT,
                                BasePrivateResource.forAbsolutePrivate(object.get("resource").getAsString()))
                        );
                    }

                    throw new IllegalArgumentException("Unparseable node");
                }
        );

        this.gson = gsonBuilder.create();
    }
}
