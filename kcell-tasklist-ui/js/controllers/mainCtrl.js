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
/*
		var regions = {
			alm : 'Almaty',
			astana : 'Astana',
			east : 'East',
			nc : 'North & Center',
			south : 'South',
			west : 'West',
		};

		$scope.regionFilters = [];
		$scope.currentRegionFilter = undefined;
*/
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
/*
		function loadRegionCount(){
			$scope.regionFilters = [];
			angular.forEach(regions,function(value,key){
				var query = {'processVariables':[{'name': 'siteRegion', "operator": "eq","value": key}]};
				$http.post(baseUrl+'/filter/'+$scope.currentFilter.id+'/count',query,{headers:{'Content-Type':'application/json'}}).then(
					function(results){
						if(results.data.count > 0){
							$scope.regionFilters.push({id: key,name: value, itemCount: results.data.count});
						}
					},
					function(error){
						console.log(error.data);
					}
				);
			});
		}
*/
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
//				if($rootScope.hasGroup('head_kcell_users')){
//					loadRegionCount();
//				}
				loadTasks();
			}
		}

/*		$scope.selectRegionFilter = function(filter, region){
			$scope.currentTask = undefined;
			$location.search({task:undefined});
			$scope.selectedView = 'task';
			$scope.view.page = 1;
			$scope.currentRegionFilter = region;
			loadTasks();
		}
*/
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
		/*
		$scope.startProcess = function(id){
			$http.get(baseUrl+'/process-definition/'+id+'/startForm').then(
				function(startFormInfo){
					if(startFormInfo.data.key){
						var url = startFormInfo.data.key.replace('embedded:app:', startFormInfo.data.contextPath + '/');
						exModal.open({
							scope: {
								processDefinitionId: id,
								url: url,
								view: {
									submitted: false
								}
							},
							templateUrl: './js/partials/start-form.html',
							controller: StartFormController,
							size: 'lg'
						}).then(function(results){
							$http.get(baseUrl+'/task?processInstanceId='+results.id).then(
								function(tasks){
									var task = null;
									if(tasks.data.length > 0){
										task = tasks.data[0];
									} else {
										task = results.data
									}

									if (task.assignee === $rootScope.authUser.id) {
										$scope.tryToOpen = task;
									}
									getTaskList();
								},
								function(error){
									console.log(error.data);
								}
							);
						});
					} else {
						$http.post(baseUrl+'/process-definition/'+id+'/start',{},{headers:{'Content-Type':'application/json'}}).then(
							function(results){
								$http.get(baseUrl+'/task?processInstanceId='+results.id).then(
									function(tasks){
										if(tasks.data.length > 0){
											$scope.tryToOpen = tasks.data[0];
										} else {
											$scope.tryToOpen = results.data
										}
										getTaskList();
									},
									function(error){
										console.log(error.data);
									}
								);
							},
							function(error){
								console.log(error.data);
							}
						);
					}
				},
				function(error){
					console.log(error.data);
				}
			);

			StartFormController.$inject = ['scope'];
			function StartFormController(scope){
				$timeout(function(){
					new CamSDK.Form({
						client: camClient,
						formUrl: scope.url,
						processDefinitionId: scope.processDefinitionId,
						containerElement: $('#start-form-modal-body'),
						done: scope.addStartFormButtion
					});
				})
				scope.addStartFormButtion = function(err, camForm, evt) {
					if (err) {
						throw err;
					}
					var $submitBtn = $('<button type="submit" class="btn btn-primary">Start</button>').click(function (e) {
						scope.view = {
							submitted : true
						};
						$timeout(function(){
							scope.$apply(function(){
								if(scope.kcell_form.$valid){
									$submitBtn.attr('disabled', true);
									if(scope.preSubmit){
										scope.preSubmit().then(
											function(result){
												camForm.submit(function (err,results) {
													if (err) {
														$submitBtn.removeAttr('disabled');
														toasty.error({title: "Could not complete task", msg: err});
														e.preventDefault();
														throw err;
													} else {
														$('#start-form-modal-body').html('');
														scope.$close(results);
													}
												});
											},
											function(err){
												$submitBtn.removeAttr('disabled');
												toasty.error({title: "Could not complete task", msg: err});
												e.preventDefault();
												throw err;
											}
										);
									} else {
										camForm.submit(function (err,results) {
											if (err) {
												$submitBtn.removeAttr('disabled');
												toasty.error({title: "Could not complete task", msg: err});
												e.preventDefault();
												throw err;
											} else {
												$('#start-form-modal-body').html('');
												scope.$close(results);
											}
										});
									}
								} else {
									toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
								}
							})
						});
					});
					$("#modal-footer").append($submitBtn);
				}
			}
		}*/
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
        $scope.searchTasks =  function(){
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

		$scope.getVariableLabel = function(key){
			return _.keyBy($scope.currentFilter.properties.variables, 'name')[key].label;
		}

/* ------------------------------------- Tasks new logic ------------------------------*/
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
			if($scope.secondLevel === 'closed'){
				$scope.collapseLevels('secondLevel');
			}
			loadProcessDefinitions();
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
        }

        $scope.collapseProcess = function(process) {
			$rootScope.updateSelectedProcess(process);
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
        }

		function loadProcessDefinitions(){
			$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=30&startablePermissionCheck=true').then(
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

		function getTaskList(){
			$http.get(baseUrl+'/filter?resoureType=Task').then(
				function(result){
					$scope.filters = result.data;
					try {
						angular.forEach($rootScope.projects, function(project){
							angular.forEach(project.processes, function(process){
								var processDefinitionKeyMap = [process.key];
								if(process.subprocesses && process.subprocesses.length > 0){
									var subprocessDefinitionKeyMap = _.map(process.subprocesses, 'key');
									processDefinitionKeyMap = _.concat(processDefinitionKeyMap, subprocessDefinitionKeyMap);
								}
								var query = {'processDefinitionKeyIn':processDefinitionKeyMap};

			 					angular.forEach($scope.filters, function(filter){
			 						process.filters = [];
									$http.post(baseUrl+'/filter/'+filter.id+'/count',query,{headers:{'Content-Type':'application/json'}}).then(
										function(results){
											var tmpfilter = angular.copy(filter);
											tmpfilter.itemCount = results.data.count;
											process.itemCount = process.itemCount ? process.itemCount + results.data.count : results.data.count;
											process.filters.push(tmpfilter);
											project.itemCount = project.itemCount ? project.itemCount + results.data.count : results.data.count;
										},
										function(error){
											console.log(error.data);
										}
									);
								});
							});
						});
					} catch(e){
						console.log(e);
					}
	
					loadTasks();
				},
				function(error){
					console.log(error.data);
				}
			);
		}


		function loadTasks() {
			if($scope.currentFilter){
				var processDefinitionKeyMap = [$rootScope.selectedProcess.key];
				if($rootScope.selectedProcess.subprocesses && $rootScope.selectedProcess.subprocesses.length > 0){
					var subprocessDefinitionKeyMap = _.map($rootScope.selectedProcess.subprocesses, 'key');
					processDefinitionKeyMap = _.concat(processDefinitionKeyMap, subprocessDefinitionKeyMap);
				}

				var queryData = {sorting:[{"sortBy":"created","sortOrder":"desc"}],'processDefinitionKeyIn':processDefinitionKeyMap};
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
								console.log($scope.tryToOpen);
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
/* ------------------------------------- Tasks new logic ------------------------------*/

	}]);
});