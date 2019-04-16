package de.adorsys.datasafe.business.api.encryption;

import javax.crypto.SecretKey;

import lombok.Data;

@Data
public class SecretKeyRecipient {
	private byte[] keyId;
	private SecretKey secretKey;
}
