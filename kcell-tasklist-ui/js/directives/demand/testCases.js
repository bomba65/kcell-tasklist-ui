define(['./../module'], function(module){
  'use strict';
  module.directive('demandTestCases', function ($rootScope, $http, exModal) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        useCases: '=',
        form: '=',
        view: '=',
        disabled: '=',
        status: '='
      },
      link: function(scope, element, attrs) {
        scope.$watch('data', function(value) {
          if (value) {
            if (!scope.data) scope.data = {};
            if (!scope.useCaseCollapsed) scope.useCaseCollapsed = [];
          }
        });

        scope.testCaseAdd = function(index) {
          if (!scope.data[index]) scope.data[index] = [];
          scope.data[index].push({value: '', status: ''});
        };

        scope.testCaseDelete = function(uIndex, index) {
          exModal.open({
            templateUrl: './js/partials/confirmModal.html',
            size: 'sm'
          }).then(function() {
            scope.data[uIndex].splice(index, 1);
          });
        };

        scope.toggleUseCaseCollapse = function(el, index) {
          if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
          scope.useCaseCollapsed[index] = !scope.useCaseCollapsed[index];
        };
      },
      templateUrl: './js/directives/demand/testCases.html'
    };
  });
});
