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
package fr.insee.sugoi.services.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.Habilitation;
import fr.insee.sugoi.model.Organization;
import fr.insee.sugoi.model.PostalAddress;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootTest(
    classes = {ExportController.class, SugoiAdviceController.class},
    properties = "spring.config.location=classpath:/controller/application.properties")
@AutoConfigureMockMvc
@EnableWebMvc
public class ExportControllerTest {

  @Autowired MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private ConfigService configService;

  ObjectMapper objectMapper = new ObjectMapper();

  private Realm reamlOneUS;
  private Realm realmTwoUS;

  @BeforeEach
  public void setup() {
    reamlOneUS = new Realm();
    reamlOneUS.setName("realmOneUS");

    realmTwoUS = new Realm();
    realmTwoUS.setName("realmTwoUs");

    UserStorage userStorage = new UserStorage();
    userStorage.setName("storage");
    userStorage.setUserMappings(
        List.of(
            new StoreMapping("username", "uid", ModelType.STRING, true),
            new StoreMapping("mail", "cn", ModelType.STRING, false),
            new StoreMapping("attributes.common_name", "cn", ModelType.STRING, true),
            new StoreMapping("attributes.description", "description", ModelType.STRING, true),
            new StoreMapping(
                "habilitations", "inseeGroupeDefaut", ModelType.LIST_HABILITATION, true),
            new StoreMapping("organization", "inseeOrganisationDN", ModelType.ORGANIZATION, true),
            new StoreMapping("groups", "memberOf", ModelType.LIST_GROUP, false),
            new StoreMapping("address", "inseeAdressePostaleDN", ModelType.ADDRESS, true)));
    User user = new User("user");
    user.setLastName("userLastName");
    user.setMail("test@test.com");
    user.setHabilitations(
        List.of(new Habilitation("application1_role"), new Habilitation("application2_role")));
    Organization organization = new Organization();
    organization.setIdentifiant("organisation1");
    user.setOrganization(organization);
    user.setGroups(List.of(new Group("app1", "group1"), new Group("app2", "group2")));
    PostalAddress address = new PostalAddress("generatedBefore");
    address.setLines(
        new String[] {"33 rue des Fleurs", "56700 Fleurville", null, "", "Cedex 1234", null, null});
    user.setAddress(address);

    User user1 = new User("user1");
    user1.setLastName("user1LastName");
    reamlOneUS.setUserStorages(List.of(userStorage));
    Mockito.when(configService.getRealm("realmOneUS")).thenReturn(reamlOneUS);

    User userAttribute = new User("userAttribute");
    userAttribute.setAttributes(
        Map.of("description", "this is a description", "common_name", "common name"));

    PageResult<User> pageResultUserStorage1 = new PageResult<>();

    pageResultUserStorage1.setResults(List.of(user, userAttribute));
    Mockito.when(userService.findByProperties(any(), eq("storage"), any(), any(), any()))
        .thenReturn(pageResultUserStorage1);

    PageResult<User> pageResultUserStorage2 = new PageResult<>();
    pageResultUserStorage2.setResults(List.of(user1));
    Mockito.when(userService.findByProperties(any(), eq("storage2"), any(), any(), any()))
        .thenReturn(pageResultUserStorage2);

    UserStorage userStorage2 = new UserStorage();
    userStorage2.setName("storage2");
    userStorage2.setUserMappings(
        List.of(
            new StoreMapping("username", "uid", ModelType.STRING, true),
            new StoreMapping("lastName", "cn", ModelType.STRING, false)));
    realmTwoUS.setUserStorages(List.of(userStorage2, userStorage));
    Mockito.when(configService.getRealm("realmTwoUS")).thenReturn(realmTwoUS);
  }

