define(['./module'], function(module){
	'use strict';
	module.directive('sharedSitePlan', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				planModel: '='
			},
			link: function(scope, el, attrs){
				scope.download = function(path){
					$http.get('/minio-client/presignedDownloadUrl?name=' + path).then(
						function(result){
							var win = window.open(result.data, '_blank');
  							win.focus();
						},
						function(error){

						}
					);
				}
			},
			templateUrl: './js/directives/sharedSitePlan.html'
		};
	}]);
});