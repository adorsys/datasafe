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

	OutputStream buildEncryptionOutputStream(OutputStream outputStream, EncryptionSpec encryptionSpec);

	OutputStream buildSignatureOutputStream(OutputStream outputStream, SignatureSpec signatureSpec);

	InputStream buildDecryptionInputStream(InputStream inputStream, KeySource keySource);

	InputStream buildVerifyicationInputStream(InputStream inputStream, KeySource keySource);

}