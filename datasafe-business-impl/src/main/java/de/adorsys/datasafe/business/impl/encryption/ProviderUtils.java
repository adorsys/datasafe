package de.adorsys.datasafe.business.impl.encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.NoSuchPaddingException;
import java.security.Provider;
import java.security.Security;

public class ProviderUtils {

	public static final Provider bcProvider;

	static {
		Security.addProvider(new BouncyCastleProvider());	
		bcProvider = Security.getProvider("BC");
		if(bcProvider==null) throw new IllegalStateException( new NoSuchPaddingException("BC"));
	}
}
