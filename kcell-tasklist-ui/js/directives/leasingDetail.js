define(['./module'], function(module){
	'use strict';
	module.directive('leasingDetail', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				leasingInfo: '='
			},
			link: function(scope, el, attrs){
				// $http.get("/camunda/api/engine/engine/default/user/" + scope.bulksmsInfo.starter + "/profile").then(function (e) {
    //                 scope.bulksmsInfo.starter = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
    //             });
				scope.download = function(path) {
	                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
	                success(function(data, status, headers, config) {
	                    document.getElementById('fileDownloadIframe').src = data;
	                }).
	                error (function(data, status, headers, config) {
	                    console.log(data);
	                });
               	};
			},
			templateUrl: './js/directives/leasingDetail.html'
		};
	}]);
});
