define(['./../module'], function(module){
	'use strict';
	module.directive('replacementInfo', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                replacementInfo: '='
			},
			link: function(scope, element, attrs) {
				scope.catalogs = {};
				console.log(scope)
                $http.get($rootScope.getCatalogsHttpByName('dismantleCatalogs')).then(
                    function (result) {
                    	angular.extend(scope.catalogs, result.data);
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
				scope.download = function(path) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
	                then(function(response) {
	                    document.getElementById('fileDownloadIframe').src = response.data;
	                }, function(error){
	                    console.log(error.data);
	                });
               	};
			},
			templateUrl: './js/directives/dismantleReplace/replacementInfo.html'
		};
	});
});