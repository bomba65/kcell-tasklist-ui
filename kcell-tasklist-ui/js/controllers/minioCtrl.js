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
				$http.post(baseUrl+'/process-instance',{businessKey: $scope.businessKey, processDefinitionKey: 'Revision',
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

								$http.post(baseUrl+'/process-instance/' + $scope.foundProcesses[0].id + '/variables', 
									{"modifications":
									    {"scanCopyFile": {"value": JSON.stringify($scope.scanCopyFileValue), type:'Json'}}
									}
								).then(
									function(result){
										toasty.success( "Данные успешно сохранены!");
										$scope.foundProcesses = [];
										$scope.scanCopyFileValue = undefined;										
										$scope.selectedFile = undefined;
										$scope.businessKey = undefined;
						            	angular.element(document.querySelector('#attachedAcceptanceFile')).val(null);
									},
									function(error){
										console.log(error.data)
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
	}]);
});