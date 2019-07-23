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
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadStorePassword;
import de.adorsys.datasafe.encrypiton.impl.cmsencryption.CMSEncryptionServiceImplRuntimeDelegatable;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.*;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.storage.impl.s3.S3StorageService;
import de.adorsys.datasafe.types.api.actions.ListRequest;
import de.adorsys.datasafe.types.api.actions.ReadRequest;
import de.adorsys.datasafe.types.api.actions.RemoveRequest;
import de.adorsys.datasafe.types.api.actions.WriteRequest;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocationWithCapability;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageCapability;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class SimpleDatasafeServiceImpl implements SimpleDatasafeService {
    private static final String AMAZON_URL = "https://s3.amazonaws.com";

    private URI systemRoot;
    private StorageService storageService;
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
        CMSEncryptionServiceImplRuntimeDelegatable.overrideWith(baseOverridesRegistry, args -> new SwitchableCmsEncryptionImpl(args.getEncryptionConfig()));
        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            FilesystemDFSCredentials filesystemDFSCredentials = (FilesystemDFSCredentials) dfsCredentials;
            LogStringFrame lsf = new LogStringFrame();
            lsf.add("FILESYSTEM");
            lsf.add("root bucket     : " + filesystemDFSCredentials.getRoot());
            lsf.add("path encryption : " + SwitchablePathEncryptionImpl.checkIsPathEncryptionToUse());
            log.info(lsf.toString());
            this.systemRoot = FileSystems.getDefault().getPath(filesystemDFSCredentials.getRoot()).toAbsolutePath().toUri();
            storageService = new FileSystemStorageService(FileSystems.getDefault().getPath(filesystemDFSCredentials.getRoot()));
            customlyBuiltDatasafeServices = DaggerDefaultDatasafeServices.builder()
                    .config(new DefaultDFSConfig(systemRoot, universalReadStorePassword.getValue()))
                    .storage(getStorageService())
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
            AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(amazonS3DFSCredentials.getAccessKey(), amazonS3DFSCredentials.getSecretKey())))
                    .enablePathStyleAccess();

            boolean useEndpoint = (!amazonS3DFSCredentials.getUrl().equals(AMAZON_URL));
            if (useEndpoint) {
                AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(amazonS3DFSCredentials.getUrl(), amazonS3DFSCredentials.getRegion());
                amazonS3ClientBuilder.withEndpointConfiguration(endpoint);
            } else {
                amazonS3ClientBuilder.withRegion(amazonS3DFSCredentials.getRegion());
            }

            AmazonS3 amazons3 = amazonS3ClientBuilder.build();

            if (!amazons3.doesBucketExistV2(amazonS3DFSCredentials.getContainer())) {
                amazons3.createBucket(amazonS3DFSCredentials.getContainer());
            }
            storageService = new S3StorageService(amazons3, amazonS3DFSCredentials.getContainer(), EXECUTOR_SERVICE);
            this.systemRoot = URI.create(S3_PREFIX + amazonS3DFSCredentials.getRootBucket());
            customlyBuiltDatasafeServices = DaggerDefaultDatasafeServices.builder()
                    .config(new DefaultDFSConfig(systemRoot, universalReadStorePassword.getValue()))
                    .storage(getStorageService())
                    .overridesRegistry(baseOverridesRegistry)
                    .build();
            log.info("build DFS to S3 with root " + amazonS3DFSCredentials.getRootBucket() + " and url " + amazonS3DFSCredentials.getUrl());
        }
    }

    public StorageService getStorageService() {
        return storageService;
    }

    @Override
    public void createUser(UserIDAuth userIDAuth) {
        if (userExists(userIDAuth.getUserID())) {
            throw new SimpleAdapterException("user \"" + userIDAuth.getUserID().getValue() + "\" already exists");
        }
        customlyBuiltDatasafeServices.userProfile().registerUsingDefaults(userIDAuth);
    }

    @Override
    public void changeKeystorePassword(UserIDAuth userIDAuth, ReadKeyPassword newPassword) {
        customlyBuiltDatasafeServices.userProfile().updateReadKeyPassword(userIDAuth, newPassword);
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
    @SneakyThrows
    public void storeDocument(UserIDAuth userIDAuth, DSDocument dsDocument) {
        try (OutputStream os = customlyBuiltDatasafeServices.privateService()
                .write(WriteRequest.forDefaultPrivate(userIDAuth, dsDocument.getDocumentFQN().getDatasafePath()))) {
            os.write(dsDocument.getDocumentContent().getValue());
        }
    }

    @SneakyThrows
    @Override
    public DSDocument readDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        DocumentContent documentContent;
        try (InputStream is = customlyBuiltDatasafeServices.privateService()
                .read(ReadRequest.forDefaultPrivate(userIDAuth, documentFQN.getDatasafePath()))) {
            documentContent = new DocumentContent(ByteStreams.toByteArray(is));
        }
        return new DSDocument(documentFQN, documentContent);
    }

    @Override
    @SneakyThrows
    public void storeDocumentStream(UserIDAuth userIDAuth, DSDocumentStream dsDocumentStream) {
        try (OutputStream os = customlyBuiltDatasafeServices
                .privateService()
                .write(WriteRequest.forDefaultPrivate(
                        userIDAuth,
                        dsDocumentStream.getDocumentFQN().getDatasafePath()))) {
            ByteStreams.copy(dsDocumentStream.getDocumentStream(), os);
        }
    }

    @Override
    public OutputStream storeDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return customlyBuiltDatasafeServices
                .privateService()
                .write(WriteRequest.forDefaultPrivate(userIDAuth, documentFQN.getDatasafePath()));
    }

    @Override
    @SneakyThrows
    public DSDocumentStream readDocumentStream(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return new DSDocumentStream(
                documentFQN,
                customlyBuiltDatasafeServices
                        .privateService()
                        .read(ReadRequest.forDefaultPrivate(userIDAuth, documentFQN.getDatasafePath()))
        );
    }

    @Override
    public void deleteDocument(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        PrivateResource resource = BasePrivateResource.forPrivate(documentFQN.getDatasafePath());
        RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
        customlyBuiltDatasafeServices.privateService().remove(request);
    }

    @Override
    public boolean documentExists(UserIDAuth userIDAuth, DocumentFQN documentFQN) {
        return customlyBuiltDatasafeServices.privateService().list(ListRequest.forDefaultPrivate(userIDAuth, documentFQN.getDatasafePath())).count() == 1;
    }

    @Override
    public void deleteFolder(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN) {
        list(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE).stream().forEach(file -> {
            PrivateResource resource = BasePrivateResource.forPrivate(file.getDatasafePath());
            RemoveRequest<UserIDAuth, PrivateResource> request = RemoveRequest.forPrivate(userIDAuth, resource);
            customlyBuiltDatasafeServices.privateService().remove(request);
        });
    }

    @Override
    public List<DocumentFQN> list(UserIDAuth userIDAuth, DocumentDirectoryFQN documentDirectoryFQN, ListRecursiveFlag recursiveFlag) {
        List<DocumentFQN> l = customlyBuiltDatasafeServices.privateService().list(
                ListRequest.forDefaultPrivate(userIDAuth, documentDirectoryFQN.getDatasafePath()))
                .map(it -> new DocumentFQN(it.getResource().asPrivate().decryptedPath().asString()))
                .collect(Collectors.toList());
        if (recursiveFlag.equals(ListRecursiveFlag.TRUE)) {
            return l;
        }
        int numberOfSlashesExpected = 1 + StringUtils.countMatches(documentDirectoryFQN.getDatasafePath(), "/");
        return l.stream().filter(el -> StringUtils.countMatches(el.getDatasafePath(), "/") == numberOfSlashesExpected).collect(Collectors.toList());
    }

    @Override
    public void cleanupDb() {
        storageService
                .list(new AbsoluteLocationWithCapability<>(
                        BasePrivateResource.forPrivate(systemRoot), StorageCapability.LIST_RETURNS_DIR)
                ).forEach(storageService::remove);
    }
}
