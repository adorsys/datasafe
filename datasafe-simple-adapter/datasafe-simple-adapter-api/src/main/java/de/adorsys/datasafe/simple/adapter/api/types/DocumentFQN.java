package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = "location")
public class DocumentFQN {
    private final String location;

    public DocumentFQN(String s) {
        if (s == null) {
            throw new SimpleAdapterException("DocumentFQN must not be null");
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

        if (value.length() == 0) {
            throw new SimpleAdapterException("not a valid value for documentFQN: String with zero length: " + s);
        }
        // add leading slash
        if (!value.substring(0,1).equals("/")) {
            value = "/" + value;
        }

        location = value;
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
