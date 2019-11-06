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
                        scope.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
                        scope.districtList = _.uniqBy(scope.dictionary.addresses, 'district').map( (e, index) => { return {"name" : e.district, "id" : index} });
                        scope.filteredDistricts = scope.districtList;
                        scope.filteredDistrictsInCAI = scope.districtList;
						
						scope.leasingCandidate.addressString = '';
						scope.leasingCandidate.cellAntenna.addressString = '';
						//tabs
						scope.minTab = 0;
						scope.maxTab = 3;
						scope.leasingCandidate.cellAntenna.sectors[0].active = 'active';
						scope.selectedSectorTab = 0;

						Object.values(scope.leasingCandidate.address).forEach((s,index) => {
							scope.leasingCandidate.addressString += index > 0 ? ', ' + s : s
						});

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
						Object.values(address).forEach((a,index) => {
							string += index > 0 ? ', ' + a : a
						})
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
						scope.filteredDistricts = scope.districtList;
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
