package de.adorsys.datasafe.metainfo.version.api.version;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;

/**
 * Manages `latest` file version link location and provides capability to read its path. For example it can be
 * managed using RDBMS.
 */
public interface EncryptedLatestLinkService {

    /**
     * Provides location of latest link.
     * @param owner user authorization
     * @param resource relative resource
     * @return Absolute resource location of file with link
     */
    AbsoluteLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth owner, PrivateResource resource);

    /**
     * Reads content of latest link.
     * @param owner user authorization
     * @param latestLink location of link-file
     * @return Location of latest resource version
     */
    AbsoluteLocation<PrivateResource> readLinkAndDecrypt(
            UserIDAuth owner,
            AbsoluteLocation<PrivateResource> latestLink);
}
