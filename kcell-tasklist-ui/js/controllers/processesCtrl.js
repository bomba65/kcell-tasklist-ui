define(['./module','jquery', 'camundaSDK'], function(app, $, CamSDK){
	'use strict';
	return app.controller('processesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 
			                         function($scope, $rootScope, $http, $routeParams, $q, $location, $timeout) {
		
		
		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		//var processDefinitionService = new camClient.resource('process-definition');
		//var userService = new camClient.resource('user');
		//var groupService = new camClient.resource('group');
		//var taskService = new camClient.resource('task');

		$rootScope.currentPage = {
			name: 'processes'
		};
		$scope._ = window._;

		var baseUrl = '/camunda/api/engine/engine/default';

		$scope.filter = {
			processDefinitionKey: 'Revision',
			participation: 'initiator',
			startedBy: $rootScope.authentication.name,
			startedAfter: undefined,
			startedBefore: undefined,
			unfinished: true
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

		if($rootScope.authentication){
			$http.get(baseUrl+'/user/'+$rootScope.authentication.name+'/profile').then(
				function(userProfile){
					$rootScope.authUser = userProfile.data;
					$http.get(baseUrl+'/group?member='+$rootScope.authUser.id).then(
						function(groups){
							$rootScope.authUser.groups = groups.data;
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

		$rootScope.hasGroup = function(group){
			if($rootScope.authUser && $rootScope.authUser.groups){
				return _.some($rootScope.authUser.groups, function(value){
					return value.id === group;
				});
			} else {
				return false;
			}
		}

		//var historyService = new camClient.resource('history');

		$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=15').then(
			function(results){
				$scope.processDefinitions = results.data;
			},
			function(error){
				console.log(error.data);
			}
		);

		$scope.search = function(){
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
			} else {
				$http.post(baseUrl+'/history/task',{taskAssignee: $rootScope.authentication.name}).then(
					function(result){
						filter.processInstanceIds = _.map(result.data, 'processInstanceId');
						getProcessInstances(filter);
					},
					function(error){
						console.log(error.data)
					}
				);
			}
		};

		function getProcessInstances(filter){
			$http.post(baseUrl+'/history/process-instance', filter).then(
				function(result){
					$scope.processInstances = result.data;
				},
				function(error){
					console.log(error.data);
				}
			);
		}

		$scope.toggleProcessView = function(index){
			$scope.showDiagramView = false;
            $scope.diagram = {};
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
	            $scope.jobModel = {};
	            $http({
					method: 'GET',
					headers:{'Accept':'application/hal+json, application/json; q=0.5'},
					url: baseUrl+'/task?processInstanceId='+$scope.processInstances[index].id,
				}).then(
	            	function(tasks){
		            	var processInstanceTasks = tasks.data._embedded.task;
		            	if(processInstanceTasks && processInstanceTasks.length > 0){
							processInstanceTasks.forEach(function(e){
								if(e.assignee){
									for(var i=0;i<tasks.data._embedded.assignee.length;i++){
										if(tasks.data._embedded.assignee[i].id === e.assignee){
											e.assigneeObject = tasks.data._embedded.assignee[i];
										}
									}
								}
							});
						}
			            $http.get(baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.processInstances[index].id).then(
			            	function(result){
			            		result.data.forEach(function(el){
			            			$scope.jobModel[el.name] = el;
			            			if(el.type === 'File' || el.type === 'Bytes'){
			            				$scope.jobModel[el.name].contentUrl = baseUrl+'/history/variable-instance/'+el.id+'/data';
			            			}
			            			if(el.type === 'Json'){
			            				$scope.jobModel[el.name].value = JSON.parse(el.value);
			            			}
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
	}]);
});
