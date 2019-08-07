begin;

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'alm_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'alm_engineer', null, 6, 'sdr_srr_request', 4930);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'astana_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'astana_engineer', null, 6, 'sdr_srr_request', 4930);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'nc_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'nc_engineer', null, 6, 'sdr_srr_request', 4930);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'east_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'east_engineer', null, 6, 'sdr_srr_request', 4930);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'south_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'south_engineer', null, 6, 'sdr_srr_request', 4930);

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'west_engineer', null, 6, 'Dismantle', 4930);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'west_engineer', null, 6, 'sdr_srr_request', 4930);


INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('infrastructure_dismantle_users', 1, 'infrastructure_dismantle_users', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'infrastructure_dismantle_users', null, 2, 'infrastructure_dismantle_users', 2) ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('dismantle_replacement_central_planning', 1, 'dismantle_replacement_central_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'dismantle_replacement_central_planning', null, 2, 'dismantle_replacement_central_planning', 2) ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('dismantle_replacement_central_leasing', 1, 'dismantle_replacement_central_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'dismantle_replacement_central_leasing', null, 2, 'dismantle_replacement_central_leasing', 2) ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('dismantle_replacement_central_sfm', 1, 'dismantle_replacement_central_sfm', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'dismantle_replacement_central_sfm', null, 2, 'dismantle_replacement_central_sfm', 2) ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('dismantle_replacement_central_tnu', 1, 'dismantle_replacement_central_tnu', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'dismantle_replacement_central_tnu', null, 2, 'dismantle_replacement_central_tnu', 2) ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_group (id_, rev_, name_, type_) VALUES ('dismantle_replacement_central_sao', 1, 'dismantle_replacement_central_sao', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, 'dismantle_replacement_central_sao', null, 2, 'dismantle_replacement_central_sao', 2) ON CONFLICT(id_) DO NOTHING;


INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Daniyar.Yespayev@kcell.kz', 2, 'Daniyar', 'Yespayev', 'Daniyar.Yespayev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Yespayev@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Yespayev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Daniyar.Yespayev@kcell.kz', 1, 'Daniyar.Yespayev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Dastan.Nussupov@kcell.kz', 2, 'Dastan', 'Nussupov', 'Dastan.Nussupov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Dastan.Nussupov@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Dastan.Nussupov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Dastan.Nussupov@kcell.kz', 1, 'Dastan.Nussupov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Gulzhan.Imandosova@kcell.kz', 2, 'Gulzhan', 'Imandosova', 'Gulzhan.Imandosova@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Imandosova@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Imandosova@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Gulzhan.Imandosova@kcell.kz', 1, 'Gulzhan.Imandosova@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Ramil.Albakov@kcell.kz', 2, 'Ramil', 'Albakov', 'Ramil.Albakov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ramil.Albakov@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ramil.Albakov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Ramil.Albakov@kcell.kz', 1, 'Ramil.Albakov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Saken.Dochshanov@kcell.kz', 2, 'Saken', 'Dochshanov', 'Saken.Dochshanov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Saken.Dochshanov@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Saken.Dochshanov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Saken.Dochshanov@kcell.kz', 1, 'Saken.Dochshanov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Vladimir.Grachyov@kcell.kz', 2, 'Vladimir', 'Grachyov', 'Vladimir.Grachyov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Grachyov@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Grachyov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Vladimir.Grachyov@kcell.kz', 1, 'Vladimir.Grachyov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Yaroslav.Arabok@kcell.kz', 2, 'Yaroslav', 'Arabok', 'Yaroslav.Arabok@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yaroslav.Arabok@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yaroslav.Arabok@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Yaroslav.Arabok@kcell.kz', 1, 'Yaroslav.Arabok@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Yuliya.Prikhodko@kcell.kz', 2, 'Yuliya', 'Prikhodko', 'Yuliya.Prikhodko@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Prikhodko@kcell.kz', 'dismantle_replacement_central_planning');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Yuliya.Prikhodko@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Yuliya.Prikhodko@kcell.kz', 1, 'Yuliya.Prikhodko@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Sara.Turabayeva@kcell.kz', 2, 'Sara', 'Turabayeva', 'Sara.Turabayeva@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sara.Turabayeva@kcell.kz', 'dismantle_replacement_central_leasing');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sara.Turabayeva@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Sara.Turabayeva@kcell.kz', 1, 'Sara.Turabayeva@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 2, 'Ikhtiyar', 'Ibrayev', 'Ikhtiyar.Ibrayev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 'dismantle_replacement_central_leasing');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Ikhtiyar.Ibrayev@kcell.kz', 1, 'Ikhtiyar.Ibrayev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Ainur.Beknazarova@kcell.kz', 2, 'Ainur', 'Beknazarova', 'Ainur.Beknazarova@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ainur.Beknazarova@kcell.kz', 'dismantle_replacement_central_leasing');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Ainur.Beknazarova@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Ainur.Beknazarova@kcell.kz', 1, 'Ainur.Beknazarova@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Bella.Mamatova@kcell.kz', 2, 'Bella', 'Mamatova', 'Bella.Mamatova@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Bella.Mamatova@kcell.kz', 'dismantle_replacement_central_leasing');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Bella.Mamatova@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Bella.Mamatova@kcell.kz', 1, 'Bella.Mamatova@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Andrey.Medvedev@kcell.kz', 2, 'Andrey', 'Medvedev', 'Andrey.Medvedev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Andrey.Medvedev@kcell.kz', 'dismantle_replacement_central_sfm');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Andrey.Medvedev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Andrey.Medvedev@kcell.kz', 1, 'Andrey.Medvedev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Sergey.Chumachenko@kcell.kz', 2, 'Sergey', 'Chumachenko', 'Sergey.Chumachenko@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chumachenko@kcell.kz', 'dismantle_replacement_central_sfm');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chumachenko@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Sergey.Chumachenko@kcell.kz', 1, 'Sergey.Chumachenko@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Rafael.Alimbetov@kcell.kz', 2, 'Rafael', 'Alimbetov', 'Rafael.Alimbetov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'dismantle_replacement_central_tnu');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Rafael.Alimbetov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Rafael.Alimbetov@kcell.kz', 1, 'Rafael.Alimbetov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Galym.Tulenbayev@kcell.kz', 2, 'Galym', 'Tulenbayev', 'Galym.Tulenbayev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Galym.Tulenbayev@kcell.kz', 'dismantle_replacement_central_tnu');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Galym.Tulenbayev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Galym.Tulenbayev@kcell.kz', 1, 'Galym.Tulenbayev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Zhanibek.Mailybayev@kcell.kz', 2, 'ZhRafael.Alimbetov@kcell.kzanibek', 'Mailybayev', 'Zhanibek.Mailybayev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Zhanibek.Mailybayev@kcell.kz', 'dismantle_replacement_central_sao');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Zhanibek.Mailybayev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Zhanibek.Mailybayev@kcell.kz', 1, 'Zhanibek.Mailybayev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Adil.Sandykbayev@kcell.kz', 2, 'Adil', 'Sandykbayev', 'Adil.Sandykbayev@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Adil.Sandykbayev@kcell.kz', 'dismantle_replacement_central_sao');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Adil.Sandykbayev@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Adil.Sandykbayev@kcell.kz', 1, 'Adil.Sandykbayev@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

INSERT INTO public.act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_, salt_, lock_exp_time_, attempts_) VALUES ('Kairat.Parmanov@kcell.kz', 2, 'Kairat', 'Parmanov', 'Kairat.Parmanov@kcell.kz', '{SHA-512}DhqxY2MTmb+QMac/TDoetlDmWtf/IULnI+Ofkm6FdbkBSqT2eLVJ3tFdn96Fs4OFnvv7GskNnjVGF6QY7PBFhw==', null, 'tLSdWo1Om7gB6uluLlEa6Q==', null, 0) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Kairat.Parmanov@kcell.kz', 'dismantle_replacement_central_sao');
INSERT INTO public.act_id_membership (user_id_, group_id_) VALUES ('Kairat.Parmanov@kcell.kz', 'infrastructure_dismantle_users');
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES (uuid_generate_v4(), 1, 1, null, 'Kairat.Parmanov@kcell.kz', 1, 'Kairat.Parmanov@kcell.kz', 2147483647) ON CONFLICT(type_, user_id_, resource_type_, resource_id_) DO NOTHING;

commit;