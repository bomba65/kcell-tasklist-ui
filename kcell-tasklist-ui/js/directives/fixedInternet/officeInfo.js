define(['./../module'], function(module){
    'use strict';
    module.directive('fixedInternetOfficeInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
            },
            link: function(scope, element, attrs) {

            },
            templateUrl: './js/directives/fixedInternet/officeInfo.html'
        };
    });
});