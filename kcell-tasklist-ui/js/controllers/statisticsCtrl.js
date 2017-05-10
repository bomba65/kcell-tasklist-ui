define(['./module','jquery'], function(app,$){
	'use strict';
	return app.controller('statisticsCtrl', ['$scope', '$rootScope', function($scope, $rootScope) {
		$rootScope.currentPage = {
			name: 'statistics'
		};
		$scope.greetings = "Hello, Tair";
	}]);
});