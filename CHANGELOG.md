# 2.7.0


- [UP] Montée de version tomcat -> 11.0.13 (#948)
- [FIX] parameters compile option required
- [UP] Change freemarker to starter-freemarker
- [UP] montée de la dépendance common.io -> 2.11.14
- [FIX] fix needed when upgrade Spring version
- [UP] Montée de dépendance Spring-boot 3.1.4 -> 3.5.6 et fixation mockweserver à 5.1.0
- [UP] test (#934)



# 2.6.0


- [UP] spotless apply
- [UP] correction coquille 'management'
- [UP] harmonisation noms variable
- [UP] Correction coquille ldapBindDn
- [UP] Correction coquille authentification accès
- [UP] :arrow_up: Bump org.apache.tomcat:tomcat-catalina
- [UP] :arrow_up: Bump org.apache.maven.plugins:maven-war-plugin
- [UP] :arrow_up: Bump commons-io:commons-io from 2.11.0 to 2.14.0
- [UP] :arrow_up: Bump org.xmlunit:xmlunit-core from 2.9.1 to 2.10.0
- [UP] montée de version tomcat.version -> 10.1.41



# 2.5.1


- [FIX] path root unavailable and trailing path matching



# 2.5.0


- [UP] :arrow_up: Bump maven-compiler-plugin from 3.10.1 to 3.11.0
- [UP] :arrow_up: Bump maven-assembly-plugin from 3.4.2 to 3.6.0
- [UP] action gitlab cache
- Spring boot 3 (#881)
- [UP] ⬆️ upload and download artifacts to v4
- [BUG] :bug: Fix no users are returned when doing fuzzysearch



# 2.2.3

- [BUG] :bug: Fix cannot configure instance to accept fuzzy search


# 2.2.2

- [BUG] :bug: Fix search OR and AND reversed when has objectclass


# 2.2.1

- [BUG] :bug: Fix search OR and AND reversed


# 2.2.0

- [FEA] :sparkles: Users can be searched ignoring accents


# 2.1.6

- [ENH] :sparkles: DNs can be configured on ldap stores


# 2.1.5

- [ENH] :sparkles: Add warning on reset password default mail


# 2.1.4

- [BUG] :bug: Fix conflict when getting by mail a user with a similar mail


# 2.1.3

- [BUG] :goal_net: Catch all LdapException when using mono connection


# 2.1.2

- [BUG] :bug: Fix max time before a connection is dropped from ldap connection pool


# 2.1.1

- [ENH] :sparkles: add ldaps connexion to see also decorator
- [BUG] :bug: Hotfix connection pooling created though already exists decorator


# 2.1.0

- [LOG] :loud_sound: Improve log by watching controllers
- [REF] :recycle: Refactor advice controller
- [LOG] :loud_sound: User rights log is only debug
- [UP] :arrow_up: Move google-java-format to 1.15.0


# 2.0.2

- [BUG] :bug: Fix application manager can read all realms


# 2.0.1

- [BUG] :bug: Fix isSelfManaged cannot be updated on group


# 2.0.0

- [BUG] :bug: Fix export a non existing user in a group
- [ENH] :sparkles: Compound fields on users can be defined at a userstorage level
- [REF] :recycling: Realm and US properties can have multiple values
- [BUG] :bug: Fix realm properties on ldap can contain $
- [BUG] :bug: Readers can read groups in any app
- [ENH] :sparkles: Add a group manager right
- [ENH] :sparkles: Applications can have self-managed groups
- [UP] :arrow_up: Bump tomcat.version from 9.0.73 to 9.0.78
- [UP] :arrow_up: Bump spring-boot.version from 2.7.2 to 2.7.11
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.7 to 6.0.8
- [ENH] :sparkles: Add an export endpoint for group members


# 1.10.0

- [ENH] :sparkles: Connection to ldap should have a timeout property
- [CI] :green_heart: Remove not working publishing of cucumber report
- [ENH] :sparkles: Connection to ldap should have a timeout property
- [ENH] Writer realm should not have rights on App attributes
- [REF] :recycle: Add a get and set on Sugoi Objects
- [ENH] 753 sparkles create a role password validator
- [ENH] :sparkles: SeeAlso resolution can be authenticated (#682)
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.14 to 1.7.0
- [UP] :arrow_up: Bump maven-surefire-plugin from 3.0.0-M8 to 3.0.0
- [UP] :arrow_up: Bump tomcat.version from 9.0.71 to 9.0.73
- [BUG]500-is-returned-on-password-error-on-change-password-when-using-ldapstore
- [ENH] Make seeAlso request optional
- [UP] :arrow_up: Bump freemarker from 2.3.31 to 2.3.32
- [BUG]Extra-space-in-default-send-login-title-mail


# 1.9.1

- [BUG] :bug: Fix addresses fail to be converted through JMS
- [UP] :arrow_up: Bump commons-csv from 1.9.0 to 1.10.0
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.30.0 to 2.33.0


# 1.8.0

- [BUG] :bug: Fix User Habilitations not passed through JMS (#770)
- [UP] :arrow_up: Bump maven-jar-plugin from 3.2.2 to 3.3.0
- [UP] :arrow_up: Bump httpclient from 4.5.13 to 4.5.14
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.9 to 1.6.14
- [UP] :arrow_up: Bump commons-text from 1.9 to 1.10.0
- [BUG] :bug: Fix issue: appSource is lost when realm update issue ref : https://github.com/InseeFr/sugoi-api/issues/749
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.5 to 6.0.7
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.24.1 to 2.28.0
- [BUG] 755 bug npe is thrown when modifying an application without a groups-list


# 1.7.0

- [ENH] :sparkles: Clean unnecessary lines in Ldap-utils
- [ENH] :sparkles: LIST_GROUP of GenericLdapMapper should take ASI_GROUPS
- [BUG] :bug: Fix Export csv number of results too low
- [UP] :arrow_up: Bump passay from 1.6.1 to 1.6.2
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.24.0 to 2.24.1
- [BUG] :bug: Fixed filter users by group (#418)


# 1.6.0

- [BUG] :bug: Fix retrieve realm and userstorage on webhook call
- [BUG] :bug: All realm config keys can be read on UserStorage
- [BUG] :bug: Fix secondaryEmail is never used
- [ENH] :sparkles: Add attributes to Application (#721)
- [BUG] :bug: last element of a list can't be removed
- [BUG] :bug: Fix searching users from several userstorages should give more results
- [BUG] :bug: Fix NPE on find user by mail
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.22.5 to 2.24.0
- [UP] :arrow_up: Bump exec-maven-plugin from 3.0.0 to 3.1.0
- [BUG] :bug: Get all groups from an application
- [UP] :arrow_up: Bump maven-surefire-plugin from 3.0.0-M6 to 3.0.0-M7
- [REF] :recycle: Refactor exception controller
- [UP] :arrow_up: Bump tomcat.version from 9.0.64 to 9.0.65
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.22.4 to 2.22.5
- [UP] :arrow_up: Bump spring-boot.version from 2.6.7 to 2.7.2
- [UP] :arrow_up: Bump cucumber.version from 7.4.1 to 7.5.0
- [UP] :arrow_up: Bump maven-assembly-plugin from 3.3.0 to 3.4.2
- [UP] :arrow_up: Bump cucumber.version from 7.3.4 to 7.4.1
- [BUG] :bug: Quick fix application comes with 1000 groups
- [REF] :recycle: Config maps are maps of RealmConfigKeys
- [REF] :recycle: Configuration keys are refered via ref instead of string name
- [REF] :recycle: EventKeysConfig is independant from Realm configurations
- [REF] :recycle: Remove void or unused Key files


# 1.5.0

- [ENH] ✨ Add filter users by group (#418)
- [UP] :arrow_up: Bump tomcat.version from 9.0.62 to 9.0.64
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.8 to 1.6.9
- [BUG]  Default is not set on realm mappings #625
- [ENH] :sparkles: LdapSeeAlso can now read any Ldap URL (rfc4516)
- [BUG] :heavy_check_mark: Fix FileReaderStoreTest (#630)
- [BUG] 🐛 Creating a group on a non existing application should return 404 instead of 500 (#630)
- [ENH] :sparkles: Password configuration can be customized at a userstorage level (#626)
- [FIX] :bug: fix NPE on realm without userstorage
- [ENH] :sparkles: Userstorages take default properties from realm
- [BUG] :bug: Fix write Organization UiField in Ldap Config (#695)
- [BUG] Fix FileStoreReaderTest
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.4 to 6.0.5
- [BUG] 🐛 Ldap error code should be logged (#670)
- [REF] ♻️ Move exceptions to model (#670)
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.22.3 to 2.22.4
- [UP] :arrow_up: Bump cucumber.version from 7.2.3 to 7.3.4 AND Bump reporting-plugin from 7.2.0 to 7.3.0 (simultaneous required)
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.22.2 to 2.22.3


# 1.4.0

- [ENH] :sparkles: Have a configuration property for default roles to give to all authenticated users (#586)
- [BC] :boom: Address should appear as an object instead of Array
- [TEST] :white_check_mark: Fix groups properties are at realm level in integration tests
- [ENH] :sparkles: Realms are searched for according to their pattern
- [BC] :boom: Remove compatibility with realm being its own userstorage
- [BUG] controle de l'email et de l'user en cas de reinit password
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.6 to 1.6.8
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.22.1 to 2.22.2
- [UP] :arrow_up: Bump tomcat.version from 9.0.60 to 9.0.62
- [UP] :arrow_up: Bump maven-surefire-plugin from 3.0.0-M5 to 3.0.0-M6
- [UP] :arrow_up: Bump spring-boot.version from 2.6.6 to 2.6.7
- [BUG] :bug: checking id match on PUT should be case insensitive (#657)
- [BUG] :bug: ldap attribute for filter can be multivalued, filter must implement it
- [BUG] :bug: application and groups config must be at realm level not at userstorage
- [BUG] :bug: UnsupportedOperationException not thrown when it should (#486)
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.19.2 to 2.22.1
- [UP] :arrow_up: Bump jacoco-maven-plugin from 0.8.7 to 0.8.8
- [BUG] :bug: Resources should be closed (#656)
- [ENH] :sparkles: Add an option to limit the number of object a user can fetch in one time (#618)
- [FIX] :bug: remove empty attributes values when adding on ldap
- [UP] :arrow_up: Bump spingboot.version from 2.6.4 to 2.6.6


# 1.3.0

- [UP] :arrow_up: Bump spring version from 2.6.2 to 2.6.6
- [BUG] :bug: load US causes realm cache to be avoided
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.5 to 1.6.6
- [UP] :arrow_up: Bump tomcat.version from 9.0.56 to 9.0.60
- [UP] :arrow_up: Bump maven-compiler-plugin from 3.8.1 to 3.10.1
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.3 to 6.0.4
- [BUG] :heavy_minus_sign: Remove wagon-ftp
- [UP] :arrow_up: Bump maven-jar-plugin from 3.2.0 to 3.2.2
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.2 to 1.6.5
- [UP] :arrow_up: Bump spring-boot.version from 2.6.3 to 2.6.4
- [UP] :arrow_up: Bump spring-boot.version from 2.6.2 to 2.6.3
- [ENH] :sparkles: add verifyUniqueMail configuration

# 1.2.1

- [TEST] :white_check_mark: fix computing user position in csv header
- [ENH] :sparkles: better printed exports
- [ENH] :recycle: refactor address
- [ENH] :recycle: refactor store mappings
- [ENH] ✨ Default change password mail template should warn to respect the password (#627)
- [FIX] :bug: Resource ldap port should be configurable by realm (#573)
- [BUG] :bug: Fetching a certificate on a user without certificate should return 404 (#524)
- [ENH] Changelog should only contain last modification (#510)


# 1.1.2

- [BUG] :bug: fix bug when adding 2 times same app-managed-attributes
- [ENH] :sparkles: Add first name and last name in search parameters
- [FIX] :bug: Inconsistency of search users endpoint with or without storage
- [UP] :arrow_up: Bump xmlunit-core from 2.8.3 to 2.9.0
- [UP] :arrow_up: Bump cucumber.version from 7.1.0 to 7.2.3
- [BUG] :bug: fix group filter on user in ldap
- [BUG] :bug: UiField should be written on realm via API (#599)


# 1.1.1

- [ENH] ✨ Have a property to define which Event are allowed to call webhook (#605)
- [UP] :arrow_up: Bump spring-boot.version from 2.6.1 to 2.6.2
- [ENH] log4j2--> to --> slf4j
- [DOC] :memo: Fix UiField options documentation
- [BUG] :bug: Avoid StackOverflow when organization is its own suborganization
- [BUG] :bug: Fix update realm on file store
- [UP] :arrow_up: Bump tomcat.version from 9.0.54 to 9.0.56
- [ENH] :sparkles: filtering export by group (#572)
- [FIX] :bug: Avoid null pointer exception on webhook event handler
- [ENH] :sparkles: Password validation and generation can be configured at realm level (#530)
- [ENH] :sparkles:  WebHook send email to secondary emails too
- [ENH] :sparkles: add verification of required field
- [ENH] :heavy_minus_sign: remove jansi dependency


# 1.0.0

- [UP] :arrow_up: Bump log4j-core from 2.15.0 to 2.17.1
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.17.6 to 2.19.2
- [UP] :arrow_up: Bump org.springdoc.version from 1.6.0 to 1.6.2
- [UP] :arrow_up: Bump reporting-plugin from 7.0.0 to 7.2.0
- [UP] :arrow_up: Bump log4j to 2.17.0
- [ENH] :construction_worker: Tag action can be triggered on other branch than master
- [BUG] :mute: Add IgnoreSizeOf for realm cache sizing
- [ENH] :sparkles: /change-password can call an external webservice
- [ENH] :sparkles: /init-password can change reset password status
- [ENH] :boom: /init-password takes a PasswordView
- [ENH] :boom: Remove camel case from /init-password /change-password and /reinit-password
- [ENH] :boom: /reinitPassword takes optional TemplatePropertiesView
- [ENH] :rotating_light: Fix warnings on UiFields
- [BUG] :bug: Fix modify request on ldap config store should be authenticated
- [BUG] :bug: Description should be writable on ldap config store
- [UP] :arrow_up: Bump maven-surefire-plugin from 3.0.0-M4 to 3.0.0-M5
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.17.4 to 2.17.6
- [UP] :arrow_up: Bump org.springdoc.version from 1.5.12 to 1.6.0
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.2 to 6.0.3


# 0.9.1.1

- [UP] :arrow_up: Bump log4j to 2.17.0
- [ENH] :construction_worker: Tag action can be triggered on other branch than master


# 0.9.1

- [FIX] :ambulance: fix cve log4j2, avoid injection code
- [UP] :arrow_up: Bump spring-boot.version from 2.5.5 to 2.6.1
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.17.3 to 2.17.4
- [UP] :arrow_up: Bump cucumber.version from 7.0.0 to 7.1.0


# 0.9.0

- [ENH] :sparkle: delete attribute if it has only a blank or empty value
- [ENH] :recycle: Remove RealmProvider from controllers
- [ENH] :recycle: Move management of realm not found to service level
- [ENH] :recycle: Move management of group not found to service level
- [ENH] :recycle: Move management of user not found to service level
- [ENH] :recycle: Move management of organization not found at service level
- [ENH] :recycle: Move management of application not found at service level
- [ENH] :sparkles: Ouganext controller can throw OuganextParsingException
- [ENH] :sparkles: Add a ldap mapping type exists (#519)
- [ENH] :recycle: Pass passwords to CredentialsService instead of PCR
- [UP] :arrow_up: Bump xmlunit-core from 2.8.2 to 2.8.3
- [BUG] :bug: Fix swagger ui display ouganext request body instead of sugoi (#500)
- [ENH] :memo: Add documentation on webhooks
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.17.0 to 2.17.3
- [UP] :arrow_up: Bump org.springdoc.version from 1.5.11 to 1.5.12


# 0.8.8

- [BUG] :bug: Fix synchronous request behind JMS doesn't return a body
- [ENH] :sparkles: password in a PasswordView instead of a Map on /validate-password (#501)
- [ENH] :sparkles: manage asi group from sugoi
- [UP] :arrow_up: Bump spring-boot.version from 2.5.4 to 2.5.5
- [UP] :arrow_up: Bump maven-war-plugin from 3.3.1 to 3.3.2
- [UP] :arrow_up: Bump org.springdoc.version from 1.5.10 to 1.5.11
- [UP] :arrow_up: Bump tomcat.version from 9.0.52 to 9.0.54
- [UP] :arrow_up: Bump cucumber.version from 6.10.2 to 7.0.0
- [UP] :arrow_up: Bump unboundid-ldapsdk from 6.0.0 to 6.0.2
- [BUG] fix errors around monoconnection reopening
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.12.3 to 2.17.0
- [BUG] :bug: Get application get 200 groups instead of 20 (#468)


# 0.8.7

- [BUG] :bug: Fail in event doesn't stop request-process
- [ENH] generate id if missing and better id and mail unicity check
- [BUG] :bug: Fix unfounded mail check on user update


# 0.8.5

- [BUG] unable to add app managed attribute from jms writer
- [BUG] restore old xml mapping for retro endpoints
- [ENH] only generate friendly password (#459)
- [BUG] :bug: Fix event without realm name (#449)
- [ENH] adjust typo line address
- [BUG] Default ui mapping passwordlastchange not modifiable (#456)
- [UP] :arrow_up: Bump tomcat.version from 9.0.50 to 9.0.52
- [UP] :arrow_up: Bump spring-boot.version from 2.5.3 to 2.5.4
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.12.2 to 2.12.3
- [FIX] adjust mapping of search parameters
- [FIX] accept generic strings for searching habilitations
- [FIX] :bug: update and delete certificate
- [ENH] :sparkles: manage certificate


# 0.8.4

- [FIX] adjust habilitation endpoints
- [ENH] Sort ui items
- [FIX] adjust default values
- [FIX] adjust default values
- [FIX] adjust default values
- [BUG] :bug: repair ui mapping
- [BUG] :bug: retrieve good exception type after provider response post in broker
- [ENH] :sparkles: add send login
- [BUG] :bug: avoid null pointer exception when reset password
- [ENH] :sparkles: add ui field configuration in realm
- [ENH] role claim more generic
- [ENH] Add authenticated connections for ldapReader
- [FIX] Avoid null Ldap Connection


# 0.8.1

- [FIX] Delete member group dont work when multi storage (#412)
- [ENH] :sparkles: Allow to configure mail unicity by realm
- [ENH] :sparkles: allow to add realm description on ui-side
- [UP] :arrow_up: Bump git-commit-id-plugin from 4.0.5 to 4.9.10
- [UP] :arrow_up: Bump org.springdoc.version from 1.5.9 to 1.5.10
- [UP] :arrow_up: Bump commons-csv from 1.8 to 1.9.0
- [ENH] :sparkles: Appmanager need to have reader and appmanager roles to update application groups (#331)
- [UP] :arrow_up: Bump jansi from 2.3.3 to 2.3.4
- [UP] :arrow_up: Bump passay from 1.6.0 to 1.6.1
- [UP] :arrow_up: Bump spring-boot.version from 2.5.2 to 2.5.3
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.12.1 to 2.12.2
- [ENH] :sparkles: make broker working synchrone asynchrone
- [ENH] :rocket: manage exchange between front and a back


# 0.7.0

- [BUG] 🐛 fix some sonar/compilation warnings
- [BUG] :bug: renew connection with ldap if server is down (#391)
- [UP] :arrow_up: Bump commons-io from 2.10.0 to 2.11.0
- [UP] :arrow_up: Bump tomcat.version from 9.0.48 to 9.0.50
- [ENH] :sparkles: Mapping Sugoi object with Ldap must be customisable (#191)
- [ENH] :sparkles: Realm and UserStorage mapping attributes for Ldap Store Provider
- [ENH] global export feature


# 0.6.0

- [BUG] :bug: Signoff commit when preparing release
- [META] :card_index: change commit message when releasing
- [BUG] 🐛 Fix ldap connexion leak (#379)
- [ENH] objectClass can be set by userStorage
- [ENH] implement paging on ldap store
- [UP] :arrow_up: Bump spring-boot.version from 2.5.1 to 2.5.2
- [ENH] :sparkles: sort key must be customizable
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.12.0 to 2.12.1
- [UP] :arrow_up: Bump tomcat.version from 9.0.46 to 9.0.48
- [META] 📇 Fix changelog format and url
- Resolve [250] Add paging properties on userstorage of type ldap + Resolve [247] Having a generic interface for paged requests  + Resolve [248] Refactor findByProperties services for multi-userstorage paging + Resolve [251] Implement paging on Ldap store


# 0.4.0

- [BUG] 🐛Fix release action
- [FEAT] ✨Allow to define a local monitoring user (#367)
- [META] 📇 Manage changelog on release (#258)
- [ENH] :sparkles: improve ci on release
- [UP] :arrow_up: Bump jansi from 2.3.2 to 2.3.3
- [UP] :arrow_up: Bump spotless-maven-plugin from 2.11.1 to 2.12.0
- [UP] :arrow_up: Bump commons-io from 2.9.0 to 2.10.0
- [UP] :arrow_up: Bump spring-boot.version from 2.5.0 to 2.5.1
- [BUG] :bug: unable to delete app managed when user not writer (#360)
