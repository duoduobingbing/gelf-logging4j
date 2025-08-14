package biz.paluch.logging.gelf;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

public class SslCertHelper {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void generateKeystore(String keystorePath, char[] password) throws Exception {
        // 1. CA Keypair
        final KeyPair caKeyPair = generateKeyPair();

        // 2. CA Certificate
        final X509Certificate caCert = generateCertificate(
                "CN=CA Certificate,O=logstash-gelf,ST=Unknown,C=NN",
                caKeyPair,
                7300,
                null,
                null
        );

        // 3. Server Keypair
        final KeyPair serverKeyPair = generateKeyPair();

        // 4. Server Certificate signed by CA
        final X509Certificate serverCert = generateCertificate(
                "CN=localhost,O=logstash-gelf,ST=Unknown,C=NN",
                serverKeyPair,
                375,
                caKeyPair,
                caCert
        );

        // 5. Keystore mit CA + Server
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);

        ks.setKeyEntry("server", serverKeyPair.getPrivate(), password, new java.security.cert.Certificate[]{serverCert, caCert});
        ks.setCertificateEntry("ca", caCert);

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            ks.store(fos, password);
        }
    }

    public static File createTestKeystoreFile(final String password) throws Exception {
        final File tempFile = File.createTempFile("test-keystore", ".jks");

        generateKeystore(tempFile.getAbsolutePath(), password.toCharArray());

        return tempFile;
    }

    private static KeyPair generateKeyPair() throws Exception {
        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private static X509Certificate generateCertificate(
            String dn,
            KeyPair pair,
            int days,
            KeyPair caKeyPair,
            X509Certificate caCert
    ) throws Exception {

        final X500Name issuer = (caCert == null) ? new X500Name(dn) : new X500Name(caCert.getSubjectX500Principal().getName());
        final X500Name subject = new X500Name(dn);

        final PublicKey publicKey = pair.getPublic();
        final PrivateKey signingKey = (caKeyPair == null) ? pair.getPrivate() : caKeyPair.getPrivate();

        final Date from = new Date();
        final Date to = new Date(from.getTime() + days * 86400000L);
        final BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

        final X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer,
                serial,
                from,
                to,
                subject,
                publicKey
        );

        final ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(signingKey);
        final X509CertificateHolder holder = certBuilder.build(signer);
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }
}
