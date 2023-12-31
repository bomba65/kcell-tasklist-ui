define('app',[
	'angular',
	'toasty',
	'deep-diff',
	'translate',
	'angular-translate-storage-local',
	'angular-translate-storage-cookie',
	'angular-ui-bootstrap',
	'./controllers/all',
	'./directives/index',
	'./directives/all',
	'./services/all',
	'camundaSDK',
	'big-js',
	'xlsx',
	'angular-ui-router',
	'ngAnimate',
	'ng-file-upload',
	'angular-local-storage',
	'angularjs-dropdown-multiselect',
	'moment',
	'daterangepicker',
	'bootstrap',
	'bootstrap-select',
	'angular-toarrayfilter',
	'ui.mask',
	'angular-ui-sortable',
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
		'ngAnimate',
		'ui.router',
		'ngFileUpload',
		'LocalStorageModule',
		'angularjs-dropdown-multiselect',
		'angular-toArrayFilter',
		'ui.mask',
		'ui.sortable'
		//'bootstrap-select'
	]);
	var preLoginUrl;
	var resolve = {
		baseUrl: function(){
			return '/camunda/api/engine/engine/default';
		},
		projects: function(baseUrl, $http){
			$http.get(baseUrl+'/version').then(function(version){});
			return [
				{
					"key" : "NetworkInfrastructure",
					"name" : "Network Infrastructure",
					"processes" : [
						{key:'Revision', name:'Revision', group:'infrastructure_revision_users',
							subprocesses:[{key:'PreparePermitDocs'}]
						},
						{key:'Revision-power', name:'Revision Power', group:'infrastructure_revision_users'},
						{key:'CreatePR', name:'Create PR', group:'infrastructure_pr_users'},
						{key:'PrNumbersAssignment', name:'PR Numbers Assignment', group:'pr_users'},
						{key:'Invoice', name:'Monthly Act', group:'infrastructure_monthly_act_users'},
						{key:'monthlyAct', name:'New Monthly Act', group:'infrastructure_monthly_act_users'},
						{key:'change-tsd', name:'Change TSD', group:'infrastructure_change_tsd_users'},
						{key:'tsd-processing', name:'TSD Processing', group:'infrastructure_get_rfs_by_permit'},
						{key:'leasing', name:'Leasing Rollout Site', group:'infrastructure_leasing_users'},
						{key:'cancel-tsd', name:'Cancel TSD', group:'infrastructure_hop_delete_users'},
						{key:'SiteSharingTopProcess', name:'4G Site Sharing', group:'infrastructure_sharing_users',
							subprocesses:[{key:'BeelineHostBeelineSite'},{key:'BeelineHostKcellSite'},{key:'KcellHostBeelineSite'},{key:'ReplanSiteAP'},{key:'KcellHostKcellSite'}]
						},
						{key:'Dismantle', name:'Dismantle', group:'infrastructure_dismantle_users', subprocesses:[{key:'sdr_srr_request'}], businessKeyLike: 'SDR-%'},
						{key:'Replacement', name:'Replacement', group:'infrastructure_replacement_users', subprocesses:[{key:'sdr_srr_request'}], businessKeyLike: 'SRR-%'},
						{key:'create-new-tsd', name:'Create new TSD', group:'infrastructure_tnu_users'},
						{key:'VPN_Port_process', name:'VPN/Port process', group:'port_starter'},
						{key:'VPN_Port_auto_process', name:'VPN/Port auto process', group:'port_starter'},
					]
				},
				{
					"key" : "DeliveryPortal",
					"name" : "Delivery Portal",
					"processes" : [
						{key:'freephone', name:'Подключение IVR', group:'delivery_freephone_users'},
						{key:'PBX', name:'PBX', group:'delivery_pbx_users'},
						{key:'PBXdropConnection', name:'PBXdropConnection', group:'delivery_pbx_users'},
						{key:'bulksmsConnectionKAE', name:'Bulk SMS through KAE', group:'delivery_sms_users'},
						{key:'ConnectSMSPartners', name:'Подключение клиента', group:'delivery_sms_partner_users'},
						{key:'DisconnectSMSPartners', name:'Отключение клиента', group:'delivery_sms_partner_users'},
						{key:'after-sales-ivr-sms', name:'After Sales IVR SMS', group:'delivery_sms_ivr_b2b_delivery',
							subprocesses:[{key:'ivr_disconnection'},{key:'BulkSMS_disconnection'},{key:'changeConnectionType'}]
						},
            			{key:'AftersalesPBX', name:'Aftersales PBX', group:'delivery_pbx_users'},
            			{key:'revolvingNumbers', name:'PBX Revolving Numbers', group:'delivery_pbx_users'},
            			{key:'ASRev', name:'Aftersales Revolving Numbers', group:'delivery_pbx_b2b_delivery'},
            			{key:'FixedInternet', name:'Fixed Internet', group:'fixint_b2b_delivery'}
					]
				},
				{
					"key" : "DemandUAT",
					"name" : "Demand Management",
					"processes" : [
						{key:'Demand', name:'Demand and AOP', group:'demand_uat_users'},
						{key:'UAT', name:'UAT protocol', group:'demand_uat_users'}
					]
				},
				{
					"key" : "RPA",
					"name" : "RPA",
					"processes" : [
						{key:'SSU', name:'SSU', group:'rpa_ssu_users'}
					]
				}
			];
		},
        authentication: function(AuthenticationService) {
            return AuthenticationService.getAuthentication();
        },
        authUser: function(baseUrl, $http, authentication, $rootScope) {
            return $http.get(baseUrl+'/user/'+authentication.name+'/profile').then(
            	function(res){
            		$rootScope.authUser = res.data;
            		return res.data;
            	}
        	);
        },
        groups: function(baseUrl, $http, authentication, $rootScope, authUser) {
            return $http.get(baseUrl+'/group?member='+authUser.id).then(
            	function(res){
            		$rootScope.authUser.groups = res.data;
            		return res.data;
            	}
        	);
        },
        defaults: function ($rootScope, projects, groups, localStorageService){
			$rootScope.holidaysForYear = ['7/1/2017', '20/3/2017', '7/7/2017', '1/9/2017', '7/1/2018', '9/3/2018', '30/4/2018',
					'8/5/2018', '21/8/2018', '31/8/2018', '31/12/2018', '7/1/2019', '10/5/2019', '7/1/2020', '3/1/2020',
					'8/5/2020', '31/7/2020', '18/12/2020', '5/7/2021', '20/7/2021'];
			$rootScope.holidays = ['1/1', '2/1', '8/3', '21/3', '22/3', '23/3', '1/5', '7/5', '9/5', '6/7',
					'30/8', '1/12', '16/12', '17/12'];
            $rootScope.weekendWorking = ['18/3/2017', '1/7/2017', '3/3/2018', '28/4/2018', '5/5/2018', '25/8/2018', '29/12/2018',
					'4/5/2019', '5/1/2020','11/5/2020', '20/12/2020', '3/7/2021'];
            $rootScope.doNotMarkNextHoliday = ['9/5/2020'];

            $rootScope.catalogsServerUrl = "https://catalogs.test-flow.kcell.kz";
            $rootScope.assetsServerUrl = "https://asset.test-flow.kcell.kz";

			function hasGroup(group){
				if(groups){
					return _.some($rootScope.authUser.groups, function(value){
						return value.id === group;
					});
				} else {
					return false;
				}
			}

        	var aviableProjects = [
			];

			angular.forEach(projects, function(project){
				var p = angular.copy(project);
				p.processes = [];
				angular.forEach(project.processes, function(process){
					if(hasGroup(process.group) || hasGroup('camunda-admin') || (
						(process.key === 'leasing' && (hasGroup('statistics_rollout') || hasGroup('search_rollout'))) ||
						(process.key === 'Revision' && (hasGroup('statistics_revision') || hasGroup('search_revision'))) ||
						(process.key === 'Revision-power' && (hasGroup('statistics_revision') || hasGroup('search_revision'))) ||
						(process.key === 'Revision-power' && (hasGroup('power_admin') || hasGroup('power_search'))) ||
						(process.key === 'Revision-power' && ["alm_power", "astana_power", "nc_power", "south_power", "west_power", "east_power"].some(group => hasGroup(group))) ||
						(process.key === 'Invoice' && (hasGroup('statistics_monthlyact') || hasGroup('search_monthlyact'))) ||
						(process.key === 'monthlyAct' && (hasGroup('statistics_monthlyact') || hasGroup('search_monthlyact')) ||
						(process.key === 'VPN_Port_process' && (hasGroup('port_admin') || hasGroup('port_ipcore') || hasGroup('port_provider_relations'))) ||
						(process.key === 'VPN_Port_auto_process' && (hasGroup('port_admin') || hasGroup('port_ipcore') || hasGroup('port_provider_relations'))))
					  )){
						p.processes.push(process);
					}
				});
				if(p.processes.length>0){
					aviableProjects.push(p);
				}
			});

			$rootScope.projects = aviableProjects;

			if(!$rootScope.selectedProject && localStorageService.get('selectedProjectKey') && _.some($rootScope.projects, function(project){ return project.key === localStorageService.get('selectedProjectKey')})){
				$rootScope.selectedProject = _.find($rootScope.projects, function(project){ return  project.key === localStorageService.get('selectedProjectKey')})
			}
			if($rootScope.selectedProject && !$rootScope.selectedProcess && localStorageService.get('selectedProcessKey') && _.some($rootScope.selectedProject.processes, function(process){ return process.key === localStorageService.get('selectedProcessKey')})){
				$rootScope.selectedProcess = _.find($rootScope.selectedProject.processes, function(process){ return process.key === localStorageService.get('selectedProcessKey')});
			}
			if($rootScope.selectedProject && $rootScope.selectedProcess && !$rootScope.selectedTask && localStorageService.get('selectedTaskKey') && _.some($rootScope.selectedProject.processes, function(process){ return process.key === localStorageService.get('selectedTaskKey')})){
				$rootScope.selectedTask = _.find($rootScope.selectedProject.processes, function(process){ return process.key === localStorageService.get('selectedTaskKey')});
			}
			return [];
		}
    }
	app.config(['$urlRouterProvider', '$httpProvider', '$stateProvider','$locationProvider', function($urlRouterProvider, $httpProvider, $stateProvider,$locationProvider){
		$locationProvider.hashPrefix('');
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
	    }).state("tasks.massapprove", {
			url: "/massapprove/:defKey",
			templateUrl: "js/partials/massApprove.html",
			controller: "MassApproveCtrl",
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
			url: "/statistics?report&task&region&reason&mainContract&subContractor&unitR&contractFilter&unitFilter&regionFilter&subContractorFilter",
	    	templateUrl: "js/partials/statistics.html",
	    	controller: "statisticsCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    }).state("leasingStatistics", {
			url: "/leasingStatistics",
	    	templateUrl: "js/partials/leasingStatistics.html",
	    	controller: "leasingStatisticsCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    }).state("PowerRevisionStatistics", {
			url: "/PowerRevisionStatistics",
			templateUrl: "js/partials/PowerRevisionStatistics.html",
			controller: "PowerRevisionStatisticsCtrl",
			authenticate: true,
			resolve: resolve
		}).state("filesAndDictionaries", {
			url: "/filesAndDictionaries",
			templateUrl: "js/partials/files.html",
			controller: "filesCtrl",
			authenticate: true,
			resolve: resolve
		}).state("search", {
	    	url: "/search",
	    	templateUrl: "js/partials/search_new.html",
	    	controller: "searchCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    }).state("minio", {
	    	url: "/minio",
	    	templateUrl: "js/partials/minio.html",
	    	controller: "minioCtrl",
	    	authenticate: true,
	    	resolve: resolve
	    });
	}]).config(['localStorageServiceProvider', function (localStorageServiceProvider) {
		localStorageServiceProvider.setPrefix('flow');
	}]).config(['$qProvider', function ($qProvider) {
   		$qProvider.errorOnUnhandledRejections(false);
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
	}).run(['AuthenticationService', '$rootScope', '$http', 'localStorageService', 'exModal', 'StartProcessService', function(AuthenticationService, $rootScope, $http, localStorageService, exModal, StartProcessService){
		$rootScope.userList = []
		$rootScope.isProjectVisible = function(projectKey){
			if($rootScope.selectedProject.key === projectKey){
				return true;
			} else {
				return false;
			}
		}

		$rootScope.isProjectAvailable = function(projectKey){
			return _.some($rootScope.projects, function(project){
				return project.key === projectKey;
			});
		}

		$rootScope.isProcessVisible = function(processKey){
			if($rootScope.selectedProcess && $rootScope.selectedProcess.key === processKey){
				return true;
			} else if($rootScope.selectedProject && _.find($rootScope.selectedProject.processes, { 'key': processKey})){
				return true;
			} else {
				return false;
			}
		}

		$rootScope.isProcessAvailable = function(processKey){
			return _.some($rootScope.projects, function(project){
				return _.some(project.processes, function(process){
					return process.key === processKey;
				});
			});
		}

		$rootScope.getCurrentProcesses = function(){
			var result = [];
			if($rootScope.selectedProcess){
				result.push($rootScope.selectedProcess);
			}
			return result;
		}
		$rootScope.hasGroup = function(group){
			if($rootScope.authUser && $rootScope.authUser.groups){
				return _.some($rootScope.authUser.groups, function(value){
					return value.id === group;
				});
			} else {
				return false;
			}
		}
		$rootScope.contractorRegion = function(pattern) {
			if($rootScope.authUser && $rootScope.authUser.groups){
				return _.some($rootScope.authUser.groups, function(value){
					return value.id.indexOf(pattern) > -1;
				});
			} else {
				return false;
			}
		}
		$rootScope.checkGroup = async function(userId) {
			var groups = await $http.get('/camunda/api/engine/engine/default/group?member='+userId).then(
				function(res){
					return res.data;
				}
			);
			groups = groups.filter(f => f.id.indexOf('_contractor_') > -1)
			return groups.length > 0 ? groups[0].id : null;
		}
		$rootScope.updateSelectedProject = function(project){
			$rootScope.selectedProject = project;
			if(project){
				localStorageService.set('selectedProjectKey',project.key);
			} else {
				localStorageService.set('selectedProjectKey', undefined);
			}

		}
		$rootScope.updateSelectedProcess = function(process){
			$rootScope.selectedProcess = process;
			if(process){
				localStorageService.set('selectedProcessKey',process.key);
			}else {
				localStorageService.set('selectedProcessKey', undefined);
			}
		}
		$rootScope.updateSelectedTask = function(process){
			$rootScope.selectedTask = process;
			if(process){
				localStorageService.set('selectedTaskKey', process.key);
			} else {
				localStorageService.set('selectedTaskKey', undefined);
			}
		}
		$rootScope.getCatalogsHttpByName = function(name){
			return '/api/' + name + '?v=12';
		}
		$rootScope.modalStartProcess = function() {
			var processList = [];
			angular.forEach($rootScope.projects, function(project) {
				processList += _.map(project.processes, 'key');
			});
			$http.get('/camunda/api/engine/engine/default/process-definition?latest=true&active=true&firstResult=0&maxResults=100&startablePermissionCheck=true').then(
				function(results){
					var processDefinitions = [];
					angular.forEach(results.data, function(e){
						if($rootScope.isProcessAvailable(e.key) && processList.indexOf(e.key) !== -1 && e.key !== 'after-sales-ivr-sms'){
							processDefinitions.push(e);
						}
					});
					exModal.open({
						scope: {
							allProcessDefinitions: processDefinitions,
							startProcess: StartProcessService
						},
						templateUrl: './js/partials/startProcess.html',
						size: 'md'
					}).then(function (results) {
					});
				},
				function(error){
					console.log(error.data);
				}
			);
		};
		async function getUserById (userId) {
			var user = _.find($rootScope.userList, o => o.id == userId)
			if (userId){
				if (user) {
					return user
				} else {
					user = await $http({
						method: 'GET',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						url: '/camunda/api/engine/engine/default/user/?id=' + userId
					}).then(function(results){
						// var index = _.findIndex($scope.jobModel.tasks, function(v){
						// 	return v.processInstanceId = task.processInstanceId
						// })
						// $scope.jobModel.tasks[index].assigneeObject = results.data[0]
						$rootScope.userList.push(results.data[0])
						return results.data[0]
					})
				}

			}
			return user
		};
		$rootScope.showHistory = function(resolutions, procDef){
			exModal.open({
				scope: {
					resolutions: procDef === 'leasing' ? _.orderBy(resolutions, ['type', 'assignDate'], ['asc', 'desc']) : resolutions,
					// resolutions: resolutions,
					procDef: procDef,
					download: function(path) {
						$http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
						then(function(response) {
							document.getElementById('fileDownloadIframe').src = response.data;
						}, function(error){
							console.log(error);
						});
					},
					isFileVisible: function(file) {
						return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
					},
					showDetailHistory: async function(resolution, resolutions) {
						if (resolution.taskId != 'noTaskId') {
							this.$dismiss()
							await $http({method: 'GET', url: '/camunda/api/engine/engine/default/history/identity-link-log/?taskId=' + resolution.taskId}).
							then(async function(response) {
								$rootScope.userTaskHistoryList = response.data.filter(function(el) {
									return el.type == 'assignee'
								})
								for (var i = 0; i < $rootScope.userTaskHistoryList.length; i ++){
									var el = $rootScope.userTaskHistoryList[i]
									el.assigneeName =  await getUserById(el.assignerId)
									el.operation_responsibleName =  await getUserById(el.userId)
								}
							}, function(error){
								console.log(error);
							});
							exModal.open({
								scope: {
									userTaskHistoryList: $rootScope.userTaskHistoryList,
									close: function() {
										this.$dismiss();
										$rootScope.showHistory(resolutions, procDef)
									}
								},
								templateUrl: './js/partials/detailHistoryModal.html',
								size: 'hg'
							}).then(function(results){
							});
						}

					},
					isKcellStaff: $rootScope.hasGroup('kcellUsers')
				},
				templateUrl: './js/partials/resolutionsModal.html',
				size: 'hg'
			}).then(function(results){
			});
		};
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
	}).config(['uiMask.ConfigProvider', function(uiMaskConfigProvider) {
	  uiMaskConfigProvider.maskDefinitions({'A': /[a-z]/, '*': /[a-zA-Z0-9]/, 'N': /[4-5]/, 'E': /[4-8]/});
  		uiMaskConfigProvider.clearOnBlur(false);
  		uiMaskConfigProvider.eventsToHandle(['input', 'keyup', 'click']);
  	}]);
	return app;
});
