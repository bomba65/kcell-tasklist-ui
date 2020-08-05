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

		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$rootScope.authentication = null;
			});
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
		$scope.catalogs = {};

		$scope.reverseOrder = false;
		$scope.fieldName = 'businessKey';
		$scope.fieldFilter = {};
		$scope.visibilityFilter = {};

		$scope.invoiceContractorFilter = {};
        $scope.currentDate = new Date();
		$scope.invoiceContractorFilter.beginYear = $scope.currentDate.getFullYear() - 1;
        $scope.invoiceContractorFilter.endYear = $scope.currentDate.getFullYear();
        $scope.years = [];
        for (var year = 2017; year <= $scope.invoiceContractorFilter.endYear; year++) {
            $scope.years.push(year);
        }
        $scope.regionsMap = {
            'alm': 'Almaty',
            'astana': 'Astana',
            'nc': 'North & Center',
            'east': 'East',
            'south': 'South',
            'west': 'West'
        };
        $scope.contractorsSearchResults = [];

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

		getAllTaskNodes();

		function getAllTaskNodes(){
			$http.get(baseUrl + '/process-definition/key/Revision/xml')
				.then(function(response) {
					var domParser = new DOMParser();

					var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

					function getUserTasks(xml) {
						var namespaces = {
							bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
						};

						var userTaskNodes = getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]);

						function getElementsByXPath(doc, xpath, namespaceFn, parent) {
							let results = [];
							let query = doc.evaluate(xpath,
								parent || doc,
								namespaceFn,
								XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
							for (let i=0, length=query.snapshotLength; i<length; ++i) {
								results.push(query.snapshotItem(i));
							}
							return results;
						}

						return userTaskNodes.map(node => {
							var id = node.id;
							var name = node.attributes["name"] && node.attributes["name"].textContent;
							var description = getElementsByXPath(
								xml,
								'bpmn:documentation/text()',
								prefix => namespaces[prefix],
								node
							)[0];

							description = description && description.textContent;

							return {
								"id" : id,
								"name" : name,
								"description": description
							};
						});
					}

					var userTasks = getUserTasks(xml);
					var userTasksMap = _.keyBy(userTasks, function(v) {
						return v.id
					})
					var userTasksFiltered = _.filter(userTasksMap, function(v){
						if (v.description){
							return v.description.indexOf('{') < 0
						}
					})
					$scope.taskIds = [{id:'all', name: 'All'}]
					$scope.taskIds = $scope.taskIds.concat(userTasksFiltered);
				});
		}
		$scope.searchForContractors = function(refresh){
			$scope.searchProcessesForContractors(refresh);
			// if($scope.accepted){
			// 	$scope.searchProcessesForContractors();
			// } else {
			// 	$scope.searchTasksForContractors();
			// }
		}

        // $scope.searchTasksForContractors = function(){
        // 	var queryParams = {processDefinitionKey:'Revision'};
		// 	if($scope.site && $scope.site_name){
        // 		queryParams.processVariables = [{name:"site", value:$scope.site, operator: "eq"}];
        // 	}
        // 	if($scope.taskId !== 'all'){
        // 		queryParams.taskDefinitionKey = $scope.taskId;
        // 	} else {
        // 		var taskIdList = _.filter($scope.taskIds, function(n) {
		// 		  return n.id !== 'all';
		// 		});
		// 		queryParams.taskDefinitionKeyIn = _.map(taskIdList, 'id');
        // 	}
		//
        // 	if($scope.priority === 'emergency'){
        // 		queryParams.priority = 100;
        // 	}
		// 	queryParams.processVariables = [];
        // 	if($scope.bussinessKey){
        // 		queryParams.processVariables.push({name:"jrNumber", value:$scope.bussinessKey, operator: "eq"});
        // 	}
		// 	queryParams.processVariables.push({name:"contractorJobAssignedDate", value:new Date(), operator: "lteq"});
		//
		// 	queryParams.processVariables.push({name:"contractor", value: 4, operator: "eq"});
		//
        // 	queryParams.candidateUser = $rootScope.authUser.id;
        // 	queryParams.includeAssignedTasks = true;
		// 	$scope.searchResults = [];
		// 	$http({
		// 		method: 'POST',
		// 		headers:{'Accept':'application/hal+json, application/json; q=0.5'},
		// 		data: queryParams,
		// 		url: baseUrl+'/task'
		// 	}).then(
		// 		function(results){
		// 			$scope.searchResults = _.filter(results.data, function(d) {
		// 				return !d.assignee || d.assignee === $rootScope.authUser.id;
		// 			});
		//
		// 			if($scope.searchResults.length > 0){
		// 				_.forEach(['jrNumber','site_name', 'validityDate'], function(variable) {
		// 					var varSearchParams = {processInstanceIdIn: _.map($scope.searchResults, 'processInstanceId'), variableName: variable};
		// 					$http({
		// 						method: 'POST',
		// 						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
		// 						data: varSearchParams,
		// 						url: baseUrl+'/variable-instance'
		// 					}).then(
		// 						function(vars){
		// 							$scope.searchResults.forEach(function(el) {
		// 								var f =  _.filter(vars.data, function(v) {
		// 									return v.processInstanceId === el.processInstanceId;
		// 								});
		// 								if(f){
		// 									el[variable] = f[0].value;
		// 								}
		// 							});
		// 						},
		// 						function(error){
		// 							console.log(error.data);
		// 						}
		// 					);
		// 				});
		// 			}
		// 		},
		// 		function(error){
		// 			console.log(error.data);
		// 		}
		// 	);
        // };
		$scope.getExcelFile = function () {
			if($scope.xlsxPreparedRevision) {
				var tbl = document.getElementById('revisionsSearchTask');
				var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});
				var wb = XLSX.utils.book_new();
				XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
				return XLSX.writeFile(wb, 'revision-search-task-result.xlsx');
			} else {
				$scope.searchProcessesForContractors(false, true);
				$scope.xlsxPreparedRevision = true;
			}

		}

        $scope.searchFilter = {
            page: 1,
            pageSize: 20
        };

		$scope.processInstancesTotal = 0;
		$scope.processInstancesPages = 0;

		$scope.nextPage = function () {
			$scope.searchFilter.page++;
			$scope.searchForContractors(false);
			$scope.piIndex = undefined;
		}

		$scope.prevPage = function () {
			$scope.searchFilter.page--;
			$scope.searchForContractors(false);
			$scope.piIndex = undefined;
		}

		$scope.selectPage = function (page) {
			$scope.searchFilter.page = page;
			$scope.searchForContractors(false);
			$scope.piIndex = undefined;
		}

		$scope.getPages = function () {
			var array = [];
			if ($scope.processInstancesPages < 8) {
				for (var i = 1; i <= $scope.processInstancesPages; i++) {
					array.push(i);
				}
			} else {
				var decrease = $scope.searchFilter.page - 1;
				var increase = $scope.searchFilter.page + 1;
				array.push($scope.searchFilter.page);
				while (increase - decrease < 8) {
					if (decrease > 0) {
						array.unshift(decrease--);
					}
					if (increase < $scope.processInstancesPages) {
						array.push(increase++);
					}
				}
			}
			return array;
		}

		$scope.contractorTasksNames = ['Attach Material List', 'Upload TR', 'Fill Applied Changes Info', 'Attach Additional Material List', 'Upload additional TR'];
		$scope.contractorTasksIds = ['attach_material_list_contractor', 'upload_tr_contractor', 'fill_applied_changes_info', 'attach_additional_material_list_contractor', 'upload_additional_tr_contractor'];

        $scope.searchProcessesForContractors = async function(refresh, skipPagination){
			var queryParams = {processDefinitionKey: 'Revision', variables: []};
			var taskDefKey;

            if(refresh){
                $scope.searchFilter = {
                    page: 1,
                    pageSize: 20
                };
				$scope.xlsxPreparedRevision = false;
            }

            if(!skipPagination){
				$scope.xlsxPreparedRevision = false;
				$scope.xlsFilter = $scope.searchFilter;
			}

			if(!$scope.accepted){
				queryParams.variables.push({name:"contractorJobAssignedDate", value:new Date(), operator: "lteq"});
				queryParams.variables.push({name:"contractor", value: 4, operator: "eq"});
				if($scope.taskId && $scope.taskId !== 'all'){
					taskDefKey = $scope.taskId;
					queryParams.activityIdIn = [$scope.taskId];
				}
			}
			if($scope.site && $scope.site_name){
        		queryParams.variables.push({name:"site", value:$scope.site, operator: "eq"});
        	}
        	if($scope.priority === 'emergency'){
        		queryParams.variables.push({name:"priority", value:"emergency", operator: "eq"});
        	}
        	if($scope.bussinessKey){
        		queryParams.businessKey = $scope.bussinessKey;
        	}
        	if($scope.accepted) {
				queryParams.activityIdIn = ['attach-scan-copy-of-acceptance-form','intermediate_wait_acts_passed','intermediate_wait_invoiced']
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

			if(skipPagination){
				$scope.processSearchResultsXsl = [];
			} else {
				$scope.processSearchResults = [];
			}
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: queryParams,
                url: baseUrl + '/process-instance/count'
			}).then(function(countResults){
				$scope.processInstancesTotal = countResults.data.count;
				$scope.processInstancesPages = Math.floor(countResults.data.count / $scope.searchFilter.pageSize) + ((countResults.data.count % $scope.searchFilter.pageSize) > 0 ? 1 : 0)
			    $http({
                    method: 'POST',
                    headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                    data: queryParams,
                    url: baseUrl + '/process-instance?firstResult=' + (!skipPagination ? (($scope.searchFilter.page - 1) * $scope.searchFilter.pageSize + '&maxResults=' + $scope.searchFilter.pageSize) : '')
                }).then(function(results){
					var processSearchResults = 'processSearchResults';
                	if(skipPagination){
						processSearchResults = 'processSearchResultsXls';
					}
                    $scope[processSearchResults] = results.data;
                    if($scope[processSearchResults].length > 0){
                        _.forEach(['site_name', 'priority', 'validityDate', 'requestedDate','starter'], function(variable) {
                            var varSearchParams = {processInstanceIdIn: _.map($scope[processSearchResults], 'id'), variableName: variable};
                            $http({
                                method: 'POST',
                                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                                data: varSearchParams,
                                url: baseUrl+'/variable-instance'
                            }).then(
                                function(vars){
                                    $scope[processSearchResults].forEach(function(el) {
                                        var f =  _.filter(vars.data, function(v) {
                                            return v.processInstanceId === el.id;
                                        });
                                        if(f){
                                            el[variable] = f[0].value;
                                            if(variable === 'starter'){
                                                $http.get(baseUrl + '/user/' + f[0].value + '/profile').then(
                                                    function (result) {
                                                        el[variable] = result.data.firstName + " " + result.data.lastName;
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            }

                                        }
                                    });

                                },
                                function(error){
                                    console.log(error.data);
                                }
                            );
                        });
                        if(!$scope.accepted) {
							$scope[processSearchResults].forEach(function(el) {
								$http({
									method: 'GET',
									headers:{'Accept':'application/hal+json, application/json; q=0.5'},
									url: baseUrl+'/process-instance/' + el.id + '/activity-instances'
								}).then(function(activities){
									if(!el.tasks){
										el.tasks = [];
									}
									_.forEach(activities.data.childActivityInstances, function (firstLevel) {
										if (firstLevel.activityType === 'subProcess') {
											_.forEach(firstLevel.childActivityInstances, function (secondLevel) {
												if(secondLevel.childActivityInstances && secondLevel.childActivityInstances.length > 0) {
													_.forEach(secondLevel.childActivityInstances, function (thirdLevel) {
														if (thirdLevel.activityName && $scope.contractorTasksIds.indexOf(thirdLevel.activityId) === -1) {
															el.tasks.push({id: thirdLevel.id, name: thirdLevel.activityName});
														}
													});
												} else {
													if (secondLevel.activityName && $scope.contractorTasksIds.indexOf(secondLevel.activityId) === -1) {
														el.tasks.push({id: secondLevel.id, name: secondLevel.activityName});
													}
												}
											});
										} else {
											if(firstLevel.activityName && $scope.contractorTasksIds.indexOf(firstLevel.activityId) === -1){
												el.tasks.push({id: firstLevel.id, name:firstLevel.activityName});
											}
										}
									});
								});

								$http({
									method: 'POST',
									headers:{'Accept':'application/hal+json, application/json; q=0.5'},
									data: {processInstanceId: el.id, taskDefinitionKeyIn:$scope.contractorTasksIds},
									url: baseUrl+'/task'
								}).then(
									function(tasks){
										if(!el.tasks){
											el.tasks = [];
										}
										_.forEach(tasks.data, function (task) {
											el.tasks.push({id: task.id, name:task.name});
										});
									}
								);

							});
                        } else {
                            $scope[processSearchResults].forEach(function(el) {
                                $http({
                                    method: 'GET',
                                    headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl+'/process-instance/' + el.id + '/activity-instances'
                                }).then(function(activities){
                                	console.log(activities);
                                    activities.data.childActivityInstances.forEach(function(act) {
                                        if(['attach-scan-copy-of-acceptance-form','intermediate_wait_acts_passed','intermediate_wait_invoiced'].indexOf(act.activityId)!==-1){
                                            el.activityName = act.activityName;
                                        }
                                    });
                                });
                            });
                        }
                    }
                });
            });
        }

        $scope.toggleProcessViewRevision = async function(p) {
            $scope.jobModel = {
                state: 'Active',
                processDefinitionKey: 'Revision',
                startTime: p.requestedDate
            };
            await $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/task?processInstanceId=' + p.id,
            }).then(
                async function (tasks) {
                    var processInstanceTasks = tasks.data._embedded.task;
					if(processInstanceTasks && processInstanceTasks.length > 0){
						for (var k = 0; k < processInstanceTasks.length; k++) {
							var e = processInstanceTasks[k]
							if (e.assignee && tasks.data._embedded.assignee) {
								for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
									if (tasks.data._embedded.assignee[i].id === e.assignee) {
										e.assigneeObject = tasks.data._embedded.assignee[i];
									}
									await $http({
										method: 'GET',
										headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
										url: baseUrl + '/task/' + e.id
									}).then(
										function (taskResult) {
											if (taskResult.data._embedded && taskResult.data._embedded.group) {
												e.group = taskResult.data._embedded.group[0].id;
											}
										},
										function (error) {
											console.log(error.data);
										}
									);
								}
							}
							await $http({
								method: 'GET',
								headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
								url: baseUrl + '/task/' + e.id
							}).then(
								function (taskResult) {
									if (taskResult.data._embedded && taskResult.data._embedded.group) {
										e.group = taskResult.data._embedded.group[0].id;
									}
								},
								function (error) {
									console.log(error.data);
								}
							);
						};
						$scope.jobModel.tasks = processInstanceTasks;
					}
                    await $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + p.id).then(
                        async function (result) {
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
                            angular.extend($scope.jobModel, $scope.catalogs);
							// for(var i = 0; i< processInstanceTasks.length;i++) {
							// 	processInstanceTasks[i].assigneeObject = await $scope.getUserById(processInstanceTasks[i])
							// }
                            // $scope.jobModel.tasks = processInstanceTasks;

                            openProcessCardModalRevision(p);
                        },
                        function (error) {
                            console.log(error.data);
                        }
                    );
                    console.log('processInstanceTasks: ', processInstanceTasks)

                },
                function (error) {
                    console.log(error.data);
                }
            );
	    }

		$scope.getUserById = async function(task) {
			var user = null
			if (task.assignee){
				user = await $http({
					method: 'GET',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					url: baseUrl+'/user/?id=' + task.assignee
				}).then(function(results){
					// var index = _.findIndex($scope.jobModel.tasks, function(v){
					// 	return v.processInstanceId = task.processInstanceId
					// })
					// $scope.jobModel.tasks[index].assigneeObject = results.data[0]
					return results.data[0]
				})
			}
			return user
		}
			// open modal on task search with process info
        // $scope.openTaskCardModalRevision = async function(task) {
        // 	await $scope.searchProcessesForContractors(true)
		// 	var process = _.filter($scope.processSearchResults, function(v){
		// 		return v.id === task.processInstanceId
		// 	})
		// 	await $scope.toggleProcessViewRevision(process[0], true);
		// 	await $scope.getUserById(task);
		// 	openProcessCardModalRevision(process[0])
		//
		// }
		$scope.showGroupDetails = function (group) {
            $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/user?memberOfGroup=' + group
            }).then(
              function (result) {
                  exModal.open({
                      scope: {
                          members: result.data,
                      },
                      templateUrl: './js/partials/members.html',
                      size: 'md'
                  }).then(function (results) {
                  });
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
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
			$scope.secondLevel = "closed";
        }

        $scope.collapseProcess = function(process) {
			$rootScope.updateSelectedProcess(process);
			if ($rootScope.selectedTask && process!==$rootScope.selectedTask.key) {
				$rootScope.updateSelectedTask(undefined);
			}
			$scope.currentFilter = undefined;
			$scope.taskGroups = {};
			getTaskList();
			$scope.secondLevel = "closed";
        }
		$scope.collapseTask = function(task) {
			$rootScope.updateSelectedTask(task);
			$scope.collapseLevels('secondLevel');
		}

		$scope.clearContractorFilters = function(){
			$scope.invoiceContractorFilter = {
				beginYear: $scope.currentDate.getFullYear() - 1,
        		endYear: $scope.currentDate.getFullYear(),
        		region: 'all',
        		businessKeyFilterType: 'eq',
        		businessKey: undefined,
        		workType: undefined,
        		unfinished: undefined
			}
		}

		$scope.searchContractorInvoices = function(){
        	var queryParams = {processDefinitionKey:'Invoice', variables: []};
			if($scope.invoiceContractorFilter.beginYear){
	            queryParams.startedAfter = $scope.invoiceContractorFilter.beginYear + '-01-01T00:00:00.000+0600';
        	}
			if($scope.invoiceContractorFilter.endYear){
                queryParams.startedBefore = (Number($scope.invoiceContractorFilter.endYear) + 1) + '-01-01T00:00:00.000+0600';
        	}
            if ($scope.invoiceContractorFilter.businessKey) {
                if ($scope.invoiceContractorFilter.businessKeyFilterType === 'eq') {
                    queryParams.processInstanceBusinessKey = $scope.invoiceContractorFilter.businessKey;
                } else {
                    queryParams.processInstanceBusinessKeyLike = '%'+$scope.invoiceContractorFilter.businessKey+'%';
                }
            }
            if($scope.invoiceContractorFilter.workType){
				queryParams.variables.push({name:"workType", value:$scope.invoiceContractorFilter.workType+'', operator: "eq"});
            }
            if($scope.invoiceContractorFilter.unfinished){
				queryParams.unfinished = $scope.invoiceContractorFilter.unfinished;
            }
			if($rootScope.hasGroup('hq_contractor_lse') && $scope.invoiceContractorFilter.region!=='all'){
				queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.invoiceContractorFilter.region});
            } else {
	 			if($rootScope.hasGroup('astana_contractor_lse') && $rootScope.hasGroup('nc_contractor_lse')){
	                if(!$scope.invoiceContractorFilter.region || ['astana','nc'].indexOf($scope.invoiceContractorFilter.region)===-1){
	                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
	                    $scope.invoiceContractorFilter.region = 'astana';
	                } else {
	                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.invoiceContractorFilter.region});
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
            }
            queryParams.variables.push({"name": "contractor", "operator": "eq", "value": 'lse'});

	        $scope.piIndex = undefined;
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: queryParams,
				url: baseUrl+'/history/process-instance'
			}).then(function(results){
				$scope.contractorsSearchResults = results.data;
				if($scope.contractorsSearchResults.length > 0){
					_.forEach(['yearOfFormalPeriod', 'workType', 'monthActNumber'], function(variable) {
						var varSearchParams = {processInstanceIdIn: _.map($scope.contractorsSearchResults, 'id'), variableName: variable};
						$http({
							method: 'POST',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							data: varSearchParams,
							url: baseUrl+'/history/variable-instance'
						}).then(
							function(vars){
								$scope.contractorsSearchResults.forEach(function(el) {
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
				}
			});
		}

		$scope.toggleProcessView = function(index, id, processDefinitionKey){
			console.log(id);
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
	            $scope.jobModel = {state: $scope.contractorsSearchResults[index].state, processDefinitionKey: processDefinitionKey};
	            $http({
					method: 'GET',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					url: baseUrl+'/task?processInstanceId='+id,
				}).then(
	            	function(tasks){
		            	var processInstanceTasks = tasks.data._embedded.task;
		            	if(processInstanceTasks && processInstanceTasks.length > 0){
							processInstanceTasks.forEach(function(e){
								if(e.assignee && tasks.data._embedded.assignee){
									for(var i=0;i<tasks.data._embedded.assignee.length;i++){
										if(tasks.data._embedded.assignee[i].id === e.assignee){
											e.assigneeObject = tasks.data._embedded.assignee[i];
										}
										$http({
											method: 'GET',
											headers:{'Accept':'application/hal+json, application/json; q=0.5'},
											url: baseUrl+'/task/'+e.id
										}).then(

											function(taskResult){
												if(taskResult.data._embedded && taskResult.data._embedded.group){
													e.group = taskResult.data._embedded.group[0].id;
												}
											},
											function(error){
												console.log(error.data);
											}
										);
									}
								}
								$http({
									method: 'GET',
									headers:{'Accept':'application/hal+json, application/json; q=0.5'},
									url: baseUrl+'/task/'+e.id
								}).then(
									function(taskResult){
										if(taskResult.data._embedded && taskResult.data._embedded.group){
											e.group = taskResult.data._embedded.group[0].id;
										}
									},
									function(error){
										console.log(error.data);
									}
								);
							});
		            		$scope.jobModel.tasks = processInstanceTasks;
						}
			            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+id).then(
			            	function(result){
			            		$scope.jobModel.files = [];
			            		result.data.forEach(function(el){
			            			$scope.jobModel[el.name] = el;
			            			if(el.type === 'File' || el.type === 'Bytes'){
			            				$scope.jobModel[el.name].contentUrl = baseUrl+'/history/variable-instance/'+el.id+'/data';
			            			}
			            			if(el.type === 'Json'){
										if(el.name === 'attachInvoiceFileName') {
											$scope.jobModel.files.push(JSON.parse(el.value));
										}
										if(el.name === 'signedScanCopyFileName') {
											$scope.jobModel.files.push(JSON.parse(el.value));
										} else {
											$scope.jobModel[el.name].value = JSON.parse(el.value);
										}
									}
			            		});
			                    if($scope.jobModel.resolutions && $scope.jobModel.resolutions.value){
			                        $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
			                            return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId="+resolution.processInstanceId+"&taskId=" + resolution.taskId);
			                        })).then(function (tasks) {
			                            tasks.forEach(function (e, index) {
			                                if(e.data.length > 0){
			                                    $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
			                                    try {
			                                        $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
			                                    } catch(e){
			                                        console.log(e);
			                                    }
			                                }
			                            });
			                        });
								}
			            		angular.extend($scope.jobModel, $scope.catalogs);
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
								if($rootScope.selectedProcess.key && $rootScope.selectedProcess.key === process.key){
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
				 				}
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
        $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
            function (result) {
                angular.extend($scope.catalogs, result.data);
            },
            function (error) {
                console.log(error.data);
            }
        );
	}]);
});
