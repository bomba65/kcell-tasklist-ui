define(['./../../module'], function(module) {
    'use strict';
    module.directive('modifyPort', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                modifyPorts: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                formData: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.formData.searchOption = scope.formData?.searchOption ? scope.formData.searchOption : 'portId';

                scope.searchOblastSelected = function (obl) {
                    scope.modifyPorts.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.modifyPorts.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.modifyPorts.length = 0;
                    scope.isSearched = false;
                }

                scope.searchPorts = function () {
                    scope.modifyPorts.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.formData.search_city_village + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.modifyPorts.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.formData.search_port_number + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.isSelected = function(availablePort) {
                    return scope.modifyPorts.findIndex(item => item.id === availablePort.id) !== -1;
                };

                scope.addModifyPort = function(availablePort) {
                    if(scope.modifyPorts.findIndex(item => item.id === availablePort.id) === -1) {
                        scope.modifyPorts.push(JSON.parse(JSON.stringify(availablePort)));
                    } else {
                        scope.modifyPorts.splice(scope.modifyPorts.findIndex(item => item.id === availablePort.id), 1);
                    }
                };

                scope.addressToString = function (address) {
                    if (!address) return;

                    return (address.city_id.district_id.oblast_id.name ? address.city_id.district_id.oblast_id.name : '') + ' '
                        + (address.city_id.district_id.name ? address.city_id.district_id.name : '') + ' '
                        + (address.city_id.name ? address.city_id.name : '') + ' '
                        + (address.street ? address.street : '') + ' '
                        + (address.building ? address.building : '') + ' '
                        + (address.cadastral_number ? address.cadastral_number : '') + ' '
                        + (address.note ? address.note : '');
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/modifyPort.html'
        };
    }]);
});