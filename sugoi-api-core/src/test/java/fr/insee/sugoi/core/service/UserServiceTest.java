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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.event.configuration.EventKeysConfig;
import fr.insee.sugoi.core.event.publisher.SugoiEventPublisher;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.ProviderResponse.ProviderResponseStatus;
import fr.insee.sugoi.core.realm.RealmProvider;
import fr.insee.sugoi.core.service.impl.UserServiceImpl;
import fr.insee.sugoi.core.store.ReaderStore;
import fr.insee.sugoi.core.store.StoreProvider;
import fr.insee.sugoi.core.store.WriterStore;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.exceptions.NoCertificateOnUserException;
import fr.insee.sugoi.model.exceptions.RealmNotFoundException;
import fr.insee.sugoi.model.exceptions.UserAlreadyExistException;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = UserServiceImpl.class)
@TestPropertySource(locations = "classpath:/application.properties")
public class UserServiceTest {

  @MockBean private StoreProvider storeProvider;

  @MockBean private RealmProvider realmProvider;

  @MockBean private SugoiEventPublisher sugoiEventPublisher;

  @Mock private ReaderStore readerStore1;

  @Mock private ReaderStore readerStore2;

  @Mock private WriterStore writerStore;

  @Autowired private UserServiceImpl userService;

  private User user1;

  private Realm realm;

