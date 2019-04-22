package de.adorsys.datasafe.business.api.encryption.document;

import de.adorsys.datasafe.business.api.types.UserIDAuth;
import de.adorsys.datasafe.business.api.types.action.ListRequest;
import de.adorsys.datasafe.business.api.types.resource.PrivateResource;

import java.util.stream.Stream;

/**
 * List documents and decrypting their path.
 */
public interface EncryptedDocumentListService {

    /**
     * Lists bucket contents.
     * @param location bucket descriptor, with credentials, where to list data
     * @return stream of available bucket paths
     */
    Stream<PrivateResource> list(ListRequest<UserIDAuth> location);
}
