define(['./../module', 'xlsx'], function(module){
    'use strict';
    module.directive('demandBusinessCase', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showBanchmark: '=',
                onExcelImport: '=',
                editableQualitative: '=',
            },
            link: function(scope, element, attrs) {

                scope.selects = {
                    ipmDecision: {
                        visible: false,
                        options: [
                            'Идея оценена - не реализуется',
                            'Проект утвержден и реализуется',
                            'Проект реализован есть результаты'
                        ]
                    },
                    strategyFit: {
                        visible: false,
                        options: [
                            'Values through superior network connectivity',
                            'Customer loyalty through convergence',
                            'Competitive operations',
                            'Growth in adjacencies',
                            'Regular expenses'
                        ]
                    },
                    businessPriority: {
                        visible: false,
                        options: [
                            'A - Delayed project realization would have no impact on the business processes.',
                            'B - The delayed project realization would affect the business processes.',
                            'C - The delayed project realization would stop the business processes.'
                        ]
                    },
                    opActivitiesImpact: {
                        visible: false,
                        options: [
                            'A - The delayed project realization would not affect the daily operational activities.',
                            'B - The delayed project realization would affect the daily operational activities.',
                            'C - The delayed project realization would significantly hinder important daily operational activities.'
                        ]
                    }
                };

                scope.onItemSelect = function(index, option) {
                    scope.data[index] = option;
                    scope.onChange();
                };

                function initData() {
                    scope.data = {
                        general: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: []
                        },
                        cashFlow: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: [],
                            revenuesTotal: {},
                            capexesTotal: {},
                            othersTotal: {},
                            incomeTotal: {}
                        },
                        accurals: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: [],
                            revenuesTotal: {},
                            capexesTotal: {},
                            othersTotal: {},
                            incomeTotal: {}
                        },
                        firstYear: (new Date()).getFullYear() + 1,
                        benchmark: {
                            minPP: 0.8,
                            maxROI: 94,
                            maxNPV: 8205418520.33,
                            maxPL: 2420366789.82,
                            maxCF: 2420366789.82,
                            wacc: 13.6
                        },
                        irr: 0.0,
                        strategicGoal: 'strategicGoal growth',
                        ipmDecision: scope.selects.ipmDecision.options[0],
                        strategyFit: scope.selects.strategyFit.options[0],
                        businessPriority: scope.selects.businessPriority.options[0],
                        opActivitiesImpact: scope.selects.opActivitiesImpact.options[0]
                    };

                    scope.onChange();
                }

                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data) initData();
                        if (!scope.data.general) {
                            scope.data.general = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: []
                            };
                        }
                        if (!scope.data.cashFlow) {
                            scope.data.cashFlow = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: [],
                                revenuesTotal: {},
                                capexesTotal: {},
                                othersTotal: {},
                                incomeTotal: {}
                            };
                        }
                        if (!scope.data.accurals) {
                            scope.data.accurals = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: [],
                                revenuesTotal: {},
                                capexesTotal: {},
                                othersTotal: {},
                                incomeTotal: {}
                            };
                        }
                        if (!scope.data.firstYear) scope.data.firstYear = (new Date()).getFullYear() + 1;
                        if (!scope.data.benchmark) {
                            scope.data.benchmark = {
                                minPP: 0.8,
                                maxROI: 94,
                                maxNPV: 8205418520.33,
                                maxPL: 2420366789.82,
                                maxCF: 2420366789.82,
                                wacc: 13.6
                            };
                        }
                        if (!scope.data.irr) scope.data.irr = 0.0;
                        if (!scope.data.strategicGoal) scope.data.strategicGoal = 'strategicGoal growth';
                        for (var field of ['ipmDecision', 'strategyFit', 'businessPriority', 'opActivitiesImpact'])
                            if (!scope.data[field])
                                scope.data[field] = scope.selects[field].options[0];

                        scope.onChange();
                    }
                });

                var strategyFitPercentage = function() {
                    if (!scope.data.strategyFit) return 0;

                    if (scope.data.strategyFit.startsWith('Values')) return 1;
                    else if (scope.data.strategyFit.startsWith('Customer')) return 1;
                    else if (scope.data.strategyFit.startsWith('Competitive')) return 1;
                    else if (scope.data.strategyFit.startsWith('Growth')) return 0.8;
                    else return 0;
                };

                var businessPriorityPercentage = function() {
                    if (!scope.data.businessPriority) return 0;

                    if (scope.data.businessPriority.startsWith('C')) return 1;
                    else if (scope.data.businessPriority.startsWith('B')) return 0.5;
                    else return 0;
                };

                var operationalActivityPercentage = function() {
                    if (!scope.data.opActivitiesImpact) return 0;

                    if (scope.data.opActivitiesImpact.startsWith('C')) return 1;
                    else if (scope.data.opActivitiesImpact.startsWith('B')) return 0.5;
                    else return 0;
                };

                scope.xlsxSelected = function(el) {
                    if (scope.onExcelImport) scope.onExcelImport(el);
                    $('#loaderDiv').show();
                    var reader = new FileReader();
                    reader.onload = function(e) {
                        var wb = XLSX.read(e.target.result, {type: "binary"});
                        var ind = wb.SheetNames.indexOf('General summary');
                        if (ind === -1) ind = 1;
                        var sheet = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[ind]], {blankrows: true, header: "A"});
                        if (sheet) sheet.unshift({});
                        processSheet(sheet);
                        $(el).val('');
                        $('#loaderDiv').hide();
                    };
                    reader.onerror = function(e) {
                        $('#loaderDiv').hide();
                        console.log("reading file error:", e);
                    };
                    if (el && el.files[0]) reader.readAsBinaryString(el.files[0]);
                    else {
                        $(el).val('');
                        $('#loaderDiv').hide();
                    }
                };

                var processSheet = function(sheet) {

                    initData();

                    // Strategy Goal
                    if (sheet.length > 10 && sheet[10]['G'] && sheet[10]['G'] !== 'n/a') {
                        if (sheet[10]['G'].startsWith('Revenue')) scope.data.strategicGoal = 'Revenue growth';
                        else if (sheet[10]['G'].startsWith('Cost')) scope.data.strategicGoal = 'Cost optimisation';
                        else if (sheet[10]['G'].startsWith('Business')) scope.data.strategicGoal = 'Business continuity';
                        else if (sheet[10]['G'].startsWith('Regulatory')) scope.data.strategicGoal = 'Regulatory';
                        else if (sheet[10]['G'].startsWith('Regular')) scope.data.strategicGoal = 'Regular expenses';
                    }

                    // Strategy Fit
                    if (sheet.length > 23 && sheet[23]['G'] && sheet[23]['G'] !== 'n/a') {
                        if (sheet[23]['G'].startsWith('A'))
                            scope.data.strategyFit = 'Values through superior network connectivity';
                        else if (sheet[23]['G'].startsWith('B'))
                            scope.data.strategyFit = 'Customer loyalty through convergence';
                        else if (sheet[23]['G'].startsWith('C'))
                            scope.data.strategyFit = 'Competitive operations';
                        else if (sheet[23]['G'].startsWith('D'))
                            scope.data.strategyFit = 'Growth in adjacencies';
                        else
                            scope.data.strategyFit = 'Regular expenses';
                    }

                    if (sheet.length > 24 && sheet[24]['C'] && parseFloat(sheet[24]['C'])) {
                        scope.data.irr = parseFloat(sheet[24]['C']) * 100.0;
                    }

                    // Define impact on operational activities
                    if (sheet.length > 27 && sheet[27]['G'] && sheet[27]['G'] !== 'n/a') {
                        if (sheet[27]['G'][0] === 'A') scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact.options[0];
                        else if (sheet[27]['G'][0] === 'B') scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact.options[1];
                        else scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact.options[2];
                    }

                    // Define business priority
                    if (sheet.length > 29 && sheet[29]['G'] && sheet[29]['G'] !== 'n/a') {
                        if (sheet[29]['G'][0] === 'A') scope.data.businessPriority = scope.selects.businessPriority.options[0];
                        else if (sheet[29]['G'][0] === 'B') scope.data.businessPriority = scope.selects.businessPriority.options[1];
                        else scope.data.businessPriority = scope.selects.businessPriority.options[2];
                    }

                    // TABLE
                    if (sheet.length > 39) {
                        scope.data.firstYear = Math.floor(sheet[37]['AF']);
                        for (var r = 39; r < sheet.length; r++) {

                            if (!sheet[r]['E'] || !sheet[r]['E'].length || sheet[r]['E'] === 'n/a') continue;

                            // CASH FLOW
                            var generalInfo = {
                                rocType: (!sheet[r]['E'] || sheet[r]['E'] === 'n/a') ? null : sheet[r]['E'],
                                rocName: (!sheet[r]['F'] || sheet[r]['F'] === 'n/a') ? null : sheet[r]['F'],
                                wbs: (!sheet[r]['AE'] || sheet[r]['AE'] === 'n/a') ? null : sheet[r]['AE'],
                                cost: (!sheet[r]['O'] || sheet[r]['O'] === 'n/a') ? null : sheet[r]['O'],
                                ktLineNum1: (!sheet[r]['P'] || sheet[r]['P'] === 'n/a') ? null : sheet[r]['P'],
                                ktLineNum2: (!sheet[r]['Q'] || sheet[r]['Q'] === 'n/a') ? null : sheet[r]['Q'],
                                ktLineName: (!sheet[r]['R'] || sheet[r]['R'] === 'n/a') ? null : sheet[r]['R'],
                                incremental: (!sheet[r]['S'] || sheet[r]['S'] === 'n/a') ? null : sheet[r]['S'],
                                purchasing: (!sheet[r]['U'] || sheet[r]['U'] === 'n/a') ? null : sheet[r]['U'],
                                vendorName: (!sheet[r]['V'] || sheet[r]['V'] === 'n/a') ? null : sheet[r]['V'],
                                vendorCode: (!sheet[r]['W'] || sheet[r]['W'] === 'n/a') ? null : sheet[r]['W'],
                                contract: (!sheet[r]['X'] || sheet[r]['X'] === 'n/a') ? null : sheet[r]['X'],
                                truCode: (!sheet[r]['Y'] || sheet[r]['Y'] === 'n/a') ? null : sheet[r]['Y'],
                                neededDate: (!sheet[r]['Z'] || sheet[r]['Z'] === 'n/a') ? null : sheet[r]['Z'],
                                requestDate: (!sheet[r]['AA'] || sheet[r]['AA'] === 'n/a') ? null : sheet[r]['AA'],
                                currency: (!sheet[r]['AB'] || sheet[r]['AB'] === 'n/a') ? null : sheet[r]['AB'],
                                depCode: (!sheet[r]['AC'] || sheet[r]['AC'] === 'n/a') ? null : sheet[r]['AC']
                            };
                            var cashFlowRow = {
                                year: {
                                    1: parseFloat(sheet[r]['AF'])?parseFloat(sheet[r]['AF']):0.0,
                                    2: parseFloat(sheet[r]['AS'])?parseFloat(sheet[r]['AS']):0.0,
                                    3: parseFloat(sheet[r]['AT'])?parseFloat(sheet[r]['AT']):0.0,
                                    4: parseFloat(sheet[r]['AU'])?parseFloat(sheet[r]['AU']):0.0,
                                    5: parseFloat(sheet[r]['AV'])?parseFloat(sheet[r]['AV']):0.0
                                },
                                month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}
                            };
                            for (var c = 71; c < 83; c++) {
                                var cellVal = sheet[r]['A' + String.fromCharCode(c)];
                                cashFlowRow.month[1][scope.months[c - 71]] = parseFloat(cellVal)?parseFloat(cellVal):0.0;
                            }
                            // ACCURALS
                            var accuralsRow = {
                                year: {
                                    1: parseFloat(sheet[r]['BS'])?parseFloat(sheet[r]['BS']):0.0,
                                    2: parseFloat(sheet[r]['CF'])?parseFloat(sheet[r]['CF']):0.0,
                                    3: parseFloat(sheet[r]['CG'])?parseFloat(sheet[r]['CG']):0.0,
                                    4: parseFloat(sheet[r]['CH'])?parseFloat(sheet[r]['CH']):0.0,
                                    5: parseFloat(sheet[r]['CI'])?parseFloat(sheet[r]['CI']):0.0
                                },
                                month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}
                            };
                            for (var c = 84; c < 91; c++) {
                                var cellVal = sheet[r]['B' + String.fromCharCode(c)];
                                accuralsRow.month[1][scope.months[c - 84]] = parseFloat(cellVal)?parseFloat(cellVal):0.0;
                            }
                            for (var c = 65; c < 70; c++) {
                                var cellVal = sheet[r]['C' + String.fromCharCode(c)];
                                accuralsRow.month[1][scope.months[c - 58]] = parseFloat(cellVal)?parseFloat(cellVal):0.0;
                            }
                            if (generalInfo.rocType.toLowerCase().startsWith('revenue')) {
                                scope.data.general.revenues.push(generalInfo);
                                scope.data.cashFlow.revenues.push(cashFlowRow);
                                scope.data.accurals.revenues.push(accuralsRow);
                            } else if (generalInfo.rocType.toLowerCase().startsWith('capex')) {
                                scope.data.general.capexes.push(generalInfo);
                                scope.data.cashFlow.capexes.push(cashFlowRow);
                                scope.data.accurals.capexes.push(accuralsRow);
                            } else if (generalInfo.rocType.toLowerCase().startsWith('other')) {
                                scope.data.general.others.push(generalInfo);
                                scope.data.cashFlow.others.push(cashFlowRow);
                                scope.data.accurals.others.push(accuralsRow);
                            }
                        }
                        scope.onChange();
                        scope.$apply();
                    }
                };

                scope.months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

                scope.collapse = {
                    cashFlow: false,
                    accurals: false
                };

                scope.toggleCollapse = function(name) {
                    scope.collapse[name] = !scope.collapse[name];
                };

                scope.addItem = function(name) {
                    scope.data.general[name].push({});
                    scope.data.cashFlow[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    scope.data.accurals[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    if (!scope.data.general.income.length) {
                        scope.data.general.income.push({});
                        scope.data.cashFlow.income.push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                        scope.data.accurals.income.push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    }
                };
                scope.deleteItem = function(index, name) {
                  exModal.open({
                    templateUrl: './js/partials/confirmModal.html',
                    size: 'sm'
                  }).then(function() {
                    scope.data.general[name].splice(index, 1);
                    scope.data.cashFlow[name].splice(index, 1);
                    scope.data.accurals[name].splice(index, 1);
                    scope.onChange();
                  });
                };

                scope.onChange = function() {
                    $('#loaderDiv').show();
                    for (var table of ['cashFlow', 'accurals']) {
                        for (var name of ['revenues', 'capexes', 'others', 'income']) {
                            if (!scope.data[table][name]) continue;
                            scope.data[table][name+'Total'] = {year:{}, month: {}};
                            for (var i = 1; i < 6; i++) {
                                scope.data[table][name+'Total'].year[i] = 0;
                                scope.data[table][name+'Total'].month[i] = {};
                                if (i === 1) {
                                    for (var m of scope.months)
                                        scope.data[table][name+'Total'].month[i][m] = 0;
                                }
                            }
                            for (var i = 0; i < scope.data[table][name].length; i++) {
                                for (var y = 1; y < 6; y++) {
                                    var monthsTotal = null;
                                    for (var m in scope.data[table][name][i]['month'][y]) {
                                        if (monthsTotal === null) monthsTotal = 0;
                                        monthsTotal += scope.data[table][name][i]['month'][y][m];
                                        scope.data[table][name+'Total'].month[y][m] += scope.data[table][name][i]['month'][y][m];
                                    }
                                    if (monthsTotal != null) scope.data[table][name][i]['year'][y] = monthsTotal;
                                    if (scope.data[table][name][i]['year'][y])
                                        scope.data[table][name+'Total'].year[y] += scope.data[table][name][i]['year'][y];
                                }
                            }
                        }
                    }
                    calcQuantitative();
                };

                var calcQuantitative = function() {

                    // Discount factors
                    var discount = [0];
                    for (var i = 1; i < 6; i++) {
                        discount.push(1.0 / Math.pow(1 + (scope.data.benchmark.wacc / 100.0), i - 1));
                    }

                    // NPV
                    scope.data.npv = 0.0;
                    for (var i = 1; i < 6; i++) {
                        scope.data.npv += (scope.data.cashFlow.revenuesTotal.year[i]
                            + scope.data.cashFlow.capexesTotal.year[i]
                            + scope.data.cashFlow.othersTotal.year[i]
                            + scope.data.cashFlow.incomeTotal.year[i]) * discount[i];
                    }

                    // ROI
                    scope.data.roi = 0.0;
                    if (scope.data.npv > 0.0) {
                        var caps = 0;
                        for (var i = 1; i < 6; i++) {
                            caps += (scope.data.cashFlow.capexesTotal.year[i]
                                + scope.data.cashFlow.othersTotal.year[i]
                                + scope.data.cashFlow.incomeTotal.year[i]) * discount[i];
                        }
                        scope.data.roi = scope.data.npv / Math.abs(caps) * 100.0;
                    }

                    // Payback period
                    scope.data.paybackPeriod = 0.0;
                    var ppsum = 0.0;
                    for (var i = 1; i < 6; i++) {
                        ppsum += (scope.data.cashFlow.revenuesTotal.year[i]
                            + scope.data.cashFlow.othersTotal.year[i]
                            + scope.data.cashFlow.incomeTotal.year[i]
                            + scope.data.cashFlow.capexesTotal.year[i]) * discount[i];
                        if (ppsum > 0) {
                            scope.data.paybackPeriod = (i>1?i:0.0) + 1.0 - (ppsum / (ppsum - (scope.data.cashFlow.capexesTotal.year[i] * discount[i])));
                            break;
                        }
                    }

                    // P&L effect first year
                    scope.data.plEffect = (scope.data.accurals.revenuesTotal.year[1]
                        + scope.data.accurals.capexesTotal.year[1]
                        + scope.data.accurals.othersTotal.year[1]
                        + scope.data.accurals.incomeTotal.year[1]);

                    // CF effect first year
                    scope.data.cfEffect = (scope.data.cashFlow.revenuesTotal.year[1]
                        + scope.data.cashFlow.capexesTotal.year[1]
                        + scope.data.cashFlow.othersTotal.year[1]
                        + scope.data.cashFlow.incomeTotal.year[1]);

                    scope.calcScoring();
                };

                scope.calcScoring = function() {
                    // Strategy fit
                    scope.data.strategyFitScore = strategyFitPercentage() * 50;

                    // Risks / Opportunities / Business priority
                    scope.data.businessPriorityScore = ((businessPriorityPercentage() * 0.7) + (operationalActivityPercentage() * 0.3)) * 50;

                    // Quantitative score
                    scope.data.quantitativeScore = 0.0;
                    if (scope.data.npv > scope.data.benchmark.maxNPV) scope.data.quantitativeScore += 0.2;
                    else if (scope.data.benchmark.maxNPV) scope.data.quantitativeScore += scope.data.npv / scope.data.benchmark.maxNPV * 0.2;

                    if (scope.data.roi > 0.0) {
                        if (scope.data.roi > scope.data.benchmark.maxROI) scope.data.quantitativeScore += 0.2;
                        else if (scope.data.benchmark.maxROI) scope.data.quantitativeScore += scope.data.roi / scope.data.benchmark.maxROI * 0.2;
                    }

                    if (scope.data.paybackPeriod > 0.0) {
                        if (scope.data.paybackPeriod < scope.data.benchmark.minPP) scope.data.quantitativeScore += 0.2;
                        else if (scope.data.benchmark.minPP) scope.data.quantitativeScore += scope.data.benchmark.minPP / scope.data.paybackPeriod * 0.2;
                    }

                    if (scope.data.plEffect > scope.data.benchmark.maxPL) scope.data.quantitativeScore += 0.25;
                    else if (scope.data.benchmark.maxPL) scope.data.quantitativeScore += scope.data.plEffect / scope.data.benchmark.maxPL * 0.25;

                    if (scope.data.cfEffect > scope.data.benchmark.maxCF) scope.data.quantitativeScore += 0.15;
                    else if (scope.data.benchmark.maxCF) scope.data.quantitativeScore += scope.data.cfEffect / scope.data.benchmark.maxCF * 0.15;

                    scope.data.quantitativeScore *= 55;

                    // Qualitative score
                    scope.data.qualitativeScore = (scope.data.strategyFitScore + scope.data.businessPriorityScore) * 0.45;

                    // SCORE
                    scope.data.score = scope.data.qualitativeScore + scope.data.quantitativeScore;
                    $('#loaderDiv').hide();
                };

                scope.getUser = function(val) {
                    scope.data.networkEconomicsId = null;
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike='+encodeURIComponent('%'+val+'%')).then(
                        function(response){
                            var usersByFirstName = _.flatMap(response.data, function(s){
                                if(s.id){
                                    return s.id.split(',').map(function(user){
                                        return {
                                            id: s.id,
                                            email: (s.email?s.email.substring(s.email.lastIndexOf('/')+1):s.email),
                                            firstName: s.firstName,
                                            lastName: s.lastName,
                                            name: s.firstName + ' ' + s.lastName
                                        };
                                    })
                                } else {
                                    return [];
                                }
                            });
                            //return usersByFirstName;
                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike='+encodeURIComponent('%'+val+'%')).then(
                                function(response){
                                    var usersByLastName = _.flatMap(response.data, function(s){
                                        if(s.id){
                                            return s.id.split(',').map(function(user){
                                                return {
                                                    id: s.id,
                                                    email: s.email.substring(s.email.lastIndexOf('/')+1),
                                                    firstName: s.firstName,
                                                    lastName: s.lastName,
                                                    name: s.firstName + ' ' + s.lastName
                                                };
                                            })
                                        } else {
                                            return [];
                                        }
                                    });
                                    return _.unionWith(usersByFirstName, usersByLastName, _.isEqual);
                                }
                            );
                        }
                    );
                    return users;
                };

                scope.userSelected = function(item){
                    scope.data.networkEconomicsId = item.id;
                    scope.data.networkEconomics = item.name;
                };
            },
            templateUrl: './js/directives/demand/businessCase.html'
        };
    });

    module.directive('demandNumberFormat', function ($filter) {
        'use strict';

        return {
            restrict: 'A',
            require: '?ngModel',
            scope: {
                demandNumberFormat: '=',
                formatAddon: '='
            },
            link: function (scope, elem, attrs, ctrl) {
                if (!ctrl) return;

                ctrl.$formatters.unshift(function () {
                    var fractionSize = -1, view = ctrl.$modelValue;
                    if (!isNaN(scope.demandNumberFormat)) fractionSize = scope.demandNumberFormat;
                    if (fractionSize !== -1) view = $filter('number')(ctrl.$modelValue, fractionSize);
                    else  view = $filter('number')(ctrl.$modelValue);
                    if (ctrl.$modelValue === 0.0) view = '0';
                    if (view && scope.formatAddon && scope.formatAddon.length) view += scope.formatAddon;
                    return view;
                });

                ctrl.$parsers.unshift(function (viewValue) {
                    var fractionSize = -1;
                    var plainNumber = viewValue.replace(/,/g, '');
                    if (scope.formatAddon) plainNumber = plainNumber.replace(scope.formatAddon, '');
                    if (!isNaN(scope.demandNumberFormat)) fractionSize = scope.demandNumberFormat;

                    plainNumber = parseFloat(plainNumber);
                    var view = plainNumber;
                    if (fractionSize !== -1) view = $filter('number')(plainNumber, fractionSize);
                    else view = $filter('number')(plainNumber);
                    if (plainNumber === 0.0) view = '0';
                    if (scope.formatAddon && scope.formatAddon.length) view += scope.formatAddon;
                    elem.val(view);

                    return plainNumber;
                });
            }
        };
    });
});
