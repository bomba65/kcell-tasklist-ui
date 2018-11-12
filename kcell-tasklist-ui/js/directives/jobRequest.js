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
	                $http({method: 'GET', url: '/camunda/uploads/get/' + file.path, transformResponse: [] }).
	                then(function(response) {
	                    document.getElementById('fileDownloadIframe').src = response.data;
	                }, function(error){
	                    console.log(error);
	                });
               	};
	            scope.isFileVisible = function(file) {
	            	return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
	            }
				scope.getDictNameById = function(dictionary, id) {
					return _.find(dictionary, function(dict){
						return dict.id === id;
					});
				}
	        },
			templateUrl: './js/directives/jobRequest.html'
		};
	});
});