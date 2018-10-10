define(['./../module'], function(module){
	'use strict';
	module.directive('demandBusinessIntelligence', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
				biname: '='
			},
			link: function(scope, element, attrs) {
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
                        // if (!scope.data.requestType) scope.data.requestType = 'Consulting';
                        // if (!scope.data.dataType) scope.data.dataType = 'Kcell';
                        // if (!scope.data.fromPeriod) scope.data.fromPeriod = new Date();
                        if (scope.data.fromPeriod) scope.data.fromPeriod = new Date(scope.data.fromPeriod);
                        // if (!scope.data.toPeriod) scope.data.toPeriod = new Date();
                        if(scope.data.toPeriod) scope.data.toPeriod = new Date(scope.data.toPeriod);
					}
				});
	        },
			templateUrl: './js/directives/demand/businessIntelligence.html'
		};
	});
});