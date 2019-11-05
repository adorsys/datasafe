package de.adorsys.datasafe.simple.adapter.api;


import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
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
    public void startingSlashForDocumentDirectoryFQN() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getDocusafePath());
        Assertions.assertEquals("a/b/c", d.getDatasafePath());
    }
    @Test void emptyDocumentDirectoryFQN() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("");
        Assertions.assertEquals("/", d.getDocusafePath());
        Assertions.assertEquals("", d.getDatasafePath());
    }
    @Test void slashDocumentDirectoryFQN() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("/");
        Assertions.assertEquals("/", d.getDocusafePath());
        Assertions.assertEquals("", d.getDatasafePath());
    }
}
