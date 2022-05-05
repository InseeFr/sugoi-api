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
package fr.insee.sugoi.ldap.utils.mapper;

import com.unboundid.ldap.sdk.Attribute;
import fr.insee.sugoi.ldap.utils.config.LdapConfigKeys;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.InvalidCertificateException;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLdapMapper extends LdapMapper<User> {

  public UserLdapMapper(Map<String, String> config, List<StoreMapping> mappings) {
    this.config = config;
    if (config.get(LdapConfigKeys.USER_OBJECT_CLASSES) != null) {
      objectClasses = Arrays.asList(config.get(LdapConfigKeys.USER_OBJECT_CLASSES).split(","));
    }
    this.mappings = mappings;
  }

  @Override
  public User mapFromAttributes(Collection<Attribute> attributes) {
    User user =
        GenericLdapMapper.mapLdapAttributesToObject(attributes, User.class, config, mappings);
    if (user.getCertificate() != null) {
      try {
        user.addMetadatas("cert", getParsedCertMetadatas(user));
      } catch (CertificateException e) {
        throw new InvalidCertificateException();
      }
    }
    return user;
  }

  private Map<String, String> getParsedCertMetadatas(User user) throws CertificateException {
    Map<String, String> certDetails = new HashMap<>();
    X509Certificate myCert =
        (X509Certificate)
            CertificateFactory.getInstance("X509")
                .generateCertificate(new ByteArrayInputStream(user.getCertificate()));
    certDetails.put("expiration", myCert.getNotAfter().toString());
    certDetails.put("issuer", myCert.getIssuerX500Principal().getName());
    certDetails.put("subject", myCert.getSubjectX500Principal().getName());
    if (user.getAttributes().containsKey("properties")
        && user.getAttributes().get("properties") instanceof List) {
      ((List<?>) user.getAttributes().get("properties"))
          .stream()
              .filter(
                  entry -> entry instanceof String && ((String) entry).contains("certificateId$"))
              .findFirst()
              .ifPresent(
                  certificateIdEntry ->
                      certDetails.put("id", ((String) certificateIdEntry).split("\\$")[1]));
    }
    try {
      myCert.checkValidity();
      certDetails.put("isValid", "true");
    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
      certDetails.put("isValid", "false");
    }
    return certDetails;
  }
}
