Feature: Monitoring scenario
    Monitoring the app

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username monitor and password monitor

    Scenario: Get actuator
        When the client perform GET request on url /actuator
        And show body received
        Then the client receives status code 200
