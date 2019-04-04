//package de.adorsys.docusafe2;
//
//import de.adorsys.dfs.connection.api.complextypes.BucketPath;
//import de.adorsys.dfs.connection.api.complextypes.BucketPathUtil;
//import de.adorsys.dfs.connection.api.types.BucketPathEncryptionPassword;
//import de.adorsys.dfs.connection.api.types.properties.BucketPathEncryptionFilenameOnly;
//import de.adorsys.dfs.connection.impl.pathencryption.BucketPathEncryption;
//import de.adorsys.docusafe2.business.api.keystore.KeyStoreService;
//import de.adorsys.docusafe2.business.api.keystore.types.*;
//import KeyStoreServiceImpl;
//import de.adorsys.docusafe2.business.api.cmsencryption.services.impl.BucketPathEncryptionServiceImpl;
//import de.adorsys.docusafe2.business.api.cmsencryption.services.BucketPathEncryptionService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.security.KeyStore;
//import java.security.PublicKey;
//import java.util.Date;
//
//@Slf4j
//public class EncryptionTest {
//    BucketPathEncryptionService bucketPathEncryptionService = new BucketPathEncryptionServiceImpl();
//    BucketPathEncryption oldBucketPathEncryptionService = new BucketPathEncryption();
//    KeyStoreService keyStoreService = new KeyStoreServiceImpl();
//    ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
//    ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
//    KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);
//    KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
//    KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
//    KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
//    KeySourceAndKeyID forPublicKey = keyStoreService.getKeySourceAndKeyIDForPublicKey(keyStoreAccess);
//    KeyID keyID = forPublicKey.getKeyID();
//    PublicKey publicKey = (PublicKey) forPublicKey.getKeySource().readKey(keyID);
//    BucketPathEncryptionPassword bucketPathEncryptionPassword = new BucketPathEncryptionPassword("bucketpathencryption");
//
//    @Test
//    public void encryptionTest() {
//        BucketPath bucketPath = new BucketPath("/folder1/folder2/folder3/file1.txt");
//        int loopsize = 100;
//        {
//            long start = new Date().getTime();
//            for (int i = 0; i < loopsize; i++) {
//                BucketPath encryptedBucketPath = bucketPathEncryptionService.encrypt(publicKey, keyID, bucketPath);
//                BucketPath decryptedBucketPath = bucketPathEncryptionService.decrypt(keyStoreAccess, encryptedBucketPath);
//                Assert.assertEquals(bucketPath.toString(), decryptedBucketPath.toString());
//            }
//            long stop = new Date().getTime();
//            BucketPath encryptedBucketPath = bucketPathEncryptionService.encrypt(publicKey, keyID, bucketPath);
//
//            log.info(String.format("asymmetric encryption of \"%s\" for %d times took time: %d ms", bucketPath, loopsize, (stop - start)));
//            log.info(String.format("asymmetric encryption blew up path length from %d to %d ", BucketPathUtil.getAsString(bucketPath).length(), BucketPathUtil.getAsString(encryptedBucketPath).length()));
//        }
//        {
//            long start = new Date().getTime();
//            for (int i = 0; i < loopsize; i++) {
//                BucketPath encryptedBucketPath = oldBucketPathEncryptionService.encrypt(bucketPathEncryptionPassword, BucketPathEncryptionFilenameOnly.FALSE, bucketPath);
//                BucketPath decryptedBucketPath = oldBucketPathEncryptionService.decrypt(bucketPathEncryptionPassword, BucketPathEncryptionFilenameOnly.FALSE, encryptedBucketPath);
//                Assert.assertEquals(bucketPath.toString(), decryptedBucketPath.toString());
//            }
//            long stop = new Date().getTime();
//            BucketPath encryptedBucketPath = oldBucketPathEncryptionService.encrypt(bucketPathEncryptionPassword, BucketPathEncryptionFilenameOnly.FALSE, bucketPath);
//
//            log.info(String.format("symmetric encryption of \"%s\" for %d times took time: %d ms", bucketPath, loopsize, (stop - start)));
//            log.info(String.format("symmetric encryption blew up path length from %d to %d ",BucketPathUtil.getAsString(bucketPath).length(), BucketPathUtil.getAsString(encryptedBucketPath).length()));
//        }
//    }
//}
