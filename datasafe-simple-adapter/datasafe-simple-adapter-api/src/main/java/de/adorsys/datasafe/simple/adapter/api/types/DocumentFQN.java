package de.adorsys.datasafe.simple.adapter.api.types;

import de.adorsys.datasafe.simple.adapter.api.exceptions.SimpleAdapterException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class DocumentFQN {
    private String value;

    public DocumentFQN(String s) {
        if (s == null) {
            throw new SimpleAdapterException("DocumentFQN must not be null");
        }

        s = s.replaceAll("//+", "/");
        if (s.substring(s.length() - 1).equals("/")) {
            value = s.substring(0, s.length() - 1);
        } else {
            value = s;
        }

    }
}
