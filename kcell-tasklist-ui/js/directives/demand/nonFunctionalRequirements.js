define(['./../module'], function(module){
	'use strict';
	module.directive('demandNonFuncRequirements', function ($rootScope, $http) {
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
                        if (!scope.data) scope.data = {};
                    }
                });
                scope.collapse = {
                    legalRequirements: false,
                    securityRequirements: false,
                    serviceAvailability: false,
                    other: false
                };
                scope.toggleCollapse = function(section) {
                    scope.collapse[section] = !scope.collapse[section];
                }
	        },
			templateUrl: './js/directives/demand/nonFunctionalRequirements.html'
		};
	});
});