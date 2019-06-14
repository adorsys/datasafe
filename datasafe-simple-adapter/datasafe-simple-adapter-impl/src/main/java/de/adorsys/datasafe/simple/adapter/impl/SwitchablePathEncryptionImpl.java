package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.pathencryption.encryption.SymmetricPathEncryptionService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class SwitchablePathEncryptionImpl extends PathEncryptionImpl {

    private static final String NO_BUCKETPATH_ENCRYPTION = "SC-NO-BUCKETPATH-ENCRYPTION";

    private static boolean withPathEncryption = checkIsPathEncryptionToUse();

    @Inject
    public SwitchablePathEncryptionImpl(SymmetricPathEncryptionService bucketPathEncryptionService, PrivateKeyService privateKeyService) {
        super(bucketPathEncryptionService, privateKeyService);
    }

    public static boolean checkIsPathEncryptionToUse() {
        if (System.getProperty(NO_BUCKETPATH_ENCRYPTION) != null) {
            String value = System.getProperty(NO_BUCKETPATH_ENCRYPTION);
            if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                log.info("path encryption is on");
                return true;
            }
            log.info("path encryption is off");
            return false;
        }
        log.info("path encryption is on");
        return true;
    }


    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        if (withPathEncryption) {
            return super.encrypt(forUser, path);
        }
        return path;
    }

    @Override
    public Uri decrypt(UserIDAuth forUser, Uri path) {
        if (withPathEncryption) {
            return super.decrypt(forUser, path);
        }
        return path;
    }
}
