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
package fr.insee.sugoi.core.seealso;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@ConfigurationProperties(prefix = "fr.insee.sugoi.seealso")
public class SeeAlsoCredentialsConfiguration {

  private Map<String, String> usernameByDomain = new HashMap<>();
  private Map<String, String> passwordByDomain = new HashMap<>();

  protected static final Logger logger =
      LoggerFactory.getLogger(SeeAlsoCredentialsConfiguration.class);

  public class SeeAlsoCredential {
    private String username;
    private String password;

    SeeAlsoCredential(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String getUsername() {
      return username;
    }

    public String getPassword() {
      return password;
    }
  }

  public void setPasswordByDomain(Map<String, String> passwordByDomain) {
    this.passwordByDomain = passwordByDomain;
  }

  public void setUsernameByDomain(Map<String, String> usernameByDomain) {
    this.usernameByDomain = usernameByDomain;
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
  public Map<String, SeeAlsoCredential> seeAlsoCredentialsByHost() {
    Map<String, SeeAlsoCredential> seeAlsoCredentialsByHost = new HashMap<>();
    usernameByDomain.forEach(
        (domain, username) -> {
          if (passwordByDomain.containsKey(domain))
            seeAlsoCredentialsByHost.put(
                domain, new SeeAlsoCredential(username, passwordByDomain.get(domain)));
        });
    return seeAlsoCredentialsByHost;
  }
}
