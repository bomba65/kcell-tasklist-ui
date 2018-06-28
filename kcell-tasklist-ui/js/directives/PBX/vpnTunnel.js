define(['./../module'], function(module){
	'use strict';
	module.directive('vpnTunnel', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
			},
			link: function(scope, element, attrs) {
	    },
			templateUrl: './js/directives/PBX/vpnTunnel.html'
		};
	});
});