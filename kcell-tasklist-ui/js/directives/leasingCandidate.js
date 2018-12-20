define(['./module'], function(module){
	'use strict';
	module.directive('leasingCandidate', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				leasingCandidate: '='
			},
			link: function(scope, el, attrs){
				scope.dictionary = {};
                $http.get('/api/leasingCatalogs').then(
                    function(result){
                        angular.extend(scope.dictionary, result.data);
                        scope.dictionary.legalTypeTitle = _.keyBy(scope.dictionary.legalType, 'id');
                    },
                    function(error){
                        console.log(error);
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
               	scope.selectIndex = function(index){
	                if(scope.selectedIndex == index){
	                    scope.selectedIndex = undefined;
	                } else {
	                    scope.selectedIndex = index;
	                }               		
               	}
			},
			templateUrl: './js/directives/leasingCandidate.html'
		};
	}]);
});
