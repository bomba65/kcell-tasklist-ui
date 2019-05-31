define(['../module', 'jquery'], function (app) {
    "use strict";
    app.directive('calendarRange', function ($timeout, $parse) {
        return {
            restrict: 'A',
            //scope: false,
            //controller: '@',
            priority: 1000, // same as ng-controller
            link: function (scope, element, attrs) {
                var element_name = element[0].name;
                var startDate;
                var endDate;
                var holidays = ['1/1', '2/1', '7/1', '8/3', '21/3',
                    '22/3', '23/3', '1/5', '7/5', '9/5', '10/5', '6/7', '30/8', '1/12', '16/12', '17/12'];
                var substitues = [];
                var substitude_done = [];
                for (var i=0; i<holidays.length; i++) {
                    substitude_done.push(false);
                }

                let setMultipleDatePicker = () => {
                    $('input[name="' + element_name + '"]').daterangepicker({
                            startDate: startDate,
                            endDate: endDate,
                            autoUpdateInput: false,
                            linkedCalendars: false,
                            autoApply: true,
                            showDropdowns: true,
                            isCustomDate: function (date) {
                                var day_type = date.toDate().getDay();
                                var format_date = date.format('D/M');
                                var next_day = "";
                                var with_year = "";

                                if (holidays.indexOf(format_date) > -1) {
                                    var days_to_jump = 1;
                                    if (day_type === 6 || day_type === 0) {
                                        if (day_type === 6) {
                                            days_to_jump += 1;
                                        }
                                        next_day = getNextDay(date, days_to_jump)[0];
                                        with_year = getNextDay(date, days_to_jump)[1];
                                        while (holidays.indexOf(next_day) !== -1 ||
                                        (!substitude_done[holidays.indexOf(format_date)] && substitues.indexOf(with_year) !== -1)) {
                                            days_to_jump += 1;
                                            next_day = getNextDay(date, days_to_jump)[0];
                                            with_year = getNextDay(date, days_to_jump)[1];
                                            //substitude_done[substitues.indexOf(with_year)] = false;
                                        }
                                        substitude_done[holidays.indexOf(format_date)] = true;
                                        if (substitues.indexOf(with_year) === -1) {
                                            substitues.push(with_year);
                                        }

                                    }
                                    return 'calendar-holiday';
                                } else if (substitues.indexOf(date.format('D/M/YYYY')) > -1) {
                                    return 'calendar-holiday';
                                }


                            },
                            locale: {
                                format: 'DD.MM.YYYY',
                                cancelLabel: 'Clear'
                            }
                        }
                    );

                    function getNextDay(date, index) {
                        var tomorrow = new Date(date.toDate());
                        tomorrow.setDate(tomorrow.getDate() + index);
                        var month = tomorrow.getMonth() + 1;
                        var next_day = tomorrow.getDate();
                        var year = tomorrow.getFullYear();
                        var a = next_day + '/' + month;
                        var b = next_day + '/' + month + '/' + year;
                        return [a, b];
                    }

                    $('input[name="' + element_name + '"]').on('apply.daterangepicker', function (ev, picker) {

                        var x = $(this).attr('ng-model');
                        var model = $parse(x);
                        scope.$apply(function () {
                            model.assign(scope, picker.startDate.format('DD.MM.YY') + ' - ' + picker.endDate.format('DD.MM.YY'));
                        });
                    });


                    element.on('click', function (e) {
                        $('input[name="' + element_name + '"]').click();
                    });

                };
                $timeout(setMultipleDatePicker);


            }
        };
    });


});