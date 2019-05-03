define(['./module', 'jquery'], function (app) {
    "use strict";
    app.directive('selectPicker', function ($timeout) {
        return {
            restrict: 'A',
            //scope: false,
            //controller: '@',
            priority: 1000, // same as ng-controller
            link: function (scope, element, attrs) {
                function refresh() {
                    scope.$applyAsync(function () {
                        $('.selectpicker').selectpicker('refresh');
                    });
                }
                $timeout(function () {
                    $('.selectpicker').selectpicker("");
                });
                if (attrs.ngModel) {
                    scope.$watch(attrs.ngModel, refresh, true);
                }
                if (attrs.selectModel) {
                    scope.$watch(attrs.selectModel, refresh, true);
                }


            }
        };
    });


});
