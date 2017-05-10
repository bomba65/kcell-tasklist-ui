define(['./module'], function(module){
	'use strict';
	module.directive('jobRequest', function () {
		return {
			restrict: 'E',
			scope: {
				jobModel: '='
			},
			templateUrl: './js/directives/jobRequest.html'
		};
	});
});