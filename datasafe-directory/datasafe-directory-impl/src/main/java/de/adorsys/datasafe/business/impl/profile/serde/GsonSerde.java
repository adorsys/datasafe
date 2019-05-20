package de.adorsys.datasafe.business.impl.profile.serde;

import com.google.gson.*;
import de.adorsys.datasafe.business.api.types.resource.BasePrivateResource;
import de.adorsys.datasafe.business.api.types.resource.BasePublicResource;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.PublicResource;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.security.PublicKey;
import java.util.Base64;

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
                PublicKey.class,
                (JsonDeserializer<PublicKey>) (elem, type, ctx) -> readPubKey(elem)
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

        gsonBuilder.registerTypeAdapter(
                PublicKey.class,
                (JsonSerializer<PublicKey>) (elem, type, ctx) -> writePubKey(elem)
        );

        this.gson = gsonBuilder.create();
    }


    @SneakyThrows
    private JsonPrimitive writePubKey(PublicKey publicKey) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(publicKey);

        return new JsonPrimitive(new String(Base64.getEncoder().encode(bos.toByteArray())));
    }

    @SneakyThrows
    private PublicKey readPubKey(JsonElement in) {
        byte[] bytes = Base64.getDecoder().decode(in.getAsString());
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(bis);
        return (PublicKey) is.readObject();
    }
}
