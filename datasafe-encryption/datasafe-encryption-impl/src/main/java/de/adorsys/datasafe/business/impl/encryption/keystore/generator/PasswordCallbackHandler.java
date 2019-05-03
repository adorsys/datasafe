package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Arrays;

public final class PasswordCallbackHandler implements CallbackHandler {

	private char[] password;

	public PasswordCallbackHandler(char[] password) {
		if (password != null) {
			this.password = (char[]) password.clone();
		}
	}

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		if (!(callbacks[0] instanceof PasswordCallback)) {
			throw new UnsupportedCallbackException(callbacks[0]);
		} else {
			PasswordCallback passwordCallback = (PasswordCallback) callbacks[0];
			passwordCallback.setPassword(this.password);
		}
	}

	protected void finalize() throws Throwable {
		if (this.password != null) {
			Arrays.fill(this.password, ' ');
		}
		super.finalize();
	}

}
