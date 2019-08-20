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

commit;
