package de.adorsys.datasafe.simple.adapter.impl;


import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentDirectoryFQNTest {
    @Test
    public void slashesForDirectories() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        d = new DocumentDirectoryFQN("/a/b/c/");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        d = new DocumentDirectoryFQN("/a///b//c/");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
    }
    @Test
    public void slashesForFiles() {
        DocumentFQN d = new DocumentFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        d = new DocumentFQN("/a/b/c/");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        d = new DocumentFQN("/a///b//c/");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
    }

    @Test
    public void startingSlashForDocumentFQN() {
        DocumentFQN d = new DocumentFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        Assertions.assertEquals("a/b/c", d.getDatasafePath());
    }
    @Test
    public void startingSlashForDocumentDirectoryFQN() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        Assertions.assertEquals("a/b/c", d.getDatasafePath());
    }
}
