/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.core.service.impl;

import fr.insee.sugoi.core.service.CertificateService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CertificateServiceImpl implements CertificateService {

  public static final Logger logger = LoggerFactory.getLogger(CertificateServiceImpl.class);

  private static final int EMPLACEMENT_KEY_USAGE_SIGNATURE = 0;
  private static final int EMPLACEMENT_KEY_USAGE_AC = 5;
  public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  public static final String END_CERT = "-----END CERTIFICATE-----";
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @Override
  public X509Certificate getCertificateClientFromMultipartFile(MultipartFile file)
      throws CertificateException, IOException {
    X509Certificate cert = getCertificateFromMultipartFile(file);
    if (!isSignatureCertificate(cert)) {
      if (isAcCertificate(cert)) {
        throw new RuntimeException("Le certifcat proposé est celui d'une autorité de confiance");
      }
      throw new RuntimeException("Le certificat ne peux pas être utilisé comme certificat client");
    }
    return cert;
  }

  @Override
  public boolean verifierCertificatClientFromMultipartFile(MultipartFile file) {
    try {
      X509Certificate cert = getCertificateFromMultipartFile(file);
      return isSignatureCertificate(cert);
    } catch (CertificateException | IOException e) {
      return false;
    }
  }

  @Override
  public X509Certificate getCertificateFromMultipartFile(MultipartFile file)
      throws CertificateException, IOException {
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(file.getInputStream());
    logger.debug("Certificat importé : " + cert);
    return cert;
  }

  @Override
  public X509Certificate getCertificateFromByte(byte[] bytes) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X509");
    return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(bytes));
  }

  @Override
  public boolean isSignatureCertificate(X509Certificate certificate) {
    try {
      if (certificate.getKeyUsage() != null) {
        return certificate.getKeyUsage()[EMPLACEMENT_KEY_USAGE_SIGNATURE];
      } else if (certificate.getExtendedKeyUsage() != null) {
        // Si le champs KeyUsage normalement obligatoire n'est pas pas
        // présent on tolère la seule présence de extended key usage
        // OID 1.3.6.1.5.5.7.3.2 = utilisation client
        return certificate.getExtendedKeyUsage().contains("1.3.6.1.5.5.7.3.2");
      } else {
        // Dans le doute on refuse le certificat, on verra si le cas se
        // présente un jour
        return false;
      }
    } catch (CertificateParsingException e) {
      // Se produit si le champ OBLIGATOIRE keyusage est absent et que le
      // champ extended key usage est présent mais indéchiffrable. On a
      // donc pas de moyen de vérifier que le certificat est client
      return false;
    }
  }

  @Override
  public String encodeCertificate(X509Certificate cert) throws CertificateEncodingException {
    byte[] encodedDer = cert.getEncoded();
    MessageDigest digestSha256;
    try {
      digestSha256 = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new CertificateEncodingException("L'algorithme de hash n'existe pas \\n " + e);
    }
    digestSha256.update(encodedDer);
    byte[] hash256 = new byte[digestSha256.getDigestLength()];
    hash256 = digestSha256.digest();
    return getHexString(hash256);
  }

  /**
   * Récupérer le Hex String d'une suite de byte.
   *
   * @param bytes la suite de bytes
   * @return the hexString
   */
  private static String getHexString(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private boolean isAcCertificate(X509Certificate certificate) {
    if (certificate.getKeyUsage() != null) {
      return certificate.getKeyUsage()[EMPLACEMENT_KEY_USAGE_AC];
    } else {
      return false;
    }
  }

  @Override
  public byte[] getCertificateToPemFormat(byte[] cert) throws CertificateException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(cert);
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(inputStream);
    Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
    String encodedCertText = new String(encoder.encode(certificate.getEncoded()));
    String prettified_cert =
        BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
    return prettified_cert.getBytes();
  }
}
