# Sugoi: User management Api

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Actions Status](https://github.com/inseeFrLab/sugoi-api/workflows/Sugoi%20API%20integration%20test/badge.svg)](https://github.com/inseeFrLab/sugoi-api/actions)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/inseefrlab/sugoi-api)

Sugoi provides an API to manage users with multi tenancy in mind.

- [Sugoi: User management Api](#sugoi-user-management-api)
- [Installation](#installation)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [License](#license)

Sugoi is an API to manage users, organizations and applications. These objects are scoped to realms to isolate object with separated purposes (employees, clients, contacts...).

[View more](docs/concepts.md)

A react frontend for this API can be found here : <https://github.com/InseeFrLab/sugoi-ui>.

A Keycloak storage extension is here : <https://github.com/InseeFrLab/keycloak-http-storage-provider> (*Work in Progress*)

## Installation

Download and extract release zip, modify [configuration file](docs/configuration.md) as needed and launch :

```bash
java -jar -Dspring.profiles.active=local sugoi-api.jar
```

Some other ways could be found in [docs/install.md](docs/install.md)

## Configuration

All configuration is done through an `application.properties` file a la spring-boot.

[Configuration.md](docs/configuration.md) for details.

## Contributing

Pull requests are welcome. We ask that all pull request are linked to an issue.
The source code format should be in conformance with google guidestyles for java. this is checked for each PR, you can enforce it with `mvn spotless:apply`.

We also check that all commit are signed-off in accordance with [DCO](https://developercertificate.org/)

**All feature creation or update should be reflected in the documentation. All new configuration keys of the instance or realm should be documented.**

**Please make sure to update or create tests as appropriate.**

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
