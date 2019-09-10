package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "location")
public class DocumentDirectoryFQN {
    private final String location;

    public DocumentDirectoryFQN(String s) {
        if (s == null) {
            throw new SimpleAdapterException("DocumentDirectoryFQN must not be null");
        }
        if (s.length() == 0) {
            location = "/";
            return;
        }

        String value = null;

        // remove trailing slash
        s = s.replaceAll("//+", "/");
        if (s.substring(s.length() - 1).equals("/")) {
            value = s.substring(0, s.length() - 1);
        } else {
            value = s;
        }

        // add leading slash
        if (value.length() > 0) {
            if (!value.substring(0, 1).equals("/")) {
                value = "/" + value;
            }
        } else {
            value = "/";
        }
        location = value;

    }

    public DocumentDirectoryFQN addDirectory(String s) {
        return new DocumentDirectoryFQN(location + "/" + s);
    }

    public DocumentFQN addName(String s) {
        return new DocumentFQN(location + "/" + s);
    }

    // docusafe path always with a slash in the beginning
    public String getDocusafePath() {
        return location;
    }

    // datasave path never with a slash in the beginning
    public String getDatasafePath() {
        return location.substring(1);
    }

}
