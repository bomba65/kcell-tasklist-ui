
--ALTER USER camunda WITH ENCRYPTED PASSWORD 'camunda';
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
ALTER TABLE public.act_id_user ALTER COLUMN first_ TYPE citext;
ALTER TABLE public.act_id_user ALTER COLUMN last_ TYPE citext;

--USERS
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('natalya.spirikova@sms-consult.kz', 1, 'natalya', 'spirikova', 'natalya.spirikova@sms-consult.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('e.lopatin@sms-consult.kz', 1, 'e', 'lopatin', 'e.lopatin@sms-consult.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('zaituna@mobcontent.kz', 1, 'zaituna', 'mobcontent', 'zaituna@mobcontent.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('svetlana@kazinfoteh.kz', 1, 'svetlana', 'kazinfoteh', 'svetlana@kazinfoteh.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 1, 'Sagida', 'Adiyeva', 'Sagida.Adiyeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nazym.Muralimova@kcell.kz', 1, 'Nazym', 'Muralimova', 'Nazym.Muralimova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Vadim.Li@kcell.kz', 1, 'Vadim', 'Li', 'Vadim.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 1, 'Andrey', 'Kravchenko', 'Andrey.Kravchenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 1, 'Vassiliy', 'Perekrestov', 'Vassiliy.Perekrestov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Tatyana.Agapova@kcell.kz', 1, 'Tatyana', 'Agapova', 'Tatyana.Agapova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Kirill.Klimov@kcell.kz', 1, 'Kirill', 'Klimov', 'Kirill.Klimov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Batima.Aimagambetova@kcell.kz', 1, 'Batima', 'Aimagambetova', 'Batima.Aimagambetova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nikolai.Naumov@kcell.kz', 1, 'Nikolai', 'Naumov', 'Nikolai.Naumov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 1, 'Roman', 'Shakhmatov', 'Roman.Shakhmatov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 1, 'Evgeniy', 'Grebnev', 'Evgeniy.Grebnev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 1, 'Rafael', 'Alimbetov', 'Rafael.Alimbetov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Ilya.Gorpinich@kcell.kz', 1, 'Ilya', 'Gorpinich', 'Ilya.Gorpinich@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stanislav.Li@kcell.kz', 1, 'Stanislav', 'Li', 'Stanislav.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);

INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Assel.Nurmanova@kcell.kz', 1, 'Assel', 'Nurmanova', 'Assel.Nurmanova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Marina.Vassilyeva@kcell.kz', 1, 'Marina', 'Vassilyeva', 'Marina.Vassilyeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Ivan.Rzhanikov@kcell.kz', 1, 'Ivan', 'Rzhanikov', 'Ivan.Rzhanikov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 1, 'Yuliya', 'Luchshikova', 'Yuliya.Luchshikova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Igor.Galichenko@kcell.kz', 1, 'Igor', 'Galichenko', 'Igor.Galichenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Madi.Mukhametov@kcell.kz', 1, 'Madi', 'Mukhametov', 'Madi.Mukhametov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Guzelay.Iliyeva@kcell.kz', 1, 'Guzelay', 'Iliyeva', 'Guzelay.Iliyeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Guzel.Zhapparova@kcell.kz', 1, 'Guzel', 'Zhapparova', 'Guzel.Zhapparova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);

--GROUPS
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('provider_users', 1, 'provider_users', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_freephone_users', 1, 'delivery_freephone_users', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_sms_consult', 1, 'delivery_provider_sms_consult', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_mms', 1, 'delivery_provider_mms', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_kit', 1, 'delivery_provider_kit', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_sms_ivr_b2b_delivery', 1, 'delivery_sms_ivr_b2b_delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_enterprise_data_network', 1, 'delivery_enterprise_data_network', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_voice_network', 1, 'delivery_voice_network', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_transmission_network_operations', 1, 'delivery_transmission_network_operations', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_transmission_group', 1, 'delivery_transmission_group', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_it_delivery', 1, 'delivery_it_delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_billing_supervisor_amdocs', 1, 'Billing Supervisor Amdocs', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_billing_supervisor_orga', 1, 'Billing Supervisor Amdocs', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_billing_engineer_amdocs', 1, 'Billing Engineer Amdocs', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_billing_engineer_orga', 1, 'Billing Engineer Orga', 'WORKFLOW');

--MEMBERSHIPS
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'provider_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'provider_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'provider_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'provider_users');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Tatyana.Agapova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kirill.Klimov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Batima.Aimagambetova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nikolai.Naumov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ilya.Gorpinich@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'kcellUsers');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assel.Nurmanova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marina.Vassilyeva@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ivan.Rzhanikov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Igor.Galichenko@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Madi.Mukhametov@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Guzelay.Iliyeva@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Guzel.Zhapparova@kcell.kz', 'kcellUsers');


INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Tatyana.Agapova@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kirill.Klimov@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Batima.Aimagambetova@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nikolai.Naumov@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ilya.Gorpinich@kcell.kz', 'delivery_freephone_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_freephone_users');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'delivery_provider_sms_consult');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'delivery_provider_sms_consult');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'delivery_provider_mms');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'delivery_provider_kit');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'delivery_sms_ivr_b2b_delivery');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'delivery_sms_ivr_b2b_delivery');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_enterprise_data_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'delivery_enterprise_data_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'delivery_enterprise_data_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Tatyana.Agapova@kcell.kz', 'delivery_voice_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kirill.Klimov@kcell.kz', 'delivery_voice_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Batima.Aimagambetova@kcell.kz', 'delivery_voice_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nikolai.Naumov@kcell.kz', 'delivery_voice_network');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Shakhmatov@kcell.kz', 'delivery_transmission_network_operations');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Grebnev@kcell.kz', 'delivery_transmission_network_operations');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'delivery_transmission_group');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ilya.Gorpinich@kcell.kz', 'delivery_transmission_group');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_it_delivery');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assel.Nurmanova@kcell.kz', 'delivery_billing_supervisor_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assel.Nurmanova@kcell.kz', 'delivery_billing_supervisor_orga');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marina.Vassilyeva@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ivan.Rzhanikov@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assel.Nurmanova@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Luchshikova@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Igor.Galichenko@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Madi.Mukhametov@kcell.kz', 'delivery_billing_engineer_amdocs');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ivan.Rzhanikov@kcell.kz', 'delivery_billing_engineer_orga');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Guzelay.Iliyeva@kcell.kz', 'delivery_billing_engineer_orga');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Guzel.Zhapparova@kcell.kz', 'delivery_billing_engineer_orga');

--auth
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'natalya.spirikova@sms-consult.kz', 1, 'natalya.spirikova@sms-consult.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'e.lopatin@sms-consult.kz', 1, 'e.lopatin@sms-consult.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'zaituna@mobcontent.kz', 1, 'zaituna@mobcontent.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'svetlana@kazinfoteh.kz', 1, 'svetlana@kazinfoteh.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Sagida.Adiyeva@kcell.kz', 1, 'Sagida.Adiyeva@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Nazym.Muralimova@kcell.kz', 1, 'Nazym.Muralimova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vadim.Li@kcell.kz', 1, 'Vadim.Li@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Andrey.Kravchenko@kcell.kz', 1, 'Andrey.Kravchenko@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vassiliy.Perekrestov@kcell.kz', 1, 'Vassiliy.Perekrestov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Tatyana.Agapova@kcell.kz', 1, 'Tatyana.Agapova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Kirill.Klimov@kcell.kz', 1, 'Kirill.Klimov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Batima.Aimagambetova@kcell.kz', 1, 'Batima.Aimagambetova@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Nikolai.Naumov@kcell.kz', 1, 'Nikolai.Naumov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Roman.Shakhmatov@kcell.kz', 1, 'Roman.Shakhmatov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Evgeniy.Grebnev@kcell.kz', 1, 'Evgeniy.Grebnev@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Rafael.Alimbetov@kcell.kz', 1, 'Rafael.Alimbetov@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Ilya.Gorpinich@kcell.kz', 1, 'Ilya.Gorpinich@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Stanislav.Li@kcell.kz', 1, 'Stanislav.Li@kcell.kz', 2147483647);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'natalya.spirikova@sms-consult.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'e.lopatin@sms-consult.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'zaituna@mobcontent.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'svetlana@kazinfoteh.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Sagida.Adiyeva@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Nazym.Muralimova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Vadim.Li@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Andrey.Kravchenko@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Vassiliy.Perekrestov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Tatyana.Agapova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Kirill.Klimov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Batima.Aimagambetova@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Nikolai.Naumov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Roman.Shakhmatov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Evgeniy.Grebnev@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Rafael.Alimbetov@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Ilya.Gorpinich@kcell.kz', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'kcellUsers',null, 1, 'Stanislav.Li@kcell.kz', 2);


