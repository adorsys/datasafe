package de.adorsys.datasafe.cli;

import de.adorsys.datasafe.cli.commands.inbox.Inbox;
import de.adorsys.datasafe.cli.commands.privatespace.Privatespace;
import de.adorsys.datasafe.cli.commands.profile.Profile;
import picocli.CommandLine;

@CommandLine.Command(subcommands = {
        Profile.class,
        Privatespace.class,
        Inbox.class
})
public class Cli implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Cli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(new Cli(), System.out);
    }
}
