Feature: Organization scenario asynchrone
    Performing actions on organization

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client authentified with username appli_sugoi and password sugoi
        Given the client make an asynchronous request

    Scenario: Get organizations
        When the client perform GET request on url /realms/domaine1/organizations
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of organizations

    Scenario: Get organization
        When the client perform GET request on url /realms/domaine1/organizations/testo
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an organization

    Scenario: Get organization not exist
        When the client perform GET request on url /realms/domaine1/organizations/notExistOrga
        And show body received
        Then the client receives status code 404

    Scenario: Post organization
        When the client perform POST request with body on url /realms/domaine1/storages/default/organizations body:
            """
            {
                "identifiant": "identifiant"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Post organization already exist
        When the client perform POST request with body on url /realms/domaine1/storages/default/organizations body:
            """
            {
                "identifiant": "identifiant"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Update organization
        When the client, in 10 max retry, perform PUT request with body on url /realms/domaine1/organizations/identifiant and expect a statuscode 202 with body:
            """
            {
                "identifiant": "identifiant",
                "metadatas": {
                    "additionalProp1": "prop1"
                }
            }
            """
        And show body received
        Then the client receives status code 202


    Scenario: Update organization not exist
        When the client, in 10 max retry, perform PUT request with body on url //realms/domaine1/organizations/notExistOrga and expect a statuscode 202 with body:
            """
            {
                "identifiant": "notExistOrga",
                "metadatas": {
                    "additionalProp1": "prop1"
                }
            }
            """
        And show body received
        Then the client receives status code 404

    Scenario: Delete organization
        When the client, in 10 max retry, perform DELETE request on url /realms/domaine1/organizations/identifiant and expect a statuscode 202
        And show body received
        Then the client receives status code 202

    Scenario: Delete organization not exist
        When the client, in 10 max retry, perform DELETE request on url /realms/domaine1/organizations/identifiant and expect a statuscode 202
        And show body received
        Then the client receives status code 404

    Scenario: Get organizations (storage)
        When the client perform GET request on url /realms/domaine1/storages/default/organizations
        And show body received
        Then the client receives status code 200

    Scenario: Get organization (storage)
        When the client perform GET request on url /realms/domaine1/storages/default/organizations/testo
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an organization

    Scenario: Get organization not exist (storage)
        When the client perform GET request on url /realms/domaine1/storages/default/organizations/notExistOrga
        And show body received
        Then the client receives status code 404

    Scenario: Post organization (storage)
        When the client perform POST request with body on url /realms/domaine1/storages/default/organizations body:
            """
            {
                "identifiant": "identifiant"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Update organization (storage)
        When the client perform PUT request with body on url /realms/domaine1/storages/default/organizations/identifiant body:
            """
            {
                "identifiant": "identifiant",
                "metadatas": {
                    "additionalProp1": "prop1"
                }
            }
            """
        And show body received
        Then the client receives status code 202


    Scenario: Update organization not exist (storage)
        When the client perform PUT request with body on url /realms/domaine1/storages/default/organizations/notExistOrga body:
            """
            {
                "identifiant": "notExistOrga",
                "metadatas": {
                    "additionalProp1": "prop1"
                }
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Delete organization (storage)
        When the client perform DELETE request on url /realms/domaine1/storages/default/organizations/identifiant
        And show body received
        Then the client receives status code 202
    Scenario: Delete organization not exist (storage)
        When the client perform DELETE request on url /realms/domaine1/storages/default/organizations/identifiant
        And show body received
        Then the client receives status code 202