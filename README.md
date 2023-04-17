# Description
A messaging backend providing a REST API for sending text messages and email.
It supports both transactional messages and bulk sending.

# Stack
- Java JDK 19
- Spring Boot
- PostgreSQL

# Environments
None yet.

# Configuration
## Secrets
All secrets will be read from environment variables and kept out of version control. The required secrets are:

| Variable Name | Description               | Example                                 |
|---------------|---------------------------|-----------------------------------------|
| DATABASE_URL  | PostgreSQL connection URI | `postgresql://localhost:5432/messaging` |


In the development environment, you can use the [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin with
IntelliJ and store all the secrets in a `.env` file that is not managed in version control.

## Non-secrets
All other configuration will be maintained in conventional Spring Boot `application.properties`. These will also have profile-specific variations.

## Code Style

Open IntelliJ Settings/Preferences and go to `Editor > Code Style > Java` and next to Scheme hit the
cogwheel and `Import Scheme > IntelliJ IDEA code style XML` with
[intellij-settings/CfaFlavoredGoogleStyle.xml](intellij-settings/CfaFlavoredGoogleStyle.xml)

