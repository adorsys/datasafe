package de.adorsys.docusafe2.business.api.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIDAuth {
    private String userID;
    private String readKeyPassword;
}