  @BeforeEach
  public void setup() {
    user1 = new User();
    user1.setUsername("Toto");
    user1.setMail("toto@insee.fr");

    User userWithCertificate = new User("UserWithCertificate");
    userWithCertificate.setCertificate(
        Base64.getDecoder()
            .decode(
                "MIIDJDCCAgwCCQDzaF9oNeXFKTANBgkqhkiG9w0BAQsFADBUMQswCQYDVQQGEwJG"
                    + "UjEOMAwGA1UECAwFUGFyaXMxDjAMBgNVBAoMBUluc2VlMRIwEAYDVQQLDAlVbml0"
                    + "IFRlc3QxETAPBgNVBAMMCEpvaG4gRG9lMB4XDTIxMTExOTE3NDUxMloXDTIyMTEx"
                    + "OTE3NDUxMlowVDELMAkGA1UEBhMCRlIxDjAMBgNVBAgMBVBhcmlzMQ4wDAYDVQQK"
                    + "DAVJbnNlZTESMBAGA1UECwwJVW5pdCBUZXN0MREwDwYDVQQDDAhKb2huIERvZTCC"
                    + "ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJ5YQ14T/YjlKwE341JrzMbQ"
                    + "58ZK6/4n3W194/txrIFMThyVMF76YxZj8qTcufqLHv6XXZtWMWupPhG2PtzhkAfL"
                    + "Cxeb+92HjKmCMRi35VvtMQn9VExmpm467tMnCoMdM50Y8FBKdvFJwDIbL48LqA11"
                    + "UyVwibyT9NPcjtd5Xr4ZOQqvoqonPbYp7Atbl1hEtVNkJNvU/W7I15u6NRzY6VvB"
                    + "UGwYR0z+/sGq3fPzEU7YQefaa1mJYKoT+A5ITDUDtT72SGU/WnYX2ShcpN6G8oWk"
                    + "BrH4DZk8r4nSGXDz6DQSwX7ssA/bHERf0oaLh/1f6zIh8HJISyzLGC998ALl2xsC"
                    + "AwEAATANBgkqhkiG9w0BAQsFAAOCAQEAHQ0p9QsU9kXMAjQKUkKgE6bGack2GzGJ"
                    + "CZEvlrOeqfYyhujtg2sdDln5Mj+fn5i1l23U7qXkzwj7aiVSAZ2tLIVmZgoLYcyi"
                    + "bP4Gjwen1vV8GmYd0XHONx6fmuuPEObl5mXKz8Eovxw9TYYMcUeZQ8gRnp+t0jfz"
                    + "5Q7ZoQVm5Nkbkz8gZpTLoOL6S8aUI0C93GzZZwYkWwrFzpsssAJk/6oz1ugUiFI2"
                    + "TZF/XgwdfQCOFjSF1NX2ED9sLsiBBvjYaavk/NO9vNH6eDTZH5n1UO3/fA+bTRUj"
                    + "UYRN0GdkHQCliefZ0Y6XEususCiTApLYfdjUHsIWGldf8C2vxRv+mw=="));

    Mockito.when(realmProvider.load("idonotexist")).thenReturn(Optional.empty());

    realm = new Realm();
    realm.setName("realm");
    realm.addProperty(GlobalKeysConfig.USERS_MAX_OUTPUT_SIZE, "100000");
    UserStorage us1 = new UserStorage();
    us1.setName("us1");
    UserStorage us2 = new UserStorage();
    us2.setName("us2");
    realm.setUserStorages(List.of(us1, us2));
    Mockito.when(realmProvider.load("realm")).thenReturn(Optional.of(realm));

    Mockito.when(storeProvider.getReaderStore("realm", "us1")).thenReturn(readerStore1);
    Mockito.when(readerStore1.getUser("Toto")).thenReturn(Optional.empty());
    Mockito.when(readerStore1.getUserByMail("mail@insee.fr")).thenReturn(Optional.of(new User()));
    Mockito.when(storeProvider.getReaderStore("realm", "us2")).thenReturn(readerStore2);
    Mockito.when(readerStore2.getUser("Toto")).thenReturn(Optional.of(user1));
    Mockito.when(readerStore2.getUser("donotexist")).thenReturn(Optional.empty());
    Mockito.when(readerStore2.getUser("UserWithCertificate"))
        .thenReturn(Optional.of(userWithCertificate));

    Mockito.when(readerStore1.searchUsers(Mockito.any(), Mockito.any(), Mockito.eq("AND")))
        .thenAnswer(invocation -> mockPageResultFromNUsersUs(invocation, 20000));
    Mockito.when(readerStore2.searchUsers(Mockito.any(), Mockito.any(), Mockito.eq("AND")))
        .thenAnswer(invocation -> mockPageResultFromNUsersUs(invocation, 20000));

    Mockito.when(storeProvider.getReaderStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);
    Mockito.when(storeProvider.getWriterStore(Mockito.eq("idonotexist"), Mockito.anyString()))
        .thenThrow(RealmNotFoundException.class);
    User createdUser = new User("createdUser");
    Mockito.when(readerStore2.getUser("createdUser")).thenReturn(Optional.of(createdUser));
    ProviderResponse createdProviderResponse =
        new ProviderResponse(
            "createdUser", "createdUser", ProviderResponseStatus.OK, createdUser, null);
    Mockito.when(writerStore.createUser(Mockito.any(), Mockito.any()))
        .thenReturn(createdProviderResponse);

    Mockito.when(storeProvider.getWriterStore("realm", "us2")).thenReturn(writerStore);
    Mockito.when(writerStore.deleteUser(Mockito.eq("donotexist"), Mockito.any()))
        .thenThrow(UserNotFoundException.class);
  }

  @Test
  public void getUserOnMultipleStorage() {
    assertThat("get user Toto", userService.findById("realm", null, "Toto"), is(user1));
  }

  @Test
  @DisplayName(
      "Given we try to fetch a user on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void getUserShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> userService.findById("idonotexist", "us2", "Toto"));
  }

