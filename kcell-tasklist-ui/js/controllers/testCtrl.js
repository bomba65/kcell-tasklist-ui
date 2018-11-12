define(['./module','camundaSDK', 'lodash', 'big-js'], function(module, CamSDK, _, Big){
	'use strict';
	return module.controller('testCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', 
		function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
		
			if ($scope.authUser.id !== 'demo') {
				console.log('this is demo user');
				$scope.goHome();
			};

			$rootScope.currentPage = {
				name: 'test'
			};

			$scope.goHome = function(path) {
			    $location.path("/");
			};

			$scope.getUsers = function(val, group) {
                var users = $http.get('/camunda/api/engine/engine/default/user?memberOfGroup='+ group +'&firstNameLike=%'+val+'%').then(
                        function(response){
                            var usersByFirstName = _.flatMap(response.data, function(s){
                                if(s.id){
                                    return s.id.split(',').map(function(user){
                                        return {
                                            id: s.id,
                                            email: s.email.substring(s.email.lastIndexOf('/')+1),
                                            firstName: s.firstName,
                                            lastName: s.lastName,
                                            name: s.firstName + ' ' + s.lastName
                                        };
                                    })
                                } else {
                                    return [];
                                }
                            });
                            return $http.get('/camunda/api/engine/engine/default/user?memberOfGroup='+ group +'&lastNameLike=%'+val+'%').then(
                                    function(response){
                                        var usersByLastName = _.flatMap(response.data, function(s){
                                            if(s.id){
                                                return s.id.split(',').map(function(user){
                                                    return {
                                                        id: s.id,
                                                        email: s.email.substring(s.email.lastIndexOf('/')+1),
                                                        firstName: s.firstName,
                                                        lastName: s.lastName,
                                                        name: s.firstName + ' ' + s.lastName
                                                    };
                                                })
                                            } else {
                                                return [];
                                            }
                                        });
                                        return _.unionWith(usersByFirstName, usersByLastName, _.isEqual);
                                    }
                            );
                        }
                );
                return users;
            };		

            $scope.userSelected = function($item, varName){
                $scope[varName] = {'id': $item.id, 'name': $item.name, 'email': $item.email };
            };	
	}]);
});