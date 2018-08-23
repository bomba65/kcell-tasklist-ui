define(['./module'], function(module){
	'use strict';
	module.directive('infoPbx', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				pbxInfo: '='
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
			templateUrl: './js/directives/infoPbx.html'
		};
	}]);
});