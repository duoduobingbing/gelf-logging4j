package io.github.duoduobingbing.gelflogging4j.gelf.test.helper;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Ports the initial test script written by mp911de under test/bash
 *
 * @author TKtiki
 * @author duoduobingbing
 */
public class IntegrationTestSslCertHelper {

    static {
        if(null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)){
            Security.addProvider(new BouncyCastleProvider());
        }

    }

    public static byte[] generateKeystore(String password) throws Exception {
        // 1. Create CA Keypair
        final KeyPair caKeyPair = generateRsa2048KeyPair();

        // 2. Create CA Certificate from CA Keypair
        final X509Certificate caCert = generateCaCertificate(caKeyPair);

        // 3. Create Server Keypair
        final KeyPair serverKeyPair = generateRsa2048KeyPair();

        // 4. Create Server CSR
        final PKCS10CertificationRequest serverCsr = generateServerCsr(serverKeyPair);

        // 5. Create Server Certificate signed by CA
        final X509Certificate serverCert = generateServerCertificate(caCert, caKeyPair, serverCsr);

        // 6. Keystore with CA + Server
        final byte[] pkcs12KeyStore = generatePkcs12KeyStore(password, serverKeyPair.getPrivate(), caCert, serverCert);

        return pkcs12KeyStore;
    }

    private static KeyPair generateRsa2048KeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    @SuppressWarnings("checkstyle:illegaltype")
    private static X509Certificate generateServerCertificate(X509Certificate caCert, KeyPair caKeyPair, PKCS10CertificationRequest serverCsr) throws CertificateException, IOException, OperatorCreationException {
        ZonedDateTime notBefore = ZonedDateTime.now();
        ZonedDateTime notAfter = notBefore.plusDays(375);

        Date notBeforeAsDate = Date.from(notBefore.toInstant());
        Date notAfterAsDate = Date.from(notAfter.toInstant());

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME);
        PublicKey publicKey = converter.getPublicKey(serverCsr.getSubjectPublicKeyInfo());

        X509v3CertificateBuilder serverCertBuilder = new JcaX509v3CertificateBuilder(
                caCert,
                BigInteger.valueOf(1001),
                notBeforeAsDate,
                notAfterAsDate,
                new X500Name("CN=localhost,O=gelf-logging4j,ST=Unknown,L=Unknown,C=NN"),
                publicKey
        );

        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME);

        ContentSigner serverCertSigner = signerBuilder.build(caKeyPair.getPrivate());

        // Add server_cert extensions
        X509Certificate serverCert = new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(serverCertBuilder.build(serverCertSigner));

        return serverCert;
    }

    @SuppressWarnings("checkstyle:illegaltype") /*ignore Date b/c there is no method with modern Instant in BouncyCastle for that*/
    private static X509Certificate generateCaCertificate(
            KeyPair caKeyPair
    ) throws CertificateException, OperatorCreationException, CertIOException, NoSuchAlgorithmException {
        X500Name issuer = new X500Name("CN=CA Certificate, O=gelf-logging4j, C=NN, ST=Unknown, L=Unknown");
        BigInteger serial = BigInteger.valueOf(1000);

        ZonedDateTime notBefore = ZonedDateTime.now();
        ZonedDateTime notAfter = notBefore.plusDays(7300);

        Date notBeforeAsDate = Date.from(notBefore.toInstant());
        Date notAfterAsDate = Date.from(notAfter.toInstant());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serial, notBeforeAsDate, notAfterAsDate, issuer, caKeyPair.getPublic()
        );

        // <editor-fold defaultstate="collapsed" desc="Emulate x509_extensions=v3_ca">
        // Add Basic Constraints: CA=true
        certBuilder.addExtension(
                Extension.basicConstraints,
                true, // critical
                new BasicConstraints(true) // true = is CA
        );

        // Add Key Usage: digitalSignature, cRLSign, keyCertSign
        certBuilder.addExtension(
                Extension.keyUsage,
                true, // critical
                new KeyUsage(KeyUsage.digitalSignature | KeyUsage.cRLSign | KeyUsage.keyCertSign)
        );

        // Add Subject Key Identifier
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBuilder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(caKeyPair.getPublic())
        );

        // Add Authority Key Identifier
        certBuilder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic())
        );
        // </editor-fold>


        final ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME).build(caKeyPair.getPrivate());
        final X509CertificateHolder holder = certBuilder.build(signer);
        return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);

    }

    private static PKCS10CertificationRequest generateServerCsr(KeyPair serverKeyPair) throws OperatorCreationException {
        PKCS10CertificationRequestBuilder csrBuilder = new JcaPKCS10CertificationRequestBuilder(
                new X500Principal("C=NN, ST=Unknown, L=Unknown, O=gelf-logging4j, CN=localhost"),
                serverKeyPair.getPublic()
        );
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider.PROVIDER_NAME);
        PrivateKey serverPrivateKey = serverKeyPair.getPrivate();
        ContentSigner serverCsrSigner = signerBuilder.build(serverPrivateKey);
        PKCS10CertificationRequest csr = csrBuilder.build(serverCsrSigner);
        return csr;
    }

    private static byte[] generatePkcs12KeyStore(
            String password,
            PrivateKey serverPrivateKey,
            X509Certificate caCert,
            X509Certificate serverCert
    ) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyStore pkcs12 = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        pkcs12.load(null, null); //Initialize empty
        pkcs12.setKeyEntry(
                "gelf-logging4j",
                serverPrivateKey,
                password.toCharArray(),
                new Certificate[]{serverCert, caCert}
        );

        pkcs12.setCertificateEntry("ca", caCert);

        byte[] pkcs12KeyStore;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            pkcs12.store(bos, password.toCharArray());
            pkcs12KeyStore = bos.toByteArray();
        }

        return pkcs12KeyStore;
    }
}
