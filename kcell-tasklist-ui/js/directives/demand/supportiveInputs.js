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
                onitemdeselect: '=',
                hintText: '='
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
                    externalIdProp: 'unit',
                    template: `{{getPropertyForObject(option, settings.displayProp)}} <span ng-if="option.form == 'HR' || option.form == 'CPD' || option.form == 'WH'" title="Необходимо заполнить форму для {{option.form == 'HR'?'ресурсов':'материалов'}} Supportive request form for {{option.form == 'WH'?'Centralized Procurement Department':option.unit}}" style="color: #682d86" class="glyphicon glyphicon-question-sign"></span>`
                };
                scope.optionList = [
                  {form: "TD",	unit: "TD - Network Economics Unit", groupId: "demand_supportive_td_network_economics_unit"},
                  {form: "HR",	unit: "Human Resources Department", groupId: "demand_supportive_hr"},
                  {form: "CPD",	unit: "Centralized Procurement Department"},
                  {form: "WH",	unit: "Warehouse and Logistics Section", groupId: "demand_supportive_warehouse_and_logistics_section"},
                  {form: "BI",	unit: "CCD - Business Intelligence", groupId: "demand_supportive_ccd_business_intelligence"},
                  {form: "BI",	unit: "B2C - Business Intelligence", groupId: "demand_supportive_b2c_business_intelligence_"},
                  {form: "BI",	unit: "B2B - Business Intelligence", groupId: "demand_supportive_b2b_business_intelligence"},
                  {form: "BI",	unit: "TD - Business Analytics Solutions Unit", groupId: "demand_supportive_td_business_analytics_solutions_unit"},
                  {form: "LD",	unit: "LD - Government Relations Unit", groupId: "demand_supportive_ld_government_relations_unit"},
                  {form: "LD",	unit: "LD - Contract Management Unit", groupId: "demand_supportive_ld_contract_management_unit"},
                  {form: "LD",	unit: "LD - Commercial Expertise Unit", groupId: "demand_supportive_ld_commercial_expertise_unit"},
                  {form: "LD",	unit: "LD - Litigation and Investigations Unit", groupId: "demand_supportive_ld_litigation_and_investigations_unit"},
                  {form: "Other",	unit: "FD - Debt Control Unit", groupId: "demand_supportive_fd_debt_control_unit"},
                  {form: "Other",	unit: "FD - Internal and Financial Control Unit", groupId: "demand_supportive_fd_internal_and_financial_control_unit"},
                  {form: "Other",	unit: "FD - Scoring and Credit Control Unit", groupId: "demand_supportive_fd_scoring_and_credit_control_unit"},
                  {form: "Other",	unit: "FD - Tax Unit", groupId: "demand_supportive_fd_tax_unit"},
                  {form: "Other",	unit: "FD - Fraud Management and Revenue Assurance Section", groupId: "demand_supportive_fd_fraud_and_revenue_assurance_section"},
                  {form: "Other",	unit: "FD - Real Estate Section", groupId: "demand_supportive_fd_real_estate_section"},
                  {form: "Other",	unit: "FD - Physical and Economical Security Unit", groupId: "demand_supportive_fd_physical_and_economical_security_unit"},
                  {form: "Other",	unit: "HR - Health and Safety Unit", groupId: "demand_supportive_hr_health_and_safety_unit"},
                  {form: "TD",	unit: "TD - Network Operations Unit", groupId: "demand_supportive_td_network_operations_unit"},
                  {form: "TD",	unit: "TD - Service Operations Unit", groupId: "demand_supportive_td_service_operations_unit"},
                  {form: "TD",	unit: "TD - IT Operations Unit", groupId: "demand_supportive_td_it_operations_unit"},
                  {form: "TD",	unit: "TD - Control Center Unit", groupId: "demand_supportive_td_control_center_unit"},
                  {form: "TD",	unit: "TD - Operation Support Unit", groupId: "demand_supportive_td_operation_support_unit"},
                  {form: "TD",	unit: "TD - Consumer Solutions Project Management Unit", groupId: "demand_supportive_td_consumer_solution_project_management_unit"},
                  {form: "TD",	unit: "TD - Delivery Management Unit", groupId: "demand_supportive_td_delivery_management_unit"},
                  {form: "TD",	unit: "TD - Enterprise Solutions Project Management Unit", groupId: "demand_supportive_td_enterprise_solution_project_management_unit"},
                  {form: "TD",	unit: "TD - Site and Facility Management Unit", groupId: "demand_supportive_td_site_and_facility_management_unit"},
                  {form: "TD",	unit: "TD - Access Network Rollout Unit", groupId: "demand_supportive_td_access_network_rollout_unit"},
                  {form: "TD",	unit: "TD - Access Network Unit", groupId: "demand_supportive_td_access_network_unit"},
                  {form: "TD",	unit: "TD - Voice Network Unit", groupId: "demand_supportive_td_voice_network_unit"},
                  {form: "TD",	unit: "TD - Data Network Unit", groupId: "demand_supportive_td_data_network_unit"},
                  {form: "TD",	unit: "TD - VAS platforms Unit", groupId: "demand_supportive_td_vas_platforms_unit"},
                  {form: "TD",	unit: "TD - Transmission Network Unit", groupId: "demand_supportive_td_transmission_network_unit"},
                  {form: "TD",	unit: "TD - Information Security Unit", groupId: "demand_supportive_td_information_security_unit"},
                  {form: "TD",	unit: "TD - Enterprise IT Unit", groupId: "demand_supportive_td_enterprise_it_unit"},
                  {form: "TD",	unit: "TD - IT Platforms Unit", groupId: "demand_supportive_td_it_platforms_unit"},
                  {form: "TD",	unit: "TD - Billing and Charging Unit", groupId: "demand_supportive_td_billing_and_charging_unit"},
                  {form: "TD",	unit: "TD - IT Architecture and Delivery Unit", groupId: "demand_supportive_td_it_architecture_and_delivery_unit"},
                  {form: "TD",	unit: "TD - Consumer Solutions Development Unit", groupId: "demand_supportive_td_consumer_solutions_development_unit"},
                  {form: "TD",	unit: "TD - Software Development Unit", groupId: "demand_supportive_td_software_development_unit"},
                  {form: "TD",	unit: "TD - Process Design and Automation Unit", groupId: "demand_supportive_td_process_design_and_automation_unit"},
                  {form: "TD",	unit: "TD - Enterprise Solutions Development Unit", groupId: "demand_supportive_td_enterprise_solutions_development_unit"},
                  {form: "Other",	unit: "B2C - Consumer Marketing Section", groupId: "demand_supportive_b2c_consumer_marketing_section"},
                  {form: "Other",	unit: "B2B - Enterprise Marketing Section", groupId: "demand_supportive_b2b_enterprise_marketing_section"},
                  {form: "Other",	unit: "B2C - Interconnect and Roaming Section", groupId: "demand_supportive_b2c_interconnect_and_roaming_section"},
                  {form: "Other",	unit: "FD - Reporting unit", groupId: "demand_supportive_fd_reporting_unit"},
                  {form: "Other",	unit: "FD - Accounting Section", groupId: "demand_supportive_fd_accounting_section"},
                  {form: "Other",	unit: "FD - Treasury Section", groupId: "demand_supportive_fd_treasury_section"},
                  {form: "Other",	unit: "CCD - Customer Relations and Experience Section", groupId: "demand_supportive_ccd_customer_relations_and_experience_section"},
                  {form: "Other",	unit: "CCD - Remote Sales and Service Section", groupId: "demand_supportive_ccd_remote_sales_and_service_section"},
                  {form: "Other",	unit: "NB - Business Development Section", groupId: "demand_supportive_nb_business_development_section"},
                  {form: "Other",	unit: "B2B - Products Delivery Management Unit", groupId: "demand_supportive_b2b_products_delivery_management_unit"},
                  {form: "Other",	unit: "B2B - Large Accounts Development Section", groupId: "demand_supportive_b2b_large_accounts_development_section"},
                  {form: "Other",	unit: "B2B - Products Development Section", groupId: "demand_supportive_b2b_products_development_section"},
                  {form: "Other",	unit: "B2B - Small and Medium Accounts Development Section", groupId: "demand_supportive_b2b_small_medium_accounts_development_section"},
                  {form: "Other",	unit: "B2B - Strategic Accounts Development Section", groupId: "demand_supportive_b2b_strategic_accounts_development_section"},
                  {form: "Other",	unit: "CEO - Internal Audit Section", groupId: "demand_supportive_ceo_internal_audit_section"}
                ];

                var optionsCopy = _.map(scope.optionList, _.clone);
            },
            templateUrl: './js/directives/demand/supportiveInputs.html'
        };
    });
});


