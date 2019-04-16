package de.adorsys.datasafe.business.api.encryption;

import java.util.List;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import lombok.Data;

@Data
public class EncryptionSpec {
	List<SecretKeyRecipient> secretRecipients;
	List<PublicKeyRecipient> publicRecipients;
	ASN1ObjectIdentifier encryptionAlgo;
}
