create table message
(
    id                 bigserial                not null,
    to_phone           text                     null,
    to_email           text                     null,
    subject            text,
    body               text                     not null,
    sms_message_id     bigint references sms_message (id),
    email_message_id   bigint references email_message (id),
    creation_timestamp timestamp with time zone not null,
    update_timestamp   timestamp with time zone not null,
    primary key (id)
);