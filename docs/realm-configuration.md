# Realm and userstorage configuration

- [Realm and userstorage configuration](#realm-and-userstorage-configuration)
  - [Realm configuration](#realm-configuration)
    - [Ui mapping](#ui-mapping)
    - [Realm configuration properties](#realm-configuration-properties)
      - [Realm configuration properties on password](#realm-configuration-properties-on-password)
  - [UserStorage configuration](#userstorage-configuration)
    - [Generic UserStorage properties](#generic-userstorage-properties)
    - [Userstorage properties with a LDAP Store Provider](#userstorage-properties-with-a-ldap-store-provider)
  - [Realm and Userstorage mappings with a LDAP Store Provider](#realm-and-userstorage-mappings-with-a-ldap-store-provider)

A realm can be created, modified or deleted from Sugoi. This documentation aims to explain each possible configuration. Some configurations depends on the type of the Store Provider.

Here is an example of a Realm which is configured to be used by an Ldap Store Provider :

```json
{
  "name": "myRealm",
  "url": "example.org",
  "appSource": "ou=Applications,o=insee,c=fr",
  "userStorages": [
    {
      "name": "Profil_domaine1_WebServiceLdap",
      "userSource": "ou=contacts,ou=clients_domaine1,o=insee,c=fr",
      "organizationSource": "ou=organisations,ou=clients_domaine1,o=insee,c=fr",
      "addressSource": "ou=adresses,ou=clients_domaine1,o=insee,c=fr",
      "properties": {
        "group_filter_pattern": "(cn={group}_{appliname})",
        "group_source_pattern": "ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"
      }
    }
  ],
  "readerType": null,
  "writerType": null,
  "properties": {}
}
```

## Realm configuration

These configurations are used for all type of Store Provider.

| Field name   |                                      Example                                       |                                           Optional |                                                                                                           Default | Description                                                                                                            |
| ------------ | :--------------------------------------------------------------------------------: | -------------------------------------------------: | ----------------------------------------------------------------------------------------------------------------: | ---------------------------------------------------------------------------------------------------------------------- |
| name         |                                     "myRealm"                                      |                                                 no |                                                                                                                   | Name which identifies the realm.                                                                                       |
| url          |                             "example.org", "localhost"                             |                                                 no |                                                                                                                   | Url of the resource server.                                                                                            |
| port |               389               |                                                 yes |  fr.insee.sugoi.ldap.default.port                                                                                                                   | Port of the resource server.                                                                                            |
| appSource    |               "ou=Applications,o=insee,c=fr", "/realm1/applications"               |                                                yes |                                                                                                                   | The location of the applications to read on the server. If appSource is not set then applications cannot be managed.   |
| userStorages |                              See UserStorage section                               | no, the realm should have at least one userstorage |                                                                                                                   | A list of all userstorages the realm is made of.                                                                       |
| properties   |             See [Properties section](#realm-configuration-properties)              |                                                yes |                                                                                                               {}  | A list of other options which can be specific to the type of Store Provider.                                           |
| readerType   |                        "LdapReaderStore", "FileReaderStore"                        |                                                 no |   the default can be set via the instance property : fr.insee.sugoi.store.readerType, if not set app cannot work  | Indicates wich type of store is used for reading. This attribute is read-only for now and should be set via default.   |
| writeType    |               "JMSWriterStore", "LdapWriterStore", "FileWriterStore"               |                                                 no |  the default can be set via the instance property : fr.insee.sugoi.store.writerType?, if not set app cannot work  | Indicates wich type of store is used for writing. This attribute is read-only for now and should be set via default.   |
| mappings     | See [mappings section](#realm-and-userstorage-mappings-with-a-ldap-store-provider) |                                                yes |                                see [mappings section](#realm-and-userstorage-mappings-with-a-ldap-store-provider) | Description of how to map Sugoi application and group attributes with ldap attributes when using a ldap store provider |
| uiMapping    |                       See [ui mapping section](#ui-mappings)                       |                                                yes |                                                                                                                   | Declarative descriptions of fields constiuting a user and an organization                                              |

### UI Mappings

UI mappings are the descriptions of what constitute a user and an organization in Sugoi. These mapping are used to provide metadatas to Sugoi users.

Realm ui mappings is a map containing two list of ui mapping : a uiUserMapping list and a uiOrganizationMapping list.

A UI mapping is defined as follow :

name;helpTextTitle;helpText;path;type;modifiable;tag[;order;required;option=value...]

with :

- name : a short name for the field
- helpTextTitle : the human readable name of the field
- helpText : an extensive explanation of the field
- path : an informative value indicating where the field comes from
- type : string, list_string...
- modifiable : true or false depending of the field modification status
- tag : a tag defining a category for the field
- order : an int to order the different fields
- required: if field must be tag as required on the ui side

A list of custom key/values can be added at the end.

### Realm configuration properties

| Field name                          |                      Example                      |                                                                                Optional |                                    Default | Description                                                                                                                                                                                                                |
|-------------------------------------| :-----------------------------------------------: |----------------------------------------------------------------------------------------:|-------------------------------------------:|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| seealso_attributes                  |              "seeAlso, otherSeeAlso"              |                                                no, set to enable seealso functionnality |                                            | All values in the corresponding attributes will be parsed as [SeeAlso](concepts.md#SeeAlso) string to add a new attributes to a user. It can be a single attribute name or a list of attribute names separated by a comma. |
| app-managed-attribute-keys-list     |    "inseeGroupeDefaut, inseeGroupeApplicatif"     | yes, it just allow person with app right to give (or remove) properties on an attribute |                                            | The name of the attribute to modify                                                                                                                                                                                        |
| app-managed-attribute-patterns-list | "(.\*)\_$(application),$(application)\\$\\$(.\*)" |                                                                                     yes |                                            | The pattern that the attribute value must follow                                                                                                                                                                           |
| vlv_enabled                         |                   true or false                   |                                                                yes, disabled by default |                                            | Allowed to make vlv search on ldap                                                                                                                                                                                         |
| sort_key                            |                        uid                        |                                                                                      no |                                            | Attribute on which ordered will be done when making a paging request                                                                                                                                                       |
| usersMaxOutputSize                  | 100 |                                                                                     yes |         fr.insee.sugoi.users.maxoutputsize | The maximum number of user outputs allowed                                                                                                                                                                                 |
| groupsMaxOutputSize                 | 100 |                                                                                     yes |        fr.insee.sugoi.groups.maxoutputsize | The maximum number of grouos outputs allowed                                                                                                                                                                               |
| applicationsMaxOutputSize           | 100 |                                                                                     yes |  fr.insee.sugoi.applications.maxoutputsize | The maximum number of applications outputs allowed                                                                                                                                                                         |  
| organizationsMaxOutputSize          | 100 |                                                                                     yes | fr.insee.sugoi.organizations.maxoutputsize | The maximum number of organizations outputs allowed                                                                                                                                                                        |  

#### Realm configuration properties on password

Realm properties on password generation and validation. See [general configuration](configuration.md#password-configuration) for default values.

| Field name                          |                      Example                      | Default | Description |
| ----------------------------- | :---------: | :------------: | ------------------------------------ |
| create_password_WITHUpperCase | true | fr.insee.sugoi.password.create.withUpperCase | Set if a sugoi generated password is generated with uppercase |
| create_password_WITHLowerCase | false | fr.insee.sugoi.password.create.withLowerCase | Set if a sugoi generated password is generated with lowercase |
| create_password_WITHDigit | true | fr.insee.sugoi.password.create.withDigits | Set if a sugoi generated password is generated with digit |
| create_password_WITHSpecial | true | fr.insee.sugoi.password.create.withSpecial | Set if a sugoi generated password is generated with special characters |
| create_password_size | 20 | fr.insee.sugoi.password.create.length | Set the size of a sugoi generated password |
| validate_password_WITHUpperCase | false | fr.insee.sugoi.password.validate.withUpperCase | Define if a new password provided by the user is valid if it has no uppercase |
| validate_password_WITHLowerCase | true | fr.insee.sugoi.password.validate.withLowerCase | Define if a new password provided by the user is valid if it has no lowercase |
| validate_password_WITHDigit | true | fr.insee.sugoi.password.validate.withDigits | Define if a new password provided by the user is valid if it has no digit |
| validate_password_WITHSpecial | true | fr.insee.sugoi.password.validate.withSpecial | Define if a new password provided by the user is valid if it has no special characters |
| validate_password_size | 20 | fr.insee.sugoi.password.validate.minimal.length | Define a minimum size to check if a new password provided by the user is valid |

## UserStorage configuration

A UserStorage is a logical division of a Realm. 
These configuration should be set for each UserStorage contained in a Realm :

| Field name                   |                                      Example                                       |                                                                                   Optional |                                                                              Default | Description                                                                                                                                             |
| ---------------------------- | :--------------------------------------------------------------------------------: | -----------------------------------------------------------------------------------------: | -----------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| name                         |                                  "myUserStorage"                                   |                                                                                         no |                                                                                      | Name which identifies the userstorage in the realm                                                                                                      |
| userSource                   |          "ou=contacts,ou=clients_domaine1,o=insee,c=fr", "/realm1/users"           |                                                                                         no |                                                                                      | The location of the users to read on the server.                                                                                                        |
| organizationSource           |    "ou=organisations,ou=clients_domaine1,o=insee,c=fr", "/realm1/organizations"    |                                                                                        yes |                                                                                      | The location of the organization to read on the server. If organizationSource is not set, then organizations cannot be managed.                         |
| addressSource                |                   "ou=adresses,ou=clients_domaine1,o=insee,c=fr"                   | Only used for ldap storage. Is needed with ldap storage for now but should become optional |                                                                                      | Addresses are stored as an independant resource in ldap storage. addressSource indicates the location of users and organizations address on the server. |
| properties                   |                                                                                    |                         might be needed depending on the type of store (see next sections) |                                                                                      | A list of other options which can be specific to the type of Store Provider.                                                                            |
| readerType                   |                        "LdapReaderStore", "FileReaderStore"                        |                                                                                         no |  the default can be set via the instance property : fr.insee.sugoi.store.readerType  | Indicates wich type of store is used for reading. This attribute is read-only for now and should be set via default.                                    |
| writeType                    |               "JMSWriterStore", "LdapWriterStore", "FileWriterStore"               |                                                                                         no |  the default can be set via the instance property : fr.insee.sugoi.store.writerType  | Indicates wich type of store is used for writing. This attribute is read-only for now and should be set via default.                                    |
| mappings                     | see [mappings section](#realm-and-userstorage-mappings-with-a-ldap-store-provider) |                                             should be set when using a ldap store provider |   see [mappings section](#realm-and-userstorage-mappings-with-a-ldap-store-provider) | Description of how to map Sugoi user and organization attributes with ldap attributes when using a ldap store provider                                  |
| group_manager_source_pattern |                   "uid=ASI\_$(app),ou=Applications,o=insee,c=fr"                   |                 should be set when wanted to have a kind of group of group manager for app |                                                                                      | Description of where to put user who can manage apps groups                                                                                             |

### Generic UserStorage properties

Those are optional properties to set on a userstorage. If the property is not set at the userstorage level, 
the corresponding realm property will be used as default if set. 

| Key                                                                          |                                                                                                             Description                                                                                                              |
| ---------------------------------------------------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| {name}\_send_login_template with name being a configured external webservice |   A template to complete and send to the {name} webservice on /send-login call (see [Notify external webservices](concepts.md#notify-external-webservices) and [Webhooks configuration](configuration.md#webhooks-configuration))    |
| {name}\_reset_template with name being a configured webservice               | A template to complete and send to the {name} webservice on /reinit-password call (see [Notify external webservices](concepts.md#notify-external-webservices) and [Webhooks configuration](configuration.md#webhooks-configuration)) |
| {name}\_changepwd_template with name being a configured webservice           | A template to complete and send to the {name} webservice on /change-password call (see [Notify external webservices](concepts.md#notify-external-webservices) and [Webhooks configuration](configuration.md#webhooks-configuration)) |

### Userstorage properties with a LDAP Store Provider

With an LDAP Store Provider the properties can be :

|  Key                      |                               Example                                | Optional |                                                                                             Default | Description                                                                                                                                                                                 |
| ------------------------- | :------------------------------------------------------------------: | -------: | --------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| group_filter_pattern      |                     "(cn={group}\_{appliname})"                      |      yes | the default can be set via the instance property : fr.insee.sugoi.ldap.default.group_filter_pattern | Describe how should be name a group. {appliname} is replaced by the name of the application the group belongs to and {group} is replaced by a group name. If not set, cannot manage groups. |
| group_source_pattern      | "ou={appliname}\_Objets,ou={appliname},ou=Applications,o=insee,c=fr" |      yes | the default can be set via the instance property : fr.insee.sugoi.ldap.default.group_source_pattern | Describe where a group belonging to the application of name {appliname} should be fetch.                                                                                                    |
| user_object_class         |                             "top,person"                             |      yes |                     value of property fr.insee.sugoi.ldap.default.user-object-classes or top,person | Object classes to put on a new user in ldap storage                                                                                                                                         |
| organization_object_class |                          "top,organization"                          |      yes |       value of property fr.insee.sugoi.ldap.default.organization-object-classes or top,organization | Object classes to put on a new organization in ldap storage                                                                                                                                 |
| group_object_class        |                       "top,groupOfUniqueNames"                       |      yes |        value of property fr.insee.sugoi.ldap.default.group-object-classes or top,groupOfUniqueNames | Object classes to put on a new group in ldap storage                                                                                                                                        |
| application_object_class  |                       "top,organizationalUnit"                       |      yes |  value of property fr.insee.sugoi.ldap.default.application-object-classes or top,organizationalUnit | Object classes to put on a new application in ldap storage                                                                                                                                  |
| address_object_class      |                            "top,locality"                            |      yes |                 value of property fr.insee.sugoi.ldap.default.adress-object-classes or top,locality | Object classes to put on a new address in ldap storage **not supported yet**                                                                                                                |

## Realm and Userstorage mappings with a LDAP Store Provider

When using a ldap store provider, the administrator should set how it expects to map the sugoi value with the ldap attributes. For example, in a userstorage one might want to have the name of the user to be "cn" while in an other userstorage it'd rather be "sn".
The configuration should be done for users and for organizations, groups and applications if these features are intended to be used.

For each of these objects a list of mapping must be set.

A mapping is described as a key value with value "[name of the sugoi attribute in the object] and key [attribute name in ldap],[type of the object in sugoi (see available list of type)],[ro or rw depending on if the attribute can be modified]

To set an attribute in the metadatas or attributes map of an objet the name of the sugoi attribute should be metadatas.myField or attributes.myField. Thus no other dot is allowed in a sugoi attribute name.

Type of attribute can be :

- string
- organization
- address
- list_habilitation
- list_user
- list_group
- list_string
- exists (retrieve if the attribute exists instead of its value)

Mapping is to be set at the userstorage level for users and organizations and at the realm level for groupes and applications.

In the realm mappings map :
|  Key | Example | Optional | Default | Description |
| -------------------- | :------------------------------------------------------------------: | -------: | --------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| userMapping | {"username": "uid,String,rw", "groups": "memberOf,list_group,ro", "habilitations": "inseeGroupeDefaut,list_habilitation,rw"} | should be set when using an ldap store provider | List of mappings between sugoi user attributes and ldap attributes | parsed from the value of property fr.insee.sugoi.ldap.default.user-mapping |
| organizationMapping | {"identifiant": "uid,String,rw", "address": "inseeAdressePostaleDN,address,rw","organization": "inseeOrganisationDN,organization,rw"} | yes | List of mappings between sugoi organization attributes and ldap attributes | parsed from the value of property fr.insee.sugoi.ldap.default.organization-mapping |

In the userstorage mappings map :
|  Key | Example | Optional | Default | Description |
| -------------------- | :------------------------------------------------------------------: | -------: | --------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| groupMapping | {"name": "cn,String,rw","description": "description,String,rw",users": "uniquemember,list_user,rw"} | yes | List of mappings between sugoi group attributes and ldap attributes | parsed from the value of property fr.insee.sugoi.ldap.default.group-mapping |
| applicationMapping | {"name": "ou,String,rw"} | yes | List of mappings between sugoi application attributes and ldap attributes | parsed from the value of property fr.insee.sugoi.ldap.default.application-mapping |
