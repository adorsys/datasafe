package de.adorsys.datasafe.simple.adapter.impl.pathencryption;

import de.adorsys.datasafe.directory.api.profile.keys.PrivateKeyService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.legacy.pathencryption.LegacySymmetricPathEncryptionService;
import de.adorsys.datasafe.simple.adapter.impl.legacy.pathencryption.LegacyPathEncryptionImpl;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.function.Function;

@Slf4j
public class SwitchablePathEncryptionImpl extends LegacyPathEncryptionImpl {

    public static final String NO_BUCKETPATH_ENCRYPTION = "SC-NO-BUCKETPATH-ENCRYPTION";

    private boolean withPathEncryption = checkIsPathEncryptionToUse();

    @Inject
    public SwitchablePathEncryptionImpl(LegacySymmetricPathEncryptionService legacySymmetricPathEncryptionService,
                                        PrivateKeyService privateKeyService) {
        super(legacySymmetricPathEncryptionService, privateKeyService);
    }

    @Override
    public Uri encrypt(UserIDAuth forUser, Uri path) {
        if (withPathEncryption) {
            return super.encrypt(forUser, path);
        }
        return path;
    }

    @Override
    public Function<Uri, Uri> decryptor(UserIDAuth forUser) {
        if (withPathEncryption) {
            return super.decryptor(forUser);
        }
        return Function.identity();
    }

    public static boolean checkIsPathEncryptionToUse() {
        String value = System.getProperty(NO_BUCKETPATH_ENCRYPTION);
        if (value != null) {
            if (value.equalsIgnoreCase(Boolean.FALSE.toString())) {
                log.debug("path encryption is on");
                return true;
            }
            if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
                log.debug("path encryption is off");
                return false;
            }
            throw new RuntimeException("value " + value + " for " + NO_BUCKETPATH_ENCRYPTION + " is unknown");
        }
        log.debug("path encryption is on");
        return true;
    }

}
