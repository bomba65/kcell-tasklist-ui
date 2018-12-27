define(['./module'], function(module){
  'use strict';
  module.directive('demandProcess', ['$http', '$timeout', function ($http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      link: function(scope, el, attrs) {
      },
      templateUrl: './js/directives/demandProcess.html'
    };
  }]);
});