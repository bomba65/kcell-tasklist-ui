define(['./../module'], function(module){
	'use strict';
	module.directive('demandTargetAudience', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                readonly: '='
			},
			link: function(scope, element, attrs) {
				scope.audienceOptions = [
					{v: 'Technology'},
					{v: 'CCD'}
				];
				scope.roumingOptions = [
					{v: 'Almaty'},
					{v: 'Astana'},
					{v: 'X3'}
				];
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.audience) scope.data.audience = [];
						if (!scope.data.rouming) scope.data.rouming = [];
					}
				});
	        },
			templateUrl: './js/directives/demand/targetAudience.html'
		};
	});
});