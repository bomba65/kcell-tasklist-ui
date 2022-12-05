define(['../module'], function(module){
    'use strict';
    module.directive('demandSearch', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: false,
            link: function(scope, el, attrs) {
                scope.$watch('tabs.DemandUAT', function (switcher) {
                    if (switcher) {
                        $timeout(function() {
                            $("[elastic-textarea]")[0].style.height = 'auto';
                            $("[elastic-textarea]")[0].style.height = ($("[elastic-textarea]")[0].scrollHeight) + 'px';
                        });
                    }
                });
            },
            templateUrl: './js/directives/search/demandSearch.html'
        };
    }]);
});