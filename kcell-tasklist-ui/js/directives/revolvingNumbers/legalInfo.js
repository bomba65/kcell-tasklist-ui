define(['./../module'], function(module){
  'use strict';
  module.directive('revolvingLegalInformation', function ($rootScope, $http, toasty) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: "=",
        fixed: "="
      },
      link: function(scope, element, attrs) {
        scope.$watch('data', function (value) {
          if (value) {
            if (!scope.data.companyDate) scope.data.companyDate = new Date();
            else scope.data.companyDate = new Date(scope.data.companyDate);
            if (!scope.data.termContract) scope.data.termContract = new Date();
            else scope.data.termContract = new Date(scope.data.termContract);
            if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
          }
        });
      },
      templateUrl: './js/directives/revolvingNumbers/legalInfo.html'
    };
  });
});