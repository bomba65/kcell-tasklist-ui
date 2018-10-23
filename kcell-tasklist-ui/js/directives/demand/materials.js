define(['./../module'], function(module){
	'use strict';
	module.directive('demandMaterials', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                editexisting: '='
			},
			link: function(scope, element, attrs) {
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
                    scope.isOpen.splice(index, 1);
                    scope.countTotalSum();
                };

                scope.addItem = function() {
                    scope.data.push({
                        department: null,
                        materialType: {},
                        description: null,
                        purchaseGroup: null,
                        quantity: null,
                        measure: null,
                        existing: null,
                        currency: null,
                        pprice: null,
                        summ: null,
                        responsible: {
                            name: $rootScope.authentication.name,
                            fio: scope.responsible
                        }
                    });

                    scope.isOpen.push(false);
                };

                scope.calcSumm = function(index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].pprice;
                    scope.countTotalSum();
                    scope.setResponsible(index);
                };

                scope.setResponsible = function(index) {
                    scope.data[index].responsible = {
                        name: $rootScope.authentication.name,
                        fio: scope.responsible
                    };
                };

                scope.countTotalSum = function() {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onDepartmentChange = function(index) {
                    scope.data[index].materialType = {};
                    scope.onMaterialTypeChange(index);
                };

                scope.onMaterialTypeChange = function(index) {
                    scope.setResponsible(index);
                    scope.data[index].description = null;
                };

                scope.toggleSelect = function(index) {
                    scope.isOpen[index] = !scope.isOpen[index];
                };

                scope.selectOption = function(index, option) {
                    scope.data[index].purchaseGroup = option;
                    scope.toggleSelect(index);
                    scope.setResponsible(index);
                };

                scope.isOpen = [];

                scope.multiselectSettings = {
                    enableSearch: true,
                    smartButtonMaxItems: 1,
                    selectionLimit: 1,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v'
                };
                scope.multiselectEvents = {
                    onItemSelect: function(item) {
                        for (var i = 0; i < scope.data.length; i++)
                            if (scope.data[i].materialType.v == item.v)
                                scope.onMaterialTypeChange(i);
                    }
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
                    materialType: [
                        {v: "Server"},
                        {v: "Platform"},
                        {v: "License and Software"},
                        {v: "Equipment (parts)"},
                        {v: "New Equipment"}
                    ]
                }
	        },
			templateUrl: './js/directives/demand/materials.html'
		};
	});
});