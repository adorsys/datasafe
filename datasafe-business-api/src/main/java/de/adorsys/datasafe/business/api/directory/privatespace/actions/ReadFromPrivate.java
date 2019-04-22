package de.adorsys.datasafe.business.api.directory.privatespace.actions;

import de.adorsys.datasafe.business.api.types.action.ReadRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;
import de.adorsys.datasafe.business.api.types.resource.ResourceLocation;

import java.io.InputStream;

public interface ReadFromPrivate {

    InputStream read(ReadRequest request);
}
