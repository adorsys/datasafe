package de.adorsys.datasafe.business.impl.encryption.pathencryption.encryption;

import de.adorsys.datasafe.business.impl.encryption.keystore.generator.KeyPairGeneratorImpl;

public class ExtendedKeyPairGeneratorImpl extends KeyPairGeneratorImpl {
    public ExtendedKeyPairGeneratorImpl(String keyAlgo, Integer keySize, String serverSigAlgo, String serverKeyPairName) {
        super(keyAlgo, keySize, serverSigAlgo, serverKeyPairName);
    }

    public void setDayAfter(Integer dayAfter) {
        this.daysAfter = dayAfter;
    }

    public void setWithCA(Boolean ca) {
        this.withCA = ca;
    }
}
