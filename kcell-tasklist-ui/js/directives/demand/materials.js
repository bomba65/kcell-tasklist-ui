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
                purchaserGroups: "="
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
                    cat1: [
                        {
                            v: "RAN",
                            cat2: [
                                {
                                    v: "ANTENNAS",
                                    purchaser: {
                                        groupId: "DEMAND_CPD_L2_ANTENNAS"
                                    },
                                    expert: {
                                        groupId: "DEMAND_CPD_L2_ANTENNAS"
                                    },
                                    cat3: [
                                        "ANTENNAS COMPONENTS - SPLITTER, MCM, CABLE",
                                        "FEEDER",
                                        "CONNECTORS",
                                        "CABLE & ACCESSORIES"
                                    ]
                                }
                            ]
                        },
                        {
                            v: "INSTALLATION MATERIALS",
                            cat2: [
                                {
                                    v: "CABLE MATERIALS",
                                    purchaser: {
                                        groupId: "DEMAND_CPD_L2_CABLE_MATERIALS"
                                    },
                                    expert: {
                                        groupId: "DEMAND_CPD_L2_CABLE_MATERIALS"
                                    },
                                    cat3: [
                                        "CABLE",
                                        "CABLE LUG",
                                        "CABLE TIE&STRIP"
                                    ]
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