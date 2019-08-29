package de.adorsys.datasafe.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "--help", aliases = {"-h"}, helpCommand = true, description = "Show basic help")
public class Help implements Runnable {

    @CommandLine.ParentCommand
    private Object parent;

    @Override
    public void run() {
        CommandLine.usage(parent, System.out);
    }
}
