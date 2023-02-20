define(['./../../module'], function(module) {
    'use strict';
    module.directive('organizePort', ['$http', 'toasty', function ($http, toasty) {
        return {
            restrict: 'E',
            scope: {
                addedPorts: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                formData: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOblastSelected = function (obl) {
                    scope.addedPorts.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.addedPorts.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchCitySelected = function (city) {
                    scope.addedPorts.length = 0;
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.formData.isSearched = false;

                scope.searchPorts = function () {
                    scope.formData.isSearched = true;
                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.formData.search_city_village).then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.addPort = function () {
                    if (!scope.formData.isSearched) {
                        toasty.error({title: "Error", msg: "Please search available ports first"});
                        return;
                    }
                    scope.addedPorts.push(
                        {
                            "port_number": null,
                            "channel_type": null,
                            "port_type": null,
                            "port_capacity": null,
                            "port_capacity_unit": null,
                            "far_end_address": {
                                "city_id": {
                                    "id": scope.formData.search_city_village,
                                },
                                "street": null,
                                "building": null,
                                "cadastral_number": null,
                                "note": null,
                            }
                        }
                    )
                }

                scope.removeAddedPort = function (addedPort) {
                    const index = scope.addedPorts.indexOf(addedPort);
                    scope.addedPorts.splice(index, 1);
                }

                scope.getAddedPortId = function (index) {
                    const cityName = scope.getValueById('cityVillageCatalog', scope.formData.search_city_village)?.replace(' ', '');
                    let number = 0;
                    const existingNumbers =  _.map(scope.formData.availablePorts, (port) => Number(/(\d+)(?!.*\d)/g.exec(port.port_number)[1])).sort();
                    if (existingNumbers && existingNumbers.length > 0) number = existingNumbers[existingNumbers.length - 1];
                    return cityName + 'СЕ' + (parseInt(number) + 1 + index);
                }

                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id)?.value;
                }

                scope.addressToString = function (address) {
                    if (!address) return;

                    return address.city_id.district_id.oblast_id.name + ' '
                        + address.city_id.district_id.name + ' '
                        + address.city_id.name + ' '
                        + address.street + ' ' + address.building;
                }

            },
            templateUrl: './js/directives/vpnPortProcess/start/organizePort.html'
        };
    }]);
});