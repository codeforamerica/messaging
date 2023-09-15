# Public endpoints for callbacks and docs

Date: 2023-09-14

## Status

Accepted

## Context

To prevent misuse, access to the API has been restricted using Aptible IP filtering. This does not work
for callbacks from providers as the IPs may change for Twilio and Mailgun. Also, it is desirable
to have the API docs be publicly available without restriction.

Callback endpoints are secured using signature verification with a shared secret from the provider.

Note: CfA has purchased Twilio Security Edition that supports a static list of IPs from which callbacks originate. As we had already implemented
signature verification, we have not availed ourselves of this feature. We do not have a similar facility with Mailgun.

## Decision

We will listen on a separate port for callbacks and docs. This will not be secured with IP filtering.
The specific port can be chosen using the configuration variable `server.trustedPort`. The callbacks and docs will be
made available under the path `/public/`.

| Port Use | Security |
|-|-|
| Messaging API | IP Filtering & BasicAuth shared password |
| Messaging Provider Callbacks & API Documentation | Public w/ Provider Signature varification | 


## Consequences

Aptible supports at most one default endpoint per app. Additional endpoints will need custom domains.
For our staging environment, we have created a custom domain named https://staging.messaging.cfa-platforms.org/ using the AWS Route 53 service.
