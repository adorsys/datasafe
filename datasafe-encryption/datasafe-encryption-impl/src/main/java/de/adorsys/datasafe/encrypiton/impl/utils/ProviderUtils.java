package de.adorsys.datasafe.encrypiton.impl.utils;

import de.adorsys.keymanagement.adapter.modules.generator.GeneratorModule_ProviderFactory;
import lombok.experimental.UtilityClass;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

@UtilityClass
public class ProviderUtils {

    public static final Provider bcProvider = GeneratorModule_ProviderFactory.provider();
}
