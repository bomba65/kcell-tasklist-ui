begin ON CONFLICT(id_) DO NOTHING;
--set client_min_messages to ERROR;
--USERS
INSERT INTO act_id_user (id_, rev_, first_, last_, pwd_, picture_id_) VALUES ('Logycom_Revision_HQ', 1, 'Logycom_Revision', 'HQ', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, pwd_, picture_id_) VALUES ('ALTA_Revision_HQ', 1, 'ALTA_Revision', 'HQ', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, pwd_, picture_id_) VALUES ('Arlan_Revision_HQ', 1, 'Arlan_Revision', 'HQ', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', NULL) ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_contractor_logycom', 1, 'hq_contractor_logycom', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_contractor_alta', 1, 'hq_contractor_alta', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_contractor_arlan', 1, 'hq_contractor_arlan', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;


INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Logycom_Revision_HQ', 'hq_contractor_logycom') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Logycom_Revision_HQ', 'contractor_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Logycom_Revision_HQ', 'contractor_users_logycom') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Logycom_Revision_HQ', 'infrastructure_monthly_act_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Logycom_Revision_HQ', 'infrastructure_revision_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('ALTA_Revision_HQ', 'hq_contractor_alta') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('ALTA_Revision_HQ', 'contractor_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('ALTA_Revision_HQ', 'contractor_users_alta') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('ALTA_Revision_HQ', 'infrastructure_monthly_act_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('ALTA_Revision_HQ', 'infrastructure_revision_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arlan_Revision_HQ', 'hq_contractor_arlan') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arlan_Revision_HQ', 'contractor_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arlan_Revision_HQ', 'contractor_users_arlan') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arlan_Revision_HQ', 'infrastructure_monthly_act_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arlan_Revision_HQ', 'infrastructure_revision_users') ON CONFLICT(user_id_, group_id_) DO NOTHING;

commit;
