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

INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Alexandr.Kim@kcell.kz', 1, 'Alexandr', 'Kim', 'Alexandr.Kim@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Anastassiya.Tsoy@kcell.kz', 1, 'Anastassiya', 'Tsoy', 'Anastassiya.Tsoy@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Satzhan.Bekbolat@kcell.kz', 1, 'Satzhan', 'Bekbolat', 'Satzhan.Bekbolat@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Maxat.Pirnazarov@kcell.kz', 1, 'Maxat', 'Pirnazarov', 'Maxat.Pirnazarov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Timur.Samarkhanov@kcell.kz', 1, 'Timur', 'Samarkhanov', 'Timur.Samarkhanov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Nurbek.Auez@kcell.kz', 1, 'Nurbek', 'Auez', 'Nurbek.Auez@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Zhanibek.Abylkassymov@kcell.kz', 1, 'Zhanibek', 'Abylkassymov', 'Zhanibek.Abylkassymov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Aldiyar.Nassenov@kcell.kz', 1, 'Aldiyar', 'Nassenov', 'Aldiyar.Nassenov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Zhaslan.Umarov@kcell.kz', 1, 'Zhaslan', 'Umarov', 'Zhaslan.Umarov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Azamat.Abdikhalikov@kcell.kz', 1, 'Azamat', 'Abdikhalikov', 'Azamat.Abdikhalikov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Adlet.Zhukenov@kcell.kz', 1, 'Adlet', 'Zhukenov', 'Adlet.Zhukenov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Adilet.Yessaliyev@kcell.kz', 1, 'Adilet', 'Yessaliyev', 'Adilet.Yessaliyev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Aidos.Aituganov@kcell.kz', 1, 'Aidos', 'Aituganov', 'Aidos.Aituganov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Stanislav.fodchuk@kcell.kz', 1, 'Stanislav', 'Focdchuk', 'Stanislav.fodchuk@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Ainur.Beknazarova@kcell.kz', 1, 'Ainur', 'Beknazarova', 'Ainur.Beknazarova@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Bella.Mamatova@kcell.kz', 1, 'Bella', 'Mamatova', 'Bella.Mamatova@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Maxim.Goikolov@kcell.kz', 1, 'Maxim', 'Goikolov', 'Maxim.Goikolov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Samat.Akhmetov@kcell.kz', 1, 'Samat', 'Akhmetov', 'Samat.Akhmetov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sara.Turabayeva@kcell.kz', 1, 'Sara', 'Turabayeva', 'Sara.Turabayeva@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Adilet.Baishalov@kcell.kz', 1, 'Adilet', 'Baishalov', 'Adilet.Baishalov@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Ikhtiyar.Ibrayev@kcell.kz', 1, 'Ikhtiyar', 'Ibrayev', 'Ikhtiyar.Ibrayev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Aida.Ildebaeva@kcell.kz', 1, 'Aida', 'Ildebaeva', 'Aida.Ildebaeva@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Azamat.Mukhametalin@kcell.kz', 1, 'Azamat', 'Mukhametalin', 'Azamat.Mukhametalin@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Alexandr.Kim@kcell.kz', 1, 'Alexandr', 'Kim', 'Alexandr.Kim@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Begaly.Kokin@kcell.kz', 1, 'Begaly', 'Kokin', 'Begaly.Kokin@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Shayakhmet.Daliev@kcell.kz', 1, 'Shayakhmet', 'Daliev', 'Shayakhmet.Daliev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Dmitriy.Saidashev@kcell.kz', 1, 'Dmitriy', 'Saidashev', 'Dmitriy.Saidashev@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Alexey.Kolyagin@kcell.kz', 1, 'Alexey', 'Kolyagin', 'Alexey.Kolyagin@kcell.kz', '{SHA}fEqNCco3Yq9h5ZUglD3CZJT4lBs=', NULL) ON CONFLICT(id_) DO NOTHING;


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
