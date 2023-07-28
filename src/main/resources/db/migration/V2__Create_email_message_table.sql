create table email_message
(
    id                  bigserial                not null,
    to_email            text                     not null,
    from_email          text                     not null,
    body                text                     not null,
    subject             text                     not null,
    status              text                     not null,
    provider_message_id text                     not null,
    provider_created_at timestamp with time zone,
    creation_timestamp  timestamp with time zone not null,
    update_timestamp    timestamp with time zone,
    primary key (id)
);