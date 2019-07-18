package de.adorsys.datasafe.business.impl.e2e;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Const {

    public static final String MESSAGE_ONE = "Hello here 1";
    public static final String FOLDER = "folder1";
    public static final String PRIVATE_FILE = "secret.txt";
    public static final String PRIVATE_FILE_PATH = FOLDER + "/" + PRIVATE_FILE;
    public static final String SHARED_FILE = "hello.txt";
    public static final String SHARED_FILE_PATH = SHARED_FILE;
}
