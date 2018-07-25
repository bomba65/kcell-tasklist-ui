define(['./../module'], function(module){
	'use strict';
	module.directive('demandBusinessCase', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
			},
			link: function(scope, element, attrs) {
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.revenues) scope.data.revenues = [];
						if (!scope.data.opexes) scope.data.opexes = [];
						if (!scope.data.capexes) scope.data.capexes = [];
					}
				});

				scope.addItem = function(name) {
					scope.data[name].push({});
				};
				scope.deleteItem = function(index, name) {
					scope.data[name].splice(index, 1);
				};
	        },
			templateUrl: './js/directives/demand/businessCase.html'
		};
	});
});