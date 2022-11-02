package net.shinton.fortune.factory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an in-memory keystore used to pass to interfaces that requires a
 * keystore instead of accepting loose certs and keys (e.g. Javalin).
 */
public class KeystoreFactory {
  private static final Logger log = LoggerFactory.getLogger(KeystoreFactory.class);

  /**
   * Creates a new keystore for use with a server
   *
   * @param key Private key in PKCS #8 format that has been Base64 encoded
   * @param certs X.509 certificate chain in PEM format that has been Base64 encoded
   * @return A new keystore or null if there was a problem.
   */
  public KeyStore newServerKeystore(String key, String certs) {
    if (StringUtils.isBlank(key) || StringUtils.isBlank(certs)) {
      return null;
    }

    return newKeystore(key, certs);
  }

  /**
   * Creates a new keystore for use with a client
   *
   * @param certs X.509 certificate chain in PEM format that has been Base64 encoded
   * @return A new keystore or null if there was a problem.
   */
  public KeyStore newClientKeystore(String certs) {
    if (StringUtils.isBlank(certs)) {
      return null;
    }

    // Clients don't need private keys
    return newKeystore(null, certs);
  }

  /**
   * Create a new keystore containing the supplied key and certs.
   *
   * @param key Private key in PKCS #8 format that has been Base64 encoded
   * @param certs X.509 certificate chain in PEM format that has been Base64 encoded
   * @return The generated {@link KeyStore}
   */
  private KeyStore newKeystore(String key, String certs) {
    try {
      // Get the certificates and private key
      RSAPrivateKey privateKey = decodePrivateKey(key);
      List<X509Certificate> certificates = decodeCertificates(certs);

      // Create an empty keystore
      KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      keystore.load(null, null);

      // Load the keystore
      if (!certificates.isEmpty()) {
        int idx = 0;
        for (X509Certificate certificate : certificates) {
          keystore.setCertificateEntry("cert" + idx++, certificate);
        }
      }

      if (privateKey != null) {
        keystore.setKeyEntry("key", privateKey, null, certificates.toArray(new X509Certificate[certificates.size()]));
      }

      return keystore;

    } catch (Exception e) {
      log.error("Could not create keystore", e);
    }

    return null;
  }

  /**
   * Method used to parse the text of a private key and convert it into a
   * usable object.
   *
   * @param key The key to parse
   * @return The RSA private key
   * @throws IOException There is a problem parsing the text
   * @throws NoSuchAlgorithmException The key is in an unsupported format
   * @throws InvalidKeySpecException The key is malformed
   */
  private RSAPrivateKey decodePrivateKey(String key) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    if (key == null) {
      return null;
    }

    // Base64 decode the key and prepare it for reading
    String decodedKey = new String(Base64.getDecoder().decode(key), StandardCharsets.UTF_8);
    BufferedReader reader = new BufferedReader(new StringReader(decodedKey));

    // Iterate over the file looking for where the private key begins and ends
    PKCS8EncodedKeySpec keySpec = null;
    StringBuilder collectedKey = null;
    String line;

    while ((line = readTrimmedLine(reader)) != null) {
      if ("-----BEGIN PRIVATE KEY-----".equals(line)) {
        collectedKey = new StringBuilder();
        continue;
      }

      if ("-----END PRIVATE KEY-----".equals(line) && (collectedKey != null)) {
        // We should have the entire key in the buffer now
        keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(collectedKey.toString()));
        break;
      }

      if (collectedKey != null) {
        collectedKey.append(line);
      }
    }

    if (keySpec == null) {
      return null;
    }

    // Convert the text key into an RSAPrivateKey object
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
  }

  /**
   * Method used to parse the text of a certificate PEM and convert it into
   * usable objects.
   *
   * @param certificates The certificates to parse
   * @return A list of certificates in the text
   * @throws IOException There is a problem parsing the text
   * @throws CertificateException The certificate is malformed
   */
  private List<X509Certificate> decodeCertificates(String certificates) throws IOException, CertificateException {
    if (certificates == null) {
      return null;
    }

    // Base64 decode the certificates and prepare them for reading
    String decodedCertificates = new String(Base64.getDecoder().decode(certificates), StandardCharsets.UTF_8);
    BufferedReader reader = new BufferedReader(new StringReader(decodedCertificates));

    // Iterate over the file looking for where the certificate begins and ends
    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    List<X509Certificate> certificateList = new ArrayList<>();
    StringBuilder collectedCertificate = null;
    String line;

    while ((line = readTrimmedLine(reader)) != null) {
      if ("-----BEGIN CERTIFICATE-----".equals(line)) {
        collectedCertificate = new StringBuilder();
        continue;
      }

      if ("-----END CERTIFICATE-----".equals(line) && (certificateList != null)) {
        // We should have the entire certificate in the buffer
        certificateList.add((X509Certificate) factory.generateCertificate(
            new ByteArrayInputStream(Base64.getDecoder().decode(collectedCertificate.toString()))));
      }

      if (collectedCertificate != null) {
        collectedCertificate.append(line);
      }
    }

    return certificateList;
  }

  /**
   * Utility method to extract a clean line from the reader
   *
   * @param reader The reader
   * @return The clean line
   * @throws IOException There was a problem reading from the reader
   */
  private String readTrimmedLine(BufferedReader reader) throws IOException {
    String line = reader.readLine();

    if (line == null) {
      return null;
    }
    return line.trim();
  }
}
