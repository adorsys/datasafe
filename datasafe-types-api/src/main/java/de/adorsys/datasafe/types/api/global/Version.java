package de.adorsys.datasafe.types.api.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class carries logical Datasafe version (i.e. dictates user profile structure) so that application can
 * detect incompatibilities between old data and new code and act accordingly.
 * Can be extended and used by Datasafe client for identifying different profiles/keystores configurations
 */
@Getter
@RequiredArgsConstructor
public class Version {

    /**
     * The first version before major changes happened.
     */
    public static Version BASELINE = new Version("v0");

    private final String id;

    public static Version current() {
        return BASELINE;
    }
}
