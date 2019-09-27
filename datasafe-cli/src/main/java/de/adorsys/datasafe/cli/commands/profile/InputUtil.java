package de.adorsys.datasafe.cli.commands.profile;

import com.google.common.base.Strings;
import de.adorsys.datasafe.types.api.resource.Uri;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Allows to read users input from STDIN.
 */
@UtilityClass
public class InputUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Asks user to input value
     * @param text Message describing what value to input
     * @param defaultValue Default value to return, when user clicked enter on blank line
     * @return User input
     */
    public static String input(String text, String defaultValue) {
        System.out.printf("%s (%s):%n", text, defaultValue);
        String input = SCANNER.hasNextLine() ? SCANNER.nextLine() : null;

        if (Strings.isNullOrEmpty(input)) {
            return defaultValue;
        }
        return input;
    }

    /**
     * Asks user to input path-alike value
     * @param text Print message to user describing which value to input
     * @param defaultValue Value to return when user hits 'Enter'
     * @return Users' input
     */
    public static Uri inpPath(String text, String defaultValue) {
        String result = input(text, defaultValue);

        // Fully qualified url
        URI asUri = URI.create(result);
        if (asUri.isAbsolute()) {
            return new Uri(asUri);
        }

        // Some local resource url
        if (result.startsWith("~/")) {
            result = result.replaceFirst("^~", System.getProperty("user.home"));
        }

        return new Uri(Paths.get(result).toUri());
    }
}
