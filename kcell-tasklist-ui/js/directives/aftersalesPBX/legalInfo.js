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
                        if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
                    }
                });

                scope.checkBIN = function() {
                    if (scope.data.BIN && scope.data.BIN.length === 12) {
                        $http({method: 'GET', url: '/camunda/aftersales/pbx/bin/' + scope.data.BIN, transformResponse: [] }).then(
                            function(response) {
                                var result = JSON.parse(response.data);
                                if (result.legalInfo) parseFromPBX(JSON.parse(result.legalInfo));
                                if (result.clientPriority) scope.data.clientPriority = result.clientPriority;
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
                    if (li.termContractEnd) scope.data.termContractEnd = li.termContractEnd;
                }
            },
            templateUrl: './js/directives/aftersalesPBX/legalInfo.html'
        };
    });
});