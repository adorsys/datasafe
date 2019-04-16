package de.adorsys.datasafe.business.api.encryption;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Defines an interface to encrypt, sign, decrypt and verify data streams.
 * 
 * @author fpo
 *
 */
public interface EncryptionService {

	OutputStream buildEncryptionStream(OutputStream dataStream, EncryptionSpec encryptionSpec);

	OutputStream buildSignatureStream(OutputStream dataStream, SignatureSpec signatureSpec);

	InputStream decryptingInputStream(InputStream dataStream, KeySource keySource);

	InputStream verifyingInputStream(InputStream dataStream, KeySource keySource);

}