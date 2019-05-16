package de.adorsys.datasafe.rest.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateRequestDTO {
    private String userName;
    private String password;
}