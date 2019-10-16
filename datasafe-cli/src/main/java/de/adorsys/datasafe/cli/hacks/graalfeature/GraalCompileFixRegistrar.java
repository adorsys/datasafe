package de.adorsys.datasafe.cli.hacks.graalfeature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import de.adorsys.datasafe.cli.hacks.HackSecureRandom;
import org.graalvm.nativeimage.hosted.Feature;

import java.security.Provider;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicReference;

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
public class GraalCompileFixRegistrar implements Feature {

    private static final String PROVIDER_ACCESS_LOGGER = "PROVIDER_ACCESS_LOGGER";
    private static final AtomicReference random = new AtomicReference();

    @Override
    public void duringSetup(DuringSetupAccess access) {
        System.setProperty("RUMBA", "ZUMBA");

        access.registerObjectReplacer(orig -> {
            if (orig instanceof SecureRandom) {
                System.out.println("??? !!");
                random.compareAndSet(null, new HackSecureRandom());
                return random.get();
            }

            return orig;
        });
    }
}
