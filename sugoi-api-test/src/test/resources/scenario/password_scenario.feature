Feature: Credential scenario
    Performing actions on password

    Background: Use tomcat1
        Given the client is using tomcat1

    Scenario: Init password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /domaine1/users/test_password/initPassword body:
            """
            {
            "newPassword": "changeme",
            }
            """
        And body received
        Then the client receives status code 200

    Scenario: Change password
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /domaine1/users/test_password/changePassword body:
            """
            {
            "oldPassword": "changeme",
            "newPassword": "changeme2",
            }
            """
        And body received
        Then the client receives status code 200
        And body received
        Then the client receives status code 200

    Scenario: ResetPassword
        Given the client authentified with username appli_sugoi and password sugoi
        When the client perform POST request with body on url /domaine1/users/test_password/reinitPassword body:
            """
            {
                "email": "string",
                "address": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                },
                "properties": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                }
            }
            """
        And body received
        Then the client receives status code 200