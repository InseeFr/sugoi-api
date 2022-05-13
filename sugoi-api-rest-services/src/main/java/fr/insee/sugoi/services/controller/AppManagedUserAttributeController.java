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

import fr.insee.sugoi.core.configuration.GlobalKeysConfig;
import fr.insee.sugoi.core.model.ProviderRequest;
import fr.insee.sugoi.core.model.ProviderResponse;
import fr.insee.sugoi.core.model.SugoiUser;
import fr.insee.sugoi.core.service.ConfigService;
import fr.insee.sugoi.core.service.PermissionService;
import fr.insee.sugoi.core.service.UserService;
import fr.insee.sugoi.model.Realm;
import fr.insee.sugoi.model.User;
import fr.insee.sugoi.model.exceptions.AppCannotManagedAttributeException;
import fr.insee.sugoi.services.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(
    name = "Manage user's attribute for app",
    description = "New endpoints for apps to get add and delete some attributes on users")
@RequestMapping(value = {"/v2", "/"})
@SecurityRequirements(
    value = {@SecurityRequirement(name = "oAuth"), @SecurityRequirement(name = "basic")})
public class AppManagedUserAttributeController {

  @Autowired private UserService userService;

  @Autowired private PermissionService permissionService;

  @Autowired private ConfigService configService;

