create table sms_message
(
    id                  bigserial                not null,
    to_phone                text                     not null,
    from_phone              text                     not null,
    body                text                     not null,
    status              text                     not null,
    provider_message_id text                     not null,
    provider_created_at timestamp with time zone,
    created_at          timestamp with time zone not null,
    updated_at          timestamp with time zone not null,
    primary key (id)
);