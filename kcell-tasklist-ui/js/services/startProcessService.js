define(['./module','camundaSDK'], function(module, CamSDK){
	'use strict';
	return module.service('StartProcessService', ['$rootScope', 'toasty', '$timeout', '$location', 'exModal', '$http', function($rootScope, toasty, $timeout, $location, exModal, $http) {
		var camClient = new CamSDK.Client({
			mock: false,
			apiUri: '/camunda/api/engine/'
		});
		var baseUrl = '/camunda/api/engine/engine/default';
		return function(id){
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
										$rootScope.tryToOpen = task;
									}
									$rootScope.$broadcast('getTaskListEvent');
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
											$rootScope.tryToOpen = tasks.data[0];
										} else {
											$rootScope.tryToOpen = results.data
										}
										$rootScope.$broadcast('getTaskListEvent');
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
												///////////////////
												camForm.submit(function (err,results) {
													if (err) {
														$submitBtn.removeAttr('disabled');
														toasty.error({title: "Coguld not complete task", msg: err});
														e.preventDefault();
														throw err;
													} else {
														console.log('SEND 107');
														toasty.success({title: "Info", msg: " Your form has been successfully processed"});
														$('#start-form-modal-body').html('');
														scope.$close(results);

													}
												});
												/*
												try {
													camForm.submit(function (err) {
														if (err) {
															console.log('+++++++++++++++++++++++++++++++++');
															$submitBtn.removeAttr('disabled');
															toasty.error({title: "Could not complete task", msg: err});
															e.preventDefault();
															throw err;
													 	} else {
															console.log('---------------------------------');
															scope.preSubmit = undefined;
															$('#taskElement').html('');
															scope.currentTask = undefined;
															scope.$parent.getTaskList();
															$location.search({});
															scope.submitted = false;
													 	}
													});
												} catch(e){
													console.log(e);
												}
												*/
												///////////////////
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
												console.log('SEND 154');
												toasty.success({title: "Info", msg: " Your form has been successfully processed"});
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




	}]);
});