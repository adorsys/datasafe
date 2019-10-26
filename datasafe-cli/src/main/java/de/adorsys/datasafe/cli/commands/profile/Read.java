package de.adorsys.datasafe.cli.commands.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import picocli.CommandLine;

@CommandLine.Command(
        name = "cat",
        description = "Reads and prints user profile to STDOUT"
)
public class Read implements Runnable {

    @CommandLine.ParentCommand
    private Profile profile;

    @Override
    public void run() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(" ==== Public profile: ====");
        System.out.println(gson.toJson(
                profile.getCli().datasafe().userProfile().publicProfile(profile.getCli().auth().getUserID())
        ));

        System.out.println(" ==== Private profile: ====");
        System.out.println(gson.toJson(
                profile.getCli().datasafe().userProfile().privateProfile(profile.getCli().auth())
        ));
    }
}
