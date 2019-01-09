define(['./module','jquery', 'moment', 'camundaSDK'], function(app, $, moment, CamSDK){
	'use strict';
	return app.controller('searchCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal', '$state', 'StartProcessService', 'SearchCurrentSelectedProcessService',
			                         function($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal, $state, StartProcessService, SearchCurrentSelectedProcessService) {

		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		var baseUrl = '/camunda/api/engine/engine/default';
		var catalogs = {};

		$rootScope.currentPage = {
			name: 'search'
		};

        if(window.require){
            $scope.XLSX = require('xlsx');
        }

		$scope._ = window._;
        $scope.regionsMap = {
            'alm': 'Almaty',
            'astana':'Astana',
            'nc': 'North & Center',
            'east': 'East',
            'south': 'South',
            'west': 'West'
        };
        $scope.contractorShortName = {
            '4': 'LSE'
        };
        $scope.reasonShortName = {
            '1': 'P&O',
            '2': 'TNU',
            '3': 'S&FM',
            '4': 'SAO'
        };
        $scope.processInstancesTotal = 0;
        $scope.processInstancesPages = 0;
        $scope.shownPages = 0;
        $scope.profiles = {};
        $scope.lastSearchParams;
        $scope.xlsxPrepared = false;

        var regionGroupsMap = {
        	'alm_kcell_users' : 'alm',
        	'astana_kcell_users' : 'astana',
        	'nc_kcell_users' : 'nc',
        	'east_kcell_users' : 'east',
        	'south_kcell_users' : 'south',
        	'west_kcell_users' : 'west'
        }

        $scope.$watchGroup(['selectedProject', 'selectedProcess'], function(newValues, oldValues, scope) {
			if((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)){
				$scope.piIndex = undefined;

				if(!($rootScope.isProcessAvailable('Revision') && $rootScope.isProcessVisible('Revision'))
			    && !($rootScope.isProjectAvailable('DeliveryPortal') && $rootScope.isProjectVisible('DeliveryPortal'))){
					$state.go('tasks');
                }

            }
		}, true);

		$http.get(baseUrl + '/process-definition/key/Revision/xml')
        .then(function(response) {
            var domParser = new DOMParser();

            var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

            function getUserTasks(xml) {
                var namespaces = {
                    bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
                };

                var userTaskNodes = [
                	...getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]),
                	...getElementsByXPath(xml, '//bpmn:intermediateCatchEvent', prefix => namespaces[prefix])
                ];

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

            var excludeTasks = [
            'signpr_by_center',
            'signpr_by_manager',
            'signpr_by_budgetowner',
            'signpr_by_cto',
            'signpr_by_cfo',
            'signpr_by_ceo',
            'Task_1ix12n7',
            'Task_1uvnb7n',
            'Task_12eq7hi',
            'Task_1wf6n5j',
            'Task_1puv0a9',
            'Task_1m2xspc',
            'Task_1mb15j2',
            'Task_1mocj2s',
            'Task_1gjdn28',
            'IntermediateThrowEvent_wait_po_pr_creation'
			];

            var userTasks = getUserTasks(xml);
            var includedUserTasks = _.filter(userTasks, function(task) {
			    return excludeTasks.indexOf(task.id) === -1;
			});
            $scope.includedUserTasks = includedUserTasks;
            var userTasksMap = _.keyBy(includedUserTasks, 'id');
            $scope.userTasksMap = userTasksMap;
        });

		if($rootScope.authentication){
			$http.get(baseUrl+'/user/'+$rootScope.authentication.name+'/profile').then(
				function(userProfile){
					$rootScope.authUser = userProfile.data;
					$http.get(baseUrl+'/group?member='+$rootScope.authUser.id).then(
						function(groups){
							$rootScope.authUser.groups = groups.data;

							if (!$rootScope.hasGroup('revision_managers') && !$rootScope.hasGroup('revision_audit')){
								_.forEach($rootScope.authUser.groups, function(group){
									if(regionGroupsMap[group.id]){
										$scope.filter.region = regionGroupsMap[group.id];
									}
								});
							}
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

        $http.get('/api/catalogs?force=4').then(
            function (result) {
                angular.extend($scope, result.data);
                angular.extend(catalogs, result.data);
                
            },
            function (error) {
                console.log(error.data);
            }
        );

		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		$scope.filter = {
			processDefinitionKey: 'Revision',
			businessKeyFilterType: 'eq',
			unfinished: false,
			page: 1,
			maxResults: 20
		};

        $scope.currentDate = new Date();
        $scope.filter.beginYear = $scope.currentDate.getFullYear()-1;
        $scope.filter.endYear = $scope.currentDate.getFullYear();
        $scope.years = [];

        for(var year=2017;year<=$scope.filter.endYear;year++){
			$scope.years.push(year);
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

        $scope.siteSelected = function($item){
            $scope.filter.sitename = $item.site_name;
        };

        $scope.siteIdSelected = function($item){
            $scope.filter.siteId = $item.name;
        };

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

        $scope.getSiteId = function(val) {
            return $http.get('/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name='+val).then(
                function(response){
                    var sites = _.flatMap(response.data._embedded.sites, function(s){
                        if(s.name){
                            return {
                                name: s.name,
                                id: s._links.self.href.substring(s._links.self.href.lastIndexOf('/')+1)
                            };
                        } else {
                            return [];
                        }
                    });
                    return sites;
                }
            );
        };

		$scope.search = function(refreshPages){
			if(refreshPages){
				$scope.filter.page = 1;
				$scope.piIndex = undefined;
				$scope.xlsxPrepared = false;
			}
			var filter = {
				processDefinitionKey: 'Revision',
				sorting:[{sortBy: "startTime",sortOrder: "desc"}],
				variables:[],
				processInstanceBusinessKeyLike:'%-%'
			}
			if($scope.filter.region && $scope.filter.region!=='all'){
				filter.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.filter.region});
			}
			if($scope.filter.siteId){
				filter.variables.push({"name": "siteName", "operator": "eq", "value": $scope.filter.siteId});
			}
			if($scope.filter.sitename){
				filter.variables.push({"name": "site_name", "operator": "eq", "value": $scope.filter.sitename});
			}
			if($scope.filter.businessKey){
				if($scope.filter.businessKeyFilterType === 'eq'){
					filter.processInstanceBusinessKey = $scope.filter.businessKey;
				} else {
					filter.variables.push({"name": "jrNumber", "operator": "like", "value": $scope.filter.businessKey + '%'});
				}
			}
			if($scope.filter.workType){
				filter.variables.push({"name": "reason", "operator": "eq", "value": $scope.filter.workType});
			}
			if($scope.filter.unfinished){
				filter.unfinished = true;
			} else {
				delete filter.unfinished;
			}
			if($scope.filter.beginYear){
				filter.startedAfter = $scope.filter.beginYear + '-01-01T00:00:00.000+0600';
			}
			if($scope.filter.endYear){
				filter.startedBefore = (Number($scope.filter.endYear) + 1) + '-01-01T00:00:00.000+0600';
			}
			if($scope.filter.requestedFromDate){
				filter.variables.push({"name": "requestedDate", "operator": "gteq", "value": $scope.filter.requestedFromDate});
			}
			if($scope.filter.requestedToDate){
				filter.variables.push({"name": "requestedDate", "operator": "lteq", "value": $scope.filter.requestedToDate});				
			}
			if($scope.filter.validityFromDate){
				filter.variables.push({"name": "validityDate", "operator": "gteq", "value": $scope.filter.validityFromDate});
			}
			if($scope.filter.validityToDate){
				filter.variables.push({"name": "validityDate", "operator": "lteq", "value": $scope.filter.validityToDate});				
			}
			if($scope.filter.priority){
				filter.variables.push({"name": "priority", "operator": "eq", "value": $scope.filter.priority});
			}
			if($scope.filter.workName){
				filter.variables.push({"name": "workTitlesForSearch", "operator": "like", "value": "%" + $scope.filter.workName + "%"});
			}
			if($scope.filter.requestor){
				filter.startedBy = $scope.filter.requestor;
			}
			if($scope.filter.activityId){
				filter.activeActivityIdIn = [$scope.filter.activityId];
			}
			$scope.lastSearchParams = filter;
			getProcessInstances(filter, 'processInstances');
		};

		$scope.clearFilters = function(){
			$scope.filter.region = 'all';
			$scope.filter.siteId = undefined;
			$scope.filter.sitename = undefined;
			$scope.filter.businessKey = undefined;
			$scope.filter.workType = undefined;
			$scope.filter.beginYear = $scope.currentDate.getFullYear();
			$scope.filter.endYear = $scope.currentDate.getFullYear();
			$scope.filter.requestedFromDate = undefined;
			$scope.filter.requestedToDate = undefined;
			$scope.filter.validityFromDate = undefined;
			$scope.filter.validityToDate = undefined;
			$scope.filter.requestor = undefined;
			$scope.filter.sitename = undefined;
			$scope.filter.priority = undefined;
			$scope.filter.activityId = undefined;
			$scope.filter.workName = undefined;
			$scope.filter.businessKeyFilterType = 'eq';
		}

		$scope.getXlsxProcessInstances = function(){
			if($scope.xlsxPrepared){
				var tbl = document.getElementById('xlsxRevisionsTable');
				var ws = XLSX.utils.table_to_sheet(tbl, {dateNF:'DD.MM.YYYY'});

                var wb = XLSX.utils.book_new();
                XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                return XLSX.writeFile(wb, 'revision-search-result.xlsx');
			} else {
				getProcessInstances($scope.lastSearchParams, 'xlsxProcessInstances');
				$scope.xlsxPrepared = true;
			}
		}

		$scope.getpreparedXlsxProcessInstances = function(){
				var tbl = document.getElementById('revisionsTable');
				var ws = XLSX.utils.table_to_sheet(tbl, {dateNF:'DD.MM.YYYY'});

                var wb = XLSX.utils.book_new();
                XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                return XLSX.writeFile(wb, 'revision-search-result.xlsx');			
		}

		function getProcessInstances(filter, processInstances){
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: filter,
				url: baseUrl+'/history/process-instance/count'
			}).then(
				function(result){
					$scope[processInstances + 'Total'] = result.data.count;
					$scope[processInstances + 'Pages'] = Math.floor(result.data.count / $scope.filter.maxResults) + ((result.data.count % $scope.filter.maxResults) > 0 ? 1 : 0);
				}
			);
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: filter,
				url: baseUrl+'/history/process-instance?firstResult=' + (processInstances === 'processInstances' ? ($scope.filter.page-1)*$scope.filter.maxResults + '&maxResults=' + $scope.filter.maxResults : '')
			}).then(
			function(result){
				$scope[processInstances] = result.data;
				var variables = ['siteRegion','site_name', 'contractor', 'reason', 'requestedDate', 'validityDate', 'jobWorks', 'explanation'];

				if($scope[processInstances].length > 0){
					angular.forEach($scope[processInstances], function(el) {
						if(el.durationInMillis){
							el['executionTime'] = Math.floor(el.durationInMillis / (1000*60*60*24));
						} else {
							var startTime = new Date(el.startTime);
							el['executionTime'] = Math.floor(((new Date().getTime()) - startTime.getTime()) / (1000*60*60*24));
						}
						if(!$scope.profiles[el.startUserId]){
					        $http.get(baseUrl+'/user/' + el.startUserId + '/profile').then(
					            function (result) {
									$scope.profiles[el.startUserId] = result.data;
					            },
					            function (error) {
					                console.log(error.data);
					            }
					        );
						}
					});

					_.forEach(variables, function(variable) {
						var varSearchParams = {processInstanceIdIn: _.map($scope[processInstances], 'id'), variableName: variable};
						$http({
							method: 'POST',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							data: varSearchParams,
							url: baseUrl+'/history/variable-instance?deserializeValues=false'
						}).then(
							function(vars){
								$scope[processInstances].forEach(function(el) {
									var f =  _.filter(vars.data, function(v) {
										return v.processInstanceId === el.id; 
									});
									if(f && f[0]){
										if(f[0].type === 'Json'){
											el[variable] = JSON.parse(f[0].value);
										} else {
											el[variable] = f[0].value;
										}
									}
								});
							},
							function(error){
								console.log(error.data);
							}
						);
					});
					var activeProcessInstances = _.filter($scope[processInstances], function(pi) { return pi.state === 'ACTIVE'; });
					var taskSearchParams = {processInstanceBusinessKeyIn: _.map(activeProcessInstances, 'businessKey'), active: true};
					$http({
						method: 'POST',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						data: taskSearchParams,
						url: baseUrl+'/task'
					}).then(
						function(tasks){
							angular.forEach($scope[processInstances], function(el) {
								var f =  _.filter(tasks.data, function(t) {
									return t.processInstanceId === el.id; 
								});
								if(f && f.length>0){
									el['tasks'] = f;
									_.forEach(el.tasks, function(task){
										if(task.assignee && !$scope.profiles[task.assignee]){
									        $http.get(baseUrl+'/user/' + task.assignee + '/profile').then(
									            function (result) {
									            	$scope.profiles[task.assignee] = result.data;
									            },
									            function (error) {
									                console.log(error.data);
									            }
									        );
										}
									});
								}
							});
						},
						function(error){
							console.log(error.data);
						}					
					);
					angular.forEach(activeProcessInstances, function(pi) {
				        $http.get(baseUrl+'/process-instance/' + pi.id + '/activity-instances').then(
				            function (result) {
				            	pi.otherActivities = [];
				            	_.forEach(result.data.childActivityInstances, function(firstLevel) {
				            		if(firstLevel.activityType === 'subProcess'){
				            			_.forEach(firstLevel.childActivityInstances, function(secondLevel) {
											if(secondLevel.activityType !== 'userTask' && secondLevel.activityType !== 'multiInstanceBody') {
												pi.otherActivities.push(secondLevel);
						            		}
				            			});
				            		} else if(firstLevel.activityType !== 'userTask' && firstLevel.activityType !== 'multiInstanceBody') {
										pi.otherActivities.push(firstLevel);
				            		}
				            	});
				            },
				            function (error) {
				                console.log(error.data);
				            }
				        );
					});
				}
			},
			function(error){
				console.log(error.data);
			});
		}

		$scope.nextPage = function(){
			$scope.filter.page++;
			$scope.search(false);
			$scope.piIndex = undefined;
		}

		$scope.prevPage = function(){
			$scope.filter.page--;
			$scope.search(false);
			$scope.piIndex = undefined;
		}

		$scope.selectPage = function(page){
			$scope.filter.page = page;
			$scope.search(false);
			$scope.piIndex = undefined;
		}

		$scope.getPages = function(){
			var array = [];
			if($scope.processInstancesPages < 8){
				for(var i=1;i<=$scope.processInstancesPages;i++){
					array.push(i);
				}
			} else {
				var decrease = $scope.filter.page-1;
				var increase = $scope.filter.page+1;
				array.push($scope.filter.page);
				while (increase - decrease < 8){
					if(decrease > 0){
						array.unshift(decrease--);
					}
					if(increase < $scope.processInstancesPages){
						array.push(increase++);
					}
				}
			}
			return array;
		}

		$scope.showTaskDetail = function(task){
			exModal.open({
				scope: {
					task: task
				},
				templateUrl: './js/partials/taskModal.html',
				controller: TaskModalController,
				size: 'md'
			}).then(function(results){
			});
		}

		$scope.showWorkDetail = function(jobWorks){
			exModal.open({
				scope: {
					jobWorks: jobWorks,
					worksTitle: $scope.worksTitle
				},
				templateUrl: './js/partials/jobWorksModal.html',
				size: 'lg'
			}).then(function(results){
			});
		}

		TaskModalController.$inject = ['scope', '$http'];
		function TaskModalController(scope, $http){
			$http({
				method: 'GET',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				url: baseUrl+'/task/'+scope.task.id
			}).then(
				function(result){
					if(result.data._embedded && result.data._embedded.group && result.data._embedded.group.length > 0){
						scope.task.group = result.data._embedded.group[0].id;
						$http({
							method: 'GET',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							url: baseUrl+'/user?memberOfGroup='+result.data._embedded.group[0].id
						}).then(
							function(r){
								scope.task.members = r.data;
							}
						);
					}
				}
			);	
		}
		
		$scope.toggleProcessView = function(index, processDefinitionKey){
			$scope.showDiagramView = false;
            $scope.diagram = {};
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
	            $scope.jobModel = {state: $scope.processInstances[index].state, processDefinitionKey: processDefinitionKey};
	            $http({
					method: 'GET',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					url: baseUrl+'/task?processInstanceId='+$scope.processInstances[index].id,
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
						}
			            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.processInstances[index].id).then(
			            	function(result){
			            		var workFiles = [];
			            		result.data.forEach(function(el){
									$scope.jobModel[el.name] = el;
			            			if(el.type === 'File' || el.type === 'Bytes'){
			            				$scope.jobModel[el.name].contentUrl = baseUrl+'/history/variable-instance/'+el.id+'/data';
			            			}
			            			if(el.type === 'Json'){
			            				$scope.jobModel[el.name].value = JSON.parse(el.value);
			            			}
			            			if(el.name.startsWith('works_') && el.name.includes('_file_')){
			            				workFiles.push(el);
			            			}
			            		});
	                            if($scope.jobModel['siteWorksFiles']){
	                                _.forEach($scope.jobModel['siteWorksFiles'].value, function(file){
	                                    var workIndex = file.name.split('_')[1];
	                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
	                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
	                                    }
	                                    if(_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function(f) { return f.name == file.name; }) < 0){
	                                        $scope.jobModel.jobWorks.value[workIndex].files.push(file);
	                                    }
	                                });
	                            }
								_.forEach(workFiles, function(file){
									var workIndex = file.name.split('_')[1];
									if (!$scope.jobModel.jobWorks.value[workIndex].files) {
										$scope.jobModel.jobWorks.value[workIndex].files = [];
									}
									if(_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function(f) { return f.name == file.name; }) < 0){
										$scope.jobModel.jobWorks.value[workIndex].files.push(file);									
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

								//$scope.jobModel.tasks = processInstanceTasks;
								angular.extend($scope.jobModel, catalogs);
			            		$scope.jobModel.tasks = processInstanceTasks;
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
		};

        $scope.showGroupDetails = function(group){
			$http({
				method: 'GET',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				url: baseUrl+'/user?memberOfGroup='+group
			}).then(
				function(result){
					exModal.open({
						scope: {
							members: result.data,
						},
						templateUrl: './js/partials/members.html',
						size: 'md'
					}).then(function(results){
					});
				},
				function(error){
					console.log(error.data);
				}
			);
        };
        $scope.getStatus = function(state, value){
        	return state == 'COMPLETED'? 'Closed': (value == 'accepted'?'Accepted & waiting scan attach':(value == 'scan attached'?'Accepted & waiting invoice':'In progress'))
        };

		$scope.showDiagram = function(processDefinitionId){
			$scope.showDiagramView = true;
			getDiagram(processDefinitionId);
		}

		function getDiagram(processDefinitionId){
			$http.get(baseUrl+'/process-definition/'+processDefinitionId+'/xml').then(
				function(result){
					$timeout(function(){
						$scope.$apply(function(){
							$scope.diagram = {
								xml: result.data.bpmn20Xml,
								tasks: ($scope.jobModel.tasks && $scope.jobModel.tasks.length > 0) ? $scope.jobModel.tasks : undefined
							};
						});
					});
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.highlightTask = function() {
			$scope.diagram.tasks.forEach((task=>{
				$scope.control.highlight(task.taskDefinitionKey);
			}));
		};

        $scope.showHistory = function(resolutions){
			exModal.open({
				scope: {
					resolutions: resolutions, //resolutions.value,
					isKcellStaff: $rootScope.hasGroup('kcellUsers')
				},
				templateUrl: './js/partials/resolutions.html',
				size: 'lg'
			}).then(function(results){
			});
        };	

        $scope.open = function ($event, dateFieldOpened) {
			$event.preventDefault();
            $event.stopPropagation();
			$scope[dateFieldOpened] = true;
		};
		
		// Delivery Portal
		$scope.processInstancesDPTotal = 0;
		$scope.processInstancesDPPages = 0;
		$scope.sortedProcessInstancesDP = [];

		var excludeTasksMap = {
			'PBX': [
				'checkQuestionnaire'
			],
			'freephone': [
				'UserTask_0a7shjt',
				'Task_09uj6n6',
				'UserTask_0woyg7k',
				'UserTask_1xsquz3',
				'Task_1iyjmab',
				'UserTask_14t2lw4',
				'UserTask_1jrkuqn'
			],
			'bulksmsConnectionKAE': [
				'Task_0km85gh',
				'Task_1cagjud',
				'Task_1a4tell',
				'Task_0807llk',
				'Task_0zconst',
				'Task_1ffdpes',
				'Task_1q0yosd',
				'Task_0gv08f6',
				'Task_1uz37wa',
				'Task_065funq',
				'UserTask_1nk8ri5',
				'UserTask_1xq5pie',
				'UserTask_012elxb',
				'UserTask_01gopqr',
				'UserTask_18jefcx',
				'UserTask_1iy9zim',
				'Task_1qswlt4',
				'SendTask_1wfg3ue',
				'Task_1257fik',
				'Task_0d7igwz'
			]
		};

		var startDate;
		var endDate;

		$('input[name="multipleDate"]').daterangepicker({
			startDate: startDate,
			endDate: endDate,
			autoUpdateInput: false,
			locale: {
			   format: 'DD/MM/YYYY',
			   cancelLabel: 'Clear'
			}
		}
		/*, function(start, end, label) {
			//$(this).val(picker.startDate.format('MM/DD/YYYY') + ' - ' + picker.endDate.format('MM/DD/YYYY'));
			console.log("Apply :: A new date selection was made: " + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD'));
			$scope.$apply(function() {
				$scope.startDate = start.toDate();
				$scope.endDate = end.toDate();
			});
		}*/
		);

		$('input[name="multipleDate"]').on('apply.daterangepicker', function(ev, picker) {
			$(this).val(picker.startDate.format('DD/MM/YYYY') + ' - ' + picker.endDate.format('DD/MM/YYYY'));
			//console.log("Apply :: A new date selection was made: " + picker.startDate.format('DD/MM/YYYY') + ' to ' + picker.endDate.format('DD/MM/YYYY'));
			$scope.$apply(function() {
				$scope.filterDP.startDate = picker.startDate.toDate();
				$scope.filterDP.endDate = picker.endDate.toDate();
				//console.log("Apply :: A new date selection was made: " + $scope.filterDP.startDate + ' to ' + $scope.filterDP.endDate);
			});
		});

		$('input[name="multipleDate"]').on('cancel.daterangepicker', function(ev, picker) {
			$(this).val('');
			//console.log("Cancel :: A new date selection was made: " + picker.startDate.format('DD/MM/YYYY') + ' to ' + picker.endDate.format('DD/MM/YYYY'));
			$scope.$apply(function() {
				$scope.filterDP.startDate = undefined;	//picker.start.toDate();
				$scope.filterDP.endDate = undefined;		//picker.end.toDate();
			});
		});

		$('#daterangepickerButton').on('click', function(){
			//console.log('button click', $('input[name="multipleDate"]'));
			$('input[name="multipleDate"]').click();
		});

		$scope.filterDP = {
			processDefinitionKey: '',
			processDefinitions: [{name: 'PBX', value: 'PBX'}, {name: 'Подключение IVR', value: 'freephone'}, {name: 'BulkSMS', value: 'bulksmsConnectionKAE'}],
			processDefinitionActivities: {},
			activityId: '',
			businessKey: '',
			//businessKeyFilterType: 'eq',
			initiatorId: '',
			unfinished: false,
			page: 1,
			maxResults: 20
		};

		$scope.userTasksDP = {};
		$scope.userTasksMapDP = {};
		$scope.clientBINMap = {};
		$scope.binPattern = /^(?:\d{12}|\w+@\w+\.\w{2,3})$/;

		if ($rootScope.selectedProject.key === "DeliveryPortal"){
			$scope.clientBINs = [];
			//if ($scope.clientBINs.length == 0) { // Initial load only; if previously between project switches did not load bins
				// since processDefinitionKey is available in Camunda only from version 7.9, which is not our current version
				// omitted looping through processDefinitionKeys and passing down to each request in a loop
				$http({
					method: 'POST',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					data: {
						variableName: 'clientBIN'
					},
					url: baseUrl+'/history/variable-instance'						
				}).then(response => {
					//$scope.clientBINs = [];
					response.data.forEach(r => {
						//$scope.clientBINMap[r.value] = true;
						$scope.clientBINs.push(Number(r.value));
					});
					//$scope.clientBINs = Object.keys($scope.clientBINMap);
				}).catch(e => null);
			//}			
		}

		$scope.getBIN = function(val) {
			return $scope.clientBINs;
		};

		$scope.handleProcessDefintionChange = function() {
			$scope.filterDP.activityId = undefined;			
		}
		
		$scope.filterDP.processDefinitions.forEach(def => {
			$http.get(baseUrl + '/process-definition/key/'+def.value+'/xml')
				.then(function(response) {
					if (response) {
						var domParser = new DOMParser();
						var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');
						var userTasks = getUserTasks(xml);
						var excludeTasks = excludeTasksMap[def.value];
						var includedUserTasks = _.filter(userTasks, function(task) {
							return excludeTasks.indexOf(task.id) === -1;
						});
						$scope.userTasksDP[def.value] = includedUserTasks;				
						var userTasksMap = _.keyBy(includedUserTasks, 'id');
						$scope.userTasksMapDP[def.value] = userTasksMap;
					}
				})
				.catch(e => null);			
		});

		function getUserTasks(xml) {
			var namespaces = {
				bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
			};

			var userTaskNodes = [
				...getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]),
				...getElementsByXPath(xml, '//bpmn:intermediateCatchEvent', prefix => namespaces[prefix])
			];

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

			//console.log('userTaskNodes', userTaskNodes);

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
		};

		$scope.showTaskList = function(tasks){
			exModal.open({
				scope: {
					tasks: tasks
				},
				templateUrl: './js/partials/taskListModal.html',
				size: 'lg'
			}).then(function(results){
			});
		}

		$scope.getUsers = function(val) {
			var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=%'+val+'%').then(
				function(response){
					var usersByFirstName = _.flatMap(response.data, function(s){
						if(s.id){
							return s.id.split(',').map(function(user){
								return {
									id: s.id,
									email: s.email.substring(s.email.lastIndexOf('/')+1),
									firstName: s.firstName,
									lastName: s.lastName,
									name: s.firstName + ' ' + s.lastName
								};
							})
						} else {
							return [];
						}
					});
					//return usersByFirstName;
					return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=%'+val+'%').then(
							function(response){
								var usersByLastName = _.flatMap(response.data, function(s){
									if(s.id){
										return s.id.split(',').map(function(user){
											return {
												id: s.id,
												email: s.email.substring(s.email.lastIndexOf('/')+1),
												firstName: s.firstName,
												lastName: s.lastName,
												name: s.firstName + ' ' + s.lastName
											};
										})
									} else {
										return [];
									}
								});
								return _.unionWith(usersByFirstName, usersByLastName, _.isEqual);
							}
					);
				}
			);
			return users;
		};

		$scope.userSelected = function($item, $model, $label){
			$scope.initiator = $item;
			$scope.initiatorId = $item.id;
		};

		$scope.binSelected = function($item){
			$scope.filterDP.bin = $item;
		};

		$scope.searchDP = function(refreshPages){
			if(refreshPages){
				$scope.filterDP.page = 1;
				$scope.piIndex = undefined;
				$scope.xlsxPrepared = false;
			}

			$scope.xlsxPrepared = true;
			$scope.sortedProcessInstancesDP = [];

			var filter = {
				processDefinitionKey: $scope.filterDP.processDefinitionKey,
				activeActivityIdIn: [],
				variables: []
			}

			if($scope.filterDP.businessKey){
				//filter.processInstanceBusinessKey = $scope.filterDP.businessKey;
				filter.processInstanceBusinessKeyLike = '%' + $scope.filterDP.businessKey + '%';
			}
			if($scope.filterDP.unfinished){
				filter.unfinished = true;
			} else {
				delete filter.unfinished;
			}
			if($scope.filterDP.startDate){
				filter.startedAfter = $scope.filterDP.startDate;
				//console.log('startDate', $scope.filterDP.startDate);
			}			
			if($scope.filterDP.endDate){
				var beforeDate = new Date($scope.filterDP.endDate);
				//beforeDate.setDate(beforeDate.getDate()+1); //In daterangepicker for endDate time is 23:59:59, so no need adding a day
				filter.startedBefore = beforeDate;
				//console.log('endDate', $scope.filterDP.endDate);
			}
			if($scope.filterDP.activityId){
				filter.activeActivityIdIn.push($scope.filterDP.activityId);
			}

			if($scope.filterDP.shortNumber){
				filter.variables.push({"name": "finalIDs", "operator": "like", "value": "%" + $scope.filterDP.shortNumber + "%"});
			}
			if($scope.filterDP.bin){
				filter.variables.push({"name": "clientBIN", "operator": "eq", "value": $scope.filterDP.bin.toString()});
			} else if( $('#bin').val() ) {
				filter.variables.push({"name": "clientBIN", "operator": "like", "value": "%" + $('#bin').val() + "%"});
			}
			if ($scope.filterDP.initiator) {
				if($scope.filterDP.participation === 'initiator'){
					filter.startedBy = $scope.filterDP.initiator.id;
					$scope.lastSearchParams = filter;
					getProcessInstancesDP(filter, 'processInstancesDP');
				} else if($scope.filterDP.participation === 'participant') {
					$http.post(baseUrl+'/history/task',{taskAssignee: $scope.filterDP.initiator.id}).then(
						function(result){
							filter.processInstanceIds = _.map(result.data, 'processInstanceId');
							$scope.lastSearchParams = filter;
							getProcessInstancesDP(filter, 'processInstancesDP');
						},
						function(error){
							console.log(error.data)
						}
					);
				} else {
					$http.post(baseUrl+'/history/task',{taskAssignee: $scope.filterDP.initiator.id}).then(
						function(result){
							filter.processInstanceIds = _.map(result.data, 'processInstanceId');
							filter.startedBy = $scope.filterDP.initiator.id;
							$scope.lastSearchParams = filter;
							getProcessInstancesDP(filter, 'processInstancesDP');
						},
						function(error){
							console.log(error.data)
						}
					);
					//$scope.lastSearchParams = filter;
					//getProcessInstancesDP(filter, 'processInstancesDP');
				}
			} else {
				$scope.lastSearchParams = filter;
				getProcessInstancesDP(filter, 'processInstancesDP');
			}
		};

		$scope.clearFiltersDP = function(){
			$scope.filterDP.bin = undefined;
			$scope.filterDP.shortNumber = undefined;
			$scope.filterDP.participation = undefined;
			$scope.filterDP.businessKey = undefined;
			$scope.filterDP.startDate = undefined;
			$scope.filterDP.endDate = undefined;
			$scope.filterDP.initiator = undefined;
			$scope.filterDP.initiatorId = undefined;
			$scope.filterDP.activityId = undefined;
			//$scope.filterDP.businessKeyFilterType = 'eq';
			$scope.filterDP.processDefinitionKey = undefined;
			$scope.filterDP.processDefinitionActivities = {};
			$scope.filterDP.unfinished = false;
			$('input[name="multipleDate"]').val('');
			$('#bin').val('');
		}

		$scope.getXlsxProcessInstancesDP = function(){
			//return $scope.ExcellentExport.convert({anchor: 'xlsxClick',format: 'xlsx',filename: 'delivery-portal'}, [{name: 'Process Instances',from: {table: 'xlsxDeliveryPortalTable'}}]);
			var tbl = document.getElementById('xlsxDeliveryPortalTable');
			var ws = XLSX.utils.table_to_sheet(tbl, {dateNF:'DD.MM.YYYY'});

            var wb = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

            return XLSX.writeFile(wb, 'delivery-portal-search-result.xlsx');
		}

		function getProcessInstancesDP(filter, processInstancesDP){
			var defs = $scope.filterDP.processDefinitions.filter(def => filter.processDefinitionKey ? filter.processDefinitionKey === def.value : true);
			var instanceCount = 0;
			var result = [];
			//console.log('filter', filter);
			function mapOrder (array, key) {
				var order = {};  
				array.sort( function (a, b) {
					var A = a[key], B = b[key];
					if (a.processDefinitionId.indexOf("freephone") > -1 ) {
						order = freephoneTaskOrder;
					} else if (a.processDefinitionId.indexOf("bulksmsConnectionKAE") > -1 ) {
						order = bulkSMSTaskOrder;
					} else if (a.processDefinitionId.indexOf("PBX") > -1 ) {
						order = PBXTaskOrder;
					}
					if (order[A] > order[B]) {
						return 1;
					} else {
						return -1;
					}					
				});
				return array;
			};

			var freephoneTaskOrder = {
				add_cost_and_status_of_number: 0, 
				match_identifier_with_client: 1, 
				create_application_to_connection: 2, 
				confirmBillingSystemsDeliveryIdentifiers: 3,
				title_create_from_PC: 4, 
				confirm_other_operators_creation_short_number: 5,
				check_other_operators_creation_short_number: 6,
				offnetSettings: 7, 
				UserTask_0djlmd5: 8, 
				UserTask_1slm9vl: 9, 
				UserTask_1srxk5z: 10,
				create_SIP_trank_on_SBC: 11, 
				firewall_access_confirm: 12,
				correct_access_to_firewall: 13,
				confirmTheSettingsAreCorrect: 14, 
				send_SIP_trank_preferences_to_client: 15,
				confirmSuccessOfTestsWithVPN: 16,
				start_tv_check: 17, 
				result_tv_check: 18,
				confirm_client_agreement_to_connect: 19,
				confirm_last_mile_start_construction: 20, 
				fillConnectionInformation: 21,
				confirm_last_mile_finish_construction: 22, 
				confirm_setting_number_on_MSS: 23,
				confirmSuccessOfTestsW: 24, 
				/*
				UserTask_14t2lw4, 
				UserTask_0woyg7k, 
				UserTask_0a7shjt, 
				UserTask_1jrkuqn, 
				UserTask_1xsquz3, 
				*/
			};

			var bulkSMSTaskOrder = {
				addCostAndStatusOfNumbers: 0,
				matchIdentifierWithClient: 1,
				createApplicationToConnection: 2,
				confirmBillingSystemsDeliveryIdentifiers: 3,
				createConnectionForm: 4,
				createTitlePC: 5,
				confirmOtherOperatorsCreationShortNumber: 6,
				checkOtherOperatorsCreationShortNumber: 7,
			
				smsbulkAccountInfo: 8,
				markTheConnecttionForm: 9,
				"firewallAccessConfirm.html": 10,
				correctAccessToFirewall: 11,
				sendPreferencesToClient: 12,
				confirmSuccessOfTests: 13,
				confirmGeneralReadiness: 14,
				shareWithClient: 15,
				/*
				Task_0km85gh, 
				Task_1cagjud, 
				Task_1a4tell, 
				Task_0807llk, 
				Task_0zconst, 
				Task_1ffdpes, 
				Task_1q0yosd, 
				Task_0gv08f6, 
				Task_1uz37wa, 
				Task_065funq, 
				UserTask_1nk8ri5, 
				UserTask_1xq5pie, 
				UserTask_012elxb, 
				UserTask_01gopqr, 
				UserTask_18jefcx, 
				UserTask_1iy9zim, 
				*/
			};

			var PBXTaskOrder = {
				checkQuestionnaire: 0,
				confirmCheckingTSScheme: 1,
				modifyQuestionnaire: 2,
				confirmAgreementTSScheme: 3,
				fillLegalInformation: 4, 
				confirmInTIC: 5,
				confirmCreatedRoot: 6,
				confirmCreatedPOI: 7,
				confirmNumbersInTIC: 8,
				confirmAdditionIP: 9,
				confirmSendingTest: 10,
				confirmTestingSuccess: 11,
				modifyConnectionSettings: 12,
				importClientTestCallData: 13,
				importBillingTestCallData: 14,
				confirmTestingBilling: 15,
				confirmStartService: 16,
				confirmCommercialService: 17,
				createRequestTCF: 18,
				confirmTCFRequest: 19,
				confirmContractReceiption: 20,
				e1confirmCheckingTSScheme: 21, 
				confirmReturn: 22, 
				confirmPaybackRisk: 23,
				e1confirmAgreementTUScheme: 24, 
				e1ModifyQuestionnaire: 25, 
				e1downloadContract: 26, 
				confirmTheEnd: 27,
				confirmTheChannel: 28,
				e1fillLegalInformation: 29,
				e1confirmLegalInformation: 30, 
				e1confirmCreatedRoot: 31, 
				e1confirmCreatedPOI: 32, 
				e1confirmInTIC: 33,
				e1confirmNumbersInTIC: 34, 
				e1confirmTestingBilling: 35, 
				e1createRequestTCF: 36
			}

			/*
			$q.all(defs.map(def => {
				return $http({
					method: 'POST',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					data: {
						...filter,
						...{ processDefinitionKey: def.value }
					},
					url: baseUrl+'/history/process-instance/count'
				});
			}))
			.then(result => {
				result.forEach(r => {
					instanceCount = instanceCount + r.data.count;
				});
				$scope[processInstancesDP + 'Total'] = instanceCount;
				$scope[processInstancesDP + 'Pages'] = Math.floor(instanceCount / $scope.filterDP.maxResults) + ((instanceCount % $scope.filterDP.maxResults) > 0 ? 1 : 0);
			});
			*/
			$scope.afterSalesIvrSmsDefinitionId = undefined;
			$http.get(baseUrl+'/process-definition/key/after-sales-ivr-sms').then(function(response){
				if(response && response.data){
					if(response.data.id){
						$scope.afterSalesIvrSmsDefinitionId = response.data.id;
					}
				}
			});

			$scope[processInstancesDP] = [];
			if (filter.startedBy !== undefined & filter.processInstanceIds !== undefined) {
				var processInstanceList = defs.reduce((r, e) => r.push(
					$http({
						method: 'POST',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						data: {
							...filter,
							processInstanceIds: undefined,
							...{ processDefinitionKey: e.value }
						},
						url: baseUrl+'/history/process-instance/'
					}),
					$http({
						method: 'POST',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						data: {
							...filter,
							startedBy: undefined,
							...{ processDefinitionKey: e.value }
						},
						url: baseUrl+'/history/process-instance/'
					})
				) && r,
				[])
			} else {
				var processInstanceList = defs.reduce((r, e) => r.push(
					$http({
						method: 'POST',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						data: {
							...filter,
							...{ processDefinitionKey: e.value }
						},
						url: baseUrl+'/history/process-instance'
					})
				) && r,
				[])
			}
			//console.log('processInstanceList', processInstanceList);

			/*$q.all(defs.map(def => {
				return $http({
					method: 'POST',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					data: {
						...filter,
						...{ processDefinitionKey: def.value }
					},
					url: baseUrl+'/history/process-instance'
				});
			}))*/
			$q.all(processInstanceList.map(def => {
				return def;
			}))
			.then(responseResult => {
				//console.log('responseResult', responseResult);
				responseResult.forEach(r => {
					result.push(...r.data);
				});
				//console.log('result', result);
				// exclude duplicates, since user can be as initiator and as participant of the same process instances: this is the case when it duplicates
				$scope[processInstancesDP] = result.filter((obj, index, self) => index === self.findIndex((t) => t.id === obj.id ) );
				// order by by startTime descending
				$scope[processInstancesDP] = $scope[processInstancesDP].sort(function(a,b){
					return new Date(b.startTime) - new Date(a.startTime);
				});

				instanceCount = $scope[processInstancesDP].length;
				//console.log('instanceCount', instanceCount);
				$scope[processInstancesDP + 'Total'] = instanceCount;
				$scope[processInstancesDP + 'Pages'] = Math.floor(instanceCount / $scope.filterDP.maxResults) + ((instanceCount % $scope.filterDP.maxResults) > 0 ? 1 : 0);

				if($scope[processInstancesDP].length > 0){
					var activeProcessInstancesDP = _.filter($scope[processInstancesDP], function(pi) { return pi.state === 'ACTIVE'; });
					var taskSearchParams = {processInstanceBusinessKeyIn: _.map(activeProcessInstancesDP, 'businessKey'), active: true};
					$http({
						method: 'POST',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						data: taskSearchParams,
						url: baseUrl+'/task'
					}).then(
						function(tasks){
							$scope[processInstancesDP].forEach(function(el) {
								var f = _.filter(tasks.data, function(t) {
									return t.processInstanceId === el.id; 
								});
								if(f && f.length>0){
									//el['tasks'] = f;
									el['tasks'] = mapOrder(f, 'taskDefinitionKey');
									_.forEach(el.tasks, function(task){
										if(task.assignee && !$scope.profiles[task.assignee]){
									        $http.get(baseUrl+'/user/' + task.assignee + '/profile').then(
									            function (result) {
									            	$scope.profiles[task.assignee] = result.data;
									            },
									            function (error) {
									                console.log(error.data);
									            }
									        );
										}
									});
								}
							});
						},
						function(error){
							console.log(error.data);
						}					
					);

					$scope[processInstancesDP].forEach(instance => {
						if ( ['freephone', 'bulksmsConnectionKAE'].indexOf(instance.processDefinitionKey) > -1 ) {
							$http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+instance.id).then(
								function(result){
									result.data.forEach(function(el){
										if(el.name === 'clientBIN'){
											instance.bin = el.value;
										}
										if(el.name === 'identifiers'){
											instance.identifiers = JSON.parse(el.value).map(identifier => identifier.title).toString();
										}
										//if(el.name === 'finalIDs'){
										//	if (el.type === "Json") {
										//		instance.finalIDs = JSON.parse(el.value).map(identifier => identifier.title).toString();
										//	} else {
										//		instance.finalIDs = el.value;
										//	}
										//}
									});
								},
								function(error){
									console.log(error.data);
								}
							);
						} else if (instance.processDefinitionKey === 'PBX') {
							$http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+instance.id).then(
								function(result){
									result.data.forEach(function(el){
										if(el.name === 'customerInformation'){
											instance.bin = JSON.parse(el.value).bin;
										}
									});
								},
								function(error){
									console.log(error.data);
								}
							);
						}
					});
				}
			},
			function(error){
				console.log(error.data);
			});
		}

		$scope.nextPageDP = function(){
			$scope.filterDP.page++;
			$scope.piIndex = undefined;
		}

		$scope.prevPageDP = function(){
			$scope.filterDP.page--;
			$scope.piIndex = undefined;
		}

		$scope.selectPageDP = function(page){
			$scope.filterDP.page = page;
			$scope.piIndex = undefined;
		}

		$scope.getPagesDP = function(){
			var array = [];
			if($scope.processInstancesDPPages < 8){
				for(var i=1;i<=$scope.processInstancesDPPages;i++){
					array.push(i);
				}
			} else {
				var decrease = $scope.filterDP.page-1;
				var increase = $scope.filterDP.page+1;
				array.push($scope.filterDP.page);
				while (increase - decrease < 8){
					if(decrease > 0){
						array.unshift(decrease--);
					}
					if(increase < $scope.processInstancesDPPages){
						array.push(increase++);
					}
				}
			}
			return array;
		}

		$scope.getPageInstancesDP = function(){
			if ($scope.processInstancesDP.length !== 0) {
				return $scope.processInstancesDP.slice(
                	($scope.filterDP.page-1)*$scope.filterDP.maxResults,
					$scope.filterDP.page*$scope.filterDP.maxResults
				);
            }
            return [];
        };

		$scope.toggleProcessViewDP = function(rowIndex, processDefinitionKey){
			$scope.showDiagramView = false;
			$scope.diagram = {};
			var index = ($scope.filterDP.page-1)*$scope.filterDP.maxResults+rowIndex;
			if (processDefinitionKey === 'freephone' || processDefinitionKey === 'bulksmsConnectionKAE' || processDefinitionKey === 'PBX'){
				if($scope.piIndex === index){
					$scope.piIndex = undefined;
				} else {
					$scope.piIndex = index;
					$scope.jobModel = {state: $scope.processInstancesDP[index].state, processDefinitionKey: processDefinitionKey};
					$scope.currentProcessInstance = SearchCurrentSelectedProcessService($scope.processInstancesDP[index]);
					$http({
						method: 'GET',
						headers:{'Accept':'application/hal+json, application/json; q=0.5'},
						url: baseUrl+'/task?processInstanceId='+$scope.processInstancesDP[index].id,
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
							}
							$http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.processInstancesDP[index].id).then(
								function(result){
									result.data.forEach(function(el){
										$scope.jobModel[el.name] = el;
										if(el.type !== 'Json' && (el.value || el.value === "" || el.type === 'Boolean')) {
					            			$scope.jobModel[el.name] = el.value;
					            		}
					            		if(el.type === 'File' || el.type === 'Bytes'){
											$scope.jobModel[el.name].contentUrl = baseUrl+'/history/variable-instance/'+el.id+'/data';
										}
										if(el.type === 'Json'){
											if(el.name === 'resolutions'){
												$scope.jobModel[el.name].value = JSON.parse(el.value);
											} else if(['contractScanCopyFileName', 'applicationScanCopyFileName', 'vpnQuestionnaireFileName'].indexOf(el.name) > -1) {
												if (!$scope.jobModel.files) {
													$scope.jobModel.files = [];
												}
												$scope.jobModel.files.push(JSON.parse(el.value));
											} else {
												$scope.jobModel[el.name] = JSON.parse(el.value);
											}
										}
									});
									//console.log('jobModel', $scope.jobModel);
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
									angular.extend($scope.jobModel, catalogs);
									$scope.jobModel.showTarif = true;
									$scope.jobModel.tasks = processInstanceTasks;
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
		};
		$scope.startProcess = function(id){
			StartProcessService(id);
		};
    }]);
});
