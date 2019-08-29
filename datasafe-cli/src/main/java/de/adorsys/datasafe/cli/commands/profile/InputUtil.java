package de.adorsys.datasafe.cli.commands.profile;

import com.google.common.base.Strings;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Scanner;

@UtilityClass
public class InputUtil {

    private static final Scanner sc = new Scanner(System.in);

    public static String input(String text, String defaultValue) {
        System.out.println(text + " (" + defaultValue + "):");
        String input = sc.hasNextLine() ? sc.nextLine() : null;

        if (Strings.isNullOrEmpty(input)) {
            return defaultValue;
        }
        return input;
    }

    public static String inpPath(String text, String defaultValue) {
        String result = input(text, defaultValue);

        if (URI.create(result).isAbsolute()) {
            return result;
        }

        if (result.startsWith("~/")) {
            result = result.replaceFirst("^~", System.getProperty("user.home"));
        }

        return Paths.get(result).toUri().toASCIIString();
    }
}
