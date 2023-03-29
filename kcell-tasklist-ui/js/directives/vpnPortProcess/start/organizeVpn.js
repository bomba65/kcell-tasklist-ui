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
                formData: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOblastSelected = function (obl) {
                    scope.addedServices.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.addedServices.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchCitySelected = function (city) {
                    scope.addedServices.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.formData.isSearched = false;
                scope.formData.searchOption = scope.formData?.searchOption ? scope.formData.searchOption : 'portId'

                scope.searchPorts = function () {
                    scope.addedServices.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.formData.search_city_village + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    scope.addedServices.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.formData.search_port_number + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
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

                scope.removeAddedService = function (addedService) {
                    const index = scope.addedServices.indexOf(addedService);
                    scope.addedServices.splice(index, 1);
                }

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id)?.value;
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