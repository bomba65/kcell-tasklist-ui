define(['./../module'], function(module){
    'use strict';
    module.directive('demandDropdown', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                ngModel: '=',
                options: '=',
                onItemSelect: '=',
                searchField: '=',
                objectList: '=',
                displayProp: '=',
                titleProp: '=',
                index: '=',
                disabled: '=',
                nowrap: '=',
                fieldName: '=',
                isRequired: '=',
                toggleCallback: '='
            },
            link: function(scope, el, attrs) {

                //TODO: position ul-list by js (so it didn't depend on overflow)

                scope.$watch('ngModel', function (value) {
                    scope.theModel = scope.ngModel;
                }, true);

                scope.theModel = null;
                scope.isOpen = false;
                scope.searchVal = '';

                $(document).bind('click', function(e){
                    if (el !== e.target && e.target.classList.contains('page-disabler') && scope.isOpen) {
                        scope.$apply(function () {
                            scope.isOpen = false;
                            if (scope.toggleCallback) scope.toggleCallback(scope.isOpen);
                        });
                    }
                });

                var setWidth = function() {
                    var element = el[0].querySelector('.list-group');
                    var curWidth = element.offsetWidth, scrollWidth;
                    element.style.width = 'auto';
                    scrollWidth = element.scrollWidth;
                    if (scrollWidth > curWidth) element.style.width = scrollWidth + 'px';
                    else element.style.width = curWidth + 'px';
                };

                scope.toggleSelect = function() {
                    scope.isOpen = !scope.isOpen;
                    scope.searchVal = '';

                    if (scope.isOpen) $timeout(setWidth);
                    if (scope.toggleCallback) scope.toggleCallback(scope.isOpen);
                };

                scope.selectOption = function(option) {
                    scope.toggleSelect();
                    scope.theModel = option;
                    if (scope.onItemSelect) scope.onItemSelect(scope.index, option);
                };

                scope.filterFunc = function (value, index, array) {
                    if (!scope.objectList) return (value + '').toLowerCase().includes(scope.searchVal.toLowerCase());
                    return (value[scope.displayProp] + '').toLowerCase().includes(scope.searchVal.toLowerCase());
                };
            },
            templateUrl: './js/directives/demand/demandDropdown.html'
        };
    }]);
});