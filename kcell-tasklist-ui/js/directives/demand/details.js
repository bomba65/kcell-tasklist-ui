define(['./../module'], function(module){
	'use strict';
	module.directive('demandDetails', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                readonly: '='
			},
			link: function(scope, element, attrs) {
				scope.multiselectSettings = {
					enableSearch: true,
					smartButtonMaxItems: 3,
					showCheckAll: false,
					showUncheckAll: false,
					displayProp: 'v',
					idProp: 'v',
					externalIdProp: 'v'
				};
				scope.productNameOptions = [
					{v: 'Мобильный круг'}
				]
				scope.offerNameOptions = [
					{v: 'Мобильный круг S'}
				]
				scope.deliverableOptions = [
					{v: 'Charging logic in BSS'},
					{v: 'USSD channel'},
					{v: 'Web channel'},
					{v: 'Custom Detalization'},
					{v: 'TV cmpaign'},
					{v: 'Billboards'}
				];

				scope.productNameAdd = function() {
					var val = prompt("Please enter product name");
					if (val && val.length) {
						scope.productNameOptions.push({v: val});
					}
				}

				scope.offerNameAdd = function() {
					var val = prompt("Please enter product offer name");
					if (val && val.length) {
						scope.offerNameOptions.push({v: val});
					}
				}

				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.productName) scope.data.productName = [];
						else scope.productNameOptions = _.unionWith(scope.productNameOptions, scope.data.productName, _.isEqual);
						if (!scope.data.productOfferName) scope.data.productOfferName = [];
						else scope.offerNameOptions = _.unionWith(scope.offerNameOptions, scope.data.productOfferName, _.isEqual);
						if (!scope.data.deliverable) scope.data.deliverable = [];
						if (!scope.data.productNameVersion) scope.data.productNameVersion = '1.0';
						if (!scope.data.offerNameVersion) scope.data.offerNameVersion = '1.0';
					}
				});
	        },
			templateUrl: './js/directives/demand/details.html'
		};
	});
});