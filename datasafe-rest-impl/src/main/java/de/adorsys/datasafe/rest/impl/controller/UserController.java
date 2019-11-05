package de.adorsys.datasafe.rest.impl.controller;

import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.directory.api.types.StorageCredentials;
import de.adorsys.datasafe.encrypiton.api.types.UserID;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import de.adorsys.datasafe.rest.impl.config.DatasafeProperties;
import de.adorsys.datasafe.rest.impl.dto.NewPasswordDTO;
import de.adorsys.datasafe.rest.impl.dto.StorageCredsDTO;
import de.adorsys.datasafe.rest.impl.dto.UserDTO;
import de.adorsys.datasafe.rest.impl.dto.UserPrivateProfileDTO;
import de.adorsys.datasafe.rest.impl.dto.UserPublicProfileDTO;
import de.adorsys.datasafe.rest.impl.exceptions.UserDoesNotExistsException;
import de.adorsys.datasafe.rest.impl.exceptions.UserExistsException;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import de.adorsys.datasafe.types.api.types.ReadKeyPassword;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * User profile REST api.
 */
@RestController
@RequestMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Api(description = "Create and delete users")
public class UserController {

    private final DefaultDatasafeServices dataSafeService;

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
    @ApiOperation("Creates new user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successfully created"),
            @ApiResponse(code = 400, message = "User already exists")
    })
    public void createUser(@Validated @RequestBody UserDTO userDTO) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(userDTO.getPassword());
        UserIDAuth auth = new UserIDAuth(new UserID(userDTO.getUserName()), readKeyPassword);
        if (dataSafeService.userProfile().userExists(auth.getUserID())) {
            throw new UserExistsException("user '" + auth.getUserID().getValue() + "' already exists");
        }
        dataSafeService.userProfile().registerUsingDefaults(auth);
    }

    @PostMapping("/password")
    @ApiOperation("Change users' password")
    public void changePassword(@RequestHeader String user,
                               @RequestHeader String password,
                               @Validated @RequestBody NewPasswordDTO newPassword) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        dataSafeService.userProfile().updateReadKeyPassword(auth, ReadKeyPasswordHelper.getForString(newPassword.getNewPassword()));
    }

    @GetMapping("/publicProfile")
    @ApiOperation("Reads users' public profile")
    public UserPublicProfileDTO getPublicProfile(@RequestHeader String user,
                                                 @RequestHeader String password) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        return UserPublicProfileDTO.from(dataSafeService.userProfile().publicProfile(auth.getUserID()));
    }

    @GetMapping("/privateProfile")
    @ApiOperation("Reads users' private profile")
    public UserPrivateProfileDTO getPrivateProfile(@RequestHeader String user,
                                                   @RequestHeader String password) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        return UserPrivateProfileDTO.from(dataSafeService.userProfile().privateProfile(auth));
    }

    @PostMapping("/publicProfile")
    @ApiOperation("Modifies users' public profile")
    public void updatePublicProfile(@RequestHeader String user,
                                    @RequestHeader String password,
                                    @Validated @RequestBody UserPublicProfileDTO profileDto) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        dataSafeService.userProfile().updatePublicProfile(auth, profileDto.toProfile());
    }

    @PostMapping("/privateProfile")
    @ApiOperation("Modifies users' private profile")
    public void updatePrivateProfile(@RequestHeader String user,
                                     @RequestHeader String password,
                                     @Validated @RequestBody UserPrivateProfileDTO profileDto) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        dataSafeService.userProfile().updatePrivateProfile(auth, profileDto.toProfile());
    }

    @PostMapping("/storages")
    @ApiOperation("Adds users' storage id and credentials")
    public void addStorageCredentials(@RequestHeader String user,
                                      @RequestHeader String password,
                                      @Validated @RequestBody StorageCredsDTO creds) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        dataSafeService.userProfile().registerStorageCredentials(
                auth,
                new StorageIdentifier(creds.getStorageRegexMatcher()),
                new StorageCredentials(creds.getUsername(), creds.getPassword())
        );
    }

    @DeleteMapping("/storages")
    @ApiOperation("Removes users' storage id and credentials")
    public void removeStorageCredentials(@RequestHeader String user,
                                         @RequestHeader String password,
                                         @RequestHeader String storageId) {
        ReadKeyPassword readKeyPassword = ReadKeyPasswordHelper.getForString(password);
        UserIDAuth auth = new UserIDAuth(new UserID(user), readKeyPassword);
        dataSafeService.userProfile().deregisterStorageCredentials(auth, new StorageIdentifier(storageId));
    }

    /**
     * Removes user.
     *
     * @param user     username
     * @param password user password.
     */
    @DeleteMapping
    @ApiOperation("Deletes existing user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "User successfully deleted"),
            @ApiResponse(code = 404, message = "User does not exist")
    })
    public void deleteUser(@RequestHeader String user,
                           @RequestHeader String password) {
        UserIDAuth auth = new UserIDAuth(new UserID(user), ReadKeyPasswordHelper.getForString(password));
        if (!dataSafeService.userProfile().userExists(auth.getUserID())) {
            throw new UserDoesNotExistsException("user '" + auth.getUserID().getValue() + "' does not exists");
        }
        dataSafeService.userProfile().deregister(auth);
    }
}
