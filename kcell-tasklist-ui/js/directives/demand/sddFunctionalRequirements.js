define(['./../module'], function(module){
  'use strict';
  module.directive('demandSddFuncRequirements', function ($rootScope, $http, exModal) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: '=',
        processId: '=',
        taskId: '='
      },
      link: function(scope, element, attrs) {
        const fields = [
          {name: 'User functions', hidden: true, value: ''},
          {name: 'Administrative functions', hidden: true, value: ''},
          {name: 'Product connection', hidden: true, value: ''},
          {name: 'Product suspention', hidden: true, value: ''},
          {name: 'Product renewal', hidden: true, value: ''},
          {name: 'Product disconnection', hidden: true, value: ''},
          {name: 'Charging', hidden: true, value: ''},
          {name: 'Invoicing', hidden: true, value: ''}
        ];
        scope.showScenarios = false;
        scope.$watch('data', function(value) {
          if (value) {
            if (!scope.data) scope.data = {};
            if (!scope.data.fields) scope.data.fields = fields;
            if (!scope.data.customs) scope.data.customs = [];
            scope.showScenarios = false;
            for (var i = 2; i < 6; i++)
              if (!scope.data.fields[i].hidden) {
                scope.showScenarios = true;
                break;
              }
          }
        }, true);
      },
      templateUrl: './js/directives/demand/sddFunctionalRequirements.html'
    };
  });
});