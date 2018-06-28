--USERS
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nazym.Muralimova@kcell.kz', 1, 'Nazym', 'Muralimova', 'Nazym.Muralimova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 1, 'Nabat', 'Oralbekova', 'Nabat.Oralbekova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stanislav.Li@kcell.kz', 1, 'Li', 'Stanislav', 'Stanislav.Li@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('kazinfotech', 1, 'kazinfotech', '', 'info@kazinfotech.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('smsconsult', 1, 'smsconsult', '', 'info@smsconsult.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);

--GROUPS
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_kae', 1, 'KAE', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_b2b', 1, 'B2B Delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_it', 1, 'IT Delivery', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_tech_sao', 1, 'TECH-SAO-SO-CVO', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('smsUsers', 1, 'bulk SMS Users', 'WORKFLOW');

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_partner_b2b_senior', 1, 'B2B Senior', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_partner_b2b_manager', 1, 'B2B Manager', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_partner_finance_dept', 1, 'Financial department', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('sms_partner', 1, 'Partner', 'WORKFLOW');

--MEMBERSHIPS
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'sms_b2b');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nazym.Muralimova@kcell.kz', 'smsUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 'smsUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nabat.Oralbekova@kcell.kz', 'sms_tech_sao');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'smsUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Li@kcell.kz', 'sms_it');

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('kazinfotech', 'smsUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('smsconsult', 'smsUsers');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('kazinfotech', 'sms_partner');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('smsconsult', 'sms_partner');

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008bc61f-4f69-11e7-a8c6-0242ac120027', 1, 1, 'sms_kae', NULL, 6, 'connectionProcess', 2147483647);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008bc61f-4f69-11e7-a8c6-0242ac120067', 1, 1, 'sms_b2b', NULL, 6, 'connectionProcessB2B', 2147483647);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008bc61f-4f69-11e7-a8c6-0242ac120037', 1, 1, 'sms_kae', NULL, 6, 'disconnectionProcess', 2147483647);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008bc61f-4f69-11e7-a8c6-0242ac120047', 1, 1, 'sms_partner', NULL, 6, 'connectionSMSthrougPartners', 2147483647);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008bc61f-4f69-11e7-a8c6-0242ac120057', 1, 1, 'sms_partner', NULL, 6, 'disconnectionSMSthrougPartners', 2147483647);

--FILTERS
INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e642gd-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Claimed Tasks', 'smsUsers', '{"taskAssigneeExpression":"${ currentUser() }"}', '{"variables":[{"name":"companyName","label":"Компания"}],"description":"Мои Задачи","priority":-10}');
INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e83eg0-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Unclaimed Tasks', 'smsUsers', '{"taskCandidateUserExpression":"${currentUser()}"}', '{"variables":[{"name":"companyName","label":"Компания"}],"description":"Задачи назначенные мне","priority":-10}');
INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e99eg3-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Group Tasks', 'smsUsers', '{"taskCandidateGroupInExpression":"${ currentUserGroups() }"}', '{"variables":[{"name":"companyName","label":"Компания"}],"description":"Задачи назначенные группе","priority":-10}');

--AUTHORIZATIONS
--FILTER AUTHORIZATION
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00e7a2gf-4f69-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 5, '00e642gd-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00e928g2-4f69-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 5, '00e83eg0-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00ea3ag5-4f69-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 5, '00e99eg3-4f69-11e7-a8c6-0242ac120007', 2);

--PROCESS INSTANCE AUTHORIZATION
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008d9ag1-4f69-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 8, '*', 2147483647);

--APPLICATION AUTHORIZATION
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008a3fge-4f69-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 0, 'tasklist', 32);


--CAMUNDA-ADMIN AUTHORIZATION
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('ffb319g0-4f68-11e7-a8c6-0242ac120007', 1, 1, 'smsUsers', NULL, 2, '*', 2);

--USER AUTHORIZATION
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('06e2fdg1-6569-11e7-b615-0242ac150007', 1, 1, 'smsUsers', NULL, 1, '*', 2);