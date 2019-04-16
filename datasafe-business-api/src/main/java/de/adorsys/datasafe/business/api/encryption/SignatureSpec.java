package de.adorsys.datasafe.business.api.encryption;

import java.security.PrivateKey;

import lombok.Data;

@Data
public class SignatureSpec {
	private byte[] keyId;
	private PrivateKey privateKey;
	private String signatureAlgorithm;
	
}
