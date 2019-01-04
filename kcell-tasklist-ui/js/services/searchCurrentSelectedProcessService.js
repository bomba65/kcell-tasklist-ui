define(['./module'], function(module){
	'use strict';
	return module.service('SearchCurrentSelectedProcessService', ['$rootScope', function($rootScope) {
		return function (currentProcessInstance) {
			if(currentProcessInstance && Object.keys(currentProcessInstance).length > 0){
				$rootScope.currentProcessInstance = currentProcessInstance;
			}
			return $rootScope.currentProcessInstance;
	    };
	}]);
});