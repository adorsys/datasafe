package de.adorsys.datasafe.encrypiton.impl.cmsencryption;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.encrypiton.api.cmsencryption.CMSEncryptionService;
import de.adorsys.datasafe.encrypiton.api.keystore.KeyStoreService;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.EncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.KeyCreationConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAccess;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyStoreAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.exceptions.DecryptionException;
import de.adorsys.datasafe.encrypiton.impl.keystore.KeyStoreServiceImpl;
import de.adorsys.datasafe.types.api.shared.BaseMockitoTest;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ReadKeyPasswordTestFactory;
import de.adorsys.keymanagement.juggler.services.DaggerBCJuggler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Sets;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSEnvelopedDataStreamGenerator;
import org.bouncycastle.cms.RecipientInfoGenerator;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;

import static com.google.common.io.ByteStreams.toByteArray;
import static de.adorsys.datasafe.encrypiton.impl.cmsencryption.KeyStoreUtil.getKeys;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.internal.util.io.IOUtil.closeQuietly;

@Slf4j
class CmsEncryptionServiceImplTest extends BaseMockitoTest {

    private static final String TEST_MESSAGE_CONTENT = "message content";

    private static KeyStoreAccess keyStoreAccess;
    private static KeyStoreService keyStoreService = new KeyStoreServiceImpl(
            EncryptionConfig.builder().build().getKeystore(),
            DaggerBCJuggler.builder().build()
    );
    private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl(
            new ASNCmsEncryptionConfig(CmsEncryptionConfig.builder().build())
    );

    @BeforeAll
    static void setUp() {
        keyStoreAccess = getKeyStoreAccess();
    }

    @Test
    @SneakyThrows
    void testCmsStreamEnvelopeEncryptAndDecryptTestWithMultipleRecipients() {
        KeyStoreAccess keyStoreAccess1 = getKeyStoreAccess("Alice");
        KeyStoreAccess keyStoreAccess2 = getKeyStoreAccess("Bob");
        KeyStoreAccess keyStoreAccess3 = getKeyStoreAccess("Suzanne");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream, Sets.newHashSet(
                Arrays.asList(getPublicKeyIDWithPublicKey(keyStoreAccess1), getPublicKeyIDWithPublicKey(keyStoreAccess2))
        ));

        encryptionStream.write(TEST_MESSAGE_CONTENT.getBytes());
        encryptionStream.close();

        byte[] byteArray = outputStream.toByteArray();

        for(KeyStoreAccess keyStoreAccessItem : Arrays.asList(keyStoreAccess1, keyStoreAccess2)) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(inputStream,
                    keyIds -> getKeys(keyIds, keyStoreAccessItem));

            byte[] actualResult = toByteArray(decryptionStream);

