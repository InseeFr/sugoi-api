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

import fr.insee.sugoi.core.exceptions.GroupNotFoundException;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.model.Group;
import fr.insee.sugoi.model.paging.PageResult;
import fr.insee.sugoi.model.paging.PageableResult;
import fr.insee.sugoi.services.view.GroupView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequestMapping(value = {"/v2", "/"})
@RestController
@Tag(
    name = "Manage Groups",
    description = "New endpoints to create, update, delete, or find groups")
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class GroupController {

  @Autowired private GroupService groupService;

  @GetMapping(
      path = {"/realms/{realm}/applications/{application}/groups"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search groups by parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Groups found according to parameter",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PageResult.class))
            })
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<PageResult<Group>> getGroups(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Name of application where to search group", required = false)
          @PathVariable(value = "application")
          String applicationName,
      @Parameter(description = "Quick description of wanted group", required = false)
          @RequestParam(value = "description", required = false)
          String description,
      @Parameter(description = "Group's name search", required = false)
          @RequestParam(value = "name", required = false)
          String name,
      @Parameter(description = "Expected size of result", required = false)
          @RequestParam(value = "size", defaultValue = "20")
          int size,
      @Parameter(description = "Token to continue a previous search", required = false)
          @RequestParam(name = "searchToken", required = false)
          String searchCookie,
      @Parameter(description = "Offset to apply when searching", required = false)
          @RequestParam(value = "offset", defaultValue = "0")
          int offset) {
    Group filterGroup = new Group();
    filterGroup.setName(name);
    filterGroup.setDescription(description);
    PageableResult pageableResult = new PageableResult(size, offset, searchCookie);

    PageResult<Group> foundGroups =
        groupService.findByProperties(realm, applicationName, filterGroup, pageableResult);

    if (foundGroups.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("searchToken", foundGroups.getSearchToken())
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(foundGroups);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(foundGroups);
    }
  }

  @PostMapping(
      value = {"/realms/{realm}/applications/{application}/groups"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Create group in application according to parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Group created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Group.class))
            }),
        @ApiResponse(
            responseCode = "409",
            description = "Group already exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<?> createGroups(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Name of application where to add group", required = true)
          @PathVariable(value = "application", required = true)
          String applicationName,
      @Parameter(description = "Group to create", required = true) @RequestBody
          GroupView groupView) {

    Group group = new Group();
    group.setAppName(groupView.getAppName());
    group.setDescription(groupView.getDescription());
    group.setName(groupView.getName());

    Group groupCreated = groupService.create(realm, applicationName, group);

    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/" + group.getName())
            .build()
            .toUri();

    return ResponseEntity.created(location).body(groupCreated);
  }

  @PutMapping(
      value = {"/realms/{realm}/applications/{application}/groups/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update group in application according to parameters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Group.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Group and groupId are not equals",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "404",
            description = "Group didn't exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<?> updateGroups(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Group's id to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "Name of application which contains group", required = true)
          @PathVariable("application")
          String applicationName,
      @Parameter(description = "Group to update", required = true) @RequestBody
          GroupView groupView) {

    if (!groupView.getName().equalsIgnoreCase(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    Group group = new Group();
    group.setAppName(groupView.getAppName());
    group.setDescription(groupView.getDescription());
    group.setName(groupView.getName());

    groupService.update(realm, applicationName, group);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.LOCATION, location.toString())
        .body(groupService.findById(realm, applicationName, id));
  }

  @DeleteMapping(
      value = {"/realms/{realm}/applications/{application}/groups/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete group in application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Group deleted",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Group.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Group didn't exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<String> deleteGroups(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Name of application which contains group", required = true)
          @PathVariable("application")
          String applicationName,
      @Parameter(description = "Group's id to delete", required = true) @PathVariable("id")
          String id) {

    groupService.delete(realm, applicationName, id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping(
      path = {"/realms/{realm}/applications/{application}/groups/{groupname}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get group by name in an application")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Group found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = Group.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Group didn't exist",
            content = {@Content(mediaType = "application/json")})
      })
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  public ResponseEntity<Group> getGroupByGroupname(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "Name of application which contains group", required = true)
          @PathVariable("application")
          String applicationName,
      @Parameter(description = "Group's name to search", required = true) @PathVariable("groupname")
          String id) {

    Group group =
        groupService
            .findById(realm, applicationName, id)
            .orElseThrow(
                () ->
                    new GroupNotFoundException(
                        "Cannot find group "
                            + id
                            + " in app "
                            + applicationName
                            + " in realm "
                            + realm));
    return ResponseEntity.status(HttpStatus.OK).body(group);
  }

  @PutMapping(
      value = {"/realms/{realm}/applications/{application}/groups/{group_id}/members/{user_id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Add user to group")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<?> addUserToGroup(
      @PathVariable("realm") String realm,
      @PathVariable("group_id") String groupId,
      @PathVariable("user_id") String userId,
      @PathVariable("application") String applicationName) {

    groupService.addUserToGroup(realm, userId, applicationName, groupId);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.LOCATION, location.toString())
        .build();
  }

  @DeleteMapping(
      value = {"/realms/{realm}/applications/{application}/groups/{group_id}/members/{user_id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete user from group")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAppManager(#realm,#applicationName)")
  public ResponseEntity<?> deleteUserFromGroup(
      @PathVariable("realm") String realm,
      @PathVariable("group_id") String groupId,
      @PathVariable("user_id") String userId,
      @PathVariable("application") String applicationName) {

    groupService.deleteUserFromGroup(realm, userId, applicationName, groupId);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
    return ResponseEntity.status(HttpStatus.OK)
        .header(HttpHeaders.LOCATION, location.toString())
        .build();
  }
}
