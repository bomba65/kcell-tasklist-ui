define(['./../module'], function(module){
	'use strict';
	module.directive('resolutionHistory', function ($http, $rootScope) {
		return {
			restrict: 'E',
			scope: {
				resolutions: '=',
				procDef: '='
			},
			link: function(scope, element, attrs) {				
				scope.download = function(path) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
	                then(function(response) {
	                    document.getElementById('fileDownloadIframe').src = response.data;
	                }, function(error){
	                    console.log(error);
	                });
               	}
               	scope.isFileVisible = function(file) {
	            	return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
	            }
	            scope.isKcellStaff = $rootScope.hasGroup('kcellUsers');
	        },
			templateUrl: './js/partials/resolutions.html'
		};
	});
});