define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('minioCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', 
		function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
		
			if ($scope.authUser.id !== 'demo') {
				console.log('this is demo user');
				$scope.goHome();
			};

			$rootScope.currentPage = {
				name: 'minio'
			};

			$scope.goHome = function(path) {
			    $location.path("/");
			};

			$scope.pathToFile = '';
			$scope.selectedFile=undefined;

			$scope.selectFile = function (el){
                $scope.$apply(function () {
                    $scope.selectedFile = el.files[0];
                });
            };

            function uploadFileToMinio(file) {
                if ($scope.pathToFile.length > 1) {
	                $http({method: 'GET', url: '/camunda/uploads/admin/put/' + $scope.pathToFile + '/' + file.name, transformResponse: [] })
	                .success(function(data, status, headers, config) {
	                    $http.put(data, file, {headers: {'Content-Type': undefined }}).then(
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
	                })
	                .error (function(data, status, headers, config) {
	                    console.log(data, status, headers, config);
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
	}]);
});