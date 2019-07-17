package de.adorsys.datasafe.rest.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewPasswordDTO {

    @NotBlank
    private String newPassword;
}
