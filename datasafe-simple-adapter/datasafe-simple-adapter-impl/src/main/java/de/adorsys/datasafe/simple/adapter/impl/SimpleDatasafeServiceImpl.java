package de.adorsys.datasafe.simple.adapter.impl;

import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class SimpleDatasafeServiceImpl implements SimpleDatasafeService {
    private DefaultDatasafeServices defaultDatasafeServices;

    SimpleDatasafeServiceImpl(DFSCredentials dfsCredentials) {
        boolean withEncryption = true;
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            defaultDatasafeServices = DaggerSimpleAdapterDatasafeSerivce.builder()
                    .config(new DefaultDFSConfig(dfsCredentials.getRoot().toAbsolutePath().toUri(), "secret"))
                    .storage(new FileSystemStorageService(dfsCredentials.getRoot()))
                    .build();
        }
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        if (userExists(userIDAuth.getUserID())) {
            throw new SimpleAdapterException("user \"" + userIDAuth.getUserID().getValue() + "\" already exists");
        }
        defaultDatasafeServices.userProfile().registerUsingDefaults(userIDAuth);

    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        PrivateResource location = BasePrivateResource.forPrivate(new Uri("/"));
        defaultDatasafeServices.privateService().remove(RemoveRequest.forPrivate(userIDAuth, location));
        defaultDatasafeServices.userProfile().deregister(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return defaultDatasafeServices.userProfile().userExists(userID);
    }

    @Override
    public void registerDFSCredentials(UserIDAuth userIDAuth, DFSCredentials dfsCredentials) {

    }

    @Override
    @SneakyThrows
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        try (OutputStream os = defaultDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(userIDAuth, dsDocument.getDocumentFQN().getValue()))) {
            os.write(dsDocument.getDocumentContent().getValue());
        }
    }

    @SneakyThrows
    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        DocumentContent documentContent = null;
        try (InputStream is = defaultDatasafeServices.privateService()
                .read(ReadRequest.forDefaultPrivate(userIDAuth, documentFQN.getValue()))) {
            documentContent = new DocumentContent(ByteStreams.toByteArray(is));
        }
        return new DSDocument(documentFQN, documentContent);
    }

    @Override
    public void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream) {

    }

    @Override
    public DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return null;
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {

    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return false;
    }

    @Override
    public void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {

    }

    @Override
    public List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        return null;
    }

    @Override
    public List<DocumentFQN> listInbox(UserIDAuth userIDAuth) {
        return null;
    }

    @Override
    public void writeDocumentToInboxOfUser(UserID receiverUserID, DSDocument document, DocumentFQN destDocumentFQN) {

    }

    @Override
    public DSDocument readDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source) {
        return null;
    }

    @Override
    public void deleteDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN documentFQN) {

    }

    @Override
    public void moveDocumnetToInboxOfUser(UserIDAuth userIDAuth, UserID receiverUserID, DocumentFQN sourceDocumentFQN, DocumentFQN destDocumentFQN, MoveType moveType) {

    }

    @Override
    public DSDocument moveDocumentFromInbox(UserIDAuth userIDAuth, DocumentFQN source, DocumentFQN destination) {
        return null;
    }
}
