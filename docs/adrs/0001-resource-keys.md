# Data Type for Resource Keys

Date: 2023-04-20

## Status

Accepted

## Context

The usual choices for keys for resources are:

1. Long integers. These are compact, efficient and easy to use. Their disadvantages are that they are easy to guess and give away information about populations and growth rates in the system.
2. UUIDs. These are very useful in a multi-tenancy environment.Their disadvantage is that they are slightly cumbersome to use and inefficient.

## Decision

We will use long integers for resource keys. The current assumption is that the deployment will be single tenant, one-to-one with each client application.

## Consequences

If we are wildly successful and need to support this as a multi-tenant service, it should be easy to transition to UUIDs. However, migrating existing instances may require some downtime.



