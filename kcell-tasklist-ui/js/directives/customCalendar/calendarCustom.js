define(['../module', 'angular', 'angular-ui-bootstrap'], function (app, angular) {
    "use strict";
    var myApp = angular.module('ui.bootstrap');

    function Decorate($provide, datepickerPopupConfig, datepickerConfig) {
        datepickerPopupConfig.showButtonBar = false;
        datepickerConfig.showCustomRangeLabel = true;
        datepickerConfig.startingDay = 1;
        $provide.decorator('daypickerDirective', function ($delegate, $rootScope) {
            var directive = $delegate[0];
            var holidays = $rootScope.holidays;
            var weekendWorking = $rootScope.weekendWorking;
            directive.templateUrl = './js/directives/customCalendar/dayPicker.html';
            var link = directive.link;
            directive.compile = function() {
                return function(scope, element, attrs, ngModelCtrl) {
                    link.apply(this, arguments);
                    //scope.weekends = holidays;

                    scope.$watchGroup(['rows'], function() {
                        var mark_next = 0;
                        angular.forEach(scope.rows, function(row) {
                            angular.forEach(row, function(day) {
                                var year = day.date.getFullYear();
                                var month = day.date.getMonth() + 1;
                                var thisDay = day.date.getDate();
                                var day_type = day.date.getDay();

                                if (holidays.includes(thisDay+'/'+month)) {
                                    day.holiday = true;
                                }
                                if (mark_next>0 && !day.holiday &&!(day_type===0 || day_type===6) ) {
                                    day.holiday = true;
                                    mark_next-=1;
                                }
                                if (day.holiday && (day_type===6 || day_type===0)) {
                                    mark_next+=1;
                                }
                                if (!day.holiday && (day_type===6 || day_type===0) &&
                                    !weekendWorking.includes(thisDay+'/'+month + '/' + year)) {
                                    day.weekDay = true;
                                }
                            });
                        });
                    });
                };
            };
            return $delegate;
        });

        $provide.decorator('datepickerPopupWrapDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/calendarPopUp.html';
            return $delegate;
        });
        $provide.decorator('monthpickerDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/monthPicker.html';
            return $delegate;
        });
        $provide.decorator('yearpickerDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/yearPicker.html';
            return $delegate;
        });

    }

    myApp.config(['$provide', 'datepickerPopupConfig', "datepickerConfig", Decorate]);


});
