define(['./../module'], function(module){
	'use strict';
	module.directive('sipProtocol', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				sip: '=',
				form: '=',
				view: '=',
				readonly: '='
			},
			link: function(scope, element, attrs) {
        		if(!scope.sip.vpnTuning) scope.sip.vpnTuning = "No";
	    	},
			templateUrl: './js/directives/PBX/sipProtocol.html'
		};
	});
});