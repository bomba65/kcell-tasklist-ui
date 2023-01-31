define(['./../../module'], function(module) {
    'use strict';
    module.directive('disbandPort', ['$http', '$timeout', 'toasty', function ($http, $timeout, toasty) {
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
                    scope.availablePorts = [];
                    scope.availablePorts.push(
                        {
                            "port_id": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 20,
                            "capacity_unit": "Gb",
                            "far_end_address": "Алматинская область, Балхашский район, п. Баканас, ул. Канаева, 32 АТС АО \"Казахтелеком\""
                        },
                        {
                            "port_id": "БаканасСЕ2",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 40,
                            "capacity_unit": "Mb",
                            "far_end_address": "Алматинская область, Балхашский район, п. Баканас, ул. Канаева, 33 АТС АО \"Казахтелеком\""
                        }
                    )
                }

                scope.searchByPortNumber = function () {
                    scope.disbandPorts.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;
                    scope.availablePorts = [];
                    scope.availablePorts.push(
                        {
                            "port_id": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 20,
                            "capacity_unit": "Gb",
                            "far_end_address": "Алматинская область, Балхашский район, п. Баканас, ул. Канаева, 32 АТС АО \"Казахтелеком\""
                        },
                        {
                            "port_id": "БаканасСЕ2",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 40,
                            "capacity_unit": "Mb",
                            "far_end_address": "Алматинская область, Балхашский район, п. Баканас, ул. Канаева, 33 АТС АО \"Казахтелеком\""
                        }
                    )
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

            },
            templateUrl: './js/directives/vpnPortProcess/start/disbandPort.html'
        };
    }]);
});