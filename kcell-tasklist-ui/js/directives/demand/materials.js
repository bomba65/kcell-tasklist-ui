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
                editprice: '='
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
                    scope.countTotalSum();
                };

                scope.addItem = function() {
                    scope.data.push({
                        department: null,
                        materialType: null,
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
                    scope.setResponsible(index);
                    scope.data[index].materialType = null;
                    scope.data[index].description = null;
                };

                scope.onMaterialTypeChange = function(index) {
                    scope.setResponsible(index);
                    scope.data[index].description = null;
                };
	        },
			templateUrl: './js/directives/demand/materials.html'
		};
	});
});