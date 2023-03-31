create table messages
(
    id                  bigserial                not null,
    "to"                text                     not null,
    "from"              text                     not null,
    body                text                     not null,
    status              text                     not null,
    provider_message_id text                     not null,
    provider_created_at timestamp with time zone,
    created_at          timestamp with time zone not null,
    updated_at          timestamp with time zone,
    primary key (id)
);