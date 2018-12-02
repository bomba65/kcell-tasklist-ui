define(['./../module'], function(module){
    'use strict';
    module.directive('demandGeneral', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                technical: '=',
                actualLaunch: '='
            },
            link: function(scope, element, attrs) {
                scope.datePickerMinDate = new Date();
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};

                        if (!scope.data.plannedLaunch) scope.data.plannedLaunch = new Date();
                        else scope.data.plannedLaunch = new Date(scope.data.plannedLaunch);

                        if (scope.data.actualLaunch) scope.data.actualLaunch = new Date(scope.data.actualLaunch);

                        if (!scope.data.technicalAnalysis) scope.data.technicalAnalysis = false;

                        if (!scope.data.demandOwner) {
                            scope.data.demandOwner = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name+ "/profile").then(function(result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.data.demandOwner = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                scope.getUser = function(val) {
                    scope.data.demandSupervisorId = null;
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike='+encodeURIComponent('%'+val+'%')).then(
                        function(response){
                            var usersByFirstName = _.flatMap(response.data, function(s){
                                if(s.id){
                                    return s.id.split(',').map(function(user){
                                        return {
                                            id: s.id,
                                            email: (s.email?s.email.substring(s.email.lastIndexOf('/')+1):s.email),
                                            firstName: s.firstName,
                                            lastName: s.lastName,
                                            name: s.firstName + ' ' + s.lastName
                                        };
                                    })
                                } else {
                                    return [];
                                }
                            });
                            //return usersByFirstName;
                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike='+encodeURIComponent('%'+val+'%')).then(
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

                scope.userSelected = function(item){
                    scope.data.demandSupervisorId = item.id;
                    scope.data.demandSupervisor = item.name;
                };
            },
            templateUrl: './js/directives/demand/general.html'
        };
    });
});