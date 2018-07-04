define(['./../module'], function(module){
	'use strict';
	module.directive('demandResources', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                readonly: '='
			},
			link: function(scope, element, attrs) {
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        scope.totalSumm = 0;
                        for (var d of scope.data) {
                            scope.totalSumm += d.summ;
                        }
                    }
                });
                scope.deleteItem = function(index) {
                    scope.data.splice(index, 1);
                }
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
                        summ: null
                    });
                }
                scope.calcSumm = function(index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].labor) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].labor * scope.data[index].pprice;
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                }
	        },
			templateUrl: './js/directives/demand/resources.html'
		};
	});
});