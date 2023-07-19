alter table template
    add version     smallserial     not null,
    add status      text            not null    default 'DRAFT',
    drop constraint template_name_key;

update template t
    set status = 'ACTIVE'
    from template_variant tv
        inner join message m on tv.id = m.template_variant_id
    where tv.template_id = t.id;