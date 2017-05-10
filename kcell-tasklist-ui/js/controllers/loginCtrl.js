define(['./module','jquery'], function(app,$){
	'use strict';
	return app.controller('loginCtrl', ['$scope', '$rootScope', 'AuthenticationService', '$location', 'translateWithDefault', function($scope, $rootScope, AuthenticationService, $location, translateWithDefault) {
		if ($rootScope.authentication) {
			return $location.path('/');
		}
		var loginErrorsTranslation = translateWithDefault({
			LOGIN_ERROR_MSG: 'Wrong credentials or missing access rights to application',
			LOGIN_FAILED: 'Login Failed'
		});
		$rootScope.showBreadcrumbs = false;
		var autofocusField = $('form[name="signinForm"] [autofocus]')[0];
		if (autofocusField) {
			autofocusField.focus();
		}
		$scope.login = function() {
			AuthenticationService.login($scope.username, $scope.password).then(function() {
			}).catch(function(error) {
				delete $scope.username;
				delete $scope.password;
				return loginErrorsTranslation.then(function(loginError) {
					console.log((error.data && error.data.message) || loginError.LOGIN_ERROR_MSG);
				});
			});
		};
	}]);
});