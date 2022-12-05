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
					then(function(response) {
					  document.getElementById('fileDownloadIframe').src = response.data;
					}, function(error){
					  console.log(error.data);
					});
				}
			},
			templateUrl: './js/directives/uatProcess.html'
		};
	}]);
});