package de.adorsys.datasafe.cli.commands.inbox;

import picocli.CommandLine;

@CommandLine.Command(
        name = "cat",
        description = "Decrypts inbox file contents and prints it to STDOUT"
)
public class Cat implements Runnable {

    @Override
    public void run() {

    }
}
