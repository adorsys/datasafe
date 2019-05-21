//package de.adorsys.datasafe.business.impl.encryption.keystore.generator;
//
//import lombok.SneakyThrows;
//import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
//import org.bouncycastle.cert.X509CertificateHolder;
//
//import java.security.KeyFactory;
//import java.security.Provider;
//import java.security.PublicKey;
//import java.security.spec.X509EncodedKeySpec;
//
//public class PublicKeyUtils {
//
//    @SneakyThrows
//    public static PublicKey getPublicKey(X509CertificateHolder certificateHolder, Provider provider) {
//        if (certificateHolder == null) return null;
//        SubjectPublicKeyInfo subjectPublicKeyInfo = certificateHolder.getSubjectPublicKeyInfo();
//        X509EncodedKeySpec x509EncodedKeySpec;
//        x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
//        return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
//    }
//
//    @SneakyThrows
//    public static PublicKey getPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Provider provider)  {
//        if (subjectPublicKeyInfo == null) return null;
//        X509EncodedKeySpec x509EncodedKeySpec;
//        x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
//        return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
//    }
//
//    @SneakyThrows
//    public static PublicKey getPublicKeySilent(X509CertificateHolder certificateHolder, Provider provider) {
//        if (certificateHolder == null) return null;
//        SubjectPublicKeyInfo subjectPublicKeyInfo = certificateHolder.getSubjectPublicKeyInfo();
//        X509EncodedKeySpec x509EncodedKeySpec;
//        x509EncodedKeySpec = new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
//        return KeyFactory.getInstance(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId(), provider).generatePublic(x509EncodedKeySpec);
//    }
//}
