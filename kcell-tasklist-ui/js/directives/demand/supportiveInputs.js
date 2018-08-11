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
				scope.multiselectEvents = {
					onItemSelect: function(item) {
						var elt = scope.data.find(function(e) {return e.unit == item.unit});
						var opt = scope.optionList.find(function(e) {return e.unit == item.unit});
						if (elt && opt) angular.copy(opt, elt);
					}
				};
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
					{unit: 'Technology Department', form: 'TD'},
					{unit: 'Human Resource Department', form: 'HR'},
					{unit: 'Infrasturcture Procurement Section', form: 'CPD'},
					{unit: 'Operational Procurement Section', form: 'CPD'},
					{unit: 'Products and Services Procurement Section', form: 'CPD'},
					{unit: 'Warehouse and Logistics Section', form: 'CPD'},
					{unit: 'Consumer Marketing Section', form: 'CPD'},
					{unit: 'Digital Marketing Section', form: 'CPD'},
					{unit: 'Enterprise Marketing Section', form: 'CPD'},
					{unit: 'Real Estate Section', form: 'CPD'},
					{unit: 'Business Intelligence - CCD', form: 'BI'},
					{unit: 'Business Intelligence - B2C', form: 'BI'},
					{unit: 'Business Intelligence - B2B', form: 'BI'},
					{unit: 'Business Intelligence - TD', form: 'BI'},
					{unit: 'Interconnect and Roaming Section', form: 'Common'},
					{unit: 'Tax Unit', form: 'Common'},
					{unit: 'Fraud Management and Revenue Assurance Section', form: 'Common'},
					{unit: 'Risk and Controls Section', form: 'Common'},
					{unit: 'Reporting Unit', form: 'Common'},
					{unit: 'Legal Affairs and Government Relations Department', form: 'LD'},
					{unit: 'Almaty Regional Office', form: 'Common'},
					{unit: 'Customer Relations and Experience Section', form: 'Common'},
					{unit: 'etc.', form: 'Common'}
				];
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                    }
                });
	        },
			templateUrl: './js/directives/demand/supportiveInputs.html'
		};
	});
});