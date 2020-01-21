begin;
--USERS
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Evgeni.Serebryakov@kcell.kz', 1, 'Evgeni', 'Serebryakov', 'Evgeni.Serebryakov@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sholpan.Ongarova@kcell.kz', 1, 'Sholpan', 'Ongarova', 'Sholpan.Ongarova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Sasa.Lekovic@kcell.kz', 1, 'Sasa', 'Lekovic', 'Sasa.Lekovic@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('Saule.Khassenova@kcell.kz', 1, 'Saule', 'Khassenova', 'Saule.Khassenova@kcell.kz', '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);
INSERT INTO act_id_user (id_, rev_, first_, last_, email_, pwd_, picture_id_) VALUES ('LSE_Revision_HQ', 1, 'LSE_Revision', 'HQ', null, '{SHA}ieSV55Qc+eQOaYDRSha/AjzNTJE=', NULL);


--GROUPS
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_optimization_specialist', 1, 'hq_optimization_specialist', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_optimization_head', 1, 'hq_optimization_head', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_operation_specialist', 1, 'hq_operation_specialist', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_operation_head', 1, 'hq_operation_head', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_transmission_specialist', 1, 'hq_transmission_specialist', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_transmission_budget_supervisor', 1, 'hq_transmission_budget_supervisor', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_transmission_head', 1, 'hq_transmission_head', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_infrastructure_specialist', 1, 'hq_infrastructure_specialist', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_infrastructure_budget_supervisor', 1, 'hq_infrastructure_budget_supervisor', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_infrastructure_head', 1, 'hq_infrastructure_head', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_manager_ni', 1, 'hq_manager_ni', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_manager_sao', 1, 'hq_manager_sao', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_cto', 1, 'hq_cto', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_engineer_ma_approver', 1, 'alm_engineer_ma_approver', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_engineer_po_ma_approver', 1, 'alm_engineer_po_ma_approver', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_engineer_tr_ma_approver', 1, 'alm_engineer_tr_ma_approver', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_engineer_sfm_ma_approver', 1, 'alm_engineer_sfm_ma_approver', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_engineer_sao_ma_approver', 1, 'alm_engineer_sao_ma_approver', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_manager', 1, 'alm_manager', 'WORKFLOW');
INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('hq_contractor_lse', 1, 'hq_contractor_lse', 'WORKFLOW');
--INSERT INTO act_id_group (id_, rev_, name_, type_) VALUES ('alm_contractor_lse', 1, 'alm_contractor_lse', 'WORKFLOW');

--MEMBERSHIPS
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Imandosova@kcell.kz', 'hq_optimization_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Grachyov@kcell.kz', 'hq_optimization_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Marina.Paramonova@kcell.kz', 'hq_optimization_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Daniyar.Yespayev@kcell.kz', 'hq_optimization_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Grachyov@kcell.kz', 'hq_optimization_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Gulzhan.Imandosova@kcell.kz', 'hq_optimization_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Keremet.Ibragimova@kcell.kz', 'hq_operation_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Saule.Khassenova@kcell.kz', 'hq_operation_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kairat.Parmanov@kcell.kz', 'hq_operation_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Askar.Bekmurzayev@kcell.kz', 'hq_operation_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aigerim.Satybekova@kcell.kz', 'hq_transmission_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Bolat.Idirissov@kcell.kz', 'hq_transmission_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Lyudmila.Vilkova@kcell.kz', 'hq_transmission_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Georgiy.Kan@kcell.kz', 'hq_transmission_budget_supervisor');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Aigerim.Satybekova@kcell.kz', 'hq_transmission_budget_supervisor');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Galym.Tulenbayev@kcell.kz', 'hq_transmission_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Tatyana.Solovyova@kcell.kz', 'hq_infrastructure_specialist');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Andrey.Medvedev@kcell.kz', 'hq_infrastructure_budget_supervisor');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chumachenko@kcell.kz', 'hq_infrastructure_budget_supervisor');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeni.Serebryakov@kcell.kz', 'hq_infrastructure_head');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kirill.Strashenko@kcell.kz', 'hq_manager_ni');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sholpan.Ongarova@kcell.kz', 'hq_manager_sao');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sasa.Lekovic@kcell.kz', 'hq_cto');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Semenov@kcell.kz', 'alm_engineer_po_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chekh@kcell.kz', 'alm_engineer_po_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Asset.Rashitov@kcell.kz', 'alm_engineer_tr_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Vladimir.Yefanov@kcell.kz', 'alm_engineer_tr_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Shyngys.Kassabekov@kcell.kz', 'alm_engineer_tr_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Semenov@kcell.kz', 'alm_engineer_sfm_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chekh@kcell.kz', 'alm_engineer_sfm_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Evgeniy.Semenov@kcell.kz', 'alm_engineer_sao_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Sergey.Chekh@kcell.kz', 'alm_engineer_sao_ma_approver');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kerey.Zatilda@kcell.kz', 'alm_manager');
INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('LSE_Revision_HQ', 'hq_contractor_lse');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kerey.Zatilda@kcell.kz', 'alm_optimization_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kanat.Kulmukhambetov@kcell.kz', 'alm_optimization_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maulen.Kempirbayev@kcell.kz', 'alm_transmission_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kali.Esimbekov@kcell.kz', 'alm_infrastructure_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maulen.Kempirbayev@kcell.kz', 'alm_infrastructure_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Samat.Akhmetov@kcell.kz', 'alm_infrastructure_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhanat.Seitkanov@kcell.kz', 'alm_infrastructure_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Kali.Esimbekov@kcell.kz', 'alm_operation_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Maulen.Kempirbayev@kcell.kz', 'alm_operation_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Samat.Akhmetov@kcell.kz', 'alm_operation_head');
--INSERT INTO act_id_membership (user_id_, group_id_) VALUES ('Zhanat.Seitkanov@kcell.kz', 'alm_operation_head');


--INVOICE PROCESS AUTOHIRZATION

INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee99', 2, 1, 'east_engineer', null, 6, 'Invoice', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee98', 2, 1, 'west_engineer', null, 6, 'Invoice', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee97', 2, 1, 'south_engineer', null, 6, 'Invoice', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee96', 2, 1, 'nc_engineer', null, 6, 'Invoice', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee95', 2, 1, 'astana_engineer', null, 6, 'Invoice', 258);
INSERT INTO public.act_ru_authorization (id_, rev_, type_, group_id_, user_id_, resource_type_, resource_id_, perms_) VALUES ('64636c10-3c57-11ea-964a-322bdcb6ee94', 2, 1, 'alm_engineer', null, 6, 'Invoice', 258);

commit;
