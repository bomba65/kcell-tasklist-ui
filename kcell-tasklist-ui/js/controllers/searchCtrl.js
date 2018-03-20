define(['./module','jquery', 'camundaSDK'], function(app, $, CamSDK){
	'use strict';
	return app.controller('searchCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal',
			                         function($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal) {
		
		
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
            $scope.ExcellentExport = require('excellentexport');
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

        $http.get('/api/catalogs').then(
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
			unfinished: false,
			page: 1,
			maxResults: 20
		};

        var currentDate = new Date();
        $scope.filter.beginYear = currentDate.getFullYear();
        $scope.filter.endYear = currentDate.getFullYear();
        $scope.years = [];

        for(var year=2017;year<=$scope.filter.beginYear;year++){
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
			if($scope.filter.sitename){
				filter.variables.push({"name": "site_name", "operator": "eq", "value": $scope.filter.sitename});
			}
			if($scope.filter.businessKey){
				filter.processInstanceBusinessKey = $scope.filter.businessKey;
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
			if($scope.filter.requestor){
				filter.startedBy = $scope.filter.requestor;
			}
			$scope.lastSearchParams = filter;
			getProcessInstances(filter, 'processInstances');
		};

		$scope.getXlsxProcessInstances = function(){
			if($scope.xlsxPrepared){
				return $scope.ExcellentExport.convert({anchor: 'xlsxClick',format: 'xlsx',filename: 'revisions'}, [{name: 'Sheet Name Here 1',from: {table: 'xlsxRevisionsTable'}}]);
			} else {
				getProcessInstances($scope.lastSearchParams, 'xlsxProcessInstances');
				$scope.xlsxPrepared = true;
			}
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

				if($scope[processInstances].length > 0){
					$scope[processInstances].forEach(function(el) {
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
						if(el.durationInMillis){
							el['executionTime'] = Math.floor(el.durationInMillis / (1000*60*60*24));
						} else {
							var startTime = new Date(el.startTime);
							el['executionTime'] = Math.floor(((new Date().getTime()) - startTime.getTime()) / (1000*60*60*24));
						}	
					});

					_.forEach(['siteRegion','site_name', 'contractor', 'reason', 'requestedDate', 'validityDate', 'jobWorks'], function(variable) {
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
							$scope[processInstances].forEach(function(el) {
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
					_.forEach(activeProcessInstances, function(pi) {
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
			console.log(scope.task);
			$http({
				method: 'GET',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				url: baseUrl+'/task/'+scope.task.id
			}).then(
				function(result){
					console.log(result);
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
								task: ($scope.jobModel.tasks && $scope.jobModel.tasks.length > 0) ? $scope.jobModel.tasks[0]:undefined
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
			$scope.control.highlight($scope.diagram.task.taskDefinitionKey);
		};

        $scope.showHistory = function(resolutions){
			exModal.open({
				scope: {
					resolutions: resolutions.value,
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
    }]);
});
