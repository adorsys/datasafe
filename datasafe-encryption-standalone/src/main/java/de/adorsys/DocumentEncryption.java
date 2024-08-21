package de.adorsys;

import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentReadService;
import de.adorsys.datasafe.encrypiton.api.document.EncryptedDocumentWriteService;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.CmsEncryptionConfig;
import de.adorsys.datasafe.encrypiton.api.types.keystore.KeyID;
import de.adorsys.datasafe.encrypiton.api.types.keystore.SecretKeyIDWithKey;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentReadService;
import de.adorsys.datasafe.encrypiton.impl.document.CMSDocumentWriteService;
import de.adorsys.datasafe.storage.api.actions.StorageWriteService;
import de.adorsys.datasafe.types.api.callback.ResourceWriteCallback;
import de.adorsys.datasafe.types.api.resource.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.OutputStream;


@Component
public class DocumentEncryption {

    private EncryptedDocumentWriteService writer;

    private EncryptedDocumentReadService reader;

    private UserIDAuth user;
    private SecretKeyIDWithKey secretKey;
    private String path;
    private File file;


    public DocumentEncryption(File file) {
        this.file = file;
    }

    public OutputStream write(UserIDAuth user, String path, String password) {

        SecretKeyIDWithKey secretKey = new SecretKeyIDWithKey (new KeyID(password), )
        Uri location = new Uri(path);
        PrivateResource privateResource = new BasePrivateResource(location).resolve(new Uri("/path/to/file.txt"), new Uri("/path/to/file.txt"));
        AbsoluteLocation<PrivateResource> absoluteLocation = new AbsoluteLocation<>(privateResource);

        ResourceWriteCallback callback;

        WithCallback<AbsoluteLocation<PrivateResource>, ResourceWriteCallback> locationWithCallback = new WithCallback<>(absoluteLocation,callback);

        return writer.write();
    }


}
