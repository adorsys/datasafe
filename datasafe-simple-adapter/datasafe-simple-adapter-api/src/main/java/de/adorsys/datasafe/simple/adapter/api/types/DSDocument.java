package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DSDocument {
    private final DocumentFQN documentFQN;
    private final DocumentContent documentContent;
}
