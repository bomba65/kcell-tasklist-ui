define(['./../module'], function(module){
    'use strict';
    module.directive('leasingAttachments', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                leasingFiles: '=',
                deletedFiles: '=',
                taskKey: '=',
                modify: '='
            },
            link: function(scope, el, attrs){
                console.log(scope)
                scope.fileDownload = function(file) {
                    $http({method: 'GET', url: '/camunda/uploads/get/' + file.path, transformResponse: [] }).
                    then(function(response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function(error){
                        console.log(error.data);
                    });
                };

                scope.clearFile = function (list, fileIndex) {
                    if (scope.deletedFiles){
                        scope.deletedFiles.push(scope[list][fileIndex])
                    }
                    scope[list].splice(fileIndex, 1);
                };
            },
            templateUrl: './js/directives/leasing/leasingAttachments.html'
        };
    }]);
});
