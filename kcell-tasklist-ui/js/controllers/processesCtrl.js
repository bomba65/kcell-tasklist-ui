define(['./module','jquery', 'camundaSDK'], function(app, $, CamSDK){
	'use strict';
	return app.controller('processesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal',
			                         function($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal) {
		
		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		$rootScope.currentPage = {
			name: 'processes'
		};

		$scope._ = window._;
		$scope.currentPI = [];
		$scope.participations = [{key:'initiator', label:'I am Inititator'},{key:'participant', label:'I am Participant'}];

		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		var baseUrl = '/camunda/api/engine/engine/default';
		$scope.processDefinitions = $rootScope.getCurrentProcesses();
		$scope.$watchGroup(['selectedProject', 'selectedProcess'], function(newValues, oldValues, scope) {
			if((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)){
        		$scope.processDefinitions = $rootScope.getCurrentProcesses();
        		if($scope.filter.processDefinitionKey && !_.some($scope.processDefinitions, function(pd){ return pd.key === $scope.filter.processDefinitionKey})){
					if($scope.processDefinitions.length > 0){
						$scope.filter.processDefinitionKey = $scope.processDefinitions[0].key;
						$scope.checkParticipation();
					}
					$scope.processInstances = undefined;
         		}
			}
		}, true);

		$scope.filter = {
			processDefinitionKey: $scope.processDefinitions[0].key,
			participation: 'initiator',
			startedBy: $rootScope.authentication.name,
			startedAfter: undefined,
			startedBefore: undefined,
			unfinished: true,
			page: 1,
			maxResults: 20
		};

		var catalogs = {};

        $http.get('/api/catalogs').then(
            function (result) {
                angular.extend(catalogs, result.data);
            },
            function (error) {
                console.log(error.data);
            }
        );

		$scope.search = function(refreshPages){
			if(refreshPages){
				$scope.filter.page = 1;
				$scope.piIndex = undefined;
			}
			$scope.currentPD = $scope.filter.processDefinitionKey;
			var filter = {
				processDefinitionKey: $scope.filter.processDefinitionKey,
				sorting:[{sortBy: "startTime",sortOrder: "desc"}]
			}
			if($scope.filter.businessKey){
				filter.processInstanceBusinessKey = $scope.filter.businessKey;
			}
			if($scope.filter.unfinished){
				filter.unfinished = true;
			} else {
				delete filter.unfinished;
			}
			if($scope.filter.participation === 'initiator'){
				filter.startedBy = $rootScope.authentication.name;
				getProcessInstances(filter);
			} else if($scope.filter.participation === 'participant') {
				$http.post(baseUrl+'/history/task',{taskAssignee: $rootScope.authentication.name}).then(
					function(result){
						filter.processInstanceIds = _.map(result.data, 'processInstanceId');
						getProcessInstances(filter);
					},
					function(error){
						console.log(error.data)
					}
				);
			} else if($scope.filter.participation) {
				console.log($scope.filter.participation);
				getProcessInstances(filter);
			}
		};

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

		$scope.checkParticipation = function(){
			if($scope.filter.processDefinitionKey !== 'Revision' && $scope.filter.participation === 'all'){
				$scope.filter.participation = undefined;
			}
		}

		function getProcessInstances(filter){
			$http.post(baseUrl+'/history/process-instance?firstResult=' + ($scope.filter.page-1)*$scope.filter.maxResults + '&maxResults=' + $scope.filter.maxResults, filter).then(
				function(result){
					$scope.processInstances = result.data;
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.toggleProcessView = function(index, processDefinitionKey){
		if(processDefinitionKey === 'SiteSharingTopProcess'){
			$scope.showDiagramView = false;
            $scope.diagram = {};
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
	            $scope.jobModel = {state: $scope.processInstances[index].state};
	            console.log('$scope.processInstances[index]');
	            console.log($scope.processInstances[index]);
	            $http.get(baseUrl+'/process-instance?superProcessInstance='+$scope.processInstances[index].id+'&active=true').then(
					function(result){
						if (result.data.length > 0) {
							$scope.currentPI[index] = result.data[0];
						} else {
							$scope.currentPI[index] = $scope.processInstances[index];
						}
						$http({
							method: 'GET',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							url: baseUrl+'/task?processInstanceId='+$scope.currentPI[index].id,
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
					            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.currentPI[index].id).then(
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
					            		console.log($scope.jobModel);
										workFiles.forEach(function(file){
											var workIndex = file.name.split('_')[1];
											if (!$scope.jobModel.jobWorks.value[workIndex].files) {
												$scope.jobModel.jobWorks.value[workIndex].files = [];
											}
											$scope.jobModel.jobWorks.value[workIndex].files.push(file);
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
					},
					function(error){
						console.log(error.data);
					}
				);	            
            }
		} else if (processDefinitionKey === 'freephone' || processDefinitionKey === 'bulksmsConnectionKAE'){
			$scope.showDiagramView = false;
            $scope.diagram = {};
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
	            $scope.jobModel = {state: $scope.processInstances[index].state};
	            console.log('$scope.processInstances[index]');
	            console.log($scope.processInstances[index]);
	            $http.get(baseUrl+'/process-instance?superProcessInstance='+$scope.processInstances[index].id+'&active=true').then(
					function(result){
						if (result.data.length > 0) {
							$scope.currentPI[index] = result.data[0];
							console.log($scope.currentPI)
						} else {
							$scope.currentPI[index] = $scope.processInstances[index];
						}
						$http({
							method: 'GET',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							url: baseUrl+'/task?processInstanceId='+$scope.currentPI[index].id,
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
					            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.currentPI[index].id).then(
					            	function(result){
					            		var workFiles = [];
					            		result.data.forEach(function(el){
					            			$scope.jobModel[el.name] = el;
					            			if(el.value || el.value === "" || el.type === 'Boolean') {
					            				$scope.jobModel[el.name] = el.value;
					            			}
					            			if(el.type === 'File' || el.type === 'Bytes'){
					            				$scope.jobModel[el.name].contentUrl = baseUrl+'/history/variable-instance/'+el.id+'/data';
					            			}
					            			if(el.name === 'resolutions'){
					            				$scope.jobModel[el.name] = JSON.parse(el.value);
					            			}
					            			if(el.type === 'Json' && el.name !== 'resolutions'){
					            				$scope.jobModel[el.name] = JSON.parse(el.value);	
					            			}
					            		});
					            		console.log($scope.jobModel);
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
					            		// angular.extend($scope.jobModel, catalogs);
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
					},
					function(error){
						console.log(error.data);
					}
				);	            
            }
		} else if (processDefinitionKey === 'UAT'){
			
            $scope.printDiv = function(forPrint) {
                var printContents = document.getElementById('forPrint').innerHTML;
                var popupWin = window.open('', 'PRINT', 'height=400,width=600');
                popupWin.document.open();
                popupWin.document.write('<html><head><link href="css/bootstrap.min.css" rel="stylesheet"><link href="css/styles.css" rel="stylesheet" type="text/css"></head><body onload="window.print()">' + printContents + '</body></html>');
                popupWin.document.close();
            }

			$scope.showDiagramView = false;
            $scope.diagram = {};
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
				
				$scope.jobModel = {state: $scope.processInstances[index].state};
				$scope.jobModel['businessKeyUAT'] = {value: $scope.processInstances[index].businessKey};
	            $http.get(baseUrl+'/process-instance?superProcessInstance='+$scope.processInstances[index].id+'&active=true').then(
					function(result){
						if (result.data.length > 0) {
							$scope.currentPI[index] = result.data[0];
							console.log($scope.currentPI)
						} else {
							$scope.currentPI[index] = $scope.processInstances[index];
						}
						$http({
							method: 'GET',
							headers:{'Accept':'application/hal+json, application/json; q=0.5'},
							url: baseUrl+'/task?processInstanceId='+$scope.currentPI[index].id,
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
					            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.currentPI[index].id).then(
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
					            		console.log($scope.jobModel);
										workFiles.forEach(function(file){
											var workIndex = file.name.split('_')[1];
											if (!$scope.jobModel.jobWorks.value[workIndex].files) {
												$scope.jobModel.jobWorks.value[workIndex].files = [];
											}
											$scope.jobModel.jobWorks.value[workIndex].files.push(file);
										});

										$http.get(baseUrl+'/history/process-instance/'+$scope.currentPI[index].id).then(function(pi) {
											if (pi.data.startTime) $scope.jobModel.startTime = {value: new Date(pi.data.startTime)};
											if (pi.data.endTime) $scope.jobModel.endTime = {value: new Date(pi.data.endTime)};
										});

    								if($scope.jobModel.resolutions && $scope.jobModel.resolutions.value){
    									$q.all($scope.jobModel.resolutions.value.map(function (resolution) {
			                                return $http.get("/camunda/api/engine/engine/default/user/" + resolution.assignee + "/profile");
			                            })).then(function (profiles) {
			                                profiles.forEach(function (e, index) {
			                                    // $scope.jobModel.resolutions[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
			                                    $scope.jobModel.resolutions.value[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
			                                });
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
				                    }
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
					},
					function(error){
						console.log(error.data);
					}
				);	            
            }
		} else {
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
						}
			            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.processInstances[index].id).then(
			            	function(result){
			            		var workFiles = [];
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
		}         
		};

		$scope.showDiagram = function(index, processDefinitionKey){
			if(['SiteSharingTopProcess','freephone','bulksmsConnectionKAE'].indexOf(processDefinitionKey) !== -1){
				console.log(index)
				console.log($scope.currentPI[index])
				if ($scope.currentPI[index].definitionId) {
					var processDefinitionId = $scope.currentPI[index].definitionId;
				} else {
					var processDefinitionId = $scope.currentPI[index].processDefinitionId;
				}
			}
			else {
				var processDefinitionId = $scope.processInstances[index].processDefinitionId;
			}
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
							console.log($scope.jobModel.tasks[0]);
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

        $scope.getStatus = function(state, value){
			return (state == 'COMPLETED' || state == 'EXTERNALLY_TERMINATED')? 'Closed': (value == 'accepted'?'Accepted & waiting scan attach':(value == 'scan attached'?'Accepted & waiting invoice':'In progress'))
        };

        $scope.hasPermissionToViewAll = function(processDefinitionKey){
			if (processDefinitionKey === 'Revision') {
				return $scope.hasOneOfListedGroup(['revision_managers','revision_audit']);
			} else if (processDefinitionKey === 'Invoice') {
				return $scope.hasOneOfListedGroup(['monthly_act_managers','monthly_act_audit']);
			} else return false;
        }

		$scope.hasOneOfListedGroup = function(groups){
			return _.some(groups, function(group){
				return $rootScope.hasGroup(group);
			});
		}

		$scope.hasLinkToTask = function(task, processDefinitionKey, accessGroups){
			if(task.processDefinitionId.substring(0,task.processDefinitionId.indexOf(':')) === processDefinitionKey){
				return $scope.hasOneOfListedGroup(accessGroups);
			} else 
				return false;
		}
	}]);
});
