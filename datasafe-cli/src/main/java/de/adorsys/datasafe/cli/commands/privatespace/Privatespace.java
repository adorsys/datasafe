package de.adorsys.datasafe.cli.commands.privatespace;

import picocli.CommandLine;

@CommandLine.Command(
        name = "private",
        description = "Allows user to read and write encrypted files in his privatespace",
        subcommands = {
                Cat.class,
                Copy.class,
                Delete.class,
        })
public class Privatespace implements Runnable {

    @Override
    public void run() {

    }
}
