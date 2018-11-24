define(['./../module'], function(module){
    'use strict';
    module.directive('demandFuncRequirements', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                processId: '=',
                taskId: '='
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        // if (!scope.data.businessCases) scope.data.businessCases = [];
                        if (!scope.data.useCases) scope.data.useCases = [];
                    }
                });
                scope.collapse = {
                    // businessCase: false,
                    // businessCases: {},
                    useCase: false,
                    useCases: {},
                    actorProfiles: false,
                    businessSchema: false,
                    chargingRequirements: false
                };
                scope.toggleCollapse = function(section, index) {
                    if (index !== -1) {
                        scope.collapse[section][index] = !scope.collapse[section][index];
                    } else {
                        scope.collapse[section] = !scope.collapse[section];
                    }
                };
                scope.deleteObject = function(section, index) {
                    scope.data[section].splice(index, 1);
                    delete scope.collapse[section][index];
                };

                /*scope.businessCaseAdd = function() {
                    scope.data.businessCases.push({text: ''});
                    scope.collapse.businessCases[scope.data.businessCases.length - 1] = false;
                };*/

                scope.useCaseAdd = function() {
                    scope.data.useCases.push({name: '', text: ''});
                    scope.collapse.useCases[scope.data.useCases.length - 1] = false;
                }
            },
            templateUrl: './js/directives/demand/functionalRequirements.html'
        };
    });
});