            assertThat(TEST_MESSAGE_CONTENT).isEqualTo(new String(actualResult));
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        assertThrows(DecryptionException.class, () -> cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, keyStoreAccess3)));
    }

    private PublicKeyIDWithPublicKey getPublicKeyIDWithPublicKey(KeyStoreAccess keyStoreAccess) {
        return keyStoreService.getPublicKeys(keyStoreAccess).stream().findFirst().get();
    }

    @Test
    @SneakyThrows
    void cmsStreamEnvelopeEncryptAndDecryptTest() {
        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                Collections.singleton(new PublicKeyIDWithPublicKey(
                        publicKeyIDWithPublicKey.getKeyID(),
                        publicKeyIDWithPublicKey.getPublicKey()
                ))
        );

        encryptionStream.write(TEST_MESSAGE_CONTENT.getBytes());
        encryptionStream.close();

        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, keyStoreAccess)
        );
        byte[] actualResult = toByteArray(decryptionStream);

        assertThat(TEST_MESSAGE_CONTENT).isEqualTo(new String(actualResult));
    }

    @Test
    @SneakyThrows
    void cmsStreamEnvelopeZeroKeyPairFailTest() {
        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();
        gen.open(outputStream, new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build());

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                Collections.singleton(new PublicKeyIDWithPublicKey(
                        publicKeyIDWithPublicKey.getKeyID(),
                        publicKeyIDWithPublicKey.getPublicKey()
                ))
        );

        encryptionStream.write(TEST_MESSAGE_CONTENT.getBytes());
        closeQuietly(encryptionStream);
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        assertThrows(DecryptionException.class, () -> cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, keyStoreAccess)));
    }

    @Test
    @SneakyThrows
    void cmsStreamEnvelopeTwoKeysPairFailTest() {
        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        RecipientInfoGenerator recipientInfo1 = new JceKeyTransRecipientInfoGenerator("key1".getBytes(), publicKeyIDWithPublicKey.getPublicKey());
        RecipientInfoGenerator recipientInfo2 = new JceKeyTransRecipientInfoGenerator("key2".getBytes(), publicKeyIDWithPublicKey.getPublicKey());

        CMSEnvelopedDataStreamGenerator gen = new CMSEnvelopedDataStreamGenerator();
        gen.addRecipientInfoGenerator(recipientInfo1);
        gen.addRecipientInfoGenerator(recipientInfo2);
        gen.open(outputStream, new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC)
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build());

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                Collections.singleton(new PublicKeyIDWithPublicKey(
                        publicKeyIDWithPublicKey.getKeyID(),
                        publicKeyIDWithPublicKey.getPublicKey()
                ))
        );

        encryptionStream.write(TEST_MESSAGE_CONTENT.getBytes());
        closeQuietly(encryptionStream);
        byte[] byteArray = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        assertThrows(DecryptionException.class, () -> cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, keyStoreAccess)));
    }

    @Test
    @SneakyThrows
    void cmsStreamEnvelopeOneKeyPairFailTest() {
        KeyStoreAccess keyStoreAccess = getKeyStoreAccess();

        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(outputStream,
                Collections.singleton(new PublicKeyIDWithPublicKey(
                        publicKeyIDWithPublicKey.getKeyID(),
                        publicKeyIDWithPublicKey.getPublicKey()
                ))
        );

        encryptionStream.write(TEST_MESSAGE_CONTENT.getBytes());
        encryptionStream.close();
        byte[] byteArray = outputStream.toByteArray();

        KeyStoreAccess newKeyStoreAccess = getKeyStoreAccess(); // new store access would be different from first even was generated similar
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
        assertThrows(DecryptionException.class, () -> cmsEncryptionService.buildDecryptionInputStream(
                inputStream, keyIds -> getKeys(keyIds, newKeyStoreAccess)));
    }

    private static KeyStoreAccess getKeyStoreAccess(String label) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordTestFactory.getForString(label);
        ReadStorePassword readStorePassword = new ReadStorePassword(label);
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyCreationConfig config = KeyCreationConfig.builder().encKeyNumber(1).signKeyNumber(1).build();
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, config);

        return new KeyStoreAccess(keyStore, keyStoreAuth);
    }

    private static KeyStoreAccess getKeyStoreAccess() {
        return getKeyStoreAccess("readkeypassword");
    }

    @Test
    @SneakyThrows
    void cmsEnvelopeEncryptAndDecryptFileStreamTest() {
        String folderPath = "target/";
        String testFilePath = folderPath + "test.dat";
        String encryptedFilePath = folderPath + "test_encrypted.dat";
        String decryptedTestFilePath = folderPath + "test_decrypted.dat";

        int _1MbInBytes = 1024 * 1024;
        int testFileSizeInBytes = 1024 * 1024 * 128; //128Mb
        double freeSpaceThresholdCoeff = 3.1;

        log.info("For the test, needed {}Mb free space", 3.1 * testFileSizeInBytes / _1MbInBytes);

        File testFilesDirectory = new File("target");
        if(testFilesDirectory.getFreeSpace() < testFileSizeInBytes * freeSpaceThresholdCoeff) {
            log.debug("Free disk space: {}, Size of one test file: {}", testFilesDirectory.getFreeSpace(), testFileSizeInBytes);
            fail("Free space on the disk isn't enough for test, encrypted and decrypted files");
        }

        generateTestFile(testFilePath, testFileSizeInBytes);
        log.info("Test file with size {}Mb generated: {}", testFileSizeInBytes / _1MbInBytes, testFilePath);

        PublicKeyIDWithPublicKey publicKeyIDWithPublicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0);

        File encryptedFile = new File(encryptedFilePath);
        FileOutputStream fosEnFile = new FileOutputStream(encryptedFile);

        OutputStream encryptionStream = cmsEncryptionService.buildEncryptionOutputStream(
                fosEnFile,
                Collections.singleton(new PublicKeyIDWithPublicKey(
                        publicKeyIDWithPublicKey.getKeyID(),
                        publicKeyIDWithPublicKey.getPublicKey()
                ))
        );

        Files.copy(Paths.get(testFilePath), encryptionStream);

        closeQuietly(encryptionStream);
        closeQuietly(fosEnFile);
        log.info("File encrypted");

        FileInputStream fisEnFile = new FileInputStream(encryptedFile);
        InputStream decryptionStream = cmsEncryptionService.buildDecryptionInputStream(
                fisEnFile, keyIds -> getKeys(keyIds, keyStoreAccess));

        File decryptedFile = new File(decryptedTestFilePath);
        OutputStream osDecrypt = new FileOutputStream(decryptedFile);

        ByteStreams.copy(decryptionStream, osDecrypt);
        log.info("File decrypted");

        closeQuietly(osDecrypt);
        closeQuietly(fisEnFile);
        closeQuietly(decryptionStream);

        File testFile = new File(testFilePath);
        String checksumOfOriginTestFile = checksum(testFile);
        String checksumOfDecryptedTestFile = checksum(decryptedFile);

        log.info("Origin test file checksum hex: {}", checksumOfOriginTestFile);
        log.info("Decrypted test file checksum hex: {}", checksumOfDecryptedTestFile);
        assertEquals(checksumOfOriginTestFile, checksumOfDecryptedTestFile);

        if(testFile.delete() && encryptedFile.delete() && decryptedFile.delete()) {
            log.trace("Test files were deleted");
        }
    }

    private void generateTestFile(String testFilePath, int testFileSizeInBytes) throws IOException {
        RandomAccessFile originTestFile = new RandomAccessFile(testFilePath, "rw");
        MappedByteBuffer out = originTestFile.getChannel()
                .map(FileChannel.MapMode.READ_WRITE, 0, testFileSizeInBytes);

        for (int i = 0; i < testFileSizeInBytes; i++) {
            out.put((byte) 'x');
        }
    }

    @SneakyThrows
    String checksum(File input) {
        try (InputStream in = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] block = new byte[4096];
            int length;
            while ((length = in.read(block)) > 0) {
                digest.update(block, 0, length);
            }
            return Hex.toHexString(digest.digest());
        }
    }
}
