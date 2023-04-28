define(['./../../module'], function(module) {
    'use strict';
    module.directive('modifyVpn', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                modifyServices: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                serviceTypeCatalog: '=',
                formData: '=',
                form: '=',
                view: '='
            },
            link: function (scope) {
                scope.formData.searchOption = scope.formData?.searchOption ? scope.formData.searchOption : 'vpnId';

                scope.searchOblastSelected = function (obl) {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.searchCitySelected = function (city) {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = false;
                }

                scope.isSearched = false;

                scope.searchServices = function () {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    if (scope.formData.searchOption === 'farEndAddress') {
                        $http.get('/camunda/vpn/far_end_city_id/' + scope.formData.search_city_village + '?status=Active').then(
                            (response) => {
                                scope.formData.availableServices = response.data;
                            }
                        );
                    } else if (scope.formData.searchOption === 'nearEndAddress') {
                        $http.get('/camunda/vpn/near_end_city_id/' + scope.formData.search_city_village + '?status=Active').then(
                            (response) => {
                                scope.formData.availableServices = response.data
                            }
                        );
                    }
                }

                scope.searchByPortNumber = function () {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_port_number) return;

                    $http.get('/camunda/vpn/port_number/' + scope.formData.search_port_number  + '?status=Active').then(
                        (response) => {
                            scope.formData.availableServices = response.data
                        }
                    );
                }

                scope.searchByVpnId = function() {
                    scope.modifyServices.length = 0;
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_vpn_id) return;

                    $http.get('/camunda/vpn/vpn_number/' + scope.formData.search_vpn_number  + '?status=Active').then(
                        (response) => {
                            scope.formData.availableServices = response.data
                        }
                    );
                }

                scope.isSelected = function(availableService) {
                    return scope.modifyServices.findIndex(item => item.id === availableService.id) !== -1;
                };

                scope.addModifyService = function(availableService) {
                    if(scope.modifyServices.findIndex(item => item.id === availableService.id) === -1) {
                        scope.addServiceTypeTitle(availableService);
                        scope.modifyServices.push(availableService);
                    } else {
                        scope.modifyServices.splice(scope.modifyServices.findIndex(item => item.id === availableService.id), 1);
                    }
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
                };

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id)?.value;
                }

                scope.addServiceTypeTitle = function (service) {
                    service.service_type_title =
                        scope.getValueById("serviceTypeCatalog", service.service_type_id);
                }

            },
            templateUrl: './js/directives/vpnPortProcess/start/modifyVpn.html'
        };
    }]);
});