(function() {
	console.log('fklsdjflkdjf');

	var $ = angular.element;

	var camClient = new CamSDK.Client({
	  mock: false,
	  apiUri: '/engine-rest'
	});

	var taskService = new camClient.resource('task');
	var processDefinitionService = new camClient.resource('process-definition');

	var $formContainer = $('.column.right');

	var app = angular.module('kcellWF', ['cam.embedded.forms', 'pascalprecht.translate', 'ui.bootstrap', 'angular-toasty', 'ui.router']);

	app.provider('Uri',  function() {
		var TEMPLATES_PATTERN = /[\w]+:\/\/|:[\w]+/g;
		var replacements = {};
		this.replace = function(pattern, replacement) {
			replacements[pattern] = replacement;
		};
		this.replace('engine://', '/camunda/api/engine/');
		this.replace(':engine', 'default');
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
	});
	app.service('translateWithDefault', ['$translate', '$q', function($translate, $q) {
		return function(translationObject) {
			var promises = Object.keys(translationObject).reduce(function(promises, key) {
				promises[key] = translateKey(key);
				return promises;
			}, {});
	   		return $q.all(promises);
	    	function translateKey(key) {
				return $translate(key).catch(function() {
					return translationObject[key];
				});
			}
		};
	}]);
	app.factory('exModal', function ($http, $controller, $q, $rootScope, $compile, $templateCache, $timeout) {
		var service = {};
		service.open = openModal;
		return service;
		function openModal(options) {
			var modalResultDeferred = $q.defer();
			$http.get(options.templateUrl, {
				cache : $templateCache
			}).then(function(result) {
				var modalInstance = {
					close : function(result) {
						modalResultDeferred.resolve(result);
						destroy();
					},
					dismiss : function(reason) {
						if(options.warnOnClose){
							if (confirm('Вы уверены что хотите закрыть модальное окно?')) {
								modalResultDeferred.reject(reason);
								destroy();
							}
						} else{
							modalResultDeferred.reject(reason);
							destroy();
						}
					},
				};
				var modalScope = $rootScope.$new();
				if (options.scope) {
					angular.forEach(options.scope, function(obj, key) {
						modalScope[key] = angular.copy(obj);
					});
				}
				modalScope.$close = modalInstance.close;
				modalScope.$dismiss = modalInstance.dismiss;
				if (options.controller) {
					$controller(options.controller, {
						$scope : modalScope,
						scope : modalScope,
						modalInstance : modalInstance
					});
				}
				var html = result.data;
				var modalWindow = $('<div class="modal fade ex-modal" style="display: block;" tabindex="-1"></div>');
				var modalDialog = null;
				if(options.myclass){
					modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content" style="background-color: rgb(250, 250, 250);"></div></div>');
				} else {
					modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content"></div></div>');
				}

				modalWindow.append(modalDialog);
				var backdrop = $('<div class="modal-backdrop fade"></div>');
				modalWindow.find('div[class="modal-content"]').html(html);
				var compiled = $compile(modalWindow)(modalScope);
				$('body').append(compiled).append(backdrop);
				$timeout(function(){
					modalWindow.addClass('in');
					backdrop.addClass('in');
					$('body').addClass('modal-open');
					angular.element('.ex-modal').focus();
					if(options.warnOnClose){
						$(window).bind('beforeunload', function(){return 'Внимание';});
					}
				});
				function destroy() {
					modalWindow.removeClass('in');
					backdrop.removeClass('in');
					$('body').removeClass('modal-open');
					$(window).unbind('beforeunload');

					$timeout(function(){
						modalScope.$destroy();
						modalWindow.remove();
						backdrop.remove();
						compiled.remove();
					}, 500);
				}
			});
			return modalResultDeferred.promise;
		}
	})
	.directive('jobRequest', function () {
		return {
			restrict: 'E',
			scope: {
				jobModel: '='
			},
			templateUrl: '/customScripts/jobRequest.html'
		};
	})
	.directive('trackChange', function(){
		return {
			restrict: 'A',
			priority: 10000,
			link: function(scope, element, attrs, ctrl) {
				var paths = scope.myModelExpression.split('.')
	          				.reduce((a, b) => [...a, [...a[a.length - 1], b]], [[]])
							.map(e => e.join('.'))
							.reverse()
							.filter((e, i) => i && e.length);

				element.on('blur keyup change', function(){
					var [originalPath, original] = paths
								.map(e => [e, scope.$parent.$eval(e + "._original") || {}])
								.find(e => e[1]);
					if (original) {
						var fieldPath = scope.myModelExpression.slice(originalPath.length + 1);
						var originalValue = scope.$parent.$eval(fieldPath, original);
						if (originalValue === scope.myModelValue) {
							if(element.attr('type') === 'checkbox'){
								element.parent().removeClass('track-change-unequal');
							} else {
								element.removeClass("track-change-unequal");
							}
						} else {
							if(element.attr('type') === 'checkbox'){
								element.parent().addClass('track-change-unequal');
							} else {
								element.addClass("track-change-unequal");
							}
						}
					}
				});
			},
			require: 'ngModel',
			scope: {
				myModelExpression: '@ngModel',
				myModelValue: '=ngModel'
			},
			template: '{{myModelExpression}} = {{myModelValue}}'  
		}
	})
	.directive('requiredFile',function(){
		return {
			require:'ngModel',
			restrict: 'A',
			link: function(scope,el,attrs,ctrl){
				function updateValidity(fileRequired){
					var valid = (fileRequired && el.val() !== '') || (!fileRequired);
					ctrl.$setValidity('validFile', valid);
					ctrl.$setValidity('camVariableType', valid);
				}
				updateValidity(scope.$eval(attrs.requiredFile));
				scope.$watch(attrs.requiredFile, updateValidity);
				el.bind('change',function(){
					updateValidity(scope.$eval(attrs.requiredFile));
					scope.$apply(function(){
						ctrl.$setViewValue(el.val());
						ctrl.$render();
					});
				});
			}
		}
	})
	.service('AuthenticationService', [  '$rootScope', '$q', '$http', 'Uri', function($rootScope,   $q,   $http,   Uri) {
		function emit(event, a, b) {
			$rootScope.$broadcast(event, a, b);
		}
		function parse(response) {
			if (response.status !== 200) {
				return $q.reject(response);
			}
			var data = response.data;
			return new Authentication({
				name: data.userId,
				authorizedApps: data.authorizedApps
			});
		}
		function update(authentication) {
			$rootScope.authentication = authentication;
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
	}])
	app.run([ '$rootScope', '$location', function($rootScope, $location) {
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
	}])
	.run(['$cacheFactory', '$rootScope', '$location', '$timeout', 'translateWithDefault', function($cacheFactory, $rootScope, $location, $timeout, translateWithDefault) {
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
					Notifications.addMessage({
						status: translations.LOGOUT_SUCCESSFUL,
						message: translations.LOGOUT_THANKS + ' ' + translations[getDayContext()] + '!',
						exclusive: true
					});
				});
			});
		});
	}])
	.run(['$rootScope', 'translateWithDefault', function($rootScope, translateWithDefault) {
		var translations = translateWithDefault({
			FAILED_TO_DISPLAY_RESOURCE: 'Failed to display resource' ,
			AUTHENTICATION_FAILED: 'Authentication failed. Your session might have expired, you need to login.'
		});
		$rootScope.$on('authentication.login.required', function() {
			if (shouldDisplayAuthenticationError()) {
				translations.then(function(translations) {
					Notifications.addError({
						status: translations.FAILED_TO_DISPLAY_RESOURCE,
						message: translations.AUTHENTICATION_FAILED,
						http: true,
						exclusive: ['http']
					});
				});
			}
		});
	}])
	.run(['AuthenticationService', function(){

	}]);
	app.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', function($scope, $rootScope, toasty) {
		$scope.camForm = null;
		$scope.tryToOpen = {};
		$scope.starProcess = function(id){
			processDefinitionService.start({id:id}, function(err, results){
				$scope.tryToOpen = results;
				loadTasks();
			});
		}
		function loadTasks(e) {
			taskService.list({}, function(err, results) {
				if (err) {
					throw err;
				}
				$scope.$apply(function() {
					$scope.tasks = results._embedded.task;
					$scope.tasks.forEach(function(e){
						if(e.processInstanceId === $scope.tryToOpen.id){
							$formContainer.html('');
							var taskId = e.id;
							taskService.form(e.id, function(err, taskFormInfo) {
								var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
								$rootScope.authentication = {name:'demo'};
								new CamSDK.Form({
									client: camClient,
									formUrl: url,
									taskId: taskId,
									containerElement: $formContainer,
									done: addFormButton
								});
							});
						}
					});
				});
			});
		}

		function loadProcessDefinitions(e){
			processDefinitionService.list({latest:true, active:true, firstResult:0, maxResults:15}, function(err, results){
				if(err) {
					throw err;
				}
				$scope.$apply(function (){
					$scope.processDefinitions = results.items;
				});
			});
		}

		function addFormButton(err, camForm, evt) {
			if (err) {
				throw err;
			}
			var $submitBtn = $('<button type="submit">Complete</button>').click(function (e) {
				$scope.view = {
					submitted : true
				};
				if($scope.kcell_form.$valid){
					camForm.submit(function (err,a1,b1,c1,d3) {
						if (err) {
							toasty.error({title: "Could not complete task", msg: err});
							e.preventDefault();
							throw err;
						} else {
							$formContainer.html('');
							loadTasks();
						}
					});
				} else {
					toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
				}
			});
			camForm.formElement.append($submitBtn);
		}
		
		$scope.loadTaskForm = function($event) {
			var taskId = $($event.currentTarget).attr('data-task-id');
			$formContainer.html('');
			taskService.form(taskId, function(err, taskFormInfo) {
				var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
				$rootScope.authentication = {name:'demo'};
				new CamSDK.Form({
					client: camClient,
					formUrl: url,
					taskId: taskId,
					containerElement: $formContainer,
					done: addFormButton
				});
			});
		};
		loadTasks();
		loadProcessDefinitions();
	}]);

	app.config(function([$stateProvider, $urlRouterProvider, function($stateProvider, $urlRouterProvider){
		$urlRouterProvider.otherwise('/tasks');
		$stateProvider
		.state('tlpr', {
			controller : 'MainController',
			url : '/app',
			templateUrl : '/views/main.html'
		})
		.state('market', {
			controller : 'SignatureController',
			url : '/market/signature',
			templateUrl : '/views/market/signature.html'
		});
	}]);

	//angular.bootstrap(document, ['kcellWF']);
	angular.element(document).ready(function () {
		angular.bootstrap(document, ['kcellWF']);
	});

	console.log("run")

})();