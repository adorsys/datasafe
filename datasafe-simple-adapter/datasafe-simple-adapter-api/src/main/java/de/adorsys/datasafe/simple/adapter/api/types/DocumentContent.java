package de.adorsys.datasafe.simple.adapter.api.types;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "value")
public class DocumentContent {
    private byte[] value;
}
