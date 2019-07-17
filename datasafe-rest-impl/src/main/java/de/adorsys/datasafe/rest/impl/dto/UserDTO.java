package de.adorsys.datasafe.rest.impl.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @ApiModelProperty(value = "user name", position = 1)
    private String userName;
    @ApiModelProperty(value = "password", position = 2)
    private String password;
}
