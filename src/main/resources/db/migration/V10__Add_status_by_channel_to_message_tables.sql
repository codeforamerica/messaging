ALTER TABLE message
    ADD email_status text NULL,
    ADD email_error_message text NULL,
    ADD sms_status text NULL,
    ADD sms_error_message text NULL;

ALTER TABLE email_message
DROP status;

ALTER TABLE sms_message
DROP status;
