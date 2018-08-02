define(['./../module'], function(module){
	'use strict';
	module.directive('demandGeneral', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
			},
			link: function(scope, element, attrs) {
                scope.datePickerMinDate = new Date();
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        
                        if (!scope.data.plannedLaunch) scope.data.plannedLaunch = new Date();
                        else scope.data.plannedLaunch = new Date(scope.data.plannedLaunch);

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
	        },
			templateUrl: './js/directives/demand/general.html'
		};
	});
});