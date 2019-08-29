package de.adorsys.datasafe.cli.commands.privatespace;

import picocli.CommandLine;

@CommandLine.Command(
        name = "cat",
        description = "Decrypts private file contents and prints it to STDOUT"
)
public class Cat implements Runnable {

    @Override
    public void run() {

    }
}
