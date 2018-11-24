define(['./../module'], function(module){
    'use strict';
    module.directive('demandDetails', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                catalog: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.productOffers) scope.data.productOffers = [];
                        if (!scope.data.deliverable) scope.data.deliverable = [];
                    }
                });

                scope.multiselectSettings = {
                    enableSearch: true,
                    smartButtonMaxItems: 3,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v'
                };
                scope.deliverableOptions = [
                    {v: 'Charging logic in BSS'},
                    {v: 'USSD channel'},
                    {v: 'Web channel'},
                    {v: 'Custom Detalization'},
                    {v: 'TV cmpaign'},
                    {v: 'Billboards'}
                ];

                // ------ Product name -----

                scope.productNameChange = function() {
                    scope.data.productVersion = 1;
                    if (scope.catalog && scope.catalog.products && scope.catalog.products.length) {
                        var elt = scope.catalog.products.find(function(e) {return e.name === scope.data.productName});
                        if (elt) scope.productNameSelected(elt);
                    }
                    scope.data.productOffers = [];
                };
                scope.productNameSelected = function(option) {
                    scope.data.productName = option.name;
                    scope.data.productVersion = option.version + 1;
                };

                scope.productOfferAdd = function() {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers.push({
                        name: '',
                        description: '',
                        version: version
                    });
                };
                scope.productOfferDelete = function(index) {
                    scope.data.productOffers.splice(index, 1);
                };
                scope.productOfferChange = function(index) {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].version = version;
                    if (scope.catalog && scope.catalog.offers && scope.catalog.offers.length) {
                        var elt = scope.catalog.offers.find(function(e) {return e.name === scope.data.productOffers[index].name});
                        if (elt) scope.productOfferSelected(index, elt);
                    }
                };
                scope.productOfferSelected = function(index, option) {
                    var version = (option.version + 1) + '';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].name = option.name;
                    scope.data.productOffers[index].description = option.description;
                    scope.data.productOffers[index].version = version;
                };
            },
            templateUrl: './js/directives/demand/details.html'
        };
    });
});