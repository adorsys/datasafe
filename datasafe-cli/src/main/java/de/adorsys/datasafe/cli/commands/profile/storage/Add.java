package de.adorsys.datasafe.cli.commands.profile.storage;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.BasePrivateResource;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Getter;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(
        name = "add",
        description = "Adds private storage"
)
public class Add implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Storage storage;

    @CommandLine.Option(
            names = {"--identifier", "-i"},
            description = "Storage identifier to be associated with this path",
            required = true)
    private String identifier;

    @CommandLine.Option(
            names = {"--path", "-p"},
            description = "Absolute path (with protocol) that is associated with this identifier",
            required = true)
    private String path;

    @Override
    public void run() {
        Cli cli = storage.getProfile().getCli();

        UserPrivateProfile privateProfile = cli.datasafe().userProfile().privateProfile(cli.auth());
        Map<StorageIdentifier, AbsoluteLocation<PrivateResource>> pathsMap = new HashMap<>(
                privateProfile.getPrivateStorage()
        );

        pathsMap.put(new StorageIdentifier(identifier), BasePrivateResource.forAbsolutePrivate(path));

        cli.datasafe().userProfile().updatePrivateProfile(
                cli.auth(),
                privateProfile.toBuilder().privateStorage(pathsMap).build()
        );
    }
}
