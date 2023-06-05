Feature: Realms scenario synchrone
    Performing actions on realms

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get realms
        When the client perform GET request on url /realms
        And show body received
        Then the client receives status code 200
        Then the client expect to have realms access

    Scenario: Get realm not exist
        When the client perform GET request on url /realms?id=eoreotggfg
        And show body received
        Then the client receives status code 404

    Scenario: Create new realm
        When the client perform POST request with body on url /realms body:
            """
            {
              "name": "newrealm",
              "url": "localhost",
              "appSource": "ou=Applications,o=insee,c=fr",
              "properties": {
                "app-managed-attribute-keys-list": ["inseegroupedefaut,inseeroleapplicatif"],
                "app-managed-attribute-patterns-list": ["(.*)_$(application),$(application)\\$\\$(.*)"],
                "description": ["Le profil domaine2<br/>Test <b>html</b> in description"]
              },
              "userStorages": [
                {
                  "name": "default",
                  "userSource": "ou=contacts,ou=clients_domaine1,o=insee,c=fr",
                  "organizationSource": "ou=organisations,ou=clients_domaine1,o=insee,c=fr",
                  "addressSource": "ou=adresses,ou=clients_domaine1,o=insee,c=fr",
                  "properties": {
                    "group_filter_pattern": ["(cn={group}_{appliname})"],
                    "organization_object_classes": ["top,inseeOrganisation"],
                    "user_object_classes": ["top,inseeCompte,inseeContact,inseeAttributsAuthentification,inseeAttributsHabilitation,inseeAttributsCommunication"],
                    "group_source_pattern": ["ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"]
                  },
                  "userMappings": [
                    {
                      "sugoiName": "username",
                      "storeName": "uid",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "lastName",
                      "storeName": "sn",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "mail",
                      "storeName": "mail",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "firstName",
                      "storeName": "givenname",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.common_name",
                      "storeName": "cn",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.personal_title",
                      "storeName": "personalTitle",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.description",
                      "storeName": "description",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.phone_number",
                      "storeName": "telephoneNumber",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "habilitations",
                      "storeName": "inseeGroupeDefaut",
                      "modelType": "LIST_HABILITATION",
                      "writable": true
                    },
                    {
                      "sugoiName": "organization",
                      "storeName": "inseeOrganisationDN",
                      "modelType": "ORGANIZATION",
                      "writable": true
                    },
                    {
                      "sugoiName": "address",
                      "storeName": "inseeAdressePostaleDN",
                      "modelType": "ADDRESS",
                      "writable": true
                    },
                    {
                      "sugoiName": "groups",
                      "storeName": "memberOf",
                      "modelType": "LIST_GROUP",
                      "writable": false
                    },
                    {
                      "sugoiName": "attributes.insee_roles_applicatifs",
                      "storeName": "inseeRoleApplicatif",
                      "modelType": "LIST_STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.hasPassword",
                      "storeName": "userPassword",
                      "modelType": "EXISTS",
                      "writable": false
                    },
                    {
                      "sugoiName": "metadatas.modifyTimestamp",
                      "storeName": "modifyTimestamp",
                      "modelType": "STRING",
                      "writable": false
                    },
                    {
                      "sugoiName": "attributes.additionalMail",
                      "storeName": "inseeMailCorrespondant",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.passwordShouldBeReset",
                      "storeName": "pwdReset",
                      "modelType": "STRING",
                      "writable": false
                    }
                  ],
                  "organizationMappings": [
                    {
                      "sugoiName": "identifiant",
                      "storeName": "uid",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.description",
                      "storeName": "description",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.mail",
                      "storeName": "mail",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "address",
                      "storeName": "inseeAdressePostaleDN",
                      "modelType": "ADDRESS",
                      "writable": true
                    },
                    {
                      "sugoiName": "organization",
                      "storeName": "inseeOrganisationDN",
                      "modelType": "ORGANIZATION",
                      "writable": true
                    }
                  ]
                }
              ],
              "applicationMappings": [],
              "groupMappings": [],
              "uiMapping": {
                "uiOrganizationMapping": [],
                "uiUserMapping": [
                  {
                    "name": "Identifiant",
                    "helpTextTitle": "Identifiant de l'utilisateur",
                    "helpText": "Il servira pour le rechercher a travers l'annuaire. Caracteres autorises alphabetiques chiffres apostrophes espaces tirets",
                    "path": "username",
                    "type": "string",
                    "required": true,
                    "modifiable": true,
                    "tag": "main",
                    "order": 2147483647,
                    "options": {}
                  }
                ]
              },
              "readerType": "LdapReaderStore",
              "writerType": "LdapWriterStore"
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect uiUserMapping to contain Identifiant
    
    Scenario: Update realm
        When the client perform PUT request with body on url /realms/newrealm body:
            """
            {
              "name": "newrealm",
              "url": "localhost",
              "appSource": "ou=Applications,o=insee,c=fr",
              "properties": {
                "app-managed-attribute-keys-list": ["inseegroupedefaut,inseeroleapplicatif"],
                "app-managed-attribute-patterns-list": ["(.*)_$(application),$(application)\\$\\$(.*)"],
                "description": ["I'm updated"]
              },
              "userStorages": [
                {
                  "name": "default",
                  "userSource": "ou=contacts,ou=clients_domaine1,o=insee,c=fr",
                  "organizationSource": "ou=organisations,ou=clients_domaine1,o=insee,c=fr",
                  "addressSource": "ou=adresses,ou=clients_domaine1,o=insee,c=fr",
                  "properties": {
                    "group_filter_pattern": ["(cn={group}_{appliname})"],
                    "organization_object_classes": ["top,inseeOrganisation"],
                    "user_object_classes": ["top,inseeCompte,inseeContact,inseeAttributsAuthentification,inseeAttributsHabilitation,inseeAttributsCommunication"],
                    "group_source_pattern": ["ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"]
                  },
                  "userMappings": [
                    {
                      "sugoiName": "username",
                      "storeName": "uid",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "lastName",
                      "storeName": "sn",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "mail",
                      "storeName": "mail",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "firstName",
                      "storeName": "givenname",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.common_name",
                      "storeName": "cn",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.personal_title",
                      "storeName": "personalTitle",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.description",
                      "storeName": "description",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.phone_number",
                      "storeName": "telephoneNumber",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "habilitations",
                      "storeName": "inseeGroupeDefaut",
                      "modelType": "LIST_HABILITATION",
                      "writable": true
                    },
                    {
                      "sugoiName": "organization",
                      "storeName": "inseeOrganisationDN",
                      "modelType": "ORGANIZATION",
                      "writable": true
                    },
                    {
                      "sugoiName": "address",
                      "storeName": "inseeAdressePostaleDN",
                      "modelType": "ADDRESS",
                      "writable": true
                    },
                    {
                      "sugoiName": "groups",
                      "storeName": "memberOf",
                      "modelType": "LIST_GROUP",
                      "writable": false
                    },
                    {
                      "sugoiName": "attributes.insee_roles_applicatifs",
                      "storeName": "inseeRoleApplicatif",
                      "modelType": "LIST_STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.hasPassword",
                      "storeName": "userPassword",
                      "modelType": "EXISTS",
                      "writable": false
                    },
                    {
                      "sugoiName": "metadatas.modifyTimestamp",
                      "storeName": "modifyTimestamp",
                      "modelType": "STRING",
                      "writable": false
                    },
                    {
                      "sugoiName": "attributes.additionalMail",
                      "storeName": "inseeMailCorrespondant",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.passwordShouldBeReset",
                      "storeName": "pwdReset",
                      "modelType": "STRING",
                      "writable": false
                    }
                  ],
                  "organizationMappings": [
                    {
                      "sugoiName": "identifiant",
                      "storeName": "uid",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.description",
                      "storeName": "description",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "attributes.mail",
                      "storeName": "mail",
                      "modelType": "STRING",
                      "writable": true
                    },
                    {
                      "sugoiName": "address",
                      "storeName": "inseeAdressePostaleDN",
                      "modelType": "ADDRESS",
                      "writable": true
                    },
                    {
                      "sugoiName": "organization",
                      "storeName": "inseeOrganisationDN",
                      "modelType": "ORGANIZATION",
                      "writable": true
                    }
                  ]
                }
              ],
              "applicationMappings": [],
              "groupMappings": [],
              "uiMapping": {
                "uiOrganizationMapping": [],
                "uiUserMapping": [
                  {
                    "name": "Identifiant",
                    "helpTextTitle": "Identifiant de l'utilisateur",
                    "helpText": "Il servira pour le rechercher a travers l'annuaire. Caracteres autorises alphabetiques chiffres apostrophes espaces tirets",
                    "path": "username",
                    "type": "string",
                    "required": true,
                    "modifiable": true,
                    "tag": "main",
                    "order": 2147483647,
                    "options": {}
                  },
                  {
                    "name": "Nom commun",
                    "helpTextTitle": "Nom commun de l'utilisateur",
                    "helpText": "Caracteres autorises alphabetiques chiffres apostrophes espaces tirets",
                    "path": "attributes.common_name",
                    "type": "string",
                    "required": false,
                    "modifiable": true,
                    "tag": "main",
                    "order": 2147483647,
                    "options": {}
                  },
                  {
                    "name": "Nom",
                    "helpTextTitle": "Nom de l'utilisateur",
                    "helpText": "Caracteres autorises alphabetiques chiffres apostrophes espaces tirets",
                    "path": "lastName",
                    "type": "string",
                    "required": false,
                    "modifiable": true,
                    "tag": "main",
                    "order": 2147483647,
                    "options": {}
                  }
                ]
              },
              "readerType": "LdapReaderStore",
              "writerType": "LdapWriterStore"
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect description to be I'm updated
        Then the client expect uiUserMapping to contain Identifiant
        Then the client expect uiUserMapping to contain Nom Commun
    
    Scenario: Delete realm
        When the client perform DELETE request on url /realms/newrealm
        And show body received
        Then the client receives status code 204

    Scenario: Realm is deleted
        When the client perform GET request on url /realms?id=newrealm
        And show body received
        Then the client receives status code 404
