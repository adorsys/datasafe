package de.adorsys.datasafe.cli.hacks.graalfeature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import de.adorsys.datasafe.cli.hacks.NoOpRandom;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.graalvm.nativeimage.hosted.Feature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.Provider;

/**
 * This class fixes NPE exception in Graal-compilator - when it tries to get non-existing engines from
 * {@link Provider}
 * <p>
 * Additionally can log access to null service types using property PROVIDER_ACCESS_LOGGER,
 * so you can add necessary fields to extra_engines.hack. (This will break build later, so you will need
 * to remove this property when you detected all nulls in Provider).
 * <p>
 * Override string example:
 * X509Store=false,null
 */
@AutomaticFeature
public class GraalCompileFixCryptoRegistrar implements Feature {

    @Override
    @SneakyThrows
    public void afterRegistration(AfterRegistrationAccess access) {
        Field secureRandom = CryptoServicesRegistrar.class.getDeclaredField("defaultSecureRandom");
        secureRandom.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(secureRandom, secureRandom.getModifiers() & ~Modifier.FINAL);

        secureRandom.set(CryptoServicesRegistrar.class, new NoOpRandom());
    }
}
