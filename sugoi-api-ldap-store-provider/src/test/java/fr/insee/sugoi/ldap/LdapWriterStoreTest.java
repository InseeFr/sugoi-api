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
package fr.insee.sugoi.ldap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import fr.insee.sugoi.model.Application;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.store.ldap.LdapReaderStore;
import fr.insee.sugoi.store.ldap.LdapStoreBeans;
import fr.insee.sugoi.store.ldap.LdapWriterStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {EmbeddedLdapAutoConfiguration.class, LdapStoreBeans.class})
@TestPropertySource(locations = "classpath:/application.properties")
public class LdapWriterStoreTest {

  @Value("${fr.insee.sugoi.ldap.default.organizationsource:}")
  private String organizationSource;

  @Value("${fr.insee.sugoi.ldap.default.appsource:}")
  private String appSource;

  @Value("${fr.insee.sugoi.ldap.default.usersource:}")
  private String userSource;

  @Bean
  public UserStorage userStorage() {
    UserStorage us = new UserStorage();
    us.setOrganizationSource(organizationSource);
    us.setUserSource(userSource);
    us.setName("default");
    return us;
  }

  @Bean(name = "Realm")
  public Realm realm() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    realm.setUrl("localhost");
    realm.setAppSource(appSource);
    return realm;
  }

  @Autowired ApplicationContext context;

  @Test
  public void testCreateOrganization() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Organization organization = new Organization();
    organization.setIdentifiant("Titi");
    organization.addAttributes("description", "titi le test");
    ldapWriterStore.createOrganization(organization);
    assertThat(
        "Titi should have been added", ldapReaderStore.getOrganization("Titi"), not(nullValue()));
  }

  @Test
  public void testUpdateOrganizationDescription() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Organization organization = ldapReaderStore.getOrganization("amodifier");
    organization.addAttributes("description", "nouvelle description");
    ldapWriterStore.updateOrganization(organization);
    assertThat(
        "amodifier should have a new description",
        ldapReaderStore.getOrganization("amodifier").getAttributes().get("description"),
        is("nouvelle description"));
  }

  @Test
  public void testDeleteOrganization() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    ldapWriterStore.deleteOrganization("asupprimer");
    assertThat(
        "asupprimer should have been deleted",
        ldapReaderStore.getOrganization("asupprimer"),
        is(nullValue()));
  }

  @Test
  public void testCreateUser() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    User user = new User();
    user.setUsername("Titi");
    user.setLastName("Test");
    user.setFirstName("Petit");
    user.setMail("petittest@titi.fr");
    ldapWriterStore.createUser(user);
    assertThat("Titi should have been added", ldapReaderStore.getUser("Titi"), not(nullValue()));
  }

  @Test
  public void testUpdateUserMail() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    User user = ldapReaderStore.getUser("testo");
    user.setMail("nvtest@insee.fr");
    ldapWriterStore.updateUser(user);
    assertThat(
        "testc should have a new mail",
        ldapReaderStore.getUser("testo").getMail(),
        is("nvtest@insee.fr"));
  }

  @Test
  public void testDeleteUser() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    ldapWriterStore.deleteUser("byebye");
    assertThat(
        "testc should have been deleted", ldapReaderStore.getUser("byebye"), is(nullValue()));
  }

  @Test
  public void testCreateApplication() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = new Application();
    application.setName("MyApplication");
    application.setOwner("Mine");
    ldapWriterStore.createApplication(application);
    assertThat(
        "MyApplication should have been added",
        ldapReaderStore.getApplication("MyApplication"),
        not(nullValue()));
  }

  @Test
  public void testUpdateApplicationOwner() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = ldapReaderStore.getApplication("Applitest");
    application.setOwner("Mine");
    ldapWriterStore.updateApplication(application);
    assertThat(
        "Applitest should have a new owner",
        ldapReaderStore.getApplication("Applitest").getOwner(),
        is("Mine"));
  }

  @Test
  public void testDeleteApplication() {
    LdapWriterStore ldapWriterStore =
        (LdapWriterStore) context.getBean("LdapWriterStore", realm(), userStorage());
    LdapReaderStore ldapReaderStore =
        (LdapReaderStore) context.getBean("LdapReaderStore", realm(), userStorage());
    Application application = new Application();
    application.setName("EmptyApplication");
    application.setOwner("Mine");
    ldapWriterStore.createApplication(application);
    ldapWriterStore.deleteApplication("EmptyApplication");
    assertThat(
        "EmptyApplication should have been deleted",
        ldapReaderStore.getApplication("EmptyApplication"),
        is(nullValue()));
  }
}
