package de.adorsys.datasafe.rest.impl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StorageCredsDTO {

    @NotBlank
    private String storageRegexMatcher;

    @NotNull
    private String username;

    @NotNull
    private String password;
}
