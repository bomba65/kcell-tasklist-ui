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
					{unit: 'Interconnect and Roaming Section', form: 'Others'},
					{unit: 'Tax Unit', form: 'Others'},
					{unit: 'Fraud Management and Revenue Assurance Section', form: 'Others'},
					{unit: 'Risk and Controls Section', form: 'Others'},
					{unit: 'Reporting Unit', form: 'Others'},
					{unit: 'Legal Affairs and Government Relations Department', form: 'LD'},
					{unit: 'Almaty Regional Office', form: 'Others'},
					{unit: 'Customer Relations and Experience Section', form: 'Others'},
					{unit: 'Macro-Region A Section', form: 'Others'},
					{unit: 'Macro-Region B Section', form: 'Others'},
					{unit: 'Macro-Region C Section', form: 'Others'},
					{unit: 'Regional Channels Management Section', form: 'Others'},
					{unit: 'Remote Sales and Service Section', form: 'Others'},
					{unit: 'Business Development Section', form: 'Others'},
					{unit: 'Delivery & Operations Section', form: 'Others'},
					{unit: 'Large Accounts Development Section', form: 'Others'},
					{unit: 'Process and Performance Section', form: 'Others'},
					{unit: 'Products Development Section', form: 'Others'},
					{unit: 'Service Section', form: 'Others'},
					{unit: 'Small and Medium Accounts Development Section', form: 'Others'},
					{unit: 'Strategic Accounts Development Section', form: 'Others'},
					{unit: 'Accounting Section', form: 'Others'},
					{unit: 'Business Control, Analysis and Reporting Section', form: 'Others'},
					{unit: 'Treasury Section', form: 'Others'},
					{unit: 'Internal Audit Section', form: 'Others'},
					{unit: 'Contract Management and Litigation Section', form: 'Others'},
					{unit: 'Almaty Region Section', form: 'Others'},
					{unit: 'Astana Region Section', form: 'Others'},
					{unit: 'Development and Innocations Section', form: 'Others'},
					{unit: 'East Region Section', form: 'Others'},
					{unit: 'Information Security Section', form: 'Others'},
					{unit: 'IT Section', form: 'Others'},
					{unit: 'Network and Infrastructure Section', form: 'Others'},
					{unit: 'Network Development and Operation Division', form: 'Others'},
					{unit: 'North Region Section', form: 'Others'},
					{unit: 'Portfolio and Project Management Offoce', form: 'Others'},
					{unit: 'Products and Services Section', form: 'Others'},
					{unit: 'Service Assurance and Operations Section', form: 'Others'},
					{unit: 'South Region Section', form: 'Others'},
					{unit: 'Technical Planning Section', form: 'Others'},
					{unit: 'West Region Section', form: 'Others'}
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