  @PatchMapping(
      value = {
        "/realms/{realm}/storages/{storage}/users/{id}/{app-managed-attribute-name}/{app-managed-attribute-value}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "add attribute managed by application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid combinaison of id between body and path",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<ProviderResponse> addUserAttributesManagedByApp(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realmName,
      @Parameter(
              description = "Name of the userstorage where the operation will be made",
              required = true)
          @PathVariable("storage")
          String storage,
      @Parameter(description = "User's id to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "key of attribute to add", required = true)
          @PathVariable("app-managed-attribute-name")
          String attributeKey,
      @Parameter(description = "value of attribute to add", required = true)
          @PathVariable("app-managed-attribute-value")
          String attributeValue,
      @Parameter(description = "Allowed asynchronous request", required = false)
          @RequestHeader(name = "X-SUGOI-ASYNCHRONOUS-ALLOWED-REQUEST", defaultValue = "false")
          boolean isAsynchronous,
      @Parameter(description = "Make request prioritary", required = false)
          @RequestHeader(name = "X-SUGOI-URGENT-REQUEST", defaultValue = "false")
          boolean isUrgent,
      @Parameter(description = "Transaction Id", required = false)
          @RequestHeader(name = "X-SUGOI-TRANSACTION-ID", required = false)
          String transactionId,
      Authentication authentication) {
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    Realm realm = configService.getRealm(realmName);
    String patternOfAttribute = getPatternOfAttribute(realm, attributeKey);
    if (permissionService.isWriter(roles, realmName, storage)
        || permissionService.isValidAttributeAccordingAttributePattern(
            roles, realmName, storage, patternOfAttribute, attributeValue)) {
      ProviderResponse response =
          userService.addAppManagedAttribute(
              realmName,
              storage,
              id,
              attributeKey,
              attributeValue,
              new ProviderRequest(
                  new SugoiUser(authentication.getName(), roles), isAsynchronous, null, isUrgent));
      return ResponseEntity.status(Utils.convertStatusTHttpStatus(response, false, true))
          .header("X-SUGOI-TRANSACTION-ID", response.getRequestId())
          .header("X-SUGOI-REQUEST-STATUS", response.getStatus().toString())
          .build();
    } else {
      throw new AccessDeniedException(
          "Not allowed to add appmanagedattribute "
              + attributeKey
              + " . Attribute should match pattern : "
              + patternOfAttribute);
    }
  }

  @DeleteMapping(
      value = {
        "/realms/{realm}/storages/{storage}/users/{id}/{app-managed-attribute-name}/{app-managed-attribute-value}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "delete attribute managed by application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid combinaison of id between body and path",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<ProviderResponse> deleteUserAttributesManagedByApp(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realmName,
      @Parameter(
              description = "Name of the userstorage where the operation will be made",
              required = true)
          @PathVariable("storage")
          String storage,
      @Parameter(description = "User's id to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "key of attribute to add", required = true)
          @PathVariable("app-managed-attribute-name")
          String attributeKey,
      @Parameter(description = "value of attribute to add", required = true)
          @PathVariable("app-managed-attribute-value")
          String attributeValue,
      @Parameter(description = "Allowed asynchronous request", required = false)
          @RequestHeader(name = "X-SUGOI-ASYNCHRONOUS-ALLOWED-REQUEST", defaultValue = "false")
          boolean isAsynchronous,
      @Parameter(description = "Make request prioritary", required = false)
          @RequestHeader(name = "X-SUGOI-URGENT-REQUEST", defaultValue = "false")
          boolean isUrgent,
      @Parameter(description = "Transaction Id", required = false)
          @RequestHeader(name = "X-SUGOI-TRANSACTION-ID", required = false)
          String transactionId,
      Authentication authentication) {
    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .collect(Collectors.toList());
    Realm realm = configService.getRealm(realmName);
    String patternOfAttribute = getPatternOfAttribute(realm, attributeKey);
    if (permissionService.isWriter(roles, realmName, storage)
        || permissionService.isValidAttributeAccordingAttributePattern(
            roles, realmName, storage, patternOfAttribute, attributeValue)) {
      ProviderResponse response =
          userService.deleteAppManagedAttribute(
              realmName,
              storage,
              id,
              attributeKey,
              attributeValue,
              new ProviderRequest(
                  new SugoiUser(authentication.getName(), roles),
                  isAsynchronous,
                  transactionId,
                  isUrgent));
      return ResponseEntity.status(Utils.convertStatusTHttpStatus(response, false, true))
          .header("X-SUGOI-REQUEST-STATUS", response.getStatus().toString())
          .header("X-SUGOI-TRANSACTION-ID", response.getRequestId())
          .build();
    } else {
      throw new AccessDeniedException(
          "Not allowed to delete appmanagedattribute "
              + attributeKey
              + " . Attribute should match pattern : "
              + patternOfAttribute);
    }
  }

  @PatchMapping(
      value = {
        "/realms/{realm}/users/{id}/{app-managed-attribute-name}/{app-managed-attribute-value}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "add attribute managed by application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid combinaison of id between body and path",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<ProviderResponse> addUserAttributesManagedByApp(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "User's id to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "key of attribute to add", required = true)
          @PathVariable("app-managed-attribute-name")
          String attributeKey,
      @Parameter(description = "value of attribute to add", required = true)
          @PathVariable("app-managed-attribute-value")
          String attributeValue,
      @Parameter(description = "Allowed asynchronous request", required = false)
          @RequestHeader(name = "X-SUGOI-ASYNCHRONOUS-ALLOWED-REQUEST", defaultValue = "false")
          boolean isAsynchronous,
      @Parameter(description = "Make request prioritary", required = false)
          @RequestHeader(name = "X-SUGOI-URGENT-REQUEST", defaultValue = "false")
          boolean isUrgent,
      @Parameter(description = "Transaction Id", required = false)
          @RequestHeader(name = "X-SUGOI-TRANSACTION-ID", required = false)
          String transactionId,
      Authentication authentication) {

    return addUserAttributesManagedByApp(
        realm,
        (String)
            userService
                .findById(realm, null, id)
                .getMetadatas()
                .get(GlobalKeysConfig.USERSTORAGE.getName()),
        id,
        attributeKey,
        attributeValue,
        isAsynchronous,
        isUrgent,
        transactionId,
        authentication);
  }

  @DeleteMapping(
      value = {
        "/realms/{realm}/users/{id}/{app-managed-attribute-name}/{app-managed-attribute-value}"
      },
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(summary = "delete attribute managed by application")
  @PreAuthorize("@NewAuthorizeMethodDecider.isReader(#realm,#storage)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = User.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = {@Content(mediaType = "application/json")}),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid combinaison of id between body and path",
            content = {@Content(mediaType = "application/json")})
      })
  public ResponseEntity<?> deleteUserAttributesManagedByApp(
      @Parameter(
              description = "Name of the realm where the operation will be made",
              required = true)
          @PathVariable("realm")
          String realm,
      @Parameter(description = "User's id to update", required = true) @PathVariable("id")
          String id,
      @Parameter(description = "key of attribute to add", required = true)
          @PathVariable("app-managed-attribute-name")
          String attributeKey,
      @Parameter(description = "value of attribute to add", required = true)
          @PathVariable("app-managed-attribute-value")
          String attributeValue,
      @Parameter(description = "Allowed asynchronous request", required = false)
          @RequestHeader(name = "X-SUGOI-ASYNCHRONOUS-ALLOWED-REQUEST", defaultValue = "false")
          boolean isAsynchronous,
      @Parameter(description = "Make request prioritary", required = false)
          @RequestHeader(name = "X-SUGOI-URGENT-REQUEST", defaultValue = "false")
          boolean isUrgent,
      @Parameter(description = "Transaction Id", required = false)
          @RequestHeader(name = "X-SUGOI-TRANSACTION-ID", required = false)
          String transactionId,
      Authentication authentication) {

    return deleteUserAttributesManagedByApp(
        realm,
        (String)
            userService
                .findById(realm, null, id)
                .getMetadatas()
                .get(GlobalKeysConfig.USERSTORAGE.getName()),
        id,
        attributeKey,
        attributeValue,
        isAsynchronous,
        isUrgent,
        transactionId,
        authentication);
  }

  /**
   * Each attributes allowed have an authorized pattern in the pattern config at the same place
   * there are in the attribute managed config. If the attribute exists, return this pattern to be
   * checked for user permissions.
   *
   * @param realm where the config will be fetched
   * @param attributeKey attribute the user wants to update
   * @throws AppCannotManagedAttributeException if the attribute is not an app managed attribute
   * @return the authorized pattern for the attribute
   */
  private String getPatternOfAttribute(Realm realm, String attributeKey) {
    List<String> attributesAllowed = getAttributesAllowed(realm);
    if (attributesAllowed.contains(attributeKey.toUpperCase())) {
      return realm
          .getProperties()
          .get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_PATTERNS_LIST)
          .toUpperCase()
          .split(",")[attributesAllowed.indexOf(attributeKey.toUpperCase())];
    } else {
      throw new AppCannotManagedAttributeException(
          "Cannot add delete to user: " + attributeKey + " is not an app managed attribute");
    }
  }

  private List<String> getAttributesAllowed(Realm realm) {
    return Arrays.asList(
        realm
            .getProperties()
            .get(GlobalKeysConfig.APP_MANAGED_ATTRIBUTE_KEYS_LIST)
            .toUpperCase()
            .split(","));
  }
}
