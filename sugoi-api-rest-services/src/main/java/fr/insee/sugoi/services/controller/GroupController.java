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

import fr.insee.sugoi.core.model.PageResult;
import fr.insee.sugoi.core.model.PageableResult;
import fr.insee.sugoi.core.service.GroupService;
import fr.insee.sugoi.model.Group;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Manage Groups")
@SecurityRequirement(name = "oAuth")
public class GroupController {

  @Autowired private GroupService groupService;

  @GetMapping(
      path = {"/{realm}/groups", "/{realm}/{storage}/groups"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Search groups")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<?> getGroups(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(value = "application") String applicationName,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "name", required = false) String name,
      @RequestParam(value = "size", defaultValue = "20") int size,
      @RequestParam(value = "offset", defaultValue = "0") int offset) {
    Group filterGroup = new Group();
    filterGroup.setName(name);
    filterGroup.setDescription(description);
    PageableResult pageableResult = new PageableResult(size, offset);

    PageResult<Group> foundGroups =
        groupService.findByProperties(realm, storage, applicationName, filterGroup, pageableResult);

    if (foundGroups.isHasMoreResult()) {
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .replaceQueryParam("offset", offset + size)
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
      value = {"/{realm}/groups", "/{realm}/{storage}/groups"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Add group")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> createGroups(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam(value = "application") String applicationName,
      @RequestBody Group group) {

    if (groupService.findById(realm, storage, applicationName, group.getName()) == null) {
      groupService.create(realm, storage, applicationName, group);

      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + group.getName())
              .build()
              .toUri();

      return ResponseEntity.created(location)
          .body(groupService.findById(realm, storage, applicationName, group.getName()));
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  @PutMapping(
      value = {"/{realm}/groups/{id}", "/{realm}/{storage}/groups/{id}"},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Update group")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<?> updateGroups(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @PathVariable("id") String id,
      @RequestParam("application") String applicationName,
      @RequestBody Group group) {

    if (!group.getName().equals(id)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (groupService.findById(realm, storage, applicationName, id) != null) {
      groupService.update(realm, storage, applicationName, group);
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/" + group.getName())
              .build()
              .toUri();
      return ResponseEntity.status(HttpStatus.OK)
          .header(HttpHeaders.LOCATION, location.toString())
          .body(groupService.findById(realm, storage, applicationName, id));
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @DeleteMapping(
      value = {"/{realm}/groups/{id}", "/{realm}/{storage}/groups/{id}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Delete group")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastWriter(#realm,#storage)")
  public ResponseEntity<String> deleteGroups(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam("application") String applicationName,
      @PathVariable("id") String id) {

    if (groupService.findById(realm, storage, applicationName, id) != null) {
      groupService.delete(realm, storage, applicationName, id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }

  @GetMapping(
      path = {"/{realm}/groups/{name}", "/{realm}/{storage}/groups/{name}"},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "Get group by name")
  @PreAuthorize("@NewAuthorizeMethodDecider.isAtLeastReader(#realm,#storage)")
  public ResponseEntity<Group> getGroupByGroupname(
      @PathVariable("realm") String realm,
      @PathVariable(name = "storage", required = false) String storage,
      @RequestParam("application") String applicationName,
      @PathVariable("groupname") String id) {
    Group group = groupService.findById(realm, storage, applicationName, id);
    if (group != null) {
      return ResponseEntity.status(HttpStatus.OK).body(group);
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}
