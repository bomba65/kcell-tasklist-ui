define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$routeParams', '$timeout', '$location', 'exModal', '$http', '$state', 'StartProcessService', function($scope, $rootScope, toasty, AuthenticationService, $routeParams, $timeout, $location, exModal, $http, $state, StartProcessService) {
		$rootScope.currentPage = {
			name: 'tasks'
		};

		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		var Authentication = function(data) {
			angular.extend(this, data);
		}

		$scope.view = {
			page: 1,
			maxResults: 20
		}

		var baseUrl = '/camunda/api/engine/engine/default';

		$scope.selectedView = 'task';
		$scope.camForm = null;

		if($routeParams.task){
			$scope.tryToOpen = {
				id: $routeParams.task
			}
		}

		$scope.reverseOrder = false;
		$scope.fieldName = 'businessKey';
		$scope.fieldFilter = {};
		$scope.visibilityFilter = {};


		$scope.setVisibilityFilter = function(fieldName) {
			//$scope.fieldName = fieldName;
			$scope.fieldFilter[fieldName] = $scope.visibilityFilter[fieldName] ? undefined : $scope.fieldFilter[fieldName];
			$scope.visibilityFilter[fieldName] = $scope.visibilityFilter[fieldName] ? false : true;
		};

		$scope.orderByFieldName = function(fieldName) {
			if ($scope.fieldName == fieldName) {
				$scope.reverseOrder = !$scope.reverseOrder;
			} else {
				$scope.reverseOrder = false;
				$scope.fieldName = fieldName;
			}
		};

		$scope.filterByFields = function() {
			if (Object.keys($scope.fieldFilter).length !== 0) {
				return Object.keys($scope.fieldFilter).filter(fieldName => 
						$scope.fieldFilter[fieldName]
					).reduce(function(obj, val, i) {
						obj[val] = $scope.fieldFilter[val];
					  	return obj;
					},
					{});
			}
			return undefined;
		};
		$scope.isRegionFiltersVisible = function(){
			return (($rootScope.isProcessAvailable('Revision') && $rootScope.isProcessVisible('Revision')) || ($rootScope.isProcessAvailable('Invoice') && $rootScope.isProcessVisible('Invoice'))) && $rootScope.hasGroup('head_kcell_users');
		}

		$scope.selectFilter = function(filter){
			$scope.currentTask = undefined;
			$scope.currentFilter = filter;
			$location.search({task:undefined});
			$scope.currentRegionFilter = undefined;
			if (filter === 'search'){
				$scope.selectedView = 'search';
			} else {
				$scope.selectedView = 'task';
				$scope.view.page = 1;
				loadTasks();
			}
		}

		$scope.openTask = function(taskId){
			$scope.selectedView = 'task';
			$state.go('tasks.task', {id:taskId});
		}

		$scope.$on('getTaskListEvent', function(event, data){
			getTaskList();
		});
		$scope.startProcess = function(id){
			StartProcessService(id);
		}
		function showProcessStartButtonVisible () {
			var showModal = false;
			$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=100&startablePermissionCheck=true').then(
				function(results){
					$scope.processDefinitions = [];
					angular.forEach(results.data, function(e){
						if($rootScope.isProcessAvailable(e.key)){
							$scope.processDefinitions.push(e);
						}
					});
					if ($scope.processDefinitions.length>0) {
						showModal= true;
					}
					$rootScope.isProcessStartButtonVisible = showModal;
				},
				function(error){
					console.log(error.data);
					$rootScope.isProcessStartButtonVisible = showModal;
				}
			);
		}
		$rootScope.isProcessStartButtonVisible = showProcessStartButtonVisible();
		$rootScope.modalStartProcess = function() {
			exModal.open({
				scope: {
					projects : $scope.projects,
					getAllProcessDefinitions: $scope.getAllProcessDefinitions,
					startProcess: $scope.startProcess
				},
				templateUrl: './js/partials/startProcess.html',
				size: 'md'
			}).then(function (results) {
			});
		};
		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		$scope.nextTasks = function(){
			$scope.view.page++;
			loadTasks();
		}

		$scope.prevTasks = function(){
			$scope.view.page--;
			loadTasks();
		}

		$scope.setSelectedProcessKey = function(key){
			if($scope.selectedProcessKey === key){
				$scope.selectedProcessKey = undefined;
			} else {
				$scope.selectedProcessKey = key;
			}
			$scope.currentTaskGroup = undefined;
		}

		$scope.setSelectedTaskGroupName = function(currentTaskGroup){

			$scope.currentTask = undefined;
			$location.search({task:undefined});
			$scope.currentTaskGroup = currentTaskGroup;

			if(currentTaskGroup.taskDefinitionKey.indexOf("massApprove") === 0){
				$state.go('tasks.massapprove', {defKey:currentTaskGroup.taskDefinitionKey});
				$scope.selectedView = 'massapprove';
			} else {
				$scope.selectedView = 'tasks';
				
				var varSearchParams = {processInstanceIds: _.map($scope.currentTaskGroup.tasks, 'processInstanceId')};
	            $http({
	                method: 'POST',
	                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
	                data: varSearchParams,
	                url: baseUrl + '/history/process-instance'
	            }).then(
	                    function(processes){
	                    	angular.forEach($scope.currentTaskGroup.tasks, function(task){
	                    		var process = _.find(processes.data, function(p){ return p.id === task.processInstanceId });
	                    		task.processBusinessKey = process.businessKey;
	                    		task.processStartUserId = process.startUserId;
	                    		task.processStartTime = process.startTime;
	                    	});
	                    },
	                    function(error){
	                        console.log(error.data);
	                    }
	            );
	            if($rootScope.selectedProcess.key === 'Revision'){
	            	$scope.getAdditionalVariablesToTasks(['site_name']);
	            }
	            if($rootScope.selectedProcess.key === 'leasing'){
	            	$scope.getAdditionalVariablesToTasks(['ncpID', 'siteName']);
	            }
			}
		}

		$scope.getAdditionalVariablesToTasks = function(variableList){
			_.forEach(variableList, function(variable) {
				var varSearchParams = {processInstanceIdIn: _.map($scope.currentTaskGroup.tasks, 'processInstanceId'), variableName: variable};
				$http({
					method: 'POST',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					data: varSearchParams,
					url: baseUrl+'/variable-instance'
				}).then(
					function(vars){
						angular.forEach($scope.currentTaskGroup.tasks, function(task){
							var f =  _.find(vars.data, function(v) {
								return v.processInstanceId === task.processInstanceId; 
							});
							if(f){
								task[variable] = f.value;
							}
						});
					},
					function(error){
						console.log(error.data);
					}
				);
			});

		}

        $scope.getSite = function(val) {
            return $http.get('/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name='+val).then(
                function(response){
                    var sites = _.flatMap(response.data._embedded.sites, function(s){
                        if(s.params.site_name){
                            return s.params.site_name.split(',').map(function(sitename){
                                return {
                                    name: s.name,
                                    id: s._links.self.href.substring(s._links.self.href.lastIndexOf('/')+1),
                                    site_name: sitename
                                };
                            })
                        } else {
                            return [];
                        }
                    });
                    return sites;
                }
            );
        };

        $scope.siteSelected = function($item){
            $scope.siteName = $item.name;
            $scope.site = $item.id;
            $scope.site_name = $item.site_name;
        };

        $scope.taskIds = [{id:'all', label:'All'},{id:'attach_material_list_contractor', label:'Attach Material List'},{id:'upload_tr_contractor', label:'Upload TR'},{id:'fill_applied_changes_info', label:'Fill Applied Changes Info'},
        					{id:'attach_additional_material_list_contractor', label:'Attach Additional Material List'},{id:'upload_additional_tr_contractor', label:'Upload Additional TR'}];

		$scope.searchForContractors = function(){
			if($scope.accepted){
				$scope.searchProcessesForContractors();
			} else {
				$scope.searchTasksForContractors();
			}
		}

        $scope.searchTasksForContractors = function(){
        	var queryParams = {};
			if($scope.site && $scope.site_name){
        		queryParams.processVariables = [{name:"site", value:$scope.site, operator: "eq"}];
        	}
        	if($scope.taskId !== 'all'){
        		queryParams.taskDefinitionKey = $scope.taskId;
        	} else {
        		var taskIdList = _.filter($scope.taskIds, function(n) {
				  return n.id !== 'all';
				});
				queryParams.taskDefinitionKeyIn = _.map(taskIdList, 'id');
        	}
        	if($scope.priority === 'emergency'){
        		queryParams.priority = 100;
        	}
        	if($scope.bussinessKey){
        		if(!queryParams.processVariables){
        			queryParams.processVariables = [];
        		}
        		queryParams.processVariables.push({name:"jrNumber", value:$scope.bussinessKey, operator: "eq"});
        	}

        	queryParams.candidateUser = $rootScope.authUser.id;
        	queryParams.includeAssignedTasks = true;

			$scope.searchResults = [];
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: queryParams,
				url: baseUrl+'/task'
			}).then(
				function(results){
					$scope.searchResults = _.filter(results.data, function(d) {
						return !d.assignee || d.assignee === $rootScope.authUser.id;
					});

					if($scope.searchResults.length > 0){
						_.forEach(['jrNumber','site_name', 'validityDate'], function(variable) {
							var varSearchParams = {processInstanceIdIn: _.map($scope.searchResults, 'processInstanceId'), variableName: variable};
							$http({
								method: 'POST',
								headers:{'Accept':'application/hal+json, application/json; q=0.5'},
								data: varSearchParams,
								url: baseUrl+'/variable-instance'
							}).then(
								function(vars){
									$scope.searchResults.forEach(function(el) {
										var f =  _.filter(vars.data, function(v) {
											return v.processInstanceId === el.processInstanceId; 
										});
										if(f){
											el[variable] = f[0].value;
										}
									});
								},
								function(error){
									console.log(error.data);
								}
							);
						});
					}
				},
				function(error){
					console.log(error.data);
				}
			);
        };

        $scope.searchProcessesForContractors = function(){
        	var queryParams = {processDefinitionKey: 'Revision', variables: [], activityIdIn: ['attach-scan-copy-of-acceptance-form','intermediate_wait_acts_passed','intermediate_wait_invoiced']};
			if($scope.site && $scope.site_name){
        		queryParams.variables.push({name:"site", value:$scope.site, operator: "eq"});
        	}
        	if($scope.priority === 'emergency'){
        		queryParams.variables.push({name:"priority", value:"emergency", operator: "eq"});
        	}
        	if($scope.bussinessKey){
        		queryParams.businessKey = $scope.bussinessKey;
        	}

 			if($rootScope.hasGroup('hq_contractor_lse')){
                // all values
            } else if($rootScope.hasGroup('astana_contractor_lse') && $rootScope.hasGroup('nc_contractor_lse')){
                if(!$scope.region || ['astana','nc'].indexOf($scope.region)===-1){
                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
                    $scope.region = 'astana';
                } else {
                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.region});
                }
            } else if($rootScope.hasGroup('astana_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
            } else if($rootScope.hasGroup('nc_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'nc'});
            } else if($rootScope.hasGroup('alm_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'alm'});
            } else if($rootScope.hasGroup('east_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'east'});
            } else if($rootScope.hasGroup('south_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'south'});
            } else if($rootScope.hasGroup('west_contractor_lse')){
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'west'});
            }

        	$scope.processSearchResults = [];
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: queryParams,
				url: baseUrl+'/process-instance'
			}).then(function(results){
				$scope.processSearchResults = results.data;
				if($scope.processSearchResults.length > 0){
					_.forEach(['site_name', 'priority', 'validityDate', 'requestedDate'], function(variable) {
						var varSearchParams = {processInstanceIdIn: _.map($scope.processSearchResults, 'id'), variableName: variable};
						$http({
							method: 'POST',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							data: varSearchParams,
							url: baseUrl+'/variable-instance'
						}).then(
							function(vars){
								$scope.processSearchResults.forEach(function(el) {
									var f =  _.filter(vars.data, function(v) {
										return v.processInstanceId === el.id;
									});
									if(f){
										el[variable] = f[0].value;
									}
								});
							},
							function(error){
								console.log(error.data);
							}
						);
					});
					$scope.processSearchResults.forEach(function(el) {
						$http({
							method: 'GET',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							url: baseUrl+'/process-instance/' + el.id + '/activity-instances'
						}).then(function(activities){
							activities.data.childActivityInstances.forEach(function(act) {
								if(['attach-scan-copy-of-acceptance-form','intermediate_wait_acts_passed','intermediate_wait_invoiced'].indexOf(act.activityId)!==-1){
									el.activityName = act.activityName;
								}
							});
						});
					});
				}
			});
        }

        $scope.toggleProcessViewRevision = function(p) {
            $scope.jobModel = {
                state: 'Active',
                processDefinitionKey: 'Revision',
                startTime: p.requestedDate
            };
            $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/task?processInstanceId=' + p.id,
            }).then(
                function (tasks) {
                    var processInstanceTasks = tasks.data._embedded.task;
                    $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + p.id).then(
                        function (result) {
                            var workFiles = [];
                            result.data.forEach(function (el) {
                                $scope.jobModel[el.name] = el;
                                if (el.type === 'File' || el.type === 'Bytes') {
                                    $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                }
                                if (el.type === 'Json') {
                                    $scope.jobModel[el.name].value = JSON.parse(el.value);
                                }
                                if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                    workFiles.push(el);
                                }
                            });
                            if ($scope.jobModel['siteWorksFiles']) {
                                _.forEach($scope.jobModel['siteWorksFiles'].value, function (file) {
                                    var workIndex = file.name.split('_')[1];
                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
                                    }
                                    if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                        return f.name == file.name;
                                    }) < 0) {
                                        $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                    }
                                });
                            }
                            _.forEach(workFiles, function (file) {
                                var workIndex = file.name.split('_')[1];
                                if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                    $scope.jobModel.jobWorks.value[workIndex].files = [];
                                }
                                if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                    return f.name == file.name;
                                }) < 0) {
                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                }
                            });
                            angular.extend($scope.jobModel, catalogs);
                            $scope.jobModel.tasks = processInstanceTasks;
                            openProcessCardModalRevision(p);
                        },
                        function (error) {
                            console.log(error.data);
                        }
                    );
                },
                function (error) {
                    console.log(error.data);
                }
            );
	    };

	    function openProcessCardModalRevision(p) {
	        exModal.open({
	            scope: {
	                jobModel: $scope.jobModel,
	                getStatus: $scope.getStatus,
	                showHistory: $scope.showHistory,
	                hasGroup: $scope.hasGroup,
	                showGroupDetails: $scope.showGroupDetails,
	                processDefinitionId: p.definitionId,
	                businessKey: p.businessKey,
	                download: function (file) {
	                    $http({
	                        method: 'GET',
	                        url: '/camunda/uploads/get/' + file.path,
	                        transformResponse: []
	                    }).then(function (response) {
	                        document.getElementById('fileDownloadIframe').src = response.data;
	                    }, function (error) {
	                        console.log(error);
	                    });
	                },
	                isFileVisible: function (file) {
	                    return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
	                },
	                getDictNameById: function (dictionary, id) {
	                    return _.find(dictionary, function (dict) {
	                        return dict.id === id;
	                    });
	                },
	                compareDate: new Date('2019-02-05T06:00:00.000')
	            },
	            templateUrl: 'js/partials/processCardModal.html',
	            size: 'lg'
	        }).then(function (results) {
	        });
	    }

        $scope.showHistory = function(resolutions, procDef){
            exModal.open({
                scope: {
                    resolutions: resolutions,
                    isKcellStaff: $rootScope.hasGroup('kcellUsers'),
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
                    }
                },
                templateUrl: 'js/partials/resolutionsModal.html',
                size: 'hg'
            }).then(function(results){
            });
        };

		$scope.getVariableLabel = function(key){
			return _.keyBy($scope.currentFilter.properties.variables, 'name')[key].label;
		}

		$scope.firstLevel = "open";
		$scope.secondLevel = "closed";

        $scope.collapseLevels = function(levelName) {
        	if(levelName === "firstLevel"){
        		$scope.firstLevel = $scope.firstLevel === 'open'? 'closed' : 'open';
        	} else if(levelName === "secondLevel"){
        		$scope.secondLevel = $scope.secondLevel === 'open'? 'closed' : 'open';
        	}
        }

        $scope.collapseProject = function(project) {
			$rootScope.updateSelectedProject(project);
			$rootScope.updateSelectedProcess(undefined);
			$rootScope.updateSelectedTask(undefined);
			loadProcessDefinitions();
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
			getTaskList();
			$scope.secondLevel = "closed";
        }

        $scope.collapseProcess = function(process) {
			$rootScope.updateSelectedProcess(process);
			if ($rootScope.selectedTask && process!==$rootScope.selectedTask.key) {
				$rootScope.updateSelectedTask(undefined);
			}
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
			$scope.secondLevel = "closed";
        }
		$scope.collapseTask = function(task) {
			$rootScope.updateSelectedTask(task);
			$scope.collapseLevels('secondLevel');
		}
		function loadProcessDefinitions(){
			$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=100&startablePermissionCheck=true').then(
				function(results){
					$scope.processDefinitions = [];
					angular.forEach(results.data, function(e){
						if($rootScope.isProcessAvailable(e.key)){
							$scope.processDefinitions.push(e);
						}
					});
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.getProcessDefinitions = function(project){
			var processList = _.map(project.processes, 'key');
			return _.filter($scope.processDefinitions, function(pd){
				return processList.indexOf(pd.key) !== -1 && pd.key !== 'after-sales-ivr-sms';
			});
		}
		$scope.getAllProcessDefinitions = function(projects){
			var processList = [];
			angular.forEach(projects, function(project) {
				processList += _.map(project.processes, 'key');
			});
			return _.filter($scope.processDefinitions, function(pd){
				return processList.indexOf(pd.key) !== -1 && pd.key !== 'after-sales-ivr-sms';
			});
		}
		function getTaskList(){
			$scope.projects = angular.copy($rootScope.projects);
			$http.get(baseUrl+'/filter?resoureType=Task').then(
				function(result){
					var filters = result.data;
					angular.forEach($scope.projects, function(project){
						if($rootScope.selectedProject.key && $rootScope.selectedProject.key === project.key){
							angular.forEach(project.processes, function(process){
								var allTasks = 0;
								var claimed = 0;
		 						process.filters = [];
								var selectedProcessDefinitionKeyMap = [process.key];
								if(process.subprocesses && process.subprocesses.length > 0){
									selectedProcessDefinitionKeyMap = _.concat(selectedProcessDefinitionKeyMap, _.map(process.subprocesses, 'key'));
								}
								var selectedQuery = {'processDefinitionKeyIn':selectedProcessDefinitionKeyMap};
								if(process.businessKeyLike){
									selectedQuery.processInstanceBusinessKeyLike = process.businessKeyLike;
								}

			 					angular.forEach(filters, function(filter){
			 						if(!filter.properties.processDefinitionKey || filter.properties.processDefinitionKey === process.key){
										$http.post(baseUrl+'/filter/'+filter.id+'/count',selectedQuery,{headers:{'Content-Type':'application/json'}}).then(
											function(results){
												var tmpfilter = angular.copy(filter);
												tmpfilter.itemCount = results.data.count;
												if (filter.name==='All Tasks') {
													allTasks = results.data.count;
												} else if (filter.name==='My Claimed Tasks' || filter.name=== 'My Unclaimed Tasks') {
													claimed+= results.data.count;
													}
												process.itemCount = allTasks>claimed ? allTasks : claimed;
												process.filters.push(tmpfilter);
						 						if($rootScope.selectedProcess && process.key === $rootScope.selectedProcess.key){
							 						$rootScope.selectedProcess.itemCount = process.itemCount;
							 						$rootScope.selectedProcess.filters = process.filters;
						 						}
												project.itemCount = project.itemCount ? project.itemCount + results.data.count : results.data.count;
											},
											function(error){
												console.log(error.data);
											}
										);
									}
								});
							});
						}
					});

					loadTasks();
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		function loadTasks() {
			if($rootScope.selectedTask && $rootScope.selectedProcess && $rootScope.selectedTask===$rootScope.selectedProcess && $scope.secondLevel === "closed"){
				$scope.collapseLevels('secondLevel');
			}

			if($scope.currentFilter){
				var processDefinitionKeyMap = [$rootScope.selectedProcess.key];
				if($rootScope.selectedProcess.subprocesses && $rootScope.selectedProcess.subprocesses.length > 0){
					var subprocessDefinitionKeyMap = _.map($rootScope.selectedProcess.subprocesses, 'key');
					processDefinitionKeyMap = _.concat(processDefinitionKeyMap, subprocessDefinitionKeyMap);
				}

				var queryData = {sorting:[{"sortBy":"created","sortOrder":"desc"}],'processDefinitionKeyIn':processDefinitionKeyMap};
				if($rootScope.selectedProcess.businessKeyLike){
					queryData.processInstanceBusinessKeyLike = $rootScope.selectedProcess.businessKeyLike;
				}
				$scope.taskGroups = {};

				$http({
					method: 'POST',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					data: queryData,
					url: baseUrl+'/filter/'+$scope.currentFilter.id+'/list'
				}).then(
					function(results){
						try {
							$scope.tasks = results.data._embedded.task;
							var selectedTask;

							if($scope.tasks && $scope.tasks.length > 0){
								$scope.tasks.forEach(function(e){
									if(e.assignee){
										for(var i=0;i<results.data._embedded.assignee.length;i++){
											if(results.data._embedded.assignee[i].id === e.assignee){
												e.assigneeObject = results.data._embedded.assignee[i];
											}
										}
									}
									if(!$scope.taskGroups[e.name]){
										$scope.taskGroups[e.name] = {tasks:[], taskDefinitionKey: e.taskDefinitionKey, name: e.name};
									}
									$scope.taskGroups[e.name].tasks.push(e);

									if($scope.tryToOpen && e.assignee === $rootScope.authentication.name && e.processInstanceId === $scope.tryToOpen.id){
										selectedTask = e;
									}
								});
							}
							if(selectedTask){
								$state.go('tasks.task', {id:selectedTask.id});
							} else if($scope.tryToOpen){
								$http.get(baseUrl+'/task/'+$scope.tryToOpen.id).then(
									function(taskResult){
										$scope.tryToOpen = undefined;
										if(taskResult.data.assignee){
											$http.get(baseUrl+'/user/'+taskResult.data.assignee+'/profile').then(
												function(userResult){
													taskResult.data.assigneeObject = userResult.data;
													$state.go('tasks.task', {id:taskResult.data.id});
												},
												function(error){
													console.log(error.data);
												}
											);
										} else{
											$state.go('tasks.task', {id:taskResult.data.id});
										}
									},
									function(error){
										console.log(error.data);
									}
								);
							}
						} catch(e) {
							console.log(e);
						}
					},
					function(error){
						console.log(error.data);
					}
				);
			}
		}


		$scope.closeDiv = function(level){
			if($scope.currentFilter && $scope.firstLevel === 'open'){
				$scope.collapseLevels('firstLevel');
			}
		}

		$scope.openDiv = function(level){
			if($scope.currentFilter && $scope.firstLevel === 'closed'){
				$scope.collapseLevels('firstLevel');
			}
		}

		$scope.selectFilter = function(filter){
			$scope.currentTask = undefined;
			$scope.currentFilter = filter;
			$location.search({task:undefined});
			if (filter === 'search'){
				$scope.selectedView = 'search';
			} else {
				$scope.selectedView = 'task';
				$scope.view.page = 1;
				loadTasks();
			}
			if($scope.firstLevel === 'open'){
				$scope.collapseLevels('firstLevel');
			}
		}


		getTaskList();

		$scope.getTaskList = getTaskList;

        var catalogs = {};
        $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
            function (result) {
                angular.extend(catalogs, result.data);
            },
            function (error) {
                console.log(error.data);
            }
        );
	}]);
});