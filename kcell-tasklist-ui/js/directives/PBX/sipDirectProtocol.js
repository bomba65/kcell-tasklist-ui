define(['./../module'], function(module){
	'use strict';
	module.directive('sipDirectProtocol', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				dir: '=',
				form: '=',
				view: '=',
				readonly: '='
			},
			link: function(scope, element, attrs) {
        if (!scope.dir.signalingPort) scope.dir.signalingPort = '5060';
        if (!scope.dir.minChannelCapacity) scope.dir.minChannelCapacity = 0;
        scope.minChannelCapacityChanged = function() {
          scope.dir.minChannelCapacity = 0;
          if (scope.dir.preferredCoding && scope.dir.sessionCount) {
            scope.dir.minChannelCapacity = scope.dir.sessionCount * (scope.dir.preferredCoding=='g711'?87.2:32.2);
            scope.dir.minChannelCapacity = Math.floor(scope.dir.minChannelCapacity);
          }
        }
        scope.floor = function(x) {
          return Math.floor(x);
        }
	    },
			templateUrl: './js/directives/PBX/sipDirectProtocol.html'
		};
	});
});