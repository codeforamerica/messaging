alter table message
    rename column body to sms_body;
alter table message
    alter column sms_body drop not null,
    add column if not exists email_body text;
update message
    set email_body=sms_body;

alter table template_variant
    rename column body to sms_body;
alter table template_variant
    alter column sms_body drop not null,
    add column if not exists email_body text;
update template_variant
    set email_body=sms_body;
