# Configuration

- [Configuration](#configuration)
  - [Changing the configuration](#changing-the-configuration)
  - [Available Properties](#available-properties)
    - [Realm provider configuration](#realm-provider-configuration)
    - [Reader writer configuration](#reader-writer-configuration)
      - [Jms Specific configuration](#jms-specific-configuration)
    - [SpringDoc configuration](#springdoc-configuration)
    - [Security configuration](#security-configuration)
      - [Password configuration](#password-configuration)
    - [WebHooks configuration](#webhooks-configuration)
    - [Spring actuator configuration](#spring-actuator-configuration)
    - [SeeAlso configuration](#seealso-configuration)
    - [Other info configuration](#other-info-configuration)
    - [Old endpoints configuration](#old-endpoints-configuration)

## Changing the configuration

All configuration is done through spring properties. By default, spring Boot loads properties from application.properties. You can also use system properties for example `java -jar app.jar -DNameofpropertie=valueOfProperty`. Command line application arguments also work : `-Dspring-boot.run.arguments="--nameofpropertie=valueOfProperty"`. You can also use OS environment variables.

Sugoi-api is a springboot app working with extension. Each extension is activated by a property.

## Available Properties

### Realm provider configuration

Realm can be load from different sources.

| Properties                                                 |                                                                       Description                                                                        |             Default value |                             example |
|------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------:|--------------------------:|------------------------------------:|
| fr.insee.sugoi.realm.config.type                           |                                                        RealmProvider type (could be ldap or file)                                                        |                      file |                                file |
| fr.insee.sugoi.realm.config.local.path                     |                               Use only if config type is file. Path to a file containing an array of realms in json format                               |                           |                         realms.json |
| fr.insee.sugoi.config.ldap.profils.url                     |                              Use only if config type is ldap. Ldap host and port where the realm configurations are stored                               |                           |                         my-ldap.url |
| fr.insee.sugoi.config.ldap.profils.port                    |                              Use only if config type is ldap. Ldap host and port where the realm configurations are stored                               |                           |                                 389 |
| fr.insee.sugoi.config.ldap.profils.branche                 |                                      Use only if config type is ldap. Ldap subtree where configurations are stored                                       |                           |                                     |
| fr.insee.sugoi.config.ldap.profils.pattern                 | Use only if config type is ldap. String pattern to find realms ('{realm}' is replaced with realm's name). cn={realm} will search realm config for realm1 | cn=Profil\_{realm}\_Sugoi | cn=config\_{realm}\_WebServicesLdap |
| fr.insee.sugoi.config.ldap.profils.timeout                 |                                                  Timeout before failing to get profiles in milliseconds                                                  |                     30000 |                               30000 |
| fr.insee.sugoi.ldap.default.vlv.enabled                    |                                                               enable vlv searched on ldap                                                                |                     false |                                     |
| fr.insee.sugoi.config.ldap.default.sortKey                 |                                                    attribute on which paging request will be ordered                                                     |                           |                                 uid |
| fr.insee.sugoi.config.ldap.default.max-pool-connection-age |                                        default time before a connection is dropped from connection pool in millis                                        |                     60000 |                                 uid |
| fr.insee.sugoi.verify-unique-mail                          |                                        indicate if a check on user email must be done before each update/creation                                        |                           |                                true |

### Reader writer configuration

For each realm we have the possibility to configure a default reader and a default writer. For the moment it's possible to use ldap, file, and jms as writerStore and only ldap and file as reader.

| Properties                                                           |                                                                                                                     Description                                                                                                                      |                                                                                                       Default value |              example |
|----------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|--------------------------------------------------------------------------------------------------------------------:|---------------------:|
| fr.insee.sugoi.store.defaultReader                                   |                                                                                                       Can be LdapReaderStore, FileReaderStore                                                                                                        |                                                                                                                     |      LdapReaderStore |
| fr.insee.sugoi.store.defaultWriter                                   |                                                                                               Can be LdapWriterStore, FileWriterStore, JmsWriterStore                                                                                                |                                                                                                                     |      LdapWriterStore |
| fr.insee.sugoi.ldap.default.ldap.pool                                |                                                                                    Use only if defaultWriter is ldap. Default pool size for each ldap connection                                                                                     |                                                                                                                     |                   10 |
| fr.insee.sugoi.ldap.default.username                                 |                                                                                Use only if defaultWriter is ldap. Default username to establish connection with ldap                                                                                 |                                                                                                                     | cn=Directory Manager |
| fr.insee.sugoi.ldap.default.password                                 |                                                                                Use only if defaultWriter is ldap. Default password to establish connection with ldap                                                                                 |                                                                                                                     |                admin |
| fr.insee.sugoi.ldap.default.port                                     |                                                                                  Use only if defaultWriter is ldap. Default port to establish connection with ldap                                                                                   |                                                                                                                     |                10389 |
| fr.insee.sugoi.ldap.default.use-authenticated-connection-for-reading |                                                                Use only if defaultWriter or defaultReader is ldap. Should the connections have to be authenticated for reading usage.                                                                |                                                                                                                true |
| fr.insee.sugoi.ldap.default.group_source_pattern                     |                                                                                      Use only if defaultWriter is ldap. Default pattern to follow to find group                                                                                      |                                                                                                                     |                      |
| fr.insee.sugoi.ldap.default.group_filter_pattern                     |                                                                                    Use only if defaultWriter is ldap. Default pattern to follow for naming groups                                                                                    |                                                                                                                     |                      |
| fr.insee.sugoi.default.app_managed_attribute_keys                    |                                                                                               a list of all attributes that a user can update directly                                                                                               |                                                                                                                     |
| fr.insee.sugoi.default.app_managed_attribute_patterns                |                                                                               Default pattern that each fr.insee.sugoi.default.app_managed_attribute_keys must follow                                                                                |                                                                                                                     |
| fr.insee.sugoi.ldap.default.user-mapping                             |                                                     List of mappings between sugoi user attributes and ldap attributes divided by semicolon , see [Realm configuration](realm-configuration.md)                                                      |           username:uid,String,rw;groups:memberOf,list_group,ro;habilitations:inseeGroupeDefaut,list_habilitation,rw |
| fr.insee.sugoi.ldap.default.organization-mapping                     |                                                  List of mappings between sugoi organization attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                  | identifiant:uid,String,rw;address:inseeAdressePostaleDN,address,rw;organization:inseeOrganisationDN,organization,rw |
| fr.insee.sugoi.ldap.default.group-mapping                            |                                                     List of mappings between sugoi group attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                      |                                 name:cn,String,rw;description:description,String,rw;users:uniquemember,list_user,rw |
| fr.insee.sugoi.ldap.default.application-mapping                      |                                                  List of mappings between sugoi application attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                   |                                                                                                   name:ou,String,rw |
| fr.insee.sugoi.id-create-length                                      |                                                                                                          Size of the ids randomly generated                                                                                                          |                                                                                                                   7 |
| fr.insee.sugoi.reader-store-asynchronous                             | Is the reader store asynchronous, ie a difference can exist between what we read in readerstore and the realty. Can occur if the current service is connected by a broker to the real service. If true MAIL and ID unicity control are NOT performed |                                                                                                               false |
| fr.insee.sugoi.users.maxoutputsize                                   |                                                                                                  The default maximum number of user outputs allowed                                                                                                  |                                                                                                                1000 |                  100 |
| fr.insee.sugoi.groups.maxoutputsize                                  |                                                                                                 The default maximum number of groups outputs allowed                                                                                                 |                                                                                                                1000 |                  100 |
| fr.insee.sugoi.organizations.maxoutputsize                           |                                                                                             The default maximum number of organizations outputs allowed                                                                                              |                                                                                                                1000 |                  100 |
| fr.insee.sugoi.applications.maxoutputsize                            |                                                                                              The default maximum number of applications outputs allowed                                                                                              |                                                                                                                1000 |                  100 |
| fr.insee.sugoi.ldap.default.connection.timeout                       |                                                                                      Default response timeout for all types of operations with a ldap provider.                                                                                      |                                                                                                               30000 |                30000 | 

#### Jms Specific configuration

These configurations used with Jms as default writer or reader or to use the server as a receiver for another Sugoi server request.

| Properties                                          |                                                                                                           Description                                                                                                            |                          Default value |                                     example |
|-----------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|---------------------------------------:|--------------------------------------------:|
| fr.insee.sugoi.jms.receiver.request.enabled         |                                                                        Enable to process request for other Sugoi by reading in the broker request queue.                                                                         |                                  false |                                        true |
| fr.insee.sugoi.jms.broker.url                       |                                                                                Url of the broker to use for Writer, Reader or receiving request.                                                                                 |                                   none | ssl://broker.com:61617?verifyHostName=false |
| fr.insee.sugoi.jms.broker.username                  |                                                                                User to use on the broker for Writer reader or receiving request.                                                                                 |                                   none |                                       sugoi |
| fr.insee.sugoi.jms.broker.password                  |                                                                              Password to use on the broker for Writer reader or receiving request.                                                                               |                                   none |                                    password |
| fr.insee.sugoi.jms.broker.timeout                   | Set a timeout for receiving response (in milliseconds) . Is used to wait if the message is not immediately available particularly when doing "synchronous" request to the broker (sending message and waiting for the response). |                                   5000 |                                        5000 |
| fr.insee.sugoi.jms.broker.expiration.synchronous    |                                                                     Expiration of request and response messages send on synchronous queue (in milliseconds).                                                                     |                                  60000 |                                       60000 |
| fr.insee.sugoi.jms.broker.expiration.asynchronous   |                         Expiration of request and response messages send on asynchronous queue (in milliseconds). Must be set to a short value to avoid queue from filling when responses are not read.                          |                                3600000 |                                     1000000 |
| fr.insee.sugoi.jms.queue.requests.name              |                                                  Name of the queue where the JmsWriter writes synchronous request and where a receiver read the synchronous request to process.                                                  |                                   none |          queue.sugoi.developpement.requests |
| fr.insee.sugoi.jms.queue.response.name              |                                                Name of the queue where the JmsWriter read a synchronous response writes request and where a receiver send a synchronous response.                                                |                                   none |          queue.sugoi.developpement.response |
| fr.insee.sugoi.jms.queue.requests.asynchronous.name |                                                 Name of the queue where the JmsWriter writes asynchronous request and where a receiver read the asynchronous request to process.                                                 | Takes the synchronous queue as default |    queue.sugoi.developpement.async.requests |
| fr.insee.sugoi.jms.queue.response.asynchronous.name |                                              Name of the queue where the JmsWriter read a response writes asynchronous request and where a receiver send an asynchronous response.                                               | Takes the synchronous queue as default |    queue.sugoi.developpement.async.response |

### SpringDoc configuration

Sugoi-api implements springdoc with full customization allowed

| Properties                                        | Description | Default value | example |
| ------------------------------------------------- | :---------: | ------------: | ------: |
| springdoc.pathsToMatch                            |             |               |         |
| springdoc.swagger-ui.oauth.clientId               |             |               |         |
| fr.insee.sugoi.springdoc.issuer.url.authorization |             |               |         |
| fr.insee.sugoi.springdoc.issuer.url.refresh       |             |               |         |
| fr.insee.sugoi.springdoc.issuer.url.token         |             |               |         |
| fr.insee.sugoi.springdoc.issuer.description       |             |               |         |
| fr.insee.sugoi.springdoc.contact.name             |             |               |         |
| fr.insee.sugoi.springdoc.contact.email            |             |               |         |
| springdoc.swagger-ui.path                         |             |               |         |

### Security configuration

Sugoi-api implements spring security with full customization allowed

| Properties                                                 |                                                                                    Description                                                                                    | Default value |                                                                 example |
|------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|--------------:|------------------------------------------------------------------------:|
| fr.insee.sugoi.cors.allowed-origins                        |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.cors.allowed-methods                        |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.basic-authentication-enabled       |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.ldap-account-managment-enabled     |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.ldap-account-managment-url         |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.ldap-account-managment-user-base   |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.ldap-account-managment-groupe-base |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.bearer-authentication-enabled      |                                                                                                                                                                                   |               |                                                                         |
| spring.security.oauth2.resourceserver.jwt.jwk-set-uri      |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.old.regexp.role.consultant              |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.old.regexp.role.gestionnaire            |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.old.regexp.role.admin                   |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.old.enable.preauthorize                 |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.reader                      |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.writer                      |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.admin.realm                 | Default pattern to use when searching the admin of a realm or a userstorage. Realm should be passed as$(realm) and for a userstorage admin userstorage is passed as$(userstorage) |    no default |      ROLE_SUGOI_$(realm)_ADMIN,ROLE_SUGOI_$(realm)_$(userStorage)_ADMIN |                                                                                                         |               |         |
| fr.insee.sugoi.ldap.default.group_manager_source_pattern   |                              Default pattern to use when searching manager group for application. Application name should be passed via {appliname}                               |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.password.manager            |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.password.validator          |            Default pattern to use when searching password manager in a realm or userstorage. Userstorage should be passed as$(realm) and userstorage as$(userstorage)             |    no default | PASSWORD_\VALIDATOR_$(realm),PASSWORD_VALIDATOR_$(realm)_$(userstorage) |                                                                                                              |               |         |
| fr.insee.sugoi.api.enable.preauthorize                     |                                                                                                                                                                                   |               |                                                                         |
| fr.insee.sugoi.security.default-roles-for-users            |                                                                    default role to add to each connected user                                                                     |               |                                                                         |
| fr.insee.sugoi.api.regexp.role.application.manager         |                                                                    Pattern used to find  application manager.                                                                     |    no default |               ROLE_ASI_$(realm)_$(application), ROLE_ASI_$(application) |
| fr.insee.sugoi.api.regexp.role.group.manager               |                                                                       Pattern used to find group managers.                                                                        |    no default |                                                       ROLE_ASI_$(group) |
#### Password configuration

  Passwords follows rules when there are passed by a user or randomly generated by Sugoi. A default for these rules which will apply to all realm that do not have its own configuration can be set by properties. For configuration at the realm level see [Realm configuration properties on password](realm-configuration.md#realm-configuration-properties-on-password).

| Properties                                                 | Description | Default value |
| ---------------------------------------------------------- | :---------: | ------------: |
| fr.insee.sugoi.password.create.length | Set the default size of a sugoi generated password | 13 |
| fr.insee.sugoi.password.create.withDigits | Set if a sugoi generated password is generated with digit by default | true |
| fr.insee.sugoi.password.create.withUpperCase | Set if a sugoi generated password is generated with uppercase by default  | true |
| fr.insee.sugoi.password.create.withLowerCase | Set if a sugoi generated password is generated with lowercase by default  | true |
| fr.insee.sugoi.password.create.withSpecial | Set if a sugoi generated password is generated with special characters by default | true |
| fr.insee.sugoi.password.validate.minimal.length | Define a default minimum size to check if a new password provided by the user is valid | 13 |
| fr.insee.sugoi.password.validate.withDigits | Define if a new password provided by the user is valid if it has no digit by default | true |
| fr.insee.sugoi.password.validate.withUpperCase | Define if a new password provided by the user is valid if it has no uppercase by default | true |
| fr.insee.sugoi.password.validate.withLowerCase | Define if a new password provided by the user is valid if it has no lowercase by default | true |
| fr.insee.sugoi.password.validate.withSpecial | Define if a new password provided by the user is valid if it has no special characters by default | true |

### WebHooks configuration

Sugoi-api can be configured to call another webservice. One or several can be configured. It can be used to call a webservice that sends email.
To learn more about this feature see : [Notify external webservices](concepts.md#notify-external-webservices)

| Properties                                                 |                                                                 Description                                                                 |                              Default value |                                                                                        example |
|------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------:|-------------------------------------------:|-----------------------------------------------------------------------------------------------:|
| sugoi.api.event.webhook.enabled                            |                                                        Turns on the webhook feature                                                         |                                      false |                                                                                                |
| sugoi.api.event.webhook.name                               |                  The name of the webservice. Is is only used to find the other configuration properties and the templates                   |                                            |                                                                                   mail-service |
| sugoi.api.event.webhook.{name}.target                      |                                                               Webservice url                                                                |                                            |                                                                  https://mail-service.insee.fr |
| sugoi.api.event.webhook.{name}.auth.type                   |                                                           Type of authentication                                                            |                                            |                                                                    Can be basic, oauth or none |
| sugoi.api.event.webhook.{name}.auth.user                   |                              If auth.type is basic : the name of the account to authenticate on the webservice                              |                                            |                                                                                                |
| sugoi.api.event.webhook.{name}.auth.password               |                            If auth.type is basic : the password of the account to authenticate on the webservice                            |                                            |                                                                                                |
| sugoi.api.event.webhook.{name}.auth.token                  |                                         If auth.type is oauth : the token to pass in Bearer header                                          |                                            |
| sugoi.api.event.webhook.{name}.tag                         | The tag define the type of webhook to call. When a request use a webhook, a tag can be set. By default the tag in the request will be MAIL. |                                            |                                                                          MAIL or anything else |
| sugoi.api.event.webhook.{name}.default.send-login.template |    Define the path to a default template to send to the webhook on send-login request. It can be overidden by userstorage configuration.    |                                            |     ([see template](../sugoi-api-event-webhook/src/main/resources/template/login_default.ftl)) |
| sugoi.api.event.webhook.{name}.default.reset.template      | Define the path to a default template to send to the webhook on reinit-password request. It can be overidden by userstorage configuration.  |                                            |     ([see template](../sugoi-api-event-webhook/src/main/resources/template/reset_default.ftl)) |
| sugoi.api.event.webhook.{name}.default.changepwd.template  | Define the path to a default template to send to the webhook on change-password request. It can be overidden by userstorage configuration.  |                                            | ([see template](../sugoi-api-event-webhook/src/main/resources/template/changepwd_default.ftl)) |
| sugoi.api.event.webhook.mail.secondaryMailAttribute        |                                    Define the attribute where alternative mail for an user will be found                                    |                                            |                                                                                  secondaryMail |
| sugoi.api.event.webhook.enabled.events                     |  List of events that will be sent by webhook event listener. The only accepted values are SEND_LOGIN, REINIT_PASSWORD and CHANGE_PASSWORD   | SEND_LOGIN,REINIT_PASSWORD,CHANGE_PASSWORD |                                                                     SEND_LOGIN,REINIT_PASSWORD |

### Spring actuator configuration

Sugoi-api implements spring actuator documentation available here : [link](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)

| Properties                                   | Description | Default value | example |
| -------------------------------------------- | :---------: | ------------: | ------: |
| management.endpoints.web.exposure.include    |             |               |         |
| management.health.diskspace.path             |             |               |         |
| management.health.diskspace.threshold        |             |               |         |
| management.health.defaults.enabled           |             |               |         |
| management.health.jms.enabled                |             |               |         |
| management.health.ldap.enabled               |             |               |         |
| management.endpoint.health.show-details      |             |               |         |
| management.info.defaults.enabled             |             |               |         |
| management.endpoint.metrics.enabled          |             |               |         |
| management.endpoint.prometheus.enabled       |             |               |         |
| management.prometheus.metrics.export.enabled |             |               |         |

A metrics event module is provided to add metrics when events occured. This is disabled by default and could be enable with :

```
fr.insee.sugoi.api.event.metrics.enabled=true
```

All actuator endpoints are available to admin users. You can also enable a specific monitoring user with the properties :

```
fr.insee.sugoi.security.monitor-user-enabled=true
fr.insee.sugoi.security.monitor-user-name=monitor
fr.insee.sugoi.security.monitor-user-password=monitor
```

This user only has rights on `/actuator` endpoints.

### SeeAlso configuration

| Properties                                   | Description | Default value | example |
| -------------------------------------------- | :---------: | ------------: | ------: |
| fr.insee.sugoi.seealso.username-by-domain.[domain] |  Set username used to resolve seealsos that are on [domain]. |  By default, no seealso request is authenticated. Each domain on which an authentication is required should have this property and the password property set or the request will not be authenticated. | fr.insee.sugoi.seealso.username-by-domain.domain1.insee.fr=user : if the seealso as to be resolved on domain1.insee.fr the user user will be used if the password property is also set. |
| fr.insee.sugoi.seealso.password-by-domain.[domain] |  Set username used to resolve seealsos that are on [domain]. |  By default, no seealso request is authenticated. Each domain on which an authentication is required should have this property and the username property set or the request will not be authenticated. | fr.insee.sugoi.seealso.password-by-domain.domain1.insee.fr=mypass : if the seealso as to be resolved on domain1.insee.fr the password user will be used if the username property is also set. |
| fr.insee.sugoi.seealso.http.timeout          | A timeout in second for http seealso resolution. |   1    |  5  |

### Other info configuration

You can add all other spring properties for example :

| Properties                                 |            Description            | Default value | example |
| ------------------------------------------ | :-------------------------------: | ------------: | ------: |
| server.port                                | Port where application will start |          8090 |         |
| logging.level.root                         |                                   |               |         |
| logging.level.fr.insee.sugoi               |                                   |               |         |
| logging.level.org.springframework.security |                                   |               |         |

### Old endpoints configuration

| Properties                                                  |                                                             Description                                                             | Default value |                                              example |
| ----------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------------: | ------------: | ---------------------------------------------------: |
| fr.insee.sugoi.api.old.domain.realm_userStorage.association | MAPPING BETWEEN OLD DOMAINS -> NEW REALM AND USERSTORAGE, Must be of the form domain1:realm1_userstorage,domain2:realm2_userstorage |               | TCC:TCC_Profil_TCC_Sugoi,SSM:SSP_ssm,INSEE:SSP_insee |
