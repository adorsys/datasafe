package de.adorsys.datasafe.simple.adapter.impl;

import de.adorsys.datasafe.simple.adapter.api.types.DSDocument;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentContent;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;

import java.util.ArrayList;
import java.util.List;

public class TestHelper {

    public static List<DSDocument>  createDocuments(DocumentDirectoryFQN directory, int numberOfDirectories, int numberOfFiles, int depth) {
        List<DSDocument> list = new ArrayList<>();
        createDocuments(directory, numberOfDirectories, numberOfFiles, depth, list);
        return list;
    }

    private static void createDocuments(DocumentDirectoryFQN directory, int numberOfDirectories, int numberOfFiles, int depth, List<DSDocument> list) {
        // create files in this directory
        for (int file = 0; file < numberOfFiles; file++) {
            list.add(createFile(directory, file));
        }

        if (depth == 0) {
            return;
        }

        // createFile for current directory
        for (int dir = 0; dir < numberOfDirectories; dir++) {
            DocumentDirectoryFQN subdir = directory.addDirectory("subdir_" + dir);
            createDocuments(subdir, numberOfDirectories, numberOfFiles, depth-1, list);
        }
    }

    static private DSDocument createFile(DocumentDirectoryFQN directoryFQN, int numberOfFile) {
        DocumentFQN documentFQN = directoryFQN.addName("file" + numberOfFile + "txt");
        DocumentContent documentContent = new DocumentContent(("my name is " + documentFQN.toString()).getBytes());
        return new DSDocument(documentFQN, documentContent);
    }

}
