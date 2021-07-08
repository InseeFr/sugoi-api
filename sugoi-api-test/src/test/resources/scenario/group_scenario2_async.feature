Feature: Groups scenario asynchrone
    Performing actions on groups with an non admin user

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client authentified with username appli_applitest and password applitest
        Given the client make an asynchronous request

    Scenario: Get groups
        When the client perform GET request on url /realms/domaine1/applications/applitest/groups
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of groups

    Scenario: Get groups failed because no rights on app
        When the client perform GET request on url /realms/domaine1/applications/applitest2/groups
        And show body received
        Then the client receives status code 403