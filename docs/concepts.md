# Concepts

- [Concepts](#concepts)
  - [Use cases](#use-cases)
  - [Terminology](#terminology)
  - [Access control and permissions](#access-control-and-permissions)
    - [Administrator](#administrator)
    - [Writer](#writer)
    - [Reader](#reader)
    - [Password manager](#password-manager)
    - [Application manager](#application-manager)
  - [Providers](#providers)
    - [Realm config provider](#realm-config-provider)
      - [File](#file)
      - [Ldap](#ldap)
    - [Store Providers](#store-providers)
      - [File Store Provider](#file-store-provider)
      - [Ldap Store Provider](#ldap-store-provider)
      - [JMS Store Provider](#jms-store-provider)
    - [Services](#services)
      - [Rest Services](#rest-services)
      - [JMS Services](#jms-services)
      
## Use cases

- Read and write information on users and organization
- Validate user secrets for authentication (password or X.509 client certificate)
- Check users permissions on an application, add and remove permissions
- Manage groups of users for applications

Users, organizations and applications can come from multiple storages and can be functionnaly isolated with realms.

## Terminology

**Realm** : base unit for sugoi, each realm is isolated from one another by permissions. Realm represents a set of sugoi objects (users, organizations, applications) managed together. *Users* can have read or write rights scoped to a realm.

**UserStorage** : storage for users and organizations in a *realm*. A *realm* can have multiple user storages. Each user storage can be at a different location and have its own user storage type (ldap, file, broker) corresponding to a *store provider*.

**Store Provider** : the way to access data. For now there are 3 store providers avalaible : local file, ldap and broker.

**User** : Represents an individual. It bears profile data (name, address, username...). It may also bear roles and authentification secrets and will then represent an account. A user can belong to *groups*.

**Organization** : Represents an organizational unit. An organization can have an address, secrets, suborganization... Users can belong to organizations.

**Application** : Represents an information system with which users and organizations can interact. Applications belong to a realm. Application is used to define roles for users on the corresponding information system with the *group* concept.

**Group** : A group is a unit of an *application* which contains *users*. The information system requesting sugoi uses groups to determine the users' status. Permissions on the information system can be managed via groups.

## Access control and permissions

Users can authenticate on sugoi with basic authentication, bearer authentication or ldap authentication. For now users will have rights on the application only by using ldap authentication. The permissions are checked from groups defined in security.ldap-account-managment-url property (see [configuration](configuration.md)).

There are five profiles on sugoi permission model : admin, read and write, password and application manager. A user have a right when it belong to the corresonding ldap group. All profiles are scoped to realm except the admin group. Sugoi management groups are entirely configurable with patterns, those patterns accept the `*` wildcard. All the pattern configuration should be done in uppercase (as this is the way spring-security fills `Authorities`).

### Administrator

The admin group is defined via the regexp.role.admin property.

An administrator can :

- read, create, modify or delete any realms.
- read, create, modify or delete userstorages.
- do whatever the writer can do on all realms.
- request all actuator endpoints.

### Writer

The writer group is defined via the regexp.role.writer property. It is scoped to one realm.

A writer can :

- modify and create users on the realms they manage
- modify and create organizations on the realms they manage
- modify and create application and groups on the realms they manage
- do whatever a reader can do on the realms they manage.

### Reader

The reader group is defined via the regexp.role.reader property. It is scoped to one realm.

A reader can :

- read users, organizations, applications and groups on the realm they are reader on.

### Password manager

The password manager role is defined via the regexp.role.password.manager property. It is scoped to one realm.

Password manager can initialize passwords, update password (with a given password or not) or validate a password on its realm.

### Application manager

The application manager role is defined via the regexp.role.application.manager property. 

Application manager can update an application and the associated groups (add or remove members, as well as create or delete groups). This also enable reader rights on a realm.

Creating or deleting an application requires writer privileges.

## Providers

### Realm config provider

#### File

This provider loads *realms* from a file described in `fr.insee.sugoi.realm.config.local.path`. The file is fetched through Spring [ResourceLoader](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#resources-resourceloader).

The file is expected to contain an array of realm representations, like : 

```json
[
    {
        "name": "test",
        "url": "/test",
        "appSource": "/data/applications",
        "userStorages": [
            {
                "name": "default",
                "userSource": "/data/users",
                "organizationSource": "/data/organizations",
                "properties": null,
                "readerType": null,
                "writerType": null
            }
        ]
    }
]
```

#### Ldap

This provider loads realm from ldap entries, an exemple ldif file and configuration can be found in the [sugoi-distribution-full-env](../sugoi-api-distribution/sugoi-api-distribution-full-env/src/main/resources) module.

### Store Providers

They are responsible to effectively read and write sugoi objects (users, organizations and applications) in a store.


#### File Store Provider

Each object is stored in a json file located in the folder as configured in the realm.

#### Ldap Store Provider

Sugoi objects are read and written in an ldap. Attributes for each objects are described in  [sugoi-api-ldap-utils](../sugoi-api-sugoi-api-ldap-utils/src/main/java/fr/insee/sugoi/ldap/utils/mapper).

> ⚠️ You will need to use the [provided schema](ldap-schemas/insee.schema) for everything to work.

#### JMS Store Provider

This allows you to send write requests to a JMS queue (tested with ActiveMQ), to be managed by the [JMS Service]() 

### Services

Those are responsible to accept Write or read requests from sugoi users.

#### Rest Services

Simple http rest services.

#### JMS Services

Listen to message on a JMS queue, made to work well with JMS write provider

### SeeAlso

Sometimes user informations that are not stored in the sugoi realm are needed. It can be for instance a logo or an information on another directory. If these informations are free to read it is possible to use the SeeAlso functionnality to fetch those informations.

The SeeAlso functionnality will add attributes to user attributes when reading a seealso link. SeeAlso links can be described as :

url|suboject|attributetoset

with url being the url of the resource to fetch, suboject an instruction to take a subobject of the resource and attributetoset the name of the new attribute which will be added to the user attributes.
Depending on the protocol defined in the url a different parser will be used. The parsers are pluggable.

The resource fetch should be a string or a list of strings. If the resouce can't be retrieved or parsed the attribute will be created but set to null.

To enable the SeeAlso functionality a realm property as to be set : seealso_attributes (see [realm-configuration](realm-configuration.md) for more details). It will enable looking for SeeAlso links in the named attribute. A good default for this property is seealso_attributes: SeeAlso . It is now possible to add SeeAlso links in the SeeAlso attribute of the users. All the links in this attribute will be parse as SeeAlso links.

Two parsers exist for now but other can be added (see a future Development documentation). They have to be added as a module to the application.

#### SeeAlso http+json

If the protocol is http or https then the resource will be considered to be a valid json data.

The url must be a valid http url to a webservice openly readable.
The object describe how to parse the json with node separated by dots and array values chosen with []

A valid seealso would be :

`http://example.org/toto|items[3].name|http_string`

The attribute http_string of the user would be set to the value of the name of the third item of the resource at `http://example.org/toto`

#### SeeAlso ldap

If the protocol is ldap, url is expected to be the value of a ldap url without attribute. The ldap server should be openly readable. The suboject should be the name of the attribute we want to get from the ldap resource.

A valid seealso would be :

ldap://localhost:10389/uid=testc,ou=contacts,ou=clients_domaine1,o=insee,c=fr|cn|ldap_string

The user attribute ldap_string would be set to the cn of the testc resource on localhost:10389 ldap server.
