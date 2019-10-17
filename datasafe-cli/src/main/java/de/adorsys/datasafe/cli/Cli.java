package de.adorsys.datasafe.cli;

import com.google.gson.Gson;
import de.adorsys.datasafe.business.impl.service.DefaultDatasafeServices;
import de.adorsys.datasafe.cli.commands.Help;
import de.adorsys.datasafe.cli.commands.inbox.Inbox;
import de.adorsys.datasafe.cli.commands.privatespace.Privatespace;
import de.adorsys.datasafe.cli.commands.profile.Profile;
import de.adorsys.datasafe.cli.config.DatasafeFactory;
import de.adorsys.datasafe.cli.dto.Credentials;
import de.adorsys.datasafe.encrypiton.api.types.UserIDAuth;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import picocli.CommandLine;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;

@Slf4j
@CommandLine.Command(
        name = "datasafe-cli",
        subcommands = {
                Help.class,
                Profile.class,
                Privatespace.class,
                Inbox.class
        }
)
@RequiredArgsConstructor
public class Cli implements Runnable {

    @Getter
    @CommandLine.Option(
            names = {"--root-dir", "-rd"},
            description = "Folder with user profiles, default is current directory"
    )
    private Path profilesRoot = Paths.get("");

    @CommandLine.ArgGroup(multiplicity = "1")
    private CredentialsExclusive credentials;

    @SneakyThrows
    public static void main(String[] args) {
        // Restoring correct SecureRandom implementation
        reInitializeRandomAgain();
        // Only needed when running using JRE, unnecessary for CLI:
        Security.addProvider(new BouncyCastleProvider());
        // silencing AWS SDK:
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        int exitCode = new CommandLine(new Cli()).execute(args);

        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(new Cli(), System.out);
    }

    public DefaultDatasafeServices datasafe() {
        return DatasafeFactory.datasafe(profilesRoot, credentials.getSystemPassword());
    }

    public UserIDAuth auth() {
        return new UserIDAuth(credentials.getUsername(), credentials.getPassword());
    }

    private static class CredentialsExclusive {

        @CommandLine.Option(
                names = {"--credentials", "-c"},
                description = "File with credentials location. It contains JSON: " +
                        "{" +
                        "\"username\": \"<username>\", " +
                        "\"password\": \"<password>\", " +
                        "\"systemPassword\": \"<systemPassword>\"" +
                        "}")
        private Path credentialsFile;

        @CommandLine.ArgGroup(exclusive = false)
        private Credentials credentials;

        String getUsername() {
            return credentials().getUsername();
        }

        String getPassword() {
            return credentials().getPassword();
        }

        String getSystemPassword() {
            return credentials().getSystemPassword();
        }

        @SneakyThrows
        private Credentials credentials() {
            if (null != credentials) {
                return credentials;
            }

            try (Reader is = Files.newBufferedReader(credentialsFile)) {
                return new Gson().fromJson(is, Credentials.class);
            }
        }
    }

    @SneakyThrows
    private static void reInitializeRandomAgain() {
        Field secureRandom = CryptoServicesRegistrar.class.getDeclaredField("defaultSecureRandom");
        secureRandom.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(secureRandom, secureRandom.getModifiers() & ~Modifier.FINAL);

        secureRandom.set(CryptoServicesRegistrar.class, null);
    }
}
