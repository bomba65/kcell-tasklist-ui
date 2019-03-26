define(['./../module'], function(module){
	'use strict';
	module.directive('demandTargetAudience', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
			},
			link: function(scope, element, attrs) {
        scope.audienceHidden = null;
        scope.roumingHidden = null;
				scope.multiselectSettings = {
					enableSearch: true,
					showCheckAll: false,
					showUncheckAll: false,
					displayProp: 'v',
					idProp: 'v',
					externalIdProp: 'v'
				};
				scope.audienceOptions = [
					{v: "All"},
					{v: "B2B"},
					{v: "B2C"},
					{v: "CCD"},
					{v: "CEO"},
					{v: "CPD"},
					{v: "FD"},
					{v: "HR"},
					{v: "LD"},
					{v: "TD"},
					{v: "NB"}
				];
				scope.roumingOptions = [
					{v:'All'},
					{v:'Astana and Akmola region'},
					{v:'Almaty and Almaty region'},
					{v:'Taraz and Zhambyl region'},
					{v:'Kyzylorda and Kyzylorda region'},
					{v:'Shymkent and Turkestan region'},
					{v:'Oskemen and East-Kazakhstan region'},
					{v:'Kostanai and Kostanai region'},
					{v:'Pavlodar and Pavlodar region'},
					{v:'Petropavlovsk and North-Kazakhstan region'},
					{v:'Aktau and Mangystau region'},
					{v:'Aktobe and Aktobe region'},
					{v:'Uralsk and West-Kazakhstan region'},
					{v:'Karagandy and Karagangdy region'},
					{v:'Atyrau and Atyrau region'}
				];
				scope.$watch('data', function(value) {
					if (value) {
						if (!scope.data) scope.data = {};
						if (!scope.data.audience) scope.data.audience = [];
						if (!scope.data.rouming) scope.data.rouming = [];
            scope.audienceHidden = 'something';
            if (!scope.data.audience.length) scope.audienceHidden = null;
            scope.roumingHidden = 'something';
            if (!scope.data.rouming.length) scope.roumingHidden = null;
					}
				}, true);
	    },
			templateUrl: './js/directives/demand/targetAudience.html'
		};
	});
});