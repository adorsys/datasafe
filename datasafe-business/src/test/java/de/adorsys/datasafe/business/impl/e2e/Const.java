package de.adorsys.datasafe.business.impl.e2e;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Const {

    protected static final String MESSAGE_ONE = "Hello here 1";
    protected static final String FOLDER = "folder1";
    protected static final String PRIVATE_FILE = "secret.txt";
    protected static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;
    protected static final String SHARED_FILE = "hello.txt";
    protected static final String SHARED_FILE_PATH = SHARED_FILE;

    public static String getMessageOne() {
        return MESSAGE_ONE;
    }

    public static String getFolder() {
        return FOLDER;
    }

    public static String getPrivateFile() {
        return PRIVATE_FILE;
    }

    public static String getPrivateFilePath() {
        return PRIVATE_FILE_PATH;
    }

    public static String getSharedFile() {
        return SHARED_FILE;
    }

    public static String getSharedFilePath() {
        return SHARED_FILE_PATH;
    }
}
