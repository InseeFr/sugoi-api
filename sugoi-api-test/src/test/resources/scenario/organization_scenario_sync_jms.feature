Feature: Organization scenario synchronous jms
    Performing actions on organization

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get organizations
        When the client perform GET request on url /realms/domaine1/organizations
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of organizations

    Scenario: Get organization
        When the client perform GET request on url /realms/domaine1/organizations/testoJMS
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
                "identifiant": "identifiantJMS"
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an organization
        Then the client expect the identifiant of organization to be identifiantJMS

    Scenario: Post organization already exist
        When the client perform POST request with body on url /realms/domaine1/storages/default/organizations body:
            """
            {
                "identifiant": "identifiantJMS"
            }
            """
        And show body received
        Then the client receives status code 409

    Scenario: Update organization
        When the client perform PUT request with body on url /realms/domaine1/organizations/identifiantJMS body:
            """
            {
                "identifiant": "identifiantJMS",
                "metadatas": {
                    "additionalProp1": "prop1"
                }
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an organization
        Then the client expect the identifiant of organization to be identifiantJMS

    Scenario: Update organization not exist
        When the client perform PUT request with body on url /realms/domaine1/organizations/notExistOrga body:
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
        When the client perform DELETE request on url /realms/domaine1/organizations/identifiantJMS
        And show body received
        Then the client receives status code 204

    Scenario: Delete organization not exist
        When the client perform DELETE request on url /realms/domaine1/organizations/identifiantJMS
        And show body received
        Then the client receives status code 404