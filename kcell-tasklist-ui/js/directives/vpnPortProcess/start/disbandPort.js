define(['./../../module'], function(module) {
    'use strict';
    module.directive('disbandPort', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                disbandPorts: '=',
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
                    scope.disbandPorts.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.disbandPorts.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.disbandPorts.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchPorts = function () {
                    scope.disbandPorts.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.formData.search_city_village + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.disbandPorts.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.formData.search_port_number + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.isSelected = function(availablePort) {
                    return scope.disbandPorts.findIndex(item => item.id === availablePort.id) !== -1;
                };

                scope.addDisbandPort = function(availablePort) {
                    if(scope.disbandPorts.findIndex(item => item.id === availablePort.id) === -1) {
                        scope.disbandPorts.push(availablePort);
                    } else {
                        scope.disbandPorts.splice(scope.disbandPorts.findIndex(item => item.id === availablePort.id), 1);
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
            templateUrl: './js/directives/vpnPortProcess/start/disbandPort.html'
        };
    }]);
});