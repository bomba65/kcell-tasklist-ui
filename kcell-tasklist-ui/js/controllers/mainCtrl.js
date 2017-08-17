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

		$scope.camForm = null;
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

		if($routeParams.task){
			$scope.tryToOpen = {
				id: $routeParams.task
			}
		}
		function getTaskList(){
			$http.get(baseUrl+'/filter?itemCount=true&resoureType=Task').then(
				function(result){
					$scope.filters = result.data;
					if($scope.filters.length > 0 && $scope.currentFilter == undefined){
						$scope.currentFilter = $scope.filters[0];
					}
					loadTasks();
				},
				function(error){
					console.log(error.data);
				}
			);
		}
		$scope.selectFilter = function(filter){
			$scope.view.page = 1;
			$scope.currentTask = undefined;
			$location.search({task:undefined});
			$scope.currentFilter = filter;
			loadTasks();
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
		function loadProcessDefinitions(e){
			$http.get(baseUrl+'/process-definition?latest=true&active=true&firstResult=0&maxResults=15').then(
				function(results){
					$scope.processDefinitions = [];
					results.data.forEach(function(e){
						if(e.key === 'Revision' || e.key === 'SiteSharingTopProcess'){
							$scope.processDefinitions.push(e);
						}
					})
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
		function loadTasks(e) {
			$http({
				method: 'POST',
				headers:{'Accept':'application/hal+json, application/json; q=0.5'},
				data: {sorting:[{"sortBy":"created","sortOrder":"desc"}]},
				url: baseUrl+'/filter/'+$scope.currentFilter.id+'/list?firstResult='+(($scope.view.page-1)*$scope.view.maxResults) + '&maxResults=' + $scope.view.maxResults
			}).then(
				function(results){
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
				},
				function(error){
					console.log(error.data);
				}
			);
		}
		getTaskList();
		loadProcessDefinitions();

		$scope.getTaskList = getTaskList;
	}]);
});