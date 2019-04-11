package de.adorsys.datasafe.business.impl.keystore.generator;

import de.adorsys.datasafe.business.api.keystore.types.KeyEntry;
import org.bouncycastle.cert.X509CertificateHolder;

public interface TrustedCertEntry extends KeyEntry {
    X509CertificateHolder getCertificate();
}
