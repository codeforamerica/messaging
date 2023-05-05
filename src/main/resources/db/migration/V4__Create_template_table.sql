create table template
(
    id                  bigserial                not null,
    name                text                     not null,
    subject             text,
    body                text                     not null,
    creation_timestamp  timestamp with time zone not null,
    update_timestamp    timestamp with time zone,
    primary key (id)
);

alter table message
    add column template_name text not null default 'default',
    alter column body drop not null;