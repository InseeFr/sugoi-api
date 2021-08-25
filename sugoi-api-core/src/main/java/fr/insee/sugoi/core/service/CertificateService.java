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
package fr.insee.sugoi.core.service;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.springframework.web.multipart.MultipartFile;

public interface CertificateService {

  /**
   * Extrait le certificat d'un fichier
   *
   * @param file : le fichier proposé par l'utilisateur pour en extraire un certificat
   * @return le certificat s'il est correct et qu'il correspond bien à un certificat client.
   * @throws CertificateException si le certificat n'est pas correctement construit ou que ce n'est
   *     pas un certificat client auquel cas l'erreur est plus spécifique :
   *     PasUnCertificatClientException. S'il est en plus détecté que c'est une autorité de
   *     confiance l'exception est encore plus spécifique : PasUnCertificatClientCarACException
   * @throws IOException si le fichier proposé est introuvable
   */
  public X509Certificate getCertificateClientFromMultipartFile(MultipartFile file)
      throws CertificateException, IOException;

  public X509Certificate getCertificateFromByte(byte[] bytes) throws CertificateException;

  /**
   * Vérifie si le fichier fourni est un certificat client.
   *
   * @param file le fichier à vérifier
   * @return true si le fichier est un certificat client ou faux si le fichier n'est pas trouvé, si
   *     ce n'est pas un certificat ou si c'est un certificat mais pas client
   */
  public boolean verifierCertificatClientFromMultipartFile(MultipartFile file);

  /**
   * @param file : le fichier proposé par l'utilisateur pour en extraire un certificat
   * @return le certificat s'il est correct.
   * @throws CertificateException si le certificat n'est pas correctement construit
   * @throws IOException si le fichier proposé est introuvable
   */
  public X509Certificate getCertificateFromMultipartFile(MultipartFile file)
      throws CertificateException, IOException;

  /**
   * Le certificat renseigné doit être de type certificat client. Il faut s'assurer que le
   * certificat n'est pas par exemple celui de l'autorité de confiance qui l'a signé.
   *
   * <p>Vérifie le type du certificat à l'aide de l'attribut KeyUsage qui suit la RFC 3280.
   *
   * <p>Gets a boolean array representing bits of the KeyUsage extension, (OID = 2.5.29.15). The key
   * usage extension defines the purpose (e.g., encipherment, signature, certificate signing) of the
   * key contained in the certificate. The ASN.1 definition for this is: KeyUsage ::= BIT STRING {
   * digitalSignature (0), nonRepudiation (1), keyEncipherment (2), dataEncipherment (3),
   * keyAgreement (4), keyCertSign (5), cRLSign (6), encipherOnly (7), decipherOnly (8) }
   *
   * <p>RFC 3280 recommends that when used, this be marked as a critical extension.Returns:the
   * KeyUsage extension of this certificate, represented as an array of booleans. The order of
   * KeyUsage values in the array is the same as in the above ASN.1 definition. The array will
   * contain a value for each KeyUsage defined above. If the KeyUsage list encoded in the
   * certificate is longer than the above list, it will not be truncated. Returns null if this
   * certificate does not contain a KeyUsage extension.
   *
   * @param certificate le certificat à vérifier
   * @return true si c'est un certificat client
   */
  public boolean isSignatureCertificate(X509Certificate certificate);

  /**
   * Encode le certificat en der, -> digest SH256 ->getHexString. Permet d'avoir la représentation
   * LDAP du certificat.
   *
   * @param cert certificat à encoder
   * @return La représentation Ldap du certificat
   * @throws CertificateEncodingException si l'algorithme utilisé n'est pas applicable
   */
  public String encodeCertificate(X509Certificate cert) throws CertificateEncodingException;

  public byte[] getCertificateToPemFormat(byte[] cert) throws CertificateException;
}
