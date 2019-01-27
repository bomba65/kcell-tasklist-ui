define(['./../module'], function(module){
    'use strict';
    module.directive('aftersalesLegalInformation', function ($rootScope, $http, toasty) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
                pbxData: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.companyDate) scope.data.companyDate = new Date();
                        else scope.data.companyDate = new Date(scope.data.companyDate);
                        if (!scope.data.termContract) scope.data.termContract = new Date();
                        else scope.data.termContract = new Date(scope.data.termContract);
                        if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.checkBIN = function() {
                    if (scope.data.BIN && scope.data.BIN.length === 12) {
                        $http({method: 'GET', url: '/camunda/aftersales/pbx/bin/' + scope.data.BIN, transformResponse: [] }).then(
                            function(response) {
                                var result = JSON.parse(response.data);
                                if (result.clientPriority) scope.data.clientPriority = result.clientPriority;
                                if (!result.aftersales && result.legalInfo) parseFromPBX(JSON.parse(result.legalInfo));
                                $rootScope.$broadcast('aftersalesPBXBINCheck', result);
                            },
                            function(response) { toasty.error(response.data);});
                    } else toasty.error('BIN must have length 12!');
                };

                function parseFromPBX(li) {
                    if (!li) return;
                    if (li.legalName) scope.data.legalName = li.legalName;
                    if (li.companyRegistrationCity) scope.data.companyCity = li.companyRegistrationCity;
                    if (li.purchaseType) scope.data.purchaseType = li.purchaseType;
                    if (li.salesRepresentative) scope.data.salesRepr = li.salesRepresentative;
                    if (li.salesRepresentativeId) scope.data.salesReprId = li.salesRepresentativeId;
                    if (li.legalAddress) scope.data.legalAddress = li.legalAddress;
                    if (li.mailingAddress) scope.data.mailingAddress = li.mailingAddress;
                    if (li.email) scope.data.email = li.email;
                    if (li.companyRegistrationDate) scope.data.companyDate = new Date(li.companyRegistrationDate);
                    if (li.accountant) scope.data.accountant = li.accountant;
                    if (li.accountantNumber) scope.data.accountantNumber = li.accountantNumber;
                    if (li.ticName) scope.data.ticName = li.ticName;
                    if (li.mainTypeActivityCustomer) scope.data.mainType = li.mainTypeActivityCustomer;
                    if (li.providerName) scope.data.providerName = li.providerName;
                    if (li.providerBin) scope.data.providerBIN = li.providerBin;
                    if (li.vatCertificate) scope.data.vatCert = li.vatCertificate;
                    if (li.iban) scope.data.iban = li.iban;
                    if (li.bankName) scope.data.bankName = li.bankName;
                    if (li.swift) scope.data.swift = li.swift;
                    if (li.kbe) scope.data.kbe = li.kbe;
                    if (li.termContract) scope.data.termContract = new Date(li.termContract);
                    scope.data.termContractEnd = li.termContractEnd;
                    if (scope.data.termContractEnd === null || scope.data.termContractEnd === undefined) scope.data.termContractEnd = false;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    if (scope.pbxData.companyDate) scope.pbxData.companyDate = new Date(scope.pbxData.companyDate);
                    if (scope.pbxData.termContract) scope.pbxData.termContract = new Date(scope.pbxData.termContract);
                    scope.pbxData.fetched = true;
                }


              scope.getUser = function(val) {
                scope.data.salesReprId = null;
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

              scope.userSelected = function($item){
                scope.data.salesReprId = $item.id;
                scope.data.salesRepr = $item.name;
              };
            },
            templateUrl: './js/directives/aftersalesPBX/legalInfo.html'
        };
    });
});