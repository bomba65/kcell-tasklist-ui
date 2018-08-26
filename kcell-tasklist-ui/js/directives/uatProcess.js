define(['./module'], function(module){
	'use strict';
	module.directive('uatProcess', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				uatModel: '=',
                print: '=',
                bk: '='
			},
			link: function(scope, el, attrs) {
				scope.download = function (path) {
					$http({method: 'GET', url: '/camunda/uploads/tmp/get/' + path, transformResponse: [] }).
					success(function(data, status, headers, config) {
					  document.getElementById('fileDownloadIframe').src = data;
					}).
					error (function(data, status, headers, config) {
					  console.log(data);
					});
				}
			},
			templateUrl: './js/directives/uatProcess.html'
		};
	}]);
});