package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.VersionedDatasafeServices;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.encrypiton.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.rest.impl.config.DatasafeProperties;
import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExists;
import de.adorsys.datasafe.rest.impl.exceptions.UserExistsException;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * User profile REST api.
 */
@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserController {

    private final VersionedDatasafeServices dataSafeService;

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
    public void createUser(@RequestBody UserDTO requestDTO) {
        ReadKeyPassword readKeyPassword = new ReadKeyPassword(requestDTO.getPassword());
        UserIDAuth auth = new UserIDAuth(new UserID(requestDTO.getUserName()), readKeyPassword);
        if (dataSafeService.userProfile().userExists(auth.getUserID())) {
            throw new UserExistsException("user \"" + auth.getUserID().getValue() + "\" already exists");
        }
        dataSafeService.userProfile().registerUsingDefaults(auth);
    }

    /**
     * Removes user.
     * @param user username
     * @param password user password.
     */
    @DeleteMapping
    @ApiOperation("Delete user")
    public void deleteUser(@RequestHeader String user,
                           @RequestHeader String password) {
        UserIDAuth auth = new UserIDAuth(new UserID(user), new ReadKeyPassword(password));
        if (!dataSafeService.userProfile().userExists(auth.getUserID())) {
            throw new UserDoesNotExists("user \"" + auth.getUserID().getValue() + "\" does not exists");
        }
        dataSafeService.userProfile().deregister(auth);
    }
}
