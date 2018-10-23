define(['./../module'], function(module){
	'use strict';
	module.directive('demandTargetAudience', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
			},
			link: function(scope, element, attrs) {
				scope.multiselectSettings = {
					enableSearch: true,
					smartButtonMaxItems: 3,
					showCheckAll: false,
					showUncheckAll: false,
					displayProp: 'v',
					idProp: 'v',
					externalIdProp: 'v'
				};
				scope.audienceOptions = [
					{v: "B2B"},
					{v: "B2C"},
					{v: "CCD"},
					{v: "CEO"},
					{v: "CPD"},
					{v: "FD"},
					{v: "HR"},
					{v: "LD"},
					{v: "TD"}
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