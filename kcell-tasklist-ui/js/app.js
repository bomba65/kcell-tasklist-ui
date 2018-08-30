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
	'camundaSDK',
	'big-js',
	'excellentexport',
	'angular-ui-router',
	'ng-file-upload',
	'angularjs-dropdown-multiselect',
	'ngSanitize'
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
		'ngCookies',
		'ui.router',
		'ngFileUpload',
		'angularjs-dropdown-multiselect',
		'ngSanitize'
	]);
	var preLoginUrl;
	var resolve = {
        authentication: ['AuthenticationService', '$q', function(AuthenticationService, $q) {
        	var defer = $q.defer();
            return AuthenticationService.getAuthentication().then(
            	function(res){
            		return defer.resolve(res);
            	},
            	function(err){
            		return defer.reject(err);
            	}
        	);
        }]
    }
	app.config(['$urlRouterProvider', '$httpProvider', '$stateProvider', function($urlRouterProvider, $httpProvider, $stateProvider){
		$httpProvider.interceptors.push('httpInterceptor');
		$urlRouterProvider.otherwise("/tasks");

		$stateProvider.state("tasks", {
			url: "/tasks",
			templateUrl: "js/partials/tasks.html",
			controller: "mainCtrl",
			authenticate: true,
		    resolve: resolve
	    }).state("tasks.task", {
			url: "/:id",
			templateUrl: "js/partials/task.html",
			controller: "TaskCtrl",
			authenticate: true,
		    resolve: resolve
	    }).state("login", {
	    	url: "/login",
	    	templateUrl: "js/partials/login.html",
	    	controller: "loginCtrl",
	    	authenticate: false
	    }).state("processes", {
	    	url: "/processes",
	    	templateUrl: "js/partials/processes.html",
	    	controller: "processesCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    }).state("statistics", {
			url: "/statistics?report&task&region&reason",
	    	templateUrl: "js/partials/statistics.html",
	    	controller: "statisticsCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    }).state("search", {
	    	url: "/search",
	    	templateUrl: "js/partials/search.html",
	    	controller: "searchCtrl",
	    	authenticate: true,
	    	resolve: resolve
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

	}]).run([ '$rootScope', '$location', 'AuthenticationService', '$q', '$state', function($rootScope, $location, AuthenticationService, $q, $state) {
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
	}]).factory('httpInterceptor', ['$q', '$rootScope', '$injector', '$location', function ($q, $rootScope, $injector, $location) {
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
				if(response.status === 401){
					var authentication = $injector.get('AuthenticationService');
					preLoginUrl = $location.url();
					authentication.logout();
				}
				return $q.reject(response);
			}
		};
	}]).config(function ($translateProvider) {
		$translateProvider.useSanitizeValueStrategy('escape');
		$translateProvider.preferredLanguage('en_EN');
		$translateProvider.useLoader('translateLoader');
	}).factory('translateLoader', function ($http, $q) {
		return function (options) {
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
	}).factory('exModal', function ($http, $controller, $q, $rootScope, $compile, $templateCache, $timeout) {
	    var service = {};
	    service.open = openModal;
	    return service;
	    function openModal(options) {
	        var modalResultDeferred = $q.defer();
	        $http.get(options.templateUrl, {
	            cache: $templateCache
	        }).then(function (result) {
	            var modalInstance = {
	                close: function (result) {
	                    modalResultDeferred.resolve(result);
	                    destroy();
	                },
	                dismiss: function (reason) {
	                    if (options.warnOnClose) {
	                        if (confirm('Вы уверены что хотите закрыть модальное окно?')) {
	                            modalResultDeferred.reject(reason);
	                            destroy();
	                        }
	                    } else {
	                        modalResultDeferred.reject(reason);
	                        destroy();
	                    }
	                },
	            };
	            var modalScope = $rootScope.$new();
	            if (options.scope) {
	                angular.forEach(options.scope, function (obj, key) {
	                    modalScope[key] = angular.copy(obj);
	                });
	            }
	            modalScope.$close = modalInstance.close;
	            modalScope.$dismiss = modalInstance.dismiss;
	            if (options.controller) {
	                $controller(options.controller, {
	                    $scope: modalScope,
	                    scope: modalScope,
	                    modalInstance: modalInstance
	                });
	            }
	            var html = result.data;
	            var modalWindow = $('<div class="modal fade ex-modal" style="display: block;" tabindex="-1"></div>');
	            var modalDialog = null;
	            if (options.myclass) {
	                modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content" style="background-color: rgb(250, 250, 250);"></div></div>');
	            } else {
	                modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content"></div></div>');
	            }

	            modalWindow.append(modalDialog);
	            var backdrop = $('<div class="modal-backdrop fade"></div>');
	            modalWindow.find('div[class="modal-content"]').html(html);
	            var compiled = $compile(modalWindow)(modalScope);
	            $('body').append(compiled).append(backdrop);
	            $timeout(function () {
	                modalWindow.addClass('in');
	                backdrop.addClass('in');
	                $('body').addClass('modal-open');
	                angular.element('.ex-modal').focus();
	                if (options.warnOnClose) {
	                    $(window).bind('beforeunload', function () {
	                        return 'Внимание';
	                    });
	                }
	            });
	            function destroy() {
	                modalWindow.removeClass('in');
	                backdrop.removeClass('in');
	                $('body').removeClass('modal-open');
	                $(window).unbind('beforeunload');

	                $timeout(function () {
	                    modalScope.$destroy();
	                    modalWindow.remove();
	                    backdrop.remove();
	                    compiled.remove();
	                }, 500);
	            }
	        });
	        return modalResultDeferred.promise;
	    }
	});
	return app;
});