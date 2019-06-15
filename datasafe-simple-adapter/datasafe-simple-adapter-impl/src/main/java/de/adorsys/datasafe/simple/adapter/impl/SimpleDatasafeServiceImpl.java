package de.adorsys.datasafe.simple.adapter.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DaggerDefaultDatasafeServices;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.ResolvedResource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class SimpleDatasafeServiceImpl implements SimpleDatasafeService {
    private DefaultDatasafeServices customlyBuiltDatasafeServices;
    private final static ReadStorePassword universalReadStorePassword = new ReadStorePassword("secret");
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);
    private final static String S3_PREFIX = "s3://";


    public SimpleDatasafeServiceImpl() {
        this(DFSCredentialsFactory.getFromEnvironmnet());
    }

    public SimpleDatasafeServiceImpl(DFSCredentials dfsCredentials) {

        BaseOverridesRegistry baseOverridesRegistry = new BaseOverridesRegistry();
        PathEncryptionImplRuntimeDelegatable.overrideWith(baseOverridesRegistry, args -> new SwitchablePathEncryptionImpl(args.getBucketPathEncryptionService(), args.getPrivateKeyService()));
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
            LogStringFrame lsf = new LogStringFrame();
            lsf.add("FILESYSTEM");
            lsf.add("root bucket     : " + filesystemDFSCredentials.getRoot());
            lsf.add("path encryption : " + SwitchablePathEncryptionImpl.checkIsPathEncryptionToUse());
            log.info(lsf.toString());
            URI systemRoot = filesystemDFSCredentials.getRoot().toAbsolutePath().toUri();
            customlyBuiltDatasafeServices = DaggerDefaultDatasafeServices.builder()
                    .config(new DefaultDFSConfig(systemRoot, universalReadStorePassword.getValue()).userProfileLocation(new CustomizedUserProfileLocation(systemRoot)))
                    .storage(new FileSystemStorageService(filesystemDFSCredentials.getRoot()))
                    .overridesRegistry(baseOverridesRegistry)
                    .build();

            log.info("build DFS to FILESYSTEM with root " + filesystemDFSCredentials.getRoot());
        }
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            AmazonS3DFSCredentials amazonS3DFSCredentials = (AmazonS3DFSCredentials) dfsCredentials;
            LogStringFrame lsf = new LogStringFrame();
            lsf.add("AMAZON S3");
            lsf.add("root bucket     : " + amazonS3DFSCredentials.getRootBucket());
            lsf.add("url             : " + amazonS3DFSCredentials.getUrl());
            lsf.add("region          : " + amazonS3DFSCredentials.getRegion());
            lsf.add("path encryption : " + SwitchablePathEncryptionImpl.checkIsPathEncryptionToUse());
            log.info(lsf.toString());
            AmazonS3 amazons3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(amazonS3DFSCredentials.getUrl(), amazonS3DFSCredentials.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonS3DFSCredentials.getAccessKey(), amazonS3DFSCredentials.getSecretKey())))
                    .enablePathStyleAccess()
                    .build();


            if (!amazons3.doesBucketExistV2(amazonS3DFSCredentials.getContainer())) {
                amazons3.createBucket(amazonS3DFSCredentials.getContainer());
            }
            String systemRoot = S3_PREFIX + amazonS3DFSCredentials.getRootBucket();
            customlyBuiltDatasafeServices = DaggerDefaultDatasafeServices.builder()
                    .config(new DefaultDFSConfig(systemRoot, universalReadStorePassword.getValue()).userProfileLocation(new CustomizedUserProfileLocation(systemRoot)))
                    .storage(new S3StorageService(amazons3, amazonS3DFSCredentials.getContainer(), EXECUTOR_SERVICE))
                    .overridesRegistry(baseOverridesRegistry)
                    .build();
            log.info("build DFS to S3 with root " + amazonS3DFSCredentials.getRootBucket() + " and url " + amazonS3DFSCredentials.getUrl());
        }
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        if (userExists(userIDAuth.getUserID())) {
            throw new SimpleAdapterException("user \"" + userIDAuth.getUserID().getValue() + "\" already exists");
        }
        customlyBuiltDatasafeServices.userProfile().registerUsingDefaults(userIDAuth);

    }

    @Override
    public void destroyUser(UserIDAuth userIDAuth) {
        customlyBuiltDatasafeServices.userProfile().deregister(userIDAuth);
    }

    @Override
    public boolean userExists(UserID userID) {
        return customlyBuiltDatasafeServices.userProfile().userExists(userID);
    }

    @Override
    public void registerDFSCredentials(UserIDAuth userIDAuth, DFSCredentials dfsCredentials) {

    }

    @Override
    @SneakyThrows
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        try (OutputStream os = customlyBuiltDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(userIDAuth, dsDocument.getDocumentFQN().getValue()))) {
            os.write(dsDocument.getDocumentContent().getValue());
        }
    }

    @SneakyThrows
    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        DocumentContent documentContent = null;
        try (InputStream is = customlyBuiltDatasafeServices.privateService()
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
        return customlyBuiltDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(userIDAuth, documentFQN.getValue())).count() == 1;
    }

    @Override
    public void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {

    }

    @Override
    public List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        List<DocumentFQN> l = customlyBuiltDatasafeServices.privateService().list(
                ListRequest.forDefaultPrivate(userIDAuth, documentDirectoryFQN.getValue()))
                .map(it -> new DocumentFQN(it.getResource().asPrivate().decryptedPath().toASCIIString()))
                .collect(Collectors.toList());
        if (recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
            return l;
        }
        int numberOfSlashesExpected = 1 + StringUtils.countMatches(documentDirectoryFQN.getValue(), "/");
        return l.stream().filter(el -> StringUtils.countMatches(el.getValue(), "/") == numberOfSlashesExpected).collect(Collectors.toList());
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
