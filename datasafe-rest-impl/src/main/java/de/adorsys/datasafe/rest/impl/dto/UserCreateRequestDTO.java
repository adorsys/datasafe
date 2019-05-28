package de.adorsys.datasafe.rest.impl.dto;

import lombok.Data;

@Data
public class UserCreateRequestDTO {

    private String userName;
    private String password;
}
