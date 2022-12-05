define(['./../module'], function(module){
  'use strict';
  module.directive('revolvingDirectProtocol', function ($rootScope, $http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: '='
      },
      link: function(scope, element, attrs) {
        scope.onChannelCapacityChange = function () {
          if (!scope.data.sessionsCount || !scope.data.coding) return;
          if (scope.data.coding === 'g711') {
            scope.data.channelCapacity = 87.2 * scope.data.sessionsCount / 1024;
          } else {
            scope.data.channelCapacity = 32.2 * scope.data.sessionsCount / 1024;
          }
        };
      },
      templateUrl: './js/directives/revolvingNumbers/directProtocol.html'
    };
  });
});