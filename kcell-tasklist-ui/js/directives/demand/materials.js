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
                    "cat1":[
                      {
                        "v": "DEVICES",
                        "cat2": [
                          {
                            "v": "ACCESSORIES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_ACCESSORIES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "AFTER SALES_DEVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_AFTER_SALES_DEVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MOBILE DATA COLLECTORS/TERMINALS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MOBILE_DATA_COLLECTORS_TERMINALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MOBILE ROUTERS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MOBILE_ROUTERS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MPOS TERMINALS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MPOS_TERMINALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SET-TO-BOX",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SET_TO_BOX"
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
                              "groupId": "Demand_Supportive_CPD_L2_AFTER_SALES_MOBILE_PHONES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MOBILE PHONES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MOBILE_PHONES"
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
                              "groupId": "Demand_Supportive_CPD_L2_AIR_CONDITIONING"
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
                              "groupId": "Demand_Supportive_CPD_L2_ANTIVANDAL_CONSTRUCTION"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "CAMUFLAGE ",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CAMUFLAGE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "CONTAINER",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CONTAINER"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FENCES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FENCES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "INFRASTRUCTURE CONSTRUCTION SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_INFRASTRUCTURE_CONSTRUCTION_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "PROJECT SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_PROJECT_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TOWER",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TOWER"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TOWER SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TOWER_SERVICES"
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
                              "groupId": "Demand_Supportive_CPD_L2_OTHER_POWER_MATERIALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "BATTERIES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_BATTERIES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "CIRCUIT BREAKER",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CIRCUIT_BREAKER"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "COUNTERS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_COUNTERS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "DC POWER SYSTEM",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DC_POWER_SYSTEM"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "DC POWER SYSTEM INSTALLATION SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DC_POWER_SYSTEM_INSTALLATION_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "DG",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DG"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "DG INSTALLATION WORKS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OTHER_POWER_MATERIALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "INVERTOR",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_INVERTOR"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "\"OTHER POWER MATERIALS (SOCKETS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OTHER_POWER_MATERIALS"
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
                              "groupId": "Demand_Supportive_CPD_L2_STABILISATOR"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "UPS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_UPS"
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
                              "groupId": "Demand_Supportive_CPD_L2_ANALYTICS_STATISTICS_BIG_DATA"
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
                              "groupId": "Demand_Supportive_CPD_L2_DB_LISENCES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "ERP",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_ERP"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "HW",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_HW"
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
                              "groupId": "Demand_Supportive_CPD_L2_INFORMATION_SECURITY_SYSTEM"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MOBILE FINACIAL SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MOBILE_FINACIAL_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "NFVI",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_NFVI"
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
                              "groupId": "Demand_Supportive_CPD_L2_OMNI_CHANNEL"
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
                              "groupId": "Demand_Supportive_CPD_L2_PLATFORMS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "WORKSPACE SW",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_WORKSPACE_SW"
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
                              "groupId": "Demand_Supportive_CPD_L2_ANTENNAS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": [
                              "\"ANTENNAS COMPONENTS - SPLITTER",
                              "CABLE & ACCESSORIES",
                              "CONNECTORS",
                              "FEEDER"
                            ]
                          },
                          {
                            "v": "BSC/RNC HW",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_BSC_RNC_HW"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "\"BSC/RNC SW (capacity licence",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_BSC_RNC_SW"
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
                              "groupId": "Demand_Supportive_CPD_L2_NETWORK_MEASUREMENT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": [
                              "INSTALLATION&IMPLEMENTATION SERVICES",
                              "\"MEASUREMENT HW",
                              "MEASUREMENT TOOL HW&SW&LISENCES - DRIVE TESTS",
                              "MEASUREMENT TOOL UPGRADE - DRIVE TESTS",
                              "OTHER MEASUREMENT TOOLS"
                            ]
                          },
                          {
                            "v": "NETWORK PLANNING ",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_NETWORK_PLANNING"
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
                              "groupId": "Demand_Supportive_CPD_L2_OPTIMIZATION_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RBS HW",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RBS_HW"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RBS INSTAL MAT FROM VENDOR",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RBS_INSTAL_MAT_FROM_VENDOR"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RBS SPARE PARTS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RBS_SPARE_PARTS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RBS SW",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RBS_SW"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "ROLLOUT SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_ROLLOUT_SERVICES"
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
                              "groupId": "Demand_Supportive_CPD_L2_AUDIO_RECORD"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Branding",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_BRANDING"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Commercial Print",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_COMMERCIAL_PRINT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Marketing production",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MARKETING_PRODUCTION"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Photo Video production",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_PHOTO_VIDEO_PRODUCTION"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Souvenirs",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SOUVENIRS"
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
                              "groupId": "Demand_Supportive_CPD_L2_AUDIT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "CONSULTANCY",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CONSULTANCY"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FINANCIAL SERVICES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FINANCIAL_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Insurances",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_INSURANCES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Research",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RESEARCH"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Stardardization & certification",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_STARDARDIZATION_CERTIFICATION"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TESTING SERVICES_OTHER",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TESTING_SERVICES_OTHER"
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
                              "groupId": "Demand_Supportive_CPD_L2_AUTOMONITORING"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "B2B products - COGS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_B2B_PRODUCTS_COGS"
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
                              "groupId": "Demand_Supportive_CPD_L2_BILLING"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "DATA",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DATA"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SMS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SMS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "VOICE",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_VOICE"
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
                              "groupId": "Demand_Supportive_CPD_L2_BILLING_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "CORE_SUPPORT",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CORE_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FIELD ",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FIELD_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "IT_SUPPORT",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_IT_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "LI (SORM)",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_LI_SORM_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "OSS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OSS_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RAN",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RAN_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SECURITY",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SECURITYS_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TR",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TR_SUPPORT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "VAS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_VAS_SUPPORT"
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
                              "groupId": "Demand_Supportive_CPD_L2_BRAND_AND_PRODUCT_CREATIVE_SERVICE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Web/Digital creative",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_WEB_DIGITAL_CREATIVE"
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
                              "groupId": "Demand_Supportive_CPD_L2_BTL"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Digital",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DIGITAL"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "direct mail",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_DIRECT_MAIL"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Newspapers/Magazines",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_NEWSPAPERS_MAGAZINES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "OOH",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OOH"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Radio",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RADIO"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TV",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TV"
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
                              "groupId": "Demand_Supportive_CPD_L2_BUILDING_MAINTENANCE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Construction & repair",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CONSTRUCTION_REPAIR"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Furniture",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FURNITURE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Office supply",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OFFICE_SUPPLY"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Shop supply",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SHOP_SUPPLY"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Stationary",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_STATIONARY"
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
                              "groupId": "Demand_Supportive_CPD_L2_CABLE_MATERIALS"
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
                              "groupId": "Demand_Supportive_CPD_L2_FEATURED_INSTALLATION_MATERIALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FIXING MATERIALS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FIXING_MATERIALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "HOSE & ACCESSORIES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_HOSE_ACCESSORIES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "ISOLATION MATERIALS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_ISOLATION_MATERIALS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "LADDERS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_LADDERS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "LOCKS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_LOCKS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "PIPES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_PIPES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SENSORS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SENSORS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TOOLS FOR CIVIL WORKS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TOOLS_FOR_CIVIL_WORKS"
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
                              "groupId": "Demand_Supportive_CPD_L2_CAR_MAITENANCE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Cars",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_CARS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Spare parts",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SPARE_PARTS"
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
                              "groupId": "Demand_Supportive_CPD_L2_COURIER_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Logistic services",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_LOGISTIC_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Transportation",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TRANSPORTATION"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Warehousing",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_WAREHOUSING"
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
                              "groupId": "Demand_Supportive_CPD_L2_CRM"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TESTING_BSS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TESTING_BSS"
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
                              "groupId": "Demand_Supportive_CPD_L2_CS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "IT",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_IT_OSS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "PS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_PS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "RAN",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RAN"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "TN",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TN"
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
                              "groupId": "Demand_Supportive_CPD_L2_CS_CORE"
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
                              "groupId": "Demand_Supportive_CPD_L2_PS_CORE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": [
                              "\"EPC (MME",
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
                              "groupId": "Demand_Supportive_CPD_L2_DEVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FACILITY",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FACILITY"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "FINANCIAL  ",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_FINANCIAL"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "IT",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_IT_SECURITY"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "PHYSICAL",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_PHYSICAL"
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
                              "groupId": "Demand_Supportive_CPD_L2_DIGITAL_CHANNELS_RENT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "INTERNET",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_INTERNET"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "MICROWAVE",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_MICROWAVE"
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
                              "groupId": "Demand_Supportive_CPD_L2_OPTIC_NETWORK"
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
                              "groupId": "Demand_Supportive_CPD_L2_SATELLITE_CHANNELS_RENT"
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
                              "groupId": "Demand_Supportive_CPD_L2_EVENT_ACTIVITIES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Rent of premises and catering",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RENT_OF_PREMISES_CATERING"
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
                              "groupId": "Demand_Supportive_CPD_L2_HOTEL_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Travel agency services",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TRAVEL_AGENCY_SERVICES"
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
                              "groupId": "Demand_Supportive_CPD_L2_HR_SERVICES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Outstaff",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_OUTSTAFF"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Recruitment",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_RECRUITMENT"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Trainings",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_TRAININGS"
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
                              "groupId": "Demand_Supportive_CPD_L2_L2_SWITCHES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "L3 SWITCHES",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_L3_SWITCHES"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "ROUTERS",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_ROUTERS"
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
                              "groupId": "Demand_Supportive_CPD_L2_PR_SERVICE"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "Sponsorship",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SPONSORSHIP"
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
                              "groupId": "Demand_Supportive_CPD_L2_SCRATCH_CARDS"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SIM card packaging",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SIM_CARD_PACKAGING"
                            },
                            "expert": {
                              "groupId": null
                            },
                            "cat3": []
                          },
                          {
                            "v": "SIM cards",
                            "purchaser": {
                              "groupId": "Demand_Supportive_CPD_L2_SIM_CARDS"
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
                              "groupId": "Demand_Supportive_CPD_L2_SW_DEVELOPMENT"
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
                              "groupId": "Demand_Supportive_CPD_L2_VAS"
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