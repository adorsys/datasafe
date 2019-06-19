package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
public class DocumentDirectoryFQN {
    private String value;
    public DocumentDirectoryFQN(String s) {
        if (s == null) {
            throw new SimpleAdapterException("DocumentDirectoryFQN must not be null");
        }

        // remove trailing slash
        s = s.replaceAll("//+","/");
        if (s.substring(s.length() - 1).equals("/")) {
            value = s.substring(0,s.length()-1);
        } else {
            value = s;
        }

        // add leading slash
        if (!value.substring(0,1).equals("/")) {
            value = "/" + value;
        }

    }
    public DocumentDirectoryFQN addDirectory(String s) {
        return new DocumentDirectoryFQN(value + "/" + s);
    }

    public DocumentFQN addName(String s) {
        return new DocumentFQN(value + "/" + s);
    }

    // docusafe path always with a slash in the beginning
    public String getDocusafePath() {
        return value;
    }

    // datasave path never with a slash in the beginning
    public String getDatasafePath() {
        return value.substring(1);
    }

}
