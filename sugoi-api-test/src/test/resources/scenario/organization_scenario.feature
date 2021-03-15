Feature: Organization scenario
    Performing actions on organization

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get organization
        When the client perform GET request on url /realms/domaine1/organizations
        And body received
        Then the client receives status code 200
        Then the client expect to receive a list of organizations

    Scenario: Post organization
        When the client perform POST request with body on url /realms/domaine1/storages/Profil_domaine1_WebServiceLdap/organizations body:
            """
            {
                "identifiant": "identifiant"
            }
            """
        And body received
        Then the client receives status code 201
