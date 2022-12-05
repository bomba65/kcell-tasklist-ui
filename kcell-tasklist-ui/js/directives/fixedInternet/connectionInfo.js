define(['./../module'], function(module){
    'use strict';
    module.directive('fixedInternetConnectionInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                extra: '=',
                view: '=',
                disabled: "=",
            },
            link: function(scope, element, attrs) {

            },
            templateUrl: './js/directives/fixedInternet/connectionInfo.html'
        };
    });
});