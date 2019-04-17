package de.adorsys.datasafe.business.api.deployment.desired;

import lombok.Data;

@Data
public class DSDocument {

    private final DocumentFQN path;
    private final byte[] content;
}
