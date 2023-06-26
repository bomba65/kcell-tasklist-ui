define(['./../../module'], function(module) {
    'use strict';
    module.directive('organizeVpn', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                formData: '=',
                addedServices: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                serviceTypeCatalog: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                if (scope.addedServices != null && scope.addedServices.length > 0) {
                    for (var i = 0; i < scope.addedServices.length; i++) {
                        scope.addedServices[i].terminationPoint2FormData = {};
                    }
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
                        "vpn_termination_point_2": {
                            "city_id": {
                                "id": null
                            },
                            "street": null,
                            "building": null,
                            "cadastral_number": null,
                            "note": null,
                        },
                        "terminationPoint2FormData": {}
                    }
                    scope.addedServices.push(service);
                };

                scope.addTerminationPoint2 = function (addedService, availablePort) {
                    addedService.vpn_termination_point_2 = availablePort.port_termination_point;
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