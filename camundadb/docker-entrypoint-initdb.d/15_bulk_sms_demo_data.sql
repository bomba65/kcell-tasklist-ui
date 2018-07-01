
--ALTER USER camunda WITH ENCRYPTED PASSWORD 'camunda';
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;
-- ALTER TABLE public.act_id_user ALTER COLUMN first_ TYPE citext;
-- ALTER TABLE public.act_id_user ALTER COLUMN last_ TYPE citext;

--USERS
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('natalya.spirikova@sms-consult.kz', 1, 'natalya', 'spirikova', 'natalya.spirikova@sms-consult.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('e.lopatin@sms-consult.kz', 1, 'e', 'lopatin', 'e.lopatin@sms-consult.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('zaituna@mobcontent.kz', 1, 'zaituna', 'mobcontent', 'zaituna@mobcontent.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('svetlana@kazinfoteh.kz', 1, 'svetlana', 'kazinfoteh', 'svetlana@kazinfoteh.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 1, 'Sagida', 'Adiyeva', 'Sagida.Adiyeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nazym.Muralimova@kcell.kz', 1, 'Nazym', 'Muralimova', 'Nazym.Muralimova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Vadim.Li@kcell.kz', 1, 'Vadim', 'Li', 'Vadim.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 1, 'Andrey', 'Kravchenko', 'Andrey.Kravchenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 1, 'Vassiliy', 'Perekrestov', 'Vassiliy.Perekrestov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
-- INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stanislav.Li@kcell.kz', 1, 'Stanislav', 'Li', 'Stanislav.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Andrey.Khan@kcell.kz', 1, 'Andrey', 'Khan', 'Andrey.Khan@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Alexander.Travkin@kcell.kz', 1, 'Alexander', 'Travkin', 'Alexander.Travkin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 1, 'Nabat', 'Oralbekova', 'Nabat.Oralbekova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);

--GROUPS
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('provider_users', 1, 'provider_users', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_sms_users', 1, 'delivery_sms_users', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_sms_consult', 1, 'delivery_provider_sms_consult', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_mms', 1, 'delivery_provider_mms', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_provider_kit', 1, 'delivery_provider_kit', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_sms_ivr_b2b_delivery', 1, 'delivery_sms_ivr_b2b_delivery', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_enterprise_data_network', 1, 'delivery_enterprise_data_network', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_voice_network', 1, 'delivery_voice_network', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_transmission_network_operations', 1, 'delivery_transmission_network_operations', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_transmission_group', 1, 'delivery_transmission_group', 'WORKFLOW');
-- INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_it_delivery', 1, 'delivery_it_delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('delivery_tech_sao_so_cvo', 1, 'delivery_tech_sao_so_cvo', 'WORKFLOW');

--MEMBERSHIPS
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'provider_users');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'provider_users');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'provider_users');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'provider_users');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'kcellUsers');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'kcellUsers');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'kcellUsers');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'kcellUsers');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'kcellUsers');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Khan@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexander.Travkin@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 'kcellUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Khan@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexander.Travkin@kcell.kz', 'delivery_sms_users');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 'delivery_sms_users');

-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('natalya.spirikova@sms-consult.kz', 'delivery_provider_sms_consult');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('e.lopatin@sms-consult.kz', 'delivery_provider_sms_consult');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Khan@kcell.kz', 'delivery_tech_sao_so_cvo');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexander.Travkin@kcell.kz', 'delivery_tech_sao_so_cvo');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 'delivery_tech_sao_so_cvo');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('zaituna@mobcontent.kz', 'delivery_provider_mms');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('svetlana@kazinfoteh.kz', 'delivery_provider_kit');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sagida.Adiyeva@kcell.kz', 'delivery_sms_ivr_b2b_delivery');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'delivery_sms_ivr_b2b_delivery');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vadim.Li@kcell.kz', 'delivery_enterprise_data_network');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Kravchenko@kcell.kz', 'delivery_enterprise_data_network');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Perekrestov@kcell.kz', 'delivery_enterprise_data_network');
-- INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'delivery_it_delivery');

--auth
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'natalya.spirikova@sms-consult.kz', 1, 'natalya.spirikova@sms-consult.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'e.lopatin@sms-consult.kz', 1, 'e.lopatin@sms-consult.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'zaituna@mobcontent.kz', 1, 'zaituna@mobcontent.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'svetlana@kazinfoteh.kz', 1, 'svetlana@kazinfoteh.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Sagida.Adiyeva@kcell.kz', 1, 'Sagida.Adiyeva@kcell.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Nazym.Muralimova@kcell.kz', 1, 'Nazym.Muralimova@kcell.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vadim.Li@kcell.kz', 1, 'Vadim.Li@kcell.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Andrey.Kravchenko@kcell.kz', 1, 'Andrey.Kravchenko@kcell.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vassiliy.Perekrestov@kcell.kz', 1, 'Vassiliy.Perekrestov@kcell.kz', 2147483647);
-- INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Stanislav.Li@kcell.kz', 1, 'Stanislav.Li@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Andrey.Khan@kcell.kz', 1, 'Andrey.Khan@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Alexander.Travkin@kcell.kz', 1, 'Alexander.Travkin@kcell.kz', 2147483647);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Nabat.Oralbekova@kcell.kz', 1, 'Nabat.Oralbekova@kcell.kz', 2147483647);
