define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesLegalInformation', function ($rootScope, $http, toasty) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.companyDate) scope.data.companyDate = new Date();
                        else scope.data.companyDate = new Date(scope.data.companyDate);
                        if (!scope.data.termContract) scope.data.termContract = new Date();
                        else scope.data.termContract = new Date(scope.data.termContract);
                    }
                });

                scope.checkBIN = function() {
                    if (scope.data.BIN && scope.data.BIN.length === 12) {
                        $http({method: 'GET', url: '/camunda/crm/client/bin/' + scope.data.BIN, transformResponse: [] }).success(function(data) {
                            var result = JSON.parse(data);

                            if (result.status != 200) {
                                toasty.error(result.message);
                                return;
                            } else {}

                            if (Object.keys(clientCRM).length > 0) {
                                if (clientCRM.accountName) {
                                    $scope.ci.legalName = clientCRM.accountName;

                                    if(clientCRM.salesExecutiveUser && clientCRM.salesExecutiveUser.username) {
                                        //var camundaUserID = clientCRM.salesExecutiveUser.username.split('.').map(val=>val.charAt(0).toUpperCase() + val.substr(1).toLowerCase()).join('.').concat('@kcell.kz');
                                        var camundaUserID = clientCRM.salesExecutiveUser.email.replace('[','').replace(']','').split('|')[0];
                                        $http.get("/camunda/api/engine/engine/default/user/" + camundaUserID + "/profile").then(function(response){
                                            //console.log('profile', response.data);
                                            $scope.ci.salesRepresentative = response.data.firstName + ' ' + response.data.lastName;
                                        }).catch(function(error){
                                            console.log('error',error);
                                            console.log(camundaUserID, clientCRM.salesExecutiveUser.username, clientCRM.salesExecutiveUser.email);
                                            $scope.ci.salesRepresentative = clientCRM.salesExecutiveUser.username;
                                        });
                                    }

                                    if (clientCRM.city && clientCRM.city.nameEn) {
                                        $scope.ci.companyRegistrationCity = clientCRM.city.nameEn;
                                    }

                                    if (clientCRM.dicSectorEconomics && clientCRM.dicSectorEconomics.name) {
                                        $scope.ci.mainTypeActivityCustomer = clientCRM.dicSectorEconomics.name;
                                    }

                                    $scope.ci.foundClientCRM = true;
                                }
                            }

                        }).error (function(data) {
                            toasty.error(data);
                        });
                    } else {
                        toasty.error('BIN must have length 12!');
                    }
                };
            },
            templateUrl: './js/directives/aftersalesPBX/legalInfo.html'
        };
    });
});