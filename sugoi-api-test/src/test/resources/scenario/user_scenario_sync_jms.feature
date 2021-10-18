Feature: User scenario synchronous jms
    Performing actions on user

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get users
        When the client perform GET request on url /realms/domaine1/users
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of users

    Scenario: Get user
        When the client perform GET request on url /realms/domaine1/users/testc
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an user

    Scenario: Get user not exist
        When the client perform GET request on url /realms/domaine1/users/trtr
        And show body received
        Then the client receives status code 404

    Scenario: Post user
        When the client perform POST request with body on url /realms/domaine1/storages/Profil_domaine1_WebServiceLdap/users body:
            """
            {
                "username": "abcdJMS",
                "mail": "test@insee.fr"
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an user
        Then the client expect the username of user not to be null

    Scenario: Post user with no username
        When the client perform POST request with body on url /realms/domaine1/storages/Profil_domaine1_WebServiceLdap/users body:
            """
            {
                "mail": "test42JMS@insee.fr"
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an user
        Then the client expect the username of user not to be null

    Scenario: Post user with email already exit
        When the client perform POST request with body on url /realms/domaine1/storages/Profil_domaine1_WebServiceLdap/users body:
            """
            {
                "username": "abcd2",
                "mail": "test@insee.fr"
            }
            """
        And show body received
        Then the client receives status code 409

    Scenario: Post user already exist
        When the client perform POST request with body on url /realms/domaine1/storages/Profil_domaine1_WebServiceLdap/users body:
            """
            {
                "username": "abcdJMS"
            }
            """
        And show body received
        Then the client receives status code 409

    Scenario: Update user
        When the client perform PUT request with body on url /realms/domaine1/users/abcdJMS body:
            """
            {
                "username": "abcdJMS",
                "mail": "abcd@test.fr"
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an user


    Scenario: Update user not exist
        When the client perform PUT request with body on url /realms/domaine1/users/abcde body:
            """
            {
                "username": "abcde",
                "mail": "abcde@test.fr"
            }
            """
        And show body received
        Then the client receives status code 404

    Scenario: Delete user
        When the client perform DELETE request on url /realms/domaine1/users/abcdJMS
        And show body received
        Then the client receives status code 204

    Scenario: Delete user not exist
        When the client perform DELETE request on url /realms/domaine1/users/abcde
        And show body received
        Then the client receives status code 404