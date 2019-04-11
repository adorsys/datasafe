package de.adorsys.docusafe2.business.impl.bucketpathencryption;

import de.adorsys.dfs.connection.api.complextypes.BucketDirectory;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.complextypes.BucketPathUtil;
import de.adorsys.docusafe2.business.api.bucketpathencryption.BucketPathEncryptionService;
import de.adorsys.docusafe2.business.api.keystore.KeyStoreService;
import de.adorsys.docusafe2.business.api.keystore.types.*;
import de.adorsys.docusafe2.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BucketPathEncryptionTest {

    @Test
    public void encryptionTest() {
        BucketPathEncryptionService bucketPathEncryptionService = new BucketPathEncryptionServiceImpl();
        SecretKeySpec secretKeySpec = getSecretKey();

        BucketPath bucketPath = new BucketPath("/folder1/folder2/folder3/file1.txt");
        int loopsize = 100;
        {
            long start = new Date().getTime();
            for (int i = 0; i < loopsize; i++) {
                BucketPath encryptedBucketPath = bucketPathEncryptionService.encrypt(secretKeySpec, bucketPath);
                BucketPath decryptedBucketPath = bucketPathEncryptionService.decrypt(secretKeySpec, encryptedBucketPath);
                assertThat(decryptedBucketPath.toString()).isEqualTo(bucketPath.toString());
            }
            long stop = new Date().getTime();
            BucketPath encryptedBucketPath = bucketPathEncryptionService.encrypt(secretKeySpec, bucketPath);

            log.info(String.format("asymmetric encryption of \"%s\" for %d times took time: %d ms", bucketPath, loopsize, (stop - start)));
            log.info(String.format("asymmetric encryption blew up path length from %d to %d ", BucketPathUtil.getAsString(bucketPath).length(), BucketPathUtil.getAsString(encryptedBucketPath).length()));
        }

    }

    @Test
    public void encryptionPartTest() {
        BucketPathEncryptionService bucketPathEncryptionService = new BucketPathEncryptionServiceImpl();
        SecretKeySpec secretKeySpec = getSecretKey();

        BucketPath bucketPath1 = new BucketPath("/folder1/folder2/folder3/file1.txt");
        BucketPath bucketPath2 = bucketPath1.getBucketDirectory().appendName("anotherfile");
        BucketPath full1 = bucketPathEncryptionService.encrypt(secretKeySpec, bucketPath1);
        BucketPath full2 = bucketPathEncryptionService.encrypt(secretKeySpec, bucketPath2);
        BucketDirectory d1 = full1.getBucketDirectory();
        BucketDirectory d2 = full2.getBucketDirectory();

        assertThat(BucketPathUtil.getAsString(d2)).isEqualTo(BucketPathUtil.getAsString(d1));
        log.info(bucketPath1 + " and " + bucketPath2 + " both have thZZe same prefix when encrypted:" + d1);
    }

    private SecretKeySpec getSecretKey() {
        KeyStoreService keyStoreService = new KeyStoreServiceImpl();
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
        ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
        KeyStoreCreationConfig config = new KeyStoreCreationConfig(0, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        SecretKeyIDWithKey randomSecretKeyIDWithKey = keyStoreService.getRandomSecretKeyID(keyStoreAccess);
        return (SecretKeySpec) randomSecretKeyIDWithKey.getSecretKey();
    }
}
