define(['./../module', 'xlsx'], function(module){
	'use strict';
	module.directive('demandBusinessCase', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
			},
			link: function(scope, element, attrs) {
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.wacc) scope.data.wacc = 13.6;
						if (!scope.data.cashFlow) {
							scope.data.cashFlow = {
								revenues: [],
								opexes: [],
								capexes: []
							};
						}
						if (!scope.data.accurals) {
							scope.data.accurals = {
								revenues: [],
								opexes: [],
								capexes: []
							};
						}
						if (!scope.data.firstYear) scope.data.firstYear = (new Date()).getFullYear() + 1;
					}
				});

				scope.xlsxSelected = function(el) {
					$('#loaderDiv').show();
					var reader = new FileReader();
					reader.onload = function(e) {
						var wb = XLSX.read(e.target.result, {type: "binary"});
						var sheet = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[1]], {blankrows: true, header: "A"});
						if (sheet) sheet.unshift({});
						processSheet(sheet);
						$('#loaderDiv').hide();
					};
					reader.onerror = function(e) {
						$('#loaderDiv').hide();
						console.log("reading file error:", e);
					};
					if (el && el.files[0]) reader.readAsBinaryString(el.files[0]);
					else $('#loaderDiv').hide();
				};

				var processSheet = function(sheet) {
					//

					// TABLE
					if (sheet.length > 39) {
						scope.data.firstYear = Math.floor(sheet[37]['AK']);

						scope.data.cashFlow = { revenues: [], opexes: [], capexes: [] };
						scope.data.accurals = { revenues: [], opexes: [], capexes: [] };
						for (var r = 39; r < sheet.length; r++) {
							if (sheet[r]['AH'].toLowerCase().startsWith('income')) break;
							if (!sheet[r]['AG']) continue;
							// CASH FLOW
							var cashFlowRow = {
								rocName: (!sheet[r]['AH'] || sheet[r]['AH'] == 'n/a') ? null : sheet[r]['AH'],
								wbs: (!sheet[r]['AJ'] || sheet[r]['AJ'] == 'n/a') ? null : sheet[r]['AJ'],
								cost: (!sheet[r]['N'] || sheet[r]['N'] == 'n/a') ? null : sheet[r]['N'],
								purchasing: (!sheet[r]['V'] || sheet[r]['V'] == 'n/a') ? null : sheet[r]['V'],
								contract: (!sheet[r]['Y'] || sheet[r]['Y'] == 'n/a') ? null : sheet[r]['Y'],
								startDate: (!sheet[r]['AA'] || sheet[r]['AA'] == 'n/a') ? null : sheet[r]['AA'],
								endDate: (!sheet[r]['AB'] || sheet[r]['AB'] == 'n/a') ? null : sheet[r]['AB'],
								amount: (!sheet[r]['AD'] || sheet[r]['AD'] == 'n/a') ? null : sheet[r]['AD'],
								depCode: (!sheet[r]['AI'] || sheet[r]['AI'] == 'n/a') ? null : sheet[r]['AI'],
								year: {
									1: sheet[r]['AK'],
									2: sheet[r]['AX'],
									3: sheet[r]['BK'],
									4: sheet[r]['BL'],
									5: sheet[r]['BM']
								},
								month: {1: {}, 2: {
									'Jan': sheet[r]['AY'],
									'Feb': sheet[r]['AZ']
								}, 3: {}, 4: {}, 5: {}}
							};
							for (var c = 76; c < 88; c++) {
								cashFlowRow.month[1][scope.months[c - 76]] = sheet[r]['A' + String.fromCharCode(c)];
							}
							for (var c = 65; c < 75; c++) {
								cashFlowRow.month[2][scope.months[c - 63]] = sheet[r]['B' + String.fromCharCode(c)];
							}
							// ACCURALS
							var accuralsRow = {
								lineName: sheet[r]['AH'],
								year: {
									1: (!parseFloat(sheet[r]['CW'])?sheet[r]['BO']:sheet[r]['CW']),
									2: (!parseFloat(sheet[r]['DJ'])?sheet[r]['BP']:sheet[r]['DJ']),
									3: (!parseFloat(sheet[r]['DW'])?sheet[r]['BQ']:sheet[r]['DW']),
									4: (!parseFloat(sheet[r]['DX'])?sheet[r]['BR']:sheet[r]['DX']),
									5: (!parseFloat(sheet[r]['DY'])?sheet[r]['BS']:sheet[r]['DY'])
								},
								month: {1: {
									'Jan': (!parseFloat(sheet[r]['CX'])?sheet[r]['BW']:sheet[r]['CX']),
									'Feb': (!parseFloat(sheet[r]['CY'])?sheet[r]['BX']:sheet[r]['CY']),
									'Mar': (!parseFloat(sheet[r]['CZ'])?sheet[r]['BY']:sheet[r]['CZ']),
									'Apr': (!parseFloat(sheet[r]['DA'])?sheet[r]['BZ']:sheet[r]['DA'])
								}, 2: {}, 3: {}, 4: {}, 5: {}}
							};
							for (var c = 66; c < 74; c++) {
								var val = sheet[r]['D' + String.fromCharCode(c)];
								accuralsRow.month[1][scope.months[c - 62]] = !parseFloat(val)?sheet[r]['C' + String.fromCharCode(c - 1)]:val;
							}
							for (var c = 75; c < 87; c++) {
								var val = sheet[r]['D' + String.fromCharCode(c)];
								accuralsRow.month[2][scope.months[c - 75]] = !parseFloat(val)?sheet[r]['C' + String.fromCharCode(c - 1)]:val;
							}
							if (sheet[r]['AG'].toLowerCase().startsWith('revenue')) {
								scope.data.cashFlow.revenues.push(cashFlowRow);
								scope.data.accurals.revenues.push(accuralsRow);
							} else if (sheet[r]['AG'].toLowerCase().startsWith('opex')) {
								scope.data.cashFlow.opexes.push(cashFlowRow);
								scope.data.accurals.opexes.push(accuralsRow);
							} else if (sheet[r]['AG'].toLowerCase().startsWith('capex')) {
								scope.data.cashFlow.capexes.push(cashFlowRow);
								scope.data.accurals.capexes.push(accuralsRow);
							}
						}
						scope.onChange();
						calcQuantitative();
						scope.$apply();
					}
				};
				
				scope.months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

				scope.collapse = {
					cashFlow1: false,
					cashFlow2: false,
					accurals1: false,
					accurals2: false
				};

				scope.toggleCollapse = function(name) {
					scope.collapse[name] = !scope.collapse[name];
				};

				scope.addItem = function(name) {
					scope.data.cashFlow[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
					scope.data.accurals[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
				};
				scope.deleteItem = function(index, name) {
					scope.data.cashFlow[name].splice(index, 1);
					scope.data.accurals[name].splice(index, 1);
				};

				scope.onChange = function() {
					for (var table of ['cashFlow', 'accurals']) {
						for (var name of ['revenues', 'capexes', 'opexes']) {
							scope.data[table][name+'Year'] = {1: 0, 2: 0, 3: 0, 4: 0, 5: 0};
							for (var i = 0; i < scope.data[table][name].length; i++) {
								for (var y = 1; y < 6; y++) {
									var months = null;
									for (var m in scope.data[table][name][i]['month'][y]) {
										if (months == null) months = 0;
										months += scope.data[table][name][i]['month'][y][m];
									}
									if (months != null) scope.data[table][name][i]['year'][y] = months;
									if (scope.data[table][name][i]['year'][y])
										scope.data[table][name+'Year'][y] += scope.data[table][name][i]['year'][y];
								}
							}
							for (var y = 1; y < 6; y++) 
								if (!scope.data[table][name+'Year'][y])
									scope.data[table][name+'Year'][y] = null;
						}
					}
					calcQuantitative();
				};

				var calcQuantitative = function() {
					// NPV
					scope.data.npv = 0.0;
					for (var i = 1; i < 6; i++) {
						var discount = 1.0 / Math.pow(1 + (scope.data.wacc / 100.0), i - 1);
						scope.data.npv += (scope.data.cashFlow.revenuesYear[i]
										- scope.data.cashFlow.capexesYear[i]
										- scope.data.cashFlow.opexesYear[i]) * discount;
					}

					// ROI
					scope.data.roi = 0.0;
					if (scope.data.npv > 0.0) {
						var caps = 0;
						for (var i = 1; i < 6; i++) {
							var discount = 1 / Math.pow(1 + (scope.data.wacc / 100.0), i - 1);
							caps += scope.data.cashFlow.capexesYear[i]
						}
						scope.data.roi = scope.data.npv / caps;
					}

					// Payback period
					scope.data.paybackPeriod = 0.0;
					var ppsum = 0.0;
					for (var i = 1; i < 6; i++) {
						var discount = 1 / Math.pow(1 + (scope.data.wacc / 100.0), i - 1);
						var total = (scope.data.cashFlow.revenuesYear[i]
										- scope.data.cashFlow.capexesYear[i]
										- scope.data.cashFlow.opexesYear[i]);
						ppsum += (total * discount);
						if (ppsum >= 0.0) {
							scope.data.paybackPeriod = 1.0 - ((total * discount) / ((total - scope.data.cashFlow.capexesYear[i]) * discount));
							break;
						}
					}

					// P&L effect first year
					scope.data.plEffect = (scope.data.accurals.revenuesYear[1]
										- scope.data.accurals.capexesYear[1]
										- scope.data.accurals.opexesYear[1]);
					
					// CF effect first year
					scope.data.cfEffect = (scope.data.cashFlow.revenuesYear[1]
										- scope.data.cashFlow.capexesYear[1]
										- scope.data.cashFlow.opexesYear[1]);

					scope.calcScoring();
				};

				scope.calcScoring = function() {
					scope.data.score = 0.0;

					// Strategy fit
					scope.data.strategyFitScore = 0.0;
					if (scope.data.strategyFit) {
						scope.data.strategyFitScore = 0.225;
						if (scope.data.strategyFit == 'Indirect impact')
							scope.data.strategyFitScore = 0.18;
					}
					scope.data.score += scope.data.strategyFitScore;

					// Risks / Opportunities / Business priority
					var bp = 0, oa = 0;
					if (scope.data.businessPriority) {
						if (scope.data.businessPriority == 'Impacts') bp = 11.0;
						else if (scope.data.businessPriority == 'would stop') bp = 23.0;
					}
					if (scope.data.opActivitiesImpact) {
						if (scope.data.opActivitiesImpact == 'Impacts') oa = 11.0;
						else if (scope.data.opActivitiesImpact == 'significantly hinder') oa = 23.0;
					}
					scope.data.businessPriorityScore = bp + oa;
					scope.data.score += scope.data.businessPriorityScore;

					// Quantitative score
					scope.data.quantitativeScore = 0.0;
					if (scope.data.npv > scope.data.maxNPV) scope.data.quantitativeScore += 11.0;
					else if (scope.data.maxNPV) scope.data.quantitativeScore += (scope.data.npv / scope.data.maxNPV) * 11.0;

					if (scope.data.roi > scope.data.maxROI) scope.data.quantitativeScore += 11.0;
					else if (scope.data.maxNPV) scope.data.quantitativeScore += (scope.data.roi / scope.data.maxROI) * 11.0;

					if (scope.data.paybackPeriod < scope.data.minPP) scope.data.quantitativeScore += 11.0;
					else if (scope.data.minPP) scope.data.quantitativeScore += (scope.data.minPP / scope.data.paybackPeriod) * 11.0;

					if (scope.data.plEffect > scope.data.maxPL) scope.data.quantitativeScore += 14.0;
					else if (scope.data.maxPL) scope.data.quantitativeScore += (scope.data.plEffect / scope.data.maxPL) * 14.0;

					if (scope.data.cfEffect > scope.data.maxCF) scope.data.quantitativeScore += 8.0;
					else if (scope.data.maxCF) scope.data.quantitativeScore += (scope.data.cfEffect / scope.data.maxCF) * 8.0;

					// Qualitative score
					scope.data.qualitativeScore = scope.data.strategyFitScore + scope.data.businessPriorityScore;
					scope.data.score += scope.data.qualitativeScore;

					// CF effect first year
					scope.data.cfEffectScore = scope.data.cfEffect;
					scope.data.score += scope.data.cfEffectScore;
				};
	        },
			templateUrl: './js/directives/demand/businessCase.html'
		};
	});
});