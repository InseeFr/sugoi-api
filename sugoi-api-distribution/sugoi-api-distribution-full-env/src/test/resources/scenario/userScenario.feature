Feature: User scnario
    Performing actions on user

    Background: Use tomcat1
        Given the client is using tomcat1

    Scenario: Get users
        When the client perform GET request on url /tomcat1/domaine1/users
        Then the client receives status code 200
        Then the client expect to receive a list of users
        Then the client want to see the users list

    Scenario: Post users
        When the client perform POST request with body on url /tomcat1/domaine1/users body:
            """
            {
                "username": "Donatien"
            }
            """
        Then the client receives status code 201
        When the client perform GET request on url /tomcat1/domaine1/users
        Then the client expect to receive a list of users
        Then the client want to see the users list
