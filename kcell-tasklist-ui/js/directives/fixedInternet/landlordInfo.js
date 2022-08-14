define(['./../module'], function(module){
    'use strict';
    module.directive('fixedInternetLandlordInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
                connection: "=",
            },
            link: function(scope, element, attrs) {

            },
            templateUrl: './js/directives/fixedInternet/landlordInfo.html'
        };
    });
});