define(['./module'], function(module){
	'use strict';
	return module.service('disconnectSelectedProcessService', ['$rootScope', function($rootScope) {
		return function (disconnect) {
			if(typeof disconnect !== "undefined"){
				$rootScope.disconnect = disconnect;
			}
			return $rootScope.disconnect;
	    };
	}]);
});