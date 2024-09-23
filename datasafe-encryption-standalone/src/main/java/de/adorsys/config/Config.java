package de.adorsys.config;

import de.adorsys.EncryptionServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
@UtilityClass
public class Config {
    public static final String FILESYSTEM_ENV = "USE_FILESYSTEM";
    private  Properties properties;


    private static StorageService StorageServiceFilesystem(Path fsRoot) {
        log.info("==================== FILESYSTEM");
        log.info("build to FILESYSTEM with root " + fsRoot);
        return new FileSystemStorageService(fsRoot);
    }
    private static MutableEncryptionConfig encryptionConfig = new MutableEncryptionConfig();

    public static EncryptionServices.EncryptionServicesImpl encryptionServices(Path fsRoot, ReadStorePassword readStorePassword) {
        return EncryptionServices
                .EncryptionBuilder
                .newInstance()
                .setDFSConfig(new DefaultDFSConfig(fsRoot.toUri(), readStorePassword))
                .setEncryption(encryptionConfig.toEncryptionConfig())
                .setStorage(StorageServiceFilesystem(fsRoot))
                .build();

    }

//    public static DefaultDatasafeServices datasafeService(Path fsRoot, ReadStorePassword readStorePassword) {
//        OverridesRegistry registry = new BaseOverridesRegistry();
//
//        return DaggerDefaultDatasafeServices
//                .builder()
//                .config(new DefaultDFSConfig(fsRoot.toUri(), readStorePassword))
//                .storage(StorageServiceFilesystem(fsRoot))
//                .overridesRegistry(registry)
//                .encryption(encryptionConfig.toEncryptionConfig())
//                .build();
//    }

}
