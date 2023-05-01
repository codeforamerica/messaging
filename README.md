![Maven Build](https://github.com/codeforamerica/messaging/actions/workflows/build.yml/badge.svg)

# Description
A messaging backend providing a REST API for sending text messages and email.
It supports both transactional messages and bulk sending.

# Stack
- Java JDK 19
- Spring Boot
- PostgreSQL

# Configuration
## Secrets
All secrets will be read from environment variables and kept out of version control. The required secrets are:

| Variable Name                 | Description                           | Notes                                              |
|-------------------------------|---------------------------------------|----------------------------------------------------|
| FORCE_SSL                     | Disallows HTTP and redirects to HTTPS | Set to "true"                                      |
| SPRING_DATASOURCE_URL         | PostgreSQL connection URI             | E.g., `jdbc:postgresql://localhost:5432/messaging` |
| SPRING_SECURITY_USER_PASSWORD | Password for Basic Authentication     | Saved in a shared folder in LastPass               |
| MAILGUN_API_DOMAIN            |
| MAILGUN_API_FROM              |                                       |
| MAILGUN_API_KEY               |                                       |
| TWILIO_ACCOUNT_SID            |                                       |                                                    |
| TWILIO_AUTH_TOKEN             |                                       |                                                    |
| TWILIO_MESSAGING_SERVICE_SID  |                                       |                                                    |

In the development environment, you can use the [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin with
IntelliJ and store all the secrets in a `.env` file that is not managed in version control.

## Non-secrets

All other configuration will be maintained in conventional Spring Boot `application.properties`.
These will also have profile-specific variations.

## Code Style

Open IntelliJ Settings/Preferences and go to `Editor > Code Style > Java` and next to Scheme hit the
cogwheel and `Import Scheme > IntelliJ IDEA code style XML` with
[intellij-settings/CfaFlavoredGoogleStyle.xml](intellij-settings/CfaFlavoredGoogleStyle.xml)

# Documentation

We use [Springdoc-openapi](https://springdoc.org/v2), which helps automate the generation of API
documentation in Spring Boot projects. It works by examining an application at runtime to infer API
semantics based on spring configurations, class structure, and various annotations. Springdoc is a
better supported and more recently updated alternative to Springfox.

This project has configured a custom docs location in
[application.properties](src/main/resources/application.properties) and a Swagger UI can be found at
[{$BASE_URL}/docs/api/v1](BASE_URL/docs/api/v1) when the app is running locally.

# Environments

| App               | Database             | Twilio Account | Twilio Messaging Service | Stack            | Environment            | Security                                                             |
|-------------------|----------------------|----------------|--------------------------|------------------|------------------------|----------------------------------------------------------------------| 
| messaging-staging | messaging-staging-db | CfA SNIL       | messaging-staging        | shared-us-west-1 | innovation-lab-staging | Use Basic Authentication with user="user" and password from LastPass |

# Deployment
- We use [Dockerfile Deploy](https://deploy-docs.aptible.com/docs/dockerfile-deploy)
- Configure your git remotes for staging and production, e.g., `git remote add aptible-staging git@beta.aptible.com:innovation-lab-staging/messaging-staging.git`.
- Deploy using git push: `git push aptible-staging main`
