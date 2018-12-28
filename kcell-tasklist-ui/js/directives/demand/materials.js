define(['./../module'], function(module){
    'use strict';
    module.directive('demandMaterials', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                editexisting: '=',
                purchaserGroup: "="
            },
            link: function(scope, el, attrs) {

                var setHeight = function() {
                    var element = el[0].querySelector('.materials-container');
                    element.style.height = 'auto';
                    element.style.height = (element.scrollHeight) + 'px';
                };

                $(document).bind('click', function (e) {
                    // if (el === e.target || el[0].contains(e.target))
                        $timeout(setHeight);
                });

                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        scope.countTotalSum();

                        if (!scope.userFIO) {
                            scope.userFIO = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name+ "/profile").then(function(result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.userFIO = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                scope.deleteItem = function(index) {
                    scope.data.splice(index, 1);
                    scope.countTotalSum();
                    $timeout(setHeight);
                };

                scope.addItem = function() {
                    scope.data.push({
                        cat1: null,
                        cat2: null,
                        cat3: null,
                        specification: null,
                        expert: null,
                        purchaseGroup: null,
                        quantity: null,
                        measure: null,
                        existing: null,
                        currency: null,
                        pprice: null,
                        summ: null
                    });
                };

                scope.calcSumm = function(index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].pprice;
                    scope.countTotalSum();
                };

                scope.countTotalSum = function() {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onPurchaseGroupChange = function(index, option) {
                    scope.data[index].purchaseGroup = option;
                };

                scope.onCat1Change = function(index, option) {
                    scope.data[index].cat1 = option;
                    scope.data[index].cat2 = null;
                    scope.data[index].cat3 = null;
                    scope.data[index].purchaser = null;
                  scope.data[index].expert = null;
                };

                scope.onCat2Change = function(index, option) {
                    scope.data[index].cat2 = option;
                    scope.data[index].cat3 = null;
                    scope.data[index].purchaser = option.purchaser;
                    scope.data[index].expert = option.expert;
                    setPurchaserGroupName(index, option.purchaser);
                    setExpertGroupName(index, option.expert);
                };

                scope.onCat3Change = function(index, option) {
                    scope.data[index].cat3 = option;
                };

                var setPurchaserGroupName = function (index, purchaser) {
                    if (!purchaser || !purchaser.groupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + purchaser.groupId).then(
                        function(result) {
                            if (result.data) scope.data[index].purchaser.groupName = result.data.name;
                        },
                        function(error) { toasty.error(error.data); }
                    );
                };

                scope.setPurchaser = function(index) {
                    scope.data[index].purchaser.id = $rootScope.authentication.name;
                    scope.data[index].purchaser.fio = scope.userFIO;
                    scope.calcSumm(index);
                };

                var setExpertGroupName = function (index, expert) {
                    if (!expert || !expert.groupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + expert.groupId).then(
                        function(result) {
                            if (result.data) scope.data[index].expert.groupName = result.data.name;
                        },
                        function(error) { toasty.error(error.data); }
                    );
                };

                scope.setExpert = function(index) {
                    scope.data[index].expert.id = $rootScope.authentication.name;
                    scope.data[index].expert.fio = scope.userFIO;
                    scope.calcSumm(index);
                };

                scope.options = {
                    purchaseGroup: [
                        {v: 100, t: "100 - Инфраструктура: Радио и Инфраструктура"},
                        {v: 110, t: "110 - Инфраструктура: Information Technology and Support Services"},
                        {v: 120, t: "120 - Инфраструктура: Коммутация и передача данных"},
                        {v: 130, t: "130 - Инфраструктура: Information Technology and Support Services"},
                        {v: 131, t: "131 - Операционный закуп"},
                        {v: 140, t: "140 - Продукты и услуги: Терминалы и SIM карты"},
                        {v: 150, t: "150 - Продукты и услуги: Маркетинг"},
                        {v: 170, t: "170 - Продукты и услуги: корпоративные продукты и услуги"}
                    ],
                  "cat1": [
                    {
                      "v": "DEVICES",
                      "cat2": [
                        {
                          "v": "ACCESSORIES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_accessories"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "AFTER SALES_DEVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_after_sales_devices"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MOBILE DATA COLLECTORS/TERMINALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_data_collectors_terminals"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MOBILE ROUTERS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_routers"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MPOS TERMINALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mpos_terminals"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SET-TO-BOX",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_set_to_box"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "MOBILE PHONES",
                      "cat2": [
                        {
                          "v": "AFTER SALES_MOBILE PHONES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_after_sales_mobile_phones"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MOBILE PHONES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_phones"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "INFRASTRUCTURE & CIVIL WORKS",
                      "cat2": [
                        {
                          "v": "AIR CONDITIONING",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_air_conditioning"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "AIR CONDITIONERS",
                            "AIR CONDITIONERS SPARE PARTS",
                            "AIR CONDITIONERS CONSUMABLES - FREON",
                            "AIR CONDITIONERS TOOLS",
                            "HEATERS"
                          ]
                        },
                        {
                          "v": "ANTIVANDAL CONSTRUCTION",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_antivandal_construction"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "CAMUFLAGE ",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_camuflage"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "CONTAINER",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_container"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FENCES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_fences"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "INFRASTRUCTURE CONSTRUCTION SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_infrastructure_construction_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "PROJECT SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_project_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TOWER",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tower"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TOWER SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tower_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "TOWER INFRASTRUCTURE SERVICES",
                            "TOWER MATERIALS - LAMP",
                            "TOWER MATERIALS INSTALLATION SERVICES",
                            "EXPERTIZE SERVICES"
                          ]
                        }
                      ]
                    },
                    {
                      "v": "POWER MATERIALS",
                      "cat2": [
                        {
                          "v": "ALTERNATIVE ENERGY SOURCES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "BATTERIES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_batteries"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "CIRCUIT BREAKER",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_circuit_breaker"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "COUNTERS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_counters"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "DC POWER SYSTEM",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dc_power_system"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "DC POWER SYSTEM INSTALLATION SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dc_power_system_installation_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "DG",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dg"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "DG INSTALLATION WORKS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "INVERTOR",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_invertor"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "OTHER POWER MATERIALS (SOCKETS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            " RECLOSER"
                          ]
                        },
                        {
                          "v": "STABILISATOR",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stabilisator"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "UPS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ups"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "IT",
                      "cat2": [
                        {
                          "v": "ANALYTICS/STATISTICS/BIG DATA",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_analytics_statistics_big_data"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "BI",
                            "NPS"
                          ]
                        },
                        {
                          "v": "DB LISENCES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_db_lisences"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "ERP",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_erp"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "HW",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "Workspace",
                            "Servers",
                            "Storages"
                          ]
                        },
                        {
                          "v": "INFORMATION SECURITY SYSTEM",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_information_security_system"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MOBILE FINACIAL SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_finacial_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "NFVI",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_nfvi"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "Virtualization layer",
                            "SDN"
                          ]
                        },
                        {
                          "v": "OMNI-CHANNEL (CONTACT CENTER)",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_omni_channel"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "IVR",
                            "CHATBOT"
                          ]
                        },
                        {
                          "v": "PLATFORMS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_platforms"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "WORKSPACE SW",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_workspace_sw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "RAN",
                      "cat2": [
                        {
                          "v": "ANTENNAS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_antennas"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "ANTENNAS COMPONENTS - SPLITTER",
                            "CABLE & ACCESSORIES",
                            "CONNECTORS",
                            "FEEDER"
                          ]
                        },
                        {
                          "v": "BSC/RNC HW",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_bsc_rnc_hw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "BSC/RNC SW (capacity licence",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_bsc_rnc_sw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            " features"
                          ]
                        },
                        {
                          "v": "NETWORK MEASUREMENT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_network_measurement"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "INSTALLATION&IMPLEMENTATION SERVICES",
                            "MEASUREMENT HW",
                            "MEASUREMENT TOOL HW&SW&LISENCES - DRIVE TESTS",
                            "MEASUREMENT TOOL UPGRADE - DRIVE TESTS",
                            "OTHER MEASUREMENT TOOLS"
                          ]
                        },
                        {
                          "v": "NETWORK PLANNING ",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_network_planning"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "DIGITAL MAPS",
                            "INSTALLATION&IMPLEMENTATION SERVICES",
                            "PLANNING TOOL HW&SW&LISENCES",
                            "PLANNING TOOL UPGRADE"
                          ]
                        },
                        {
                          "v": "OPTIMIZATION SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_optimization_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RBS HW",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_hw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RBS INSTAL MAT FROM VENDOR",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_instal_mat_from_vendor"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RBS SPARE PARTS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_spare_parts"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RBS SW",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_sw"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "ROLLOUT SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rollout_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "REVISION",
                            "ROLLOUT",
                            "BSC/RNC"
                          ]
                        }
                      ]
                    },
                    {
                      "v": "PRODUCTION",
                      "cat2": [
                        {
                          "v": "Audio record",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_audio_record"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Branding",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_branding"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Commercial Print",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_commercial_print"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Marketing production",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_marketing_production"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Photo Video production",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_photo_video_production"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Souvenirs",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_souvenirs"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "PROFESSIONAL SERVICES",
                      "cat2": [
                        {
                          "v": "AUDIT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_audit"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "CONSULTANCY",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_consultancy"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FINANCIAL SERVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_financial_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Insurances",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_insurances"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Research",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_research"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Stardardization & certification",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stardardization_certification"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TESTING SERVICES_OTHER",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_testing_services_other"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "B2B PRODUCTS",
                      "cat2": [
                        {
                          "v": "AUTOMONITORING",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_automonitoring"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "B2B products - COGS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_b2b_products_cogs"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "LI (COPM)",
                      "cat2": [
                        {
                          "v": "BILLING",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_billing"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "DATA",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_data"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SMS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sms"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "VOICE",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_voice"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "SUPPORT",
                      "cat2": [
                        {
                          "v": "BILLING",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_billing_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "CORE_SUPPORT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_core_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FIELD ",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_field_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "IT_SUPPORT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "LI (SORM)",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_li_sorm_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "OSS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_oss_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RAN",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ran_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SECURITY",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_securitys_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TR",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tr_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "VAS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_vas_support"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "CREATIVE",
                      "cat2": [
                        {
                          "v": "Brand and product creative service",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_brand_and_product_creative_service"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Web/Digital creative",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_web_digital_creative"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "MARKETING COMMUNICATION CHANNELS",
                      "cat2": [
                        {
                          "v": "BTL",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_btl"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Digital",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_digital"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "direct mail",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_direct_mail"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Newspapers/Magazines",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_newspapers_magazines"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "OOH",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ooh"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Radio",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_radio"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TV",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tv"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "REAL ESTATE& FACILITY MANAGEMENT",
                      "cat2": [
                        {
                          "v": "Building maintenance",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_building_maintenance"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Construction & repair",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_construction_repair"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Furniture",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_furniture"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Office supply",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_office_supply"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Shop supply",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_shop_supply"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Stationary",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stationary"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "INSTALLATION MATERIALS",
                      "cat2": [
                        {
                          "v": "CABLE MATERIALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cable_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "CABLE",
                            "CABLE LUG",
                            "CABLE TIE&STRIP"
                          ]
                        },
                        {
                          "v": "FEATURED INSTALLATION MATERIALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_featured_installation_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FIXING MATERIALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_fixing_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "HOSE & ACCESSORIES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hose_accessories"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "ISOLATION MATERIALS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_isolation_materials"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "LADDERS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ladders"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "LOCKS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_locks"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "PIPES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_pipes"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SENSORS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sensors"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TOOLS FOR CIVIL WORKS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tools_for_civil_works"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "FLEET MANAGEMENT",
                      "cat2": [
                        {
                          "v": "Car maitenance",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_car_maitenance"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Cars",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cars"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Spare parts",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_spare_parts"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "LOGISTICS",
                      "cat2": [
                        {
                          "v": "Courier services",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_courier_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Logistic services",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_logistic_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Transportation",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_transportation"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Warehousing",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_warehousing"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "BSS",
                      "cat2": [
                        {
                          "v": "CRM",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_crm"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TESTING_BSS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_testing_bss"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "OSS",
                      "cat2": [
                        {
                          "v": "CS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cs"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "IT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_oss"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "PS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ps"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "RAN",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ran"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "TN",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tn"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "CORE NETWORK",
                      "cat2": [
                        {
                          "v": "CS CORE",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cs_core"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "CUDB",
                            "MSS",
                            "MGW",
                            "IMS"
                          ]
                        },
                        {
                          "v": "PS CORE",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ps_core"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "EPC (MME",
                            "SGSN-GGSN ",
                            "PCRF",
                            "CGNAT/FIREWALL"
                          ]
                        }
                      ]
                    },
                    {
                      "v": "SECURITY",
                      "cat2": [
                        {
                          "v": "DEVICES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_devices"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FACILITY",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_facility"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "FINANCIAL  ",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_financial"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "IT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_security"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "PHYSICAL",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_physical"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "TRANSPORT NETWORK",
                      "cat2": [
                        {
                          "v": "DIGITAL CHANNELS RENT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_digital_channels_rent"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "INTERNET",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_internet"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "MICROWAVE",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_microwave"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "MW VENDOR HW",
                            "MW VENDOR SW"
                          ]
                        },
                        {
                          "v": "OPTIC NETWORK",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_optic_network"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "FOCL",
                            "PTN HW",
                            "OPTIC MATERIALS",
                            "DWDM"
                          ]
                        },
                        {
                          "v": "SATELLITE CHANNELS RENT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_satellite_channels_rent"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "EVENTS",
                      "cat2": [
                        {
                          "v": "Event activities ",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_event_activities"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Rent of premises and catering",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rent_of_premises_catering"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "TRAVEL",
                      "cat2": [
                        {
                          "v": "Hotel services",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hotel_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Travel agency services",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_travel_agency_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "HR",
                      "cat2": [
                        {
                          "v": "HR services",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hr_services"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Outstaff",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_outstaff"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Recruitment",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_recruitment"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Trainings",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_trainings"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "IP ROUTERS/SWITCHES",
                      "cat2": [
                        {
                          "v": "L2 SWITCHES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_l2_switches"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "L3 SWITCHES",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_l3_switches"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "ROUTERS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_routers"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": [
                            "DATA CENTRE",
                            "CORE NETWORK"
                          ]
                        }
                      ]
                    },
                    {
                      "v": "PR",
                      "cat2": [
                        {
                          "v": "PR service",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_pr_service"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "Sponsorship",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sponsorship"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "SIM& STRATCH CARDS",
                      "cat2": [
                        {
                          "v": "Scratch cards",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_scratch_cards"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SIM card packaging",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sim_card_packaging"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        },
                        {
                          "v": "SIM cards",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sim_cards"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "SW DEVELOPMENT",
                      "cat2": [
                        {
                          "v": "SW DEVELOPMENT",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sw_development"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    },
                    {
                      "v": "VAS",
                      "cat2": [
                        {
                          "v": "VAS",
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_vas"
                          },
                          "expert": {
                            "groupId": null
                          },
                          "cat3": []
                        }
                      ]
                    }
                  ]
                }
            },
            templateUrl: './js/directives/demand/materials.html'
        };
    });
});