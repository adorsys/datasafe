package de.adorsys.datasafe.rest.dto;

import lombok.Data;

@Data
public class UserCreateRequestDTO {
    private String userName;
    private String password;
}