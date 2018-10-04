define(['./../module'], function(module){
	'use strict';
	module.directive('demandResources', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                showprice: '='
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
                        position: null,
                        description: null,
                        quantity: null,
                        labor: null,
                        rate: null,
                        existing: null,
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
                    if (!scope.data[index].labor) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].labor * scope.data[index].pprice;
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
                    scope.data[index].position = null;
                    scope.data[index].description = null;
                };

                scope.onPositionChange = function(index) {
                    scope.setResponsible(index);
                    scope.data[index].description = null;
                };
	        },
			templateUrl: './js/directives/demand/resources.html'
		};
	});
});