CREATE TABLE message_batch
(
    id                 bigserial                       not null,
    template_id        bigint references template (id) not null,
    recipients         bytea                           not null,
    send_at            timestamp with time zone,
    sms_message_id     bigint references sms_message (id),
    email_message_id   bigint references email_message (id),
    creation_timestamp timestamp with time zone        not null,
    update_timestamp   timestamp with time zone,
    primary key (id)
);

ALTER TABLE message
    ADD message_batch_id bigint references message_batch(id);
