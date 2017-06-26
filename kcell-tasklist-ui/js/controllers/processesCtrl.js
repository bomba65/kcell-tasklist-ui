define(['./module','jquery', 'camundaSDK'], function(app, $, CamSDK){
	'use strict';
	return app.controller('processesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 
			                         function($scope, $rootScope, $http, $routeParams, $q, $location, $timeout) {
		
		
		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		var processDefinitionService = new camClient.resource('process-definition');
		var userService = new camClient.resource('user');
		var groupService = new camClient.resource('group');
		var taskService = new camClient.resource('task');

		$rootScope.currentPage = {
			name: 'processes'
		};
		$scope._ = window._;

		$scope.baseUrl = '/camunda/api/engine/engine/default';

		$scope.filter = {
			processDefinitionKey: 'Revision',
			participation: 'initiator',
			startedBy: $rootScope.authentication.name,
			startedAfter: undefined,
			startedBefore: undefined
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
			userService.profile($rootScope.authentication.name, function(err, userProfile){
				if(err){
					throw err;
				}
				$scope.$apply(function(){
					$rootScope.authUser = userProfile;
					groupService.list({member:$rootScope.authUser.id}, function(err, groups){
						$scope.$apply(function(){
							$rootScope.authUser.groups = groups;
						});
					});
				});
			});
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

		var historyService = new camClient.resource('history');

		processDefinitionService.list({latest:true, active:true, firstResult:0, maxResults:15}, function(err, results){
			if(err) {
				throw err;
			}
			$scope.$apply(function (){
				$scope.processDefinitions = results.items;
			});
		});

		$scope.search = function(){
			var filter = {
				processDefinitionKey: $scope.filter.processDefinitionKey,
				sorting:[{sortBy: "startTime",sortOrder: "desc"}],
				unfinished: true
			}
			if($scope.filter.businessKey){
				filter.processInstanceBusinessKey = $scope.filter.businessKey;
			}
			if($scope.filter.participation === 'initiator'){
				filter.startedBy = $rootScope.authentication.name;
				getProcessInstances(filter);
			} else {
				$http.post($scope.baseUrl+'/history/task',{taskAssignee: $rootScope.authentication.name}).then(
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
			historyService.processInstance(filter, function(err, result){
				$scope.$apply(function () {
					$scope.processInstances = result;
				});
			});
		}

		$scope.toggleProcessView = function(index){
			$scope.showDiagramView = false;
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
            }
            $scope.jobModel = {};
            taskService.list({processInstanceId:$scope.processInstances[index].id},function(err, tasks){
            	var processInstanceTasks = tasks._embedded.task;
            	if(processInstanceTasks && processInstanceTasks.length > 0){
					processInstanceTasks.forEach(function(e){
						if(e.assignee){
							for(var i=0;i<tasks._embedded.assignee.length;i++){
								if(tasks._embedded.assignee[i].id === e.assignee){
									e.assigneeObject = tasks._embedded.assignee[i];
								}
							}
						}
					});
				}
	            $http.get($scope.baseUrl+'/history/variable-instance?deserializeValues=false&processInstanceId='+$scope.processInstances[index].id).then(
	            	function(result){
	            		result.data.forEach(function(el){
	            			$scope.jobModel[el.name] = el;
	            			if(el.type === 'File' || el.type === 'Bytes'){
	            				$scope.jobModel[el.name].contentUrl = $scope.baseUrl+'/history/variable-instance/'+el.id+'/data';
	            			}
	            			if(el.type === 'Json'){
	            				$scope.jobModel[el.name].value = JSON.parse(el.value);
	            			}
	            		});
	            		angular.extend($scope.jobModel, catalogs);
	            		$scope.jobModel.tasks = processInstanceTasks;
	            		console.log($scope.jobModel.tasks);
	            	},
	            	function(error){}
	        	);
	        });
		};
		$scope.showDiagram = function(processDefinitionId){
			$scope.showDiagramView = true;
			getDiagram(processDefinitionId);
		}

		function getDiagram(processDefinitionId){
			processDefinitionService.xml({id:processDefinitionId}, function(err, result){
				if(err){
					throw err;
				}
				$timeout(function(){
					$scope.$apply(function(){
						console.log($scope.jobModel.tasks);
						$scope.diagram = {
							xml: result.bpmn20Xml,
							task: ($scope.jobModel.tasks && $scope.jobModel.tasks.length > 0) ? $scope.jobModel.tasks[0]:undefined
						};
					});
				});
			});
		}

		$scope.highlightTask = function() {
			console.log($scope.control);
			$scope.control.highlight($scope.diagram.task.taskDefinitionKey);
		};
	}]);
});
