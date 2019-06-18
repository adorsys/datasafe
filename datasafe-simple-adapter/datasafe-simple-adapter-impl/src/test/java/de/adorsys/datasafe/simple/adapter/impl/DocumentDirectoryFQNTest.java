package de.adorsys.datasafe.simple.adapter.impl;


import de.adorsys.datasafe.simple.adapter.api.types.DocumentDirectoryFQN;
import de.adorsys.datasafe.simple.adapter.api.types.DocumentFQN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DocumentDirectoryFQNTest {
    @Test
    public void slashesForDirectories() {
        DocumentDirectoryFQN d = new DocumentDirectoryFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getValue());
        d = new DocumentDirectoryFQN("/a/b/c/");
        Assertions.assertEquals("/a/b/c", d.getValue());
        d = new DocumentDirectoryFQN("/a///b//c/");
        Assertions.assertEquals("/a/b/c", d.getValue());
    }
    @Test
    public void slashesForFiles() {
        DocumentFQN d = new DocumentFQN("/a/b/c");
        Assertions.assertEquals("/a/b/c", d.getValue());
        d = new DocumentFQN("/a/b/c/");
        Assertions.assertEquals("/a/b/c", d.getValue());
        d = new DocumentFQN("/a///b//c/");
        Assertions.assertEquals("/a/b/c", d.getValue());
    }
}
