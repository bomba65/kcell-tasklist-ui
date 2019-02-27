define(['../module'], function(module){
  'use strict';
  module.directive('infoAftersalesPbx', ['$http', '$timeout', function ($http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        aftersalesInfo: '='
      },
      link: function(scope, el, attrs){
        if (scope.aftersalesInfo.starter) {
          scope.aftersalesInfo.starterName = scope.aftersalesInfo.starter;
          $http.get('/camunda/api/engine/engine/default/user/' + scope.aftersalesInfo.starter + '/profile').then(
            function (result) {
              scope.aftersalesInfo.starterName = result.data.firstName + ' ' + result.data.lastName;
            },
            function (error) {
              console.log(error.data);
            }
          );
        }
        scope.download = function(path) {
          $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
          then(function(response) {
            document.getElementById('fileDownloadIframe').src = response.data;
          }, function(error){
            console.log(error.data);
          });
        };
      },
      templateUrl: './js/directives/aftersalesPBX/infoAftersalesPBX.html'
    };
  }]);
});
