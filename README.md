![Maven Build](https://github.com/codeforamerica/messaging/actions/workflows/build.yml/badge.svg)

# Description
A messaging backend providing a REST API for sending text messages and email.
It supports both transactional messages and bulk sending.

# Stack

#### Core tech
- Java JDK 17
- Spring Boot 3.x
    - Spring Data JPA
- PostgreSQL

#### Integration
- SMS: Twilio
- Email: Mailgun

#### Important Tools & Libraries
- Flyway
- Maven
- Aptible
- JobRunr
- Lombok annotations
- Handlebars (Mustache)
- Apache Commons CSV

# Configuration

These environment variables are key to configuring an instance of this application and must be set 
managed carefully.

| Variable Name                 | Description                                                                                                                                                | Notes                                                                                                                                                       |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| FORCE_SSL                     | Disallows HTTP and redirects to HTTPS                                                                                                                      | Set to "true"                                                                                                                                               |
| SPRING_DATASOURCE_URL         | PostgreSQL connection URI                                                                                                                                  | E.g., `jdbc:postgresql://localhost:5432/messaging`                                                                                                          |
| SPRING_SECURITY_USER_PASSWORD | Password for Basic Authentication                                                                                                                          | Saved in a shared folder in LastPass                                                                                                                        |
| MAILGUN_API_DOMAIN            | The appropriate Mailgun domain for the environment                                                                                                         | E.g., `sandbox610d23a2c4f441c78f8e077c7daf45e0.mailgun.org`                                                                                                 |
| MAILGUN_API_FROM              | An email address using the Mailgun domain                                                                                                                  | E.g., `developer@sandbox610d23a2c4f441c78f8e077c7daf45e0.mailgun.org`                                                                                       |
| MAILGUN_API_KEY               | Mailgun domain sending key                                                                                                                                 | [Retrieve from Mailgun](https://documentation.mailgun.com/en/latest/api-intro.html#authentication-1)                                                        |
| TWILIO_ACCOUNT_SID            | The appropriate Twilio account identifier for the environment                                                                                              | [Retrieve from Twilio account home page](https://support.twilio.com/hc/en-us/articles/14726256820123-What-is-a-Twilio-Account-SID-and-where-can-I-find-it-) |
| TWILIO_AUTH_TOKEN             | [What is an auth token?](https://www.twilio.com/docs/glossary/what-is-an-authentication-token)                                                             | Retrieve from Twilio account home page                                                                                                                      |
| TWILIO_MESSAGING_SERVICE_SID  | [Messaging Service](https://support.twilio.com/hc/en-us/articles/223181308-Getting-started-with-Messaging-Services) identifier within the provided account | [Retrieve from Messaging Services page](https://console.twilio.com/us1/develop/sms/services)                                                                |

## Secrets
All secrets are read from environment variables and kept out of version control. Configuration for 
secrets required for deployment are set in the [deploy.yml](.github/workflows/deploy.yml) file, which use 
encrypted secret values stored at https://github.com/codeforamerica/messaging/settings/secrets/actions. 
Any future secrets should be added to the table above, added to the deployment action, and stored in 
LastPass under the "Shared-Messaging Product" folder.

## Non-secrets
All other configuration is maintained in conventional Spring Boot `application.properties` with 
`application-test` and `application-ci` variations.

## Code Style

Our code style is not strictly followed, but if you wuld like to apply the project defaults, open 
IntelliJ Settings/Preferences and go to `Editor > Code Style > Java` and next to "Scheme" hit the
cogwheel and `Import Scheme > IntelliJ IDEA code style XML` with
[CfaFlavoredGoogleStyle.xml](intellij-settings/CfaFlavoredGoogleStyle.xml)

# Documentation

We use [Springdoc-openapi](https://springdoc.org/v2), which helps automate the generation of API
documentation in Spring Boot projects. It works by examining an application at runtime to infer API
semantics based on spring configurations, class structure, and various annotations. Springdoc is a
better supported and more recently updated alternative to Springfox.

This project has configured a custom docs location in
[application.properties](src/main/resources/application.properties) and a Swagger UI can be found at
[BASE_URL/docs/api/v1](http://localhost:8080/docs/api/v1) wherever the app is running.

# Environments

| App               | Database             | Twilio Account | Twilio Messaging Service | Stack            | Environment            | Security                                                             |
|-------------------|----------------------|----------------|--------------------------|------------------|------------------------|----------------------------------------------------------------------| 
| messaging-staging | messaging-staging-db | CfA SNIL       | messaging-staging        | shared-us-west-1 | innovation-lab-staging | Use Basic Authentication with user="user" and password from LastPass |

# Deployment
- We use [Dockerfile Deploy](https://deploy-docs.aptible.com/docs/dockerfile-deploy)
- Configure your git remotes for staging and production, e.g., `git remote add aptible-staging git@beta.aptible.com:innovation-lab-staging/messaging-staging.git`.
- Deploy using git push: `git push aptible-staging main`
- Alternatively, use the github action to deploy a branch
