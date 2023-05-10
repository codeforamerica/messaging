create table template
(
    id                  bigserial                not null,
    name                text                     not null,
    subject             text,
    body                text                     not null,
    language            text                     not null,
    variant             text                     not null,
    creation_timestamp  timestamp with time zone not null,
    update_timestamp    timestamp with time zone,
    primary key (id),
    constraint unique_name_language_variant unique (name, language, variant)
);

create table message
(
    id                 bigserial                not null,
    to_phone           text                     null,
    to_email           text                     null,
    template_id        bigint references template (id) not null,
    sms_message_id     bigint references sms_message (id),
    email_message_id   bigint references email_message (id),
    creation_timestamp timestamp with time zone not null,
    update_timestamp   timestamp with time zone not null,
    primary key (id)
);