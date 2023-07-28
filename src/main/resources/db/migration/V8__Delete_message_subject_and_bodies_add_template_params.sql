alter table message
    drop column if exists email_body,
    drop column if exists sms_body,
    drop column if exists subject;

alter table message
    add template_params jsonb NULL;