  @Test
  @WithMockUser
  public void headerTest() throws Exception {

    assertThat(
        "Should have headers username and description",
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList()).get(0),
        is(
            "username,mail,attributes.common_name,attributes.description,habilitations,organization,groups,address"));
  }

  @Test
  @WithMockUser
  public void headerTestMultipleUserStorages() throws Exception {
    assertThat(
        "Should have headers username and mail",
        getTwoUsResponse().getContentAsString().lines().collect(Collectors.toList()).get(0),
        is(
            "username,lastName,mail,attributes.common_name,attributes.description,habilitations,organization,groups,address"));
  }

  @Test
  @WithMockUser
  public void bodyTestString() throws Exception {
    String response = getOneUsResponse().getContentAsString();
    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "Should have mail in body",
        retrieveAttribute(lines.get(0), lines.get(1), "mail"),
        is("test@test.com"));
  }

  @Test
  @WithMockUser
  public void bodyTestMultipleStorages() throws Exception {
    List<String> lines =
        getTwoUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "Should have mail in body",
        retrieveAttribute(lines.get(0), lines.get(2), "mail"),
        is("test@test.com"));
    assertThat(
        "Should have mail in body",
        retrieveAttribute(lines.get(0), lines.get(1), "lastName"),
        is("user1LastName"));
  }

  @Test
  @WithMockUser
  void testGetAttributesFromMap() throws Exception {
    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());

    assertThat(
        "should have common name in body",
        retrieveAttribute(lines.get(0), lines.get(2), "attributes.description"),
        is("this is a description"));
  }

  @Test
  @WithMockUser
  void testGetHabilitations() throws Exception {

    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "should have habilitation in body",
        retrieveAttribute(lines.get(0), lines.get(1), "habilitations"),
        is("\"application1_role,application2_role\""));
  }

  @Test
  @WithMockUser
  void testGetOrganization() throws Exception {
    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "should have organization in body",
        retrieveAttribute(lines.get(0), lines.get(1), "organization"),
        is("organisation1"));
  }

  @Test
  @WithMockUser
  void testGetGroup() throws Exception {
    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "should have group in body",
        retrieveAttribute(lines.get(0), lines.get(1), "groups"),
        is("\"group1 (app1),group2 (app2)\""));
  }

  @Test
  @WithMockUser
  void testAddress() throws Exception {
    List<String> lines =
        getOneUsResponse().getContentAsString().lines().collect(Collectors.toList());
    assertThat(
        "should have address in body",
        retrieveAttribute(lines.get(0), lines.get(1), "address"),
        is("\"33 rue des Fleurs 56700 Fleurville Cedex 1234 \""));
  }

  private String retrieveAttribute(String header, String userLine, String attributeName) {
    // split sur les , qui ne sont pas entre guillemets
    String[] s = userLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    int attributePosition =
        header.substring(0, header.lastIndexOf(attributeName)).split(",").length;
    return s[attributePosition];
  }

  @Test
  @WithMockUser
  @DisplayName("When there are only two users in userstorage all users should be returned")
  public void getAllUsersWhenTotalIs2() throws Exception {
    Mockito.when(
            userService.findByProperties(
                Mockito.eq("realmOneUS"),
                Mockito.eq("storage"),
                Mockito.isA(User.class),
                Mockito.isA(PageableResult.class),
                Mockito.isA(SearchType.class)))
        .thenReturn(createPageResult("toto", "tutu", false));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/realmOneUS/storages/storage/export/users/export.csv");
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat(
        "HttpServletResponse type should be a csv",
        "text/csv;charset=UTF-8",
        is(response.getContentType()));
    List<String> usernames = convertCsvToUsernames(response.getContentAsString());
    assertThat("Should return 2 users", 2, is(usernames.size()));
    assertThat(
        "Should have a user tutu",
        usernames.stream().anyMatch(username -> username.equals("tutu")));
  }

  @Test
  @WithMockUser
  @DisplayName(
      "When there are more users than the pagesize, "
          + "but less than the maximum value then "
          + "users should be requested several times and all users should be returned")
  public void getTotalUsersWhenMoreThanPageSize() throws Exception {

    Mockito.when(
            userService.findByProperties(
                Mockito.eq("realmOneUS"),
                Mockito.eq("storage"),
                Mockito.isA(User.class),
                Mockito.isA(PageableResult.class),
                Mockito.isA(SearchType.class)))
        .thenReturn(createPageResult("toto", "tata", true))
        .thenReturn(createPageResult("tutu", null, false));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/realmOneUS/storages/storage/export/users/export.csv");
    List<String> usernames =
        convertCsvToUsernames(
            mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString());

    assertThat("Should return 3 users even if page size is 2", 3, is(usernames.size()));
  }

  @Test
  @WithMockUser
  @DisplayName(
      "When there are more users than the maximum a user can fetch, max exception should be returned")
  public void exceptionIsReturnedWhenMoreThanMax() throws Exception {

    Mockito.when(
            userService.findByProperties(
                Mockito.eq("realmOneUS"),
                Mockito.eq("storage"),
                Mockito.isA(User.class),
                Mockito.isA(PageableResult.class),
                Mockito.isA(SearchType.class)))
        .thenReturn(createPageResult("toto", "tata", true))
        .thenReturn(createPageResult("tutu", "toto2", true))
        .thenReturn(createPageResult("toto4", "toto4", true))
        .thenReturn(createPageResult("toto5", "toto6", true))
        .thenReturn(createPageResult("toto7", "toto8", true))
        .thenReturn(createPageResult("toto9", "toto10", true))
        .thenReturn(createPageResult("toto11", "toto12", false));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/realmOneUS/storages/storage/export/users/export.csv");
    List<String> usernames =
        convertCsvToUsernames(
            mockMvc.perform(requestBuilder).andReturn().getResponse().getContentAsString());

    assertThat("Should return a value under the max", 10, greaterThanOrEqualTo(usernames.size()));
    assertThat(
        "Should return a value above the max plus the pagesize",
        10 - 2,
        lessThan(usernames.size()));
  }

  private List<String> convertCsvToUsernames(String usersCsv) throws UnsupportedEncodingException {
    List<String> usernames =
        Arrays.stream(usersCsv.split("\n"))
            .skip(1)
            .map(line -> line.split(",")[0])
            .collect(Collectors.toList());
    return usernames;
  }

  private PageResult<User> createPageResult(
      String firstName, String secondName, boolean hasMoreResult) {
    PageResult<User> usersResult = new PageResult<>();
    usersResult.setPageSize(2);
    usersResult.setHasMoreResult(hasMoreResult);
    List<User> users = new ArrayList<>();
    if (firstName != null) {
      users.add(new User(firstName));
    }
    if (secondName != null) {
      users.add(new User(secondName));
    }
    usersResult.setResults(users);
    return usersResult;
  }

  private MockHttpServletResponse getOneUsResponse() throws Exception {
    return getResponse("/realms/realmOneUS/storages/storage/export/users/export.csv");
  }

  private MockHttpServletResponse getTwoUsResponse() throws Exception {
    return getResponse("/realms/realmTwoUS/export/users/export.csv");
  }

  private MockHttpServletResponse getResponse(String url) throws Exception {
    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON);
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();
    return response;
  }
}
