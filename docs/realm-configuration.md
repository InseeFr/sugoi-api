# Realm and userstorage configuration

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

| Field name   |                        Example                         |                                           Optional |                                                                                                           Default | Description                                                                                                          |
| ------------ | :----------------------------------------------------: | -------------------------------------------------: | ----------------------------------------------------------------------------------------------------------------: | -------------------------------------------------------------------------------------------------------------------- |
| name         |                       "myRealm"                        |                                                 no |                                                                                                                   | Name which identifies the realm.                                                                                     |
| url          |               "example.org", "localhost"               |                                                 no |                                                                                                                   | Url of the resource server.                                                                                          |
| appSource    | "ou=Applications,o=insee,c=fr", "/realm1/applications" |                                                yes |                                                                                                                   | The location of the applications to read on the server. If appSource is not set then applications cannot be managed. |
| userStorages |                See UserStorage section                 | no, the realm should have at least one userstorage |                                                                                                                   | A list of all userstorages the realm is made of.                                                                     |
| properties   |                    See next section                    |                                                yes |                                                                                                               {}  | A list of other options which can be specific to the type of Store Provider.                                         |
| readerType   |          "LdapReaderStore", "FileReaderStore"          |                                                 no |   the default can be set via the instance property : fr.insee.sugoi.store.readerType, if not set app cannot work  | Indicates wich type of store is used for reading. This attribute is read-only for now and should be set via default. |
| writeType    | "JMSWriterStore", "LdapWriterStore", "FileWriterStore" |                                                 no |  the default can be set via the instance property : fr.insee.sugoi.store.writerType?, if not set app cannot work  | Indicates wich type of store is used for writing. This attribute is read-only for now and should be set via default. |
### Realm configuration properties

| Field name                          |                      Example                      |                                                                                Optional | Default | Description                                                                                                                                                                                                                |
| ----------------------------------- | :-----------------------------------------------: | --------------------------------------------------------------------------------------: | ------: | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| seealso_attributes                  |              "seeAlso, otherSeeAlso"              |                                                no, set to enable seealso functionnality |         | All values in the corresponding attributes will be parsed as [SeeAlso](concepts.md#SeeAlso) string to add a new attributes to a user. It can be a single attribute name or a list of attribute names separated by a comma. |
| app-managed-attribute-keys-list     |    "inseeGroupeDefaut, inseeGroupeApplicatif"     | yes, it just allow person with app right to give (or remove) properties on an attribute |         | The name of the attribute to modify                                                                                                                                                                                        |
| app-managed-attribute-patterns-list | "(.\*)\_$(application),$(application)\\$\\$(.\*)" |                                                                                     yes |         | The pattern that the attribute value must follow                                                                                                                                                                           |
| vlv_enabled        |      true or false      |                 yes, disabled by default |         | Allowed to make vlv search on ldap                                                                                                                                                                                         |
| sort_key           |           uid           |                                       no |         | Attribute on which ordered will be done when making a paging request    



## UserStorage configuration

A UserStorage is a logical division of a Realm.
These configuration should be set for each UserStorage contained in a Realm :

| Field name         |                                    Example                                    |                                                                                   Optional |                                                                              Default | Description                                                                                                                                             |
| ------------------ | :---------------------------------------------------------------------------: | -----------------------------------------------------------------------------------------: | -----------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| name               |                                "myUserStorage"                                |                                                                                         no |                                                                                      | Name which identifies the userstorage in the realm                                                                                                      |
| userSource         |        "ou=contacts,ou=clients_domaine1,o=insee,c=fr", "/realm1/users"        |                                                                                         no |                                                                                      | The location of the users to read on the server.                                                                                                        |
| organizationSource |  "ou=organisations,ou=clients_domaine1,o=insee,c=fr", "/realm1/organizations" |                                                                                        yes |                                                                                      | The location of the organization to read on the server. If organizationSource is not set, then organizations cannot be managed.                         |
| addressSource      |                "ou=adresses,ou=clients_domaine1,o=insee,c=fr"                 | Only used for ldap storage. Is needed with ldap storage for now but should become optional |                                                                                      | Addresses are stored as an independant resource in ldap storage. addressSource indicates the location of users and organizations address on the server. |
| properties         |                                                                               |                         might be needed depending on the type of store (see next sections) |                                                                                      | A list of other options which can be specific to the type of Store Provider.                                                                            |
| readerType         |                     "LdapReaderStore", "FileReaderStore"                      |                                                                                         no |  the default can be set via the instance property : fr.insee.sugoi.store.readerType  | Indicates wich type of store is used for reading. This attribute is read-only for now and should be set via default.                                    |
| writeType          |            "JMSWriterStore", "LdapWriterStore", "FileWriterStore"             |                                                                                         no |  the default can be set via the instance property : fr.insee.sugoi.store.writerType  | Indicates wich type of store is used for writing. This attribute is read-only for now and should be set via default.                                    |

### Userstorage properties with a LDAP Store Provider

With an LDAP Store Provider the properties can be :

|  Key                 |                               Example                                | Optional |                                                                                             Default | Description                                                                                                                                                                                 |
| -------------------- | :------------------------------------------------------------------: | -------: | --------------------------------------------------------------------------------------------------: | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| group_filter_pattern |                     "(cn={group}\_{appliname})"                      |      yes | the default can be set via the instance property : fr.insee.sugoi.ldap.default.group_filter_pattern | Describe how should be name a group. {appliname} is replaced by the name of the application the group belongs to and {group} is replaced by a group name. If not set, cannot manage groups. |
| group_source_pattern | "ou={appliname}\_Objets,ou={appliname},ou=Applications,o=insee,c=fr" |      yes | the default can be set via the instance property : fr.insee.sugoi.ldap.default.group_source_pattern | Describe where a group belonging to the application of name {appliname} should be fetch.                                                                                                    |
