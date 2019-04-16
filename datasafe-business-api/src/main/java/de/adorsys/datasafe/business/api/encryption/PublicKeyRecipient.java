package de.adorsys.datasafe.business.api.encryption;

import java.security.PublicKey;

import lombok.Data;

@Data
public class PublicKeyRecipient {
	private byte[] keyId;
	private PublicKey publicKey;
}
