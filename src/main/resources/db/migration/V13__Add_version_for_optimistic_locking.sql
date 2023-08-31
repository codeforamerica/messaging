ALTER TABLE message
ADD version bigint default 1;

ALTER TABLE sms_message
    ADD version bigint default 1;

ALTER TABLE email_message
    ADD version bigint default 1;
