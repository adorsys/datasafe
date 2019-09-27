package de.adorsys.datasafe.cli.commands.profile.storage;

import de.adorsys.datasafe.cli.Cli;
import de.adorsys.datasafe.directory.api.types.UserPrivateProfile;
import de.adorsys.datasafe.types.api.resource.AbsoluteLocation;
import de.adorsys.datasafe.types.api.resource.PrivateResource;
import de.adorsys.datasafe.types.api.resource.StorageIdentifier;
import lombok.Getter;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(
        name = "remove",
        aliases = "rm",
        description = "Removes path access credentials"
)
public class Remove implements Runnable {

    @Getter
    @CommandLine.ParentCommand
    private Storage storage;

    @CommandLine.Option(
            names = {"--identifier", "-i"},
            description = "Storage identifier to be associated with this path",
            required = true)
    private String identifier;

    @Override
    public void run() {
        Cli cli = storage.getProfile().getCli();

        UserPrivateProfile privateProfile = cli.datasafe().userProfile().privateProfile(cli.auth());
        Map<StorageIdentifier, AbsoluteLocation<PrivateResource>> pathsMap = new HashMap<>(
                privateProfile.getPrivateStorage()
        );
        pathsMap.remove(new StorageIdentifier(identifier));

        cli.datasafe().userProfile().updatePrivateProfile(
                cli.auth(),
                privateProfile.toBuilder().privateStorage(pathsMap).build()
        );
    }
}
