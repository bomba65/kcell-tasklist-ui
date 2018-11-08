define(['./../module'], function(module){
    'use strict';
    module.directive('demandResources', function ($rootScope, $http, $timeout) {
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

                var setHeight = function() {
                    var element = el[0].querySelector('.resources-container');
                    element.style.height = 'auto';
                    element.style.height = (element.scrollHeight) + 'px';
                };

                $(document).bind('click', function (e) {
                    if (el === e.target || el[0].contains(e.target))
                        $timeout(setHeight);
                });

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];

                        scope.isOpen = [];
                        scope.searchVal = [];
                        for (var i = 0; i < scope.data.length; i++) {
                            scope.isOpen.push(false);
                            scope.searchVal.push('');
                        }

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

                scope.deleteItem = function (index) {
                    scope.data.splice(index, 1);
                    scope.isOpen.splice(index, 1);
                    scope.searchVal.splice(index, 1);
                    scope.countTotalSum();
                    $timeout(setHeight);
                };

                scope.addItem = function () {
                    scope.data.push({
                        department: null,
                        position: null,
                        description: null,
                        quantity: null,
                        hours: null,
                        period: null,
                        existing: null,
                        pprice: null,
                        summ: null,
                        responsible: {
                            name: $rootScope.authentication.name,
                            fio: scope.responsible
                        }
                    });

                    scope.isOpen.push(false);
                    scope.searchVal.push('');
                };

                scope.calcSumm = function (index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].hours) return;
                    if (!scope.data[index].period) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].hours * scope.data[index].period * scope.data[index].pprice;
                    scope.countTotalSum();
                    scope.setResponsible(index);
                };

                scope.setResponsible = function (index) {
                    scope.data[index].responsible = {
                        name: $rootScope.authentication.name,
                        fio: scope.responsible
                    };
                };

                scope.countTotalSum = function () {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onEmplTypeChange = function (index) {
                    scope.data[index].department = null;
                    scope.onDepartmentChange(index);
                };


                scope.onDepartmentChange = function (index) {
                    scope.data[index].position = null;
                    scope.onPositionChange(index);
                };

                scope.onPositionChange = function (index, option) {
                    scope.data[index].position = option;
                    scope.data[index].description = null;
                    scope.setResponsible(index);
                };

                scope.toggleSelect = function(index) {
                    scope.isOpen[index] = !scope.isOpen[index];
                    scope.searchVal[index] = '';
                };

                scope.selectOption = function(index, option) {
                    scope.data[index].position = option;
                    scope.toggleSelect(index);
                    scope.setResponsible(index);
                    scope.onPositionChange(index);
                };

                scope.isOpen = [];
                scope.searchVal = [];

                scope.multiselectSettings = {
                    enableSearch: true,
                    smartButtonMaxItems: 1,
                    selectionLimit: 1,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v'
                };
                scope.multiselectEvents = {
                    onItemSelect: function(item) {
                        for (var i = 0; i < scope.data.length; i++)
                            if (scope.data[i].position.v == item.v)
                                scope.onPositionChange(i);
                    }
                }; scope.catalogs = {
                    "Kcell staff": {
                        "CPD": [
                            "Administrator",
                            "Chancellery Senior Administrator",
                            "ata Input Senior Specialist",
                            "Director",
                            "Expert",
                            "Fleet Management Specialist",
                            "General Warehouse Senior Administrator",
                            "Head",
                            "Intern",
                            "Logistics Senior Specialist",
                            "Manager",
                            "Master Data Senior Administrator",
                            "Monitoring and Optimization Senior Specialist",
                            "Monitoring and Optimization Specialist",
                            "Processes Support Expert",
                            "Senior Administrator",
                            "Senior Specialist",
                            "Sourcing Senior Specialist",
                            "Sourcing Specialist",
                            "Specialist",
                            "Supervisor",
                            "Travel Management Senior Administrator",
                            "Warehouse Administrator",
                            "Warehouse Senior Administrator"
                        ],
                        "CEO": [
                            "Adviser-Consultant",
                            "Assistant",
                            "Corporate Social Responsibility Projects Senior Specialist",
                            "Due Diligence Expert",
                            "Due Diligence Senior Specialist",
                            "Due Diligence Specialist",
                            "Ethics and Compliance Officer",
                            "Expert",
                            "Intern",
                            "Investor Relations Expert",
                            "Manager",
                            "Public Relations Junior Specialist",
                            "Public Relations Senior Specialist",
                            "Senior Specialist",
                            "Chief Executive Officer",
                            "Corporate Secretary",
                            "Expert",
                            "Manager"
                        ],
                        "B2C": [
                            "Activ Marketing Communication Expert",
                            "Analysis Expert",
                            "Analysis Senior Specialist",
                            "Analysis Specialist",
                            "Brand Communication Senior Specialist",
                            "BTL Senior Specialist",
                            "Channel Development Expert",
                            "Channel Development Senior Specialist",
                            "CMS Expert",
                            "Commercial Roaming Expert",
                            "Content Business Development Expert",
                            "Content Marketing Expert",
                            "Content Specialist",
                            "Design and Production Specialist",
                            "Designer",
                            "Expert",
                            "Exploring Expert",
                            "Exploring Senior Specialist",
                            "Forecasting and Planning Senior Specialist",
                            "Head",
                            "Intern",
                            "Junior Specialist",
                            "Kcell Marketing Communication Expert",
                            "Logistic Senior Specialist",
                            "Manager",
                            "Media Senior Specialist",
                            "Merchandising and Direct Delivery Senior Specialist",
                            "Network Partners Expert",
                            "Operational Analysis Senior Specialist",
                            "POSM Senior Administrator",
                            "Product and Сhannels Аnalysis Senior Special",
                            "Product Management Expert",
                            "Reporting Senior Specialist",
                            "Reporting Specialist",
                            "Research Expert",
                            "Rewards Expert",
                            "Roaming Exploitation and Testing Senior Engineer",
                            "Sales Automation Expert",
                            "Senior Engineer",
                            "Senior Specialist",
                            "Social Media and Email Marketing Specialist",
                            "Specialist",
                            "Technical Support Expert",
                            "User Interface Designer"
                        ],
                        "CCD": [
                            "Administrator",
                            "Analysis Senior Specialist",
                            "Analysis Specialist",
                            "Assistant",
                            "Cashier",
                            "Customer Senior Representative",
                            "Dealers Documents Support Specialist",
                            "Director",
                            "Document Support Specialist",
                            "Exclusive Retail Expert",
                            "Expert",
                            "Head",
                            "Information Support Senior Specialist",
                            "Information Support Specialist",
                            "Intern",
                            "Internal Requests Specialist",
                            "Key Account Sales Executive",
                            "Key Account Sales Executive - Acquisition",
                            "Key Account Sales Executive - Retention",
                            "Key Account Senior Sales Executive",
                            "Logistics Specialist",
                            "Manager",
                            "Monitoring Junior Specialist",
                            "Monitoring Specialist",
                            "NPS Expert",
                            "NPS Senior Specialist",
                            "Operator",
                            "Reporting Senior Specialist",
                            "Reporting Specialist",
                            "Sales Senior Representative",
                            "Self Service Senior Specialist",
                            "Self Service Specialist",
                            "Senior Administrator",
                            "Senior Cashier",
                            "Senior Operator",
                            "Senior Representative",
                            "Senior Specialist",
                            "Senior Trainer",
                            "Specialist",
                            "Supervisor",
                            "Support Operator",
                            "Team Leader",
                            "Teleopti  Specialist",
                            "Teleopti Senior Specialist",
                            "Trainer",
                            "Usability Expert",
                            "Warehouse Administrator",
                            "Warehouse Senior Administrator",
                            "Warehouse Specialist"
                        ],
                        "B2B": [
                            "Administrator",
                            "Big Data Expert",
                            "Data Mining Expert",
                            "Digital Expert",
                            "Director",
                            "Expert",
                            "Head",
                            "Head (Souht & East Regions)",
                            "Intern",
                            "Junior Specialist",
                            "Key Account Sales Executive - Acquisition",
                            "Key Account Sales Executive - Retention",
                            "Key Account Senior Sales Executive - Retention",
                            "Loyalty Expert",
                            "Manager",
                            "Products and Solutions Development Expert",
                            "Sales Executive",
                            "Senior Administrator",
                            "Senior Representative",
                            "Senior Specialist",
                            "Specialist",
                            "Strategic Accounts Sales Executive",
                            "Supervisor",
                            "Team Leader",
                            "Tenders and Market Analysis Specialist"
                        ],
                        "FD": [
                            "Accountant",
                            "Advisor",
                            "Architect",
                            "Chief Accountant",
                            "Crisis Coordinator & Business Continuity Expert",
                            "Data Integration and Analysis Senior Engineer",
                            "Debt Control Senior Specialist",
                            "Debt portfolio Senior Specialist",
                            "Director",
                            "Economical Security Senior Specialist",
                            "Electronic Invoice Administrator",
                            "Engineer",
                            "Expert",
                            "Expert Engineer",
                            "Head",
                            "Intern",
                            "Internal Control Expert",
                            "Internal Control Senior Specialist",
                            "Internal Fraud Control Expert",
                            "Internal Fraud Control Senior Specialist",
                            "Internal Fraud Control Specialist",
                            "Junior Accountant",
                            "Manager",
                            "Network Utilities Senior Engineer",
                            "Outgoing Payments Accountant",
                            "Outgoing Payments Expert",
                            "Outgoing Payments Senior Accountant",
                            "Senior Accountant",
                            "Senior Specialist",
                            "Specialist",
                            "Supervisor",
                            "Tax Expert",
                            "Tax Senior Accountant",
                            "Сredit Risk Expert"
                        ],
                        "HR": [
                            "Digital HR Expert",
                            "Director",
                            "Expert",
                            "External Training and Development Senior Specialist",
                            "Fire Safety Senior Specialist",
                            "Head",
                            "HR Business Partner Expert",
                            "HR Business Partner Senior Specialist",
                            "Intern",
                            "Internal Communications Senior Specialist",
                            "Internal Training and Development Expert",
                            "Legal Consultant for labor issues",
                            "Safety and Labor Protection Senior Engineer",
                            "Senior Specialist",
                            "Specialist"
                        ],
                        "LD": [
                            "Director",
                            "Expert",
                            "Government Relations Advisor",
                            "Government Relations Expert",
                            "Government Relations Specialist",
                            "Head",
                            "Lawyer",
                            "Manager",
                            "Senior Lawyer",
                            "Senior Specialist",
                            "Specialist",
                            "Supervisor",
                            "Translation Senior Specialist"
                        ],
                        "TD": [
                            "Active Equipment Based Products Senior Engineer",
                            "Analysis and Reporting Senior Specialist",
                            "Architecture Expert Engineer",
                            "Assistant",
                            "Chief Power Engineer",
                            "Core VAS Operations Engineer",
                            "Core VAS Operations Expert Engineer",
                            "Data Center Engineer",
                            "Data Center Senior Engineer",
                            "Data Integration Expert Engineer",
                            "Departmental Sanitary Expert",
                            "Director",
                            "Electro-Magnetic Field Inspection Senior Engineer",
                            "Electro-Magnetic Field Inspection Senior Specialist",
                            "Engineer",
                            "Ericsson Controllers Dimensioning Senior Engineer",
                            "Expert Engineer",
                            "Fiber Network Planning and Implementation Engineer",
                            "Field Operations Expert Engineer",
                            "Fixed Internet Engineer",
                            "Fixed internet Junior Specialist",
                            "Hardware testing Engineer",
                            "Hardware testing Senior Engineer",
                            "Head",
                            "HW/SW Planning and Implementation Engineer",
                            "Inspection Senior Engineer",
                            "Interconnect Senior Engineer",
                            "Intern",
                            "IP BackBone Expert Engineer",
                            "IT Architecture Expert Engineer",
                            "IT Architecture Senior Engineer",
                            "IT Service Delivery Expert Engineer",
                            "IT Service Delivery Senior Specialist",
                            "IT Support Senior Engineer",
                            "Junior Engineer",
                            "Junior Robotics Automation Engineer",
                            "Junior Specialist",
                            "License Engineer",
                            "Manager",
                            "Metro and IP Transport Network Expert Engineer",
                            "National Operation Acquisition Senior Specialist",
                            "National Operation Acquisition Specialist",
                            "OSS Expert Engineer",
                            "Passive Equipment Based Products Senior Engineer",
                            "Payment Administrator",
                            "Payment Senior Administrator",
                            "Permission Processing Specialist",
                            "Planning Senior Engineer",
                            "Power Engineer",
                            "Power Junior Engineer",
                            "Power Senior Engineer",
                            "Process and Guidelines Preparation Specialist",
                            "Process Automation Engineer",
                            "Process Automation Specialist",
                            "Providers Cooperation Senior Engineer",
                            "Providers Cooperation Senior Specialist",
                            "Quality Control Junior Specialist",
                            "Quality Control Senior Specialist",
                            "Radio and Transmission Equipment Senior Engineer",
                            "Radiofrequency Senior Engineer",
                            "RAN Operations Senior Engineer",
                            "RAN Transmission and IP Transport Senior Engineer",
                            "Roaming Senior Engineer",
                            "Senior Administrator",
                            "Senior Engineer",
                            "Senior Payment Administrator",
                            "Senior Specialist",
                            "Site Leasing Senior Specialist",
                            "Site Senior Engineer",
                            "Site Technician",
                            "Software Development Engineer",
                            "Software Development Expert Engineer",
                            "Software Development Junior Engineer",
                            "Software Development Senior Engineer",
                            "Software Development Senior Engineer, Category II",
                            "Software Development Senior Specialist",
                            "Software Development Specialist",
                            "Solution Intergation Engineer",
                            "Specialist",
                            "Supervisor",
                            "Technician",
                            "Tower Construction Engineer",
                            "Transmission Network Development Expert Engineer",
                            "Transmission Purchasing Control Senior Specialist",
                            "Voice and Data Senior Engineer",
                            "Voice Network Expert Engineer",
                            "ZTE Controllers Dimensioning BSS Senior Engineer"
                        ]
                    },
                    "Outstaff - Individual Entrepreneur": {
                        "CCD": [
                            "Distant Outbound Operator",
                            "Merchandiser",
                            "NPS call back administrator",
                            "Sales Administration Specialist (Consumer)",
                            "Sanitary, Electric&Subsidiary Services Specialist",
                            "Subscription Operator (Activ Operator)"
                        ],
                        "CPD": ["Archive Specialist"],
                        "FD": [
                            "Credit Control  Specialist",
                            "Credit Control Senior Specialist"
                        ],
                        "LD": ["Translator, Administrative Support"],
                        "TD": [
                            "Conditioner Technician",
                            "Electro meters consumption controller"
                        ]
                    },
                    "Outstaff - Outsource": {
                        "B2B": [
                            "B2B Assistant (document specialist)",
                            "B2B Senior Administrator SME",
                            "B2B Senior Representative SME",
                            "B2B Telesales",
                            "Courier"
                        ],
                        "CCD": [
                            "Call Center Operator Transtelecom",
                            "Call Center Operators",
                            "CCD Senior Representative",
                            "CCD Team leader",
                            "Senior representative service (договор обучения)",
                            "Senior Specialist",
                            "Telesales agents"
                        ],
                        "CPD": [
                            "1st category Assistant",
                            "Archive Specialist/Outsource",
                            "Dispatcher car-pool",
                            "Driver",
                            "Driver auto-loader",
                            "Foreman",
                            "Handing worker",
                            "Mechanic",
                            "Medical specialist",
                            "Reception"
                        ],
                        "FD": [
                            "Debt Collection Senior Specialist",
                            "Debt Collection Specialist",
                            "Physical Security Specialist"
                        ],
                        "HR": [
                            "Designer",
                            "HR Admin Specialist"
                        ]
                    },
                    "Outstaff - Phys.Person": {
                        "B2B": ["UX/UI Designer"],
                        "B2C": [
                            "Designer",
                            "Junior Research Specialist"
                        ],
                        "CPD": ["Stickerman-Loader (Regions)"],
                        "FD": ["Archivist"]
                    }
                };
            },
            templateUrl: './js/directives/demand/resources.html'
        };
    });
});