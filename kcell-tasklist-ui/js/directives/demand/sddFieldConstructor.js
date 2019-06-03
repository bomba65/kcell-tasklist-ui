define(['./../module'], function(module){
  'use strict';
  module.directive('demandSddFieldConstructor', function ($rootScope, $http, $timeout, exModal) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        section: '='
      },
      link: function(scope, element, attrs) {
        scope.selected = null;
        scope.sections = [
          'Functional requirements',
          'Non-Functional requirements',
          'Solution design',
          'Components'
        ];

        scope.leftIndex = null;
        scope.rightIndex = null;
        scope.isCustom = false;
        scope.onLeftSelect = function(index) {
          scope.rightIndex = null;
          scope.leftIndex = index;
        };

        scope.onRightSelect = function(index, customField) {
          scope.leftIndex = null;
          scope.rightIndex = index;
          scope.isCustom = customField;
        };

        scope.moveRight = function() {
          if (scope.leftIndex === -1) scope.data.customs.push({name: ''});
          else scope.data.fields[scope.leftIndex].hidden = false;
          scope.leftIndex = null;
        };

        scope.moveLeft = function() {
          exModal.open({
            scope: {
              message: 'Are you sure you want to delete the field?',
              cancel: 'No',
              ok: 'Yes'
            },
            templateUrl: './js/partials/confirmModal.html',
            size: 'sm'
          }).then(function() {
            if (scope.isCustom) scope.data.customs.splice(scope.rightIndex, 1);
            else scope.data.fields[scope.rightIndex].hidden = true;
            scope.rightIndex = null;
          });
        };
      },
      templateUrl: './js/directives/demand/sddFieldConstructor.html'
    };
  });
});