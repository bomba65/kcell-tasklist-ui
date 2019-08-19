define(['../module'], function(module){
    'use strict';
    module.directive('revisionSearch', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: false,
            link: function(scope, el, attrs) {
            },
            templateUrl: './js/directives/search/revisionSearch.html'
        };
    }]);
});