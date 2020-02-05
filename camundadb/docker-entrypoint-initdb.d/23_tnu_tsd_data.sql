-- tnu_tsd
begin;

-- groups
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_tn_engineer', 1, 'alm_tn_engineer', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_tn_engineer', 1, 'astana_tn_engineer', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_tn_engineer', 1, 'nc_tn_engineer', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_tn_engineer', 1, 'east_tn_engineer', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_tn_engineer', 1, 'south_tn_engineer', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_tn_engineer', 1, 'west_tn_engineer', 'WORKFLOW');

-- authorizations
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('3095cad6-4327-11ea-b160-8a38eff8d212', 2, 1, 'infrastructure_tnu_users', null, 6, 'tnu_tsd_db', 4674);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'alm_tn_engineer', null, 6, 'tnu_tsd_db', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'astana_tn_engineer', null, 6, 'tnu_tsd_db', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'nc_tn_engineer', null, 6, 'tnu_tsd_db', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'east_tn_engineer', null, 6, 'tnu_tsd_db', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'south_tn_engineer', null, 6, 'tnu_tsd_db', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('0be3865b-4326-11ea-b160-8a38eff8d212', 3, 1, 'west_tn_engineer', null, 6, 'tnu_tsd_db', 258);

commit;