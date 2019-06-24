package de.adorsys.datasafe.simple.adapter.api;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentFQNTest {
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
    @Test void emptyDocumentFQN() {
        DocumentFQN d = new DocumentFQN("");
        Assertions.assertEquals("/", d.getDocusafePath());
        Assertions.assertEquals("", d.getDatasafePath());
    }
    @Test void slashDocumentFQN() {
        Assertions.assertThrows(SimpleAdapterException.class , () ->new DocumentFQN("/"));
    }
}
