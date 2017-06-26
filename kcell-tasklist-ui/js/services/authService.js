define(['./module'], function(module){
	'use strict';
	return module.service('AuthenticationService', [  '$rootScope', '$q', '$http', 'Uri', function($rootScope,   $q,   $http,   Uri) {
		function emit(event, a, b) {
			$rootScope.$broadcast(event, a, b);
		}
		function parse(response) {
			if (response.status !== 200) {
				return $q.reject(response);
			}
			var Authentication = function(data) {
				angular.extend(this, data);
			}
			var data = response.data;
			return new Authentication({
				name: data.userId,
				authorizedApps: data.authorizedApps
			});
		}
		function update(authentication) {
			$rootScope.authentication = authentication;
			$rootScope.authUser = authentication;
			emit('authentication.changed', authentication);
		}
		this.updateAuthentication = update;
		this.login = function(username, password) {
			var form = $.param({
				username: username,
				password: password
			});
			function success(authentication) {
				update(authentication);
				emit('authentication.login.success', authentication);
				return authentication;
			}
			function error(response) {
				emit('authentication.login.failure', response);
				return $q.reject(response);
			}
			return $http({
				method: 'POST',
				url: Uri.appUri('admin://auth/user/:engine/login/:appName'),
				data: form,
				headers: {
					'Content-Type': 'application/x-www-form-urlencoded'
				}
			}).then(parse).then(success, error);
		};
		this.logout = function() {
			function success(response) {
				update(null);
				emit('authentication.logout.success', response);
			}
			function error(response) {
				emit('authentication.logout.failure', response);
				return $q.reject(response);
			}
			return $http.post(Uri.appUri('admin://auth/user/:engine/logout')).then(success, error);
		};
		var authenticationPromise;
		$rootScope.$on('authentication.changed', function(e, authentication) {
			authenticationPromise = $q[authentication ? 'when' : 'reject'](authentication);
		});
		this.getAuthentication = function() {
			function success(authentication) {
				update(authentication);
				return authentication;
			}
			if (!authenticationPromise) {
				if ($rootScope.authentication) {
					authenticationPromise = $q.when($rootScope.authentication);
				} else {
					authenticationPromise = $http.get(Uri.appUri('admin://auth/user/:engine')).then(parse).then(success);
				}
			}
	    	return authenticationPromise;
		};
		$rootScope.$on('$routeChangeStart', function(event, next) {
			if (next.authentication) {
				if (!next.resolve) {
					next.resolve = {};
				}
				if (!next.resolve.authentication) {
					next.resolve.authentication = [ 'AuthenticationService', function(AuthenticationService) {
						return AuthenticationService.getAuthentication().catch(function(response) {
							if (next.authentication === 'optional') {
								return null;
							} else {
								emit('authentication.login.required', next);
								return $q.reject(response);
							}
						});
					}];
				}
			}
		});
	}]);
});