define(['./../../module'], function(module) {
    'use strict';
    module.directive('approveModifyPort', ['$http', 'toasty', function ($http, toasty) {
        return {
            restrict: 'E',
            scope: {
                modifyPorts: '=',
                form: '=',
                view: '='
            },
            link: function (scope, el, attrs) {
                scope.getValueById = function (name, id) {
                    return _.find(scope[name], el => el.id === id).value;
                }

                scope.addressToString = function (address) {
                    if (!address) return;

                    return address.city_id.district_id.oblast_id.name + ' '
                        + address.city_id.district_id.name + ' '
                        + address.city_id.name + ' '
                        + address.street + ' ' + address.building;
                }

            },
            templateUrl: './js/directives/vpnPortProcess/approveRequest/approveModifyPort.html'
        };
    }]);
});