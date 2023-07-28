ALTER TABLE sms_message
    ADD provider_error jsonb NULL;
ALTER TABLE email_message
    ADD provider_error jsonb NULL;
