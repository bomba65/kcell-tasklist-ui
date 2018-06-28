define(['./../module'], function(module){
	'use strict';
	module.directive('customerInformation', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				ci: '=',
				form: '=',
				view: '=',
				readonly: "=",
				legal: "="
			},
			link: function(scope, element, attrs) {
				scope.$watch('ci', function (value) {
					if (value) {
						if (!scope.ci.companyRegistrationDate) scope.ci.companyRegistrationDate = new Date();
						else scope.ci.companyRegistrationDate = new Date(scope.ci.companyRegistrationDate);

						if (scope.legal) {
							if (!scope.ci.termContract) scope.ci.termContract = new Date();
							else scope.ci.termContract = new Date(scope.ci.termContract);
						}
					}
				})
	    	},
			templateUrl: './js/directives/PBX/customerInformation.html'
		};
	});
});