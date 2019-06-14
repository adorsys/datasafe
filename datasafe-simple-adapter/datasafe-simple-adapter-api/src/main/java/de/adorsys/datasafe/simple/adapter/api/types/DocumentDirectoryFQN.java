package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class DocumentDirectoryFQN {
    private String value;
    public DocumentDirectoryFQN addDirectory(String s) {
        return new DocumentDirectoryFQN(value + "/" + s);
    }

    public DocumentFQN addName(String s) {
        return new DocumentFQN(value + "/" + s);
    }
}
