begin;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
--USERS
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Damir.Auezov@kcell.kz', 1, 'Damir', 'Auezov', 'Damir.Auezov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Denis.Revenko@kcell.kz', 1, 'Denis', 'Revenko', 'Denis.Revenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 1, 'Roman', 'Shakhmatov', 'Roman.Shakhmatov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 1, 'Evgeniy', 'Grebnev', 'Evgeniy.Grebnev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Irina.Belova@kcell.kz', 1, 'Irina', 'Belova', 'Irina.Belova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Elena.Blinova@kcell.kz', 1, 'Elena', 'Blinova', 'Elena.Blinova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Assya.Yeriskina@kcell.kz', 1, 'Assya', 'Yeriskina', 'Assya.Yeriskina@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Mariya.Dmitruk@kcell.kz', 1, 'Mariya', 'Dmitruk', 'Mariya.Dmitruk@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Akmaral.Aldasheva@kcell.kz', 1, 'Akmaral', 'Aldasheva', 'Akmaral.Aldasheva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Alexandr.Varfolomeyev@kcell.kz', 1, 'Alexandr', 'Varfolomeyev', 'Alexandr.Varfolomeyev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Rustambek.Kebirov@kcell.kz', 1, 'Rustambek', 'Kebirov', 'Rustambek.Kebirov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Anel.Udanova@kcell.kz', 1, 'Anel', 'Udanova', 'Anel.Udanova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 1, 'Yuliya', 'Luchshikova', 'Yuliya.Luchshikova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Vadim.Li@kcell.kz', 1, 'Vadim', 'Li', 'Vadim.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stepan.Kipel@kcell.kz', 1, 'Stepan', 'Kipel', 'Stepan.Kipel@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Valentin.Lissassin@kcell.kz', 1, 'Valentin', 'Lissassin', 'Valentin.Lissassin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stanislav.Li@kcell.kz', 1, 'Stanislav', 'Li', 'Stanislav.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);


--GROUPS
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('kcellUsers', 1, 'kcellUsers', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_users', 1, 'delivery_pbx_users', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_b2b_delivery', 1, 'delivery_pbx_b2b_delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_sbc_technical_dept', 1, 'delivery_pbx_sbc_technical_dept', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_sip_proxy_technical_dept', 1, 'delivery_pbx_sip_proxy_technical_dept', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_asterisk_technical_dept', 1, 'delivery_pbx_asterisk_technical_dept', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_tic_sales_support_team', 1, 'delivery_pbx_tic_sales_support_team', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_contract_sales_support_team', 1, 'delivery_pbx_contract_sales_support_team', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_osst', 1, 'delivery_pbx_osst', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_bsst', 1, 'delivery_pbx_bsst', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_fw_enterprise_data_network', 1, 'delivery_pbx_enterprise_data_network', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_pbx_it_delivery', 1, 'delivery_pbx_it_delivery', 'WORKFLOW');


--INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_contractor_lse', 1, 'alm_contractor_lse', 'WORKFLOW');

--MEMBERSHIPS

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Damir.Auezov@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Denis.Revenko@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Irina.Belova@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Elena.Blinova@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assya.Yeriskina@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mariya.Dmitruk@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Akmaral.Aldasheva@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Varfolomeyev@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustambek.Kebirov@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anel.Udanova@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stepan.Kipel@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Valentin.Lissassin@kcell.kz', 'kcell_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'kcell_users');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Damir.Auezov@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Denis.Revenko@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Irina.Belova@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Elena.Blinova@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assya.Yeriskina@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mariya.Dmitruk@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Akmaral.Aldasheva@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Varfolomeyev@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustambek.Kebirov@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anel.Udanova@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stepan.Kipel@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Valentin.Lissassin@kcell.kz', 'delivery_pbx_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_pbx_users');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Damir.Auezov@kcell.kz', 'delivery_pbx_b2b_delivery');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Denis.Revenko@kcell.kz', 'delivery_pbx_b2b_delivery');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'delivery_pbx_sbc_technical_dept');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'delivery_pbx_sbc_technical_dept');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Irina.Belova@kcell.kz', 'delivery_pbx_tic_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Irina.Belova@kcell.kz', 'delivery_pbx_contract_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Elena.Blinova@kcell.kz', 'delivery_pbx_tic_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assya.Yeriskina@kcell.kz', 'delivery_pbx_tic_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mariya.Dmitruk@kcell.kz', 'delivery_pbx_tic_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Akmaral.Aldasheva@kcell.kz', 'delivery_pbx_tic_sales_support_team');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Varfolomeyev@kcell.kz', 'delivery_pbx_osst');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustambek.Kebirov@kcell.kz', 'delivery_pbx_osst');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anel.Udanova@kcell.kz', 'delivery_pbx_bsst');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 'delivery_pbx_bsst');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_pbx_fw_enterprise_data_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stepan.Kipel@kcell.kz', 'delivery_pbx_sip_proxy_technical_dept');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Valentin.Lissassin@kcell.kz', 'delivery_pbx_asterisk_technical_dept');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_pbx_it_delivery');

--auth
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Damir.Auezov@kcell.kz', 1, 'Damir.Auezov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Denis.Revenko@kcell.kz', 1, 'Denis.Revenko@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Roman.Shakhmatov@kcell.kz', 1, 'Roman.Shakhmatov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Evgeniy.Grebnev@kcell.kz', 1, 'Evgeniy.Grebnev@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Irina.Belova@kcell.kz', 1, 'Irina.Belova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Elena.Blinova@kcell.kz', 1, 'Elena.Blinova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Assya.Yeriskina@kcell.kz', 1, 'Assya.Yeriskina@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Mariya.Dmitruk@kcell.kz', 1, 'Mariya.Dmitruk@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Akmaral.Aldasheva@kcell.kz', 1, 'Akmaral.Aldasheva@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Alexandr.Varfolomeyev@kcell.kz', 1, 'Alexandr.Varfolomeyev@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Rustambek.Kebirov@kcell.kz', 1, 'Rustambek.Kebirov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Anel.Udanova@kcell.kz', 1, 'Anel.Udanova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Yuliya.Luchshikova@kcell.kz', 1, 'Yuliya.Luchshikova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vadim.Li@kcell.kz', 1, 'Vadim.Li@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Stepan.Kipel@kcell.kz', 1, 'Stepan.Kipel@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Valentin.Lissassin@kcell.kz', 1, 'Valentin.Lissassin@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Stanislav.Li@kcell.kz', 1, 'Valentin.Lissassin@kcell.kz', 2147483647);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Damir.Auezov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Denis.Revenko@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Roman.Shakhmatov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Evgeniy.Grebnev@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Irina.Belova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Elena.Blinova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Assya.Yeriskina@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Mariya.Dmitruk@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Akmaral.Aldasheva@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Alexandr.Varfolomeyev@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Rustambek.Kebirov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Anel.Udanova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Yuliya.Luchshikova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Vadim.Li@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Stepan.Kipel@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Valentin.Lissassin@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Stanislav.Li@kcell.kz', 2);

commit;