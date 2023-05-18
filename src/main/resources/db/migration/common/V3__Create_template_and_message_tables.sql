create table template
(
    id                      bigserial                               not null,
    name                    text                                    not null unique,
    creation_timestamp      timestamp with time zone                not null,
    update_timestamp        timestamp with time zone,
    primary key (id)
);

create table template_variant
(
    id                      bigserial                               not null,
    subject                 text,
    body                    text                                    not null,
    language                text                                    not null,
    treatment               text                                    not null,
    template_id             bigint,
    creation_timestamp      timestamp with time zone                not null,
    update_timestamp        timestamp with time zone,
    primary key (id),
    foreign key (template_id) references template (id),
    constraint unique_template_language_variant unique (template_id, language, treatment)
);

create table message
(
    id                      bigserial                               not null,
    to_phone                text                                    null,
    to_email                text                                    null,
    subject                 text,
    body                    text                                    not null,
    template_variant_id     bigint                                  not null,
    sms_message_id          bigint references sms_message (id),
    email_message_id        bigint references email_message (id),
    creation_timestamp      timestamp with time zone                not null,
    update_timestamp        timestamp with time zone,
    primary key (id),
    foreign key (template_variant_id) references template_variant (id)
);