define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesRevolvingNumbersTechnicalSpecificationsFull', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                datatech: '=',
                datasip: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showCpNew: '=',
                pbxData: '=',
                pbxDatas: '=',
                editConnPoint: '=',
                removeNumbers: '=',
                changeRevNumbers: '=',
                newCaller: '=',
                hiddenFields: '=',
                showNewFields: '=',
                modifyConnection: '=',
                legalInfo: '=',
            },
            link: function(scope, element, attrs) {
                scope.$watch('datatech', function (value) {
                    if (value) {
                        if (!scope.datatech.removalRequired) scope.datatech.removalRequired = false;
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.$watch('pbxDataS', function (value) {
                    if (!scope.pbxDataS) scope.pbxDataS = {};
                });
            },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/techSpecsFull.html'
        };
    });
});