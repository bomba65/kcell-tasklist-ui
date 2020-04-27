begin;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRolloutGroup', 1, 'LeasingRolloutGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalPlanningGroup', 1, 'LeasingRegionalPlanningGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingCentralPlanningGroup', 1, 'LeasingCentralPlanningGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalGroupHeadSpecialist', 1, 'LeasingRegionalGroupHeadSpecialist', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingCentralGroupHeadSpecialist', 1, 'LeasingCentralGroupHeadSpecialist', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalPowerEngineer', 1, 'LeasingRegionalPowerEngineer', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalTransmissionGroup', 1, 'LeasingRegionalTransmissionGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingPlanningGroup', 1, 'LeasingPlanningGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalGroup', 1, 'LeasingRegionalGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingCentralGroup', 1, 'LeasingCentralGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingPowerEngineer', 1, 'LeasingPowerEngineer', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingInstallationGroup', 1, 'LeasingInstallationGroup', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('LeasingRegionalPlanner', 1, 'LeasingRegionalPlanner', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Ainur.Beknazarova@kcell.kz', 1, 'Ainur', 'Beknazarova', 'Ainur.Beknazarova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexey.Kolesnikov@kcell.kz', 1, 'Alexey', 'Kolesnikov', 'Alexey.Kolesnikov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexey.Kolyagin@kcell.kz', 1, 'Alexey', 'Kolyagin', 'Alexey.Kolyagin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Anastassiya.Shenojak@kcell.kz', 1, 'Anastassiya', 'Shenojak', 'Anastassiya.Shenojak@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Andrei.Lugovoy@kcell.kz', 1, 'Andrei', 'Lugovoy', 'Andrei.Lugovoy@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Askar.Pernebekov@kcell.kz', 1, 'Askar', 'Pernebekov', 'Askar.Pernebekov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Asset.Rashitov@kcell.kz', 1, 'Asset', 'Rashitov', 'Asset.Rashitov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Begaly.Kokin@kcell.kz', 1, 'Begaly', 'Kokin', 'Begaly.Kokin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Bella.Mamatova@kcell.kz', 1, 'Bella', 'Mamatova', 'Bella.Mamatova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Daniyar.Yespayev@kcell.kz', 1, 'Daniyar', 'Yespayev', 'Daniyar.Yespayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Dmitriy.Saidashev@kcell.kz', 1, 'Dmitriy', 'Saidashev', 'Dmitriy.Saidashev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Galym.Tulenbayev@kcell.kz', 1, 'Galym', 'Tulenbayev', 'Galym.Tulenbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kanat.Kulmukhambetov@kcell.kz', 1, 'Kanat', 'Kulmukhambetov', 'Kanat.Kulmukhambetov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Maulen.Kempirbayev@kcell.kz', 1, 'Maulen', 'Kempirbayev', 'Maulen.Kempirbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Maxim.Goikolov@kcell.kz', 1, 'Maxim', 'Goikolov', 'Maxim.Goikolov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurzhan.Kochshigulov@kcell.kz', 1, 'Nurzhan', 'Kochshigulov', 'Nurzhan.Kochshigulov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Samat.Akhmetov@kcell.kz', 1, 'Samat', 'Akhmetov', 'Samat.Akhmetov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Sara.Turabayeva@kcell.kz', 1, 'Sara', 'Turabayeva', 'Sara.Turabayeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Adilet.Baishalov@kcell.kz', 1, 'Adilet', 'Baishalov', 'Adilet.Baishalov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexandr.Kim@kcell.kz', 1, 'Alexandr', 'Kim', 'Alexandr.Kim@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nikolay.Ustinov@kcell.kz', 1, 'Nikolay', 'Ustinov', 'Nikolay.Ustinov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Evgeniy.Degtyarev@kcell.kz', 1, 'Evgeniy', 'Degtyarev', 'Evgeniy.Degtyarev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 1, 'Ikhtiyar', 'Ibrayev', 'Ikhtiyar.Ibrayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Sergey.Michshenko@kcell.kz', 1, 'Sergey', 'Michshenko', 'Sergey.Michshenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Vassiliy.Gopkalo@kcell.kz', 1, 'Vassiliy', 'Gopkalo', 'Vassiliy.Gopkalo@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Vladimir.Yefanov@kcell.kz', 1, 'Vladimir', 'Yefanov', 'Vladimir.Yefanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yermek.Tanabekov@kcell.kz', 1, 'Yermek', 'Tanabekov', 'Yermek.Tanabekov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yevgeniy.Elunin@kcell.kz', 1, 'Yevgeniy', 'Elunin', 'Yevgeniy.Elunin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhanat.Seitkanov@kcell.kz', 1, 'Zhanat', 'Seitkanov', 'Zhanat.Seitkanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Konstantin.Pominov@kcell.kz', 1, 'Konstantin', 'Pominov', 'Konstantin.Pominov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexander.Buss@kcell.kz', 1, 'Alexander', 'Buss', 'Alexander.Buss@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Igor.Orlov@kcell.kz', 1, 'Igor', 'Orlov', 'Igor.Orlov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Evgeniy.Tsoy@kcell.kz', 1, 'Evgeniy', 'Tsoy', 'Evgeniy.Tsoy@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Valeriy.Zhilyaev@kcell.kz', 1, 'Valeriy', 'Zhilyaev', 'Valeriy.Zhilyaev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Pavel.Zaitsev@kcell.kz', 1, 'Pavel', 'Zaitsev', 'Pavel.Zaitsev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Stanislav.Khvan@kcell.kz', 1, 'Stanislav', 'Khvan', 'Stanislav.Khvan@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Dastan.Kazhibayev@kcell.kz', 1, 'Dastan', 'Kazhibayev', 'Dastan.Kazhibayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Altynay.Ashken@kcell.kz', 1, 'Altynay', 'Ashken', 'Altynay.Ashken@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Makhambet.Sandybayev@kcell.kz', 1, 'Makhambet', 'Sandybayev', 'Makhambet.Sandybayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Dulat.Kumekov@kcell.kz', 1, 'Dulat', 'Kumekov', 'Dulat.Kumekov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexandr.Galat@kcell.kz', 1, 'Alexandr', 'Galat', 'Alexandr.Galat@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yuriy.Ivanov@kcell.kz', 1, 'Yuriy', 'Ivanov', 'Yuriy.Ivanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kenzhekhan.Mussayev@kcell.kz', 1, 'Kenzhekhan', 'Mussayev', 'Kenzhekhan.Mussayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aida.Ildebaeva@kcell.kz', 1, 'Aida', 'Ildebaeva', 'Aida.Ildebaeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Azamat.Mukhametalin@kcell.kz', 1, 'Azamat', 'Mukhametalin', 'Azamat.Mukhametalin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Victor.Aredacov@kcell.kz', 1, 'Victor', 'Aredacov', 'Victor.Aredacov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurlan.Sumbaev@kcell.kz', 1, 'Nurlan', 'Sumbaev', 'Nurlan.Sumbaev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Ilya.Nevpryaga2@kcell.kz', 1, 'Ilya', 'Nevpryaga2', 'Ilya.Nevpryaga2@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Roman.Stupko@kcell.kz', 1, 'Roman', 'Stupko', 'Roman.Stupko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Oleg.Yelayev@kcell.kz', 1, 'Oleg', 'Yelayev', 'Oleg.Yelayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Farkhat.Baterikov@kcell.kz', 1, 'Farkhat', 'Baterikov', 'Farkhat.Baterikov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Oleg.Babkin@kcell.kz', 1, 'Oleg', 'Babkin', 'Oleg.Babkin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Rishad.Makirov@kcell.kz', 1, 'Rishad', 'Makirov', 'Rishad.Makirov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Bulat.Smagulov@kcell.kz', 1, 'Bulat', 'Smagulov', 'Bulat.Smagulov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Viktor.Maximenko@kcell.kz', 1, 'Viktor', 'Maximenko', 'Viktor.Maximenko@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Timur.Akylkhanov@kcell.kz', 1, 'Timur', 'Akylkhanov', 'Timur.Akylkhanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Marat.Bakezhanov@kcell.kz', 1, 'Marat', 'Bakezhanov', 'Marat.Bakezhanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Baurzhan.Khamzin@kcell.kz', 1, 'Baurzhan', 'Khamzin', 'Baurzhan.Khamzin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kanat.Mussabekov@kcell.kz', 1, 'Kanat', 'Mussabekov', 'Kanat.Mussabekov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Amangeldy.Kamerdenov@kcell.kz', 1, 'Amangeldy', 'Kamerdenov', 'Amangeldy.Kamerdenov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurbolat.Uteyev@kcell.kz', 1, 'Nurbolat', 'Uteyev', 'Nurbolat.Uteyev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhaxylyk.Moldakhmetov@kcell.kz', 1, 'Zhaxylyk', 'Moldakhmetov', 'Zhaxylyk.Moldakhmetov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Timur.Mukhamedyanov@kcell.kz', 1, 'Timur', 'Mukhamedyanov', 'Timur.Mukhamedyanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexandr.Uzhakin@kcell.kz', 1, 'Alexandr', 'Uzhakin', 'Alexandr.Uzhakin@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Memleket.Toleuuly@kcell.kz', 1, 'Memleket', 'Toleuuly', 'Memleket.Toleuuly@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Marta.Gabdrakhmanova@kcell.kz', 1, 'Marta', 'Gabdrakhmanova', 'Marta.Gabdrakhmanova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurbol.Kalibekuly@kcell.kz', 1, 'Nurbol', 'Kalibekuly', 'Nurbol.Kalibekuly@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhassulan.Makhanbetov@kcell.kz', 1, 'Zhassulan', 'Makhanbetov', 'Zhassulan.Makhanbetov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yerlan.Begimbayev@kcell.kz', 1, 'Yerlan', 'Begimbayev', 'Yerlan.Begimbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aikyn.Bayanbayev@kcell.kz', 1, 'Aikyn', 'Bayanbayev', 'Aikyn.Bayanbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Andrey.Cheslavskiy@kcell.kz', 1, 'Andrey', 'Cheslavskiy', 'Andrey.Cheslavskiy@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Maksat.Nauryzbay@kcell.kz', 1, 'Maksat', 'Nauryzbay', 'Maksat.Nauryzbay@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Tatyana.Tsoy@kcell.kz', 1, 'Tatyana', 'Tsoy', 'Tatyana.Tsoy@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yerbol.Tyshkambayev@kcell.kz', 1, 'Yerbol', 'Tyshkambayev', 'Yerbol.Tyshkambayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Alexandr.Koval@kcell.kz', 1, 'Alexandr', 'Koval', 'Alexandr.Koval@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aziz.Kadyrkulov@kcell.kz', 1, 'Aziz', 'Kadyrkulov', 'Aziz.Kadyrkulov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yerdos.Tuleuov@kcell.kz', 1, 'Yerdos', 'Tuleuov', 'Yerdos.Tuleuov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Askar.Ospanov@kcell.kz', 1, 'Askar', 'Ospanov', 'Askar.Ospanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Boris.Alevtsev@kcell.kz', 1, 'Boris', 'Alevtsev', 'Boris.Alevtsev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Azat.Kelgenbayev@kcell.kz', 1, 'Azat', 'Kelgenbayev', 'Azat.Kelgenbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Galymzhan.Kuanyshov@kcell.kz', 1, 'Galymzhan', 'Kuanyshov', 'Galymzhan.Kuanyshov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yerbol.Tasenov@kcell.kz', 1, 'Yerbol', 'Tasenov', 'Yerbol.Tasenov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zaurbek.Bastarbekov@kcell.kz', 1, 'Zaurbek', 'Bastarbekov', 'Zaurbek.Bastarbekov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Olzhas.Sardarov@kcell.kz', 1, 'Olzhas', 'Sardarov', 'Olzhas.Sardarov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Daulet.Ziyashov@kcell.kz', 1, 'Daulet', 'Ziyashov', 'Daulet.Ziyashov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhassulan.Lukpanov@kcell.kz', 1, 'Zhassulan', 'Lukpanov', 'Zhassulan.Lukpanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Askhat.Shektibayev@kcell.kz', 1, 'Askhat', 'Shektibayev', 'Askhat.Shektibayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Mansur.Saidirassulov@kcell.kz', 1, 'Mansur', 'Saidirassulov', 'Mansur.Saidirassulov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Igor.Drantussov@kcell.kz', 1, 'Igor', 'Drantussov', 'Igor.Drantussov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Myktybek.Zulpukharov@kcell.kz', 1, 'Myktybek', 'Zulpukharov', 'Myktybek.Zulpukharov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Didar.Saparov@kcell.kz', 1, 'Didar', 'Saparov', 'Didar.Saparov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhandos.Shaldanbayev@kcell.kz', 1, 'Zhandos', 'Shaldanbayev', 'Zhandos.Shaldanbayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhenis.Sailau@kcell.kz', 1, 'Zhenis', 'Sailau', 'Zhenis.Sailau@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhandos.Usenov@kcell.kz', 1, 'Zhandos', 'Usenov', 'Zhandos.Usenov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Erden.Bektaubayev@kcell.kz', 1, 'Erden', 'Bektaubayev', 'Erden.Bektaubayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhomart.Uteev@kcell.kz', 1, 'Zhomart', 'Uteev', 'Zhomart.Uteev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Valikhan.Baizenov@kcell.kz', 1, 'Valikhan', 'Baizenov', 'Valikhan.Baizenov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Islam.Kosbatyrov@kcell.kz', 1, 'Islam', 'Kosbatyrov', 'Islam.Kosbatyrov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Maxet.Akzhigitov@kcell.kz', 1, 'Maxet', 'Akzhigitov', 'Maxet.Akzhigitov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aleksandr.Kryuchkov@kcell.kz', 1, 'Aleksandr', 'Kryuchkov', 'Aleksandr.Kryuchkov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Galym.Atambayev@kcell.kz', 1, 'Galym', 'Atambayev', 'Galym.Atambayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurken.Seish@kcell.kz', 1, 'Nurken', 'Seish', 'Nurken.Seish@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurlan.Tazhmukhanov@kcell.kz', 1, 'Nurlan', 'Tazhmukhanov', 'Nurlan.Tazhmukhanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Talgat.Galym@kcell.kz', 1, 'Talgat', 'Galym', 'Talgat.Galym@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Askhat.Altybayev@kcell.kz', 1, 'Askhat', 'Altybayev', 'Askhat.Altybayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kulsary.Tolemis@kcell.kz', 1, 'Kulsary', 'Tolemis', 'Kulsary.Tolemis@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Temir.Isayev@kcell.kz', 1, 'Temir', 'Isayev', 'Temir.Isayev@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yaroslav.Arabok@kcell.kz', 1, 'Yaroslav', 'Arabok', 'Yaroslav.Arabok@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Saken.Dochshanov@kcell.kz', 1, 'Saken', 'Dochshanov', 'Saken.Dochshanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Rustem.Kenzherakhmanov@kcell.kz', 1, 'Rustem', 'Kenzherakhmanov', 'Rustem.Kenzherakhmanov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kuralai.Tubekbayeva@kcell.kz', 1, 'Kuralai', 'Tubekbayeva', 'Kuralai.Tubekbayeva@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Ainamkoz.Karakulova@kcell.kz', 1, 'Ainamkoz', 'Karakulova', 'Ainamkoz.Karakulova@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Albert.Koshan@kcell.kz', 1, 'Albert', 'Koshan', 'Albert.Koshan@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Almas.Omarov@kcell.kz', 1, 'Almas', 'Omarov', 'Almas.Omarov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Amir.Zharmaganbetov@kcell.kz', 1, 'Amir', 'Zharmaganbetov', 'Amir.Zharmaganbetov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Arman.Koshekbayev@kcell.kz', 1, 'Arman', 'Koshekbayev', 'Arman.Koshekbayev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Bakytbek.Malikbayev@kcell.kz', 1, 'Bakytbek', 'Malikbayev', 'Bakytbek.Malikbayev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Daniyar.Gafiz@kcell.kz', 1, 'Daniyar', 'Gafiz', 'Daniyar.Gafiz@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Daniyar.Nakipov@kcell.kz', 1, 'Daniyar', 'Nakipov', 'Daniyar.Nakipov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Jaras.Sabyr@kcell.kz', 1, 'Jaras', 'Sabyr', 'Jaras.Sabyr@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kalmakhan.Tursynbek@kcell.kz', 1, 'Kalmakhan', 'Tursynbek', 'Kalmakhan.Tursynbek@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kanat.Yessimbekov@kcell.kz', 1, 'Kanat', 'Yessimbekov', 'Kanat.Yessimbekov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Kuandyk.Bekbayev@kcell.kz', 1, 'Kuandyk', 'Bekbayev', 'Kuandyk.Bekbayev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Marat.Abdin@kcell.kz', 1, 'Marat', 'Abdin', 'Marat.Abdin@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Mariya.Sanarova@kcell.kz', 1, 'Mariya', 'Sanarova', 'Mariya.Sanarova@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurlan.Sapargaliyev@kcell.kz', 1, 'Nurlan', 'Sapargaliyev', 'Nurlan.Sapargaliyev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Shayakhmet.Daliev@kcell.kz', 1, 'Shayakhmet', 'Daliev', 'Shayakhmet.Daliev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Talgat.Kaleev@kcell.kz', 1, 'Talgat', 'Kaleev', 'Talgat.Kaleev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Viktoriya.Khafizova@kcell.kz', 1, 'Viktoriya', 'Khafizova', 'Viktoriya.Khafizova@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yerkin.Issimbay@kcell.kz', 1, 'Yerkin', 'Issimbay', 'Yerkin.Issimbay@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Yessenzhol.Kzylov@kcell.kz', 1, 'Yessenzhol', 'Kzylov', 'Yessenzhol.Kzylov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Anastassiya.Tsoy@kcell.kz', 1, 'Anastassiya', 'Tsoy', 'Anastassiya.Tsoy@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Nurbek.Auez@kcell.kz', 1, 'Nurbek', 'Auez', 'Nurbek.Auez@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhanibek.Abylkassymov@kcell.kz', 1, 'Zhanibek', 'Abylkassymov', 'Zhanibek.Abylkassymov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aldiyar.Nassenov@kcell.kz', 1, 'Aldiyar', 'Nassenov', 'Aldiyar.Nassenov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Zhaslan.Umarov@kcell.kz', 1, 'Zhaslan', 'Umarov', 'Zhaslan.Umarov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Azamat.Abdikhalikov@kcell.kz', 1, 'Azamat', 'Abdikhalikov', 'Azamat.Abdikhalikov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Adlet.Zhukenov@kcell.kz', 1, 'Adlet', 'Zhukenov', 'Adlet.Zhukenov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Adilet.Yessaliyev@kcell.kz', 1, 'Adilet', 'Yessaliyev', 'Adilet.Yessaliyev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Aidos.Aituganov@kcell.kz', 1, 'Aidos', 'Aituganov', 'Aidos.Aituganov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Stanislav.fodchuk@kcell.kz', 1, 'Stanislav', 'Focdchuk', 'Stanislav.fodchuk@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Satzhan.Bekbolat@kcell.kz', 1, 'Satzhan', 'Bekbolat', 'Satzhan.Bekbolat@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Timur.Samarkhanov@kcell.kz', 1, 'Timur', 'Samarkhanov', 'Timur.Samarkhanov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;
INSERT INTO public.act_id_user(id_, rev_, first_, last_, email_, pwd_, salt_, picture_id_, lock_exp_time_, attempts_) VALUES ('Maxat.Pirnazarov@kcell.kz', 1, 'Maxat', 'Pirnazarov', 'Maxat.Pirnazarov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', null, null, null, null) ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_planning', 1, 'hq_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_rollout', 1, 'hq_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_planning', 1, 'alm_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_planning', 1, 'astana_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_planning', 1, 'nc_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_planning', 1, 'south_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_planning', 1, 'east_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_planning', 1, 'west_planning', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_power', 1, 'alm_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_power', 1, 'astana_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_power', 1, 'nc_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_power', 1, 'south_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_power', 1, 'east_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_power', 1, 'west_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_transmission', 1, 'alm_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_transmission', 1, 'astana_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_transmission', 1, 'nc_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_transmission', 1, 'south_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_transmission', 1, 'east_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_transmission', 1, 'west_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_leasing', 1, 'hq_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_leasing', 1, 'alm_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_leasing', 1, 'astana_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_leasing', 1, 'nc_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_leasing', 1, 'south_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_leasing', 1, 'east_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_leasing', 1, 'west_leasing', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_rollout', 1, 'alm_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('astana_rollout', 1, 'astana_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('nc_rollout', 1, 'nc_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('south_rollout', 1, 'south_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('east_rollout', 1, 'east_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('west_rollout', 1, 'west_rollout', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_power', 1, 'hq_power', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_transmission', 1, 'hq_transmission', 'WORKFLOW') ON CONFLICT(id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Kim@kcell.kz', 'hq_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Begaly.Kokin@kcell.kz', 'hq_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Shayakhmet.Daliev@kcell.kz', 'hq_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Dmitriy.Saidashev@kcell.kz', 'alm_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexey.Kolyagin@kcell.kz', 'alm_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anastassiya.Shenojak@kcell.kz', 'alm_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Michshenko@kcell.kz', 'alm_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Timur.Akylkhanov@kcell.kz', 'alm_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Oleg.Babkin@kcell.kz', 'astana_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhaxylyk.Moldakhmetov@kcell.kz', 'astana_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Farkhat.Baterikov@kcell.kz', 'astana_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Timur.Mukhamedyanov@kcell.kz', 'astana_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;


INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.Khvan@kcell.kz', 'nc_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Anastassiya.Tsoy@kcell.kz', 'nc_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Dastan.Kazhibayev@kcell.kz', 'nc_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Altynay.Ashken@kcell.kz', 'nc_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;


INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Azat.Kelgenbayev@kcell.kz', 'south_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Galymzhan.Kuanyshov@kcell.kz', 'south_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Satzhan.Bekbolat@kcell.kz', 'south_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerbol.Tasenov@kcell.kz', 'south_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zaurbek.Bastarbekov@kcell.kz', 'south_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kanat.Mussabekov@kcell.kz', 'east_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Cheslavskiy@kcell.kz', 'east_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Tatyana.Tsoy@kcell.kz', 'east_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maksat.Nauryzbay@kcell.kz', 'east_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aikyn.Bayanbayev@kcell.kz', 'east_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maxet.Akzhigitov@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Oleg.Yelayev@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurbol.Kalibekuly@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhassulan.Makhanbetov@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marta.Gabdrakhmanova@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerlan.Begimbayev@kcell.kz', 'west_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Yespayev@kcell.kz', 'hq_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yaroslav.Arabok@kcell.kz', 'hq_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Saken.Dochshanov@kcell.kz', 'hq_planning') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhanat.Seitkanov@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexey.Kolesnikov@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrei.Lugovoy@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Konstantin.Pominov@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yermek.Tanabekov@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yevgeniy.Elunin@kcell.kz', 'alm_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Bulat.Smagulov@kcell.kz', 'astana_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Memleket.Toleuuly@kcell.kz', 'astana_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Igor.Orlov@kcell.kz', 'nc_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Dulat.Kumekov@kcell.kz', 'nc_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Makhambet.Sandybayev@kcell.kz', 'nc_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhandos.Usenov@kcell.kz', 'south_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Myktybek.Zulpukharov@kcell.kz', 'south_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhandos.Shaldanbayev@kcell.kz', 'south_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Didar.Saparov@kcell.kz', 'south_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhenis.Sailau@kcell.kz', 'south_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustem.Kenzherakhmanov@kcell.kz', 'east_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maxat.Pirnazarov@kcell.kz', 'east_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Timur.Samarkhanov@kcell.kz', 'east_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askar.Pernebekov@kcell.kz', 'west_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Talgat.Galym@kcell.kz', 'west_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askhat.Altybayev@kcell.kz', 'west_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Temir.Isayev@kcell.kz', 'west_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kulsary.Tolemis@kcell.kz', 'west_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;


INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maulen.Kempirbayev@kcell.kz', 'alm_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Asset.Rashitov@kcell.kz', 'alm_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Yefanov@kcell.kz', 'alm_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Viktor.Maximenko@kcell.kz', 'astana_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Boris.Alevtsev@kcell.kz', 'astana_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marat.Abdin@kcell.kz', 'astana_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Pavel.Zaitsev@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Galat@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ilya.Nevpryaga2@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yuriy.Ivanov@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurbek.Auez@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerdos.Tuleuov@kcell.kz', 'nc_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Koval@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askhat.Shektibayev@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aziz.Kadyrkulov@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Igor.Drantussov@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mansur.Saidirassulov@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurbolat.Uteyev@kcell.kz', 'south_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Amangeldy.Kamerdenov@kcell.kz', 'east_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Baurzhan.Khamzin@kcell.kz', 'east_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Roman.Stupko@kcell.kz', 'east_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerbol.Tyshkambayev@kcell.kz', 'east_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhanibek.Abylkassymov@kcell.kz', 'east_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askar.Ospanov@kcell.kz', 'west_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aleksandr.Kryuchkov@kcell.kz', 'west_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurlan.Tazhmukhanov@kcell.kz', 'west_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurken.Seish@kcell.kz', 'west_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Galym.Atambayev@kcell.kz', 'west_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sara.Turabayeva@kcell.kz', 'hq_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Bella.Mamatova@kcell.kz', 'hq_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 'hq_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Mariya.Sanarova@kcell.kz', 'hq_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kuandyk.Bekbayev@kcell.kz', 'hq_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Adilet.Baishalov@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maxim.Goikolov@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Viktoriya.Khafizova@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aldiyar.Nassenov@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Ainamkoz.Karakulova@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Samat.Akhmetov@kcell.kz', 'alm_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kuralai.Tubekbayeva@kcell.kz', 'astana_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Almas.Omarov@kcell.kz', 'astana_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhaslan.Umarov@kcell.kz', 'astana_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Azamat.Mukhametalin@kcell.kz', 'nc_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aida.Ildebaeva@kcell.kz', 'nc_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Talgat.Kaleev@kcell.kz', 'nc_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yessenzhol.Kzylov@kcell.kz', 'nc_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Gafiz@kcell.kz', 'nc_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhandos.Usenov@kcell.kz', 'south_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kanat.Yessimbekov@kcell.kz', 'south_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Arman.Koshekbayev@kcell.kz', 'south_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kalmakhan.Tursynbek@kcell.kz', 'south_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Azamat.Abdikhalikov@kcell.kz', 'south_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rustem.Kenzherakhmanov@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Bakytbek.Malikbayev@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurlan.Sapargaliyev@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Jaras.Sabyr@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Adlet.Zhukenov@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Adilet.Yessaliyev@kcell.kz', 'east_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askar.Pernebekov@kcell.kz', 'west_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Yerkin.Issimbay@kcell.kz', 'west_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Amir.Zharmaganbetov@kcell.kz', 'west_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Albert.Koshan@kcell.kz', 'west_leasing') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kanat.Kulmukhambetov@kcell.kz', 'alm_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurzhan.Kochshigulov@kcell.kz', 'alm_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Degtyarev@kcell.kz', 'alm_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexandr.Uzhakin@kcell.kz', 'astana_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Rishad.Makirov@kcell.kz', 'astana_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kenzhekhan.Mussayev@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Nakipov@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Alexander.Buss@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Tsoy@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Valeriy.Zhilyaev@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Victor.Aredacov@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nurlan.Sumbaev@kcell.kz', 'nc_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daulet.Ziyashov@kcell.kz', 'south_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aidos.Aituganov@kcell.kz', 'south_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Stanislav.fodchuk@kcell.kz', 'south_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Olzhas.Sardarov@kcell.kz', 'south_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhassulan.Lukpanov@kcell.kz', 'south_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kanat.Mussabekov@kcell.kz', 'east_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marat.Bakezhanov@kcell.kz', 'east_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maxet.Akzhigitov@kcell.kz', 'west_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhomart.Uteev@kcell.kz', 'west_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Islam.Kosbatyrov@kcell.kz', 'west_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Erden.Bektaubayev@kcell.kz', 'west_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Valikhan.Baizenov@kcell.kz', 'west_rollout') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vassiliy.Gopkalo@kcell.kz', 'hq_power') ON CONFLICT (user_id_, group_id_) DO NOTHING;

INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Galym.Tulenbayev@kcell.kz', 'hq_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Nikolay.Ustinov@kcell.kz', 'hq_transmission') ON CONFLICT (user_id_, group_id_) DO NOTHING;
commit;
