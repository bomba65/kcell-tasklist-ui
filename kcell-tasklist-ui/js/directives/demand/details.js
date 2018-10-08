define(['./../module'], function(module){
	'use strict';
	module.directive('demandDetails', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
				disabled: '=',
				editdescription: '='
			},
			link: function(scope, element, attrs) {
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.productNames) scope.data.productNames = [];
						if (!scope.data.offerNames) scope.data.offerNames = [];
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
				var productNameOptions = [
					{ name: 'Мобильный круг', version: 1.0 }
				];
				scope.filteredProductNames = [];
				scope.productNameAdd = function() {
					scope.data.productNames.push({ name: '', version: null });
					scope.filteredProductNames.push([]);
				}
				scope.productNameDelete = function(index) {
					scope.data.productNames.splice(index, 1);
					scope.filteredProductNames.splice(index, 1);
				}
				scope.productNameComplete = function(index) {
					scope.filteredProductNames[index] = _.filter(productNameOptions, function(opt) {
						return opt.name.startsWith(scope.data.productNames[index].name);
					});
				}
				scope.productNameSelect = function(index, selected) {
					scope.data.productNames[index] = _.clone(selected);
					scope.productNameBlur(index);
				}
				scope.productNameBlur = function(index) {
					if (!scope.data.productNames[index].version)
						scope.data.productNames[index].version = 1.0;
					scope.filteredProductNames[index] = null;
				}

				// ------ Product offer name ------
				var offerNameOptions = [
					{name: 'Мобильный круг S', description:'', version: 1.0}
				];
				scope.filteredOfferNames = [];
				scope.offerNameAdd = function() {
					scope.data.offerNames.push({ name: '', version: null });
					scope.filteredOfferNames.push([]);
				}
				scope.offerNameDelete = function(index) {
					scope.data.offerNames.splice(index, 1);
					scope.filteredOfferNames.splice(index, 1);
				}
				scope.offerNameComplete = function(index) {
					scope.filteredOfferNames[index] = _.filter(offerNameOptions, function(opt) {
						return opt.name.startsWith(scope.data.offerNames[index].name);
					});
				}
				scope.offerNameSelect = function(index, selected) {
					scope.data.offerNames[index] = _.clone(selected);
					scope.offerNameBlur(index);
				}
				scope.offerNameFocus = function(index) {
					scope.offerNameComplete(index);
				}
				scope.offerNameBlur = function(index) {
					if (!scope.data.offerNames[index].version)
						scope.data.offerNames[index].version = 1.0;
					scope.filteredOfferNames[index] = null;
				}
	        },
			templateUrl: './js/directives/demand/details.html'
		};
	});
});