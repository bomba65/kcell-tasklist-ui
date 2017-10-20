define(['./module'], function(module){
	'use strict';
	module.directive('jobRequest', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				jobModel: '=',
				processInstanceId: '=',
				taskId: '='
			},
			link: function(scope, element, attrs) {
	            scope.download = function(file) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + scope.processInstanceId + '/' + scope.taskId + '/' + file.value.name, transformResponse: [] }).
	                success(function(data, status, headers, config) {
	                    document.getElementById('fileDownloadIframe').src = data;
	                }).
	                error (function(data, status, headers, config) {
	                    console.log(data);
	                });
               	};
	            scope.isFileVisible = function(file) {
	            	return !file.value.visibility || file.value.visibility == 'all' || (file.value.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
	            }
	            scope.isVisible = function(visibility) {
	            	return !visibility || !visibility.value || visibility.value == 'all' ||  (visibility.value == 'kcell' && $rootScope.hasGroup('kcellUsers'));
	            }
	        },
			templateUrl: './js/directives/jobRequest.html'
		};
	});
});