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
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOption = 'portId';

                scope.searchOblastSelected = function (obl) {
                    scope.disbandPorts.length = 0;
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.disbandPorts.length = 0;
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.disbandPorts.length = 0;
                    scope.isSearched = false;
                }

                scope.isSearched = false;

                scope.searchPorts = function () {
                    scope.disbandPorts.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_oblast || !scope.search_district || !scope.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.search_city_village).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.disbandPorts.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.search_port_number).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.isSelected = function(availablePort) {
                    return scope.disbandPorts.indexOf(availablePort) !== -1;
                };

                scope.addDisbandPort = function(availablePort) {
                    if(scope.disbandPorts.indexOf(availablePort) === -1) {
                        scope.disbandPorts.push(availablePort);
                    } else {
                        scope.disbandPorts.splice(scope.disbandPorts.indexOf(availablePort), 1);
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
            templateUrl: './js/directives/vpnPortProcess/start/disbandPort.html'
        };
    }]);
});