define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('minioCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', 
		function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
		
			if ($scope.authUser.id !== 'demo' && !$rootScope.hasGroup('revisionAdmin')) {
				console.log('this is not demo or revision_admin');
				$scope.goHome();
			};

			var baseUrl = '/camunda/api/engine/engine/default';

			$rootScope.currentPage = {
				name: 'minio'
			};

			$scope.goHome = function(path) {
			    $location.path("/");
			};

			$scope.pathToFile = '';
			$scope.selectedFile = undefined;
			$scope.selected = 'scanCopy';
			$scope.foundProcesses = [];
			$scope.scanCopyFileValue = undefined;
			$scope.processTechnicalUpdates = "";
			var businessKey = "businessKey";

			$scope.changeSelected = function (selected){
				$scope.selected = selected;
			}

			$scope.selectFile = function (el){
                $scope.$apply(function () {
                    $scope.selectedFile = el.files[0];
                });
            };

            function uploadFileToMinio(file) {
                if ($scope.pathToFile.length > 1) {
	                $http({method: 'GET', url: '/camunda/uploads/admin/put/' + $scope.pathToFile + '/' + file.name, transformResponse: [] })
	                .then(function(response) {
	                    $http.put(response.data, file, {headers: {'Content-Type': undefined }}).then(
	                        function () {
	                            $scope.clearFile();
                				$scope.touchedFile = false;
                				$scope.pathToFile = '';
	                            alert(`${file.name} was successfully uploaded`);
	                        },
	                        function (error) {
	                        	console.log(`Could not upload ${file.name} to ${$scope.pathToFile}`);
	                        	console.log(error.data);
	                            alert(`Could not upload ${file.name} to ${$scope.pathToFile}`);
	                        }
	                    );
	                }, function(error){
	                    console.log(error.data);
	                    alert('No such file ' + file.name);
	                });
                }                
            }

            $scope.clearFile = function() {
                $scope.selectedFile = null;
                $scope.touchedFile = true;
            	angular.element(document.querySelector('#attachedFile')).val(null);
            };

            $scope.uploadFile = function () {
            	$scope.touchedFile = true;
            	$timeout(function () {
                    $scope.$apply(function () {
                        uploadFileToMinio($scope.selectedFile);
                    });
                });
            };

            $scope.uploadCancel = function() {
            	$scope.clearFile();
                $scope.touchedFile = false;
            	$scope.goHome();            	
            };

            $scope.searchBusinessKey = function(){
            	if($scope.businessKey && $scope.businessKey !== ''){
	            	businessKey = $scope.businessKey;
            	}

				$http.post(baseUrl+'/process-instance',{businessKey: businessKey, processDefinitionKey: 'Revision',
					active: true, activityIdIn: ['intermediate_wait_invoiced']}).then(
					function(result){
						if(result.data.length > 0){
							$scope.foundProcesses = result.data;
							var processInstanceIds = [];
							processInstanceIds.push($scope.foundProcesses[0].id);

							$http.post(baseUrl+'/history/variable-instance?deserializeValues=false', {processInstanceIdIn:processInstanceIds, 
								variableName: "scanCopyFile"}).then(
								function(varResult){
									$scope.scanCopyFileValue = JSON.parse(varResult.data[0].value);
								},
								function(error){
									console.log(error.data)
								}
							);
							$http.post(baseUrl+'/history/variable-instance?deserializeValues=false', {processInstanceIdIn:processInstanceIds, 
								variableName: "processTechnicalUpdates"}).then(
								function(varResult){
									$scope.processTechnicalUpdates = varResult.data[0].value;
								},
								function(error){
									console.log(error.data)
								}
							);
						}
					},
					function(error){
						console.log(error.data)
					}
				);            	
            }

            $scope.uploadAcceptanceFile = function () {
            	$timeout(function () {
                    $scope.$apply(function () {
                        uploadAcceptanceToMinio($scope.selectedFile);
                    });
                });
            };

			$rootScope.logout = function(){
				AuthenticationService.logout().then(function(){
					$scope.authentication = null;
				});
			}

            function uploadAcceptanceToMinio(file) {
                if ($scope.foundProcesses.length > 0 && $scope.scanCopyFileValue) {
                	var path = $scope.scanCopyFileValue.value.path;
                	path = path.substring(0, path.lastIndexOf('/'));
	                $http({method: 'GET', url: '/camunda/uploads/admin/put/' + path + '/' + file.name, transformResponse: [] })
	                .then(function(response) {
	                    $http.put(response.data, file, {headers: {'Content-Type': undefined }}).then(
	                        function () {
	                        	$scope.scanCopyFileValue.value.path = path  + '/' + file.name;
	                        	$scope.scanCopyFileValue.value.name = file.name;

	                        	var processTechnicalUpdates = ($scope.processTechnicalUpdates === "") ? $scope.comment : ($scope.processTechnicalUpdates + " " + $scope.comment);

								$http.post(baseUrl+'/process-instance/' + $scope.foundProcesses[0].id + '/variables', 
									{"modifications":
									    {
									    	"scanCopyFile": {"value": JSON.stringify($scope.scanCopyFileValue), type:'Json'},
									    	"processTechnicalUpdates": {"value": processTechnicalUpdates, type:'String'},
									    }
									}
								).then(
									function(result){
										toasty.success( "Данные успешно сохранены!");
										$scope.foundProcesses = [];
										$scope.scanCopyFileValue = undefined;
										$scope.selectedFile = undefined;
										$scope.businessKey = undefined;
										businessKey = 'businessKey';
										$scope.comment = undefined;
										$scope.processTechnicalUpdates = "";
						            	angular.element(document.querySelector('#attachedAcceptanceFile')).val(null);
									},
									function(error){
										console.log(error.data)
										toasty.success( error.data);
									}
								);
	                        },
	                        function (error) {
	                        	console.log(`Could not upload ${file.name} to ${path}`);
	                        	console.log(error.data);
	                            alert(`Could not upload ${file.name} to ${path}`);
	                        }
	                    );
	                }, function(error){
	                    console.log(error.data);
	                    alert('No such file ' + file.name);
	                });
                }                
            }

            //----------------------------------------------------------- Change Activity --------------------------------------

			 $scope.$watch('activityProcess', function (activityProcess) {
			 	$scope.userTasksMap = [];
			 	$scope.processInstanceId = undefined;
			 	$scope.cancelActivities = {};
			 	$scope.activityProcessTechnicalUpdates = "";
			 	$scope.loadProcessDefinitionActivities();
			 },true);


			$scope.loadProcessDefinitionActivities = function (){
				if($scope.activityProcess){
					$http.get(baseUrl + '/process-definition/key/' + $scope.activityProcess + '/xml')
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

			            var userTasks = getUserTasks(xml);
			            $scope.userTasksMap = userTasks;
			        });				
			    }
			}

            $scope.searchActivityBusinessKey = function(){
            	var activityBusinessKey = 'businessKey';
            	if($scope.activityBusinessKey && $scope.activityBusinessKey !== ''){
	            	activityBusinessKey = $scope.activityBusinessKey;
            	}

				$http.post(baseUrl+'/process-instance',{businessKey: activityBusinessKey, processDefinitionKey: $scope.activityProcess,
					active: true}).then(
					function(result){
						if(result.data.length > 0){
							$scope.foundProcesses = result.data;
							$scope.processInstanceId = $scope.foundProcesses[0].id;

							$http.get(baseUrl+'/process-instance/' + $scope.processInstanceId + '/activity-instances').then(
								function(activityResult){
					            	$scope.activityProcessActivities = [];
					            	_.forEach(activityResult.data.childActivityInstances, function(firstLevel) {
					            		if(firstLevel.activityType === 'subProcess'){
					            			_.forEach(firstLevel.childActivityInstances, function(secondLevel) {
												if(secondLevel.activityType !== 'multiInstanceBody') {
													$scope.activityProcessActivities.push(secondLevel);
							            		}
					            			});
					            		} else if(firstLevel.activityType !== 'multiInstanceBody') {
											$scope.activityProcessActivities.push(firstLevel);
					            		}
					            	});
								},
								function(error){
									console.log(error.data)
								}
							);
							$http.post(baseUrl+'/history/variable-instance?deserializeValues=false', {processInstanceId:$scope.processInstanceId, 
								variableName: "processTechnicalUpdates"}).then(
								function(varResult){
									$scope.activityProcessTechnicalUpdates = varResult.data[0].value;
								},
								function(error){
									console.log(error.data)
								}
							);
						}
					},
					function(error){
						console.log(error.data)
					}
				);            	
            }

            $scope.moveToActivity = function(){
            	var instructions = [];
            	for (var property in $scope.cancelActivities) {
            		if($scope.cancelActivities[property]){
            			instructions.push({type:'cancel',activityInstanceId:property});
            		}
				}

				if(instructions.length > 0){
            		instructions.push({type:'startBeforeActivity',activityId:$scope.activityTask});	

            		var modification = {
						skipCustomListeners: false,
						skipIoMappings: false,
						instructions: instructions
            		}
            		console.log(modification);

					$http.post(baseUrl+'/process-instance/' + $scope.processInstanceId + '/modification',modification).then(
						function(result){

                        	var activityProcessTechnicalUpdates = ($scope.activityProcessTechnicalUpdates === "") ? $scope.activityComment : ($scope.activityProcessTechnicalUpdates + " " + $scope.activityComment);
							$http.post(baseUrl+'/process-instance/' + $scope.processInstanceId + '/variables', 
								{"modifications":
								    {
								    	"processTechnicalUpdates": {"value": activityProcessTechnicalUpdates, type:'String'}
								    }
								}
							).then(
								function(result){
									$scope.activityComment = undefined;
								 	$scope.userTasksMap = [];
								 	$scope.processInstanceId = undefined;
								 	$scope.cancelActivities = {};
								 	$scope.activityBusinessKey = undefined;
									toasty.success('Process successfully modified');
								},
								function(error){
									console.log(error.data)
									toasty.success( error.data);
								}
							);
						},
			            function (error) {
							toasty.error('Process modification error');
			                console.log(error.data);
			            }
			        ); 			        
				} else {
					toasty.error('At least one cancel activity should be selected');
				}
            }
	}]);
});