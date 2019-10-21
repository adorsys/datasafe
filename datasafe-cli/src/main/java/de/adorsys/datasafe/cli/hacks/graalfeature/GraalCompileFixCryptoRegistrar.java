package de.adorsys.datasafe.cli.hacks.graalfeature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import de.adorsys.datasafe.cli.hacks.NoOpRandom;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.graalvm.nativeimage.hosted.Feature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;

/**
 * Bouncy Castle attempts to access SecureRandom in {@link CryptoServicesRegistrar} class, during CLI
 * generation. Because it is not allowed to have NativePRNG in image heap this breaks compilation.
 * We fix this by setting {@code CryptoServicesRegistrar#defaultSecureRandom} to {@link NoOpRandom} that
 * is simply stub class. Then, in runtime in main() CLI simply sets {@code defaultSecureRandom} to null
 * before doing any useful work and such trick forces CryptoServicesRegistrar to get correct {@link SecureRandom}
 * implementation, because when it is null BC initializes the field.
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
