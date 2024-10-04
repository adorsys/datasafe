package de.adorsys.config;

import de.adorsys.EncryptionServices;
import de.adorsys.datasafe.directory.impl.profile.config.DefaultDFSConfig;
import de.adorsys.datasafe.encrypiton.api.types.encryption.MutableEncryptionConfig;
import de.adorsys.datasafe.storage.api.StorageService;
import de.adorsys.datasafe.storage.impl.fs.FileSystemStorageService;
import de.adorsys.datasafe.types.api.context.BaseOverridesRegistry;
import de.adorsys.datasafe.types.api.context.overrides.OverridesRegistry;
import de.adorsys.datasafe.types.api.types.ReadStorePassword;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.file.Path;

@Slf4j
@UtilityClass
public class Config {
    public static final String FILESYSTEM_ENV = "USE_FILESYSTEM";
    private static final MutableEncryptionConfig encryptionConfig = new MutableEncryptionConfig();

    private static StorageService StorageServiceFilesystem(Path fsRoot ) {
        log.info("==================== FILESYSTEM");
        log.info("build to FILESYSTEM with root " + fsRoot);
        String path = System.getenv(FILESYSTEM_ENV);
        return new FileSystemStorageService(Path.of(path));
    }

    @SneakyThrows
    public static EncryptionServices.EncryptionServicesImpl encryptionServices(Path fsRoot, ReadStorePassword readStorePassword, int algo) {
        OverridesRegistry registry = new BaseOverridesRegistry();
        return EncryptionServices
                .builder()
                .setDFSConfig(new DefaultDFSConfig(new URI(fsRoot.toString()), readStorePassword))
                .setEncryption(encryptionConfig.toEncryptionConfig())
                .setStorage(StorageServiceFilesystem(fsRoot))
                .setOverridesRegistry(registry)
                .setAlgorithm(algo)
                .build();
    }

}
