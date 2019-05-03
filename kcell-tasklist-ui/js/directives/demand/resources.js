define(['./../module'], function(module){
    'use strict';
    module.directive('demandResources', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                showprice: '='
            },
            link: function(scope, el, attrs) {

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        if (!scope.collapsed || !(scope.collapsed instanceof Array)) scope.collapsed = [];

                        scope.countTotalSum();

                        if (!scope.responsible) {
                            scope.responsible = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name + "/profile").then(function (result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.responsible = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                scope.toggleCollapse = function(el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.collapsed[index] = !scope.collapsed[index];
                };

                scope.deleteItem = function (index) {
                  exModal.open({
                    templateUrl: './js/partials/confirmModal.html',
                    size: 'sm'
                  }).then(function() {
                    scope.data.splice(index, 1);
                    scope.collapsed.splice(index, 1);
                    scope.countTotalSum();
                  });
                };

                scope.addItem = function () {
                    scope.data.push({
                        department: null,
                        role: null,
                        description: null,
                        quantity: null,
                        hours: null,
                        period: null,
                        existing: null,
                        pprice: null,
                        summ: null,
                        responsible: null
                    });
                    scope.collapsed.push(false);
                };

                scope.calcSumm = function (index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].hours) return;
                    if (!scope.data[index].period) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].hours * scope.data[index].period * scope.data[index].pprice;
                    scope.countTotalSum();
                };

                scope.setResponsible = function (index) {
                    scope.data[index].responsible = {
                        id: $rootScope.authentication.name,
                        fio: scope.responsible
                    };
                    scope.calcSumm(index);
                };

                scope.countTotalSum = function () {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onEmployeeTypeChange = function (index) {
                    scope.data[index].department = null;
                    scope.onDepartmentChange(index);
                };

                scope.onDepartmentChange = function (index) {
                    scope.data[index].role = null;
                    scope.onRoleChange(index);
                };

                scope.onRoleChange = function (index, option) {
                    scope.data[index].role = option;
                    scope.data[index].description = null;
                    if (option && option.description) scope.data[index].description = option.description;
                    if (option && option.expert) setExpertGroupName(index, option.expert);
                };

                var setExpertGroupName = function (index, expert) {
                    if (!expert || !expert.groupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + expert.groupId).then(
                      function(result) {
                          if (result.data) scope.data[index].role.expert.groupName = result.data.name;
                      },
                      function(error) { toasty.error(error.data); }
                    );
                };

                scope.catalogs = {
                    "Kcell staff": {
                        "CPD": [
                          {v: "Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Chancellery Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "ata Input Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Director", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Expert", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Fleet Management Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "General Warehouse Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Head", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Intern", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Logistics Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Manager", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Master Data Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Monitoring and Optimization Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Monitoring and Optimization Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Processes Support Expert", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Sourcing Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Sourcing Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Specialist", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Supervisor", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Travel Management Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Warehouse Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "Warehouse Senior Administrator", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true},
                          {v: "New role", expert: {groupId: 'DEMAND_KCELL_CPD'}, needCheck: true}
                        ],
                        "CEO": [
                          {v: "Adviser-Consultant", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Assistant", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Corporate Social Responsibility Projects Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Due Diligence Expert", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Due Diligence Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Due Diligence Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Ethics and Compliance Officer", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Expert", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Intern", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Investor Relations Expert", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Manager", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Public Relations Junior Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Public Relations Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Chief Executive Officer", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "Corporate Secretary", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true},
                          {v: "New role", expert: {groupId: 'DEMAND_KCELL_CEO'}, needCheck: true}
                        ],
                        "B2C": [
                          {v: "Activ Marketing Communication Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Analysis Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Analysis Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Analysis Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Brand Communication Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "BTL Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Channel Development Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Channel Development Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "CMS Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Commercial Roaming Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Content Business Development Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Content Marketing Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Content Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Design and Production Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Designer", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Exploring Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Exploring Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Forecasting and Planning Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "Intern", expert: {groupId: ''}, needCheck: true},
                          {v: "Junior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Kcell Marketing Communication Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Logistic Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Manager", expert: {groupId: ''}, needCheck: true},
                          {v: "Media Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Merchandising and Direct Delivery Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Network Partners Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Operational Analysis Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "POSM Senior Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Product and Сhannels Аnalysis Senior Special", expert: {groupId: ''}, needCheck: true},
                          {v: "Product Management Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Reporting Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Reporting Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Research Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Rewards Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Roaming Exploitation and Testing Senior Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Sales Automation Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Social Media and Email Marketing Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Technical Support Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "User Interface Designer", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "CCD": [
                          {v: "Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Analysis Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Analysis Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Assistant", expert: {groupId: ''}, needCheck: true},
                          {v: "Cashier", expert: {groupId: ''}, needCheck: true},
                          {v: "Customer Senior Representative", expert: {groupId: ''}, needCheck: true},
                          {v: "Dealers Documents Support Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Director", expert: {groupId: ''}, needCheck: true},
                          {v: "Document Support Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Exclusive Retail Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "Information Support Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Information Support Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Intern", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Requests Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Sales Executive", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Sales Executive - Acquisition", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Sales Executive - Retention", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Senior Sales Executive", expert: {groupId: ''}, needCheck: true},
                          {v: "Logistics Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Manager", expert: {groupId: ''}, needCheck: true},
                          {v: "Monitoring Junior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Monitoring Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "NPS Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "NPS Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Operator", expert: {groupId: ''}, needCheck: true},
                          {v: "Reporting Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Reporting Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Sales Senior Representative", expert: {groupId: ''}, needCheck: true},
                          {v: "Self Service Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Self Service Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Cashier", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Operator", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Representative", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Trainer", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Supervisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Support Operator", expert: {groupId: ''}, needCheck: true},
                          {v: "Team Leader", expert: {groupId: ''}, needCheck: true},
                          {v: "Teleopti  Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Teleopti Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Trainer", expert: {groupId: ''}, needCheck: true},
                          {v: "Usability Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Warehouse Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Warehouse Senior Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Warehouse Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "B2B": [
                          {v: "Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Big Data Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Data Mining Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Digital Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Director", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "Head (Souht & East Regions)", expert: {groupId: ''}, needCheck: true},
                          {v: "Intern", expert: {groupId: ''}, needCheck: true},
                          {v: "Junior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Sales Executive - Acquisition", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Sales Executive - Retention", expert: {groupId: ''}, needCheck: true},
                          {v: "Key Account Senior Sales Executive - Retention", expert: {groupId: ''}, needCheck: true},
                          {v: "Loyalty Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Manager", expert: {groupId: ''}, needCheck: true},
                          {v: "Products and Solutions Development Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Sales Executive", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Representative", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Strategic Accounts Sales Executive", expert: {groupId: ''}, needCheck: true},
                          {v: "Supervisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Team Leader", expert: {groupId: ''}, needCheck: true},
                          {v: "Tenders and Market Analysis Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "FD": [
                          {v: "Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Advisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Architect", expert: {groupId: ''}, needCheck: true},
                          {v: "Chief Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Crisis Coordinator & Business Continuity Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Data Integration and Analysis Senior Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Debt Control Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Debt portfolio Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Director", expert: {groupId: ''}, needCheck: true},
                          {v: "Economical Security Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Electronic Invoice Administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "Intern", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Control Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Control Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Fraud Control Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Fraud Control Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Fraud Control Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Junior Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Manager", expert: {groupId: ''}, needCheck: true},
                          {v: "Network Utilities Senior Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Outgoing Payments Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Outgoing Payments Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Outgoing Payments Senior Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Supervisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Tax Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Tax Senior Accountant", expert: {groupId: ''}, needCheck: true},
                          {v: "Сredit Risk Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "HR": [
                          {v: "Digital HR Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Director", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "External Training and Development Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Fire Safety Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "HR Business Partner Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "HR Business Partner Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Intern", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Communications Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Internal Training and Development Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Legal Consultant for labor issues", expert: {groupId: ''}, needCheck: true},
                          {v: "Safety and Labor Protection Senior Engineer", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "LD": [
                          {v: "Director", expert: {groupId: ''}, needCheck: true},
                          {v: "Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Government Relations Advisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Government Relations Expert", expert: {groupId: ''}, needCheck: true},
                          {v: "Government Relations Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Head", expert: {groupId: ''}, needCheck: true},
                          {v: "Lawyer", expert: {groupId: ''}, needCheck: true},
                          {v: "Manager", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Lawyer", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Supervisor", expert: {groupId: ''}, needCheck: true},
                          {v: "Translation Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "TD": [
                          {v: 'Amdocs', expert: {groupId: ''}, needCheck:true, description: 'Configuration of Amdocs billing products - TCF etc.'},
                          {v: 'Android', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Apex', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Application support', expert: {groupId: ''}, needCheck:true, description: 'Consultation for a long of  project. After finish of project take application for support.'},
                          {v: 'Avaya', expert: {groupId: ''}, needCheck:true, description: 'Avaya expertise'},
                          {v: 'Billing expert', expert: {groupId: ''}, needCheck:true, description: 'Billing experts consultation, RFI, etc'},
                          {v: 'Core network expert', expert: {groupId: ''}, needCheck:true, description: 'Core/Radio network expert consultation'},
                          {v: 'Enterprise IT', expert: {groupId: ''}, needCheck:true, description: 'EIT expert consultation for a long of  project.'},
                          {v: 'Frontend', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'HW expert', expert: {groupId: ''}, needCheck:true, description: 'HW expert consultation'},
                          {v: 'HW support', expert: {groupId: ''}, needCheck:true, description: 'Consultation for a long of  project. After finish of project take HW for support.'},
                          {v: 'iOs', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Java Backend', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Java Frontend', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Legal', expert: {groupId: ''}, needCheck:true, description: 'Legal experts consultaion'},
                          {v: 'Middleware', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Network Expert', expert: {groupId: ''}, needCheck:true, description: 'Network expert consultation'},
                          {v: 'NOC Monitoring', expert: {groupId: ''}, needCheck:true, description: 'Consultation for a long of  project. After finish of project configure monitoring of product.'},
                          {v: 'ORGA', expert: {groupId: ''}, needCheck:true, description: 'Configuration of ORGA billing products - TCF etc.'},
                          {v: 'OS support', expert: {groupId: ''}, needCheck:true, description: 'Consultation for a long of  project. After finish of project take OS for support.'},
                          {v: 'Platform Engineer', expert: {groupId: ''}, needCheck:true, description: 'Technical platform expertise, preparation technical description'},
                          {v: 'Procurement', expert: {groupId: ''}, needCheck:true, description: 'Procurement process'},
                          {v: 'Product Manager', expert: {groupId: ''}, needCheck:true, description: 'Product (Solution) Manager'},
                          {v: 'Project Manager', expert: {groupId: ''}, needCheck:true, description: 'Project (Requirement) Manager'},
                          {v: 'Reporting', expert: {groupId: ''}, needCheck:true, description: 'DB/Reporting expert consultation'},
                          {v: 'Security', expert: {groupId: ''}, needCheck:true, description: 'Security expert consultation'},
                          {v: 'SharePoint', expert: {groupId: ''}, needCheck:true, description: 'Sharepoint form development'},
                          {v: 'Software Architect', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'TAS', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'VAS Platform', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'DevOps Outstaff', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Outsource', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Product Manager Outstaff', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: 'Software Development  Outstaff', expert: {groupId: ''}, needCheck:true, description: 'Software development'},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ]
                    },
                    "Outstaff - Individual Entrepreneur": {
                        "CCD": [
                          {v: "Distant Outbound Operator", expert: {groupId: ''}, needCheck: true},
                          {v: "Merchandiser", expert: {groupId: ''}, needCheck: true},
                          {v: "NPS call back administrator", expert: {groupId: ''}, needCheck: true},
                          {v: "Sales Administration Specialist (Consumer)", expert: {groupId: ''}, needCheck: true},
                          {v: "Sanitary, Electric&Subsidiary Services Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Subscription Operator (Activ Operator)", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "CPD": [
                          {v: "Archive Specialist",  expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "FD": [
                          {v: "Credit Control  Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Credit Control Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "LD": [
                          {v: "Translator, Administrative Support", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "TD": [
                          {v: "Conditioner Technician", expert: {groupId: ''}, needCheck: true},
                          {v: "Electro meters consumption controller", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ]
                    },
                    "Outstaff - Outsource": {
                        "B2B": [
                          {v: "B2B Assistant (document specialist)", expert: {groupId: ''}, needCheck: true},
                          {v: "B2B Senior Administrator SME", expert: {groupId: ''}, needCheck: true},
                          {v: "B2B Senior Representative SME", expert: {groupId: ''}, needCheck: true},
                          {v: "B2B Telesales", expert: {groupId: ''}, needCheck: true},
                          {v: "Courier", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "CCD": [
                          {v: "Call Center Operator Transtelecom", expert: {groupId: ''}, needCheck: true},
                          {v: "Call Center Operators", expert: {groupId: ''}, needCheck: true},
                          {v: "CCD Senior Representative", expert: {groupId: ''}, needCheck: true},
                          {v: "CCD Team leader", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior representative service (договор обучения)", expert: {groupId: ''}, needCheck: true},
                          {v: "Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Telesales agents", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "CPD": [
                          {v: "1st category Assistant", expert: {groupId: ''}, needCheck: true},
                          {v: "Archive Specialist/Outsource", expert: {groupId: ''}, needCheck: true},
                          {v: "Dispatcher car-pool", expert: {groupId: ''}, needCheck: true},
                          {v: "Driver", expert: {groupId: ''}, needCheck: true},
                          {v: "Driver auto-loader", expert: {groupId: ''}, needCheck: true},
                          {v: "Foreman", expert: {groupId: ''}, needCheck: true},
                          {v: "Handing worker", expert: {groupId: ''}, needCheck: true},
                          {v: "Mechanic", expert: {groupId: ''}, needCheck: true},
                          {v: "Medical specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Reception", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "FD": [
                          {v: "Debt Collection Senior Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Debt Collection Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "Physical Security Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "HR": [
                          {v: "Designer", expert: {groupId: ''}, needCheck: true},
                          {v: "HR Admin Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ]
                    },
                    "Outstaff - Phys.Person": {
                        "B2B": [
                          {v: "UX/UI Designer", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "B2C": [
                          {v: "Designer", expert: {groupId: ''}, needCheck: true},
                          {v: "Junior Research Specialist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "CPD": [
                          {v: "Stickerman-Loader (Regions)", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ],
                        "FD": [
                          {v: "Archivist", expert: {groupId: ''}, needCheck: true},
                          {v: "New role", expert: {groupId: ''}, needCheck: true}
                        ]
                    }
                };
            },
            templateUrl: './js/directives/demand/resources.html'
        };
    });
});