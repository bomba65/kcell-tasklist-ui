define(['./../module'], function(module){
	'use strict';
	module.directive('demandSupportiveInputs', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
				disabled: '='
			},
			link: function(scope, element, attrs) {
				scope.multiselectSettings = {
					enableSearch: true,
					smartButtonMaxItems: 1,
					showCheckAll: false,
					showUncheckAll: false,
					displayProp: 'unit',
					idProp: 'unit',
					externalIdProp: 'unit'
				};
				scope.optionList = [
					{unit: 'Technology Department'},
					{unit: 'Human Resource Department'},
					{unit: 'Infrasturcture Procurement Section'},
					{unit: 'Operational Procurement Section'},
					{unit: 'Products and Services Procurement Section'},
					{unit: 'Warehouse and Logistics Section'},
					{unit: 'Consumer Marketing Section'},
					{unit: 'Digital Marketing Section'},
					{unit: 'Enterprise Marketing Section'},
					{unit: 'Real Estate Section'},
					{unit: 'Business Intelligence - CCD'},
					{unit: 'Business Intelligence - B2C'},
					{unit: 'Business Intelligence - B2B'},
					{unit: 'Business Intelligence - TD'},
					{unit: 'Interconnect and Roaming Section'},
					{unit: 'Tax Unit'},
					{unit: 'Fraud Management and Revenue Assurance Section'},
					{unit: 'Risk and Controls Section'},
					{unit: 'Reporting Unit'},
					{unit: 'Legal Affairs and Government Relations Department'},
					{unit: 'Almaty Regional Office'},
					{unit: 'Customer Relations and Experience Section'},
					{unit: 'etc.'}
				];
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = [];
                    }
                });
	        },
			templateUrl: './js/directives/demand/supportiveInputs.html'
		};
	});
});