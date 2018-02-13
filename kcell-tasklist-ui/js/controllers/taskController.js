define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('TaskCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		$scope.hasAssignPermission = false;

		var Authentication = function(data) {
			angular.extend(this, data);
		}
		init();
		function disableForm(){
			$("[name=kcell_form]").css("pointer-events", "none");
			$("[name=kcell_form]").css("opacity", "0.4");
		}
		function addFormButton(err, camForm, evt) {
			if (err) {
				throw err;
			}
			var $submitBtn = $('<button type="submit" class="btn btn-primary">Complete</button>').click(function (e) {
				$scope.view.submitted = true;
				if($scope.kcell_form.$valid){
					$(this).attr('disabled', true);
					if($scope.preSubmit){
						$scope.preSubmit().then(
							function(result){
								camForm.submit(function (err) {
									if (err) {
										$(this).removeAttr('disabled');
										toasty.error({title: "Could not complete task", msg: err});
										e.preventDefault();
										throw err;
									} else {
										$scope.preSubmit = undefined;
										$('#taskElement').html('');
										$scope.currentTask = undefined;
										$scope.$parent.getTaskList();
										$location.search({});
										$scope.submitted = false;
									}
								});
							},
							function(err){
								$(this).removeAttr('disabled');
								toasty.error({title: "Could not complete task", msg: err});
								e.preventDefault();
								throw err;
							}
						);
					} else {
						camForm.submit(function (err) {
							if (err) {
								$(this).removeAttr('disabled');
								toasty.error({title: "Could not complete task", msg: err});
								e.preventDefault();
								throw err;
							} else {
								$('#taskElement').html('');
								$scope.currentTask = undefined;
								$scope.$parent.getTaskList();
								$location.search({});
								$scope.preSubmit = undefined;
								$scope.submitted = false;
							}
						});
					}
				} else {
					toasty.error({title: "Could not complete task", msg: "Please fill required fields"});
				}
			});
			camForm.formElement.append($submitBtn);
		}

		var baseUrl = '/camunda/api/engine/engine/default';
		function init(){
			$http({
				method: 'GET',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				url: '/camunda/api/engine/engine/default/task/' + $stateParams.id
			}).then(
				function(result){
					if(result.data._embedded.user && result.data._embedded.user.length > 0){
						result.data.assigneeObject = result.data._embedded.user[0];
					}
					if(result.data._embedded.identityLink && result.data._embedded.identityLink.length > 0){
						result.data.candidateObject = result.data._embedded.identityLink.find(function (el) { return el.type === 'candidate'; });
					}
					if(result.data._embedded.processDefinition && result.data._embedded.processDefinition[0].key){
						$scope.processDefinitionKey = result.data._embedded.processDefinition[0].key;
					}
					initData(result.data);
				},
				function(error){
					$http.get('/camunda/api/engine/engine/default/history/task?taskId=' + $stateParams.id).then(
						function(result){
							if(result.data && result.data[0]){
								initHistoryData(result.data[0]);
							}
						},
						function(error){
							console.log('Not found in history');
						}
					);					
				}
			);
		}
		function initData(task){
			$scope.selectedTab = 'form';
			$scope.currentTask = task;
			$scope.$parent.currentTask = task;
			$scope.view.submitted = false;
			var taskId = task.id;
			$('#taskElement').html('');
			$scope.isHistoryOpen = false;
			if(task.assignee === $rootScope.authentication.name){
				$http.get(baseUrl+'/task/'+taskId+'/form').then(
					function(taskFormInfo) {
						var url = taskFormInfo.data.key.replace('embedded:app:', taskFormInfo.data.contextPath + '/');
						new CamSDK.Form({
							client: camClient,
							formUrl: url,
							taskId: taskId,
							containerElement: $('#taskElement'),
							done: addFormButton
						});
					},
					function(error){
						console.log(error.data);
					}
				);
			} else {
				$http.get(baseUrl+'/task/'+taskId+'/form').then(
					function(taskFormInfo) {
						$scope.isHistoryOpen = true;
						var url = taskFormInfo.data.key.replace('embedded:app:', taskFormInfo.data.contextPath + '/');
						new CamSDK.Form({
							client: camClient,
							formUrl: url,
							taskId: taskId,
							containerElement: $('#taskElement'),
							done: disableForm
						});
					},
					function(error) {
						console.log(error.data);
					}
				);
			}
		}
		function initHistoryData(task){
			var variableQuery = {
                processInstanceId: task.processInstanceId,
                variableName: 'resolutions'
            };
            $http.post('/camunda/api/engine/engine/default/history/variable-instance?deserializeValues=false', variableQuery).then(
				function(result){
					if(result.data && result.data[0] && result.data[0].value){
						$scope.resolution = JSON.parse(result.data[0].value).find(function (el) { return el.taskId === $stateParams.id; });
						$scope.historyTask = task;
					}
				}
			);					
		}

		$scope.claim = function(task) {
			$http.post(baseUrl+'/task/'+task.id+'/claim', {userId: $rootScope.authentication.name}).then(
				function(){
					$scope.tryToOpen = {};
					$scope.$parent.getTaskList();
					task.assigneeObject = $rootScope.authUser;
					task.assignee = $rootScope.authentication.name;
					init();
				},
				function(error){
					console.log(error.data);
				}
			);
		}
		$scope.unclaim = function(task) {
			$http.post(baseUrl+'/task/'+task.id+'/unclaim').then(
				function(){
					$scope.tryToOpen = {
						id: task.id
					};
					$scope.$parent.getTaskList();
					init();
				},
				function(error){
					console.log(error.data);
				}
			);
		}
		$scope.assignmentInProgress = false;
		$scope.dispayAssignField = function() {
			$scope.assignmentInProgress = true;
		}
		$scope.assign = function(keyCode, userId, task) {
			$scope.assignmentInProgress = false;
			if(keyCode === 13){
				$http.post(baseUrl+'/task/'+task.id+'/claim', {userId: userId}).then(
					function(){
						$scope.tryToOpen = {};
						$scope.$parent.getTaskList();
						task.assigneeObject = $rootScope.authUser;
						task.assignee = $rootScope.authentication.name;
						init();
					},
					function(error){
						console.log(error.data);
					}
				);
			}
		}
		$scope.selectedTab = 'form';
		$scope.selectTab = function(tab){
			$scope.selectedTab = tab;
			if(tab==='diagram'){
				$scope.getDiagram();
			}
		}
		$scope.getDiagram = function(){
			$http.get(baseUrl+'/process-definition/'+$scope.currentTask.processDefinitionId+'/xml').then(
				function(result){
					$timeout(function(){
						$scope.$apply(function(){
							$scope.diagram = {
								xml: result.data.bpmn20Xml,
								task: $scope.currentTask
							};
						});
					});
				},
				function(error) {
					console.log(error.data);
				}
			);
		}

		$scope.highlightTask = function() {
			$scope.control.highlight($scope.diagram.task.taskDefinitionKey);
		};
	}]);
});