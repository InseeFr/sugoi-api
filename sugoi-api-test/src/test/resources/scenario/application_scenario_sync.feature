Feature: Applications scenario
    Performing actions on applications

    Background: Use tomcat1
        Given the client is using tomcat1
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
                "name": "MyApp",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an application

    Scenario: Post application without group
        When the client perform POST request with body on url /realms/domaine1/applications body:
            """
            {
      "name": "dada",
      "groups": null,
      "attributes": {
        "owner": "Branche privative de l'application applitest"
      }
    }
            """
        And show body received
        Then the client receives status code 201
        Then the client expect to receive an application

    Scenario: Post application alreadyExist
        When the client perform POST request with body on url /realms/domaine1/applications body:
            """
            {
                "name": "MyApp",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 409

    Scenario: Update application
        When the client perform PUT request with body on url /realms/domaine1/applications/MyApp body:
            """
            {
                "name": "MyApp",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 200
        Then the client expect to receive an application

    Scenario: Update application not exist
        When the client perform PUT request with body on url /realms/domaine1/applications/MyApp2 body:
            """
            {
                "name": "MyApp2",
                "groups": []
            }
            """
        And show body received
        Then the client receives status code 404


    Scenario: Delete application
        When the client perform DELETE request on url /realms/domaine1/applications/MyApp
        And show body received
        Then the client receives status code 204

    Scenario: Delete application not exist
        When the client perform DELETE request on url /realms/domaine1/applications/MyApp2
        And show body received
        Then the client receives status code 404

    Scenario: Get max output applications
        When the client perform GET request on url /realms/maxsize/applications
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of applications
        Then The client expect to receive a list of 1 application(s)