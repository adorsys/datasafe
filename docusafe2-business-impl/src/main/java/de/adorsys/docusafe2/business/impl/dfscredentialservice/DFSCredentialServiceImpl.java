package de.adorsys.docusafe2.business.impl.dfscredentialservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.adorsys.dfs.connection.api.complextypes.BucketPath;
import de.adorsys.dfs.connection.api.domain.Payload;
import de.adorsys.dfs.connection.api.service.api.DFSConnection;
import de.adorsys.dfs.connection.api.service.impl.SimplePayloadImpl;
import de.adorsys.dfs.connection.impl.factory.DFSConnectionFactory;
import de.adorsys.docusafe2.business.api.cmsencryption.CMSEncryptionService;
import de.adorsys.docusafe2.business.api.dfscredentialservice.DFSCredentialService;
import de.adorsys.docusafe2.business.api.keystore.KeyStoreService;
import de.adorsys.docusafe2.business.api.keystore.types.*;
import de.adorsys.docusafe2.business.api.types.DFSCredentials;
import de.adorsys.docusafe2.business.api.types.DocumentContent;
import de.adorsys.docusafe2.business.api.types.UserIDAuth;
import de.adorsys.docusafe2.business.impl.cmsencryption.services.CMSEncryptionServiceImpl;
import de.adorsys.docusafe2.business.impl.keystore.service.KeyStoreServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSException;

import java.io.IOException;
import java.security.KeyStore;
import java.security.PublicKey;

@Slf4j
public class DFSCredentialServiceImpl implements DFSCredentialService {

    public static final String SYSTEM_DFS_DIRECTORY_NAME = "system-dfs";
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private CMSEncryptionService cmsEncryptionService = new CMSEncryptionServiceImpl();
    private KeyStoreService keyStoreService = new KeyStoreServiceImpl();
    private DFSConnection dfsConnectionService = DFSConnectionFactory.get();

    @Override
    public DFSCredentials getDFSCredentials(UserIDAuth userIDAuth) {
        log.trace("get dfs credential for user: " + userIDAuth.getUserID().getValue());

        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);

        BucketPath bucketPathForNewUserInSystemDfs = new BucketPath(SYSTEM_DFS_DIRECTORY_NAME, userIDAuth.getUserID().getValue());
        byte[] data = dfsConnectionService.getBlob(bucketPathForNewUserInSystemDfs).getData();
        DocumentContent decrypt = null;
        try {
            decrypt = cmsEncryptionService.decrypt(new CMSEnvelopedData(data), keyStoreAccess);
        } catch (CMSException e) {
            throw new DFSCredentialException(e.getMessage(), e);
        }

        return gson.fromJson(new String(decrypt.getValue()), DFSCredentials.class);
    }

    @Override
    public void registerDFS(DFSCredentials dfsCredentials, UserIDAuth userIDAuth) {
        log.trace("register dfs for user: " + userIDAuth.getUserID().getValue());

        KeyStoreAccess keyStoreAccess = getKeyStoreAccess(userIDAuth);

        SecretKeyIDWithKey forPublicKey = keyStoreService.getRandomSecretKeyID(keyStoreAccess);
        KeyID keyID = forPublicKey.getKeyID();
        PublicKey publicKey = keyStoreService.getPublicKeys(keyStoreAccess).get(0).getPublicKey();

        DocumentContent origMessage = new DocumentContent(gson.toJson(dfsCredentials).getBytes());
        CMSEnvelopedData encrypted = cmsEncryptionService.encrypt(origMessage, publicKey, keyID);

        BucketPath bucketPathForNewUserInSystemDfs = new BucketPath(SYSTEM_DFS_DIRECTORY_NAME, userIDAuth.getUserID().getValue());

        if(dfsConnectionService.containerExists(bucketPathForNewUserInSystemDfs.getBucketDirectory())) {
            throw new DFSCredentialException("Duplicate account. DFS for user " + userIDAuth.getUserID().getValue() + " already was register");
        }

        dfsConnectionService.createContainer(bucketPathForNewUserInSystemDfs.getBucketDirectory());

        try {
            Payload payload = new SimplePayloadImpl(encrypted.getEncoded());
            dfsConnectionService.putBlob(bucketPathForNewUserInSystemDfs, payload);

            log.trace("dfs registered");
        } catch (IOException e) {
            throw new DFSCredentialException(e.getMessage(), e);
        }
    }

    private KeyStoreAccess getKeyStoreAccess(UserIDAuth userIDAuth) {
        ReadKeyPassword readKeyPassword = userIDAuth.getReadKeyPassword();

        //What is correct way to obtain readStorePassword?
        ReadStorePassword readStorePassword = new ReadStorePassword("read-store-password-for-" + userIDAuth.getUserID().getValue());
        KeyStoreAuth keyStoreAuth = new KeyStoreAuth(readStorePassword, readKeyPassword);

        KeyStoreCreationConfig config = new KeyStoreCreationConfig(1, 0, 1);
        KeyStore keyStore = keyStoreService.createKeyStore(keyStoreAuth, KeyStoreType.DEFAULT, config);

        return new KeyStoreAccess(keyStore, keyStoreAuth);
    }
}
