package de.adorsys.datasafe.cli.dto;

import lombok.Data;
import picocli.CommandLine;

@Data
public class Credentials {

    @CommandLine.Option(names = {"--username", "-u"}, description = "Username", required = true)
    private String username;

    @CommandLine.Option(names = {"--password", "-p"}, description = "Users' password", interactive = true,
            required = true)
    private String password;

    @CommandLine.Option(names = {"--system-password", "-sp"}, description = "System password", interactive = true,
            required = true)
    private String systemPassword;
}
