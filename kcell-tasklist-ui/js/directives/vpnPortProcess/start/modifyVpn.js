define(['./../../module'], function(module) {
    'use strict';
    module.directive('modifyVpn', ['$http', '$timeout', 'toasty', function ($http, $timeout, toasty) {
        return {
            restrict: 'E',
            scope: {
                modifyServices: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                serviceTypeCatalog: '=',
                form: '=',
                view: '='
            },
            link: function (scope) {
                scope.searchOption = 'vpnId';

                scope.searchOblastSelected = function (obl) {
                    scope.modifyServices.length = 0;
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.modifyServices.length = 0;
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.modifyServices.length = 0;
                    scope.isSearched = false;
                }

                scope.isSearched = false;

                scope.searchServices = function () {
                    scope.modifyServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_oblast || !scope.search_district || !scope.search_city_village) return;
                    scope.availableServices = [];
                    scope.availableServices.push(
                        {
                            "vpn_id": "VPN001",
                            "service": "L3",
                            "service_type": 3,
                            "service_capacity": 100,
                            "vlan": "232",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                            "vpn_id": "VPN002",
                            "service": "L2",
                            "service_type": 10,
                            "service_capacity": 100,
                            "vlan": "235",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                    scope.modifyServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;
                    scope.availableServices = [];
                    scope.availableServices.push(
                        {
                            "vpn_id": "VPN001",
                            "service": "L3",
                            "service_type": 3,
                            "service_capacity": 100,
                            "vlan": "232",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                            "vpn_id": "VPN002",
                            "service": "L2",
                            "service_type": 10,
                            "service_capacity": 100,
                            "vlan": "235",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                
                scope.searchByVpnId = function() {
                    scope.modifyServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_vpn_id) return;
                    scope.availableServices = [];
                    scope.availableServices.push(
                        {
                            "vpn_id": "VPN001",
                            "service": "L3",
                            "service_type": 3,
                            "service_capacity": 100,
                            "vlan": "232",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                            "vpn_id": "VPN002",
                            "service": "L2",
                            "service_type": 10,
                            "service_capacity": 100,
                            "vlan": "235",
                            "provider_ip": "10.0.0.1",
                            "kcell_ip": "10.0.0.2",
                            "provider_as": 65500,
                            "kcell_as": 65501,
                            "port_number": "БаканасСЕ1",
                            "channel_type": "Main",
                            "port_type": "Optic",
                            "far_end_address": {
                                "oblast": 1,
                                "district": 1,
                                "city": 1,
                                "street": "ул. Канаева",
                                "building": "33",
                                "cadastral_number": null,
                                "address_note": null
                            },
                            "near_end_address": {
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
                
                
                scope.isSelected = function(availableService) {
                    return scope.modifyServices.indexOf(availableService) !== -1;
                };

                scope.addModifyService = function(availableService) {
                    if(scope.modifyServices.indexOf(availableService) === -1) {
                        scope.modifyServices.push(availableService);
                    } else {
                        scope.modifyServices.splice(scope.modifyServices.indexOf(availableService), 1);
                    }
                };

                scope.farEndAddressToString = function (availableService) {
                    return scope.getValueById('oblastCatalog', availableService.far_end_address.oblast) + ' '
                        + scope.getValueById('districtCatalog', availableService.far_end_address.district) + ' '
                        + scope.getValueById('cityVillageCatalog', availableService.far_end_address.city) + ' '
                        + availableService.far_end_address.street + ' ' + availableService.far_end_address.building;
                }

                scope.nearEndAddressToString = function (availableService) {
                    return scope.getValueById('oblastCatalog', availableService.near_end_address.oblast) + ' '
                        + scope.getValueById('districtCatalog', availableService.near_end_address.district) + ' '
                        + scope.getValueById('cityVillageCatalog', availableService.near_end_address.city) + ' '
                        + availableService.near_end_address.street + ' ' + availableService.near_end_address.building;
                }

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id).value;
                }

            },
            templateUrl: './js/directives/vpnPortProcess/start/modifyVpn.html'
        };
    }]);
});