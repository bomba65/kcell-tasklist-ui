define(['./../module'], function(module){
	'use strict';
	module.directive('demandMaterials', function ($rootScope, $http) {
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
                        materialType: null,
                        description: null,
                        purchaseGroup: null,
                        quantity: null,
                        measure: null,
                        existing: null,
                        currency: null,
                        pprice: null,
                        summ: null
                    });
                }
                scope.calcSumm = function(index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].pprice;
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                }
	        },
			templateUrl: './js/directives/demand/materials.html'
		};
	});
});