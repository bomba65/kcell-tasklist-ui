define(['./../module'], function(module){
    'use strict';
    module.directive('leasingAttachments', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                leasingFiles: '='
            },
            link: function(scope, el, attrs){
                scope.fileDownload = function(file) {
                    $http({method: 'GET', url: '/camunda/uploads/get/' + file.path, transformResponse: [] }).
                    then(function(response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function(error){
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/leasing/leasingAttachments.html'
        };
    }]);
});
