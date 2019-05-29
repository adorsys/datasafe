package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.types.CreateUserPrivateProfile;
import de.adorsys.datasafe.directory.api.types.CreateUserPublicProfile;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.rest.impl.config.DatasafeProperties;
import de.adorsys.datasafe.rest.impl.dto.UserCreateRequestDTO;
import de.adorsys.datasafe.types.api.resource.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * User profile REST api.
 */
@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class UserController {

   private DatasafeProperties properties;

    private static final String PRIVATE_COMPONENT = "private";
    private static final String PRIVATE_FILES_COMPONENT = PRIVATE_COMPONENT + "/files";
    private static final String INBOX_COMPONENT = "inbox";
    private static final String PUBLIC_COMPONENT = "public";
    private static final String VERSION_COMPONENT = "versions";

    private final DefaultDatasafeServices dataSafeService;

    public UserController(DatasafeProperties properties, DefaultDatasafeServices dataSafeService) {
        this.properties = properties;
        this.dataSafeService = dataSafeService;
    }

    /**
     * Registers user profile using default Datasafe convention - user profile, user private and inbox space
     * are located within {@link DatasafeProperties#getSystemRoot()} storage root, using convention:
     * User profile {@link de.adorsys.datasafe.directory.impl.profile.operations.DFSBasedProfileStorageImpl}:
     * public-profile: ${systemRoot}/profiles/public/${userName}
     * private-profile: ${systemRoot}/profiles/private/${userName}
     * User files:
     * privatespace-raw-files: ${systemRoot}/${userName}/private/files
     * privatespace-latest-file-version: ${systemRoot}/${userName}/versions
     * privatespace-keystore: ${systemRoot}/${userName}/private/keystore
     * inbox: ${systemRoot}/${userName}/inbox
     * public-keys: ${systemRoot}/${userName}/public/keystore
     */
    @PutMapping
    @ApiOperation("Create user")
    public void createUser(@RequestBody UserCreateRequestDTO requestDTO) {

        ReadKeyPassword readKeyPassword = new ReadKeyPassword(requestDTO.getPassword());
        UserIDAuth auth = new UserIDAuth(new UserID(requestDTO.getUserName()), readKeyPassword);

        Uri rootLocation = new Uri(properties.getSystemRoot());
        rootLocation = rootLocation.resolve(requestDTO.getUserName() + "/");
        Uri inboxUri = rootLocation.resolve("./" + INBOX_COMPONENT + "/");

        Uri keyStoreUri = rootLocation.resolve("./" + PRIVATE_COMPONENT + "/keystore");

        AbsoluteLocation<PublicResource> publicKeys = access(
                rootLocation.resolve("./" + PUBLIC_COMPONENT + "/keystore")
        );

        dataSafeService.userProfile().registerPublic(CreateUserPublicProfile.builder()
                .id(auth.getUserID())
                .inbox(access(inboxUri))
                .publicKeys(publicKeys)
                .build()
        );

        Uri filesUri = rootLocation.resolve("./" + PRIVATE_FILES_COMPONENT + "/");

        dataSafeService.userProfile().registerPrivate(CreateUserPrivateProfile.builder()
                .id(auth)
                .privateStorage(accessPrivate(filesUri))
                .keystore(accessPrivate(keyStoreUri))
                .inboxWithWriteAccess(accessPrivate(inboxUri))
                .documentVersionStorage(accessPrivate(rootLocation.resolve("./" + VERSION_COMPONENT + "/")))
                .publishPubKeysTo(publicKeys)
                .build()
        );
    }

    @DeleteMapping
    @ApiOperation("Delete user")
    public void deleteUser(@RequestHeader String user,
                           @RequestHeader String password) {
        UserIDAuth userIDAuth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        dataSafeService.userProfile().deregister(userIDAuth);
    }

    private AbsoluteLocation<PublicResource> access(Uri path) {
        return new AbsoluteLocation<>(new BasePublicResource(path));
    }

    private AbsoluteLocation<PrivateResource> accessPrivate(Uri path) {
        return new AbsoluteLocation<>(new BasePrivateResource(path, new Uri(""), new Uri("")));
    }
}
