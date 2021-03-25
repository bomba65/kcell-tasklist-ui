begin;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('hq_ma_responsible', 1, 'hq_ma_responsible', 'WORKFLOW');

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('da6b60b3-8bdf-11eb-ae7f-0242ac130008', 1, 1, 'hq_ma_responsible', null, 6, 'monthlyAct', 770);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('a430c758-8bdf-11eb-ae7f-0242ac130008', 1, 1, 'hq_ma_responsible', null, 2, 'hq_ma_responsible', 2);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_) VALUES ('b85ab747-8be0-11eb-ae7f-0242ac130008', 1, 1, 'hq_contractor_alta', null, 6, 'monthlyAct');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_) VALUES ('81630f86-8be0-11eb-ae7f-0242ac130008', 2, 1, 'hq_contractor_logycom', null, 6, 'monthlyAct');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_) VALUES ('6d5ba655-8be0-11eb-ae7f-0242ac130008', 1, 1, 'hq_contractor_arlan', null, 6, 'monthlyAct');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_) VALUES ('008b0d44-8be0-11eb-ae7f-0242ac130008', 1, 1, 'hq_contractor_lse', null, 6, 'monthlyAct');


commit;