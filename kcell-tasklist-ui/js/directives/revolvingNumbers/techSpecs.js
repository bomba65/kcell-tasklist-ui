define(['./../module'], function(module){
  'use strict';
  module.directive('revolvingTechnicalSpecifications', function ($rootScope, $http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: '=',
        activeConnType: '=',
        callbackForwarding: '='
      },
      link: function(scope, element, attrs) {
        scope.$watch('data', function (value) {
          if (value) {
            if (!scope.data.connectionType) scope.data.connectionType = 'SIP over internet';
            if (!scope.data.vpnRequired) scope.data.vpnRequired = false;
          }
        });

        scope.$on('tab-selected', function(e, tabName) {
          if (tabName === 'techSpec') {
            var pbxTmp = scope.data.pbxNumbers;
            scope.data.pbxNumbers = 'this is because of tabset';
            var descriptionTmp = scope.data.description;
            scope.data.description = 'this is because of tabset';
            $timeout(function () {
              scope.data.pbxNumbers = pbxTmp;
              scope.data.description = descriptionTmp;
            });
          }
        });
      },
      templateUrl: './js/directives/revolvingNumbers/techSpecs.html'
    };
  });
});