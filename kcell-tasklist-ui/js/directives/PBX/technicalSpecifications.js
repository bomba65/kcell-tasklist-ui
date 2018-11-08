define(['./../module'], function(module){
	'use strict';
	module.directive('technicalSpecifications', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				ts: '=',
				form: '=',
				view: '=',
				readonly: '='
			},
			link: function(scope, element, attrs) {
				if (!scope.ts.intenationalCallAccess) scope.ts.intenationalCallAccess = "No";
				if (!scope.ts.pbxType) scope.ts.pbxType = "Цифровая АТС";
	    },
			templateUrl: './js/directives/PBX/technicalSpecifications.html'
		};
	});
});