package de.adorsys.datasafe.business.api.encryption;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

/**
 * Interface providing read access to key materials.
 * 
 * @author fpo
 *
 */
public interface KeySource {

	/**
	 * Finds the matching public key.
	 * 
	 * @param subjectKeyId : the identifier of the public key to be retrieved.
	 * 
	 * @return
	 */
	PublicKey findPublicKey(byte[] subjectKeyId);
	
	/**
	 * Finds the private key matching the given subjectKey identifier.
	 * 
	 * @param subjectKeyId
	 * @return
	 */
	PrivateKey findPrivateKey(byte[] subjectKeyId);
	
	/**
	 * Finds the select key matching the given key identifier.
	 * 
	 * @param keyIdentifier
	 * @return
	 */
	SecretKey findSecretKey(byte[] keyIdentifier);

}
