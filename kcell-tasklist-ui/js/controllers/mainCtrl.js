define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$routeParams', '$timeout', '$location', 'exModal', '$http', '$state', function($scope, $rootScope, toasty, AuthenticationService, $routeParams, $timeout, $location, exModal, $http, $state) {
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

		var regions = {
			alm : 'Almaty',
			astana : 'Astana',
			east : 'East',
			nc : 'North & Center',
			south : 'South',
			west : 'West',
		};

		$scope.$watchGroup(['selectedProject', 'selectedProcess'], function(newValues, oldValues, scope) {
			if((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)){
	            getTaskList();
			}
		}, true);

		$scope.regionFilters = [];
		$scope.currentRegionFilter = undefined;

		$scope.selectedView = 'task';
		$scope.camForm = null;

		if($routeParams.task){
			$scope.tryToOpen = {
				id: $routeParams.task
			}
		}

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

		function getTaskList(){
			$http.get(baseUrl+'/filter?resoureType=Task').then(
				function(result){
					$scope.filters = result.data;
					angular.forEach($scope.filters, function(filter){
						var query = {'processDefinitionKeyIn':_.map($rootScope.getCurrentProcesses(), 'key')};
						$http.post(baseUrl+'/filter/'+filter.id+'/count',query,{headers:{'Content-Type':'application/json'}}).then(
							function(results){
								filter.itemCount = results.data.count;
							},
							function(error){
								console.log(error.data);
							}
						);
					});
					if($scope.filters.length > 0 && $scope.currentFilter == undefined){
						$scope.currentFilter = $scope.filters[0];
						if(($rootScope.isProcessAvailable('Revision') || $rootScope.isProcessAvailable('Invoice')) && $rootScope.hasGroup('head_kcell_users')){
							loadRegionCount();
						}
					}
					loadTasks();
				},
				function(error){
					console.log(error.data);
				}
			);
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
				if($rootScope.hasGroup('head_kcell_users')){
					loadRegionCount();
				}
				loadTasks();
			}
		}
		$scope.selectRegionFilter = function(filter, region){
			$scope.currentTask = undefined;
			$location.search({task:undefined});
			$scope.selectedView = 'task';
			$scope.view.page = 1;
			$scope.currentRegionFilter = region;
			loadTasks();
		}

		$scope.openTask = function(taskId){
			$scope.selectedView = 'task';
			$state.go('tasks.task', {id:taskId});
		}

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
		}
		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		function loadProcessDefinitions(){
			$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=30').then(
				function(results){
					$scope.processDefinitions = [];
					results.data.forEach(function(e){
						if($rootScope.isProcessAvailable(e.key)){
							$http.get(baseUrl+'/authorization/check?permissionName=CREATE_INSTANCE&permissionValue=256&resourceName=Process Definition&resourceType=6&resourceId=' + e.key).then(
								function(result){
									if(result && result.data && result.data.authorized){
										$scope.processDefinitions.push(e);
									}
								},
								function(error){
									console.log(error.data);
								}
							);
						}
					});
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.nextTasks = function(){
			$scope.view.page++;
			loadTasks();
		}

		$scope.prevTasks = function(){
			$scope.view.page--;
			loadTasks();
		}
		function loadTasks() {
			$scope.processDefinitionsTasks = {};
			angular.forEach($rootScope.getCurrentProcesses(), function(process){
				$scope.processDefinitionsTasks[process.key] = {name:process.name, count:0, open:false,taskDefinitionKeys:{}};
 			});

			var queryData = {sorting:[{"sortBy":"created","sortOrder":"desc"}],'processDefinitionKeyIn':_.map($rootScope.getCurrentProcesses(), 'key')};
			if($scope.currentRegionFilter){
				queryData.processVariables = [{'name': 'siteRegion', "operator": "eq","value": $scope.currentRegionFilter.id}];
			}
			var currentProcesses = _.keyBy($rootScope.getCurrentProcesses(), 'key');
			for(var propt in currentProcesses){
				currentProcesses[propt].taskGroups = {};
			}
			$scope.currentProcesses = [];

			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: queryData,
				url: baseUrl+'/filter/'+$scope.currentFilter.id+'/list'
			}).then(
				function(results){
					$scope.tasks = results.data._embedded.task;
					var selectedTask;

					if($scope.tasks && $scope.tasks.length > 0){
						$scope.tasks.forEach(function(e){
							if(!currentProcesses[e.processDefinitionId.substring(0,e.processDefinitionId.indexOf(':'))].taskGroups[e.name]){
								currentProcesses[e.processDefinitionId.substring(0,e.processDefinitionId.indexOf(':'))].taskGroups[e.name] = {tasks:[], };
							}
							currentProcesses[e.processDefinitionId.substring(0,e.processDefinitionId.indexOf(':'))].taskGroups[e.name].tasks.push(e);

							if(e.assignee){
								for(var i=0;i<results.data._embedded.assignee.length;i++){
									if(results.data._embedded.assignee[i].id === e.assignee){
										e.assigneeObject = results.data._embedded.assignee[i];
									}
								}
							}
							if($scope.tryToOpen && e.assignee === $rootScope.authentication.name && e.processInstanceId === $scope.tryToOpen.id){
								selectedTask = e;
							}
						});
						$scope.currentProcesses = [];
						for(var propt in currentProcesses){
							var currentProcess = angular.copy(currentProcesses[propt]);
							delete currentProcess.taskGroups;
							currentProcess.taskGroups = [];
							currentProcess.taskCount = 0;

							for(var taskPropt in currentProcesses[propt].taskGroups){
								var taskGroup = angular.copy(currentProcesses[propt].taskGroups[taskPropt]);
								taskGroup.name = taskPropt;
								currentProcess.taskGroups.push(taskGroup);
								currentProcess.taskCount+=taskGroup.tasks.length;
							}
							$scope.currentProcesses.push(currentProcess);
						}
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
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.setSelectedProcessKey = function(key){
			if($scope.selectedProcessKey === key){
				$scope.selectedProcessKey = undefined;
			} else {
				$scope.selectedProcessKey = key;
			}
			$scope.currentTaskGroup = undefined;
		}

		$scope.setSelectedTaskGroupName = function(currentTaskGroup, selectedProcessDisplay){
			$scope.currentTask = undefined;
			$location.search({task:undefined});
			$scope.selectedView = 'tasks';

			$scope.currentTaskGroup = currentTaskGroup;
			$scope.currentTaskGroup.selectedProcessDisplay = selectedProcessDisplay;

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
                    		task.process = process;
                    	});
                    },
                    function(error){
                        console.log(error.data);
                    }
            );
		}

		$scope.getCurrentProjectDisplay = function(){
			var project = _.find($rootScope.projects, function(project){
				return _.find(project.processes, function(process){
					return process.key === $scope.selectedProcessKey;
				});
			});
			if(project){
				return project.name;
			} else {
				return '';
			}
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

        $scope.taskIds = [{id:'all', label:'All'},{id:'attach_material_list_contractor', label:'Attach Material List'},{id:'upload_tr_contractor', label:'Upload TR'},{id:'fill_applied_changes_info', label:'Fill Applied Changes Info'}];
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

		loadProcessDefinitions();
		getTaskList();

		$scope.getTaskList = getTaskList;
	}]);
});