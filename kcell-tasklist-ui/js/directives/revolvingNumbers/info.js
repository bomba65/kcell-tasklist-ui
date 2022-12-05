define(['../module'], function(module){
  'use strict';
  module.directive('pbxRevolvingNumbersInfo', ['$http', '$timeout', function ($http, $timeout) {
    return {
      restrict: 'E',
      scope: {
        data: '='
      },
      link: function(scope, el, attrs){
        scope.download = function(path) {
          $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
          then(function(response) {
            document.getElementById('fileDownloadIframe').src = response.data;
          }, function(error){
            console.log(error.data);
          });
        };
      },
      templateUrl: './js/directives/revolvingNumbers/info.html'
    };
  }]);
});
