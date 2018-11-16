define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesSipProtocol', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showNewFields: '=',
                modifyConnection: '='
            },
            link: function(scope, element, attrs) {
            },
            templateUrl: './js/directives/aftersalesPBX/sipProtocol.html'
        };
    });
});