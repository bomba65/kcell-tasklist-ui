define(['./module'], function(module){
	'use strict';
	return module.service('translateWithDefault', ['$translate', '$q', function($translate, $q) {
		return function(translationObject) {
			var promises = Object.keys(translationObject).reduce(function(promises, key) {
				promises[key] = translateKey(key);
				return promises;
			}, {});
	   		return $q.all(promises);
	    	function translateKey(key) {
				return $translate(key).catch(function() {
					return translationObject[key];
				});
			}
		};
	}]);
});