define(['./../module'], function(module){
    'use strict';
    module.directive('demandSupportiveInputs', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                onitemselect: '=',
                onitemdeselect: '='
            },
            link: function(scope, element, attrs) {
                scope.$watch('data', function(value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        for (var elt of scope.data) {
                            var opt = scope.optionList.find(function(e) {return e.unit == elt.unit;});
                            var ind = scope.optionList.indexOf(opt), ind1 = -1, ind2 = 1;
                            scope.optionList.splice(ind, 1);
                            for (var i = 0; i < scope.data.length; i++) {
                                ind1 = optionsCopy.findIndex(function(e) {return e.unit == scope.optionList[i].unit;});
                                ind2 = optionsCopy.findIndex(function(e) {return e.unit == opt.unit;});
                                if (ind1 > ind2) {
                                    scope.optionList.splice(i, 0, opt);
                                    break;
                                }
                            }
                            if (ind1 < ind2) scope.optionList.splice(scope.data.length - 1, 0, opt);
                        }
                    }
                });
                scope.multiselectEvents = {
                    onItemSelect: function(item) {
                        var elt = scope.data.find(function(e) {return e.unit == item.unit;});
                        var opt = scope.optionList.find(function(e) {return e.unit == item.unit;});
                        if (elt && opt) angular.copy(opt, elt);
                        if (scope.onitemselect) scope.onitemselect(elt);
                        var ind = scope.optionList.indexOf(opt), ind1 = -1, ind2 = 1;
                        scope.optionList.splice(ind, 1);
                        for (var i = 0; i < scope.data.length; i++) {
                            ind1 = optionsCopy.findIndex(function(e) {return e.unit == scope.optionList[i].unit;});
                            ind2 = optionsCopy.findIndex(function(e) {return e.unit == opt.unit;});
                            if (ind1 > ind2) {
                                scope.optionList.splice(i, 0, opt);
                                break;
                            }
                        }
                        if (ind1 < ind2) scope.optionList.splice(scope.data.length - 1, 0, opt);
                    },
                    onItemDeselect: function(item) {
                        var elt = scope.optionList.find(function(e) {return e.unit == item.unit;});
                        if (scope.onitemdeselect) scope.onitemdeselect(elt);
                        var ind = scope.optionList.indexOf(elt), ind1 = -1, ind2 = 1;
                        scope.optionList.splice(ind, 1);
                        for (var i = scope.data.length; i < scope.optionList.length; i++) {
                            ind1 = optionsCopy.findIndex(function(e) {return e.unit == scope.optionList[i].unit;});
                            ind2 = optionsCopy.findIndex(function(e) {return e.unit == elt.unit;});
                            if (ind1 > ind2) {
                                scope.optionList.splice(i, 0, elt);
                                break;
                            }
                        }
                        if (ind1 < ind2) scope.optionList.push(elt);
                    },
                    onDeselectAll: function() {
                        for (var item of scope.data) {
                            var elt = scope.optionList.find(function(e) {return e.unit == item.unit;});
                            if (scope.onitemdeselect) scope.onitemdeselect(elt);
                        }
                        scope.optionList = _.map(optionsCopy, _.clone);
                    }
                };
                scope.multiselectSettings = {
                    enableSearch: true,
                    smartButtonMaxItems: 1,
                    showCheckAll: false,
                    showUncheckAll: true,
                    displayProp: 'unit',
                    idProp: 'unit',
                    externalIdProp: 'unit'
                };
                scope.optionList = [
                  {form: "TD",	unit: "TD - Network Economics Unit", groupId: "Demand_Supportive_TD_Network_Economics_Unit"},
                  {form: "HR",	unit: "Human Resources Department", groupId: "Demand_Supportive_Human_Resources_Department"},
                  {form: "CPD",	unit: "Centralized Procurement Department"},
                  {form: "WH",	unit: "Warehouse and Logistics Section", groupId: "Demand_Supportive_Warehouse_and_Logistics_Section"},
                  {form: "BI",	unit: "CCD - Business Intelligence", groupId: "Demand_Supportive_CCD_Business_Intelligence"},
                  {form: "BI",	unit: "B2C - Business Intelligence", groupId: "Demand_Supportive_B2C_Business_Intelligence"},
                  {form: "BI",	unit: "B2B - Business Intelligence", groupId: "Demand_Supportive_B2B_Business_Intelligence"},
                  {form: "BI",	unit: "TD - Business Analytics Solutions Unit", groupId: "Demand_Supportive_TD_Business_Analytics_Solutions_Unit"},
                  {form: "LD",	unit: "LD - Government Relations Unit", groupId: "Demand_Supportive_LD_Government_Relations_Unit"},
                  {form: "LD",	unit: "LD - Contract Management Unit", groupId: "Demand_Supportive_LD_Contract_Management_Unit"},
                  {form: "LD",	unit: "LD - Commercial Expertise Unit", groupId: "Demand_Supportive_LD_Commercial_Expertise_Unit"},
                  {form: "LD",	unit: "LD - Litigation and Investigations Unit", groupId: "Demand_Supportive_LD_Litigation_and_Investigations_Unit"},
                  {form: "Other",	unit: "FD - Debt Control Unit", groupId: "Demand_Supportive_FD_Debt_Control_Unit"},
                  {form: "Other",	unit: "FD - Internal and Financial Control Unit", groupId: "Demand_Supportive_FD_Internal_and_Financial_Control_Unit"},
                  {form: "Other",	unit: "FD - Scoring and Credit Control Unit", groupId: "Demand_Supportive_FD_Scoring_and_Credit_Control_Unit"},
                  {form: "Other",	unit: "FD - Tax Unit", groupId: "Demand_Supportive_FD_Tax_Unit"},
                  {form: "Other",	unit: "FD - Fraud Management and Revenue Assurance Section", groupId: "Demand_Supportive_FD_Fraud_Management_and_Revenue_Assurance_Section"},
                  {form: "Other",	unit: "FD - Real Estate Section", groupId: "Demand_Supportive_FD_Real_Estate_Section"},
                  {form: "Other",	unit: "FD - Physical and Economical Security Unit", groupId: "Demand_Supportive_FD_Physical_and_Economical_Security_Unit"},
                  {form: "Other",	unit: "HR - Health and Safety Unit", groupId: "Demand_Supportive_HR_Health_and_Safety_Unit"},
                  {form: "TD",	unit: "TD - Network Operations Unit", groupId: "Demand_Supportive_TD_Network_Operations_Unit"},
                  {form: "TD",	unit: "TD - Service Operations Unit", groupId: "Demand_Supportive_TD_Service_Operations_Unit"},
                  {form: "TD",	unit: "TD - IT Operations Unit", groupId: "Demand_Supportive_TD_IT_Operations_Unit"},
                  {form: "TD",	unit: "TD - Control Center Unit", groupId: "Demand_Supportive_TD_Control_Center_Unit"},
                  {form: "TD",	unit: "TD - Operation Support Unit", groupId: "Demand_Supportive_TD_Operation_Support_Unit"},
                  {form: "TD",	unit: "TD - Consumer Solutions Project Management Unit", groupId: "Demand_Supportive_TD_Consumer_Solutions_Project_Management_Unit"},
                  {form: "TD",	unit: "TD - Delivery Management Unit", groupId: "Demand_Supportive_TD_Delivery_Management_Unit"},
                  {form: "TD",	unit: "TD - Enterprise Solutions Project Management Unit", groupId: "Demand_Supportive_TD_Enterprise_Solutions_Project_Management_Unit"},
                  {form: "TD",	unit: "TD - Site and Facility Management Unit", groupId: "Demand_Supportive_TD_Site_and_Facility_Management_Unit"},
                  {form: "TD",	unit: "TD - Access Network Rollout Unit", groupId: "Demand_Supportive_TD_Access_Network_Rollout_Unit"},
                  {form: "TD",	unit: "TD - Access Network Unit", groupId: "Demand_Supportive_TD_Access_Network_Unit"},
                  {form: "TD",	unit: "TD - Voice Network Unit", groupId: "Demand_Supportive_TD_Voice_Network_Unit"},
                  {form: "TD",	unit: "TD - Data Network Unit", groupId: "Demand_Supportive_TD_Data_Network_Unit"},
                  {form: "TD",	unit: "TD - VAS platforms Unit", groupId: "Demand_Supportive_TD_VAS_platforms_Unit"},
                  {form: "TD",	unit: "TD - Transmission Network Unit", groupId: "Demand_Supportive_TD_Transmission_Network_Unit"},
                  {form: "TD",	unit: "TD - Information Security Unit", groupId: "Demand_Supportive_TD_Information_Security_Unit"},
                  {form: "TD",	unit: "TD - Enterprise IT Unit", groupId: "Demand_Supportive_TD_Enterprise_IT_Unit"},
                  {form: "TD",	unit: "TD - IT Platforms Unit", groupId: "Demand_Supportive_TD_IT_Platforms_Unit"},
                  {form: "TD",	unit: "TD - Billing and Charging Unit", groupId: "Demand_Supportive_TD_Billing_and_Charging_Unit"},
                  {form: "TD",	unit: "TD - IT Architecture and Delivery Unit", groupId: "Demand_Supportive_TD_IT_Architecture_and_Delivery_Unit"},
                  {form: "TD",	unit: "TD - Consumer Solutions Development Unit", groupId: "Demand_Supportive_TD_Consumer_Solutions_Development_Unit"},
                  {form: "TD",	unit: "TD - Software Development Unit", groupId: "Demand_Supportive_TD_Software_Development_Unit"},
                  {form: "TD",	unit: "TD - Process Design and Automation Unit", groupId: "Demand_Supportive_TD_Process_Design_and_Automation_Unit"},
                  {form: "TD",	unit: "TD - Enterprise Solutions Development Unit", groupId: "Demand_Supportive_TD_Enterprise_Solutions_Development_Unit"},
                  {form: "Other",	unit: "B2C - Consumer Marketing Section", groupId: "Demand_Supportive_B2C_Consumer_Marketing_Section"},
                  {form: "Other",	unit: "B2B - Enterprise Marketing Section", groupId: "Demand_Supportive_B2B_Enterprise_Marketing_Section"},
                  {form: "Other",	unit: "B2C - Interconnect and Roaming Section", groupId: "Demand_Supportive_B2C_Interconnect_and_Roaming_Section"},
                  {form: "Other",	unit: "FD - Reporting unit", groupId: "Demand_Supportive_FD_Reporting_unit"},
                  {form: "Other",	unit: "FD - Accounting Section", groupId: "Demand_Supportive_FD_Accounting_Section"},
                  {form: "Other",	unit: "FD - Treasury Section", groupId: "Demand_Supportive_FD_Treasury_Section"},
                  {form: "Other",	unit: "CCD - Customer Relations and Experience Section", groupId: "Demand_Supportive_CCD_Customer_Relations_and_Experience_Section"},
                  {form: "Other",	unit: "CCD - Remote Sales and Service Section", groupId: "Demand_Supportive_CCD_Remote_Sales_and_Service_Section"},
                  {form: "Other",	unit: "NB - Business Development Section", groupId: "Demand_Supportive_NB_Business_Development_Section"},
                  {form: "Other",	unit: "B2B - Products Delivery Management Unit", groupId: "Demand_Supportive_B2B_Products_Delivery_Management_Unit"},
                  {form: "Other",	unit: "B2B - Large Accounts Development Section", groupId: "Demand_Supportive_B2B_Large_Accounts_Development_Section"},
                  {form: "Other",	unit: "B2B - Products Development Section", groupId: "Demand_Supportive_B2B_Products_Development_Section"},
                  {form: "Other",	unit: "B2B - Small and Medium Accounts Development Section", groupId: "Demand_Supportive_B2B_Small_and_Medium_Accounts_Development_Section"},
                  {form: "Other",	unit: "B2B - Strategic Accounts Development Section", groupId: "Demand_Supportive_B2B_Strategic_Accounts_Development_Section"},
                  {form: "Other",	unit: "CEO - Internal Audit Section", groupId: "Demand_Supportive_CEO_Internal_Audit_Section"}
                ];

                var optionsCopy = _.map(scope.optionList, _.clone);
            },
            templateUrl: './js/directives/demand/supportiveInputs.html'
        };
    });
});


