package de.adorsys.datasafe.metainfo.version.api.version;

import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;

import java.util.function.Function;

/**
 * Manages `latest` file version link location and provides capability to read its path. For example it can be
 * managed using RDBMS.
 */
public interface EncryptedLatestLinkService {

    /**
     * Provides location of latest link (path-encrypted location of link-to-latest file).
     * @param owner user authorization
     * @param resource relative resource
     * @return Absolute resource location of file with link
     */
    AbsoluteLocation<PrivateResource> resolveLatestLinkLocation(
            UserIDAuth owner, PrivateResource resource, StorageIdentifier identifier);

    /**
     * Reads content of latest link file by decrypting its content.
     * @param owner user authorization
     * @return Function that provides location of latest resource version blob using location of link-file as argument.
     * Function: resource location of latest file -> its latest version blob
     */
    Function<AbsoluteLocation<PrivateResource>, AbsoluteLocation<PrivateResource>> linkDecryptingReader(
            UserIDAuth owner, StorageIdentifier identifier
    );
}
