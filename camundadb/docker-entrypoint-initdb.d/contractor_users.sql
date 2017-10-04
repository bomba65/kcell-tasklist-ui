
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('contractor_users', 1, 'Contractor Users', 'WORKFLOW');

update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'anuarbek.m@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'beibit.a@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'botagoz.ch@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'kairat.b@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'gulzhaina.t@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'meirkhan.k@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'nazym.s@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'rn-rs-almaty@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'rollout-almaty-main@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'zhandos.k@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'LSE_Revision_Alm' and group_id_ = 'kcellUsers';

INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e678be-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Claimed Tasks', 'contractor_users', '{"taskAssigneeExpression":"${ currentUser() }"}', '{"variables":[{"name":"jrNumber","label":"JR Number"}],"description":"My Claimed Tasks","priority":-10}');
INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e97d61-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Unclaimed Tasks', 'contractor_users', '{"taskCandidateUserExpression":"${currentUser()}"}', '{"variables":[{"name":"jrNumber","label":"JR Number"}],"description":"My Unclaimed Tasks","priority":-10}');
INSERT INTO act_ru_filter (id_, rev_, resource_type_, name_, owner_, query_, properties_) VALUES ('00e32b3a-4f69-11e7-a8c6-0242ac120007', 1, 'Task', 'My Group Tasks', 'contractor_users', '{"taskCandidateGroupInExpression":"${ currentUserGroups() }"}', '{"variables":[{"name":"jrNumber","label":"JR Number"}],"description":"My Group Tasks","priority":-10}');

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00fa443f-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e642ad-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00e349e2-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e83e80-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00ef5f55-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e99e13-4f69-11e7-a8c6-0242ac120007', 2);

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('37a09efc-6569-11e7-b615-0242ac150007', 1, 1, 'contractor_users', NULL, 1, '*', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('ffb445d0-4f68-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 2, '*', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 0, 'tasklist', 32);