package de.adorsys.datasafe.rest.impl.dto;

import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.BasePublicResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.PublicResource;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

    public String str(AbsoluteLocation resource) {
        if (null == resource || null == resource.location()) {
            return null;
        }

        return resource.location().asString();
    }

    public AbsoluteLocation<PrivateResource> privateResource(String str) {
        if (null == str) {
            return null;
        }

        return BasePrivateResource.forAbsolutePrivate(str);
    }

    public AbsoluteLocation<PublicResource> publicResource(String str) {
        if (null == str) {
            return null;
        }

        return BasePublicResource.forAbsolutePublic(str);
    }
}
