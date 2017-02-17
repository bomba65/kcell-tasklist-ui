'use strict';
define('job-request-module', ['angular'], function (angular) {
  var customModule = angular.module('kcell.custom.module', []);

  customModule.directive('jobRequest', function () {
    return {
      restrict: 'E',
      scope: {
        jobModel: '='
      },
      templateUrl: '/customScripts/jobRequest.html'
    };
  });
  return customModule;
});