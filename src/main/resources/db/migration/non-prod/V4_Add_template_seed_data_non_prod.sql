insert into "template_set" (id, name, creation_timestamp) VALUES (1, 'default', current_timestamp);
insert into "template_set" (id, name, creation_timestamp) VALUES (2, 'greeting', current_timestamp);


insert into "template_variant" (subject, body, language, treatment, template_set_id, creation_timestamp)
VALUES (
        '{{ subject }}',
        '{{ body }}',
        'en',
        'A',
        1,
        current_timestamp
);

insert into "template_variant" (subject, body, language, treatment, template_set_id, creation_timestamp)
VALUES (
        'This is a greeting from {{from_name}}',
        'Hi, {{to_name}}!',
        'en',
        'A',
        2,
        current_timestamp
);

insert into "template_variant" (subject, body, language, treatment, template_set_id, creation_timestamp)
VALUES (
        'Este es un saludo de {{from_name}}',
        'Hola, {{to_name}}!',
        'es',
        'A',
        2,
        current_timestamp
);

insert into "template_variant" (subject, body, language, treatment, template_set_id, creation_timestamp)
VALUES (
       'This is a variant greeting from {{from_name}}',
       'Hey, {{to_name}}.',
       'en',
       'B',
       2,
       current_timestamp
);

insert into "template_variant" (subject, body, language, treatment, template_set_id, creation_timestamp)
VALUES (
       'Este es un saludo variante de {{from_name}}',
       'Que tal, {{to_name}}.',
       'es',
       'B',
        2,
       current_timestamp
);