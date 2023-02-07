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
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOption = 'portId';

                scope.searchOblastSelected = function (obl) {
                    scope.modifyPorts.length = 0;
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.modifyPorts.length = 0;
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.modifyPorts.length = 0;
                    scope.isSearched = false;
                }

                scope.isSearched = false;

                scope.searchPorts = function () {
                    scope.modifyPorts.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_oblast || !scope.search_district || !scope.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.search_city_village).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.modifyPorts.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.search_port_number).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.isSelected = function(availablePort) {
                    const modifyPort = _.find(scope.modifyPorts, (port) => port.port_number === availablePort.port_number);
                    return scope.modifyPorts.indexOf(modifyPort) !== -1;
                };

                scope.addModifyPort = function(availablePort) {
                    const modifyPort = _.find(scope.modifyPorts, (port) => port.port_number === availablePort.port_number);
                    if(scope.modifyPorts.indexOf(modifyPort) === -1) {
                        scope.modifyPorts.push(JSON.parse(JSON.stringify(availablePort)));
                    } else {
                        const modifyPort = _.find(scope.modifyPorts, (port) => port.port_number === availablePort.port_number);
                        scope.modifyPorts.splice(scope.modifyPorts.indexOf(modifyPort), 1);
                    }
                };

                scope.addressToString = function (address) {
                    if (!address) return;

                    return address.city_id.district_id.oblast_id.name + ' '
                        + address.city_id.district_id.name + ' '
                        + address.city_id.name + ' '
                        + address.street + ' ' + address.building;
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/modifyPort.html'
        };
    }]);
});