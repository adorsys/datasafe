package de.adorsys.datasafe.business.api.version.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class BaseTypeString implements Serializable {

    private static final long serialVersionUID = 3569239558130703592L;

    private final String value;
}
