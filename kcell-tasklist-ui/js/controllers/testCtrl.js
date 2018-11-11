define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('testCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', 
		function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
		
			if ($scope.authUser.id !== 'demo') {
				console.log('this is demo user');
				$scope.goHome();
			};

			$rootScope.currentPage = {
				name: 'test'
			};

			$scope.goHome = function(path) {
			    $location.path("/");
			};
	}]);
});