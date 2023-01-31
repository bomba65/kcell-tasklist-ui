define(['./../../module'], function(module) {
    'use strict';
    module.directive('organizeVpn', ['$http', '$timeout', 'toasty', function ($http, $timeout, toasty) {
        return {
            restrict: 'E',
            scope: {
                addedServices: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                serviceTypeCatalog: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOblastSelected = function (obl) {
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.addedServices.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.addedServices.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.searchCitySelected = function (city) {
                    scope.addedServices.length = 0;
                    scope.isSearched = false;
                    scope.availablePorts = undefined;
                }

                scope.isSearched = false;
                scope.searchOption = "portId";

                scope.searchPorts = function () {
                    scope.addedServices.length = 0;
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
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            }
                        },
                        {
                            "port_id": "БаканасСЕ2",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 40,
                            "capacity_unit": "Mb",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            }
                        }
                    )
                }

                scope.searchByPortNumber = function () {
                    scope.addedServices.length = 0;
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
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            }
                        },
                        {
                            "port_id": "БаканасСЕ2",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "capacity": 40,
                            "capacity_unit": "Mb",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            }
                        }
                    )
                }

                scope.addService = function(availablePort) {
                    var service = {
                        "port_id": availablePort.port_id,
                        "service": null,
                        "service_type": null,
                        "capacity": null,
                        "near_end_address": {
                            "oblast": null,
                            "district": null,
                            "city": null,
                            "address_not_full": false,
                            "street": null,
                            "building": null,
                            "cadastral_number": null,
                            "address_note": null
                        }
                    };
                    scope.addedServices.push(service);
                    scope.addedServicesDistrictCatalog.push([]);
                    scope.addedServicesCityCatalog.push([]);
                };

                scope.addressToString = function (availablePort) {
                    return scope.getValueById('oblastCatalog', availablePort.far_end_address.oblast) + ' '
                        + scope.getValueById('districtCatalog', availablePort.far_end_address.district) + ' '
                        + scope.getValueById('cityVillageCatalog', availablePort.far_end_address.city) + ' '
                        + availablePort.far_end_address.street + ' ' + availablePort.far_end_address.building;
                }

                scope.removeAddedService = function (addedService) {
                    const index = scope.addedServices.indexOf(addedService);
                    scope.addedServices.splice(index, 1);
                    scope.addedServicesDistrictCatalog.splice(index,1);
                    scope.addedServicesCityCatalog.splice(index,1);
                }

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id).value;
                }

                scope.addedServicesDistrictCatalog = []
                scope.addedServiceOblastSelected = function (addedService) {
                    const index = scope.addedServices.indexOf(addedService);
                    scope.addedServicesDistrictCatalog[index] = _.filter(scope.districtCatalog, el => el.parent === addedService.near_end_address.oblast);
                }

                scope.addedServicesCityCatalog = []
                scope.addedServiceDistrictSelected = function (addedService) {
                    const index = scope.addedServices.indexOf(addedService);
                    scope.addedServicesCityCatalog[index] = _.filter(scope.cityVillageCatalog, el => el.parent === addedService.near_end_address.district);
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/organizeVpn.html'
        };
    }]);
});