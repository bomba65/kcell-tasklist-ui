define(['./../module'], function(module){
  'use strict';
  module.directive('revolvingSipProtocol', function ($rootScope, $http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: '='
      },
      link: function(scope, element, attrs) {
      },
      templateUrl: './js/directives/revolvingNumbers/sipProtocol.html'
    };
  });
});