CREATE TABLE IF NOT EXISTS "category" (
    id          varchar(255) primary key,
    parent_id   varchar(255),
    name        varchar(255) not null
    );

comment on column category.id is '카테고리 ID';
comment on column category.parent_id is '상위 카테고리 ID';
comment on column category.name is '카테고리 이름';

-- local test sample data
INSERT INTO public.category (id, parent_id, name) VALUES ('oKj8qQacdBTlssCzuNBP3OXpd0T8V90d', null, '1');
INSERT INTO public.category (id, parent_id, name) VALUES ('8N0xlKv85dCeENef35HcQ8jiiApMJ5YY', '7fHwyO4dnhk2E5AHdYPZ9lFL5ignYAK4', '4');
INSERT INTO public.category (id, parent_id, name) VALUES ('pMcHPSiVDjwyt5kL8rXreGD6bDNX5xNQ', 'IRp28BkdguwO9XNrLzID02xVpDmprc1P', '5');
INSERT INTO public.category (id, parent_id, name) VALUES ('IRp28BkdguwO9XNrLzID02xVpDmprc1P', 'oKj8qQacdBTlssCzuNBP3OXpd0T8V90d', '3');
INSERT INTO public.category (id, parent_id, name) VALUES ('7fHwyO4dnhk2E5AHdYPZ9lFL5ignYAK4', 'oKj8qQacdBTlssCzuNBP3OXpd0T8V90d', '2');
INSERT INTO public.category (id, parent_id, name) VALUES ('fqbddEHE45LPR8lK0mJUERqyszVX7gJI', 'IRp28BkdguwO9XNrLzID02xVpDmprc1P', '6');
INSERT INTO public.category (id, parent_id, name) VALUES ('Nvg3PnhD5DQxfELWfPfNlohd7Z6C3k5V', '8N0xlKv85dCeENef35HcQ8jiiApMJ5YY', '8');
INSERT INTO public.category (id, parent_id, name) VALUES ('E2YvJmSqsXc2lEviPyQ5PqfuAW81xUbl', '8N0xlKv85dCeENef35HcQ8jiiApMJ5YY', '7');
INSERT INTO public.category (id, parent_id, name) VALUES ('QnY2JSNikCfFGsnnKS5Q2rNREcunw2YH', 'fqbddEHE45LPR8lK0mJUERqyszVX7gJI', '9');
INSERT INTO public.category (id, parent_id, name) VALUES ('HxGEwEq2FHbnLimcPC0WSxclJqiPc3z8', 'zUBENM1fXn5qo6PFki2O2jlrORi67DKk', '13');
INSERT INTO public.category (id, parent_id, name) VALUES ('zUBENM1fXn5qo6PFki2O2jlrORi67DKk', 'FeDuAJSjgtPhFINRrlLdMWggOtbmWRvR', '12');
INSERT INTO public.category (id, parent_id, name) VALUES ('bOXXZ2xdpmXnpM1FLyS4HYyEmmftUQKG', null, '14');
INSERT INTO public.category (id, parent_id, name) VALUES ('eJ99hUiRv559HUZhKJH1ieIzIum9eXl9', null, '0');
INSERT INTO public.category (id, parent_id, name) VALUES ('FeDuAJSjgtPhFINRrlLdMWggOtbmWRvR', null, '10');
INSERT INTO public.category (id, parent_id, name) VALUES ('2BWCxo3wbYVEf1IFoVpDMxv9BKsu1LrH', 'FeDuAJSjgtPhFINRrlLdMWggOtbmWRvR', '11');
