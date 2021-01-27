Feature: Realms scenario
    Performing actions on realms

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get realms
        When the client perform GET request on url /realms
        Then the client receives status code 200
        Then the client expect to have realms access
