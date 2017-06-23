define(['./module','jquery', 'camundaSDK'], function(app, $, CamSDK){
	'use strict';
	return app.controller('processesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location',
			                         function($scope, $rootScope, $http, $routeParams, $q, $location) {
		
		
		var camClient = new CamSDK.Client({
		  mock: false,
		  apiUri: '/camunda/api/engine/'
		});

		var processDefinitionService = new camClient.resource('process-definition');
		var userService = new camClient.resource('user');

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

		userService.profile($rootScope.authentication.name, function(err, userProfile){
			if(err){
				throw err;
			}
			$scope.$apply(function (){
				$rootScope.authUser = userProfile;
			});
		});

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
				startedBy: $rootScope.authentication.name,
				sorting:[{sortBy: "startTime",sortOrder: "desc"}]
			}
			if($scope.filter.businessKey){
				filter.processInstanceBusinessKey = $scope.filter.businessKey;
			}
			historyService.processInstance(filter, function(err, result){
				$scope.$apply(function (){
					$scope.processInstances = result;
				});
			});
		};

		$scope.toggleProcessView = function(index){
			if($scope.piIndex === index){
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
            }
            $scope.jobModel = {};
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
            	},
            	function(error){}
        	)
		};
	}]);
});
