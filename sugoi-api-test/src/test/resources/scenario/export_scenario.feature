Feature: Export scenario synchrone

  Background: Use tomcat1
    Given the client is using tomcat1
    Given the client authentified with username appli_sugoi and password sugoi

  Scenario: Get export by group
    When the client perform GET request on url /realms/domaine1/export/users/export.csv?groupFilter=Utilisateurs_AppliTest
    And show body received
    Then the client receives status code 200
    Then testc has been exported
    Then nogroup has not been exported

  Scenario: Get export
    When the client perform GET request on url /realms/domaine1/export/users/export.csv
    And show body received
    Then the client receives status code 200
    Then testc has been exported
    Then nogroup has been exported