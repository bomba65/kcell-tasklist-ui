define(['./../module'], function(module){
    'use strict';
    module.directive('demandDetails', function ($rootScope, $http, $sce, exModal) {
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
                        if (!scope.pdCollapsed) scope.pdCollapsed = [];
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
                    if (!scope.data.productOffers) scope.data.productOffers = [];
                    scope.data.productOffers.push({
                      name: '',
                      description: '',
                      version: version
                    });
                    scope.pdCollapsed.push(false);
                };

                scope.productOfferDelete = function(index) {
                    exModal.open({
                      templateUrl: './js/partials/confirmModal.html',
                      size: 'sm'
                    }).then(function() {
                      scope.data.productOffers.splice(index, 1);
                      scope.pdCollapsed.splice(index, 1);
                    });
                };

                scope.productOfferChange = function(index) {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].version = version;
                    let curProductName = scope.data.productOffers[index].name;
                    if (scope.catalog && scope.catalog.offers && scope.catalog.offers.length) {
                        var elt = scope.catalog.offers.find(function(e) {return e.name === curProductName;});
                        if (elt) scope.productOfferSelected(elt, index, true);
                    }
                };
                scope.productOfferSelected = function(option, index, local) {
                    var version = (option.version + 1) + '';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].name = option.name;
                    scope.data.productOffers[index].description = option.description;
                    scope.data.productOffers[index].version = version;
                    if (!local) scope.pdCollapsed[index] = !scope.pdCollapsed[index];
                };

                scope.togglePDCollapse = function(el, index) {
                    if (el.target.classList.contains('form-control') || el.target.classList.contains('disabled-element') || el.target.classList.contains('thelabel')) return;
                    scope.pdCollapsed[index] = !scope.pdCollapsed[index];
                };
            },
            templateUrl: './js/directives/demand/details.html'
        };
    });
});