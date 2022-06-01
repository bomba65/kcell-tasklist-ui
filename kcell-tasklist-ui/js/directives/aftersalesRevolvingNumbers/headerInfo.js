define(['./../module'], function(module){
    'use strict';
    module.directive('headerInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                numberRequest: '=',
                legalInfo: '=',
                clientPriority: '=',
                workType: '='
            },
            link: function(scope, element, attrs) {
                },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/headerInfo.html'
        };
    });
});