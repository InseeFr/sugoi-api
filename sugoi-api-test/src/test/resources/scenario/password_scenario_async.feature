Feature: Credential scenario asynchrone
    Performing actions on password

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client make an asynchronous request

    Scenario: Init password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/init-password body:
            """
            {
                "newPassword": "Changeme1%0000000000"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Init password already init
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/init-password body:
            """
            {
                "newPassword": "Changeme1%000000000000000"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Change password with bad oldPassword
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/change-password body:
            """
            {
                "oldPassword": "Changeme1%00000000000000",
                "newPassword": "Changeme1%000000000000001"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Change password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/change-password body:
            """
            {
                "oldPassword": "Changeme1%000000000000000",
                "newPassword": "Changeme1%000000000000001"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Don't validate password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/validate-password body:
            """
            {
                "password": "Changeme1%000000000000000"
            }
            """
        And show body received
        Then the client receives status code 401

    Scenario: Validate password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/validate-password body:
            """
            {
                "password": "Changeme1%000000000000001"
            }
            """
        And show body received
        Then the client receives status code 200

    Scenario: ResetPassword
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/reinit-password body:
            """
            {
                "templateProperties": {
                    "application": "myapp"
                }
            }
            """
        And show body received
        Then the client receives status code 202