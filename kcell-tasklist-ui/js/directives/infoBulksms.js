define(['./module'], function(module){
	'use strict';
	module.directive('infoBulksms', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				bulksmsInfo: '='
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
			templateUrl: './js/directives/infoBulksms.html'
		};
	}]);
});
