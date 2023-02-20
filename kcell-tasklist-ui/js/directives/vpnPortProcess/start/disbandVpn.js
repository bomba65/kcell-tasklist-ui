define(['./../../module'], function(module) {
    'use strict';
    module.directive('disbandVpn', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                disbandServices: '=',
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
                    scope.disbandServices.length = 0;
                    scope.filteredDistrictCatalog = _.filter(scope.districtCatalog, el => el.parent === obl);
                    scope.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.disbandServices.length = 0;
                    scope.filteredCityVillageCatalog = _.filter(scope.cityVillageCatalog, el => el.parent === dis);
                    scope.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.disbandServices.length = 0;
                    scope.isSearched = false;
                }

                scope.isSearched = false;

                scope.searchServices = function () {
                    scope.disbandServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_oblast || !scope.search_district || !scope.search_city_village) return;

                    if (scope.searchOption === 'farEndAddress') {
                        $http.get('/camunda/vpn/far_end_city_id/' + scope.search_city_village).then(
                            (response) => {
                                scope.availableServices = response.data;
                            }
                        );
                    } else if (scope.searchOption === 'nearEndAddress') {
                        $http.get('/camunda/vpn/near_end_city_id/' + scope.search_city_village).then(
                            (response) => {
                                scope.availableServices = response.data
                            }
                        );
                    }


                }

                scope.searchByPortNumber = function () {
                    scope.disbandServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_port_number) return;

                    $http.get('/camunda/vpn/port_number/' + scope.search_port_number).then(
                        (response) => {
                            scope.availableServices = response.data
                        }
                    );
                }
                
                scope.searchByVpnId = function() {
                    scope.disbandServices.length = 0;
                    scope.isSearched = true;
                    if (!scope.search_vpn_id) return;

                    $http.get('/camunda/vpn/vpn_number/' + scope.search_vpn_number).then(
                        (response) => {
                            scope.availableServices = response.data
                        }
                    );
                }
                
                
                scope.isSelected = function(availableService) {
                    return scope.disbandServices.indexOf(availableService) !== -1;
                };

                scope.addDisbandService = function(availableService) {
                    if(scope.disbandServices.indexOf(availableService) === -1) {
                        scope.addServiceTypeTitle(availableService);
                        scope.disbandServices.push(availableService);
                    } else {
                        scope.disbandServices.splice(scope.disbandServices.indexOf(availableService), 1);
                    }
                };

                scope.addressToString = function (address) {
                    if (!address) return;

                    return address.city_id.district_id.oblast_id.name + ' '
                        + address.city_id.district_id.name + ' '
                        + address.city_id.name + ' '
                        + address.street + ' ' + address.building;
                };

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id)?.value;
                }

                scope.addServiceTypeTitle = function (service) {
                    service.service_type_title =
                        scope.getValueById("serviceTypeCatalog", service.service_type_id);
                }
            },
            templateUrl: './js/directives/vpnPortProcess/start/disbandVpn.html'
        };
    }]);
});