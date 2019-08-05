package de.adorsys.datasafe.rest.impl.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StorageCredsDTO {

    @NotBlank
    private String storageRegexMatcher;

    @NotNull
    private String username;

    @NotNull
    private String password;
}
