Feature: Organization scenario
    Performing actions on organization

    Background: Use tomcat1
        Given the client is using tomcat1
        Given the client authentified with username appli_applitest and password applitest
    
    Scenario: get application
        When the client perform GET request on url /realms/domaine1/applications/applitest
        Then the client receives status code 200
        
    Scenario: add member to application group
    		When the client perform PUT request on url /realms/domaine1/applications/applitest/groups/Administrateurs_AppliTest/members/testc
				Then the client receives status code 200