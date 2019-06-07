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
          {id: 'funcRequirements', name: 'Functional requirements'},
          {id: 'nonFuncRequirements', name: 'Non-Functional requirements'},
          {id: 'solutionDesign', name: 'Solution design'},
          {id: 'components', name: 'Components'}
        ];

        scope.leftIndex = {size: 0};
        scope.rightIndex = {size: 0};
        scope.customIndex = {size: 0};
        scope.onLeftSelect = function(index) {
          // scope.rightIndex = {size: 0};
          if (scope.leftIndex[index]) {
            scope.leftIndex.size--;
            scope.leftIndex[index] = false;
          } else {
            scope.leftIndex.size++;
            scope.leftIndex[index] = true;
          }
        };

        scope.onRightSelect = function(index, customField) {
          // scope.leftIndex = {size: 0};
          if (customField) {
            if (scope.customIndex[index]) {
              scope.customIndex.size--;
              scope.customIndex[index] = false;
            } else {
              scope.customIndex.size++;
              scope.customIndex[index] = true;
            }
          } else {
            if (scope.rightIndex[index]) {
              scope.rightIndex.size--;
              scope.rightIndex[index] = false;
            } else {
              scope.rightIndex.size++;
              scope.rightIndex[index] = true;
            }
          }
        };

        scope.moveRight = function() {
          for (var i in scope.leftIndex) {
            if (i === 'size') continue;
            else if (i === '-1') scope.data[scope.sections[scope.section].id].customs.push({name: ''});
            else scope.data[scope.sections[scope.section].id].fields[parseInt(i)].hidden = false;
          }
          scope.leftIndex = {size: 0};
        };

        scope.moveLeft = function() {
          exModal.open({
            scope: {
              message: 'Are you sure you want to delete the field' + ((scope.rightIndex.size + scope.customIndex.size > 1)?'s':'') + '?',
              cancel: 'No',
              ok: 'Yes'
            },
            templateUrl: './js/partials/confirmModal.html',
            size: 'sm'
          }).then(function() {
            for (var i in scope.rightIndex) {
              if (i === 'size') continue;
              else scope.data[scope.sections[scope.section].id].fields[parseInt(i)].hidden = true;
            }
            for (var i in scope.customIndex) {
              if (i === 'size') continue;
              else scope.data[scope.sections[scope.section].id].customs.splice(parseInt(i), 1);
            }
            scope.leftIndex = {size: 0};
            scope.customIndex = {size: 0};
          });
        };

        scope.changeSection = function(index) {
          scope.section = index;
        };
      },
      templateUrl: './js/directives/demand/sddFieldConstructor.html'
    };
  });
});