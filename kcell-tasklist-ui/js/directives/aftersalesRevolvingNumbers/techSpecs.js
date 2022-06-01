define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesRevolvingNumbersTechnicalSpecifications', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showCpNew: '=',
                pbxData: '=',
                editConnPoint: '=',
                removeNumbers: '=',
                changeRevNumbers: '=',
                newCaller: '=',
                hiddenFields: '='
            },
            link: function(scope, element, attrs) {
                scope.emailFormat = /^[a-z]+[a-z0-9._]+@[a-z]+\.[a-z.]{2,5}$/;

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.iCallAccess) scope.data.iCallAccess = 'No';
                        if (!scope.data.removalRequired) scope.data.removalRequired = false;
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.$on("aftersalesPBXBINCheck", function(e, result) {
                    if (!result || result.aftersales) return;
                    if (result.techSpecs) parseFromPBX(JSON.parse(result.techSpecs));
                });

                scope.$on('tab-selected', function(e, tabName) {
                    if (tabName === 'techSpec') {
                        var pbxTmp = scope.data.pbxNumbers;
                        var removalTmp = scope.data.removalNumbers;
                        scope.data.removalNumbers = 'this is because of tabset';
                        $timeout(function () {
                            scope.data.removalNumbers = removalTmp;
                        });
                    }
                });

              function parseFromPBX(ts) {
                    if (!ts) return;
                    if (ts.technicalPerson) scope.data.contactPerson = ts.technicalPerson;
                    if (ts.technicalNumber) scope.data.contactNumber = ts.technicalNumber;
                    if (ts.technicalEmail) scope.data.contactEmail = ts.technicalEmail;
                    if (ts.pbxType) scope.data.equipmentType = ts.pbxType;
                    if (ts.pbxVendor) scope.data.pbxVendor = ts.pbxVendor;
                    if (ts.pbxModel) scope.data.pbxModel = ts.pbxModel;
                    if (ts.pbxLocation) scope.data.pbxLocation = ts.pbxLocation;
                    if (ts.pbxCity) scope.data.equipmentCity = ts.pbxCity;
                    if (ts.pbxAddress) scope.data.equipmentAddress = ts.pbxAddress;
                    if (ts.inOutCallAccess) scope.data.callsAccess = ts.inOutCallAccess;
                    if (ts.intenationalCallAccess) scope.data.iCallAccess = ts.intenationalCallAccess;
                    if (ts.connectionType) scope.data.connectionType = ts.connectionType;
                    if (ts.connectionPoint) scope.data.connectionPoint = ts.connectionPoint;
                    if (ts.connectionPointNew) scope.data.connectionPointNew = ts.connectionPointNew;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesRevolvingNumbers/techSpecs.html'
        };
    });
});