define(['./../../module'], function(module) {
    'use strict';
    module.directive('organizePort', ['$http', '$timeout', 'toasty', function ($http, $timeout, toasty) {
        return {
            restrict: 'E',
            scope: {
                addedPorts: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                $http.get('/camunda/catalogs/api/get/id/30').then(
                    function (response) {
                        scope.oblastCatalog = response.data.data.$list;
                    }
                );

                $http.get('/camunda/catalogs/api/get/id/31').then(
                    function (response) {
                        scope.districtCatalog = response.data.data.$list;
                    }
                );

                $http.get('/camunda/catalogs/api/get/id/32').then(
                    function (response) {
                        scope.cityVillageCatalog = response.data.data.$list.sort(el => el.value);
                    }
                );

                scope.searchOblastSelected = function (obl) {
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.addedPorts.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.addedPorts.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.searchCitySelected = function (city) {
                    scope.addedPorts.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.isSearched = false;

                scope.searchPorts = function () {
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
                        }
                    )
                }

                scope.addPort = function () {
                    if (!scope.availablePorts) {
                        toasty.error({title: "Error", msg: "Please search available ports first"});
                        return;
                    }
                    scope.addedPorts.push(
                        {
                            "port_id": null,
                            "channel_type": null,
                            "port_type": null,
                            "capacity": null,
                            "capacity_unit": null,
                            "far_end_address": {
                                "oblast": scope.search_oblast,
                                "district": scope.search_district,
                                "city": scope.search_city_village,
                                "address_not_full": false,
                                "street": null,
                                "building": null,
                                "cadastral_number": null,
                                "address_note": null
                            }
                        }
                    )
                }

                scope.removeAddedPort = function (addedPort) {
                    const index = scope.addedPorts.indexOf(addedPort);
                    scope.addedPorts.splice(index, 1);
                }

                scope.getAddedPortId = function (index) {
                    const cityName = scope.getValueById('cityVillageCatalog', scope.search_city_village).replace(' ', '');
                    const number = scope.availablePorts.length === 0 ? 1 : /(\d+)(?!.*\d)/g.exec(scope.availablePorts[scope.availablePorts.length - 1].port_id)[1];
                    return cityName + 'CE' + (parseInt(number) + 1 + index);
                }

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id).value;
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/organizePort.html'
        };
    }]);
});