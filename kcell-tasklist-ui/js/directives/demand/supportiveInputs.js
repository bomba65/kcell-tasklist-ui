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
					{unit: 'Technology Department', form: 'Other'},
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
					{unit: 'Interconnect and Roaming Section', form: 'Other'},
					{unit: 'Tax Unit', form: 'Other'},
					{unit: 'Fraud Management and Revenue Assurance Section', form: 'Other'},
					{unit: 'Risk and Controls Section', form: 'Other'},
					{unit: 'Reporting Unit', form: 'Other'},
					{unit: 'Legal Affairs and Government Relations Department', form: 'LD'},
					{unit: 'Almaty Regional Office', form: 'Other'},
					{unit: 'Customer Relations and Experience Section', form: 'Other'},
					{unit: 'Macro-Region A Section', form: 'Other'},
					{unit: 'Macro-Region B Section', form: 'Other'},
					{unit: 'Macro-Region C Section', form: 'Other'},
					{unit: 'Regional Channels Management Section', form: 'Other'},
					{unit: 'Remote Sales and Service Section', form: 'Other'},
					{unit: 'Business Development Section', form: 'Other'},
					{unit: 'Delivery & Operations Section', form: 'Other'},
					{unit: 'Large Accounts Development Section', form: 'Other'},
					{unit: 'Process and Performance Section', form: 'Other'},
					{unit: 'Products Development Section', form: 'Other'},
					{unit: 'Service Section', form: 'Other'},
					{unit: 'Small and Medium Accounts Development Section', form: 'Other'},
					{unit: 'Strategic Accounts Development Section', form: 'Other'},
					{unit: 'Accounting Section', form: 'Other'},
					{unit: 'Business Control, Analysis and Reporting Section', form: 'Other'},
					{unit: 'Treasury Section', form: 'Other'},
					{unit: 'Internal Audit Section', form: 'Other'},
					{unit: 'Contract Management and Litigation Section', form: 'Other'},
					{unit: 'Almaty Region Section', form: 'Other'},
					{unit: 'Astana Region Section', form: 'Other'},
					{unit: 'Development and Innocations Section', form: 'Other'},
					{unit: 'East Region Section', form: 'Other'},
					{unit: 'Information Security Section', form: 'Other'},
					{unit: 'IT Section', form: 'Other'},
					{unit: 'Network and Infrastructure Section', form: 'Other'},
					{unit: 'Network Development and Operation Division', form: 'Other'},
					{unit: 'North Region Section', form: 'Other'},
					{unit: 'Portfolio and Project Management Offoce', form: 'Other'},
					{unit: 'Products and Services Section', form: 'Other'},
					{unit: 'Service Assurance and Operations Section', form: 'Other'},
					{unit: 'South Region Section', form: 'Other'},
					{unit: 'Technical Planning Section', form: 'Other'},
					{unit: 'West Region Section', form: 'Other'}
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