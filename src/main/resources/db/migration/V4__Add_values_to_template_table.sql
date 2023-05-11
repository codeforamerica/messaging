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

insert into "template" (name, subject, body, language, variant, creation_timestamp)
VALUES (
        'greeting',
        'Este es un saludo de {{ from_name }}',
        'Hola, {{ to_name }}!',
        'es',
        'A',
        current_timestamp
);

insert into "template" (name, subject, body, language, variant, creation_timestamp)
VALUES (
       'greeting',
       'This is a variant greeting from {{ from_name }}',
       'Hey, {{ to_name }}.',
       'en',
       'B',
       current_timestamp
);

insert into "template" (name, subject, body, language, variant, creation_timestamp)
VALUES (
       'greeting',
       'Este es un saludo variante de {{ from_name }}',
       'Que tal, {{ to_name }}.',
       'es',
       'B',
       current_timestamp
);