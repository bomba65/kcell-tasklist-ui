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
                        if (!scope.useCaseCollapsed) scope.useCaseCollapsed = [];
                    }
                });

                scope.useCaseAdd = function() {
                    scope.data.useCases.push({name: '', description: ''});
                    scope.useCaseCollapsed.push(false);
                };

                scope.useCaseDelete = function(index) {
                    scope.data.useCases.splice(index, 1);
                    scope.useCaseCollapsed.splice(index, 1);
                };

                scope.toggleUseCaseCollapse = function(el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.useCaseCollapsed[index] = !scope.useCaseCollapsed[index];
                };
            },
            templateUrl: './js/directives/demand/functionalRequirements.html'
        };
    });
});