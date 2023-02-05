drop all objects;
CREATE TABLE category (
    id          varchar(255) primary key,
    parent_id   varchar(255),
    name        varchar(255) not null
    );

comment on column category.id is '카테고리 ID';
comment on column category.parent_id is '상위 카테고리 ID';
comment on column category.name is '카테고리 이름';