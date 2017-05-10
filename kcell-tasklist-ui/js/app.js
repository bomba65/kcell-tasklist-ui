define('app',[
	'angular',
	'toasty',
	'deep-diff',
	'translate',
	'angular-translate-storage-local',
	'angular-translate-storage-cookie',
	'angular-ui-bootstrap',
	'./controllers/index',
	'./directives/index',
	'./services/index',
	'camundaSDK'
], function(ng){
	'use strict';
	var app =  ng.module('app', [
		'app.controllers',
		'app.directives',
		'app.services',
		'cam.embedded.forms',
		'pascalprecht.translate',
		'ui.bootstrap',
		'angular-toasty',
		'ngRoute',
		'ngCookies'
	]);
	app.config(['$routeProvider', '$httpProvider', function($routeProvider, $httpProvider){
		$httpProvider.interceptors.push('httpInterceptor');
		$routeProvider.when('/', {
			controller : 'mainCtrl',
			templateUrl : 'js/partials/tasks.html',
			authentication: 'required',
    		reloadOnSearch: false
		});
		$routeProvider.when('/login', {
			templateUrl: 'js/partials/login.html',
			controller: 'loginCtrl'
		});
		$routeProvider.when('/statistics', {
			templateUrl: 'js/partials/statistics.html',
			controller: 'statisticsCtrl'
		});
		$routeProvider.otherwise({
			redirectTo: '/'
		});
	}]).provider('Uri',  function() {
		var TEMPLATES_PATTERN = /[\w]+:\/\/|:[\w]+/g;
		var replacements = {};
		this.replace = function(pattern, replacement) {
			replacements[pattern] = replacement;
		};
		this.replace('engine://', '/camunda/api/engine/');
		this.replace(':engine', 'default');
		this.replace('admin://', '/camunda/api/admin/');
		this.replace(':appName', 'tasklist');
		this.$get = [ '$injector', function($injector) {
			return {
				appUri: function appUri(str) {
					var replaced = str.replace(TEMPLATES_PATTERN, function(template) {
						var replacement = replacements[template];
						if (replacement === undefined) {
							return template;
						}
						if (angular.isFunction(replacement) || angular.isArray(replacement)) {
							replacement = $injector.invoke(replacement);
						}
						return replacement;
					});
					return replaced;
	      		}
	    	};
		}];
	}).run(['AuthenticationService', '$rootScope', function(AuthenticationService, $rootScope){

	}]).run([ '$rootScope', '$location', function($rootScope, $location) {
		var preLoginUrl;
		$rootScope.$on('authentication.login.required', function(event) {
			$rootScope.$evalAsync(function() {
				var url = $location.url();
				if (url === '/login' || event.defaultPrevented) {
					return;
				}
				preLoginUrl = url;
				$location.url('/login');
			});
		});
		$rootScope.$on('authentication.login.success', function(event) {
			$rootScope.$evalAsync(function() {
				if (!event.defaultPrevented) {
					$location.url(preLoginUrl || '/').replace();
					preLoginUrl = null;
				}
			});
		});
	}]).run(['$cacheFactory', '$rootScope', '$location', '$timeout', 'translateWithDefault', function($cacheFactory, $rootScope, $location, $timeout, translateWithDefault) {
		var translations = translateWithDefault({
			LOGOUT_SUCCESSFUL: 'Logout successful',
			LOGOUT_THANKS: 'Thank you for using Camunda today. Have a great',
			MORNING: 'morning',
			DAY: 'day',
			AFTERNOON: 'afternoon',
			EVENING: 'evening',
			NIGHT: 'night'
		});
		$rootScope.$on('authentication.logout.success', function(event) {
			$rootScope.$evalAsync(function() {
				if (!event.defaultPrevented) {
					$cacheFactory.get('$http').removeAll();
					$location.url('/login');
				}
			});
			$timeout(function() {
				var getDayContext = function() {
					var now = new Date();
					if(now.getDay() >= 5) {
						return 'weekend';
					} else {
						var hour = now.getHours();
						switch(true) {
							case (hour >= 4 && hour < 7): return 'MORNING';
							case (hour >= 7 && hour < 12): return 'DAY';
							case (hour >= 12 && hour < 17): return 'AFTERNOON';
							case (hour >= 17 && hour < 22): return 'EVENING';
							case (hour >= 22 || hour < 4): return 'NIGHT';
						}
					}
					return 'day';
				};
				translations.then(function(translations) {
					console.log(translations.LOGOUT_THANKS + ' ' + translations[getDayContext()] + '!');
				});
			});
		});
	}]).factory('search', [ '$location', '$rootScope', function($location, $rootScope) {

	  var silent = false;

	  $rootScope.$on('$routeUpdate', function(e, lastRoute) {
	    if (silent) {
	      silent = false;
	    } else {
	      $rootScope.$broadcast('$routeChanged', lastRoute);
	    }
	  });

	  $rootScope.$on('$routeChangeSuccess', function() {
	    silent = false;
	  });

	  var search = function() {
	    return $location.search.apply($location, arguments);
	  };

	  search.updateSilently = function(params, replaceFlag) {
	    var oldPath = $location.absUrl();

	    angular.forEach(params, function(value, key) {
	      $location.search(key, value);
	    });

	    var newPath = $location.absUrl();

	    if (newPath != oldPath) {
	      silent = true;
	    }

	    if(replaceFlag) {
	      $location.replace();
	    }
	  };

	  return search;
	}]).factory('httpInterceptor', function ($q) {
		var numLoadings = 0;
		return {
			request: function (httpInterceptorConfig) {
				numLoadings++;
				$('#loaderDiv').show();
				return httpInterceptorConfig || $q.when(httpInterceptorConfig);
			},
			response: function (response) {
				if ((--numLoadings) === 0) {
					$('#loaderDiv').hide();
				}
				return response || $q.when(response);
			},
			responseError: function (response) {
				if (!(--numLoadings)) {
					$('#loaderDiv').hide();
				}
				return $q.reject(response);
			}
		};
	}).config(function ($translateProvider) {
		$translateProvider.useSanitizeValueStrategy('escape');
		$translateProvider.preferredLanguage('en_EN');
		$translateProvider.useLoader('translateLoader');
	}).factory('translateLoader', function ($http, $q) {
		return function (options) {
			console.log(options);
			var deferred = $q.defer();
			$http.get('./resources/locale-' + options.key + '.json').then(
				function(httpStaticData){
					var result = {};
					for(var attr1 in httpStaticData.data){
						result[attr1] = httpStaticData.data[attr1];
					}
					deferred.resolve(result);
				},
				function (error){
					console.log(error.data);
				}
			);
			return deferred.promise;
		};
	});
	return app;
});