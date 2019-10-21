package de.adorsys.datasafe.cli.hacks.graalfeature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.Locale;
import java.util.Map;

/**
 * This class fixes NPE exception in Graal-compilator - when it tries to get non-existing engines from
 * {@link java.security.Provider}
 * <p>
 * Additionally can log access to null service types using property PROVIDER_ACCESS_LOGGER,
 * so you can add necessary fields to extra_engines.hack. (This will break build later, so you will need
 * to remove this property when you detected all nulls in Provider).
 * <p>
 * Override string example:
 * X509Store=false,null
 */
@AutomaticFeature
public class GraalCompileFixNpeOnMissingServiceTypeInKnownProviders implements Feature {

    private static final String PROVIDER_ACCESS_LOGGER = "PROVIDER_ACCESS_LOGGER";

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream is = classloader.getResourceAsStream("extra_engines.hack");
             InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            reader.lines().forEach(it -> {
                System.out.println("Overriding " + it);
                String[] typeAndValue = it.split("=");
                String[] params = typeAndValue[1].split(",");
                addEngine(typeAndValue[0], params[0], params[1]);
            });
        } catch (IOException ex) {
            System.out.println("Failed to read resource - extra_engines.hack " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    private void addEngine(String name, String sp, String paramNam) {
        try {
            addEngineInternal(name, sp, paramNam);
        } catch (ReflectiveOperationException ex) {
            System.out.println("Reflective access error " + ex.getMessage());
            ex.printStackTrace();
            throw new IllegalStateException("Failed");
        }
    }

    private void addEngineInternal(String name, String sp, String paramNam) throws
            NoSuchFieldException,
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {
        Field knownEngines = Provider.class.getDeclaredField("knownEngines");
        knownEngines.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(knownEngines, knownEngines.getModifiers() & ~Modifier.FINAL);

        Class<?> engineDescription = Class.forName("java.security.Provider$EngineDescription");

        Constructor<?> ctor = engineDescription.getDeclaredConstructor(String.class, boolean.class, String.class);
        ctor.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> originalEngine = (Map<String, Object>) knownEngines.get(null);

        Map<String, Object> delegate = null != System.getProperty(PROVIDER_ACCESS_LOGGER)
                ? new EngineDelegate(originalEngine) : originalEngine;

        knownEngines.set(Map.class, delegate);

        Object engineDescInstance = ctor.newInstance(
                name,
                Boolean.parseBoolean(sp),
                "null".equals(paramNam) ? null : paramNam
        );

        delegate.put(name.toLowerCase(Locale.ENGLISH), engineDescInstance);
        delegate.put(name, engineDescInstance);
    }

    @RequiredArgsConstructor
    private static class EngineDelegate implements Map<String, Object> {

        @Delegate(excludes = Get.class)
        private final Map<String, Object> delegate;


        @Override
        public Object get(Object key) {
            Object value = delegate.get(key);

            if (null == value) {
                System.out.println("Detected access to null value in Provider.knownEngines for type " + key);
            }

            return value;
        }

        private interface Get {
            Object get(Object key);
        }
    }
}
