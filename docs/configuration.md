# Configuration

- [Configuration](#configuration)
  - [Changing the configuration](#changing-the-configuration)
  - [Available Properties](#available-properties)
    - [Realm provider configuration](#realm-provider-configuration)
    - [Reader writer configuration](#reader-writer-configuration)
    - [SpringDoc configuration](#springdoc-configuration)
    - [Security configuration](#security-configuration)
    - [WebHooks configuration](#webhooks-configuration)
    - [Spring actuator configuration](#spring-actuator-configuration)
    - [Other info configuration](#other-info-configuration)
    - [Old endpoints configuration](#old-endpoints-configuration)

## Changing the configuration

All configuration is done through spring properties. By default, spring Boot loads properties from application.properties. You can also use system properties for example `java -jar app.jar -DNameofpropertie=valueOfProperty`. Command line application arguments also work : `-Dspring-boot.run.arguments="--nameofpropertie=valueOfProperty"`. You can also use OS environment variables.

Sugoi-api is a springboot app working with extension. Each extension is activated by a property.

## Available Properties

### Realm provider configuration

Realm can be load from different sources.

| Properties                                 |                                                                       Description                                                                       | Default value |     example |
| ------------------------------------------ | :-----------------------------------------------------------------------------------------------------------------------------------------------------: | ------------: | ----------: |
| fr.insee.sugoi.realm.config.type           |                                                       RealmProvider type (could be ldap or file)                                                        |          file |        file |
| fr.insee.sugoi.realm.config.local.path     |                              Use only if config type is file. Path to a file containing an array of realms in json format                               |               | realms.json |
| fr.insee.sugoi.config.ldap.profils.url     |                              Use only if config type is ldap. Ldap host and port where the realm configurations are stored                              |               | my-ldap.url |
| fr.insee.sugoi.config.ldap.profils.port    |                              Use only if config type is ldap. Ldap host and port where the realm configurations are stored                              |               |         389 |
| fr.insee.sugoi.config.ldap.profils.branche |                                      Use only if config type is ldap. Ldap subtree where configurations are stored                                      |               |             |
| fr.insee.sugoi.config.ldap.profils.pattern | Use only if config type is ldap. String pattern to find realms ('{realm}' is replaced with realm's name). cn={realm} wil search realm config for realm1 |               |             |
| fr.insee.sugoi.ldap.default.vlv.enabled    |                                                               enable vlv searched on ldap                                                               |         false |             |
| fr.insee.sugoi.config.ldap.default.sortKey |                                                    attribute on which paging request will be ordered                                                    |               |         uid |
| fr.insee.sugoi.verify.unique.email         |                                       indicate if a check on user email must be done before each update/creation                                        |               |        true |

### Reader writer configuration

For each realm we have the possibility to configure a default reader and a default writer. For the moment it's possible to use ldap, file, and jms as writerStore and only ldap and file as reader.

| Properties                                                           |                                                                                                                     Description                                                                                                                      |                                                                                                       Default value |                                         example |
| -------------------------------------------------------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------: | ----------------------------------------------: |
| fr.insee.sugoi.store.defaultReader                                   |                                                                                                       Can be LdapReaderStore, FileReaderStore                                                                                                        |                                                                                                                     |                                 LdapReaderStore |
| fr.insee.sugoi.store.defaultWriter                                   |                                                                                               Can be LdapWriterStore, FileWriterStore, JmsWriterStore                                                                                                |                                                                                                                     |                                 LdapWriterStore |
| fr.insee.sugoi.jms.broker.url                                        |                                                                                                     Use only if default writer or reader is JMS.                                                                                                     |                                                                                                                     |     ssl://broker.com:61617?verifyHostName=false |
| fr.insee.sugoi.jms.queue.requests.name                               |                                                                                                          Use only if defaultWriter is JMS.                                                                                                           |                                                                                                                     |              queue.sugoi.developpement.requests |
| fr.insee.sugoi.jms.queue.response.name                               |                                                                                                          Use only if defaultWriter is JMS.                                                                                                           |                                                                                                                     |              queue.sugoi.developpement.response |
| fr.insee.sugoi.jms.priority.queue.request.name                       |                                                                                                         Name of the request queue to listen                                                                                                          |                                                                                                                     |  queue.sugoi.developpement.prioritaire.requests |
| fr.insee.sugoi.jms.priority.queue.response.name                      |                                                                                                         Name of the response queue to listen                                                                                                         |                                                                                                                     | queue.sugoi.developpement.prioritaire.responses |
| fr.insee.sugoi.jms.receiver.request.enabled                          |                                                                                                     Enable to listen a request queue in a broker                                                                                                     |                                                                                                                     |                                                 |
| fr.insee.sugoi.jms.receiver.response.enabled                         |                                                                                                    Enable to listen a response queue in a broker                                                                                                     |                                                                                                                     |                                                 |
| fr.insee.sugoi.ldap.default.ldap.pool                                |                                                                                    Use only if defaultWriter is ldap. Default pool size for each ldap connection                                                                                     |                                                                                                                     |                                              10 |
| fr.insee.sugoi.ldap.default.username                                 |                                                                                Use only if defaultWriter is ldap. Default username to establish connection with ldap                                                                                 |                                                                                                                     |                            cn=Directory Manager |
| fr.insee.sugoi.ldap.default.password                                 |                                                                                Use only if defaultWriter is ldap. Default password to establish connection with ldap                                                                                 |                                                                                                                     |                                           admin |
| fr.insee.sugoi.ldap.default.port                                     |                                                                                  Use only if defaultWriter is ldap. Default port to establish connection with ldap                                                                                   |                                                                                                                     |                                           10389 |
| fr.insee.sugoi.ldap.default.use-authenticated-connection-for-reading |                                                                Use only if defaultWriter or defaultReader is ldap. Should the connections have to be authenticated for reading usage.                                                                |                                                                                                                true |
| fr.insee.sugoi.ldap.default.group_source_pattern                     |                                                                                      Use only if defaultWriter is ldap. Default pattern to follow to find group                                                                                      |                                                                                                                     |                                                 |
| fr.insee.sugoi.ldap.default.group_filter_pattern                     |                                                                                    Use only if defaultWriter is ldap. Default pattern to follow for naming groups                                                                                    |                                                                                                                     |                                                 |
| fr.insee.sugoi.default.app_managed_attribute_keys                    |                                                                                               a list of all attributes that a user can update directly                                                                                               |                                                                                                                     |
| fr.insee.sugoi.default.app_managed_attribute_patterns                |                                                                               Default pattern that each fr.insee.sugoi.default.app_managed_attribute_keys must follow                                                                                |                                                                                                                     |
| fr.insee.sugoi.ldap.default.user-mapping                             |                                                     List of mappings between sugoi user attributes and ldap attributes divided by semicolon , see [Realm configuration](realm-configuration.md)                                                      |           username:uid,String,rw;groups:memberOf,list_group,ro;habilitations:inseeGroupeDefaut,list_habilitation,rw |
| fr.insee.sugoi.ldap.default.organization-mapping                     |                                                  List of mappings between sugoi organization attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                  | identifiant:uid,String,rw;address:inseeAdressePostaleDN,address,rw;organization:inseeOrganisationDN,organization,rw |
| fr.insee.sugoi.ldap.default.group-mapping                            |                                                     List of mappings between sugoi group attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                      |                                 name:cn,String,rw;description:description,String,rw;users:uniquemember,list_user,rw |
| fr.insee.sugoi.ldap.default.application-mapping                      |                                                  List of mappings between sugoi application attributes and ldap attributes divided by semicolon, see [Realm configuration](realm-configuration.md)                                                   |                                                                                                   name:ou,String,rw |
| fr.insee.sugoi.id-create-length                                      |                                                                                                          Size of the ids randomly generated                                                                                                          |                                                                                                                   7 |
| fr.insee.sugoi.reader-store-asynchronous                             | Is the reader store asynchronous, ie a difference can exist between what we read in readerstore and the realty. Can occur if the current service is connected by a broker to the real service. If true MAIL and ID unicity control are NOT performed |                                                                                                               false |

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

| Properties                                                 | Description | Default value | example |
| ---------------------------------------------------------- | :---------: | ------------: | ------: |
| fr.insee.sugoi.cors.allowed-origins                        |             |               |         |
| fr.insee.sugoi.cors.allowed-methods                        |             |               |         |
| fr.insee.sugoi.security.basic-authentication-enabled       |             |               |         |
| fr.insee.sugoi.security.ldap-account-managment-enabled     |             |               |         |
| fr.insee.sugoi.security.ldap-account-managment-url         |             |               |         |
| fr.insee.sugoi.security.ldap-account-managment-user-base   |             |               |         |
| fr.insee.sugoi.security.ldap-account-managment-groupe-base |             |               |         |
| fr.insee.sugoi.security.bearer-authentication-enabled      |             |               |         |
| spring.security.oauth2.resourceserver.jwt.jwk-set-uri      |             |               |         |
| fr.insee.sugoi.password.length                             |             |               |         |
| fr.insee.sugoi.api.old.regexp.role.consultant              |             |               |         |
| fr.insee.sugoi.api.old.regexp.role.gestionnaire            |             |               |         |
| fr.insee.sugoi.api.old.regexp.role.admin                   |             |               |         |
| fr.insee.sugoi.api.old.enable.preauthorize                 |             |               |         |
| fr.insee.sugoi.api.regexp.role.reader                      |             |               |         |
| fr.insee.sugoi.api.regexp.role.writer                      |             |               |         |
| fr.insee.sugoi.api.regexp.role.admin                       |             |               |         |
| fr.insee.sugoi.api.regexp.role.app.manager                 |             |               |         |
| fr.insee.sugoi.api.regexp.role.password.manager            |             |               |         |
| fr.insee.sugoi.api.enable.preauthorize                     |             |               |         |

### WebHooks configuration

Sugoi-api can be configured to call another webservice. One or several can be configured. It can be used to call a webservice that sends email.
To learn more about this feature see : [Notify external webservices](concepts.md#notify-external-webservices)

| Properties                                                 |                                                                 Description                                                                 | Default value |                                                                                        example |
| ---------------------------------------------------------- | :-----------------------------------------------------------------------------------------------------------------------------------------: | ------------: | ---------------------------------------------------------------------------------------------: |
| sugoi.api.event.webhook.enabled                            |                                                        Turns on the webhook feature                                                         |         false |                                                                                                |
| sugoi.api.event.webhook.name                               |                  The name of the webservice. Is is only used to find the other configuration properties and the templates                   |               |                                                                                   mail-service |
| sugoi.api.event.webhook.{name}.target                      |                                                               Webservice url                                                                |               |                                                                  https://mail-service.insee.fr |
| sugoi.api.event.webhook.{name}.auth.type                   |                                                           Type of authentication                                                            |               |                                                                    Can be basic, oauth or none |
| sugoi.api.event.webhook.{name}.auth.user                   |                              If auth.type is basic : the name of the account to authenticate on the webservice                              |               |                                                                                                |
| sugoi.api.event.webhook.{name}.auth.password               |                            If auth.type is basic : the password of the account to authenticate on the webservice                            |               |                                                                                                |
| sugoi.api.event.webhook.{name}.auth.token                  |                                         If auth.type is oauth : the token to pass in Bearer header                                          |               |
| sugoi.api.event.webhook.{name}.tag                         | The tag define the type of webhook to call. When a request use a webhook, a tag can be set. By default the tag in the request will be MAIL. |               |                                                                          MAIL or anything else |
| sugoi.api.event.webhook.{name}.default.send-login.template |    Define the path to a default template to send to the webhook on send-login request. It can be overidden by userstorage configuration.    |               |     ([see template](../sugoi-api-event-webhook/src/main/resources/template/login_default.ftl)) |
| sugoi.api.event.webhook.{name}.default.reset.template      | Define the path to a default template to send to the webhook on reinit-password request. It can be overidden by userstorage configuration.  |               |     ([see template](../sugoi-api-event-webhook/src/main/resources/template/reset_default.ftl)) |
| sugoi.api.event.webhook.{name}.default.changepwd.template  | Define the path to a default template to send to the webhook on change-password request. It can be overidden by userstorage configuration.  |               | ([see template](../sugoi-api-event-webhook/src/main/resources/template/changepwd_default.ftl)) |
| sugoi.api.event.webhook.mail.secondaryMailAttribute        |                                    Define the attribute where alternative mail for an user will be found                                    |               |                                                                                  secondaryMail |

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
| management.metrics.export.prometheus.enabled |             |               |         |

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
