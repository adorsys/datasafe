package de.adorsys.datasafe.encrypiton.impl.utils;

import de.adorsys.keymanagement.adapter.modules.generator.GeneratorModule_ProviderFactory;
import lombok.experimental.UtilityClass;

import java.security.Provider;

@UtilityClass
public class ProviderUtils {

    public static final Provider bcProvider = GeneratorModule_ProviderFactory.provider();
}
