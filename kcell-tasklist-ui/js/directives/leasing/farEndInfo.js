define(['./../module'], function(module){
	'use strict';
	module.directive('farEndInfo', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				farEnd: '='
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
			},
			templateUrl: './js/directives/leasing/farEndInfo.html'
		};
	}]);
});
