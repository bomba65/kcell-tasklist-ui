define(['./../module'], function(module){
	'use strict';
	module.directive('demandResources', function ($rootScope, $http) {
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
			link: function(scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];

                        scope.isOpen = [];
                        for (var i = 0; i < scope.data.length; i++) {
                            scope.isOpen.push(false);
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
                    scope.countTotalSum();
                };

                scope.addItem = function () {
                    scope.data.push({
                        department: null,
                        position: {},
                        description: null,
                        quantity: null,
                        labor: null,
                        rate: null,
                        existing: null,
                        pprice: null,
                        summ: null,
                        responsible: {
                            name: $rootScope.authentication.name,
                            fio: scope.responsible
                        }
                    });

                    scope.isOpen.push(false);
                };

                scope.calcSumm = function (index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].labor) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].labor * scope.data[index].pprice;
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
                    scope.data[index].position = {};
                    scope.onPositionChange(index);
                };

                scope.onPositionChange = function (index) {
                    scope.data[index].description = null;
                    scope.setResponsible(index);
                };

                scope.toggleSelect = function(index) {
                    scope.isOpen[index] = !scope.isOpen[index];
                };

                scope.selectOption = function(index, option) {
                    scope.data[index].purchaseGroup = option;
                    scope.toggleSelect(index);
                    scope.setResponsible(index);
                };

                scope.isOpen = [];

                scope.multiselectSettings = {
                    enableSearch: true,
                    smartButtonMaxItems: 1,
                    selectionLimit: 1,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v',
                    showAllSelectedText: true
                };
                scope.multiselectEvents = {
                    onItemSelect: function(item) {
                        for (var i = 0; i < scope.data.length; i++)
                            if (scope.data[i].position.v == item.v)
                                scope.onPositionChange(i);
                    }
                };

                scope.catalogs = {
                    "Kcell staff": {
                        "CPD": [
                            {v: "Administrator"},
                            {v: "Chancellery Senior Administrator"},
                            {v: "ata Input Senior Specialist"},
                            {v: "Director"},
                            {v: "Expert"},
                            {v: "Fleet Management Specialist"},
                            {v: "General Warehouse Senior Administrator"},
                            {v: "Head"},
                            {v: "Intern"},
                            {v: "Logistics Senior Specialist"},
                            {v: "Manager"},
                            {v: "Master Data Senior Administrator"},
                            {v: "Monitoring and Optimization Senior Specialist"},
                            {v: "Monitoring and Optimization Specialist"},
                            {v: "Processes Support Expert"},
                            {v: "Senior Administrator"},
                            {v: "Senior Specialist"},
                            {v: "Sourcing Senior Specialist"},
                            {v: "Sourcing Specialist"},
                            {v: "Specialist"},
                            {v: "Supervisor"},
                            {v: "Travel Management Senior Administrator"},
                            {v: "Warehouse Administrator"},
                            {v: "Warehouse Senior Administrator"}
                        ],
                        "CEO": [
                            {v: "Adviser-Consultant"},
                            {v: "Assistant"},
                            {v: "Corporate Social Responsibility Projects Senior Specialist"},
                            {v: "Due Diligence Expert"},
                            {v: "Due Diligence Senior Specialist"},
                            {v: "Due Diligence Specialist"},
                            {v: "Ethics and Compliance Officer"},
                            {v: "Expert"},
                            {v: "Intern"},
                            {v: "Investor Relations Expert"},
                            {v: "Manager"},
                            {v: "Public Relations Junior Specialist"},
                            {v: "Public Relations Senior Specialist"},
                            {v: "Senior Specialist"},
                            {v: "Chief Executive Officer"},
                            {v: "Corporate Secretary"},
                            {v: "Expert"},
                            {v: "Manager"}
                        ],
                        "B2C": [
                            {v: "Activ Marketing Communication Expert"},
                            {v: "Analysis Expert"},
                            {v: "Analysis Senior Specialist"},
                            {v: "Analysis Specialist"},
                            {v: "Brand Communication Senior Specialist"},
                            {v: "BTL Senior Specialist"},
                            {v: "Channel Development Expert"},
                            {v: "Channel Development Senior Specialist"},
                            {v: "CMS Expert"},
                            {v: "Commercial Roaming Expert"},
                            {v: "Content Business Development Expert"},
                            {v: "Content Marketing Expert"},
                            {v: "Content Specialist"},
                            {v: "Design and Production Specialist"},
                            {v: "Designer"},
                            {v: "Expert"},
                            {v: "Exploring Expert"},
                            {v: "Exploring Senior Specialist"},
                            {v: "Forecasting and Planning Senior Specialist"},
                            {v: "Head"},
                            {v: "Intern"},
                            {v: "Junior Specialist"},
                            {v: "Kcell Marketing Communication Expert"},
                            {v: "Logistic Senior Specialist"},
                            {v: "Manager"},
                            {v: "Media Senior Specialist"},
                            {v: "Merchandising and Direct Delivery Senior Specialist"},
                            {v: "Network Partners Expert"},
                            {v: "Operational Analysis Senior Specialist"},
                            {v: "POSM Senior Administrator"},
                            {v: "Product and Сhannels Аnalysis Senior Special"},
                            {v: "Product Management Expert"},
                            {v: "Reporting Senior Specialist"},
                            {v: "Reporting Specialist"},
                            {v: "Research Expert"},
                            {v: "Rewards Expert"},
                            {v: "Roaming Exploitation and Testing Senior Engineer"},
                            {v: "Sales Automation Expert"},
                            {v: "Senior Engineer"},
                            {v: "Senior Specialist"},
                            {v: "Social Media and Email Marketing Specialist"},
                            {v: "Specialist"},
                            {v: "Technical Support Expert"},
                            {v: "User Interface Designer"}
                        ],
                        "CCD": [
                            {v: "Administrator"},
                            {v: "Analysis Senior Specialist"},
                            {v: "Analysis Specialist"},
                            {v: "Assistant"},
                            {v: "Cashier"},
                            {v: "Customer Senior Representative"},
                            {v: "Dealers Documents Support Specialist"},
                            {v: "Director"},
                            {v: "Document Support Specialist"},
                            {v: "Exclusive Retail Expert"},
                            {v: "Expert"},
                            {v: "Head"},
                            {v: "Information Support Senior Specialist"},
                            {v: "Information Support Specialist"},
                            {v: "Intern"},
                            {v: "Internal Requests Specialist"},
                            {v: "Key Account Sales Executive"},
                            {v: "Key Account Sales Executive - Acquisition"},
                            {v: "Key Account Sales Executive - Retention"},
                            {v: "Key Account Senior Sales Executive"},
                            {v: "Logistics Specialist"},
                            {v: "Manager"},
                            {v: "Monitoring Junior Specialist"},
                            {v: "Monitoring Specialist"},
                            {v: "NPS Expert"},
                            {v: "NPS Senior Specialist"},
                            {v: "Operator"},
                            {v: "Reporting Senior Specialist"},
                            {v: "Reporting Specialist"},
                            {v: "Sales Senior Representative"},
                            {v: "Self Service Senior Specialist"},
                            {v: "Self Service Specialist"},
                            {v: "Senior Administrator"},
                            {v: "Senior Cashier"},
                            {v: "Senior Operator"},
                            {v: "Senior Representative"},
                            {v: "Senior Specialist"},
                            {v: "Senior Trainer"},
                            {v: "Specialist"},
                            {v: "Supervisor"},
                            {v: "Support Operator"},
                            {v: "Team Leader"},
                            {v: "Teleopti  Specialist"},
                            {v: "Teleopti Senior Specialist"},
                            {v: "Trainer"},
                            {v: "Usability Expert"},
                            {v: "Warehouse Administrator"},
                            {v: "Warehouse Senior Administrator"},
                            {v: "Warehouse Specialist"}
                        ],
                        "B2B": [
                            {v: "Administrator"},
                            {v: "Big Data Expert"},
                            {v: "Data Mining Expert"},
                            {v: "Digital Expert"},
                            {v: "Director"},
                            {v: "Expert"},
                            {v: "Head"},
                            {v: "Head (Souht & East Regions)"},
                            {v: "Intern"},
                            {v: "Junior Specialist"},
                            {v: "Key Account Sales Executive - Acquisition"},
                            {v: "Key Account Sales Executive - Retention"},
                            {v: "Key Account Senior Sales Executive - Retention"},
                            {v: "Loyalty Expert"},
                            {v: "Manager"},
                            {v: "Products and Solutions Development Expert"},
                            {v: "Sales Executive"},
                            {v: "Senior Administrator"},
                            {v: "Senior Representative"},
                            {v: "Senior Specialist"},
                            {v: "Specialist"},
                            {v: "Strategic Accounts Sales Executive"},
                            {v: "Supervisor"},
                            {v: "Team Leader"},
                            {v: "Tenders and Market Analysis Specialist"}
                        ],
                        "FD": [
                            {v: "Accountant"},
                            {v: "Advisor"},
                            {v: "Architect"},
                            {v: "Chief Accountant"},
                            {v: "Crisis Coordinator & Business Continuity Expert"},
                            {v: "Data Integration and Analysis Senior Engineer"},
                            {v: "Debt Control Senior Specialist"},
                            {v: "Debt portfolio Senior Specialist"},
                            {v: "Director"},
                            {v: "Economical Security Senior Specialist"},
                            {v: "Electronic Invoice Administrator"},
                            {v: "Engineer"},
                            {v: "Expert"},
                            {v: "Expert Engineer"},
                            {v: "Head"},
                            {v: "Intern"},
                            {v: "Internal Control Expert"},
                            {v: "Internal Control Senior Specialist"},
                            {v: "Internal Fraud Control Expert"},
                            {v: "Internal Fraud Control Senior Specialist"},
                            {v: "Internal Fraud Control Specialist"},
                            {v: "Junior Accountant"},
                            {v: "Manager"},
                            {v: "Network Utilities Senior Engineer"},
                            {v: "Outgoing Payments Accountant"},
                            {v: "Outgoing Payments Expert"},
                            {v: "Outgoing Payments Senior Accountant"},
                            {v: "Senior Accountant"},
                            {v: "Senior Specialist"},
                            {v: "Specialist"},
                            {v: "Supervisor"},
                            {v: "Tax Expert"},
                            {v: "Tax Senior Accountant"},
                            {v: "Сredit Risk Expert"}
                        ],
                        "HR": [
                            {v: "Digital HR Expert"},
                            {v: "Director"},
                            {v: "Expert"},
                            {v: "External Training and Development Senior Specialist"},
                            {v: "Fire Safety Senior Specialist"},
                            {v: "Head"},
                            {v: "HR Business Partner Expert"},
                            {v: "HR Business Partner Senior Specialist"},
                            {v: "Intern"},
                            {v: "Internal Communications Senior Specialist"},
                            {v: "Internal Training and Development Expert"},
                            {v: "Legal Consultant for labor issues"},
                            {v: "Safety and Labor Protection Senior Engineer"},
                            {v: "Senior Specialist"},
                            {v: "Specialist"}
                        ],
                        "LD": [
                            {v: "Director"},
                            {v: "Expert"},
                            {v: "Government Relations Advisor"},
                            {v: "Government Relations Expert"},
                            {v: "Government Relations Specialist"},
                            {v: "Head"},
                            {v: "Lawyer"},
                            {v: "Manager"},
                            {v: "Senior Lawyer"},
                            {v: "Senior Specialist"},
                            {v: "Specialist"},
                            {v: "Supervisor"},
                            {v: "Translation Senior Specialist"}
                        ],
                        "TD": [
                            {v: "Active Equipment Based Products Senior Engineer"},
                            {v: "Analysis and Reporting Senior Specialist"},
                            {v: "Architecture Expert Engineer"},
                            {v: "Assistant"},
                            {v: "Chief Power Engineer"},
                            {v: "Core VAS Operations Engineer"},
                            {v: "Core VAS Operations Expert Engineer"},
                            {v: "Data Center Engineer"},
                            {v: "Data Center Senior Engineer"},
                            {v: "Data Integration Expert Engineer"},
                            {v: "Departmental Sanitary Expert"},
                            {v: "Director"},
                            {v: "Electro-Magnetic Field Inspection Senior Engineer"},
                            {v: "Electro-Magnetic Field Inspection Senior Specialist"},
                            {v: "Engineer"},
                            {v: "Ericsson Controllers Dimensioning Senior Engineer"},
                            {v: "Expert Engineer"},
                            {v: "Fiber Network Planning and Implementation Engineer"},
                            {v: "Field Operations Expert Engineer"},
                            {v: "Fixed Internet Engineer"},
                            {v: "Fixed internet Junior Specialist"},
                            {v: "Hardware testing Engineer"},
                            {v: "Hardware testing Senior Engineer"},
                            {v: "Head"},
                            {v: "HW/SW Planning and Implementation Engineer"},
                            {v: "Inspection Senior Engineer"},
                            {v: "Interconnect Senior Engineer"},
                            {v: "Intern"},
                            {v: "IP BackBone Expert Engineer"},
                            {v: "IT Architecture Expert Engineer"},
                            {v: "IT Architecture Senior Engineer"},
                            {v: "IT Service Delivery Expert Engineer"},
                            {v: "IT Service Delivery Senior Specialist"},
                            {v: "IT Support Senior Engineer"},
                            {v: "Junior Engineer"},
                            {v: "Junior Robotics Automation Engineer"},
                            {v: "Junior Specialist"},
                            {v: "License Engineer"},
                            {v: "Manager"},
                            {v: "Metro and IP Transport Network Expert Engineer"},
                            {v: "National Operation Acquisition Senior Specialist"},
                            {v: "National Operation Acquisition Specialist"},
                            {v: "OSS Expert Engineer"},
                            {v: "Passive Equipment Based Products Senior Engineer"},
                            {v: "Payment Administrator"},
                            {v: "Payment Senior Administrator"},
                            {v: "Permission Processing Specialist"},
                            {v: "Planning Senior Engineer"},
                            {v: "Power Engineer"},
                            {v: "Power Junior Engineer"},
                            {v: "Power Senior Engineer"},
                            {v: "Process and Guidelines Preparation Specialist"},
                            {v: "Process Automation Engineer"},
                            {v: "Process Automation Specialist"},
                            {v: "Providers Cooperation Senior Engineer"},
                            {v: "Providers Cooperation Senior Specialist"},
                            {v: "Quality Control Junior Specialist"},
                            {v: "Quality Control Senior Specialist"},
                            {v: "Radio and Transmission Equipment Senior Engineer"},
                            {v: "Radiofrequency Senior Engineer"},
                            {v: "RAN Operations Senior Engineer"},
                            {v: "RAN Transmission and IP Transport Senior Engineer"},
                            {v: "Roaming Senior Engineer"},
                            {v: "Senior Administrator"},
                            {v: "Senior Engineer"},
                            {v: "Senior Payment Administrator"},
                            {v: "Senior Specialist"},
                            {v: "Site Leasing Senior Specialist"},
                            {v: "Site Senior Engineer"},
                            {v: "Site Technician"},
                            {v: "Software Development Engineer"},
                            {v: "Software Development Expert Engineer"},
                            {v: "Software Development Junior Engineer"},
                            {v: "Software Development Senior Engineer"},
                            {v: "Software Development Senior Engineer, Category II"},
                            {v: "Software Development Senior Specialist"},
                            {v: "Software Development Specialist"},
                            {v: "Solution Intergation Engineer"},
                            {v: "Specialist"},
                            {v: "Supervisor"},
                            {v: "Technician"},
                            {v: "Tower Construction Engineer"},
                            {v: "Transmission Network Development Expert Engineer"},
                            {v: "Transmission Purchasing Control Senior Specialist"},
                            {v: "Voice and Data Senior Engineer"},
                            {v: "Voice Network Expert Engineer"},
                            {v: "ZTE Controllers Dimensioning BSS Senior Engineer"}
                        ]
                    },
                    "Outstaff - Individual Entrepreneur": {
                        "CCD": [
                            {v: "Distant Outbound Operator"},
                            {v: "Merchandiser"},
                            {v: "NPS call back administrator"},
                            {v: "Sales Administration Specialist (Consumer)"},
                            {v: "Sanitary, Electric&Subsidiary Services Specialist"},
                            {v: "Subscription Operator (Activ Operator)"}
                        ],
                        "CPD": [{v: "Archive Specialist"}],
                        "FD": [
                            {v: "Credit Control  Specialist"},
                            {v: "Credit Control Senior Specialist"}
                        ],
                        "LD": [{v: "Translator, Administrative Support"}],
                        "TD": [
                            {v: "Conditioner Technician"},
                            {v: "Electro meters consumption controller"}
                        ]
                    },
                    "Outstaff - Outsource": {
                        "B2B": [
                            {v: "B2B Assistant (document specialist)"},
                            {v: "B2B Senior Administrator SME"},
                            {v: "B2B Senior Representative SME"},
                            {v: "B2B Telesales"},
                            {v: "Courier"}
                        ],
                        "CCD": [
                            {v: "Call Center Operator Transtelecom"},
                            {v: "Call Center Operators"},
                            {v: "CCD Senior Representative"},
                            {v: "CCD Team leader"},
                            {v: "Senior representative service (договор обучения)"},
                            {v: "Senior Specialist"},
                            {v: "Telesales agents"}
                        ],
                        "CPD": [
                            {v: "1st category Assistant"},
                            {v: "Archive Specialist/Outsource"},
                            {v: "Dispatcher car-pool"},
                            {v: "Driver"},
                            {v: "Driver auto-loader"},
                            {v: "Foreman"},
                            {v: "Handing worker"},
                            {v: "Mechanic"},
                            {v: "Medical specialist"},
                            {v: "Reception"}
                        ],
                        "FD": [
                            {v: "Debt Collection Senior Specialist"},
                            {v: "Debt Collection Specialist"},
                            {v: "Physical Security Specialist"}
                        ],
                        "HR": [
                            {v: "Designer"},
                            {v: "HR Admin Specialist"}
                        ]
                    },
                    "Outstaff - Phys.Person": {
                        "B2B": [{v: "UX/UI Designer"}],
                        "B2C": [
                            {v: "Designer"},
                            {v: "Junior Research Specialist"}
                        ],
                        "CPD": [{v: "Stickerman-Loader (Regions)"}],
                        "FD": [{v: "Archivist"}]
                    }
                };
            },
			templateUrl: './js/directives/demand/resources.html'
		};
	});
});