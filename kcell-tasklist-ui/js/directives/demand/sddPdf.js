define(['./../module'], function(module){
  'use strict';
  module.directive('demandSddPdf', function ($rootScope, $http, $sce) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        elementId: '=',
        bKey: '=',
        dName: '=',
        dDescription: '='
      },
      link: function(scope, element, attrs) {
        scope.today = new Date();
        scope.sections = [
          {id: 'funcRequirements', name: 'Functional requirements'},
          {id: 'nonFuncRequirements', name: 'Non-Functional requirements'},
          {id: 'solutionDesign', name: 'Solution design'},
          {id: 'components', name: 'Components'},
        ];
        scope.trustedHtml = function(html) {
          return $sce.trustAsHtml(html);
        };
        scope.hasFields = function(section) {
          if (section.customs && section.customs.length) return true;
          if (!section.fields || !section.fields.length) return false;
          for (var f of section.fields) {
            if (f && f.hidden === false) return true;
          }
          return false;
        };
      },
      templateUrl: './js/directives/demand/sddPdf.html'
    };
  });
});