package de.adorsys.datasafe.simple.adapter.impl.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PathEncryptionConfig {
    private final Boolean withPathEncryption;
}
