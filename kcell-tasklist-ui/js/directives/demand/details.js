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
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data.productName) {
							scope.data.productName = [];
						}
						if (!scope.data.productOfferName) {
							scope.data.productOfferName = [];
						}
						if (!scope.data.deliverable) {
							scope.data.deliverable = [];
						}
					}
				});
	        },
			templateUrl: './js/directives/demand/details.html'
		};
	});
});