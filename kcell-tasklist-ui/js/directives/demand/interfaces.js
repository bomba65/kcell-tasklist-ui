define(['./../module'], function(module){
	'use strict';
	module.directive('demandInterfaces', function ($rootScope, $http) {
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
                    UIRequirements: false,
                    CRIRequirements: false,
                    SIRequirements: false
                };
                scope.toggleCollapse = function(section) {
                    scope.collapse[section] = !scope.collapse[section];
                }
	        },
			templateUrl: './js/directives/demand/interfaces.html'
		};
	});
});