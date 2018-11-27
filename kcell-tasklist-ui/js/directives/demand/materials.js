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
                purchaser: "="
            },
            link: function(scope, el, attrs) {

                var setHeight = function() {
                    var element = el[0].querySelector('.materials-container');
                    element.style.height = 'auto';
                    element.style.height = (element.scrollHeight) + 'px';
                };

                $(document).bind('click', function (e) {
                    if (el === e.target || el[0].contains(e.target))
                        $timeout(setHeight);
                });

                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        scope.countTotalSum();

                        if (!scope.responsible) {
                            scope.responsible = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name+ "/profile").then(function(result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.responsible = result.data.firstName + " " + result.data.lastName;
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
                        purchaseGroup: null,
                        quantity: null,
                        measure: null,
                        existing: null,
                        currency: null,
                        pprice: null,
                        summ: null,
                        responsible: null
                    });
                };

                scope.calcSumm = function(index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].pprice;
                    scope.countTotalSum();
                };

                scope.setResponsible = function(index) {
                    scope.data[index].responsible = {
                        name: $rootScope.authentication.name,
                        fio: scope.responsible
                    };
                    scope.calcSumm(index);
                };

                scope.countTotalSum = function() {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onPurchaseGroupChange = function(index, option) {
                    scope.setResponsible(index);
                    scope.data[index].purchaseGroup = option;
                };

                scope.onCat1Change = function(index, option) {
                    scope.data[index].cat1 = option;
                    scope.data[index].cat2 = null;
                    scope.data[index].cat3 = null;
                    scope.data[index].purchaser = null;
                };

                scope.onCat2Change = function(index, option) {
                    scope.data[index].cat2 = option;
                    scope.data[index].cat3 = null;
                    scope.data[index].purchaser = option.purchaser;
                    setPurchaserId(index, option.purchaser);
                };

                scope.onCat3Change = function(index, option) {
                    scope.data[index].cat3 = option;
                };

                var setPurchaserId = function (index, purchaser) {
                    if (!purchaser || !purchaser.firstName || !purchaser.lastName) return;
                    $http.get("/camunda/api/engine/engine/default/user?firstName=" + purchaser.firstName  + "&lastName=" + purchaser.lastName).then(
                        function(result) {
                            if (result.data && result.data[0].id) {
                                scope.data[index].purchaser.id = result.data[0].id;
                            }
                        },
                        function (error) { toasty.error(error.data); }
                    );
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
                                    v: "RBS HW",
                                    purchaser: {
                                        firstName: "Natalya",
                                        lastName: "Oleinik"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "RBS SW",
                                    purchaser: {
                                        firstName: "Natalya",
                                        lastName: "Oleinik"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "RBS INSTAL MAT FROM VENDOR",
                                    purchaser: {
                                        firstName: "Natalya",
                                        lastName: "Oleinik"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "RBS SPARE PARTS",
                                    purchaser: {
                                        firstName: "Natalya",
                                        lastName: "Oleinik"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "BSC/RNC HW",
                                    purchaser: {
                                        firstName: "Sanzhar",
                                        lastName: "Abdygapparov"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "BSC/RNC SW (capacity licence, features, HWAC)",
                                    purchaser: {
                                        firstName: "Sanzhar",
                                        lastName: "Abdygapparov"
                                    },
                                    cat3: []
                                },
                                {
                                    v: "ANTENNAS",
                                    purchaser: {
                                        firstName: "Demo",
                                        lastName: "Demo"
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
                                        firstName: "Abai",
                                        lastName: "Shapagatin"
                                    },
                                    cat3: [
                                        "CABLE",
                                        "CABLE LUG",
                                        "CABLE TIE&STRIP"
                                    ]
                                },
                                {v: "FEATURED INSTALLATION MATERIALS", purchaser: "", cat3: []},
                                {v: "PIPES", purchaser: "", cat3: []},
                                {v: "FIXING MATERIALS", purchaser: "", cat3: []},
                                {v: "ISOLATION MATERIALS", purchaser: "", cat3: []},
                                {v: "HOSE & ACCESSORIES", purchaser: "", cat3: []},
                                {v: "LADDERS", purchaser: "", cat3: []},
                                {v: "SENSORS", purchaser: "", cat3: []},
                                {v: "LOCKS", purchaser: "", cat3: []},
                                {v: "TOOLS FOR CIVIL WORKS", purchaser: "", cat3: []}
                            ]
                        }
                    ]
                }
            },
            templateUrl: './js/directives/demand/materials.html'
        };
    });
});