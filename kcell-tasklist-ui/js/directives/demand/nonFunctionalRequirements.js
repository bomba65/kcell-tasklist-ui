define(['./../module'], function(module){
	'use strict';
	module.directive('demandNonFuncRequirements', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
          data: '=',
          form: '=',
          view: '=',
          disabled: '='
			},
			link: function(scope, element, attrs) {
          scope.$watch('data', function(value) {
              if (value) {
                  if (!scope.data) scope.data = {};
              }
          });
        },
			templateUrl: './js/directives/demand/nonFunctionalRequirements.html'
		};
	});
});