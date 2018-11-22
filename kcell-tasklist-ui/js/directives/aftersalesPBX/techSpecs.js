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
                cpRequired: '=',
                pbxData: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.iCallAccess) scope.data.iCallAccess = 'No';
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.$on("aftersalesPBXBINCheck", function(e, result) {
                    if (!result) return;
                    if (result.techSpecs) parseFromPBX(JSON.parse(result.techSpecs));
                });

                function parseFromPBX(ts) {
                    if (!ts) return;
                    if (ts.technicalPerson) scope.data.contactPerson = ts.technicalPerson;
                    if (ts.technicalNumber) scope.data.contactNumber = ts.technicalNumber;
                    if (ts.technicalEmail) scope.data.contactNumber = ts.technicalEmail;
                    if (ts.pbxType) scope.data.equipmentType = ts.pbxType;
                    if (ts.pbxVendor) scope.data.pbxVendor = ts.pbxVendor;
                    if (ts.pbxModel) scope.data.pbxModel = ts.pbxModel;
                    if (ts.pbxLocation) scope.data.pbxLocation = ts.pbxLocation;
                    if (ts.pbxCity) scope.data.equipmentCity = ts.pbxCity;
                    if (ts.pbxAddress) scope.data.equipmentAddress = ts.pbxAddress;
                    if (ts.virtualNumbersCount) scope.data.pbxQuantity = ts.virtualNumbersCount;
                    if (ts.pbxNumbers) scope.data.pbxNumbers = ts.pbxNumbers;
                    if (ts.inOutCallAccess) scope.data.callsAccess = ts.inOutCallAccess;
                    if (ts.intenationalCallAccess) scope.data.iCallAccess = ts.intenationalCallAccess;
                    if (ts.connectionType) scope.data.connectionType = ts.connectionType;
                    if (ts.connectionPoint) scope.data.connectionPoint = ts.connectionPoint;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesPBX/techSpecs.html'
        };
    });
});