define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesTechnicalSpecifications', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                cpRequired: '='
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.iCallAccess) scope.data.iCallAccess = 'No';
                    }
                });
            },
            templateUrl: './js/directives/aftersalesPBX/techSpecs.html'
        };
    });
});