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
						if (!scope.data.finFirstYear) scope.data.finFirstYear = (new Date()).getFullYear() + 1;
						initHeaders();
					}
				});

				var initHeaders = function() {
					var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

					scope.headers = {
						cashFlow1: [scope.data.finFirstYear],
						cashFlow1Swap: months,
						cashFlow2: [scope.data.finFirstYear + 1],
						cashFlow2Swap: months,
						accurals1: [scope.data.finFirstYear],
						accurals1Swap: months,
						accurals2: [scope.data.finFirstYear + 1],
						accurals2Swap: months
					};
				}

				scope.collapse = {
					cashFlow1: false,
					cashFlow2: false,
					accurals1: false,
					accurals2: false
				};

				scope.toggleCollapse = function(name) {
					scope.collapse[name] = !scope.collapse[name];
					var tmp = scope.headers[name];
					scope.headers[name] = scope.headers[name + 'Swap'];
					scope.headers[name + 'Swap'] = tmp;
				};

				scope.addItem = function(name) {
					scope.data[name].push({month1: {}, month2: {}});
				};
				scope.deleteItem = function(index, name) {
					scope.data[name].splice(index, 1);
				};
	        },
			templateUrl: './js/directives/demand/businessCase.html'
		};
	});
});