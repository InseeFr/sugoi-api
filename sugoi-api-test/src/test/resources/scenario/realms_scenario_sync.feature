Feature: Realms scenario asynchrone
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
                "userStorages": [
                {
                    "name": "Profil_domaine1_WebServiceLdap",
                    "userSource": "ou=contacts,ou=clients_domaine1,o=insee,c=fr",
                    "organizationSource": "ou=organisations,ou=clients_domaine1,o=insee,c=fr",
                    "addressSource": "ou=adresses,ou=clients_domaine1,o=insee,c=fr",
                    "properties": {
                    "group_filter_pattern": "(cn={group}_{appliname})",
                    "organization_object_classes": "top,inseeOrganisation",
                    "user_object_classes": "top,inseeCompte,inseeContact,inseeAttributsAuthentification,inseeAttributsHabilitation,inseeAttributsCommunication",
                    "group_source_pattern": "ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"
                    },
                    "mappings": {
                    "organizationMapping": {
                        "address": "inseeAdressePostaleDN,address,rw",
                        "attributes.description": "description,String,rw",
                        "identifiant": "uid,String,rw",
                        "organization": "inseeOrganisationDN,organization,rw",
                        "attributes.mail": "mail,String,rw",
                        "gpgkey": "inseeClefChiffrement,byte_array,ro"
                    },
                    "userMapping": {
                        "attributes.identifiant_metier": "inseeIdentifiantMetier,String,rw",
                        "lastName": "sn,String,rw",
                        "address": "inseeAdressePostaleDN,address,rw",
                        "mail": "mail,String,rw",
                        "attributes.description": "description,String,rw",
                        "attributes.common_name": "cn,String,rw",
                        "attributes.phone_number": "telephoneNumber,String,rw",
                        "attributes.telephone_portable": "inseenumerotelephoneportable,String,rw",
                        "certificate": "userCertificate;binary,byte_array,ro",
                        "habilitations": "inseeGroupeDefaut,list_habilitation,rw",
                        "groups": "memberOf,list_group,ro",
                        "attributes.properties": "inseePropriete,list_string,rw",
                        "metadatas.modifyTimestamp": "modifyTimestamp,string,ro",
                        "attributes.additionalMail": "inseeMailCorrespondant,String,rw",
                        "attributes.seeAlsos": "seeAlso,list_string,ro,singl",
                        "attributes.personal_title": "personalTitle,String,rw",
                        "attributes.insee_timbre": "inseeTimbre,String,rw",
                        "attributes.repertoire_distribution": "inseerepertoirededistribution,String,rw",
                        "firstName": "givenname,String,rw",
                        "attributes.insee_organisme": "inseeOrganisme,String,rw",
                        "organization": "inseeOrganisationDN,organization,rw",
                        "attributes.hasPassword": "userPassword,exists,ro",
                        "attributes.insee_roles_applicatifs": "inseeRoleApplicatif,list_string,rw",
                        "username": "uid,String,rw"
                    }
                    }
                }
                ],
                "properties": {
                    "app-managed-attribute-keys-list": "inseegroupedefaut,inseeroleapplicatif",
                    "app-managed-attribute-patterns-list": "(.*)_$(application),$(application)\\$\\$(.*)",
                    "description": "Le profil domaine2<br/>Test <b>html</b> in description"
                },
                "mappings": {
                "applicationMapping": {
                    "name": "ou,String,rw"
                },
                "groupMapping": {
                    "name": "cn,String,rw",
                    "description": "description,String,rw",
                    "users": "uniquemember,list_user,rw"
                }
                },
                "uiMapping": {
                "uiOrganizationMapping": [],
                "uiUserMapping": [
                ]
                },
                "readerType": "LdapReaderStore",
                "writerType": "LdapWriterStore"
            }
            """
        And show body received
        Then the client receives status code 201
    
    Scenario: Update realm
        When the client perform PUT request with body on url /realms/newrealm body:
            """
            {
                "name": "newrealm",
                "url": "localhost",
                "appSource": "ou=Applications,o=insee,c=fr",
                "userStorages": [
                {
                    "name": "Profil_domaine1_WebServiceLdap",
                    "userSource": "ou=contacts,ou=clients_domaine1,o=insee,c=fr",
                    "organizationSource": "ou=organisations,ou=clients_domaine1,o=insee,c=fr",
                    "addressSource": "ou=adresses,ou=clients_domaine1,o=insee,c=fr",
                    "properties": {
                    "group_filter_pattern": "(cn={group}_{appliname})",
                    "organization_object_classes": "top,inseeOrganisation",
                    "user_object_classes": "top,inseeCompte,inseeContact,inseeAttributsAuthentification,inseeAttributsHabilitation,inseeAttributsCommunication",
                    "group_source_pattern": "ou={appliname}_Objets,ou={appliname},ou=Applications,o=insee,c=fr"
                    },
                    "mappings": {
                    "organizationMapping": {
                        "address": "inseeAdressePostaleDN,address,rw",
                        "attributes.description": "description,String,rw",
                        "identifiant": "uid,String,rw",
                        "organization": "inseeOrganisationDN,organization,rw",
                        "attributes.mail": "mail,String,rw",
                        "gpgkey": "inseeClefChiffrement,byte_array,ro"
                    },
                    "userMapping": {
                        "attributes.identifiant_metier": "inseeIdentifiantMetier,String,rw",
                        "lastName": "sn,String,rw",
                        "address": "inseeAdressePostaleDN,address,rw",
                        "mail": "mail,String,rw",
                        "attributes.description": "description,String,rw",
                        "attributes.common_name": "cn,String,rw",
                        "attributes.phone_number": "telephoneNumber,String,rw",
                        "attributes.telephone_portable": "inseenumerotelephoneportable,String,rw",
                        "certificate": "userCertificate;binary,byte_array,ro",
                        "habilitations": "inseeGroupeDefaut,list_habilitation,rw",
                        "groups": "memberOf,list_group,ro",
                        "attributes.properties": "inseePropriete,list_string,rw",
                        "metadatas.modifyTimestamp": "modifyTimestamp,string,ro",
                        "attributes.additionalMail": "inseeMailCorrespondant,String,rw",
                        "attributes.seeAlsos": "seeAlso,list_string,ro,singl",
                        "attributes.personal_title": "personalTitle,String,rw",
                        "attributes.insee_timbre": "inseeTimbre,String,rw",
                        "attributes.repertoire_distribution": "inseerepertoirededistribution,String,rw",
                        "firstName": "givenname,String,rw",
                        "attributes.insee_organisme": "inseeOrganisme,String,rw",
                        "organization": "inseeOrganisationDN,organization,rw",
                        "attributes.hasPassword": "userPassword,exists,ro",
                        "attributes.insee_roles_applicatifs": "inseeRoleApplicatif,list_string,rw",
                        "username": "uid,String,rw"
                    }
                    }
                }
                ],
                "properties": {
                    "app-managed-attribute-keys-list": "inseegroupedefaut,inseeroleapplicatif",
                    "app-managed-attribute-patterns-list": "(.*)_$(application),$(application)\\$\\$(.*)",
                    "description": "I'm updated"
                },
                "mappings": {
                "applicationMapping": {
                    "name": "ou,String,rw"
                },
                "groupMapping": {
                    "name": "cn,String,rw",
                    "description": "description,String,rw",
                    "users": "uniquemember,list_user,rw"
                }
                },
                "uiMapping": {
                "uiOrganizationMapping": [],
                "uiUserMapping": [
                ]
                },
                "readerType": "LdapReaderStore",
                "writerType": "LdapWriterStore"
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect description to be I'm updated
    
    Scenario: Delete realm
        When the client perform DELETE request on url /realms/newrealm
        And show body received
        Then the client receives status code 204

    Scenario: Realm is deleted
        When the client perform GET request on url /realms?id=newrealm
        And show body received
        Then the client receives status code 404
