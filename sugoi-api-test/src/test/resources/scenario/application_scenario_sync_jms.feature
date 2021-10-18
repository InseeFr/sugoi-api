Feature: Applications scenario synchronous jms
    Performing actions on applications

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get applications
        When the client perform GET request on url /realms/domaine1/applications
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of applications

    Scenario: Get application
        When the client perform GET request on url /realms/domaine1/applications/applitest
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an application

    Scenario: Get application not exist
        When the client perform GET request on url /realms/domaine1/applications/applitest2
        And show body received
        Then the client receives status code 404

    Scenario: Post application
        When the client perform POST request with body on url /realms/domaine1/applications body:
            """
            {
                "name": "MyAppJMS",
                "owner": "toto",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an application
        Then the client expect the name of application to be MyAppJMS

    Scenario: Post application alreadyExist
        When the client perform POST request with body on url /realms/domaine1/applications body:
            """
            {
                "name": "MyAppJMS",
                "owner": "toto",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 409

    Scenario: Update application
        When the client perform PUT request with body on url /realms/domaine1/applications/MyAppJMS body:
            """
            {
                "name": "MyAppJMS",
                "owner": "tata",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an application
        Then the client expect the name of application to be MyAppJMS

    Scenario: Update application not exist
        When the client perform PUT request with body on url /realms/domaine1/applications/MyApp2JMS body:
            """
            {
                "name": "MyApp2JMS",
                "owner": "tata",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 404


    Scenario: Delete application
        When the client perform DELETE request on url /realms/domaine1/applications/MyAppJMS
        And show body received
        Then the client receives status code 204

    Scenario: Delete application not exist
        When the client perform DELETE request on url /realms/domaine1/applications/MyApp2JMS
        And show body received
        Then the client receives status code 404