package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DocumentDirectoryFQN {
    private String value;
    public DocumentDirectoryFQN(String s) {
        s = s.replaceAll("//+","/");
        if (s.substring(s.length() - 1).equals("/")) {
            value = s.substring(0,s.length()-1);
        } else {
            value = s;
        }
    }
    public DocumentDirectoryFQN addDirectory(String s) {
        return new DocumentDirectoryFQN(value + "/" + s);
    }

    public DocumentFQN addName(String s) {
        return new DocumentFQN(value + "/" + s);
    }
}
