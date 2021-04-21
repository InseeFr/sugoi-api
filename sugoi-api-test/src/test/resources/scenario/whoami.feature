Feature: Whoami scenario
    Ask whoami

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Whoami ?
        When the client perform GET request on url /whoami
        And show body received
        Then the client receives status code 200
        Then the client expect to receive his rights
