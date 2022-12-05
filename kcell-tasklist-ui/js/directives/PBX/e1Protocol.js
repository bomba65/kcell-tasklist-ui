define(['./../module'], function(module){
	'use strict';
	module.directive('e1Protocol', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				e1: '=',
				form: '=',
				view: '=',
				readonly: '='
			},
			link: function(scope, element, attrs) {
	    },
			templateUrl: './js/directives/PBX/e1Protocol.html'
		};
	});
});