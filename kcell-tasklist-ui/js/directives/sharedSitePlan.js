define(['./module'], function(module){
	'use strict';
	module.directive('sharedSitePlan', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				planModel: '=',
				planStatus: '='
			},
			link: function(scope, el, attrs){
				scope.download = function(path) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
	                success(function(data, status, headers, config) {
	                    document.getElementById('fileDownloadIframe').src = data;
	                }).
	                error (function(data, status, headers, config) {
	                    console.log(data);
	                });
               	};
			},
			templateUrl: './js/directives/sharedSitePlan.html'
		};
	}]);
});