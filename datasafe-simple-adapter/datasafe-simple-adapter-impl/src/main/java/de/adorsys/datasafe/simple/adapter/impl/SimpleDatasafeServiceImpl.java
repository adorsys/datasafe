package de.adorsys.datasafe.simple.adapter.impl;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.CharMatcher;
import com.google.common.io.ByteStreams;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.encrypiton.impl.pathencryption.PathEncryptionImplRuntimeDelegatable;
import de.adorsys.datasafe.inbox.api.InboxService;
import de.adorsys.datasafe.simple.adapter.api.SimpleDatasafeService;
import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.AmazonS3DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.DFSCredentialsFactory;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DSDocumentStream;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import de.adorsys.datasafe.simple.adapter.api.types.FilesystemDFSCredentials;
import de.adorsys.datasafe.simple.adapter.api.types.ListRecursiveFlag;
import de.adorsys.datasafe.simple.adapter.impl.config.PathEncryptionConfig;
import de.adorsys.datasafe.simple.adapter.impl.pathencryption.NoPathEncryptionImpl;
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
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import de.adorsys.datasafe.types.api.utils.ExecutorServiceUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SimpleDatasafeServiceImpl implements SimpleDatasafeService {
    private static final String AMAZON_URL = "https://.*s3.amazonaws.com";
    private static final ReadStorePassword universalReadStorePassword = new ReadStorePassword("secret");
    private static final String S3_PREFIX = "s3://";

    private SystemRootAndStorageService rootAndStorage;
    private DefaultDatasafeServices customlyBuiltDatasafeServices;

    public SimpleDatasafeServiceImpl(PathEncryptionConfig pathEncryptionConfig) {
        this(DFSCredentialsFactory.getFromEnvironmnet(), new MutableEncryptionConfig(), pathEncryptionConfig);
    }

    public SimpleDatasafeServiceImpl() {
        this(DFSCredentialsFactory.getFromEnvironmnet(), new MutableEncryptionConfig(), new PathEncryptionConfig(true));
    }

    public SimpleDatasafeServiceImpl(DFSCredentials dfsCredentials, MutableEncryptionConfig config, PathEncryptionConfig pathEncryptionConfig) {

        if (dfsCredentials instanceof FilesystemDFSCredentials) {
            this.rootAndStorage = useFileSystem((FilesystemDFSCredentials) dfsCredentials, pathEncryptionConfig);
        }
        if (dfsCredentials instanceof AmazonS3DFSCredentials) {
            this.rootAndStorage = useAmazonS3((AmazonS3DFSCredentials) dfsCredentials, pathEncryptionConfig);
        }

        SwitchableDatasafeServices.Builder switchableDatasafeService = DaggerSwitchableDatasafeServices.builder()
            .config(new DefaultDFSConfig(rootAndStorage.getSystemRoot(), universalReadStorePassword))
            .encryption(config.toEncryptionConfig())
            .storage(getStorageService());

        if (!pathEncryptionConfig.getWithPathEncryption()) {
            BaseOverridesRegistry baseOverridesRegistry = new BaseOverridesRegistry();
            PathEncryptionImplRuntimeDelegatable.overrideWith(baseOverridesRegistry, args ->
                new NoPathEncryptionImpl(
                    args.getSymmetricPathEncryptionService(),
                    args.getPrivateKeyService()));
            switchableDatasafeService.overridesRegistry(baseOverridesRegistry);
        }

        customlyBuiltDatasafeServices = switchableDatasafeService.build();
    }

    public StorageService getStorageService() {
        return rootAndStorage.getStorageService();
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
        list(userIDAuth, documentDirectoryFQN, ListRecursiveFlag.TRUE).forEach(file -> {
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

        int numberOfSlashesExpected = 1 + CharMatcher.is('/').countIn(documentDirectoryFQN.getDatasafePath());
        return l.stream()
            .filter(el -> CharMatcher.is('/').countIn(el.getDatasafePath()) == numberOfSlashesExpected)
            .collect(Collectors.toList());
    }

    @Override
    public InboxService getInboxService() {
        return customlyBuiltDatasafeServices.inboxService();
    }

    @Override
    public void cleanupDb() {
        rootAndStorage.getStorageService()
            .list(new AbsoluteLocationWithCapability<>(
                BasePrivateResource.forPrivate(rootAndStorage.getSystemRoot()), StorageCapability.LIST_RETURNS_DIR)
            ).forEach(rootAndStorage.getStorageService()::remove);
    }


    private static SystemRootAndStorageService useAmazonS3(AmazonS3DFSCredentials dfsCredentials, PathEncryptionConfig pathEncryptionConfig) {
        AmazonS3DFSCredentials amazonS3DFSCredentials = dfsCredentials;
        LogStringFrame lsf = new LogStringFrame();
        lsf.add("AMAZON S3");
        lsf.add("root bucket        : " + amazonS3DFSCredentials.getRootBucket());
        lsf.add("url                : " + amazonS3DFSCredentials.getUrl());
        lsf.add("region             : " + amazonS3DFSCredentials.getRegion());
        lsf.add("path encryption    : " + pathEncryptionConfig.getWithPathEncryption());
        lsf.add("no https           : " + amazonS3DFSCredentials.isNoHttps());
        lsf.add("threadpool size    : " + amazonS3DFSCredentials.getThreadPoolSize());
        int maxConnections = amazonS3DFSCredentials.getMaxConnections();
        if (maxConnections > 0) {
            lsf.add("max connections    : " + maxConnections);
        }
        int requestTimeout = amazonS3DFSCredentials.getRequestTimeout();
        if (requestTimeout > 0) {
            lsf.add("request timeout    : " + requestTimeout);
        }

        log.info(lsf.toString());
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        amazonS3DFSCredentials.getAccessKey(),
                        amazonS3DFSCredentials.getSecretKey()))
            );

        boolean useEndpoint = !amazonS3DFSCredentials.getUrl().matches(AMAZON_URL)
            && !amazonS3DFSCredentials.getUrl().startsWith(S3_PREFIX);
        lsf = new LogStringFrame();
        if (useEndpoint) {
            lsf.add("not real amazon, so use pathStyleAccess");
            AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(
                amazonS3DFSCredentials.getUrl(),
                amazonS3DFSCredentials.getRegion()
            );
            amazonS3ClientBuilder
                .withEndpointConfiguration(endpoint)
                .enablePathStyleAccess();
        } else {
            lsf.add("real amazon, so use bucketStyleAccess");
            amazonS3ClientBuilder.withRegion(amazonS3DFSCredentials.getRegion());
        }
        log.info("{}", lsf.toString());

        if (amazonS3DFSCredentials.isNoHttps() || maxConnections > 0 || requestTimeout > 0) {
            ClientConfiguration clientConfig = new ClientConfiguration();
            if (amazonS3DFSCredentials.isNoHttps()) {
                log.info("Creating S3 client without https");
                clientConfig.setProtocol(Protocol.HTTP);
                clientConfig.disableSocketProxy();
            }
            if (maxConnections > 0) {
                log.info("Creating S3 client with max connections:{}", maxConnections);
                clientConfig.setMaxConnections(maxConnections);
            }
            if (requestTimeout > 0) {
                log.info("Creating S3 client with connection timeout:{}", requestTimeout);
                clientConfig.setRequestTimeout(requestTimeout);
            }
            amazonS3ClientBuilder.withClientConfiguration(clientConfig);
        }

        AmazonS3 amazons3 = amazonS3ClientBuilder.build();

        if (!amazons3.doesBucketExistV2(amazonS3DFSCredentials.getContainer())) {
            amazons3.createBucket(amazonS3DFSCredentials.getContainer());
        }
        StorageService storageService = new S3StorageService(
            amazons3,
            amazonS3DFSCredentials.getContainer(),
            ExecutorServiceUtil
                .submitterExecutesOnStarvationExecutingService(
                    amazonS3DFSCredentials.getThreadPoolSize(),
                    amazonS3DFSCredentials.getQueueSize()
                )
        );
        URI systemRoot = URI.create(S3_PREFIX + amazonS3DFSCredentials.getRootBucket());
        log.info("build DFS to S3 with root " + amazonS3DFSCredentials.getRootBucket() + " and url " + amazonS3DFSCredentials.getUrl());
        return new SystemRootAndStorageService(systemRoot, storageService);
    }

    private static SystemRootAndStorageService useFileSystem(FilesystemDFSCredentials dfsCredentials, PathEncryptionConfig pathEncryptionConfig) {
        FilesystemDFSCredentials filesystemDFSCredentials = dfsCredentials;
        LogStringFrame lsf = new LogStringFrame();
        lsf.add("FILESYSTEM");
        lsf.add("root bucket     : " + filesystemDFSCredentials.getRoot());
        lsf.add("path encryption : " + pathEncryptionConfig.getWithPathEncryption());
        log.info(lsf.toString());
        URI systemRoot = FileSystems.getDefault().getPath(filesystemDFSCredentials.getRoot()).toAbsolutePath().toUri();
        StorageService storageService = new FileSystemStorageService(FileSystems.getDefault().getPath(filesystemDFSCredentials.getRoot()));
        log.info("build DFS to FILESYSTEM with root " + filesystemDFSCredentials.getRoot());
        return new SystemRootAndStorageService(systemRoot, storageService);
    }


    @AllArgsConstructor
    @Getter
    private static class SystemRootAndStorageService {
        private final URI systemRoot;
        private final StorageService storageService;
    }

}
