Feature: Credential scenario
    Performing actions on password

    Background: Use tomcat1
        Given the client is using tomcat1

    Scenario: Init password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/initPassword body:
            """
            {
                "newPassword": "Changeme1%0000000000"
            }
            """
        And body received
        Then the client receives status code 204

    Scenario: Init password already init
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/initPassword body:
            """
            {
                "newPassword": "Changeme1%000000000000000"
            }
            """
        And body received
        Then the client receives status code 204

    Scenario: Change password with bad oldPassword
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/changePassword body:
            """
            {
                "oldPassword": "Changeme1%00000000000000",
                "newPassword": "Changeme1%000000000000001"
            }
            """
        And body received
        Then the client receives status code 500

    Scenario: Change password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/changePassword body:
            """
            {
                "oldPassword": "Changeme1%000000000000000",
                "newPassword": "Changeme1%000000000000001"
            }
            """
        And body received
        Then the client receives status code 204

    Scenario: ResetPassword
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /realms/domaine1/users/test_password/reinitPassword body:
            """
            {}
            """
        And body received
        Then the client receives status code 204