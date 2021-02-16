## Installation

- [Installation](#installation)
  - [From zip distribution](#from-zip-distribution)
  - [From war distribution](#from-war-distribution)
  - [With docker](#with-docker)
  - [Developping Sugoi](#developping-sugoi)

### From zip distribution

Simply extract in a folder of your choice, there is already a working configuration in place, so simply use the executable provided to launch **Sugoi**

### From war distribution

Deploy the war in a servlet container of your choice.

### With docker

We provide a docker image https://hub.docker.com/r/inseefrlab/sugoi-api


### Developping Sugoi

In the [sugoi-distribution-full-env](../sugoi-api-distribution/sugoi-distribution-full-env) module, you can launch the `main` in the `SugoiTestService` class to get a a sugoi application with all (or almost all) providers enabled. 
This helper class launches : 
    
    - one ldap server (port 10389)
    - one tomcat with 2 sugoi war (port 8080)
    - one ActiveMq 

This is used in [cucumber tests](../sugoi-api-test)

