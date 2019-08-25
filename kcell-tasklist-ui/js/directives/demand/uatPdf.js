define(['./../module'], function(module){
  'use strict';
  module.directive('demandUatPdf', function ($rootScope, $http, $sce) {
    return {
      restrict: 'E',
      scope: {
        uat: '=',
        bKey: '=',
        dName: '=',
        dOwner: '=',
        dDescription: '=',
        useCases: '=',
        testCases: '=',
        appList: '='
      },
      link: function(scope, element, attrs) {
        scope.date = new Date();
        setInterval(function() {
          scope.date = new Date();
        }, 60 * 1000);
        scope.trustedHtml = function(html) {
          return $sce.trustAsHtml(html);
        };
      },
      templateUrl: './js/directives/demand/uatPdf.html'
    };
  });
});
