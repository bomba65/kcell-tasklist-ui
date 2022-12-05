define(['./../module'], function(module){
    'use strict';
    module.directive('fixedInternetKaseInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
            },
            link: function(scope, element, attrs) {
                scope.emailFormat = /^[a-z]+[a-z0-9._]+@[a-z]+\.[a-z.]{2,5}$/;
            },
            templateUrl: './js/directives/fixedInternet/kaseInfo.html'
        };
    });
});