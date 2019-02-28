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
                scope.deliverableHidden = null;
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.productOffers) scope.data.productOffers = [];
                        if (!scope.data.deliverable) scope.data.deliverable = [];
                        scope.deliverableHidden = 'something';
                        if (!scope.data.deliverable.length) scope.deliverableHidden = null;
                    }
                }, true);

                scope.multiselectSettings = {
                    enableSearch: true,
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
                        var elt = scope.catalog.products.find(function(e) {return e.name === scope.data.productName;});
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
                    scope.cpo = {
                        index: -1,
                        name: '',
                        description: '',
                        version: version
                    };
                };
                scope.productOfferEdit = function(index) {
                    scope.cpo = {
                        index: index,
                        name: scope.data.productOffers[index].name,
                        description: scope.data.productOffers[index].description,
                        version: scope.data.productOffers[index].version
                    };
                };
                scope.productOfferDelete = function(index) {
                    scope.data.productOffers.splice(index, 1);
                };
                scope.productOfferChange = function() {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.cpo.version = version;
                    if (scope.catalog && scope.catalog.offers && scope.catalog.offers.length) {
                        var elt = scope.catalog.offers.find(function(e) {return e.name === scope.cpo.name;});
                        if (elt) scope.productOfferSelected(elt);
                    }
                };
                scope.productOfferSelected = function(option) {
                    var version = (option.version + 1) + '';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.cpo.name = option.name;
                    scope.cpo.description = option.description;
                    scope.cpo.version = version;
                };
                scope.saveCPO = function() {
                    if (scope.cpo.index !== -1) {
                        scope.data.productOffers[scope.cpo.index].name = scope.cpo.name;
                        scope.data.productOffers[scope.cpo.index].description = scope.cpo.description;
                        scope.data.productOffers[scope.cpo.index].version = scope.cpo.version;
                    } else {
                        scope.data.productOffers.push({
                          name: scope.cpo.name,
                          description: scope.cpo.description,
                          version: scope.cpo.version
                        });
                    }
                    scope.cpo = null;
                };
                scope.cancelCPO = function() {
                    scope.cpo = null;
                };
            },
            templateUrl: './js/directives/demand/details.html'
        };
    });
});