package de.adorsys.datasafe.business.api.encryption;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

import de.adorsys.datasafe.business.api.keystore.types.PublicKeyIDWithPublicKey;
import de.adorsys.datasafe.business.api.keystore.types.SecretKeyIDWithKey;
import lombok.Data;

@Data
public class EncryptionSpec {
	List<SecretKeyIDWithKey> secretRecipients = new ArrayList<>();
	List<PublicKeyIDWithPublicKey> publicRecipients = new ArrayList<>();
	ASN1ObjectIdentifier encryptionAlgo;
	public EncryptionSpec(ASN1ObjectIdentifier encryptionAlgo) {
		this.encryptionAlgo = encryptionAlgo;
	}
}
