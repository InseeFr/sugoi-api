Feature: Realms scenario asynchrone
    Performing actions on realms

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client make an asynchronous request
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