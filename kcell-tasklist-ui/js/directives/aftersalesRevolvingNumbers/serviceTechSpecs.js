define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesRevolvingNumbersTrunk', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.iCallAccess) scope.data.iCallAccess = 'No';
                        if (!scope.data.removalRequired) scope.data.removalRequired = false;
                    }
                });

            },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/serviceTechSpecs.html'
        };
    });
});