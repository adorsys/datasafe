package de.adorsys.docusafe2.serivces;

import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.complextypes.BucketPathUtil;
import de.adorsys.docusafe2.keystore.api.types.KeyID;
import de.adorsys.docusafe2.keystore.api.types.KeyStoreAccess;
import de.adorsys.docusafe2.services.BucketPathEncryptionService;
import de.adorsys.docusafe2.services.CMSEncryptionService;
import org.adorsys.cryptoutils.utils.HexUtil;

import java.security.PublicKey;
import java.util.List;

public class BucketPathEncryptionServiceImpl implements BucketPathEncryptionService {

    private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl();

    @Override
    public BucketPath encrypt(PublicKey publicKey, KeyID keyid, BucketPath bucketPath) {
        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        String encryptedPathString = "";
        for(String subdir : subdirs) {
            byte[] encrypt = cmsEncryptionService.encrypt(subdir.getBytes(), publicKey, keyid);
            String encryptedString = HexUtil.convertBytesToHexString(encrypt);
            encryptedPathString = encryptedPathString + BucketPath.BUCKET_SEPARATOR + encryptedString;
        }
        return new BucketPath(encryptedPathString.toLowerCase());
    }

    @Override
    public BucketPath decrypt(KeyStoreAccess keyStoreAccess, BucketPath bucketPath) {
        List<String> subdirs = BucketPathUtil.split(BucketPathUtil.getAsString(bucketPath));
        String decryptedPathString = "";
        for(String subdir : subdirs) {
            byte[] decrypt = HexUtil.convertHexStringToBytes(subdir.toUpperCase());
            String decryptedString = new String(cmsEncryptionService.decrypt(decrypt, keyStoreAccess));
            decryptedPathString = decryptedPathString + BucketPath.BUCKET_SEPARATOR + decryptedString;
        }
        return new BucketPath(decryptedPathString);
    }
}
