package de.adorsys.datasafe.business.impl.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.SecretKey;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.cms.CMSAlgorithm;
import org.junit.jupiter.api.Test;

import de.adorsys.datasafe.business.api.encryption.EncryptionService;
import de.adorsys.datasafe.business.api.encryption.EncryptionSpec;
import de.adorsys.datasafe.business.api.encryption.KeySource;
import de.adorsys.datasafe.business.api.keystore.KeyStoreService;
import de.adorsys.datasafe.business.api.keystore.types.KeyID;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAccess;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreAuth;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreCreationConfig;
import de.adorsys.datasafe.business.api.keystore.types.KeyStoreType;
import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.keystore.types.ReadKeyPassword;
import de.adorsys.datasafe.business.api.keystore.types.ReadStorePassword;
import de.adorsys.datasafe.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class CMSEncryptionServiceTest {

    private static final String MESSAGE_CONTENT = "message content";
    private EncryptionService cmsEncryptionService = new CMSEncryptionService();
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl();

    @Test
    @SneakyThrows
    public void cmsEnvelopeEncryptAndDecryptTest() {

        ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
        ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        KeyID keyID = publicKeyIDWithPublicKey.getKeyID();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EncryptionSpec encryptionSpec = new EncryptionSpec(CMSAlgorithm.AES128_CBC);
        encryptionSpec.setPublicRecipients(Arrays.asList(publicKeyIDWithPublicKey));

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream, encryptionSpec);

        encryptionStream.write(MESSAGE_CONTENT.getBytes());

        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        KeySource keySource = new KeySource() {

            @Override
            public SecretKey findSecretKey(byte[] keyIdentifier) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PublicKey findPublicKey(byte[] subjectKeyId) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PrivateKey findPrivateKey(byte[] subjectKeyId) {
                return keyStoreService.getPrivateKey(keyStoreAccess, keyID);
            }
        };
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream, keySource);
        byte[] byteArray2 = IOUtils.toByteArray(decryptionStream);

        assertThat(MESSAGE_CONTENT).isEqualTo(new String(byteArray2));
        log.debug("en and decrypted successfully");
    }

    @Test
    @SneakyThrows
    public void cmsEnvelopeEncryptAndDecryptTest2() {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
        ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);

        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream, publicKeyIDWithPublicKey);

        encryptionStream.write(MESSAGE_CONTENT.getBytes());
        encryptionStream.close();
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream, keyStoreAccess);
        byte[] actualResult = IOUtils.toByteArray(decryptionStream);

        assertThat(MESSAGE_CONTENT).isEqualTo(new String(actualResult));
        log.debug("en and decrypted successfully");
    }

    @Test
    @SneakyThrows
    @Ignore
    public void cmsEnvelopeEncryptAndDecryptFileStreamTest() {
        String folderPath = "target/";
        String testFileName = folderPath + "test.dat";
        String encryptedFileName = folderPath + "test_encrypted.dat";
        String decryptedTestFileName = folderPath + "test_decrypted.dat";

        //Generate test file
        int fileSizeInBytes = 1024 * 1024 * 64;//64Mb
        MappedByteBuffer out = new RandomAccessFile(testFileName, "rw").getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, fileSizeInBytes);

        for (int i = 0; i < fileSizeInBytes; i++) {
            out.put((byte) 'x');
        }

        log.debug("Test file with size {}Mb generated", fileSizeInBytes / 1024 / 1024);


        ReadKeyPassword readKeyPassword = new ReadKeyPassword("readkeypassword");
        ReadStorePassword readStorePassword = new ReadStorePassword("readstorepassword");
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);

        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);
        KeyStoreAccess keyStoreAccess = new KeyStoreAccess(keyStore, keyStoreAuth);
        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);


        File testFile = new File(testFileName);
        FileInputStream fis = new FileInputStream(testFile);

        File encryptedFile = new File(encryptedFileName);
        FileOutputStream fos = new FileOutputStream(encryptedFile);
        System.out.println("Total file size to read (in kb) : " + fis.available() / 1024);

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(fos, publicKeyIDWithPublicKey);

        //Read test file to output encryption stream
        int content;
        byte[] buffer = new byte[4096];
        while ((content = fis.read(buffer)) >= 0) {
            encryptionStream.write(buffer, 0, content);
        }



        fis = new FileInputStream(new File(encryptedFileName));
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(fis, keyStoreAccess);

        File decryptedFile = new File(decryptedTestFileName);
        OutputStream os = new FileOutputStream(decryptedFile);

        // Read file to stream and decrypt it
        int bufferSize = 8 * 1024; //8kb
        byte[] buf = new byte[4096];
        for (int len; (len = decryptionStream.read(buf)) >= 0;) {
            os.write(buf, 0, len);
            buf = new byte[4096];
        }

        System.out.println("Successfully byte inserted");

        IOUtils.closeQuietly(os);
        IOUtils.closeQuietly(fis);
        IOUtils.closeQuietly(fos);
        IOUtils.closeQuietly(encryptionStream);
        IOUtils.closeQuietly(decryptionStream);


        System.out.println("FO: " + testFile.length());
        System.out.println("DO: " + decryptedFile.length());
        log.debug("en and decrypted successfully");
    }


}
