Feature: Groups scenario asynchrone admin
    Performing actions on groups

    Background: Use tomcat2
        Given the client is using tomcat2
        Given the client make an asynchronous request
        Given the client authentified with username appli_sugoi and password sugoi

    Scenario: Get groups
        When the client perform GET request on url /realms/domaine1/applications/applitest/groups
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a list of groups

    Scenario: Get group
        When the client perform GET request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest
        And show body received
        Then the client receives status code 200
        Then the client expect to receive a group

    Scenario: Get group not exist
        When the client perform GET request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest
        And show body received
        Then the client receives status code 200

    Scenario: Post group
        When the client perform POST request with body on url /realms/domaine1/applications/applitest/groups body:
            """
            {
                "name": "Administrateurs_fake_AppliTest",
                "description": "trololol",
                "appName": "applitest"
            }
            """
        And show body received
        Then the client receives status code 202


    Scenario: Post group already exist
        When the client perform POST request with body on url /realms/domaine1/applications/applitest/groups body:
            """
            {
                "name": "Administrateurs_fake_AppliTest",
                "description": "trololol",
                "appName": "applitest"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Update group
        When the client perform PUT request with body on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest body:
            """
            {
                "name": "Administrateurs_fake_AppliTest",
                "description": "trololola",
                "appName": "applitest"
            }
            """
        And show body received
        Then the client receives status code 202


    Scenario: Update group not exist
        When the client perform PUT request with body on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest3 body:
            """
            {
                "name": "Administrateurs_AppliTest3",
                "description": "trololol2",
                "appName": "applitest"
            }
            """
        And show body received
        Then the client receives status code 202

    Scenario: Add member to application group
        When the client perform PUT request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Add member to application group, user already present
        When the client perform PUT request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Add member to application group, group no exist
        When the client perform PUT request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest3/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Add member to application group, user no exist
        When the client perform PUT request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest/members/testc4
        And show body received
        Then the client receives status code 404

    Scenario: Delete member from application group
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Delete member from application group, already remove
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Delete member from application group, group no exist
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest3/members/testc
        And show body received
        Then the client receives status code 202

    Scenario: Delete member from application group, user no exist
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest/members/testc2
        And show body received
        Then the client receives status code 404

    Scenario: Delete group
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest
        And show body received
        Then the client receives status code 202

    Scenario: Delete group not exist
        When the client perform DELETE request on url /realms/domaine1/applications/applitest/groups/Administrateurs_fake_AppliTest
        And show body received
        Then the client receives status code 202