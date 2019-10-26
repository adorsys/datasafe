package de.adorsys.datasafe.encrypiton.api.types;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Wrapper for the String.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class BaseTypeString implements Serializable {

    private static final long serialVersionUID = 3569239558130703592L;

    private final String value;
}
