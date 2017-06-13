define(['./module','camundaSDK', 'lodash'], function(module, CamSDK, _){
	'use strict';
	return module.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$routeParams', '$timeout', '$location', 'exModal', function($scope, $rootScope, toasty, AuthenticationService, $routeParams, $timeout, $location, exModal) {
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

		var taskService = new camClient.resource('task');
		var userService = new camClient.resource('user');
		var filterService = new camClient.resource('filter');
		var processDefinitionService = new camClient.resource('process-definition');

		$scope.camForm = null;
		$scope.selectedTab = 'form';
		$scope.selectTab = function(tab){
			$scope.selectedTab = tab;
			if(tab==='diagram'){
				$scope.getDiagram();
			}
		}
		userService.profile($rootScope.authentication.name, function(err, userProfile){
			if(err){
				throw err;
			}
			$rootScope.authUser = userProfile;
		});

		if($routeParams.task){
			$scope.tryToOpen = {
				id: $routeParams.task
			}
		}
		$scope.getDiagram = function(){
			processDefinitionService.xml({id:$scope.currentTask.processDefinitionId}, function(err, result){
				if(err){
					throw err;
				}
				$timeout(function(){
					$scope.$apply(function(){
						$scope.diagram = {
							xml: result.bpmn20Xml,
							task: $scope.currentTask
						};
					});
				});
			});
		}
		function getTaskList(){
			filterService.list({itemCount:true,resoureType: 'Task'}, function(err, result){
				$scope.filters = result;
				if($scope.filters.length > 0 && $scope.currentFilter == undefined){
					$scope.currentFilter = $scope.filters[0];
				}
				loadTasks();
			});
		}
		$scope.selectFilter = function(filter){
			$scope.currentFilter = filter;
			loadTasks();
		}
		$scope.startProcess = function(id){
			processDefinitionService.startForm({id:id}, function(err, startFormInfo){
				if(startFormInfo.key){
					var url = startFormInfo.key.replace('embedded:app:', startFormInfo.contextPath + '/');
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
						taskService.list({processInstanceId:results.id}, function(err, tasks){
							if(tasks._embedded.task.length > 0){
								$scope.tryToOpen = tasks._embedded.task[0];
							} else {
								$scope.tryToOpen = results
							}
							getTaskList();
						});
					});
				} else {
					processDefinitionService.start({id:id}, function(err, results){
						taskService.list({processInstanceId:results.id}, function(err, tasks){
							if(tasks._embedded.task.length > 0){
								$scope.tryToOpen = tasks._embedded.task[0];
							} else {
								$scope.tryToOpen = results
							}
							getTaskList();
						});
					});
				}
			});

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
						if(scope.kcell_form.$valid){
							camForm.submit(function (err,results) {
								if (err) {
									toasty.error({title: "Could not complete task", msg: err});
									e.preventDefault();
									throw err;
								} else {
									$('#start-form-modal-body').html('');
									scope.$close(results);
								}
							});
						} else {
							toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
						}
					});
					camForm.formElement.append($submitBtn);
				}
			}
		}
		$rootScope.logout = function(){
			AuthenticationService.logout();
		}
		function loadProcessDefinitions(e){
			processDefinitionService.list({latest:true, active:true, firstResult:0, maxResults:15}, function(err, results){
				if(err) {
					throw err;
				}
				$scope.$apply(function (){
					$scope.processDefinitions = results.items;
				});
			});
		}
		function loadTasks(e) {
			filterService.getTasks({id:$scope.currentFilter.id,sorting:[{"sortBy":"created","sortOrder":"desc"}],"active":true}, function(err, results){
				if (err) {
					throw err;
				}
				$scope.$apply(function() {
					$scope.tasks = results._embedded.task;
					var selectedTask;
					if($scope.tasks && $scope.tasks.length > 0){
						$scope.tasks.forEach(function(e){
							if(e.assignee){
								for(var i=0;i<results._embedded.assignee.length;i++){
									if(results._embedded.assignee[i].id === e.assignee){
										e.assigneeObject = results._embedded.assignee[i];
									}
								}
							}
							if($scope.tryToOpen && e.assignee === $rootScope.authentication.name && e.processInstanceId === $scope.tryToOpen.id){
								selectedTask = e;
							}
						});
					}
					if(selectedTask){
						$scope.loadTaskForm(selectedTask);
					} else if($scope.tryToOpen){
						taskService.get($scope.tryToOpen.id, function(err, taskResult){
							if(err){
								throw err;
								console.log(err);
							}
							$scope.tryToOpen = undefined;
							if(taskResult.assignee){
								userService.profile(taskResult.assignee, function(err, userResult){
									taskResult.assigneeObject = userResult;
									$scope.loadTaskForm(taskResult);
								});
							} else{
								$scope.loadTaskForm(taskResult);
							}
						})
					}
				});
			});
		}
		function addFormButton(err, camForm, evt) {
			if (err) {
				throw err;
			}
			var $submitBtn = $('<button type="submit" class="btn btn-primary">Complete</button>').click(function (e) {
				$scope.view = {
					submitted : true
				};
				if($scope.kcell_form.$valid){
					camForm.submit(function (err) {
						if (err) {
							toasty.error({title: "Could not complete task", msg: err});
							e.preventDefault();
							throw err;
						} else {
							$('#taskElement').html('');
							$scope.currentTask = undefined;
							getTaskList();
							$location.search({});
						}
					});
				} else {
					toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
				}
			});
			camForm.formElement.append($submitBtn);
		}
		$scope.loadTaskForm = function(task) {
			$location.search({task:task.id});
			$scope.diagram = undefined;
			$scope.selectedTab = 'form';
			$scope.currentTask = task;
			$scope.view = {
				submitted: false
			}
			$scope.tryToOpen = undefined;
			$rootScope.kcell_form = undefined;
			var taskId = task.id;
			$('#taskElement').html('');
			$scope.isHistoryOpen = false;
			if(task.assignee === $rootScope.authentication.name){
				taskService.form(taskId, function(err, taskFormInfo) {
					var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
					new CamSDK.Form({
						client: camClient,
						formUrl: url,
						taskId: taskId,
						containerElement: $('#taskElement'),
						done: addFormButton
					});
				});
			} else {
				taskService.form(taskId, function(err, taskFormInfo) {
					$scope.isHistoryOpen = true;
					var url = taskFormInfo.key.replace('embedded:app:', taskFormInfo.contextPath + '/');
					new CamSDK.Form({
						client: camClient,
						formUrl: url,
						taskId: taskId,
						containerElement: $('#taskElement'),
						done: disableForm
					});
				});
			}
		}
		function disableForm(){
			$("[name=kcell_form]").css("pointer-events", "none");
			$("[name=kcell_form]").css("opacity", "0.4");
		}
		getTaskList();
		loadProcessDefinitions();

		$scope.highlightTask = function() {
			$scope.control.highlight($scope.diagram.task.taskDefinitionKey);
		};

		$scope.claim = function(task) {
			taskService.claim({taskId: task.id, userId: $rootScope.authentication.name}, function(err, result){
				if (err) {
					throw err;
				}
				$scope.tryToOpen = {
					id: task.processInstanceId
				};
				getTaskList();
			});
		}
		$scope.unclaim = function(task) {
			taskService.unclaim({taskId: task.id}, function(err, result){
				if (err) {
					throw err;
				}
				$scope.tryToOpen = {
					id: task.id
				};
				getTaskList();
			});
		}
	}]);
});