package de.adorsys.datasafe.business.api.types;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class UserId {

    private final String id;
}
