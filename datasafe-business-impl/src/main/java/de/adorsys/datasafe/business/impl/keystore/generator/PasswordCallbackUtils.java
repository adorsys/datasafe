package de.adorsys.datasafe.business.impl.keystore.generator;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class PasswordCallbackUtils {
	public static char[] getPassword(CallbackHandler callbackHandler, String name) {
		PasswordCallback callback = new PasswordCallback(name, false);
		try {
			callbackHandler.handle(new Callback[]{callback});
		} catch (IOException | UnsupportedCallbackException e) {
			throw new IllegalStateException(e);
		}
		char[] password = callback.getPassword();
		callback.clearPassword();
		return password;
	}
}
