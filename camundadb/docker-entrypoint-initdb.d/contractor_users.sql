
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('contractor_users', 1, 'Contractor Users', 'WORKFLOW');

update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'anuarbek.m@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'beibit.a@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'botagoz.ch@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'gulzhaina.t@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'kairat.b@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'LSE_Revision_Alm' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'meirkhan.k@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'nazym.s@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'rn-rs-almaty@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'rollout-almaty-main@lse.kz' and group_id_ = 'kcellUsers';
update act_id_membership set group_id_ = 'contractor_users' where user_id_ = 'zhandos.k@lse.kz' and group_id_ = 'kcellUsers';

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('ab656832-501a-11e7-a8c4-0242ac120006', 3, 1, 'contractor_users', NULL, 6, 'Revision', 4674);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008d9ff1-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 8, '*', 2147483647);

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00fa443f-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e642ad-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00e349e2-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e83e80-4f69-11e7-a8c6-0242ac120007', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('00ef5f55-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 5, '00e99e13-4f69-11e7-a8c6-0242ac120007', 2);

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('37a09efc-6569-11e7-b615-0242ac150007', 1, 1, 'contractor_users', NULL, 1, '*', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('ffb445d0-4f68-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 2, '*', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('008a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 0, 'tasklist', 32);

INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('009a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'anuarbek.m@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'beibit.a@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'botagoz.ch@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'gulzhaina.t@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'kairat.b@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'LSE_Revision_Alm', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'meirkhan.k@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'nazym.s@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'rn-rs-almaty@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'rollout-almaty-main@lse.kz', 2);
INSERT INTO act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('010a7f7c-4f69-11e7-a8c6-0242ac120007', 1, 1, 'contractor_users', NULL, 1, 'zhandos.k@lse.kz', 2);