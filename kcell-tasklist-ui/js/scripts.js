(function() {
	var $ = angular.element;

	var camClient = new CamSDK.Client({
	  mock: false,
	  apiUri: '/camunda/api/engine/'
	});

	var Authentication = function(data) {
	    angular.extend(this, data);
	  }

	var taskService = new camClient.resource('task');
	var userService = new camClient.resource('user');
	var filterService = new camClient.resource('filter');
	var processDefinitionService = new camClient.resource('process-definition');

	var app = angular.module('kcellWF', ['cam.embedded.forms', 'pascalprecht.translate', 'ui.bootstrap', 'angular-toasty', 'ngRoute']);

	app.provider('Uri',  function() {
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
	app.service('debounce', ['$timeout', function($timeout) {
		return function debounce(fn, wait) {
			var timer;
			var debounced = function() {
				var context = this, args = arguments;
				debounced.$loading = true;
				if (timer) {
					$timeout.cancel(timer);
				}
				timer = $timeout(function() {
					timer = null;
					debounced.$loading = false;
					fn.apply(context, args);
				}, wait);
			};
			return debounced;
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
					console.log(translations.LOGOUT_THANKS + ' ' + translations[getDayContext()] + '!');
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
			
		});
	}])
	.run(['AuthenticationService', '$rootScope', function(AuthenticationService, $rootScope){
	}]);
	app.controller('LoginController', ['$scope', '$rootScope', 'AuthenticationService', '$location', 'translateWithDefault', function($scope, $rootScope, AuthenticationService, $location, translateWithDefault) {
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
	}])
	app.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$routeParams', function($scope, $rootScope, toasty, AuthenticationService, $routeParams) {
		$scope.camForm = null;
		$scope.selectedTab = 'form';
		console.log($rootScope);
		$scope.selectTab = function(tab){
			$scope.selectedTab = tab;
			if(tab==='diagram'){
				$scope.getDiagram();
			}
		}
		userService.profile($rootScope.authentication.name, function(err, userProfile){
			if(err){
				throw err;
			}
			$rootScope.authUser = userProfile;
		});

		if($routeParams.task){
			$scope.tryToOpen = {
				id: $routeParams.task
			}
		}
		$scope.getDiagram = function(){
			$scope.diagram = {
				xml: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.7.2\">\n  <bpmn:error id=\"myException\" name=\"name of error\" errorCode=\"ERROR-OCCURED\" />\n  <bpmn:process id=\"Revision\" name=\"Revision\" isExecutable=\"true\">\n    <bpmn:subProcess id=\"SubProcess_0cwj4g6\" name=\"Revision Leasing Procedures\">\n      <bpmn:incoming>SequenceFlow_0jidb3c</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_17n684j</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0eg1f85</bpmn:outgoing>\n      <bpmn:endEvent id=\"EndEvent_08g7218\">\n        <bpmn:incoming>SequenceFlow_1i5ejbh</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:userTask id=\"UserTask_1uw9qzb\" name=\"Leasing status\" camunda:formKey=\"embedded:app:forms/revision/updateLeasingStatusByRegionalLeasing.html\" camunda:candidateUsers=\"Maxim.Goikolov@kcell.kz, maxim.goikolov@kcell.kz, Adilet.Baishalov@kcell.kz, Adilet.Baishalov@kcell.kz, Samat.Akhmetov@kcell.kz\">\n        <bpmn:documentation>Update Leasing status by Regional Leasing</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0k5dp8v</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0ur6mix</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0ur6mix\" sourceRef=\"UserTask_1uw9qzb\" targetRef=\"ExclusiveGateway_0x3smuu\" />\n      <bpmn:startEvent id=\"StartEvent_0tw26xn\">\n        <bpmn:outgoing>SequenceFlow_1lir1ve</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1lir1ve\" sourceRef=\"StartEvent_0tw26xn\" targetRef=\"ExclusiveGateway_19zbppn\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0x3smuu\">\n        <bpmn:incoming>SequenceFlow_0ur6mix</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_14draw4</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1i5ejbh</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1awdwuw</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1i5ejbh\" name=\"leasing done\" sourceRef=\"ExclusiveGateway_0x3smuu\" targetRef=\"EndEvent_08g7218\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${leasingCompletionTaskResult=='done'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1awdwuw\" name=\"leasing impossible\" sourceRef=\"ExclusiveGateway_0x3smuu\" targetRef=\"Task_18z71ln\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${leasingCompletionTaskResult=='impossible'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1ywl9mw\" name=\"\" sourceRef=\"Task_18z71ln\" targetRef=\"EndEvent_0zv5f6j\" />\n      <bpmn:sendTask id=\"Task_18z71ln\" name=\"Notify of impossibility of JR execution\">\n        <bpmn:extensionElements>\n          <camunda:connector>\n            <camunda:inputOutput>\n              <camunda:inputParameter name=\"to\">Maulen.Kempirbayev@kcell.kz, Alexey.Khudaev@kcell.kz, Askar.Slambekov@kcell.kz</camunda:inputParameter>\n              <camunda:inputParameter name=\"subject\">JR Execution Impossible</camunda:inputParameter>\n              <camunda:inputParameter name=\"html\">\n                <camunda:script scriptFormat=\"freemarker\" resource=\"LeasingImpossibleMessage.ftl\" />\n              </camunda:inputParameter>\n            </camunda:inputOutput>\n            <camunda:connectorId>mail-send</camunda:connectorId>\n          </camunda:connector>\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1awdwuw</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1ywl9mw</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_19zbppn\">\n        <bpmn:incoming>SequenceFlow_1lir1ve</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0k5dp8v</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1i0x6bl</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"Task_0euindd\" name=\"Leasing status\" camunda:formKey=\"embedded:app:forms/revision/updateLeasingStatusByCentralLeasing.html\" camunda:candidateUsers=\"Sara.Turabayeva@kcell.kz, Sara.Turabayeva@kcell.kz, Ainur.Beknazarova@kcell.kz, Ainur.Beknazarova@kcell.kz, Bella.Mamatova@kcell.kz, Bella.Mamatova@kcell.kz\">\n        <bpmn:documentation>Update Leasing status by Central Leasing</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_1i0x6bl</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_14draw4</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0k5dp8v\" name=\"works doesn&#39;t belong to KZT, Beeline, ODS\" sourceRef=\"ExclusiveGateway_19zbppn\" targetRef=\"UserTask_1uw9qzb\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${worksBelongsTo=='No'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1i0x6bl\" name=\"Â works belongs to KZT, Beeline, ODS\" sourceRef=\"ExclusiveGateway_19zbppn\" targetRef=\"Task_0euindd\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${worksBelongsTo=='Yes'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_14draw4\" sourceRef=\"Task_0euindd\" targetRef=\"ExclusiveGateway_0x3smuu\" />\n      <bpmn:intermediateThrowEvent id=\"EndEvent_0zv5f6j\">\n        <bpmn:incoming>SequenceFlow_1ywl9mw</bpmn:incoming>\n      </bpmn:intermediateThrowEvent>\n      <bpmn:association id=\"Association_1r45mq2\" sourceRef=\"UserTask_1uw9qzb\" targetRef=\"TextAnnotation_04juvza\" />\n    </bpmn:subProcess>\n    <bpmn:subProcess id=\"SubProcess_1okaxp7\" name=\"Revision Prepare and Send Permit Docs\">\n      <bpmn:incoming>SequenceFlow_1sfkegw</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_0jvqwqj</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0go8umh</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_0ib18ut\" name=\"Verfiy Works\" camunda:formKey=\"embedded:app:forms/revision/checkDocsAndApproveBySiteLeasing.html\" camunda:candidateUsers=\"Abai.Shapagatin@kcell.kz, Aidos.Kenzhebayev@kcell.kz, Alibek.Nurkassymov@kcell.kz, Anna.Martynova@kcell.kz, Arman.Utepov@kcell.kz, Makhabbat.Sasanova@kcell.kz, Ruslan.Tubekbayev@kcell.kz, Yerkebulan.Dauletbayev@kcell.kz, Zhandos.Bolatov@kcell.kz, Bahytzhan.Sandybayev@kcell.kz, Askar.Pernebekov@kcell.kz\">\n        <bpmn:documentation>verification of completed forms by the contractor and approval of leasing</bpmn:documentation>\n        <bpmn:outgoing>SequenceFlow_1mz65k1</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0t2jovj\">\n        <bpmn:incoming>SequenceFlow_1mz65k1</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_07r408i</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_00qbbhz</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"UserTask_0xsau1t\" name=\"Update Installation Permition\" camunda:formKey=\"embedded:app:forms/revision/updateApprovalStatusAndAttachApprovedDocs.html\" camunda:candidateUsers=\"Abai.Shapagatin@kcell.kz, Aidos.Kenzhebayev@kcell.kz, Alibek.Nurkassymov@kcell.kz, Anna.Martynova@kcell.kz, Arman.Utepov@kcell.kz, Makhabbat.Sasanova@kcell.kz, Ruslan.Tubekbayev@kcell.kz, Yerkebulan.Dauletbayev@kcell.kz, Zhandos.Bolatov@kcell.kz, Bahytzhan.Sandybayev@kcell.kz, Askar.Pernebekov@kcell.kz\">\n        <bpmn:documentation>Update Approval Status and Attach Approved Docs</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_07r408i</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_0d0znog</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1jobgdv</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mz65k1\" sourceRef=\"UserTask_0ib18ut\" targetRef=\"ExclusiveGateway_0t2jovj\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_07r408i\" name=\"approved\" sourceRef=\"ExclusiveGateway_0t2jovj\" targetRef=\"UserTask_0xsau1t\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkDocsAndApproveBySiteLeasingTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:startEvent id=\"StartEvent_14gn11t\">\n        <bpmn:outgoing>SequenceFlow_0d0znog</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0d0znog\" sourceRef=\"StartEvent_14gn11t\" targetRef=\"UserTask_0xsau1t\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1jx9hwo\">\n        <bpmn:incoming>SequenceFlow_1jobgdv</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_16pxtx8</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_10rgai8</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1jobgdv\" sourceRef=\"UserTask_0xsau1t\" targetRef=\"ExclusiveGateway_1jx9hwo\" />\n      <bpmn:endEvent id=\"EndEvent_18k9n2u\">\n        <bpmn:incoming>SequenceFlow_16pxtx8</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_16pxtx8\" name=\"approved\" sourceRef=\"ExclusiveGateway_1jx9hwo\" targetRef=\"EndEvent_18k9n2u\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${updateApprovalStatusAndAttachApprovedDocsTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_10rgai8\" name=\"not approved\" sourceRef=\"ExclusiveGateway_1jx9hwo\" targetRef=\"Task_09uy9fl\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${updateApprovalStatusAndAttachApprovedDocsTaskResult=='notApproved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_1lwuspp\">\n        <bpmn:incoming>SequenceFlow_1fnfk0m</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1fnfk0m\" sourceRef=\"Task_09uy9fl\" targetRef=\"EndEvent_1lwuspp\" />\n      <bpmn:endEvent id=\"EndEvent_0ze2bsp\">\n        <bpmn:incoming>SequenceFlow_00qbbhz</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_00qbbhz\" name=\"not approved, request corrections\" sourceRef=\"ExclusiveGateway_0t2jovj\" targetRef=\"EndEvent_0ze2bsp\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkDocsAndApproveBySiteLeasingTaskResult=='notApproved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sendTask id=\"Task_09uy9fl\" name=\"Alarm of Permission not Given for Revision JR by e-mail\">\n        <bpmn:extensionElements>\n          <camunda:connector>\n            <camunda:inputOutput>\n              <camunda:inputParameter name=\"to\">Nurzhan.Mynbayev@kcell.kz, Sergey.Grigor@kcell.kz, Andrey.Medvedev@kcell.kz, Askar.Slambekov@kcell.kz</camunda:inputParameter>\n              <camunda:inputParameter name=\"subject\">Permission not given for Revision JR</camunda:inputParameter>\n              <camunda:inputParameter name=\"html\">\n                <camunda:script scriptFormat=\"freemarker\" resource=\"PermissionNotGivenForJRMessage.ftl\" />\n              </camunda:inputParameter>\n            </camunda:inputOutput>\n            <camunda:connectorId>mail-send</camunda:connectorId>\n          </camunda:connector>\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_10rgai8</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1fnfk0m</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:association id=\"Association_0cg0efk\" sourceRef=\"UserTask_0xsau1t\" targetRef=\"TextAnnotation_0w7it18\" />\n      <bpmn:association id=\"Association_1dyvnoq\" sourceRef=\"Task_09uy9fl\" targetRef=\"TextAnnotation_1bmypeh\" />\n      <bpmn:textAnnotation id=\"TextAnnotation_1d1kzxj\">      <bpmn:text>Send Docs to Permit Departments</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:association id=\"Association_1e0wo8a\" sourceRef=\"UserTask_0xsau1t\" targetRef=\"TextAnnotation_1d1kzxj\" />\n      <bpmn:textAnnotation id=\"TextAnnotation_0xakxcy\">      <bpmn:text>ÑƒÐ²ÐµÐ´Ð¾Ð¼Ð»ÐµÐ½Ð¸Ðµ ÑƒÑ…Ð¾Ð´Ð¸Ñ‚ Ð²ÑÐµÐ¼ heads</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:association id=\"Association_1xvmvrq\" sourceRef=\"Task_09uy9fl\" targetRef=\"TextAnnotation_0xakxcy\" />\n    </bpmn:subProcess>\n    <bpmn:subProcess id=\"SubProcess_04dekrg\" name=\"Revision Material List Preparation\">\n      <bpmn:incoming>SequenceFlow_10nsvzs</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_1p8u37f</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_05vqen4</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_0b217tv</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_1951c3w</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_1wkrl5k\" name=\"Attach Material ListÂ \" camunda:formKey=\"embedded:app:forms/revision/contractorAttachesMaterialList.html\" camunda:assignee=\"Begaly.Kokin@kcell.kz\">\n        <bpmn:documentation>Contractor Attaches Material List</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0ycbsvn</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1ppjvpj</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1sjo7n5</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_0x5m8bx</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1uudu22</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:userTask id=\"UserTask_0syren9\" name=\"Approve Material List\" camunda:formKey=\"embedded:app:forms/revision/approveMaterialsListByContractorGroupsHead.html\">\n        <bpmn:documentation>Approve Materials List by Contractor Groups Head</bpmn:documentation>\n        <bpmn:outgoing>SequenceFlow_10gy0d3</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_05esr0v\">\n        <bpmn:incoming>SequenceFlow_10gy0d3</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0ycbsvn</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1nqm4yd</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1jwddwt</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"UserTask_12n8eyi\" name=\"Approve Material List\" camunda:formKey=\"embedded:app:forms/revision/approveMaterialsListByRegionGroupsHead.html\">\n        <bpmn:documentation>Approve Materials List by Region Groups Head</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.ApproveMaterialsListByRegionGroupsHeadAssignmentHandler\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1nqm4yd</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1y9us7r</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_12vbsjq</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_19vvor0</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1sqtpex\">\n        <bpmn:incoming>SequenceFlow_19vvor0</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1ppjvpj</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_0uczqhk</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1mrjxgx</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"UserTask_1n39kzy\" name=\"Approve Material List\" camunda:formKey=\"embedded:app:forms/revision/approveMaterialsListByCentralGroupsHead.html\">\n        <bpmn:documentation>Approve Materials List by Central Groups Head</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.ApproveMaterialsListByCentralGroupsHeadAssignmentHandler\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_0uczqhk</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1s0pti2</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0xdedru\">\n        <bpmn:incoming>SequenceFlow_1s0pti2</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1y9us7r</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_18hclu9</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1klmxot</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"UserTask_14yc5q6\" name=\"Upload TR\" camunda:formKey=\"embedded:app:forms/revision/bindTRToJRByContractor.html\" camunda:assignee=\"Begaly.Kokin@kcell.kz\">\n        <bpmn:documentation>Bind TR to JR by Contractor</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_18hclu9</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1cr5ksn</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1puv934</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0ycbsvn\" name=\"rejected\" sourceRef=\"ExclusiveGateway_05esr0v\" targetRef=\"UserTask_1wkrl5k\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByContractorGroupsHeadTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1ppjvpj\" name=\"rejected\" sourceRef=\"ExclusiveGateway_1sqtpex\" targetRef=\"UserTask_1wkrl5k\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByRegionGroupsHeadTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1uudu22\" sourceRef=\"UserTask_1wkrl5k\" targetRef=\"ExclusiveGateway_0cztiwt\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_10gy0d3\" sourceRef=\"UserTask_0syren9\" targetRef=\"ExclusiveGateway_05esr0v\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1nqm4yd\" name=\"approved\" sourceRef=\"ExclusiveGateway_05esr0v\" targetRef=\"UserTask_12n8eyi\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByContractorGroupsHeadTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1y9us7r\" name=\"rejected\" sourceRef=\"ExclusiveGateway_0xdedru\" targetRef=\"UserTask_12n8eyi\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByCentralGroupsHeadTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_19vvor0\" sourceRef=\"UserTask_12n8eyi\" targetRef=\"ExclusiveGateway_1sqtpex\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0uczqhk\" name=\"approved\" sourceRef=\"ExclusiveGateway_1sqtpex\" targetRef=\"UserTask_1n39kzy\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByRegionGroupsHeadTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1s0pti2\" sourceRef=\"UserTask_1n39kzy\" targetRef=\"ExclusiveGateway_0xdedru\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_18hclu9\" name=\"approved\" sourceRef=\"ExclusiveGateway_0xdedru\" targetRef=\"UserTask_14yc5q6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByCentralGroupsHeadTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:startEvent id=\"StartEvent_0tbvhus\">\n        <bpmn:outgoing>SequenceFlow_1sjo7n5</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1sjo7n5\" sourceRef=\"StartEvent_0tbvhus\" targetRef=\"UserTask_1wkrl5k\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0qmnnga\">\n        <bpmn:incoming>SequenceFlow_1puv934</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0x5m8bx</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1g2fm4f</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1puv934\" sourceRef=\"UserTask_14yc5q6\" targetRef=\"ExclusiveGateway_0qmnnga\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0x5m8bx\" name=\"required materials are not available in SAP Warehouse\" sourceRef=\"ExclusiveGateway_0qmnnga\" targetRef=\"UserTask_1wkrl5k\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${bindTRToJRByContractorTaskResult=='notAvailable'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_1nde4uy\">\n        <bpmn:incoming>SequenceFlow_1tbf21w</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1g2fm4f\" name=\"materials attached\" sourceRef=\"ExclusiveGateway_0qmnnga\" targetRef=\"Task_0jxwgbt\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${bindTRToJRByContractorTaskResult=='attached'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_1hxbqie\">\n        <bpmn:incoming>SequenceFlow_1jwddwt</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1mrjxgx</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1klmxot</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_087eljg</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1jwddwt\" name=\"modify works\" sourceRef=\"ExclusiveGateway_05esr0v\" targetRef=\"EndEvent_1hxbqie\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByContractorGroupsHeadTaskResult=='modify'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mrjxgx\" name=\"modify works\" sourceRef=\"ExclusiveGateway_1sqtpex\" targetRef=\"EndEvent_1hxbqie\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByRegionGroupsHeadTaskResult=='modify'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1klmxot\" name=\"modify works\" sourceRef=\"ExclusiveGateway_0xdedru\" targetRef=\"EndEvent_1hxbqie\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${approveMaterialsListByCentralGroupsHeadTaskResult=='modify'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0cztiwt\">\n        <bpmn:incoming>SequenceFlow_1uudu22</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_12vbsjq</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_087eljg</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_12vbsjq\" name=\"ml attached\" sourceRef=\"ExclusiveGateway_0cztiwt\" targetRef=\"UserTask_12n8eyi\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${contractorAttachesMaterialListTaskResult=='attached'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_087eljg\" name=\"modify works\" sourceRef=\"ExclusiveGateway_0cztiwt\" targetRef=\"EndEvent_1hxbqie\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${contractorAttachesMaterialListTaskResult=='modify'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mvqaxr\" sourceRef=\"Task_0jxwgbt\" targetRef=\"ExclusiveGateway_1x0cs8g\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1tbf21w\" sourceRef=\"Task_0weut1t\" targetRef=\"EndEvent_1nde4uy\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1x0cs8g\">\n        <bpmn:incoming>SequenceFlow_1mvqaxr</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1aovmnd</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1cr5ksn</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1aovmnd\" name=\"TR match with material list\" sourceRef=\"ExclusiveGateway_1x0cs8g\" targetRef=\"Task_0weut1t\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkTRMaterialsByInitiatorTaskResult== 'match'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1cr5ksn\" name=\"TR doesn&#39;t match with material list\" sourceRef=\"ExclusiveGateway_1x0cs8g\" targetRef=\"UserTask_14yc5q6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkTRMaterialsByInitiatorTaskResult== 'doesNotMatch'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:userTask id=\"Task_0jxwgbt\" name=\"Â Validate TR\" camunda:formKey=\"embedded:app:forms/revision/checkTRWithApprovedMaterialsByInitiator.html\" camunda:assignee=\"${ starter }\">\n        <bpmn:documentation>Check attached TR with approved material list by JR initiator</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_1g2fm4f</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1mvqaxr</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:serviceTask id=\"Task_0weut1t\" name=\"generate Bar code and unload to TR document\" camunda:class=\"kz.kcell.bpm.MyJavaDelegate\">\n        <bpmn:incoming>SequenceFlow_1aovmnd</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1tbf21w</bpmn:outgoing>\n      </bpmn:serviceTask>\n      <bpmn:textAnnotation id=\"TextAnnotation_0sngnpv\">      <bpmn:text>beforÂ Contractor: 1. Create Requisition list in SAP 2.Â Create TR in SAP</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:association id=\"Association_0aeot7v\" sourceRef=\"UserTask_14yc5q6\" targetRef=\"TextAnnotation_0sngnpv\" />\n      <bpmn:textAnnotation id=\"TextAnnotation_15lrjyx\">      <bpmn:text>TR: Transfer Request (SAP Form)</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:textAnnotation id=\"TextAnnotation_0g767fd\">      <bpmn:text>If materials are not available</bpmn:text>\n</bpmn:textAnnotation>\n    </bpmn:subProcess>\n    <bpmn:subProcess id=\"SubProcess_0eah6dm\" name=\"Materials Dispatch\">\n      <bpmn:incoming>SequenceFlow_1951c3w</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_0xqu1s7</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_1p8u37f</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_1g0uit4\" name=\"Set Materials Â Dispatch Status\" camunda:formKey=\"embedded:app:forms/revision/setDispatchStatusByWarehouseSpecialist.html\" camunda:candidateUsers=\"Maral.Amantay@kcell.kz, Rinat.Kurbangaliyev@kcell.kz, Dauren.Beispaev@kcell.kz\">\n        <bpmn:documentation>Set Dispatch Status by Warehouse Specialist</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0lh9xfe</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_12505uo</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:endEvent id=\"EndEvent_1hidl6i\">\n        <bpmn:incoming>SequenceFlow_1mhjbfu</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:startEvent id=\"StartEvent_00in2ji\">\n        <bpmn:outgoing>SequenceFlow_0lh9xfe</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0lh9xfe\" sourceRef=\"StartEvent_00in2ji\" targetRef=\"UserTask_1g0uit4\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1yyzaop\">\n        <bpmn:incoming>SequenceFlow_12505uo</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1mhjbfu</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1hgha1n</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_12505uo\" sourceRef=\"UserTask_1g0uit4\" targetRef=\"ExclusiveGateway_1yyzaop\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mhjbfu\" name=\"dispatched\" sourceRef=\"ExclusiveGateway_1yyzaop\" targetRef=\"EndEvent_1hidl6i\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${setDispatchStatusByWarehouseSpecialistTaskResult=='dispatched'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_0qyxs4e\">\n        <bpmn:incoming>SequenceFlow_1hgha1n</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1hgha1n\" name=\"materials are not available\" sourceRef=\"ExclusiveGateway_1yyzaop\" targetRef=\"EndEvent_0qyxs4e\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${setDispatchStatusByWarehouseSpecialistTaskResult=='notAvailable'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n    </bpmn:subProcess>\n    <bpmn:subProcess id=\"SubProcess_0a0tcem\" name=\"Execute Works\">\n      <bpmn:incoming>SequenceFlow_0xqu1s7</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0uvzo91</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_1lwdiem</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0jvqwqj</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0d3icf7</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1hw1xm0</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_05vqen4</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_1sfkegw</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_0rj3nbv\" name=\"Fill Applied Changes Info\" camunda:formKey=\"embedded:app:forms/revision/fillAppliedChangesInfoByContractorNew.html\" camunda:assignee=\"Begaly.Kokin@kcell.kz\">\n        <bpmn:documentation>Fill Applied Changes Info by Contractor</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.AssetManagementSaveListenerNew\" event=\"complete\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1tfcopp</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1je1s10</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:endEvent id=\"EndEvent_1h23ev2\">\n        <bpmn:incoming>SequenceFlow_05kk3pj</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:serviceTask id=\"ServiceTask_0yrq8kf\" name=\"Apply Changes to Asset Management\" camunda:class=\"kz.kcell.bpm.MyJavaDelegate\">\n        <bpmn:incoming>SequenceFlow_0x1dmwr</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_05kk3pj</bpmn:outgoing>\n      </bpmn:serviceTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_05kk3pj\" sourceRef=\"ServiceTask_0yrq8kf\" targetRef=\"EndEvent_1h23ev2\" />\n      <bpmn:startEvent id=\"StartEvent_18vp8g4\">\n        <bpmn:outgoing>SequenceFlow_1tfcopp</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1tfcopp\" sourceRef=\"StartEvent_18vp8g4\" targetRef=\"UserTask_0rj3nbv\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_176pq00\">\n        <bpmn:incoming>SequenceFlow_1je1s10</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0x1dmwr</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1x1uq5y</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_04ms90t</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1je1s10\" sourceRef=\"UserTask_0rj3nbv\" targetRef=\"ExclusiveGateway_176pq00\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0x1dmwr\" name=\"save changes to asset management\" sourceRef=\"ExclusiveGateway_176pq00\" targetRef=\"ServiceTask_0yrq8kf\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='saveChanges'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_02g9e0x\">\n        <bpmn:incoming>SequenceFlow_1x1uq5y</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1x1uq5y\" name=\"materials not available\" sourceRef=\"ExclusiveGateway_176pq00\" targetRef=\"EndEvent_02g9e0x\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='notAvailable'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_04ms90t\" name=\"cannot perform works\" sourceRef=\"ExclusiveGateway_176pq00\" targetRef=\"EndEvent_1utuq8j\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='cannotPerform'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:endEvent id=\"EndEvent_1utuq8j\">\n        <bpmn:incoming>SequenceFlow_04ms90t</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:textAnnotation id=\"TextAnnotation_0zdrz4s\">      <bpmn:text>beforeÂ Perform the Job by Contractor</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:association id=\"Association_0i9c9uk\" sourceRef=\"UserTask_0rj3nbv\" targetRef=\"TextAnnotation_0zdrz4s\" />\n    </bpmn:subProcess>\n    <bpmn:subProcess id=\"SubProcess_0cxhstf\" name=\"Create PR\">\n      <bpmn:incoming>SequenceFlow_17n684j</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0xhyefk</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1mr38k3</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_1o8iany</bpmn:outgoing>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1l2kum0\" name=\"\">\n        <bpmn:incoming>SequenceFlow_0mxowg3</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1r69son</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1mm8ziz</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1qu5j9t</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:endEvent id=\"EndEvent_0b9g5de\">\n        <bpmn:incoming>SequenceFlow_1qu5j9t</bpmn:incoming>\n        <bpmn:errorEventDefinition errorRef=\"myException\" />\n      </bpmn:endEvent>\n      <bpmn:endEvent id=\"EndEvent_1hxowlo\">\n        <bpmn:incoming>SequenceFlow_1r69son</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1r69son\" name=\"created\" sourceRef=\"ExclusiveGateway_1l2kum0\" targetRef=\"EndEvent_1hxowlo\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${sapLeasingCompletionCommenTaskResult=='prCreated'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mm8ziz\" name=\"budget low\" sourceRef=\"ExclusiveGateway_1l2kum0\" targetRef=\"ScriptTask_0h526ls\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${sapLeasingCompletionCommenTaskResult=='increaseBudget'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1ss7wlc\" sourceRef=\"ScriptTask_0k2vqwv\" targetRef=\"Task_0s5v6wl\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0mxowg3\" sourceRef=\"Task_0s5v6wl\" targetRef=\"ExclusiveGateway_1l2kum0\" />\n      <bpmn:userTask id=\"Task_0s5v6wl\" name=\"PR Status\" camunda:formKey=\"embedded:app:forms/revision/updatePRStatusBySAPSpecialist.html\" camunda:assignee=\"Aigerim.Segizbayeva@kcell.kz\">\n        <bpmn:documentation>Update PR Status by SAP Specialist</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:formData />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1i35baw</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1ss7wlc</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0mxowg3</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1qu5j9t\" name=\"impossible to create\" sourceRef=\"ExclusiveGateway_1l2kum0\" targetRef=\"EndEvent_0b9g5de\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${sapLeasingCompletionCommenTaskResult=='cannotCreatePR'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:startEvent id=\"StartEvent_093jrj6\">\n        <bpmn:outgoing>SequenceFlow_1mjhudh</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mjhudh\" sourceRef=\"StartEvent_093jrj6\" targetRef=\"ScriptTask_0k2vqwv\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1i35baw\" sourceRef=\"ScriptTask_0h526ls\" targetRef=\"Task_0s5v6wl\" />\n      <bpmn:sendTask id=\"ScriptTask_0k2vqwv\" name=\"Send e-Mail to SAP Specialist\" camunda:class=\"kz.kcell.bpm.MyJavaDelegate\">\n        <bpmn:incoming>SequenceFlow_1mjhudh</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1ss7wlc</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:sendTask id=\"ScriptTask_0h526ls\" name=\"e-Mail to Extend Budget\" camunda:class=\"kz.kcell.bpm.MyJavaDelegate\">\n        <bpmn:incoming>SequenceFlow_1mm8ziz</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1i35baw</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:association id=\"Association_17u0nhw\" sourceRef=\"EndEvent_0b9g5de\" targetRef=\"TextAnnotation_08gimrc\" />\n      <bpmn:textAnnotation id=\"TextAnnotation_14g54mo\">      <bpmn:text>beforeÂ Manually Create PR by SAP Specialist</bpmn:text>\n</bpmn:textAnnotation>\n      <bpmn:association id=\"Association_1jg82nu\" sourceRef=\"Task_0s5v6wl\" targetRef=\"TextAnnotation_14g54mo\" />\n    </bpmn:subProcess>\n    <bpmn:exclusiveGateway id=\"ExclusiveGateway_15zm0uz\">\n      <bpmn:incoming>SequenceFlow_1mr38k3</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_0uvzo91</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_10nsvzs</bpmn:outgoing>\n    </bpmn:exclusiveGateway>\n    <bpmn:subProcess id=\"SubProcess_0cd7y34\" name=\"Create JR\">\n      <bpmn:incoming>SequenceFlow_1o8wk2g</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0b217tv</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0eg1f85</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_1hw1xm0</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_01s02ho</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_062z6hr</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_1ru64f6\" name=\"Create JR\" camunda:formKey=\"embedded:app:forms/revision/create-jr.html\" camunda:assignee=\"${ starter }\">\n        <bpmn:documentation>Regions Create Revision JR</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.GenerateJobRequestNumber\" event=\"complete\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_0xr8fnm</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1ip3b6e</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1hoa515</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_0ifgbkt</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1eg1vhx</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_068h5f0</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_00vcuxx</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:userTask id=\"UserTask_11b2osi\" name=\"Approve JR\" camunda:formKey=\"embedded:app:forms/revision/regionGroupHeadApproval.html\">\n        <bpmn:documentation>Region Group Head Approval</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.RegionGroupAssignmentHandler\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_0k8qm6w</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0bhtmw4</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:userTask id=\"UserTask_1qf7rmc\" name=\"Approve JR\" camunda:formKey=\"embedded:app:forms/revision/centralGroupHeadApproval.html\">\n        <bpmn:documentation>Central Group Head Approval</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.CentralGroupAssignmentHandler\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_0tunbpb</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0n4aumv</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1j6sg6y\" name=\"\">\n        <bpmn:incoming>SequenceFlow_1pq5xas</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1wqdne7</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1hoa515</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:endEvent id=\"EndEvent_0hukegx\">\n        <bpmn:incoming>SequenceFlow_1wqdne7</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_03otu34</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_1p9psxx</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_00vcuxx\" name=\"JR created /corrected\" sourceRef=\"UserTask_1ru64f6\" targetRef=\"ExclusiveGateway_0gs09yt\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1wqdne7\" name=\"approved\" sourceRef=\"ExclusiveGateway_1j6sg6y\" targetRef=\"EndEvent_0hukegx\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupApprovalBudgetEngineerTaskResult=='approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1hoa515\" name=\"rejected\" sourceRef=\"ExclusiveGateway_1j6sg6y\" targetRef=\"UserTask_1ru64f6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupApprovalBudgetEngineerTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:startEvent id=\"StartEvent_1xcw8jg\">\n        <bpmn:outgoing>SequenceFlow_0xr8fnm</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0xr8fnm\" sourceRef=\"StartEvent_1xcw8jg\" targetRef=\"UserTask_1ru64f6\" />\n      <bpmn:endEvent id=\"EndEvent_03do2v0\">\n        <bpmn:incoming>SequenceFlow_05dm0js</bpmn:incoming>\n        <bpmn:terminateEventDefinition />\n      </bpmn:endEvent>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_125e0rj\">\n        <bpmn:incoming>SequenceFlow_1troz3f</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1p9994w</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_056em9e</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1p9994w\" name=\"Power engineering required\" sourceRef=\"ExclusiveGateway_125e0rj\" targetRef=\"Task_1xhzfxw\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${powerRequired=='Yes'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_056em9e\" name=\"Power engineering not required\" sourceRef=\"ExclusiveGateway_125e0rj\" targetRef=\"ExclusiveGateway_06g0jl8\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${powerRequired=='No'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1of3ayp\">\n        <bpmn:incoming>SequenceFlow_0uqg7t2</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_07gu6kr</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_0ifgbkt</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0uqg7t2\" sourceRef=\"Task_1xhzfxw\" targetRef=\"ExclusiveGateway_1of3ayp\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_07gu6kr\" name=\"approved\" sourceRef=\"ExclusiveGateway_1of3ayp\" targetRef=\"ExclusiveGateway_06g0jl8\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkByPowerEngineerAndMakeCalculationsTaskResult == 'approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0aqvpql\">\n        <bpmn:incoming>SequenceFlow_0bhtmw4</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1troz3f</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1tkoxjb</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1ip3b6e</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0bhtmw4\" sourceRef=\"UserTask_11b2osi\" targetRef=\"ExclusiveGateway_0aqvpql\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1troz3f\" name=\"approved\" sourceRef=\"ExclusiveGateway_0aqvpql\" targetRef=\"ExclusiveGateway_125e0rj\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${regionGroupHeadApprovalTaskResult == 'approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sendTask id=\"Notify_Cant_Fix_JR\" name=\"Notification: JR execution impossible\">\n        <bpmn:extensionElements>\n          <camunda:connector>\n            <camunda:inputOutput>\n              <camunda:inputParameter name=\"to\">\n                <camunda:script scriptFormat=\"groovy\" resource=\"GetStarterEmail.groovy\" />\n              </camunda:inputParameter>\n              <camunda:inputParameter name=\"subject\"><![CDATA[Can't fix JR]]></camunda:inputParameter>\n              <camunda:inputParameter name=\"html\">\n                <camunda:script scriptFormat=\"freemarker\" resource=\"CantFixJobRequestMessage.ftl\" />\n              </camunda:inputParameter>\n            </camunda:inputOutput>\n            <camunda:connectorId>mail-send</camunda:connectorId>\n          </camunda:connector>\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1tkoxjb</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_05dm0js</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1tkoxjb\" name=\"can&#39;t fix\" sourceRef=\"ExclusiveGateway_0aqvpql\" targetRef=\"Notify_Cant_Fix_JR\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${regionGroupHeadApprovalTaskResult == 'cantFix'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_05dm0js\" sourceRef=\"Notify_Cant_Fix_JR\" targetRef=\"EndEvent_03do2v0\" />\n      <bpmn:userTask id=\"Task_1xhzfxw\" name=\"Check Power\" camunda:formKey=\"embedded:app:forms/revision/checkByPowerEngineerAndMakeCalculations.html\" camunda:candidateUsers=\"Yevgeniy.Elunin@kcell.kz, Yermek.Tanabekov@kcell.kz, Alexey.Kolesnikov@kcell.kz\">\n        <bpmn:documentation>Check by Power engineer and make calculations</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_1p9994w</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0uqg7t2</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1ip3b6e\" name=\"rejected\" sourceRef=\"ExclusiveGateway_0aqvpql\" targetRef=\"UserTask_1ru64f6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${regionGroupHeadApprovalTaskResult == 'rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0ifgbkt\" name=\"rejected\" sourceRef=\"ExclusiveGateway_1of3ayp\" targetRef=\"UserTask_1ru64f6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${checkByPowerEngineerAndMakeCalculationsTaskResult == 'rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:userTask id=\"UserTask_1qksldt\" name=\"Approve Transmission works\" camunda:formKey=\"embedded:app:forms/revision/centralGroupApprovalEngineer.html\" camunda:candidateUsers=\"Sergey.Grigor@kcell.kz, Galym.Tulenbayev@kcell.kz\">\n        <bpmn:documentation>Central Group Â Approval (engineer)</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0s4pkfn</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1856psw</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:userTask id=\"UserTask_1tpn4q3\" name=\"Approve JR Budget\" camunda:formKey=\"embedded:app:forms/revision/centralGroupApprovalBudgetEngineer.html\" camunda:candidateUsers=\"Aigerim.Satybekova@kcell.kz, Tatyana.Solovyova@kcell.kz, Bolat.Idirisov@kcell.kz, Lyudmila.Vilkova@kcell.kz\">\n        <bpmn:documentation>Central Group Â Approval (budget engineer)</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0c7fdul</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1pq5xas</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_06g0jl8\">\n        <bpmn:incoming>SequenceFlow_07gu6kr</bpmn:incoming>\n        <bpmn:incoming>SequenceFlow_056em9e</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0tunbpb</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_0s4pkfn</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0tunbpb\" sourceRef=\"ExclusiveGateway_06g0jl8\" targetRef=\"UserTask_1qf7rmc\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${reason != '2'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0s4pkfn\" name=\"if transmission works selected\" sourceRef=\"ExclusiveGateway_06g0jl8\" targetRef=\"UserTask_1qksldt\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${reason == '2'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1pq5xas\" sourceRef=\"UserTask_1tpn4q3\" targetRef=\"ExclusiveGateway_1j6sg6y\" />\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_059gfjr\">\n        <bpmn:incoming>SequenceFlow_1856psw</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0c7fdul</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1eg1vhx</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1856psw\" sourceRef=\"UserTask_1qksldt\" targetRef=\"ExclusiveGateway_059gfjr\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0c7fdul\" name=\"Approved\" sourceRef=\"ExclusiveGateway_059gfjr\" targetRef=\"UserTask_1tpn4q3\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupApprovalEngineerTaskResult == 'approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1eg1vhx\" name=\"Rejected\" sourceRef=\"ExclusiveGateway_059gfjr\" targetRef=\"UserTask_1ru64f6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupApprovalEngineerTaskResult == 'rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0ejyp7l\">\n        <bpmn:incoming>SequenceFlow_0n4aumv</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_03otu34</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_068h5f0</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0n4aumv\" sourceRef=\"UserTask_1qf7rmc\" targetRef=\"ExclusiveGateway_0ejyp7l\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_03otu34\" name=\"Approved\" sourceRef=\"ExclusiveGateway_0ejyp7l\" targetRef=\"EndEvent_0hukegx\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupHeadApprovalTaskResult == 'approved'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_068h5f0\" name=\"Rejected\" sourceRef=\"ExclusiveGateway_0ejyp7l\" targetRef=\"UserTask_1ru64f6\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${centralGroupHeadApprovalTaskResult == 'rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_0gs09yt\">\n        <bpmn:incoming>SequenceFlow_00vcuxx</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0k8qm6w</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1p9psxx</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0k8qm6w\" sourceRef=\"ExclusiveGateway_0gs09yt\" targetRef=\"UserTask_11b2osi\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${ptype=='prod'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1p9psxx\" sourceRef=\"ExclusiveGateway_0gs09yt\" targetRef=\"EndEvent_0hukegx\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${ptype=='test'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:association id=\"Association_13cbx79\" sourceRef=\"UserTask_1ru64f6\" targetRef=\"TextAnnotation_0hzejdn\" />\n    </bpmn:subProcess>\n    <bpmn:startEvent id=\"StartEvent_1p9uxqe\" camunda:initiator=\"starter\">\n      <bpmn:extensionElements>\n        <camunda:executionListener class=\"kz.kcell.bpm.CreateProcessDelegate\" event=\"start\" />\n      </bpmn:extensionElements>\n      <bpmn:outgoing>SequenceFlow_1o8wk2g</bpmn:outgoing>\n    </bpmn:startEvent>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0uvzo91\" name=\"materials are not required\" sourceRef=\"ExclusiveGateway_15zm0uz\" targetRef=\"SubProcess_0a0tcem\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${materialsRequired=='No'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_10nsvzs\" name=\"materials required\" sourceRef=\"ExclusiveGateway_15zm0uz\" targetRef=\"SubProcess_04dekrg\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${materialsRequired=='Yes'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0xqu1s7\" sourceRef=\"SubProcess_0eah6dm\" targetRef=\"SubProcess_0a0tcem\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${materialsNotAvailable==false}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_01s02ho\" name=\"jr approved\" sourceRef=\"SubProcess_0cd7y34\" targetRef=\"ExclusiveGateway_0ibfmmi\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${createJRResult=='approved'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1mr38k3\" name=\"created\" sourceRef=\"SubProcess_0cxhstf\" targetRef=\"ExclusiveGateway_15zm0uz\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${prCreated==true}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1o8wk2g\" sourceRef=\"StartEvent_1p9uxqe\" targetRef=\"SubProcess_0cd7y34\" />\n    <bpmn:sequenceFlow id=\"SequenceFlow_17n684j\" name=\"leasing done\" sourceRef=\"SubProcess_0cwj4g6\" targetRef=\"SubProcess_0cxhstf\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${leasingDone==true}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:exclusiveGateway id=\"ExclusiveGateway_0gbl31i\">\n      <bpmn:incoming>SequenceFlow_1rfvru8</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_0jidb3c</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0xhyefk</bpmn:outgoing>\n    </bpmn:exclusiveGateway>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0jidb3c\" name=\"leasing required\" sourceRef=\"ExclusiveGateway_0gbl31i\" targetRef=\"SubProcess_0cwj4g6\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${leasingRequired=='Yes'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0xhyefk\" name=\"leasing not required\" sourceRef=\"ExclusiveGateway_0gbl31i\" targetRef=\"SubProcess_0cxhstf\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${leasingRequired=='No'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:subProcess id=\"SubProcess_0v7hq1m\" name=\"Accept Performed Job\">\n      <bpmn:incoming>SequenceFlow_0go8umh</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1lwdiem</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0fhdfes</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0paiubf</bpmn:outgoing>\n      <bpmn:userTask id=\"UserTask_1i7na4a\" name=\"Accept Performed Works\" camunda:formKey=\"embedded:app:forms/revision/acceptJobByRegionalGroup.html\">\n        <bpmn:documentation>Accept Job by Regional Group</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.AcceptJobByRegionalGroup\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1oqa52r</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1o9e13l</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1fzj1w1\">\n        <bpmn:incoming>SequenceFlow_1o9e13l</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0p9r9wf</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_0fkgaq0</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:userTask id=\"UserTask_0m3wppw\" name=\"Accept Performed Works\" camunda:formKey=\"embedded:app:forms/revision/acceptJobByCentralGroup.html\">\n        <bpmn:documentation>Accept Job by Central Group</bpmn:documentation>\n        <bpmn:extensionElements>\n          <camunda:taskListener class=\"kz.kcell.bpm.assignments.AcceptJobByCentralGroup\" event=\"create\" />\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_0p9r9wf</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_152tq2c</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_1aciqr8\">\n        <bpmn:incoming>SequenceFlow_152tq2c</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_0b8zlhx</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_061k4i3</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:endEvent id=\"EndEvent_1nd7ldr\">\n        <bpmn:incoming>SequenceFlow_1j4zio7</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:endEvent id=\"EndEvent_0ozmh12\">\n        <bpmn:incoming>SequenceFlow_0fkgaq0</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:endEvent id=\"EndEvent_1aget54\">\n        <bpmn:incoming>SequenceFlow_061k4i3</bpmn:incoming>\n      </bpmn:endEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0p9r9wf\" name=\"accepted\" sourceRef=\"ExclusiveGateway_1fzj1w1\" targetRef=\"UserTask_0m3wppw\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptJobByRegionalGroupTaskResult=='accepted'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_0fkgaq0\" name=\"rejected\" sourceRef=\"ExclusiveGateway_1fzj1w1\" targetRef=\"EndEvent_0ozmh12\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptJobByRegionalGroupTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_152tq2c\" sourceRef=\"UserTask_0m3wppw\" targetRef=\"ExclusiveGateway_1aciqr8\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_0b8zlhx\" name=\"accepted\" sourceRef=\"ExclusiveGateway_1aciqr8\" targetRef=\"Task_1kwxxw1\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptJobByCentralGroupTaskResult=='accepted'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_061k4i3\" name=\"rejected\" sourceRef=\"ExclusiveGateway_1aciqr8\" targetRef=\"EndEvent_1aget54\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptJobByCentralGroupTaskResult=='rejected'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:startEvent id=\"StartEvent_1g9cwh9\">\n        <bpmn:outgoing>SequenceFlow_1oqa52r</bpmn:outgoing>\n      </bpmn:startEvent>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1oqa52r\" sourceRef=\"StartEvent_1g9cwh9\" targetRef=\"UserTask_1i7na4a\" />\n      <bpmn:userTask id=\"Task_1kwxxw1\" name=\"Generate Acceptance Docs and Save to Asset Management\" camunda:formKey=\"embedded:app:forms/revision/generateAcceptanceDocsAndSaveToAssetManagement.html\" camunda:assignee=\"${ starter }\">\n        <bpmn:documentation>Generate Acceptance Docs and Save to Asset Management</bpmn:documentation>\n        <bpmn:incoming>SequenceFlow_0b8zlhx</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1ea0261</bpmn:outgoing>\n      </bpmn:userTask>\n      <bpmn:exclusiveGateway id=\"ExclusiveGateway_00yqrvw\">\n        <bpmn:incoming>SequenceFlow_1ea0261</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1j4zio7</bpmn:outgoing>\n        <bpmn:outgoing>SequenceFlow_1gr7bbx</bpmn:outgoing>\n      </bpmn:exclusiveGateway>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1j4zio7\" name=\"not required submit to leasingÂ \" sourceRef=\"ExclusiveGateway_00yqrvw\" targetRef=\"EndEvent_1nd7ldr\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${generateAcceptanceDocsAndSaveToAssetManagementTaskResult== 'accepted'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1ea0261\" sourceRef=\"Task_1kwxxw1\" targetRef=\"ExclusiveGateway_00yqrvw\" />\n      <bpmn:sequenceFlow id=\"SequenceFlow_1gr7bbx\" name=\"required submit to leasing\" sourceRef=\"ExclusiveGateway_00yqrvw\" targetRef=\"Task_1vniyf0\">\n        <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${generateAcceptanceDocsAndSaveToAssetManagementTaskResult== 'submitToLeasing'}]]></bpmn:conditionExpression>\n      </bpmn:sequenceFlow>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1mn2lcw\" sourceRef=\"Task_1vniyf0\" targetRef=\"EndEvent_1h89z2r\" />\n      <bpmn:sendTask id=\"Task_1vniyf0\" name=\"Notify of requiring submit to leasingÂ \">\n        <bpmn:extensionElements>\n          <camunda:connector>\n            <camunda:inputOutput>\n              <camunda:inputParameter name=\"to\">Askar.Slambekov@kcell.kz</camunda:inputParameter>\n              <camunda:inputParameter name=\"subject\">Job Request Requires Submit to Leasing</camunda:inputParameter>\n              <camunda:inputParameter name=\"text\">ÐÐµ Ð·Ð½Ð°ÑŽ, ÐºÐ¾Ð³Ð¾ ÑƒÐ²ÐµÐ´Ð¾Ð¼Ð»ÑÑ‚ÑŒ, Ð¿Ð¾ÑÑ‚Ð¾Ð¼Ñƒ ÑƒÐ²ÐµÐ´Ð¾Ð¼Ð»ÑÑŽ Ð²Ð°Ñ :)</camunda:inputParameter>\n            </camunda:inputOutput>\n            <camunda:connectorId>mail-send</camunda:connectorId>\n          </camunda:connector>\n        </bpmn:extensionElements>\n        <bpmn:incoming>SequenceFlow_1gr7bbx</bpmn:incoming>\n        <bpmn:outgoing>SequenceFlow_1mn2lcw</bpmn:outgoing>\n      </bpmn:sendTask>\n      <bpmn:sequenceFlow id=\"SequenceFlow_1o9e13l\" sourceRef=\"UserTask_1i7na4a\" targetRef=\"ExclusiveGateway_1fzj1w1\" />\n      <bpmn:endEvent id=\"EndEvent_1h89z2r\">\n        <bpmn:incoming>SequenceFlow_1mn2lcw</bpmn:incoming>\n      </bpmn:endEvent>\n    </bpmn:subProcess>\n    <bpmn:endEvent id=\"EndEvent_1pp7st1\">\n      <bpmn:incoming>SequenceFlow_01n47ri</bpmn:incoming>\n    </bpmn:endEvent>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1p8u37f\" name=\"materials are not available\" sourceRef=\"SubProcess_0eah6dm\" targetRef=\"SubProcess_04dekrg\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${materialsNotAvailable==true}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1lwdiem\" name=\"rejected job\" sourceRef=\"SubProcess_0v7hq1m\" targetRef=\"SubProcess_0a0tcem\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptPerformedJob== 'rejected'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0b217tv\" name=\"modify works\" sourceRef=\"SubProcess_04dekrg\" targetRef=\"SubProcess_0cd7y34\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${modifyWorks==true}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0eg1f85\" name=\"leasing impossible\" sourceRef=\"SubProcess_0cwj4g6\" targetRef=\"SubProcess_0cd7y34\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${leasingDone==false}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1hw1xm0\" name=\"cannot perform works\" sourceRef=\"SubProcess_0a0tcem\" targetRef=\"SubProcess_0cd7y34\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='cannotPerform'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0jvqwqj\" name=\"not approved, request corrections\" sourceRef=\"SubProcess_1okaxp7\" targetRef=\"SubProcess_0a0tcem\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${revisionPrepare == 'notApproved'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_05vqen4\" name=\"materials not available\" sourceRef=\"SubProcess_0a0tcem\" targetRef=\"SubProcess_04dekrg\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='notAvailable'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_01n47ri\" sourceRef=\"Task_0y93m8z\" targetRef=\"EndEvent_1pp7st1\" />\n    <bpmn:sendTask id=\"Task_0y93m8z\" name=\"Notify of Job Request Completion\">\n      <bpmn:extensionElements>\n        <camunda:connector>\n          <camunda:inputOutput>\n            <camunda:inputParameter name=\"to\">\n              <camunda:script scriptFormat=\"groovy\" resource=\"GetStarterEmail.groovy\" />\n            </camunda:inputParameter>\n            <camunda:inputParameter name=\"subject\">JR Complete</camunda:inputParameter>\n            <camunda:inputParameter name=\"html\">\n              <camunda:script scriptFormat=\"freemarker\" resource=\"JRCompleteMessage.ftl\" />\n            </camunda:inputParameter>\n          </camunda:inputOutput>\n          <camunda:connectorId>mail-send</camunda:connectorId>\n        </camunda:connector>\n      </bpmn:extensionElements>\n      <bpmn:incoming>SequenceFlow_0fhdfes</bpmn:incoming>\n      <bpmn:incoming>SequenceFlow_0paiubf</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_01n47ri</bpmn:outgoing>\n    </bpmn:sendTask>\n    <bpmn:endEvent id=\"EndEvent_1fo49fj\">\n      <bpmn:incoming>SequenceFlow_062z6hr</bpmn:incoming>\n    </bpmn:endEvent>\n    <bpmn:sequenceFlow id=\"SequenceFlow_062z6hr\" name=\"rejected\" sourceRef=\"SubProcess_0cd7y34\" targetRef=\"EndEvent_1fo49fj\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${createJRResult=='rejected'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:endEvent id=\"EndEvent_02t2w1s\">\n      <bpmn:incoming>SequenceFlow_1ovjrsn</bpmn:incoming>\n    </bpmn:endEvent>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1o8iany\" sourceRef=\"SubProcess_0cxhstf\" targetRef=\"Task_1lcm554\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${prCreated==false}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1ovjrsn\" sourceRef=\"Task_1lcm554\" targetRef=\"EndEvent_02t2w1s\" />\n    <bpmn:sequenceFlow id=\"SequenceFlow_1951c3w\" sourceRef=\"SubProcess_04dekrg\" targetRef=\"SubProcess_0eah6dm\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\">${modifyWorks==false}</bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1sfkegw\" name=\"job executed successfully\" sourceRef=\"SubProcess_0a0tcem\" targetRef=\"SubProcess_1okaxp7\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${fillAppliedChangesInfoByContractorTaskResult=='saveChanges'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0go8umh\" name=\"approved\" sourceRef=\"SubProcess_1okaxp7\" targetRef=\"SubProcess_0v7hq1m\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${revisionPrepare == 'approved'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0fhdfes\" name=\"accepted\" sourceRef=\"SubProcess_0v7hq1m\" targetRef=\"Task_0y93m8z\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptPerformedJob== 'accepted'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0paiubf\" name=\"required submit to leasing\" sourceRef=\"SubProcess_0v7hq1m\" targetRef=\"Task_0y93m8z\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${acceptPerformedJob== 'submitToLeasing'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:exclusiveGateway id=\"ExclusiveGateway_0ibfmmi\">\n      <bpmn:incoming>SequenceFlow_01s02ho</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1w6xl96</bpmn:outgoing>\n      <bpmn:outgoing>SequenceFlow_0d3icf7</bpmn:outgoing>\n    </bpmn:exclusiveGateway>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1w6xl96\" sourceRef=\"ExclusiveGateway_0ibfmmi\" targetRef=\"Task_11vrxiu\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${ptype=='prod'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_0d3icf7\" sourceRef=\"ExclusiveGateway_0ibfmmi\" targetRef=\"SubProcess_0a0tcem\">\n      <bpmn:conditionExpression xsi:type=\"bpmn:tFormalExpression\"><![CDATA[${ptype=='test'}]]></bpmn:conditionExpression>\n    </bpmn:sequenceFlow>\n    <bpmn:sequenceFlow id=\"SequenceFlow_1rfvru8\" sourceRef=\"Task_11vrxiu\" targetRef=\"ExclusiveGateway_0gbl31i\" />\n    <bpmn:serviceTask id=\"Task_11vrxiu\" name=\"Generate &#34;JR Blank&#34; and send it to Contractor\" camunda:class=\"kz.kcell.bpm.SendGeneratedJRBlank\">\n      <bpmn:incoming>SequenceFlow_1w6xl96</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1rfvru8</bpmn:outgoing>\n    </bpmn:serviceTask>\n    <bpmn:sendTask id=\"Task_1lcm554\" name=\"Notify PR cannot be created\">\n      <bpmn:extensionElements>\n        <camunda:connector>\n          <camunda:inputOutput>\n            <camunda:inputParameter name=\"to\">\n              <camunda:script scriptFormat=\"groovy\" resource=\"GetStarterEmail.groovy\" />\n            </camunda:inputParameter>\n            <camunda:inputParameter name=\"subject\">PR Cannot be created</camunda:inputParameter>\n            <camunda:inputParameter name=\"html\">\n              <camunda:script scriptFormat=\"freemarker\" resource=\"PRCannotBeCreatedMessage.ftl\" />\n            </camunda:inputParameter>\n          </camunda:inputOutput>\n          <camunda:connectorId>mail-send</camunda:connectorId>\n        </camunda:connector>\n      </bpmn:extensionElements>\n      <bpmn:incoming>SequenceFlow_1o8iany</bpmn:incoming>\n      <bpmn:outgoing>SequenceFlow_1ovjrsn</bpmn:outgoing>\n    </bpmn:sendTask>\n    <bpmn:textAnnotation id=\"TextAnnotation_08gimrc\">    <bpmn:text>Exception: if PR cannot be created</bpmn:text>\n</bpmn:textAnnotation>\n    <bpmn:textAnnotation id=\"TextAnnotation_189rkq0\">    <bpmn:text><![CDATA[Infrormation is taken from step \"Fill Applied Changes Info by Contractor\"]]></bpmn:text>\n</bpmn:textAnnotation>\n    <bpmn:textAnnotation id=\"TextAnnotation_0w7it18\">    <bpmn:text>Ð“Ð¾Ð²Ð¾Ñ€Ð¸Ð¼, Ñ‡Ñ‚Ð¾ Ð´Ð¾ÐºÑƒÐ¼ÐµÐ½Ñ‚Ñ‹ Ð¿Ñ€Ð¾Ð°Ð¿Ð¿Ñ€ÑƒÐ²Ð»ÐµÐ½Ñ‹ Ð¸ Ð·Ð°Ð³Ñ€ÑƒÐ¶Ð°ÐµÐ¼ Ð¿Ð¾Ð»ÑƒÑ‡ÐµÐ½Ð½Ñ‹Ðµ Ñ€Ð°Ð·Ñ€ÐµÑˆÐµÐ½Ð¸Ñ/Ð¾Ñ‚ÐºÐ°Ð·Ñ‹</bpmn:text>\n</bpmn:textAnnotation>\n    <bpmn:textAnnotation id=\"TextAnnotation_1bmypeh\">    <bpmn:text>??? Ð³Ð´Ðµ ÑÑ‚Ð¾ ÑÐ¾Ñ…Ñ€Ð°Ð½Ð¸Ñ‚ÑŒ Ñ‡Ñ‚Ð¾Ð±Ñ‹ ÑÑ‚Ð° Ð¸Ð½Ñ„Ð¾Ñ€Ð¼Ð°Ñ†Ð¸Ñ Ð½Ðµ Ð¿Ð¾Ñ‚ÐµÑ€ÑÐ»Ð°ÑÑŒ ???</bpmn:text>\n</bpmn:textAnnotation>\n    <bpmn:textAnnotation id=\"TextAnnotation_04juvza\">    <bpmn:text>When Leasing not done?</bpmn:text>\n</bpmn:textAnnotation>\n    <bpmn:textAnnotation id=\"TextAnnotation_0hzejdn\">    <bpmn:text>Each Work should have associated types of materials usually required</bpmn:text>\n</bpmn:textAnnotation>\n  </bpmn:process>\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Revision\">\n      <bpmndi:BPMNShape id=\"SubProcess_0cwj4g6_di\" bpmnElement=\"SubProcess_0cwj4g6\" isExpanded=\"true\">\n        <dc:Bounds x=\"-2670\" y=\"-79\" width=\"698\" height=\"297\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_1okaxp7_di\" bpmnElement=\"SubProcess_1okaxp7\" isExpanded=\"true\">\n        <dc:Bounds x=\"2537\" y=\"-671\" width=\"955\" height=\"450\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_04dekrg_di\" bpmnElement=\"SubProcess_04dekrg\" isExpanded=\"true\">\n        <dc:Bounds x=\"-1696\" y=\"-750\" width=\"2086\" height=\"468\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_0eah6dm_di\" bpmnElement=\"SubProcess_0eah6dm\" isExpanded=\"true\">\n        <dc:Bounds x=\"444\" y=\"-594\" width=\"571\" height=\"295\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_0a0tcem_di\" bpmnElement=\"SubProcess_0a0tcem\" isExpanded=\"true\">\n        <dc:Bounds x=\"1097\" y=\"-693\" width=\"1140\" height=\"488\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1i7na4a_di\" bpmnElement=\"UserTask_1i7na4a\">\n        <dc:Bounds x=\"3710\" y=\"-469\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1fzj1w1_di\" bpmnElement=\"ExclusiveGateway_1fzj1w1\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"3862\" y=\"-454\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3887\" y=\"-404\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0m3wppw_di\" bpmnElement=\"UserTask_0m3wppw\">\n        <dc:Bounds x=\"4002\" y=\"-469\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1aciqr8_di\" bpmnElement=\"ExclusiveGateway_1aciqr8\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"4188\" y=\"-454\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4213\" y=\"-404\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1nd7ldr_di\" bpmnElement=\"EndEvent_1nd7ldr\">\n        <dc:Bounds x=\"4713\" y=\"-447\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4686\" y=\"-411\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_0cxhstf_di\" bpmnElement=\"SubProcess_0cxhstf\" isExpanded=\"true\">\n        <dc:Bounds x=\"-2557\" y=\"289\" width=\"614\" height=\"465\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_15zm0uz_di\" bpmnElement=\"ExclusiveGateway_15zm0uz\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-1830\" y=\"-177\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1805\" y=\"-127\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SubProcess_0cd7y34_di\" bpmnElement=\"SubProcess_0cd7y34\" isExpanded=\"true\">\n        <dc:Bounds x=\"-4359\" y=\"-634\" width=\"1697\" height=\"509\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"StartEvent_1p9uxqe_di\" bpmnElement=\"StartEvent_1p9uxqe\">\n        <dc:Bounds x=\"-4448\" y=\"-377\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4430\" y=\"-341\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"TextAnnotation_08gimrc_di\" bpmnElement=\"TextAnnotation_08gimrc\">\n        <dc:Bounds x=\"-2106\" y=\"235\" width=\"213\" height=\"44\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0uvzo91_di\" bpmnElement=\"SequenceFlow_0uvzo91\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1780\" y=\"-152\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1209\" y=\"-152\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1209\" y=\"-205\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1625\" y=\"-188\" width=\"84\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_10nsvzs_di\" bpmnElement=\"SequenceFlow_10nsvzs\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1805\" y=\"-177\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1805\" y=\"-515\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1696\" y=\"-515\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1781\" y=\"-535\" width=\"88\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0xqu1s7_di\" bpmnElement=\"SequenceFlow_0xqu1s7\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1015\" y=\"-446\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1056\" y=\"-446\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1056\" y=\"-449\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1097\" y=\"-449\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1071\" y=\"-447.5\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0p9r9wf_di\" bpmnElement=\"SequenceFlow_0p9r9wf\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3912\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3961\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3961\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4002\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3918\" y=\"-448\" width=\"45\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_152tq2c_di\" bpmnElement=\"SequenceFlow_152tq2c\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4102\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4188\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4145\" y=\"-444\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0b8zlhx_di\" bpmnElement=\"SequenceFlow_0b8zlhx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4238\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4355\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4280\" y=\"-447\" width=\"45\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_01s02ho_di\" bpmnElement=\"SequenceFlow_01s02ho\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2662\" y=\"-581\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2487\" y=\"-620\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2542\" y=\"-637\" width=\"56\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mr38k3_di\" bpmnElement=\"SequenceFlow_1mr38k3\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1943\" y=\"408\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1805\" y=\"408\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1805\" y=\"-127\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1892\" y=\"425\" width=\"37\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1o8wk2g_di\" bpmnElement=\"SequenceFlow_1o8wk2g\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4412\" y=\"-359\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4359\" y=\"-359\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4384\" y=\"-374\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_08g7218_di\" bpmnElement=\"EndEvent_08g7218\">\n        <dc:Bounds x=\"-2035\" y=\"6\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2017\" y=\"42\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1uw9qzb_di\" bpmnElement=\"UserTask_1uw9qzb\">\n        <dc:Bounds x=\"-2478\" y=\"-40\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0ib18ut_di\" bpmnElement=\"UserTask_0ib18ut\">\n        <dc:Bounds x=\"2760\" y=\"-547\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0t2jovj_di\" bpmnElement=\"ExclusiveGateway_0t2jovj\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"2926\" y=\"-532\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2951\" y=\"-482\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0xsau1t_di\" bpmnElement=\"UserTask_0xsau1t\">\n        <dc:Bounds x=\"3251\" y=\"-547\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1wkrl5k_di\" bpmnElement=\"UserTask_1wkrl5k\">\n        <dc:Bounds x=\"-1590\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0syren9_di\" bpmnElement=\"UserTask_0syren9\">\n        <dc:Bounds x=\"-1312\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_05esr0v_di\" bpmnElement=\"ExclusiveGateway_05esr0v\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-1156\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1131\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_12n8eyi_di\" bpmnElement=\"UserTask_12n8eyi\">\n        <dc:Bounds x=\"-1007\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1sqtpex_di\" bpmnElement=\"ExclusiveGateway_1sqtpex\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-811\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-786\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1n39kzy_di\" bpmnElement=\"UserTask_1n39kzy\">\n        <dc:Bounds x=\"-670\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0xdedru_di\" bpmnElement=\"ExclusiveGateway_0xdedru\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-477\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-452\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_14yc5q6_di\" bpmnElement=\"UserTask_14yc5q6\">\n        <dc:Bounds x=\"-349\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1g0uit4_di\" bpmnElement=\"UserTask_1g0uit4\">\n        <dc:Bounds x=\"581\" y=\"-490\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1hidl6i_di\" bpmnElement=\"EndEvent_1hidl6i\">\n        <dc:Bounds x=\"889\" y=\"-468\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"907\" y=\"-432\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0rj3nbv_di\" bpmnElement=\"UserTask_0rj3nbv\">\n        <dc:Bounds x=\"1483\" y=\"-496\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1h23ev2_di\" bpmnElement=\"EndEvent_1h23ev2\">\n        <dc:Bounds x=\"1992\" y=\"-474\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2010\" y=\"-438\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ServiceTask_0yrq8kf_di\" bpmnElement=\"ServiceTask_0yrq8kf\">\n        <dc:Bounds x=\"1846\" y=\"-496\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1l2kum0_di\" bpmnElement=\"ExclusiveGateway_1l2kum0\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2180\" y=\"518\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2155\" y=\"494\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_0b9g5de_di\" bpmnElement=\"EndEvent_0b9g5de\">\n        <dc:Bounds x=\"-2173\" y=\"406\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2155\" y=\"442\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1hxowlo_di\" bpmnElement=\"EndEvent_1hxowlo\">\n        <dc:Bounds x=\"-2068\" y=\"525\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2050\" y=\"561\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1ru64f6_di\" bpmnElement=\"UserTask_1ru64f6\">\n        <dc:Bounds x=\"-4195\" y=\"-366\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_11b2osi_di\" bpmnElement=\"UserTask_11b2osi\">\n        <dc:Bounds x=\"-4005\" y=\"-366\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1qf7rmc_di\" bpmnElement=\"UserTask_1qf7rmc\">\n        <dc:Bounds x=\"-3156\" y=\"-366\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1j6sg6y_di\" bpmnElement=\"ExclusiveGateway_1j6sg6y\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2888\" y=\"-464\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2863\" y=\"-414\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_0hukegx_di\" bpmnElement=\"EndEvent_0hukegx\">\n        <dc:Bounds x=\"-2759\" y=\"-457\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2741\" y=\"-421\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0ur6mix_di\" bpmnElement=\"SequenceFlow_0ur6mix\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2378\" y=\"-20\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2329\" y=\"-20\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2227\" y=\"-20\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2227\" y=\"-1\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2278\" y=\"-35\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mz65k1_di\" bpmnElement=\"SequenceFlow_1mz65k1\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2860\" y=\"-507\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2926\" y=\"-507\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2893\" y=\"-532\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_07r408i_di\" bpmnElement=\"SequenceFlow_07r408i\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2976\" y=\"-507\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3251\" y=\"-507\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3073\" y=\"-525\" width=\"46\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0ycbsvn_di\" bpmnElement=\"SequenceFlow_0ycbsvn\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1131\" y=\"-435\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1131\" y=\"-385\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1540\" y=\"-385\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1540\" y=\"-420\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1357\" y=\"-405\" width=\"40\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ppjvpj_di\" bpmnElement=\"SequenceFlow_1ppjvpj\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-786\" y=\"-435\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-786\" y=\"-369\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1556\" y=\"-369\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1556\" y=\"-420\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-891\" y=\"-387\" width=\"40\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1uudu22_di\" bpmnElement=\"SequenceFlow_1uudu22\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1490\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1438\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1464\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_10gy0d3_di\" bpmnElement=\"SequenceFlow_10gy0d3\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1212\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1156\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1184\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1nqm4yd_di\" bpmnElement=\"SequenceFlow_1nqm4yd\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1106\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1007\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1084\" y=\"-479\" width=\"46\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1y9us7r_di\" bpmnElement=\"SequenceFlow_1y9us7r\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-452\" y=\"-435\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-452\" y=\"-357\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-957\" y=\"-357\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-957\" y=\"-420\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-723\" y=\"-378\" width=\"40\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_19vvor0_di\" bpmnElement=\"SequenceFlow_19vvor0\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-907\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-811\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-859\" y=\"-485\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0uczqhk_di\" bpmnElement=\"SequenceFlow_0uczqhk\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-761\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-670\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-735\" y=\"-477\" width=\"46\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1s0pti2_di\" bpmnElement=\"SequenceFlow_1s0pti2\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-570\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-477\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-522\" y=\"-485\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_18hclu9_di\" bpmnElement=\"SequenceFlow_18hclu9\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-427\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-349\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-409\" y=\"-475\" width=\"46\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_05kk3pj_di\" bpmnElement=\"SequenceFlow_05kk3pj\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1946\" y=\"-456\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1992\" y=\"-456\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1969\" y=\"-471\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1r69son_di\" bpmnElement=\"SequenceFlow_1r69son\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2130\" y=\"543\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2068\" y=\"543\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2120\" y=\"524\" width=\"40\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Association_17u0nhw_di\" bpmnElement=\"Association_17u0nhw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2138\" y=\"420\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2106\" y=\"275\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_00vcuxx_di\" bpmnElement=\"SequenceFlow_00vcuxx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4108\" y=\"-286\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4063\" y=\"-236\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4114\" y=\"-275\" width=\"57\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1wqdne7_di\" bpmnElement=\"SequenceFlow_1wqdne7\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2838\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2797\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2797\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2759\" y=\"-439\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2841\" y=\"-461\" width=\"46\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1hoa515_di\" bpmnElement=\"SequenceFlow_1hoa515\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2863\" y=\"-464\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2863\" y=\"-565\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4145\" y=\"-565\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4145\" y=\"-366\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2911\" y=\"-517\" width=\"39\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mm8ziz_di\" bpmnElement=\"SequenceFlow_1mm8ziz\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2155\" y=\"568\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2155\" y=\"654\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2130\" y=\"598\" width=\"52\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ss7wlc_di\" bpmnElement=\"SequenceFlow_1ss7wlc\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"464\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"526\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2337\" y=\"526\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2413\" y=\"495\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0mxowg3_di\" bpmnElement=\"SequenceFlow_0mxowg3\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2237\" y=\"543\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2180\" y=\"543\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2208\" y=\"528\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"UserTask_1ec0v1e_di\" bpmnElement=\"Task_0s5v6wl\">\n        <dc:Bounds x=\"-2337\" y=\"503\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1qu5j9t_di\" bpmnElement=\"SequenceFlow_1qu5j9t\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2155\" y=\"518\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2155\" y=\"442\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2139\" y=\"472\" width=\"71\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"StartEvent_093jrj6_di\" bpmnElement=\"StartEvent_093jrj6\">\n        <dc:Bounds x=\"-2446\" y=\"309.7273954116059\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2428\" y=\"345.7273954116059\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mjhudh_di\" bpmnElement=\"SequenceFlow_1mjhudh\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"346\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"384\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2413\" y=\"365\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1i35baw_di\" bpmnElement=\"SequenceFlow_1i35baw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2205\" y=\"694\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2287\" y=\"694\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2287\" y=\"583\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2246\" y=\"679\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"StartEvent_0tbvhus_di\" bpmnElement=\"StartEvent_0tbvhus\">\n        <dc:Bounds x=\"-1671\" y=\"-478\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1653\" y=\"-442\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1sjo7n5_di\" bpmnElement=\"SequenceFlow_1sjo7n5\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1635\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1590\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1612\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_15lrjyx_di\" bpmnElement=\"TextAnnotation_15lrjyx\">\n        <dc:Bounds x=\"-211\" y=\"-722\" width=\"165\" height=\"60\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0g767fd_di\" bpmnElement=\"TextAnnotation_0g767fd\">\n        <dc:Bounds x=\"-177\" y=\"-332\" width=\"212\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"StartEvent_14gn11t_di\" bpmnElement=\"StartEvent_14gn11t\">\n        <dc:Bounds x=\"2612\" y=\"-636\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2630\" y=\"-600\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0d0znog_di\" bpmnElement=\"SequenceFlow_0d0znog\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2647\" y=\"-615\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3251\" y=\"-527\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2904\" y=\"-586\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"StartEvent_00in2ji_di\" bpmnElement=\"StartEvent_00in2ji\">\n        <dc:Bounds x=\"473\" y=\"-468\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"491\" y=\"-432\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0lh9xfe_di\" bpmnElement=\"SequenceFlow_0lh9xfe\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"509\" y=\"-450\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"581\" y=\"-450\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"545\" y=\"-465\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"StartEvent_1xcw8jg_di\" bpmnElement=\"StartEvent_1xcw8jg\">\n        <dc:Bounds x=\"-4294\" y=\"-344\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4276\" y=\"-308\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0xr8fnm_di\" bpmnElement=\"SequenceFlow_0xr8fnm\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4258\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4195\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4226\" y=\"-341\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_17n684j_di\" bpmnElement=\"SequenceFlow_17n684j\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2304\" y=\"218\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2304\" y=\"289\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2317\" y=\"260\" width=\"63\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0gbl31i_di\" bpmnElement=\"ExclusiveGateway_0gbl31i\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2346\" y=\"-415\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2321\" y=\"-365\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0jidb3c_di\" bpmnElement=\"SequenceFlow_0jidb3c\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2321\" y=\"-365\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2321\" y=\"-223\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2321\" y=\"-223\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2321\" y=\"-79\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2359\" y=\"-171\" width=\"79\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0xhyefk_di\" bpmnElement=\"SequenceFlow_0xhyefk\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2296\" y=\"-390\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1888\" y=\"-390\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1888\" y=\"323\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1943\" y=\"323\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2262\" y=\"-427\" width=\"57\" height=\"26\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"StartEvent_0tw26xn_di\" bpmnElement=\"StartEvent_0tw26xn\">\n        <dc:Bounds x=\"-2655\" y=\"53\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2637\" y=\"89\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1lir1ve_di\" bpmnElement=\"SequenceFlow_1lir1ve\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2619\" y=\"71\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2573\" y=\"71\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2596\" y=\"56\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SubProcess_0v7hq1m_di\" bpmnElement=\"SubProcess_0v7hq1m\" isExpanded=\"true\">\n        <dc:Bounds x=\"3568\" y=\"-671\" width=\"1201\" height=\"449\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0fkgaq0_di\" bpmnElement=\"SequenceFlow_0fkgaq0\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3887\" y=\"-454\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3887\" y=\"-524\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3906\" y=\"-491\" width=\"39\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_061k4i3_di\" bpmnElement=\"SequenceFlow_061k4i3\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4213\" y=\"-454\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4213\" y=\"-516\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4230\" y=\"-488\" width=\"39\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_0ozmh12_di\" bpmnElement=\"EndEvent_0ozmh12\">\n        <dc:Bounds x=\"3869\" y=\"-560\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3887\" y=\"-524\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1aget54_di\" bpmnElement=\"EndEvent_1aget54\">\n        <dc:Bounds x=\"4195\" y=\"-552\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4213\" y=\"-516\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"StartEvent_1g9cwh9_di\" bpmnElement=\"StartEvent_1g9cwh9\">\n        <dc:Bounds x=\"3628\" y=\"-447\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3646\" y=\"-411\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1oqa52r_di\" bpmnElement=\"SequenceFlow_1oqa52r\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3664\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3686\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3686\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3710\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3701\" y=\"-429\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_1pp7st1_di\" bpmnElement=\"EndEvent_1pp7st1\">\n        <dc:Bounds x=\"5129\" y=\"-430\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"5147\" y=\"-394\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"TextAnnotation_189rkq0_di\" bpmnElement=\"TextAnnotation_189rkq0\">\n        <dc:Bounds x=\"2615\" y=\"-877\" width=\"278\" height=\"57\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"StartEvent_18vp8g4_di\" bpmnElement=\"StartEvent_18vp8g4\">\n        <dc:Bounds x=\"1217\" y=\"-474\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1235\" y=\"-438\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1tfcopp_di\" bpmnElement=\"SequenceFlow_1tfcopp\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1253\" y=\"-456\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1483\" y=\"-456\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1368\" y=\"-471\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0hzejdn_di\" bpmnElement=\"TextAnnotation_0hzejdn\">\n        <dc:Bounds x=\"-4545\" y=\"-88\" width=\"311\" height=\"78\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_13cbx79_di\" bpmnElement=\"Association_13cbx79\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4180\" y=\"-286\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4354\" y=\"-88\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0qmnnga_di\" bpmnElement=\"ExclusiveGateway_0qmnnga\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-211\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-186\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1puv934_di\" bpmnElement=\"SequenceFlow_1puv934\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-249\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-211\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-230\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0x5m8bx_di\" bpmnElement=\"SequenceFlow_0x5m8bx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-186\" y=\"-435\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-186\" y=\"-319\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1566\" y=\"-319\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1566\" y=\"-420\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-171\" y=\"-413\" width=\"89\" height=\"49\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_1nde4uy_di\" bpmnElement=\"EndEvent_1nde4uy\">\n        <dc:Bounds x=\"317\" y=\"-478\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"335\" y=\"-442\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1g2fm4f_di\" bpmnElement=\"SequenceFlow_1g2fm4f\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-161\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-105\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-179\" y=\"-482\" width=\"90\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1yyzaop_di\" bpmnElement=\"ExclusiveGateway_1yyzaop\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"756\" y=\"-475\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"781\" y=\"-425\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_12505uo_di\" bpmnElement=\"SequenceFlow_12505uo\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"681\" y=\"-450\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"756\" y=\"-450\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"719\" y=\"-465\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mhjbfu_di\" bpmnElement=\"SequenceFlow_1mhjbfu\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"806\" y=\"-450\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"889\" y=\"-450\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"820\" y=\"-468\" width=\"53\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_0qyxs4e_di\" bpmnElement=\"EndEvent_0qyxs4e\">\n        <dc:Bounds x=\"763\" y=\"-358\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"781\" y=\"-322\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1hgha1n_di\" bpmnElement=\"SequenceFlow_1hgha1n\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"781\" y=\"-425\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"781\" y=\"-358\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"790\" y=\"-409\" width=\"84\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1p8u37f_di\" bpmnElement=\"SequenceFlow_1p8u37f\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"683\" y=\"-594\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"683\" y=\"-822\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"233\" y=\"-822\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"233\" y=\"-750\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"688\" y=\"-652\" width=\"85\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_176pq00_di\" bpmnElement=\"ExclusiveGateway_176pq00\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"1654\" y=\"-481\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1679\" y=\"-431\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1je1s10_di\" bpmnElement=\"SequenceFlow_1je1s10\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1583\" y=\"-456\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1654\" y=\"-456\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1619\" y=\"-471\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0x1dmwr_di\" bpmnElement=\"SequenceFlow_0x1dmwr\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1703.562685093781\" y=\"-456\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1846\" y=\"-456\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1730\" y=\"-499\" width=\"80\" height=\"36\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0w7it18_di\" bpmnElement=\"TextAnnotation_0w7it18\">\n        <dc:Bounds x=\"3222\" y=\"-826\" width=\"147\" height=\"82\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_0cg0efk_di\" bpmnElement=\"Association_0cg0efk\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3300\" y=\"-547\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3296\" y=\"-744\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1jx9hwo_di\" bpmnElement=\"ExclusiveGateway_1jx9hwo\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"3276\" y=\"-411\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3301\" y=\"-361\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1jobgdv_di\" bpmnElement=\"SequenceFlow_1jobgdv\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3301\" y=\"-467\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3301\" y=\"-411\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3316\" y=\"-439\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_18k9n2u_di\" bpmnElement=\"EndEvent_18k9n2u\">\n        <dc:Bounds x=\"3283\" y=\"-303\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3301\" y=\"-267\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_16pxtx8_di\" bpmnElement=\"SequenceFlow_16pxtx8\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3301\" y=\"-361\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3301\" y=\"-303\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3305\" y=\"-350\" width=\"49\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_10rgai8_di\" bpmnElement=\"SequenceFlow_10rgai8\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3276\" y=\"-386\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3159\" y=\"-386\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3196\" y=\"-405\" width=\"64\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_1lwuspp_di\" bpmnElement=\"EndEvent_1lwuspp\">\n        <dc:Bounds x=\"3091\" y=\"-303\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3109\" y=\"-267\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1fnfk0m_di\" bpmnElement=\"SequenceFlow_1fnfk0m\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3109\" y=\"-346\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3109\" y=\"-303\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3124\" y=\"-324.5\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_1bmypeh_di\" bpmnElement=\"TextAnnotation_1bmypeh\">\n        <dc:Bounds x=\"2870\" y=\"-806\" width=\"165\" height=\"114\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_1dyvnoq_di\" bpmnElement=\"Association_1dyvnoq\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3093\" y=\"-426\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2977\" y=\"-692\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1lwdiem_di\" bpmnElement=\"SequenceFlow_1lwdiem\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4169\" y=\"-222\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4169\" y=\"-125\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1668\" y=\"-125\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1668\" y=\"-205\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4177\" y=\"-192\" width=\"57\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_04juvza_di\" bpmnElement=\"TextAnnotation_04juvza\">\n        <dc:Bounds x=\"-2477.861796643633\" y=\"-313\" width=\"100\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_1r45mq2_di\" bpmnElement=\"Association_1r45mq2\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"-40\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2428\" y=\"-283\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_1hxbqie_di\" bpmnElement=\"EndEvent_1hxbqie\">\n        <dc:Bounds x=\"-1149\" y=\"-686\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1131\" y=\"-650\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1jwddwt_di\" bpmnElement=\"SequenceFlow_1jwddwt\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1131\" y=\"-485\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1131\" y=\"-650\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1127\" y=\"-519\" width=\"65\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mrjxgx_di\" bpmnElement=\"SequenceFlow_1mrjxgx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-786\" y=\"-485\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-786\" y=\"-668\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1113\" y=\"-668\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-782\" y=\"-512\" width=\"65\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1klmxot_di\" bpmnElement=\"SequenceFlow_1klmxot\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-452\" y=\"-485\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-452\" y=\"-668\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1113\" y=\"-668\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-445\" y=\"-512\" width=\"65\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0cztiwt_di\" bpmnElement=\"ExclusiveGateway_0cztiwt\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-1438\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1413\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_12vbsjq_di\" bpmnElement=\"SequenceFlow_12vbsjq\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1388\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1353\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1353\" y=\"-565\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-957\" y=\"-565\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-957\" y=\"-500\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1213\" y=\"-584.0193836278962\" width=\"57\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_087eljg_di\" bpmnElement=\"SequenceFlow_087eljg\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1413\" y=\"-485\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1413\" y=\"-668\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1149\" y=\"-668\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1409\" y=\"-521\" width=\"65\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0b217tv_di\" bpmnElement=\"SequenceFlow_0b217tv\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1606\" y=\"-750\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-1606\" y=\"-841\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2890\" y=\"-841\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2890\" y=\"-634\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2284\" y=\"-863\" width=\"65\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0x3smuu_di\" bpmnElement=\"ExclusiveGateway_0x3smuu\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2252\" y=\"-1\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2227\" y=\"49\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1i5ejbh_di\" bpmnElement=\"SequenceFlow_1i5ejbh\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2202\" y=\"24\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2035\" y=\"24\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2152\" y=\"7\" width=\"61\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0eg1f85_di\" bpmnElement=\"SequenceFlow_0eg1f85\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2536\" y=\"-79\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2536\" y=\"-104\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2893\" y=\"-104\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2893\" y=\"-125\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2764\" y=\"-90\" width=\"90\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_02g9e0x_di\" bpmnElement=\"EndEvent_02g9e0x\">\n        <dc:Bounds x=\"1661\" y=\"-557.4876604146101\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1679\" y=\"-521.4876604146101\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1x1uq5y_di\" bpmnElement=\"SequenceFlow_1x1uq5y\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1679\" y=\"-481\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1679\" y=\"-521\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1610\" y=\"-510.19047619047615\" width=\"65\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1hw1xm0_di\" bpmnElement=\"SequenceFlow_1hw1xm0\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1668\" y=\"-693\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1668\" y=\"-892\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3508\" y=\"-892\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3508\" y=\"-634\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1681\" y=\"-808\" width=\"77\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_0ze2bsp_di\" bpmnElement=\"EndEvent_0ze2bsp\">\n        <dc:Bounds x=\"2933\" y=\"-404\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2951\" y=\"-368\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_00qbbhz_di\" bpmnElement=\"SequenceFlow_00qbbhz\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2951\" y=\"-482\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2951\" y=\"-404\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2961\" y=\"-467\" width=\"72\" height=\"36\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0jvqwqj_di\" bpmnElement=\"SequenceFlow_0jvqwqj\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2726\" y=\"-671\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2726\" y=\"-793\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2007\" y=\"-793\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2007\" y=\"-693\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2349\" y=\"-834\" width=\"70\" height=\"36\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_04ms90t_di\" bpmnElement=\"SequenceFlow_04ms90t\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1679\" y=\"-431\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1679\" y=\"-375.61481481481474\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1599\" y=\"-420.5534814814814\" width=\"78\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_1utuq8j_di\" bpmnElement=\"EndEvent_1utuq8j\">\n        <dc:Bounds x=\"1661\" y=\"-375.61481481481474\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1679\" y=\"-339.61481481481474\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_05vqen4_di\" bpmnElement=\"SequenceFlow_05vqen4\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1467\" y=\"-693\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1467\" y=\"-853\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"199\" y=\"-853\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"199\" y=\"-750\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"1477\" y=\"-806\" width=\"66\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_01n47ri_di\" bpmnElement=\"SequenceFlow_01n47ri\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5071\" y=\"-412\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5102\" y=\"-412\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5102\" y=\"-412\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5129\" y=\"-412\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"5117\" y=\"-412\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_14g54mo_di\" bpmnElement=\"TextAnnotation_14g54mo\">\n        <dc:Bounds x=\"-2462\" y=\"588\" width=\"145\" height=\"33\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_1jg82nu_di\" bpmnElement=\"Association_1jg82nu\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2337\" y=\"573\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2362\" y=\"588\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_0ytf71m_di\" bpmnElement=\"EndEvent_03do2v0\">\n        <dc:Bounds x=\"-2939\" y=\"-203\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2921\" y=\"-167\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SendTask_1j0jhn5_di\" bpmnElement=\"Task_0y93m8z\">\n        <dc:Bounds x=\"4971\" y=\"-452\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1fo49fj_di\" bpmnElement=\"EndEvent_1fo49fj\">\n        <dc:Bounds x=\"-3528\" y=\"-39\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3510\" y=\"-3\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_062z6hr_di\" bpmnElement=\"SequenceFlow_062z6hr\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3510\" y=\"-125\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3510\" y=\"-39\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3494\" y=\"-48\" width=\"39\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"EndEvent_02t2w1s_di\" bpmnElement=\"EndEvent_02t2w1s\">\n        <dc:Bounds x=\"-2990.133079847909\" y=\"504\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2972\" y=\"540\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1o8iany_di\" bpmnElement=\"SequenceFlow_1o8iany\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2557\" y=\"522\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2724\" y=\"522\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2685.5\" y=\"507\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ovjrsn_di\" bpmnElement=\"SequenceFlow_1ovjrsn\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2824\" y=\"522\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2954\" y=\"522\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2934\" y=\"507\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_125e0rj_di\" bpmnElement=\"ExclusiveGateway_125e0rj\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-3741\" y=\"-351\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3716\" y=\"-301\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1p9994w_di\" bpmnElement=\"SequenceFlow_1p9994w\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3691\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3605\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3678\" y=\"-372\" width=\"59\" height=\"36\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_056em9e_di\" bpmnElement=\"SequenceFlow_056em9e\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3716\" y=\"-301\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3716\" y=\"-225\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3309\" y=\"-225\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3309\" y=\"-301\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3551\" y=\"-240\" width=\"77\" height=\"36\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1of3ayp_di\" bpmnElement=\"ExclusiveGateway_1of3ayp\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-3440\" y=\"-351\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3414\" y=\"-301\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0uqg7t2_di\" bpmnElement=\"SequenceFlow_0uqg7t2\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3505\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3440\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3472\" y=\"-341\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_07gu6kr_di\" bpmnElement=\"SequenceFlow_07gu6kr\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3390\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3334\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3384\" y=\"-352\" width=\"46\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0aqvpql_di\" bpmnElement=\"ExclusiveGateway_0aqvpql\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-3862\" y=\"-351\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3836\" y=\"-301\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0bhtmw4_di\" bpmnElement=\"SequenceFlow_0bhtmw4\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3905\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3862\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3883\" y=\"-341\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1troz3f_di\" bpmnElement=\"SequenceFlow_1troz3f\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3812\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3741\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3802\" y=\"-350\" width=\"46\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SendTask_0mh2pym_di\" bpmnElement=\"Notify_Cant_Fix_JR\">\n        <dc:Bounds x=\"-3195\" y=\"-225\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1tkoxjb_di\" bpmnElement=\"SequenceFlow_1tkoxjb\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3837\" y=\"-301\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3837\" y=\"-185\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3195\" y=\"-185\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3825\" y=\"-253\" width=\"37\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_05dm0js_di\" bpmnElement=\"SequenceFlow_05dm0js\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3095\" y=\"-185\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2939\" y=\"-185\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3017\" y=\"-200\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1awdwuw_di\" bpmnElement=\"SequenceFlow_1awdwuw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2227\" y=\"49\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2227\" y=\"104\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2256\" y=\"77\" width=\"87\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ywl9mw_di\" bpmnElement=\"SequenceFlow_1ywl9mw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2177\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2106\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2106\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2035\" y=\"143\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2091\" y=\"143\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SendTask_0mxszrn_di\" bpmnElement=\"Task_18z71ln\">\n        <dc:Bounds x=\"-2277\" y=\"104\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_19zbppn_di\" bpmnElement=\"ExclusiveGateway_19zbppn\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2573\" y=\"46\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2548\" y=\"96\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_18iro5d_di\" bpmnElement=\"Task_1xhzfxw\">\n        <dc:Bounds x=\"-3605\" y=\"-366\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_0rcvgvk_di\" bpmnElement=\"Task_0euindd\">\n        <dc:Bounds x=\"-2478\" y=\"103\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0k5dp8v_di\" bpmnElement=\"SequenceFlow_0k5dp8v\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2548\" y=\"46\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2548\" y=\"0\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2478\" y=\"0\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2584\" y=\"-55\" width=\"71\" height=\"38\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1i0x6bl_di\" bpmnElement=\"SequenceFlow_1i0x6bl\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2548\" y=\"96\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2548\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2478\" y=\"143\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2587\" y=\"149\" width=\"80\" height=\"38\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_14draw4_di\" bpmnElement=\"SequenceFlow_14draw4\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2378\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2326\" y=\"143\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2326\" y=\"24\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2252\" y=\"24\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2311\" y=\"84\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SendTask_16248ju_di\" bpmnElement=\"ScriptTask_0k2vqwv\">\n        <dc:Bounds x=\"-2478\" y=\"384\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SendTask_1t31fpk_di\" bpmnElement=\"ScriptTask_0h526ls\">\n        <dc:Bounds x=\"-2205\" y=\"654\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ip3b6e_di\" bpmnElement=\"SequenceFlow_1ip3b6e\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3837\" y=\"-351\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3837\" y=\"-443\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4112\" y=\"-443\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4112\" y=\"-368\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3835\" y=\"-410\" width=\"39\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0ifgbkt_di\" bpmnElement=\"SequenceFlow_0ifgbkt\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3415\" y=\"-351\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3415\" y=\"-491\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4158\" y=\"-491\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4158\" y=\"-368\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3806\" y=\"-506\" width=\"39\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"IntermediateThrowEvent_1433yh8_di\" bpmnElement=\"EndEvent_0zv5f6j\">\n        <dc:Bounds x=\"-2035\" y=\"125\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2017\" y=\"161\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0sngnpv_di\" bpmnElement=\"TextAnnotation_0sngnpv\">\n        <dc:Bounds x=\"-438\" y=\"-623\" width=\"245\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_0aeot7v_di\" bpmnElement=\"Association_0aeot7v\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-303\" y=\"-500\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-313\" y=\"-593\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mvqaxr_di\" bpmnElement=\"SequenceFlow_1mvqaxr\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-5\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"36\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"16\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1tbf21w_di\" bpmnElement=\"SequenceFlow_1tbf21w\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"260\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"317\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"289\" y=\"-475\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_1x0cs8g_di\" bpmnElement=\"ExclusiveGateway_1x0cs8g\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"36\" y=\"-485\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"61\" y=\"-435\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1aovmnd_di\" bpmnElement=\"SequenceFlow_1aovmnd\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"86\" y=\"-460\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"160\" y=\"-460\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"71\" y=\"-496\" width=\"74\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1cr5ksn_di\" bpmnElement=\"SequenceFlow_1cr5ksn\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"61\" y=\"-485\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"61\" y=\"-557\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-299\" y=\"-557\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-299\" y=\"-502\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-164\" y=\"-572\" width=\"89\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1951c3w_di\" bpmnElement=\"SequenceFlow_1951c3w\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"390\" y=\"-459\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"444\" y=\"-459\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"417\" y=\"-474\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0zdrz4s_di\" bpmnElement=\"TextAnnotation_0zdrz4s\">\n        <dc:Bounds x=\"1320\" y=\"-579\" width=\"230\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_0i9c9uk_di\" bpmnElement=\"Association_0i9c9uk\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1497\" y=\"-496\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1449\" y=\"-549\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1sfkegw_di\" bpmnElement=\"SequenceFlow_1sfkegw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2237\" y=\"-449\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2537\" y=\"-449\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"2335\" y=\"-483\" width=\"66\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0go8umh_di\" bpmnElement=\"SequenceFlow_0go8umh\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3492\" y=\"-446\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3568\" y=\"-446\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3517\" y=\"-471\" width=\"46\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0fhdfes_di\" bpmnElement=\"SequenceFlow_0fhdfes\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4769\" y=\"-412\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4971\" y=\"-412\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4825\" y=\"-432\" width=\"45\" height=\"13\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"UserTask_0gbjxt6_di\" bpmnElement=\"Task_1kwxxw1\">\n        <dc:Bounds x=\"4355\" y=\"-470\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_00yqrvw_di\" bpmnElement=\"ExclusiveGateway_00yqrvw\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"4538\" y=\"-454\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4563\" y=\"-404\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1j4zio7_di\" bpmnElement=\"SequenceFlow_1j4zio7\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4588\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4653\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4653\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4713\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4621\" y=\"-429\" width=\"83\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1ea0261_di\" bpmnElement=\"SequenceFlow_1ea0261\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4455\" y=\"-430\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4538\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4497\" y=\"-444.5\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_1d1kzxj_di\" bpmnElement=\"TextAnnotation_1d1kzxj\">\n        <dc:Bounds x=\"3074\" y=\"-614\" width=\"211\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_1e0wo8a_di\" bpmnElement=\"Association_1e0wo8a\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3256\" y=\"-542\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3200\" y=\"-584\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SendTask_1j75kb5_di\" bpmnElement=\"Task_09uy9fl\">\n        <dc:Bounds x=\"3059\" y=\"-426\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0paiubf_di\" bpmnElement=\"SequenceFlow_0paiubf\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4328\" y=\"-222\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4328\" y=\"70\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5021\" y=\"70\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"5021\" y=\"-372\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4288\" y=\"-88\" width=\"86\" height=\"26\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"TextAnnotation_0xakxcy_di\" bpmnElement=\"TextAnnotation_0xakxcy\">\n        <dc:Bounds x=\"2901\" y=\"-313.4570582428431\" width=\"100\" height=\"30\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Association_1xvmvrq_di\" bpmnElement=\"Association_1xvmvrq\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3059\" y=\"-358\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"2979\" y=\"-313\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1gr7bbx_di\" bpmnElement=\"SequenceFlow_1gr7bbx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4563\" y=\"-404\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4563\" y=\"-366\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4563\" y=\"-366\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4563\" y=\"-314\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4509\" y=\"-389.76203180364706\" width=\"89\" height=\"25\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1mn2lcw_di\" bpmnElement=\"SequenceFlow_1mn2lcw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4513\" y=\"-274\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4422\" y=\"-274\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4422\" y=\"-274\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"4344\" y=\"-274\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4438\" y=\"-274\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"SendTask_16wr58n_di\" bpmnElement=\"Task_1vniyf0\">\n        <dc:Bounds x=\"4513\" y=\"-314\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1o9e13l_di\" bpmnElement=\"SequenceFlow_1o9e13l\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3810\" y=\"-429\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"3862\" y=\"-429\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"3836\" y=\"-454\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"UserTask_09pj0pg_di\" bpmnElement=\"Task_0jxwgbt\">\n        <dc:Bounds x=\"-105\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"EndEvent_1h89z2r_di\" bpmnElement=\"EndEvent_1h89z2r\">\n        <dc:Bounds x=\"4308\" y=\"-292\" width=\"36\" height=\"36\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"4326\" y=\"-256\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1qksldt_di\" bpmnElement=\"UserTask_1qksldt\">\n        <dc:Bounds x=\"-3284\" y=\"-479\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"UserTask_1tpn4q3_di\" bpmnElement=\"UserTask_1tpn4q3\">\n        <dc:Bounds x=\"-3032\" y=\"-479\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_06g0jl8_di\" bpmnElement=\"ExclusiveGateway_06g0jl8\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-3334\" y=\"-351\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3308\" y=\"-301\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0tunbpb_di\" bpmnElement=\"SequenceFlow_0tunbpb\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3284\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3156\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3220\" y=\"-341\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0s4pkfn_di\" bpmnElement=\"SequenceFlow_0s4pkfn\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3309\" y=\"-351\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3309\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3284\" y=\"-439\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3347\" y=\"-473\" width=\"75\" height=\"24\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1pq5xas_di\" bpmnElement=\"SequenceFlow_1pq5xas\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2932\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2888\" y=\"-439\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2910\" y=\"-454\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_059gfjr_di\" bpmnElement=\"ExclusiveGateway_059gfjr\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-3131\" y=\"-464\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3106\" y=\"-414\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1856psw_di\" bpmnElement=\"SequenceFlow_1856psw\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3184\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3131\" y=\"-439\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3157\" y=\"-454\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0c7fdul_di\" bpmnElement=\"SequenceFlow_0c7fdul\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3081\" y=\"-439\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3032\" y=\"-439\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3080\" y=\"-454\" width=\"47\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1eg1vhx_di\" bpmnElement=\"SequenceFlow_1eg1vhx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3106\" y=\"-464\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3106\" y=\"-514\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4131\" y=\"-514\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4131\" y=\"-366\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3642\" y=\"-529\" width=\"43\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0ejyp7l_di\" bpmnElement=\"ExclusiveGateway_0ejyp7l\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2888\" y=\"-351\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2862\" y=\"-301\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0n4aumv_di\" bpmnElement=\"SequenceFlow_0n4aumv\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-3056\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2887\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2971\" y=\"-351\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_03otu34_di\" bpmnElement=\"SequenceFlow_03otu34\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2838\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2741\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2741\" y=\"-421\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2813\" y=\"-341\" width=\"47\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_068h5f0_di\" bpmnElement=\"SequenceFlow_068h5f0\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2863\" y=\"-302\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2863\" y=\"-263\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2698\" y=\"-263\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2698\" y=\"-601\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4176\" y=\"-601\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4176\" y=\"-367\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2705\" y=\"-432\" width=\"43\" height=\"12\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ServiceTask_1bvwtle_di\" bpmnElement=\"Task_0weut1t\">\n        <dc:Bounds x=\"160\" y=\"-500\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0gs09yt_di\" bpmnElement=\"ExclusiveGateway_0gs09yt\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-4076\" y=\"-248\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4051\" y=\"-198\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0k8qm6w_di\" bpmnElement=\"SequenceFlow_0k8qm6w\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4051\" y=\"-248\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4051\" y=\"-326\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4005\" y=\"-326\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-4036\" y=\"-297\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1p9psxx_di\" bpmnElement=\"SequenceFlow_1p9psxx\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4051\" y=\"-199\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-4051\" y=\"115\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2904\" y=\"115\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2746\" y=\"-422\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-3477\" y=\"100\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ExclusiveGateway_0ibfmmi_di\" bpmnElement=\"ExclusiveGateway_0ibfmmi\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"-2491.992307692308\" y=\"-650.3615384615384\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2466\" y=\"-600.3615384615384\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1w6xl96_di\" bpmnElement=\"SequenceFlow_1w6xl96\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-600\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-524\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2497\" y=\"-562\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_0d3icf7_di\" bpmnElement=\"SequenceFlow_0d3icf7\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-650\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-1462\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-282\" y=\"-1462\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"1198\" y=\"-693\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-1374\" y=\"-1477\" width=\"0\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"SequenceFlow_1rfvru8_di\" bpmnElement=\"SequenceFlow_1rfvru8\">\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-444\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2467\" y=\"-390\" />\n        <di:waypoint xsi:type=\"dc:Point\" x=\"-2346\" y=\"-390\" />\n        <bpmndi:BPMNLabel>\n          <dc:Bounds x=\"-2497\" y=\"-417\" width=\"90\" height=\"0\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"ServiceTask_0hxdxvx_di\" bpmnElement=\"Task_11vrxiu\">\n        <dc:Bounds x=\"-2517\" y=\"-524\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"SendTask_16ww2hw_di\" bpmnElement=\"Task_1lcm554\">\n        <dc:Bounds x=\"-2824\" y=\"482\" width=\"100\" height=\"80\" />\n      </bpmndi:BPMNShape>\n    </bpmndi:BPMNPlane>\n  </bpmndi:BPMNDiagram>\n</bpmn:definitions>\n"
			}
		}
		function getTaskList(){
			filterService.list({itemCount:true,resoureType: 'Task'}, function(err, result){
				$scope.filters = result;
				if($scope.filters.length > 0 && $scope.currentFilter == undefined){
					$scope.currentFilter = $scope.filters[0];
				}
				loadTasks();
			});
		}
		$scope.selectFilter = function(filter){
			$scope.currentFilter = filter;
			loadTasks();
		}
		$scope.starProcess = function(id){
			processDefinitionService.start({id:id}, function(err, results){
				$scope.tryToOpen = results;
				getTaskList();
			});
		}
		$rootScope.logout = function(){
			AuthenticationService.logout();
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
		function loadTasks(e) {
			filterService.getTasks({id:$scope.currentFilter.id}, function(err, results){
				if (err) {
					throw err;
				}
				$scope.$apply(function() {
					$scope.tasks = results._embedded.task;
					var selectedTask;
					if($scope.tasks && $scope.tasks.length > 0){
						$scope.tasks.forEach(function(e){
							if(e.assignee){
								for(var i=0;i<results._embedded.assignee.length;i++){
									if(results._embedded.assignee[i].id === e.assignee){
										e.assigneeObject = results._embedded.assignee[i];
									}
								}
							}
							if($scope.tryToOpen && e.assignee === $rootScope.authentication.name && e.processInstanceId === $scope.tryToOpen.id){
								selectedTask = e;
							}
						});
					}
					if(selectedTask){
						$scope.loadTaskForm(selectedTask);
					} else if($scope.tryToOpen){
						console.log($scope.tryToOpen);
						taskService.get($scope.tryToOpen.id, function(err, taskResult){
							if(err){
								throw err;
								console.log(err);
							}
							$scope.tryToOpen = undefined;
							$scope.loadTaskForm(taskResult);
						})
					}
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
				console.log($scope);
				if($scope.kcell_form.$valid){
					camForm.submit(function (err) {
						if (err) {
							toasty.error({title: "Could not complete task", msg: err});
							e.preventDefault();
							throw err;
						} else {
							$('#taskElement').html('');
							getTaskList();
						}
					});
				} else {
					toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
				}
			});
			camForm.formElement.append($submitBtn);
		}
		$scope.loadTaskForm = function(task) {
			$scope.currentTask = task;
			$scope.view = {
				submitted: false
			}
			$rootScope.kcell_form = undefined;
			var taskId = task.id;
			$('#taskElement').html('');
			$scope.isHistoryOpen = false;
			if(task.assignee === $rootScope.authentication.name){
				taskService.form(taskId, function(err, taskFormInfo) {
					var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
					new CamSDK.Form({
						client: camClient,
						formUrl: url,
						taskId: taskId,
						containerElement: $('#taskElement'),
						done: addFormButton
					});
					console.log($scope);
				});
			} else {
				console.log("READ ONLY");
				taskService.form(taskId, function(err, taskFormInfo) {
					$scope.isHistoryOpen = true;
					var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
					new CamSDK.Form({
						client: camClient,
						formUrl: url,
						taskId: taskId,
						containerElement: $('#taskElement'),
						done: disableForm
					});
				});
			}
		}
		function disableForm(){
			$("[name=kcell_form]").css("pointer-events", "none");
			$("[name=kcell_form]").css("opacity", "0.4");
		}
		getTaskList();
		loadProcessDefinitions();

		$scope.claim = function(task) {
			taskService.claim({taskId: task.id, userId: $rootScope.authentication.name}, function(err, result){
				if (err) {
					throw err;
				}
				$scope.tryToOpen = {
					id: task.processInstanceId
				};
				getTaskList();
			});
		}
	}]);

	app.config(['$routeProvider', function($routeProvider){
		$routeProvider
		.when('/', {
			controller : 'mainCtrl',
			templateUrl : 'tasks.html',
			authentication: 'required'
		})
		.when('/login', {
			templateUrl: 'login.html',
			controller: 'LoginController'
		})
		.otherwise({
			redirectTo: '/'
		})
	}]);

	app.directive('camWidgetBpmnViewer', ['$q', '$document', '$compile', '$location', '$rootScope', 'search', 'debounce', function($q, $document, $compile,   $location,   $rootScope,   search, debounce) {

    return {
      scope: {
        diagramData: '=',
        control: '=?',
        disableNavigation: '&',
        onLoad: '&',
        onClick: '&',
        onMouseEnter: '&',
        onMouseLeave: '&'
      },

      templateUrl: './cam-widget-bpmn-viewer.html',

      link: function($scope, $element) {

        var definitions;
        console.log("IT WORKS");

        $scope.grabbing = false;

        // parse boolean
        $scope.disableNavigation = $scope.$eval($scope.disableNavigation);

        // --- CONTROL FUNCTIONS ---
        $scope.control = $scope.control || {};

        $scope.control.highlight = function(id) {
          canvas.addMarker(id, 'highlight');

          $element.find('[data-element-id="'+id+'"]>.djs-outline').attr({
            rx: '14px',
            ry: '14px'
          });
        };

        $scope.control.clearHighlight = function(id) {
          canvas.removeMarker(id, 'highlight');
        };

        $scope.control.isHighlighted = function(id) {
          return canvas.hasMarker(id, 'highlight');
        };

        // config: text, tooltip, color, position
        $scope.control.createBadge = function(id, config) {
          var overlays = viewer.get('overlays');

          var htmlElement;
          if(config.html) {
            htmlElement = config.html;
          } else {
            htmlElement = document.createElement('span');
            if(config.color) {
              htmlElement.style['background-color'] = config.color;
            }
            if(config.tooltip) {
              htmlElement.setAttribute('tooltip', config.tooltip);
              htmlElement.setAttribute('tooltip-placement', 'top');
            }
            if(config.text) {
              htmlElement.appendChild(document.createTextNode(config.text));
            }
          }

          var overlayId = overlays.add(id, {
            position: config.position || {
              bottom: 0,
              right: 0
            },
            show: {
              minZoom: -Infinity,
              maxZoom: +Infinity
            },
            html: htmlElement
          });

          $compile(htmlElement)($scope);

          return overlayId;
        };

        // removes all badges for an element with a given id
        $scope.control.removeBadges = function(id) {
          viewer.get('overlays').remove({element:id});
        };

        // removes a single badge with a given id
        $scope.control.removeBadge = function(id) {
          viewer.get('overlays').remove(id);
        };

        $scope.control.getViewer = function() {
          return viewer;
        };

        $scope.control.scrollToElement = function(element) {
          var height, width, x, y;

          var elem = viewer.get('elementRegistry').get(element);
          var viewbox = canvas.viewbox();

          height = Math.max(viewbox.height, elem.height);
          width  = Math.max(viewbox.width,  elem.width);

          x = Math.min(Math.max(viewbox.x, elem.x - viewbox.width + elem.width), elem.x);
          y = Math.min(Math.max(viewbox.y, elem.y - viewbox.height + elem.height), elem.y);

          canvas.viewbox({
            x: x,
            y: y,
            width: width,
            height: height
          });
        };

        $scope.control.getElement = function(elementId) {
          return viewer.get('elementRegistry').get(elementId);
        };

        $scope.control.getElements = function(filter) {
          return viewer.get('elementRegistry').filter(filter);
        };

        $scope.loaded = false;
        $scope.control.isLoaded = function() {
          return $scope.loaded;
        };

        $scope.control.addAction = function(config) {
          var container = $element.find('.actions');
          var htmlElement = config.html;
          container.append(htmlElement);
          $compile(htmlElement)($scope);
        };

        $scope.control.addImage = function(image, x, y) {
          return preloadImage(image)
            .then(
              function(preloadedElement) {
                var width = preloadedElement.offsetWidth;
                var height = preloadedElement.offsetHeight;
                var imageElement = $document[0].createElementNS('http://www.w3.org/2000/svg', 'image');

                imageElement.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', image);
                imageElement.setAttributeNS(null, 'width', width);
                imageElement.setAttributeNS(null, 'height', height);
                imageElement.setAttributeNS(null, 'x', x);
                imageElement.setAttributeNS(null, 'y', y);

                $document[0].body.removeChild(preloadedElement);
                canvas._viewport.appendChild(imageElement);

                return angular.element(imageElement);
              },
              function(preloadedElement) {
                $document[0].body.removeChild(preloadedElement);
              }
            );
        };

        function preloadImage(img) {
          var body = $document[0].body;
          var deferred = $q.defer();
          var imageElement = angular.element('<img>')
            .css('position', 'absolute')
            .css('left', '-9999em')
            .css('top', '-9999em')
            .attr('src', img)[0];

          imageElement.onload = function() {
            deferred.resolve(imageElement);
          };

          imageElement.onerror = function() {
            deferred.reject(imageElement);
          };

          body.appendChild(imageElement);

          return deferred.promise;
        }

        var BpmnViewer = BpmnJS;
        if($scope.disableNavigation) {
          BpmnViewer = Object.getPrototypeOf(Viewer.prototype).constructor;
        }
        var viewer = new BpmnViewer({
          container: $element[0].querySelector('.diagram-holder'),
          width: '100%',
          height: '100%',
          canvas: {
            deferUpdate: false
          }
        });

        var originalShow = viewer.get('overlays').show.bind(viewer.get('overlays'));
        viewer.get('overlays').show = function() {
          viewer.get('eventBus').fire('overlays.show');
          originalShow();
        };

        var originalHide = viewer.get('overlays').hide.bind(viewer.get('overlays'));
        viewer.get('overlays').hide = function() {
          viewer.get('eventBus').fire('overlays.hide');
          originalHide();
        };

        var showAgain = debounce(function() {
          viewer.get('overlays').show();
        }, 300);

        var originalViewboxChanged = viewer.get('canvas')._viewboxChanged.bind(viewer.get('canvas'));
        var debouncedOriginal = debounce(function() {
          originalViewboxChanged();
          viewer.get('overlays').hide();
          showAgain();
        }, 0);
        viewer.get('canvas')._viewboxChanged = function() {
          debouncedOriginal();
        };


        var diagramData = null;
        var canvas = null;

        $scope.$watch('diagramData', function(newValue) {
          if (newValue) {
            diagramData = newValue;
            renderDiagram();
          }
        });

        function renderDiagram() {
          if (diagramData) {

            $scope.loaded = false;

            var useDefinitions = (typeof diagramData === 'object');

            var importFunction = (useDefinitions ? viewer.importDefinitions : viewer.importXML).bind(viewer);

            importFunction(diagramData, function(err, warn) {

              var applyFunction = useDefinitions ? function(fn) {fn();} : $scope.$apply.bind($scope);

              applyFunction(function() {
                if (err) {
                  $scope.error = err;
                  return;
                }
                $scope.warn = warn;
                canvas = viewer.get('canvas');
                definitions = viewer.definitions;
                zoom();
                setupEventListeners();
                $scope.loaded = true;
                $scope.onLoad();
              });
            });
          }
        }

        function zoom() {
          if (canvas) {
            var viewbox = JSON.parse(($location.search() || {}).viewbox || '{}')[definitions.id];

            if (viewbox) {
              canvas.viewbox(viewbox);
            }
            else {
              canvas.zoom('fit-viewport', 'auto');
            }
          }
        }

        var mouseReleaseCallback = function() {
          $scope.grabbing = false;
          document.removeEventListener('mouseup', mouseReleaseCallback);
          $scope.$apply();
        };

        function setupEventListeners() {
          var eventBus = viewer.get('eventBus');
          eventBus.on('element.click', function(e) {
            // e.element = the model element
            // e.gfx = the graphical element
            $scope.onClick({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.hover', function(e) {
            $scope.onMouseEnter({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.out', function(e) {
            $scope.onMouseLeave({element: e.element, $event: e.originalEvent});
          });
          eventBus.on('element.mousedown', function() {
            $scope.grabbing = true;

            document.addEventListener('mouseup', mouseReleaseCallback);

            $scope.$apply();
          });
          eventBus.on('canvas.viewbox.changed', debounce(function(e) {
            var viewbox = JSON.parse(($location.search() || {}).viewbox || '{}');

            viewbox[definitions.id] = {
              x: e.viewbox.x,
              y: e.viewbox.y,
              width: e.viewbox.width,
              height: e.viewbox.height
            };

            /*search.updateSilently({
              viewbox: JSON.stringify(viewbox)
            });*/

            var phase = $rootScope.$$phase;
            if (phase !== '$apply' && phase !== '$digest') {
              $scope.$apply(function() {
                $location.replace();
              });
            } else {
              $location.replace();
            }
          }, 500));
        }

        $scope.zoomIn = function() {
          viewer.get('zoomScroll').zoom(1, {
            x: $element[0].offsetWidth / 2,
            y: $element[0].offsetHeight / 2
          });
        };

        $scope.zoomOut = function() {
          viewer.get('zoomScroll').zoom(-1, {
            x: $element[0].offsetWidth / 2,
            y: $element[0].offsetHeight / 2
          });
        };

        $scope.resetZoom = function() {
          canvas.resized();
          canvas.zoom('fit-viewport', 'auto');
        };

        $scope.control.resetZoom = $scope.resetZoom;

        $scope.control.refreshZoom = function() {
          canvas.resized();
          canvas.zoom(canvas.zoom(), 'auto');
        };
      }
    };
  }]);

var SearchFactory = [ '$location', '$rootScope', function($location, $rootScope) {

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
}];
app.factory('search', SearchFactory);

	//angular.bootstrap(document, ['kcellWF']);
	angular.element(document).ready(function () {
		angular.bootstrap(document, ['kcellWF']);
	});

})();