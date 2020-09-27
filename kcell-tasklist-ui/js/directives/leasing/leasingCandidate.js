define(['./../module'], function(module){
	'use strict';
	module.directive('leasingCandidate', ['$http', '$timeout', function ($http, $timeout) {
		return {
			require: '^form',
			restrict: 'E',
			scope: {
				leasingCandidate: '=',
				farEnd: '='
			},
			link: function(scope, el, attrs, formCtrl){
				//console.log(formCtrl, 'formCtrl');
				scope.parentForm = formCtrl;
				scope.dictionary = {};
				scope.selectedIndex = -1;
				scope.defaultFarEndCard = false;
				scope.loadCurrentFarEnd = true;
                $http.get('/api/leasingCatalogs').then(
                    function(result){
                        angular.extend(scope.dictionary, result.data);
                        scope.dictionary.legalTypeTitle = _.keyBy(scope.dictionary.legalType, 'id');
						scope.dictionary.antennasList = scope.dictionary.antennas;
						scope.dictionary.antennaType = scope.dictionary.antennaType;
						scope.addressesList = scope.dictionary.addresses;
                        scope.oblastList = _.uniqBy(scope.dictionary.addresses, 'oblast').map( (e, index) => { return {"name" : e.oblast, "id" : index} });
                        scope.currentFarEnd.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
                        scope.districtList = _.uniqBy(scope.dictionary.addresses, 'district').map( (e, index) => { return {"name" : e.district, "id" : index} });
                        scope.filteredDistricts = scope.districtList;
                        scope.filteredDistrictsInCAI = scope.districtList;
                        scope.alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');
						console.log("scope:");
						console.log(scope);
						
						scope.leasingCandidate.addressString = '';
						scope.leasingCandidate.cellAntenna.addressString = '';
						//tabs
						scope.minTab = 0;
						scope.maxTab = 3;
						scope.leasingCandidate.cellAntenna.sectors[0].active = 'active';
						scope.selectedSectorTab = 0;
						scope.leasingCandidate.checkTPSTouched = false;
						scope.leasingCandidate.checkTPSBTouched = false;
						Object.values(scope.leasingCandidate.address).forEach((s,index) => {
							scope.leasingCandidate.addressString += index > 0 ? ', ' + s : s
						});
						console.log(`scope.dictionary:`);
						console.log(scope.dictionary);
						console.log('----------------------------------------');
						console.log(`scope.leasingCandidate.cellAntenna.address:`);
						console.log(scope.leasingCandidate.cellAntenna.address);
						console.log('----------------------------------------');
						Object.values(scope.leasingCandidate.cellAntenna.address).forEach((s,index) => {
							scope.leasingCandidate.cellAntenna.addressString += index > 0 ? ', ' + s : s
						});
						console.log(`asdasdsa: ${scope.leasingCandidate.cellAntenna.addressString} 12`);	
                    },
                    function(error){
                        console.log(error);
                    }
				);
				
				scope.$watch('leasingCandidate.farEndInformation', function(farEndInformation) {
					if (farEndInformation && farEndInformation.length>0 && scope.loadCurrentFarEnd) {
						angular.forEach(farEndInformation, function (fe, i) {
							if (fe.surveyDate){
								fe.surveyDate = new Date(fe.surveyDate);
							}
						});
						scope.currentFarEnd = farEndInformation[0];
						scope.currentFarEnd.priority = true;
						scope.loadCurrentFarEnd = false;
						scope.defaultFarEndCard = true;
					}
				});
				
				scope.addressToString = function(address) {
					let string  = '';
					if (address) {
						string += `${address.cn_addr_oblast ? address.cn_addr_oblast + ', ': ''}`;
						string += `${address.cn_addr_district ? address.cn_addr_district + ', ': ''}`;
						string += `${address.cn_addr_city ? address.cn_addr_city + ', ': ''}`;
						string += `${address.cn_addr_street ? address.cn_addr_street + ', ': ''}`;
						string += `${address.cn_addr_building ? address.cn_addr_building + ', ': ''}`;
						string += `${address.cn_addr_cadastral_number ? address.cn_addr_cadastral_number + ', ': ''}`;
						string += `${address.cn_addr_note ? address.cn_addr_note + ', ': ''}`;
						//cn_addr_cadastral_number cn_addr_note
						// Object.values(address).forEach((a,index) => {
						// 	string += index > 0 ? ', ' + a : a
						// })
						// console.log(`string: ${string}`)
					}
					return string;
				};


				scope.tabStepRight = function () {
					if (scope.selectedSectorTab+1 < scope.leasingCandidate.cellAntenna.sectors.length) {
						scope.selectedSectorTab = scope.selectedSectorTab + 1;
						scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
	
						if (scope.selectedSectorTab === scope.maxTab) {
							scope.minTab= scope.minTab + 1; 
							scope.maxTab= scope.maxTab + 1;
						}
					} else {
						scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active'; 
					}
				}
	
				scope.tabStepLeft = function () {
					if (scope.selectedSectorTab-1 >= 0) {
						scope.selectedSectorTab = scope.selectedSectorTab - 1;
						scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
	
						if (scope.selectedSectorTab+1 === scope.minTab) {
							scope.minTab= scope.minTab - 1; 
							scope.maxTab= scope.maxTab - 1;
						}
					} else if (scope.selectedSectorTab-1 < 0){
						scope.leasingCandidate.cellAntenna.sectors[0].active = 'active'; 
					} else {
						scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active'; 
					}
				}
				scope.checkTPS = function(val) {
					if (val==='TPS installation required (TPS is missing)') {
						// scope.leasingCandidate.powerSource.cn_tps_belongs = '';
						// scope.leasingCandidate.powerSource.cn_tps_belongs_commentary = '';
						// scope.leasingCandidate.powerSource.cn_tps_distance = '';
						scope.leasingCandidate.checkTPSTouched = false;
					}
					else {
						scope.leasingCandidate.checkTPSTouched = true;
					}
				};
	
	
				scope.checkTPSB = function(val) {
					if (val==='Other') {
						scope.leasingCandidate.checkTPSBTouched = true;
					}
					else {
						// scope.leasingCandidate.powerSource.cn_tps_belongs_commentary = '';
						scope.leasingCandidate.checkTPSBTouched = false;
					}
				};

				scope.selectAntennaSector = function (index) {
					scope.selectedSectorTab = index;
				}

				scope.openFarEndInformation = function(index) {
					scope.currentFarEnd = scope.leasingCandidate.farEndInformation[index];
					if (scope.currentFarEnd.surveyDate){
						scope.currentFarEnd.surveyDate = new Date(scope.currentFarEnd.surveyDate);
					}
					scope.defaultFarEndCard = true;
				};
				scope.checkCableLayingType  = function($index, val) {
					if (val) scope.leasingCandidate.cableLayingTypeChecks +=1;
					else scope.leasingCandidate.cableLayingTypeChecks -=1;
					scope.leasingCandidate.checkCableLayingTypeTouched = true;
				};
				scope.sortableOptions = {
					disabled:true,
					'ui-preserve-size': true,
					stop: function(e, ui) {
						scope.leasingCandidate.farEndInformation[0].priority = true;
						angular.forEach(scope.leasingCandidate.farEndInformation, function (fe, i) {
							if(i !== 0) fe.priority = false;
						});
					}
				};
				scope.$watch('leasingCandidate.checkAndApproveFETaskResult', function(resolution) {
					if (resolution==='approved' || resolution==='rejected') {
						scope.sortableOptions.disabled = true;
					} else if (resolution=='priorityChange') scope.sortableOptions.disabled = false;
				});
				scope.$watch('leasingCandidate.transmissionAntenna.antennaType', function (antennaType) {
					scope.leasingCandidate.frequenciesByAntennaType = {};
					scope.leasingCandidate.frequenciesByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
						return p.name === antennaType;
					});
				}, true);
				scope.$watch('currentFarEnd.feAntennaType', function (antennaType) {
					scope.leasingCandidate.frequenciesFeByAntennaType = {};
					scope.leasingCandidate.frequenciesFeByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
						return p.name === antennaType;
					})
				}, true);
				scope.download = function(path) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
	                then(function(response) {
	                    document.getElementById('fileDownloadIframe').src = response.data;
	                }, function(error){
	                    console.log(error.data);
	                });
               	};
				scope.defineByAntennaType = function (antennaType, type) {
					if(antennaType){
						if (type==='transmission') {
							angular.forEach(scope.dictionary.antennaType, function (type) {
								if(type.name === antennaType){
									scope.leasingCandidate.transmissionAntenna.diameter = type.diameter;
									scope.leasingCandidate.transmissionAntenna.weight = type.weight;
								}
							});
						} else {
							angular.forEach(scope.dictionary.antennaType, function (type) {
								if(type.name === antennaType){
									scope.currentFarEnd.diameter = type.diameter;
									scope.currentFarEnd.weight = type.weight;
								}
							});

						}
					} else {
						if (type==='transmission') {
							scope.leasingCandidate.transmissionAntenna.diameter = undefined;
							scope.leasingCandidate.transmissionAntenna.weight = undefined;
						} else {
							scope.currentFarEnd.diameter = undefined;
							scope.currentFarEnd.weight = undefined;
						}
					}
				};

				scope.isError = function(block) {
					try {
						var error = false;
						if($scope.kcell_form){
							var keys = Object.keys($scope.kcell_form);
							console.log(keys)
							var fields = [];
							if(block === 'candidateInformation') {
								fields = ['siteName', 'latitude', 'longitude', 'constructionType', 'transmissionType', 'square', 'rbsLocation', 'radio', 'createNewCandidateSiteTaskComment', 'leasingCandidate', 'cn_addr_oblast', 'cn_addr_district', 'cn_addr_city', 'cn_addr_street', 'cn_addr_building', 'cn_addr_cadastral_number', 'cn_addr_note', 'cai_addr_oblast', 'cai_addr_district', 'cai_addr_street', 'cai_addr_building', 'cai_addr_cadastral_number', 'cai_addr_note', 'antennaName', 'quantity_', 'dimensions_', 'frequencyBand_', 'weight_', 'azimuth_', 'suspensionHeight_', 'check_', 'antennaLocation', 'cn_gsm900', 'cn_lte800', 'cn_dcs1800', 'cn_ret_lte800', 'cn_wcdma2100', 'cn_lte1800', 'cn_umts900', 'cn_ret_lte1800', 'cn_trx', 'cn_lte2100', 'cn_wcdma_carrier', 'cn_ret_lte2100', 'cn_gsm_antenna_quantity', 'cn_lte_antenna_quantity', 'cn_tilt_mech_gsm', 'cn_tilt_mech_lte', 'cn_tilt_electr_gsm', 'cn_tilt_electr_lte', 'cn_direction_gsm', 'cn_direction_lte', 'cn_height_gsm', 'cn_height_lte', 'cn_duplex_gsm', 'cn_diversity', 'cn_power_splitter', 'cn_hcu', 'cn_asc', 'cn_ret', 'cn_tma_gsm', 'cn_tcc', 'cn_gsm_range', 'selectedAntennas', 'antennasQuantity', 'address', 'legalType', 'RClegalName', 'legalAddress', 'telFax', 'firstLeaderName', 'firstLeaderPos', 'email', 'contactName', 'position', 'contractInfo', 'antennaType', 'antennaQuantity', 'frequencyBand', 'TRSuspensionHeight', 'TRazimuth', 'provideUs3Phase', 'cableLength', 'cableLayingType', 'agreeToReceiveMonthlyPayment', 'closestPublic04', 'closestPublic10', 'cn_tps', 'cn_tps_belongs', 'cn_tps_belongs_commentary', 'cn_tps_distance'];
							} else if(block === 'address') {
								fields = ['cn_addr_oblast' ,'cn_addr_district' ,'cn_addr_city' ,'cn_addr_street' ,'cn_addr_building' ,'cn_addr_cadastral_number' ,'cn_addr_note'];
							} else if(block === 'cellAntennaInformation'){
								fields = ['antennaLocation', 'antennaNameInSector_', 'azimuth_', 'cai_addr_building', 'cai_addr_cadastral_number', 'cai_addr_district', 'cai_addr_note', 'cai_addr_oblast', 'cai_addr_street', 'check_', 'cai_addr_city', 'cn_asc', 'cn_dcs1800_', 'cn_direction_gsm_', 'cn_direction_lte_', 'cn_diversity_', 'cn_duplex_gsm_', 'cn_gsm900_', 'cn_gsm_antenna_quantity_', 'cn_gsm_range_', 'cn_hcu_', 'cn_height_gsm_', 'cn_height_lte_', 'cn_lte1800_', 'cn_lte2100_', 'cn_lte800_', 'cn_lte_antenna_quantity_', 'cn_power_splitter_', 'cn_ret_', 'cn_ret_lte1800_', 'cn_ret_lte2100_', 'cn_ret_lte800_', 'cn_tcc_', 'cn_tilt_electr_gsm_', 'cn_tilt_electr_lte_', 'cn_tilt_mech_gsm_', 'cn_tilt_mech_lte_', 'cn_tma_gsm_', 'cn_trx_', 'cn_umts900_', 'cn_wcdma2100_', 'cn_wcdma_carrier_', 'dimensions_', 'dUnit', 'frequencyBand_', 'quantity_', 'radioUnit_', 'suspensionHeight_', 'weight_'];
							} else if(block === 'renterCompany') {
								fields = ['legalType', 'RClegalName', 'legalAddress', 'telFax', 'firstLeaderName', 'firstLeaderPos', 'email', 'contactName', 'position', 'contractInfo'];
							} else if(block === 'nearEndInformation') {
								fields = ['antennaType', 'antennaQuantity', 'frequencyBand', 'TRSuspensionHeight', 'TRazimuth', 'nei_addr_oblast', 'nei_addr_district', 'nei_addr_city', 'nei_addr_street', 'nei_addr_building', 'nei_addr_cadastral_number', 'nei_addr_note'];
							} else if(block === 'powerSource') {
								fields = ['agreeToReceiveMonthlyPayment', 'cableLength', 'closestPublic04', 'closestPublic10', 'cn_tps', 'cn_tps_belongs', 'cn_tps_belongs_commentary', 'cn_tps_distance', 'provideUs3Phase'];
							} else if(block === 'farEndInformation') {
								fields = ['farEndName', 'threeFarEndNotNecessaryCheck', 'threeFarEndNotNecessaryCheckComment', 'fe_form_contractInformation', 'fe_form_equipmentType', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_cn_addr_note', 'fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_n_addr_city', 'fe_form_farEndAddress', 'fe_form_farEndLegalType', 'fe_form_FeAntennasQuantity', 'fe_form_feAntennaType', 'fe_form_FeAzimuth', 'fe_form_FeComments', 'fe_form_FeConstructionType', 'fe_form_FeDiameter', 'fe_form_FeFrequencyBand', 'fe_form_FERC_ContractInformation', 'fe_form_FERC_Name', 'fe_form_FERC_Position', 'fe_form_FERCemail', 'fe_form_FERCFirstLeaderName', 'fe_form_FERCFirstLeaderPosition', 'fe_form_FERCLegalAddress', 'fe_form_FERClegalName', 'fe_form_FERCTelFax', 'fe_form_FeSquare', 'fe_form_FeSuspensionHeight', 'fe_form_FeWeight', 'fe_form_ResultsOfVisit', 'fe_form_surveyDate'];
							} else if(block === 'farEndInformationAccordion') {
								fields = ['fe_form_contractInformation', 'fe_form_equipmentType', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_cn_addr_note', 'fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_n_addr_city', 'fe_form_farEndAddress', 'fe_form_farEndLegalType', 'fe_form_FeAntennasQuantity', 'fe_form_feAntennaType', 'fe_form_FeAzimuth', 'fe_form_FeComments', 'fe_form_FeConstructionType', 'fe_form_FeDiameter', 'fe_form_FeFrequencyBand', 'fe_form_FERC_ContractInformation', 'fe_form_FERC_Name', 'fe_form_FERC_Position', 'fe_form_FERCemail', 'fe_form_FERCFirstLeaderName', 'fe_form_FERCFirstLeaderPosition', 'fe_form_FERCLegalAddress', 'fe_form_FERClegalName', 'fe_form_FERCTelFax', 'fe_form_FeSquare', 'fe_form_FeSuspensionHeight', 'fe_form_FeWeight', 'fe_form_ResultsOfVisit', 'fe_form_surveyDate'];
							} else if(block === 'farEndAddress') {
								fields = ['fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_n_addr_city', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_note'];
							}
							keys.forEach(function (key) {
								if (!key.startsWith('$')) {
									fields.forEach(function (field) {
										if(block === 'nearEndInformation'){
											if (key === field) {
												if ($scope.kcell_form[key].$error) {
													error = error || ($scope.kcell_form[key].$error.required && ($scope.kcell_form[key].$touched || $scope.view.submitted));
												}
											}
										} else {
											if (key.startsWith(field)) {
												if ($scope.kcell_form[key].$error) {
													error = error || ($scope.kcell_form[key].$error.required && ($scope.kcell_form[key].$touched || $scope.view.submitted));
												}
											}
										}
									});
								}
							});
						}
						return error;
					} catch (e) {
						console.log(e);
					}
					return false;
				}

               	scope.selectIndex = function(index){
	                if(scope.selectedIndex == index){
	                    scope.selectedIndex = undefined;
	                } else {
	                    scope.selectedIndex = index;
	                }               		
               	}
				scope.checkAzimuth = function(val, index) {
               		if (index>0) {
						if (!(val>=0 && val <=360)) scope.leasingCandidate.cellAntenna.selectedAntennas[index].azimuth = '';
					} else {
						if (!(val>=0 && val <=360)) scope.currentFarEnd.azimuth = '';
					}
				};

				scope.filterDistrictAfterSelectObl = function (cn_addr_oblast) {
					scope.currentFarEnd.address.cn_addr_oblast = cn_addr_oblast
					scope.currentFarEnd.address.cn_addr_district = ''
					scope.currentFarEnd.address.cn_addr_city = ''
					if(scope.currentFarEnd.address.cn_addr_oblast && scope.currentFarEnd.address.cn_addr_oblast !== ''){
						scope.currentFarEnd.filteredByOblast = scope.dictionary.addresses.filter(a => {return a.oblast === scope.currentFarEnd.address.cn_addr_oblast});
						scope.currentFarEnd.filteredDistricts = _.uniqBy(scope.currentFarEnd.filteredByOblast, 'district').map( (e, index) => { return {"name" : e.district, "id" : index} });
						scope.currentFarEnd.cityList = _.uniqBy(scope.currentFarEnd.filteredByOblast, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
					} else {
						scope.currentFarEnd.filteredDistricts = scope.districtList;
						scope.currentFarEnd.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
					}
				};

				scope.addressCitySelected = function ($item) {
					scope.currentFarEnd.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
					if (!scope.currentFarEnd.address.cn_addr_oblast) {
						scope.currentFarEnd.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
					}
				};

				scope.getCity = function (val) {
					if (val.length < 2) {
						return []
					}
					return _.filter(scope.currentFarEnd.cityList, function(o) { return o.name.toLowerCase().includes(val.toLowerCase()); })
				};
			},
			templateUrl: './js/directives/leasing/leasingCandidate.html'
		};
	}]);
});
