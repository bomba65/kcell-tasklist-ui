define(['./../module'], function(module){
    'use strict';
    module.directive('fixedInternetLegalInfo', function ($rootScope, $http, toasty, $q) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
            },
            link: function(scope, element, attrs) {
                scope.range = function(min, max, step) {
                    step = step || 1;
                    var input = [];
                    for (var i = min; i <= max; i += step) {
                        input.push(i);
                    }
                    return input;
                };

                scope.$watch('data.BIN', function(clientBIN){
                    if (clientBIN && clientBIN.length === 12) {
                        $http({method: 'GET', url: '/camunda/crm/client/bin/'+clientBIN, transformResponse: [] }).then(function(response) {
                            var clientCRM = JSON.parse(response.data);
                            if (clientCRM.city && clientCRM.city.nameRu) {
                                scope.data.companyCity = clientCRM.city.nameRu;
                            }
                            if (clientCRM.accountName) {
                                scope.data.comp_name = clientCRM.accountName;
                                if(clientCRM.salesExecutiveUser && clientCRM.salesExecutiveUser.username) {

                                    scope.userSelected({id: scope.data.salesReprId.replace('[','').replace(']','').split('|')[0]});
                                    $http.get("/camunda/api/engine/engine/default/user/" + scope.data.salesReprId.toLowerCase() + "/profile").then(function (userResponse) {
                                        scope.userSelected({
                                            id: scope.data.salesReprId.toLowerCase(),
                                            name: userResponse.data.firstName + ' ' + userResponse.data.lastName
                                        });
                                    });
                                }
                            }
                        });
                    }
                }, true);

            },
            templateUrl: './js/directives/fixedInternet/legalInfo.html'
        };
    });
});