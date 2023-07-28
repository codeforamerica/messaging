CREATE TABLE email_subscription
(
    id                 bigserial                not null,
    email              text                     not null,
    unsubscribed       boolean                  not null default true,
    source_internal    boolean                  not null default true,
    creation_timestamp timestamp with time zone not null,
    update_timestamp   timestamp with time zone,
    primary key (id)
);
