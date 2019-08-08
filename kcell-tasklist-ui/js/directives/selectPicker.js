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


                $(function() {
                    $('.selectpicker').on('show.bs.select', function () {

                        var dropdownList = $('.dropdown-menu'),
                            dropdownOffset = $(this).offset(),
                            offsetLeft = dropdownOffset.left,
                            dropdownWidth = dropdownList.width(),
                            docWidth = $(window).width(),

                            subDropdown = dropdownList.eq(1),
                            subDropdownWidth = subDropdown.width(),

                            isDropdownVisible = (offsetLeft + dropdownWidth <= docWidth),
                            isSubDropdownVisible = (offsetLeft + dropdownWidth + subDropdownWidth <= docWidth);

                        if (!isDropdownVisible || !isSubDropdownVisible) {
                            dropdownList.addClass('pull-right');
                        } else {
                            dropdownList.removeClass('pull-right');
                        }
                    });
                });



                $timeout(function () {
                    $('.selectpicker').selectpicker("");
                });
                if (attrs.ngModel) {
                    scope.$watch(attrs.ngModel, refresh, true);
                }
                if (attrs.selectModel) {
                    scope.$watch(attrs.selectModel, refresh, true);
                }
                if (attrs.ngDisabled) {
                    scope.$watch(attrs.ngDisabled, refresh, true);
                }
                if (attrs.ngRequired) {
                    scope.$watch(attrs.ngRequired, refresh, true);
                }


            }
        };
    });


});
