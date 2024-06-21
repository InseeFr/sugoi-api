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
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;

import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.*;
import fr.insee.sugoi.model.exceptions.UserNotFoundException;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.technics.ModelType;
import fr.insee.sugoi.model.technics.StoreMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
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
  @MockBean private GroupService groupService;

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
    user.setGroups(List.of(new Group("group1", "app1"), new Group("group2", "app2")));
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
    Mockito.when(
            userService.findByProperties(any(), eq("storage"), any(), any(), any(), anyBoolean()))
        .thenReturn(pageResultUserStorage1);

    PageResult<User> pageResultUserStorage2 = new PageResult<>();
    pageResultUserStorage2.setResults(List.of(user1));
    Mockito.when(
            userService.findByProperties(any(), eq("storage2"), any(), any(), any(), anyBoolean()))
        .thenReturn(pageResultUserStorage2);

    UserStorage userStorage2 = new UserStorage();
    userStorage2.setName("storage2");
    userStorage2.setUserMappings(
        List.of(
            new StoreMapping("username", "uid", ModelType.STRING, true),
            new StoreMapping("lastName", "cn", ModelType.STRING, false)));
    realmTwoUS.setUserStorages(List.of(userStorage2, userStorage));
    Mockito.when(configService.getRealm("realmTwoUS")).thenReturn(realmTwoUS);

    Group aGroup = new Group("agroup_anapplication", "anapplication");
    User nonExistantUser = new User("nonExistantUser");
    aGroup.setUsers(List.of(user1, user, nonExistantUser));
    Mockito.when(groupService.findById("realmTwoUS", "anapplication", "agroup_anapplication"))
        .thenReturn(aGroup);
    Mockito.when(userService.findById("realmTwoUS", null, "user1", false)).thenReturn(user1);
    Mockito.when(userService.findById("realmTwoUS", null, "user", false)).thenReturn(user);
    Mockito.when(userService.findById("realmTwoUS", null, "nonExistantUser", false))
        .thenThrow(UserNotFoundException.class);
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

  @Test
  @WithMockUser
  void testExportUsersInGroup() throws Exception {
    List<String> lines =
        getResponse(
                "/realms/realmTwoUS/applications/anapplication/groups/agroup_anapplication/export/export.csv")
            .getContentAsString()
            .lines()
            .collect(Collectors.toList());
    assertThat("Should have two users plus one header", lines.size(), is(3));
    assertThat(
        "Should have mail in body",
        retrieveAttribute(lines.get(0), lines.get(2), "mail"),
        is("test@test.com"));
  }

  private String retrieveAttribute(String header, String userLine, String attributeName) {
    // split sur les , qui ne sont pas entre guillemets
    String[] s = userLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    int attributePosition =
        header.substring(0, header.lastIndexOf(attributeName)).split(",").length;
    return s[attributePosition];
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
    return mockMvc.perform(requestBuilder).andReturn().getResponse();
  }
}
