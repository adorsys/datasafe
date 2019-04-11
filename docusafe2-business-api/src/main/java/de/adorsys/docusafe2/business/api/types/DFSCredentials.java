package de.adorsys.docusafe2.business.api.types;

import lombok.Data;

@Data
public class DFSCredentials { //change to interface
    //TODO: change to proper objects instead of string
    private String s3accessKey;
    private String secret;

}
