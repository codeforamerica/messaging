create table email_messages
(
    id                  bigserial                not null,
    to_email            text                     not null,
    from_email          text                     not null,
    body                text                     not null,
    subject             text                     not null,
    status              text                     not null,
    provider_message_id text                     not null,
    provider_created_at timestamp with time zone,
    created_at          timestamp with time zone not null,
    updated_at          timestamp with time zone,
    primary key (id)
);