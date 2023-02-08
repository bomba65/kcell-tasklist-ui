define(['./../../module'], function(module) {
    'use strict';
    module.directive('organizeVpn', ['$http', function ($http) {
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

                    $http.get('/camunda/port/city_id/' + scope.search_city_village).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.addedServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.search_port_number).then(
                        (response) => {
                            scope.availablePorts = response.data
                        }
                    );
                }

                scope.addService = function(availablePort) {
                    var service = {
                        "vpn_number": null,
                        "port": availablePort,
                        "service": null,
                        "service_type_id": null,
                        "service_type_catalog_id": 83,
                        "provider_ip": null,
                        "kcell_ip": null,
                        "vlan": null,
                        "service_capacity": null,
                        "provider_as": null,
                        "kcell_as": null,
                        "near_end_address": {
                            "city_id": {
                                "id": null
                            },
                            "street": null,
                            "building": null,
                            "cadastral_number": null,
                            "note": null,
                        }
                    }
                    scope.addedServices.push(service);
                    scope.addedServicesDistrictCatalog.push([]);
                    scope.addedServicesCityCatalog.push([]);
                };

                scope.addressToString = function (address) {
                    if (!address) return;

                    return address.city_id.district_id.oblast_id.name + ' '
                        + address.city_id.district_id.name + ' '
                        + address.city_id.name + ' '
                        + address.street + ' ' + address.building;
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

                scope.addServiceTypeTitle = function (service) {
                    service.service_type_title =
                        scope.getValueById("serviceTypeCatalog", service.service_type_id);
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/organizeVpn.html'
        };
    }]);
});