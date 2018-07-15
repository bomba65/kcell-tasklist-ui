define(['./../module'], function(module){
	'use strict';
	module.directive('demandSupportiveInputs', function ($rootScope, $http) {
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
					smartButtonMaxItems: 1,
					showCheckAll: false,
					showUncheckAll: false,
					displayProp: 'unit',
					idProp: 'unit',
					externalIdProp: 'unit'
				};
				scope.checkListOptions = [
					{unit: 'Human Resource Department'},
					{unit: 'Cetnralized Procurement Department'},
					{unit: 'Technology Department'},
				];
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.checkList) scope.data.checkList = [];
                    }
                });
	        },
			templateUrl: './js/directives/demand/supportiveInputs.html'
		};
	});
});