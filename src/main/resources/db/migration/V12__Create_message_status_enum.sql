CREATE TYPE message_status AS ENUM (
    'submission_succeeded',
    'submission_failed',
    'unmapped',
    'unsubscribed',
    'queued',
    'sent',
    'failed',
    'delivered',
    'undelivered'
    );

CREATE CAST (TEXT AS message_status) WITH INOUT AS IMPLICIT;

CREATE CAST (VARCHAR AS message_status) WITH INOUT AS IMPLICIT;

ALTER TABLE message
    RENAME sms_status TO raw_sms_status;
ALTER TABLE message
    RENAME email_status TO raw_email_status;

ALTER TABLE message
    ADD COLUMN sms_status message_status,
    ADD COLUMN email_status message_status;

UPDATE message
SET sms_status   = CASE
                       WHEN raw_sms_status = 'queued' OR raw_sms_status = 'accepted' THEN 'queued'
                       WHEN raw_sms_status = 'sent' THEN 'sent'
                       WHEN raw_sms_status = 'failed' THEN 'failed'
                       WHEN raw_sms_status = 'delivered' THEN 'delivered'
                       WHEN raw_sms_status = 'undelivered' THEN 'undelivered'
                       WHEN raw_sms_status = 'submission_succeeded' THEN 'submission_succeeded'
                       WHEN raw_sms_status = 'submission_failed' THEN 'submission_failed'
                       WHEN raw_sms_status = 'unsubscribed' THEN 'unsubscribed'
                       ELSE 'unmapped'
    END,
    email_status = CASE
                       WHEN raw_email_status = 'accepted' THEN 'queued'
                       WHEN raw_email_status = 'rejected' THEN 'failed'
                       WHEN raw_email_status = 'delivered' THEN 'delivered'
                       WHEN raw_email_status = 'failed' THEN 'undelivered'
                       WHEN raw_email_status = 'submission_succeeded' THEN 'submission_succeeded'
                       WHEN raw_email_status = 'submission_failed' THEN 'submission_failed'
                       WHEN raw_email_status = 'unsubscribed' THEN 'unsubscribed'
                       ELSE 'unmapped'
    END;
