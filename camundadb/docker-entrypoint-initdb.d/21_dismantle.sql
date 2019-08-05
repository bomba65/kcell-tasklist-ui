INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('infrastructure_dismantle_users', 1, 'infrastructure_dismantle_users', 'WORKFLOW');

INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('demo', 'infrastructure_dismantle_users');

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('dec89c85-b377-11e9-ae0d-76cc16d14d9d', 1, 1, 'infrastructure_dismantle_users', null, 2, 'infrastructure_dismantle_users', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('5a673000-b378-11e9-ae0d-76cc16d14d9d', 1, 1, 'infrastructure_dismantle_users', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('94d5fd21-b378-11e9-ae0d-76cc16d14d9d', 1, 1, 'infrastructure_dismantle_users', null, 6, 'sdr_srr_request', 4930);

