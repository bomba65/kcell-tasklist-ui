define(['./module'], function(module){
	'use strict';
	module.directive('invoiceDetail', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				invoice: '='
			},
			link: function(scope, element, attrs) {
	            scope.getCatalogs = function(file) {
	                $http.get('/api/catalogs').then(
                        function (result) {
                            scope.catalogs = result.data;
                        },
                        function (error) {
                            console.log(error.data);
                        }
	                );
	            }
	        },
			templateUrl: './js/directives/invoiceDetail.html'
		};
	});
});