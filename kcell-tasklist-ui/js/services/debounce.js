define(['./module'], function(module){
	'use strict';
	return module.service('debounce', ['$timeout', function($timeout) {
		return function debounce(fn, wait) {
			var timer;
			var debounced = function() {
				var context = this, args = arguments;
				debounced.$loading = true;
				if (timer) {
					$timeout.cancel(timer);
				}
				timer = $timeout(function() {
					timer = null;
					debounced.$loading = false;
					fn.apply(context, args);
				}, wait);
			};
			return debounced;
	    };
	}]);
});