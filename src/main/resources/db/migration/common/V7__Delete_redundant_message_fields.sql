alter table message
    drop column if exists to_phone,
    drop column if exists to_email,
    drop column if exists email_body,
    drop column if exists sms_body,
    drop column if exists subject;