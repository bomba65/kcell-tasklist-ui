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

                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                      if (!scope.collapsed || !(scope.collapsed instanceof Array)) scope.collapsed = [];
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

                scope.toggleCollapse = function(el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.collapsed[index] = !scope.collapsed[index];
                };

                scope.deleteItem = function(index) {
                    scope.data.splice(index, 1);
                    scope.collapsed.splice(index, 1);
                    scope.countTotalSum();
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
                    scope.collapsed.push(false);
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
                    option = scope.options.levels[scope.data[index].cat1][option];
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
                    "levels": {
                      "_list": [
                        "DEVICES",
                        "MOBILE PHONES",
                        "INFRASTRUCTURE & CIVIL WORKS",
                        "POWER MATERIALS",
                        "IT",
                        "RAN",
                        "PRODUCTION",
                        "PROFESSIONAL SERVICES",
                        "B2B PRODUCTS",
                        "LI (COPM)",
                        "SUPPORT",
                        "CREATIVE",
                        "MARKETING COMMUNICATION CHANNELS",
                        "REAL ESTATE& FACILITY MANAGEMENT",
                        "INSTALLATION MATERIALS",
                        "FLEET MANAGEMENT",
                        "LOGISTICS",
                        "BSS",
                        "OSS",
                        "CORE NETWORK",
                        "SECURITY",
                        "TRANSPORT NETWORK",
                        "EVENTS",
                        "TRAVEL",
                        "HR",
                        "IP ROUTERS/SWITCHES",
                        "PR",
                        "SIM& STRATCH CARDS",
                        "SW DEVELOPMENT",
                        "VAS"
                      ],
                      "DEVICES": {
                        "_list": [
                          "ACCESSORIES",
                          "AFTER SALES_DEVICES",
                          "MOBILE DATA COLLECTORS/TERMINALS",
                          "MOBILE ROUTERS",
                          "MPOS TERMINALS",
                          "SET-TO-BOX"
                        ],
                        "ACCESSORIES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_accessories"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "AFTER SALES_DEVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_after_sales_devices"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MOBILE DATA COLLECTORS/TERMINALS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_data_collectors_terminals"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MOBILE ROUTERS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_routers"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MPOS TERMINALS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mpos_terminals"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SET-TO-BOX": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_set_to_box"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "MOBILE PHONES": {
                        "_list": [
                          "AFTER SALES_MOBILE PHONES",
                          "MOBILE PHONES"
                        ],
                        "AFTER SALES_MOBILE PHONES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_after_sales_mobile_phones"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MOBILE PHONES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_phones"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "INFRASTRUCTURE & CIVIL WORKS": {
                        "_list": [
                          "AIR CONDITIONING",
                          "ANTIVANDAL CONSTRUCTION",
                          "CAMUFLAGE ",
                          "CONTAINER",
                          "FENCES",
                          "INFRASTRUCTURE CONSTRUCTION SERVICES",
                          "PROJECT SERVICES",
                          "TOWER",
                          "TOWER SERVICES"
                        ],
                        "AIR CONDITIONING": {
                          "_list": [
                            "AIR CONDITIONERS",
                            "AIR CONDITIONERS SPARE PARTS",
                            "AIR CONDITIONERS CONSUMABLES - FREON",
                            "AIR CONDITIONERS TOOLS",
                            "HEATERS"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_air_conditioning"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "ANTIVANDAL CONSTRUCTION": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_antivandal_construction"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "CAMUFLAGE ": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_camuflage"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "CONTAINER": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_container"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FENCES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_fences"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "INFRASTRUCTURE CONSTRUCTION SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_infrastructure_construction_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PROJECT SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_project_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TOWER": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tower"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TOWER SERVICES": {
                          "_list": [
                            "TOWER INFRASTRUCTURE SERVICES",
                            "TOWER MATERIALS - LAMP",
                            "TOWER MATERIALS INSTALLATION SERVICES",
                            "EXPERTIZE SERVICES"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tower_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "POWER MATERIALS": {
                        "_list": [
                          "ALTERNATIVE ENERGY SOURCES",
                          "BATTERIES",
                          "CIRCUIT BREAKER",
                          "COUNTERS",
                          "DC POWER SYSTEM",
                          "DC POWER SYSTEM INSTALLATION SERVICES",
                          "DG",
                          "DG INSTALLATION WORKS",
                          "INVERTOR",
                          "OTHER POWER MATERIALS (SOCKETS",
                          "STABILISATOR",
                          "UPS"
                        ],
                        "ALTERNATIVE ENERGY SOURCES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "BATTERIES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_batteries"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "CIRCUIT BREAKER": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_circuit_breaker"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "COUNTERS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_counters"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DC POWER SYSTEM": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dc_power_system"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DC POWER SYSTEM INSTALLATION SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dc_power_system_installation_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DG": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_dg"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DG INSTALLATION WORKS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "INVERTOR": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_invertor"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OTHER POWER MATERIALS (SOCKETS": {
                          "_list": [
                            " RECLOSER"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_other_power_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "STABILISATOR": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stabilisator"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "UPS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ups"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "IT": {
                        "_list": [
                          "ANALYTICS/STATISTICS/BIG DATA",
                          "DB LISENCES",
                          "ERP",
                          "HW",
                          "INFORMATION SECURITY SYSTEM",
                          "MOBILE FINACIAL SERVICES",
                          "NFVI",
                          "OMNI-CHANNEL (CONTACT CENTER)",
                          "PLATFORMS",
                          "WORKSPACE SW"
                        ],
                        "ANALYTICS/STATISTICS/BIG DATA": {
                          "_list": [
                            "BI",
                            "NPS"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_analytics_statistics_big_data"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DB LISENCES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_db_lisences"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "ERP": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_erp"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "HW": {
                          "_list": [
                            "Workspace",
                            "Servers",
                            "Storages"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "INFORMATION SECURITY SYSTEM": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_information_security_system"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MOBILE FINACIAL SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_mobile_finacial_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "NFVI": {
                          "_list": [
                            "Virtualization layer",
                            "SDN"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_nfvi"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OMNI-CHANNEL (CONTACT CENTER)": {
                          "_list": [
                            "IVR",
                            "CHATBOT"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_omni_channel"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PLATFORMS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_platforms"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "WORKSPACE SW": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_workspace_sw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "RAN": {
                        "_list": [
                          "ANTENNAS",
                          "BSC/RNC HW",
                          "BSC/RNC SW (capacity licence",
                          "NETWORK MEASUREMENT",
                          "NETWORK PLANNING ",
                          "OPTIMIZATION SERVICES",
                          "RBS HW",
                          "RBS INSTAL MAT FROM VENDOR",
                          "RBS SPARE PARTS",
                          "RBS SW",
                          "ROLLOUT SERVICES"
                        ],
                        "ANTENNAS": {
                          "_list": [
                            "ANTENNAS COMPONENTS - SPLITTER",
                            "CABLE & ACCESSORIES",
                            "CONNECTORS",
                            "FEEDER"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_antennas"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "BSC/RNC HW": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_bsc_rnc_hw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "BSC/RNC SW (capacity licence": {
                          "_list": [
                            " features"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_bsc_rnc_sw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "NETWORK MEASUREMENT": {
                          "_list": [
                            "INSTALLATION&IMPLEMENTATION SERVICES",
                            "MEASUREMENT HW",
                            "MEASUREMENT TOOL HW&SW&LISENCES - DRIVE TESTS",
                            "MEASUREMENT TOOL UPGRADE - DRIVE TESTS",
                            "OTHER MEASUREMENT TOOLS"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_network_measurement"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "NETWORK PLANNING ": {
                          "_list": [
                            "DIGITAL MAPS",
                            "INSTALLATION&IMPLEMENTATION SERVICES",
                            "PLANNING TOOL HW&SW&LISENCES",
                            "PLANNING TOOL UPGRADE"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_network_planning"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OPTIMIZATION SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_optimization_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RBS HW": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_hw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RBS INSTAL MAT FROM VENDOR": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_instal_mat_from_vendor"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RBS SPARE PARTS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_spare_parts"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RBS SW": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rbs_sw"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "ROLLOUT SERVICES": {
                          "_list": [
                            "REVISION",
                            "ROLLOUT",
                            "BSC/RNC"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rollout_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "PRODUCTION": {
                        "_list": [
                          "Audio record",
                          "Branding",
                          "Commercial Print",
                          "Marketing production",
                          "Photo Video production",
                          "Souvenirs"
                        ],
                        "Audio record": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_audio_record"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Branding": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_branding"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Commercial Print": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_commercial_print"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Marketing production": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_marketing_production"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Photo Video production": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_photo_video_production"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Souvenirs": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_souvenirs"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "PROFESSIONAL SERVICES": {
                        "_list": [
                          "AUDIT",
                          "CONSULTANCY",
                          "FINANCIAL SERVICES",
                          "Insurances",
                          "Research",
                          "Stardardization & certification",
                          "TESTING SERVICES_OTHER"
                        ],
                        "AUDIT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_audit"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "CONSULTANCY": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_consultancy"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FINANCIAL SERVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_financial_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Insurances": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_insurances"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Research": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_research"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Stardardization & certification": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stardardization_certification"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TESTING SERVICES_OTHER": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_testing_services_other"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "B2B PRODUCTS": {
                        "_list": [
                          "AUTOMONITORING",
                          "B2B products - COGS"
                        ],
                        "AUTOMONITORING": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_automonitoring"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "B2B products - COGS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_b2b_products_cogs"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "LI (COPM)": {
                        "_list": [
                          "BILLING",
                          "DATA",
                          "SMS",
                          "VOICE"
                        ],
                        "BILLING": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_billing"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "DATA": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_data"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SMS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sms"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "VOICE": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_voice"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "SUPPORT": {
                        "_list": [
                          "BILLING",
                          "CORE_SUPPORT",
                          "FIELD ",
                          "IT_SUPPORT",
                          "LI (SORM)",
                          "OSS",
                          "RAN",
                          "SECURITY",
                          "TR",
                          "VAS"
                        ],
                        "BILLING": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_billing_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "CORE_SUPPORT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_core_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FIELD ": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_field_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "IT_SUPPORT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "LI (SORM)": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_li_sorm_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OSS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_oss_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RAN": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ran_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SECURITY": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_securitys_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TR": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tr_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "VAS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_vas_support"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "CREATIVE": {
                        "_list": [
                          "Brand and product creative service",
                          "Web/Digital creative"
                        ],
                        "Brand and product creative service": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_brand_and_product_creative_service"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Web/Digital creative": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_web_digital_creative"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "MARKETING COMMUNICATION CHANNELS": {
                        "_list": [
                          "BTL",
                          "Digital",
                          "direct mail",
                          "Newspapers/Magazines",
                          "OOH",
                          "Radio",
                          "TV"
                        ],
                        "BTL": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_btl"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Digital": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_digital"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "direct mail": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_direct_mail"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Newspapers/Magazines": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_newspapers_magazines"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OOH": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ooh"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Radio": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_radio"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TV": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tv"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "REAL ESTATE& FACILITY MANAGEMENT": {
                        "_list": [
                          "Building maintenance",
                          "Construction & repair",
                          "Furniture",
                          "Office supply",
                          "Shop supply",
                          "Stationary"
                        ],
                        "Building maintenance": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_building_maintenance"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Construction & repair": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_construction_repair"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Furniture": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_furniture"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Office supply": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_office_supply"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Shop supply": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_shop_supply"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Stationary": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_stationary"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "INSTALLATION MATERIALS": {
                        "_list": [
                          "CABLE MATERIALS",
                          "FEATURED INSTALLATION MATERIALS",
                          "FIXING MATERIALS",
                          "HOSE & ACCESSORIES",
                          "ISOLATION MATERIALS",
                          "LADDERS",
                          "LOCKS",
                          "PIPES",
                          "SENSORS",
                          "TOOLS FOR CIVIL WORKS"
                        ],
                        "CABLE MATERIALS": {
                          "_list": [
                            "CABLE",
                            "CABLE LUG",
                            "CABLE TIE&STRIP"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cable_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FEATURED INSTALLATION MATERIALS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_featured_installation_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FIXING MATERIALS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_fixing_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "HOSE & ACCESSORIES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hose_accessories"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "ISOLATION MATERIALS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_isolation_materials"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "LADDERS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ladders"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "LOCKS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_locks"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PIPES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_pipes"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SENSORS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sensors"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TOOLS FOR CIVIL WORKS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tools_for_civil_works"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "FLEET MANAGEMENT": {
                        "_list": [
                          "Car maitenance",
                          "Cars",
                          "Spare parts"
                        ],
                        "Car maitenance": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_car_maitenance"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Cars": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cars"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Spare parts": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_spare_parts"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "LOGISTICS": {
                        "_list": [
                          "Courier services",
                          "Logistic services",
                          "Transportation",
                          "Warehousing"
                        ],
                        "Courier services": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_courier_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Logistic services": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_logistic_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Transportation": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_transportation"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Warehousing": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_warehousing"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "BSS": {
                        "_list": [
                          "CRM",
                          "TESTING_BSS"
                        ],
                        "CRM": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_crm"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TESTING_BSS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_testing_bss"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "OSS": {
                        "_list": [
                          "CS",
                          "IT",
                          "PS",
                          "RAN",
                          "TN"
                        ],
                        "CS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cs"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "IT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_oss"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ps"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "RAN": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ran"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "TN": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_tn"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "CORE NETWORK": {
                        "_list": [
                          "CS CORE",
                          "PS CORE"
                        ],
                        "CS CORE": {
                          "_list": [
                            "CUDB",
                            "MSS",
                            "MGW",
                            "IMS"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_cs_core"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PS CORE": {
                          "_list": [
                            "EPC (MME",
                            "SGSN-GGSN ",
                            "PCRF",
                            "CGNAT/FIREWALL"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_ps_core"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "SECURITY": {
                        "_list": [
                          "DEVICES",
                          "FACILITY",
                          "FINANCIAL  ",
                          "IT",
                          "PHYSICAL"
                        ],
                        "DEVICES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_devices"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FACILITY": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_facility"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "FINANCIAL  ": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_financial"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "IT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_it_security"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "PHYSICAL": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_physical"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "TRANSPORT NETWORK": {
                        "_list": [
                          "DIGITAL CHANNELS RENT",
                          "INTERNET",
                          "MICROWAVE",
                          "OPTIC NETWORK",
                          "SATELLITE CHANNELS RENT"
                        ],
                        "DIGITAL CHANNELS RENT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_digital_channels_rent"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "INTERNET": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_internet"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "MICROWAVE": {
                          "_list": [
                            "MW VENDOR HW",
                            "MW VENDOR SW"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_microwave"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "OPTIC NETWORK": {
                          "_list": [
                            "FOCL",
                            "PTN HW",
                            "OPTIC MATERIALS",
                            "DWDM"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_optic_network"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SATELLITE CHANNELS RENT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_satellite_channels_rent"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "EVENTS": {
                        "_list": [
                          "Event activities ",
                          "Rent of premises and catering"
                        ],
                        "Event activities ": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_event_activities"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Rent of premises and catering": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_rent_of_premises_catering"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "TRAVEL": {
                        "_list": [
                          "Hotel services",
                          "Travel agency services"
                        ],
                        "Hotel services": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hotel_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Travel agency services": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_travel_agency_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "HR": {
                        "_list": [
                          "HR services",
                          "Outstaff",
                          "Recruitment",
                          "Trainings"
                        ],
                        "HR services": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_hr_services"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Outstaff": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_outstaff"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Recruitment": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_recruitment"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Trainings": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_trainings"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "IP ROUTERS/SWITCHES": {
                        "_list": [
                          "L2 SWITCHES",
                          "L3 SWITCHES",
                          "ROUTERS"
                        ],
                        "L2 SWITCHES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_l2_switches"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "L3 SWITCHES": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_l3_switches"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "ROUTERS": {
                          "_list": [
                            "DATA CENTRE",
                            "CORE NETWORK"
                          ],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_routers"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "PR": {
                        "_list": [
                          "PR service",
                          "Sponsorship"
                        ],
                        "PR service": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_pr_service"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "Sponsorship": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sponsorship"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "SIM& STRATCH CARDS": {
                        "_list": [
                          "Scratch cards",
                          "SIM card packaging",
                          "SIM cards"
                        ],
                        "Scratch cards": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_scratch_cards"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SIM card packaging": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sim_card_packaging"
                          },
                          "expert": {
                            "groupId": null
                          }
                        },
                        "SIM cards": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sim_cards"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "SW DEVELOPMENT": {
                        "_list": [
                          "SW DEVELOPMENT"
                        ],
                        "SW DEVELOPMENT": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_sw_development"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      },
                      "VAS": {
                        "_list": [
                          "VAS"
                        ],
                        "VAS": {
                          "_list": [],
                          "purchaser": {
                            "groupId": "demand_supportive_cpd_l2_vas"
                          },
                          "expert": {
                            "groupId": null
                          }
                        }
                      }
                    }
                }
            },
            templateUrl: './js/directives/demand/materials.html'
        };
    });
});