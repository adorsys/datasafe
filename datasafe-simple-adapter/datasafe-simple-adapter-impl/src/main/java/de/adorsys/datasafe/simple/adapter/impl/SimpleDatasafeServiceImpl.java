package de.adorsys.datasafe.simple.adapter.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SimpleDatasafeServiceImpl implements SimpleDatasafeService {
    private DefaultDatasafeServices defaultDatasafeServices;
    private final static ReadStorePassword universalReadStorePassword = new ReadStorePassword("secret");
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private final static String S3_PREFIX="s3://";


    public SimpleDatasafeServiceImpl() {
        this(DFSCredentialsFactory.getFromEnvironmnet());
    }

    public SimpleDatasafeServiceImpl(DFSCredentials dfsCredentials) {
        boolean withEncryption = true;
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
            defaultDatasafeServices = DaggerSimpleAdapterDatasafeSerivce.builder()
                    .config(new DefaultDFSConfig(filesystemDFSCredentials.getRoot().toAbsolutePath().toUri(), universalReadStorePassword.getValue()))
                    .storage(new FileSystemStorageService(filesystemDFSCredentials.getRoot()))
                    .build();
            log.info("build DFS to FILESYSTEM with root " + filesystemDFSCredentials.getRoot());
        }
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            AmazonS3DFSCredentials amazonS3DFSCredentials = (AmazonS3DFSCredentials) dfsCredentials;
            AmazonS3 amazons3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonS3DFSCredentials.getUrl(), amazonS3DFSCredentials.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonS3DFSCredentials.getAccessKey(), amazonS3DFSCredentials.getSecretKey())))
                    .enablePathStyleAccess()
                    .build();


            if (!amazons3.doesBucketExistV2(amazonS3DFSCredentials.getContainer())) {
                amazons3.createBucket(amazonS3DFSCredentials.getContainer());
            }
            defaultDatasafeServices = DaggerSimpleAdapterDatasafeSerivce.builder()
                    .config(new DefaultDFSConfig(S3_PREFIX + amazonS3DFSCredentials.getRootBucket(), universalReadStorePassword.getValue()))
                    .storage(new S3StorageService(amazons3, amazonS3DFSCredentials.getContainer(), EXECUTOR_SERVICE))
                    .build();
            log.info("build DFS to S3 with root " + amazonS3DFSCredentials.getRootBucket() + " and url " + amazonS3DFSCredentials.getUrl());
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
