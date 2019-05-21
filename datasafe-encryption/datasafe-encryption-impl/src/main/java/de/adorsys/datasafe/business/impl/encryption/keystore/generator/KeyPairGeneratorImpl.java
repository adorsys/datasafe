package de.adorsys.datasafe.business.impl.encryption.keystore.generator;

import de.adorsys.datasafe.business.api.types.keystore.ReadKeyPassword;
import de.adorsys.datasafe.business.impl.encryption.keystore.types.KeyPairGenerator;
import de.adorsys.datasafe.business.impl.encryption.keystore.types.SelfSignedKeyPairData;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.KeyUsage;

import javax.security.auth.callback.CallbackHandler;
import java.security.KeyPair;

public class KeyPairGeneratorImpl implements KeyPairGenerator {

    private final static int[] keyUsageSignature = {KeyUsage.nonRepudiation};
    private final static int[] keyUsageEncryption = {KeyUsage.keyEncipherment, KeyUsage.dataEncipherment, KeyUsage.keyAgreement};

    private final String keyAlgo;
    private final Integer keySize;
    private final String serverSigAlgo;
    private final String serverKeyPairName;
    protected Integer daysAfter = 900;
    protected Boolean withCA = false;

    public KeyPairGeneratorImpl(
            String keyAlgo,
            Integer keySize,
            String serverSigAlgo,
            String serverKeyPairName) {
        this.keyAlgo = keyAlgo;
        this.keySize = keySize;
        this.serverSigAlgo = serverSigAlgo;
        this.serverKeyPairName = serverKeyPairName;
    }

    public KeyPairData generateSignatureKey(String alias, ReadKeyPassword readKeyPassword) {
        return generate(keyUsageSignature, alias, readKeyPassword);
    }

    public KeyPairData generateEncryptionKey(String alias, ReadKeyPassword readKeyPassword) {
        return generate(keyUsageEncryption, alias, readKeyPassword);
    }

    private KeyPairData generate(int[] keyUsages, String alias, ReadKeyPassword readKeyPassword) {
        KeyPair keyPair = new KeyPairBuilder().withKeyAlg(keyAlgo).withKeyLength(keySize).build();
        X500Name dn = new X500NameBuilder(BCStyle.INSTANCE).addRDN(BCStyle.CN, serverKeyPairName).build();
        SelfSignedKeyPairData keyPairData = new SingleKeyUsageSelfSignedCertBuilder()
                .withSubjectDN(dn)
                .withSignatureAlgo(serverSigAlgo)
                .withNotAfterInDays(daysAfter)
                .withCa(withCA)
                .withKeyUsages(keyUsages)
                .build(keyPair);
        return KeyPairData.builder().keyPair(keyPairData).alias(alias).readKeyPassword(readKeyPassword).build();
    }
}
