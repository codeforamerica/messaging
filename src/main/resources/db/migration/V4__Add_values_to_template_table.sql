insert into "template" (name, subject, body, language, variant, creation_timestamp)
VALUES (
        'default',
        '{{ subject }}',
        '{{ body }}',
        'en',
        'A',
        current_timestamp
);

insert into "template" (name, subject, body, language, variant, creation_timestamp)
VALUES (
        'greeting',
        'This is a greeting from {{ from_name }}',
        'Hi, {{ to_name }}!',
        'en',
        'A',
        current_timestamp
);