  @Test
  @DisplayName(
      "Given we try to find users on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findUsersShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () ->
            userService.findByProperties(
                "idonotexist", "us2", new User("Toto"), new PageableResult(), SearchType.AND));
  }

  @Test
  @DisplayName(
      "Given we try to find user by its mail on a realm that do not exist, "
          + "then throw RealmNotFound exception")
  public void findUserByMailShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () -> userService.findByMail("idonotexist", "us2", "toto@insee.fr"));
  }

  @Test
  @DisplayName(
      "Given we try to delete a user that do not exist, " + "then throw UserNotFoundException")
  public void deleteUserThatDoNotExistShouldFail() {
    assertThrows(
        UserNotFoundException.class, () -> userService.delete("realm", "us2", "donotexist", null));
  }

  @Test
  @DisplayName(
      "Given we try to delete a user on a realm do not exist, "
          + "then throw RealmNotFoundException")
  public void deleteUserShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class, () -> userService.delete("idonotexist", "us2", "toto", null));
  }

  @Test
  @DisplayName("Given a user has a well set certificate, then it should be returned by the service")
  public void getCertificateShouldSucceedWhenItIsSetOnUser() throws CertificateException {
    X509Certificate certificate =
        (X509Certificate)
            CertificateFactory.getInstance("X509")
                .generateCertificate(
                    new ByteArrayInputStream(
                        userService.getCertificate("realm", "us2", "UserWithCertificate")));
    assertThat(
        "Certificate should have a john doe subject",
        "CN=John Doe,OU=Unit Test,O=Insee,ST=Paris,C=FR",
        is(certificate.getSubjectX500Principal().getName()));
  }

  @Test
  @DisplayName(
      "Given we fetch a certificate on a user that does not exist, "
          + "then the service should fail with UserNotFoundException")
  public void getCertificateShouldFailWhenNoUser() {
    assertThrows(
        UserNotFoundException.class,
        () -> userService.getCertificate("realm", "us2", "donotexist"));
  }

  @Test
  @DisplayName(
      "Given we fetch a certificate that does not have a certificate, "
          + "then the service should fail with NoCertificateOnUserException")
  public void getCertificateShouldFailWhenNoCertificateOnUser() {
    NoCertificateOnUserException exception =
        assertThrows(
            NoCertificateOnUserException.class,
            () -> userService.getCertificate("realm", "us2", "Toto"));
    assertThat(
        "Exception message should contain name and realm",
        "User Toto on realm realm does not have a certificate",
        is(exception.getMessage()));
  }

  @Test
  @DisplayName(
      "Given we fetch a certificate on a realm that does not exist, "
          + "then the service should fail with NoCertificateOnUserException")
  public void getCertificateShouldFailWhenRealmNotFound() {
    assertThrows(
        RealmNotFoundException.class,
        () -> userService.getCertificate("idonotexist", "us2", "Toto"));
  }

  @Test
  @DisplayName(
      "Given the default setting to verify the unicity of the mail is true and the real has no pre-defined configuration"
          + "then we verify the mail does not already exist in the userstorage")
  public void defaultConfigTestVerifyUniqueMail() {
    User user1 = new User("username");
    user1.setMail("mail@insee.fr");
    assertThrows(
        UserAlreadyExistException.class,
        () -> userService.create("realm", "us2", user1, new ProviderRequest()));
  }

  @Test
  @DisplayName(
      "Given we ask for 30000 users on a realm with two storages each containing 20000 users, "
          + "with a limitation of the maximum requested users on the realm of 100000, "
          + "then we should get 30000 users from both userstorages")
  public void getUsersWithPageableSizeShouldReturnEnoughUsers() {
    PageableResult pageable = new PageableResult(30000, 0, null);
    List<User> results =
        userService
            .findByProperties(realm.getName(), null, new User(), pageable, SearchType.AND)
            .getResults();
    assertThat("30000 users are retrieved", results.size(), is(30000));
    assertThat(
        "Should contain users from userstorage1",
        results.stream()
            .anyMatch(u -> u.getMetadatas().get(EventKeysConfig.USERSTORAGE).equals("us1")));
    assertThat(
        "Should contain users from userstorage2",
        results.stream()
            .anyMatch(u -> u.getMetadatas().get(EventKeysConfig.USERSTORAGE).equals("us2")));
  }

  private PageResult<User> mockPageResultFromNUsersUs(
      InvocationOnMock invocation, int nbUsersInUs) {
    PageResult<User> pageResult = new PageResult<>();
    int minOfRequestedOrAllUsers =
        Math.min(((PageableResult) invocation.getArgument(1)).getSize(), nbUsersInUs);
    pageResult.setResults(
        Stream.generate(User::new).limit(minOfRequestedOrAllUsers).collect(Collectors.toList()));
    pageResult.setPageSize(nbUsersInUs);
    pageResult.setTotalElements(minOfRequestedOrAllUsers);
    return pageResult;
  }
}
