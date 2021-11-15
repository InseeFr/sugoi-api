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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.sugoi.commons.services.controller.technics.SugoiAdviceController;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.UserStorage;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.model.paging.SearchType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

  @BeforeEach
  public void setup() {
    Realm realm = new Realm();
    realm.setName("domaine1");
    UserStorage userStorage = new UserStorage();
    userStorage.setName("default");
    realm.setUserStorages(List.of(userStorage));
  }

  @Test
  @WithMockUser
  @DisplayName("When there are only two users in userstorage all users should be returned")
  public void getAllUsersWhenTotalIs2() throws Exception {
    Mockito.when(
            userService.findByProperties(
                Mockito.eq("domaine1"),
                Mockito.eq("default"),
                Mockito.isA(User.class),
                Mockito.isA(PageableResult.class),
                Mockito.isA(SearchType.class)))
        .thenReturn(createPageResult("toto", "tutu", false));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/storages/default/export/users/export.csv");
    MockHttpServletResponse response = mockMvc.perform(requestBuilder).andReturn().getResponse();

    assertThat(
        "HttpServletResponse type should be a csv",
        "text/csv;charset=UTF-8",
        is(response.getContentType()));
    List<String> usernames = convertCsvToUsernames(response.getContentAsString());
    assertThat("Should return 2 users", 2, is(usernames.size()));
    assertThat(
        "Should have a user tata",
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
                Mockito.eq("domaine1"),
                Mockito.eq("default"),
                Mockito.isA(User.class),
                Mockito.isA(PageableResult.class),
                Mockito.isA(SearchType.class)))
        .thenReturn(createPageResult("toto", "tata", true))
        .thenReturn(createPageResult("tutu", null, false));

    RequestBuilder requestBuilder =
        MockMvcRequestBuilders.get("/realms/domaine1/storages/default/export/users/export.csv");
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
                Mockito.eq("domaine1"),
                Mockito.eq("default"),
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
        MockMvcRequestBuilders.get("/realms/domaine1/storages/default/export/users/export.csv");
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
            .map(line -> line.split(",")[3])
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
}
