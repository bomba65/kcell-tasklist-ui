define(['./module'], function(module){
	'use strict';
	module.directive('sharedSitePlan', function () {
		return {
			restrict: 'E',
			scope: {
				planModel: '='
			},
			templateUrl: './js/directives/sharedSitePlan.html'
		};
	});
});