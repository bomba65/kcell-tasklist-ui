define(['./../../module'], function(module) {
    'use strict';
    module.directive('portSearchForm', ['$http', function ($http) {
        return {
            restrict: 'E',
            scope: {
                formData: '=',
                addedServices: '=',
                cleanOnSearch: '=',
                oblastCatalog: '=',
                districtCatalog: '=',
                cityVillageCatalog: '=',
                serviceTypeCatalog: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.searchOblastSelected = function (obl) {
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchDistrictSelected = function (dis) {
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.searchCitySelected = function (city) {
                    scope.formData.isSearched = false;
                    scope.formData.availablePorts = undefined;
                }

                scope.formData.isSearched = false;
                scope.formData.searchOption = scope.formData?.searchOption ? scope.formData.searchOption : 'portId'

                scope.searchPorts = function () {
                    if (scope.cleanOnSearch) {
                        scope.addedServices.length = 0;
                    }

                    if (!scope.formData.search_oblast || !scope.formData.search_district || !scope.formData.search_city_village) return;

                    $http.get('/camunda/port/city_id/' + scope.formData.search_city_village + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }

                scope.searchByPortNumber = function () {
                    if (scope.cleanOnSearch) {
                        scope.addedServices.length = 0;
                    }

                    scope.formData.isSearched = true;
                    if (!scope.formData.search_port_number) return;

                    $http.get('/camunda/port/port_number/' + scope.formData.search_port_number + '?status=Active').then(
                        (response) => {
                            scope.formData.availablePorts = response.data
                        }
                    );
                }
            },
            templateUrl: './js/directives/vpnPortProcess/common/portSearchForm.html'
        };
    }]);
});