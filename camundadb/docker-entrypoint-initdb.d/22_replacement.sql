begin;

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 2, 1, 'infrastructure_replacement_users', null, 6, 'Replacement', 4674);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 2, 1, 'infrastructure_replacement_users', null, 6, 'sdr_srr_request', 4674);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'alm_engineer', null, 6, 'Replacement', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'astana_engineer', null, 6, 'Replacement', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'nc_engineer', null, 6, 'Replacement', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'east_engineer', null, 6, 'Replacement', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'south_engineer', null, 6, 'Replacement', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'west_engineer', null, 6, 'Replacement', 4930);


INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('infrastructure_replacement_users', 1, 'infrastructure_replacement_users', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'infrastructure_replacement_users', null, 2, 'infrastructure_replacement_users', 2) ON CONFLICT(type_, group_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Yespayev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Dastan.Nussupov@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Imandosova@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ramil.Albakov@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Saken.Dochshanov@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Grachyov@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yaroslav.Arabok@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Prikhodko@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sara.Turabayeva@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ainur.Beknazarova@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Bella.Mamatova@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Andrey.Medvedev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chumachenko@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Galym.Tulenbayev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Zhanibek.Mailybayev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Adil.Sandykbayev@kcell.kz', 'infrastructure_replacement_users');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Kairat.Parmanov@kcell.kz', 'infrastructure_replacement_users');

commit;