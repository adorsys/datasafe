package de.adorsys.datasafe.cli.commands.privatespace;

import picocli.CommandLine;

@CommandLine.Command(
        name = "copy",
        aliases = "cp",
        description = "Encrypts and copies file into your private space - only you can read it"
)
public class Copy implements Runnable {

    @Override
    public void run() {

    }
}