--for search on create application form

-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nazym.Muralimova@kcell.kz', 1, 'Nazym', 'Muralimova', 'Nazym.Muralimova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 1, 'Sagida', 'Adiyeva', 'Sagida.Adiyeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
--USERS

-- --groups
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_sms_ivr_b2b_sales', 1, 'delivery_sms_ivr_b2b_sales', 'WORKFLOW');
--
-- --memberships
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aibek.Dosbayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aidarkhan.Kursarin@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aigerim.Anarbayeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aigul.Demesinova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ainur.Manabaeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aisulu.Zhakupova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Almas.Serikbekuly@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rauan.Daniyarov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexey.Dergachev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aliya.Kaldybekova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anar.Sarsenova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anton.Iterman@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Baglan.Bitenova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assylzhan.Almussin@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anastassiya.Kromiadi@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Bakhtiyar.Bessembayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Dalabay.Shalabayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daulet.Nagumanov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Diana.Kassenova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Dinara.Kassymova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Erkin.Akimzhanov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mukhammed.Amreyev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Gaukhar.Baiseitova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Gennadiy.Li@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Oxykbayeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Igor.Azhigaliyev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ilona.Vassilyeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Inara.Kuatpayeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Khasan.Jalalov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kirill.Chelombitskiy@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Klavdiya.Vition@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maxim.Soldatov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Liliya.Tsoy@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Oleg.Li@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mariya.Makherina@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Olga.Stepanova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sukhrab.Pakhritdinov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Oxana.Bezverhaya@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Seil.Rahim@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ravil.Bairamov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Mamin@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Pak@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askhat.Ashimov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustam.Karzhauov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Agapkin@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Savchenko@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assel.Alimbayeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Svetlana.Budashova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Umit.Salkeyeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerlan.Issanov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerlan.Omarov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuriy.Nagaitsev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yekaterina.Zorina@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zarema.Seferova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zarina.Zhumadilova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhamal.Tlegenova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhanar.Amirkulova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhandos.Bektemirov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhazira.Tazabek@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rassul.Rakhimov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rakhilya.Yessirkepova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurlan.Rakhimov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhaxylyk.Kozhakhmetov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Asanzhan.Baltushev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maksim.Shnytkin@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aliya.Mussina@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nigara.Zairova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nursultan.Nagumanov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Zhalelov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Natalya.Tsoy@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Madi.Smakov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Timur.Amirov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Baurzhan.Mukhamejanov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yeldar.Azimbayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Manat.Mukatayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kuanysh.Dossumbayev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurlan.Magzumov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ainur.Syrgabayeva@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askar.Zhumashev@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerlan.Beissenov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nadezhda.Zhonisova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Olzhas.Akparov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Elmira.Urbisinova@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assylbek.Kassiyenov@kcell.kz', 'delivery_sms_ivr_b2b_sales');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Assem.Saifutdinova@kcell.kz', 'delivery_sms_ivr_b2b_sales');