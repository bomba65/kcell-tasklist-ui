define(['./module','jquery'], function(app,$){
	'use strict';
	return app.controller('statisticsCtrl', ['$scope', '$rootScope', '$http', '$state', '$stateParams', '$q', '$location', 'AuthenticationService',
			                         function($scope,   $rootScope,   $http, $state,  $stateParams,   $q, $location, AuthenticationService) {
		$rootScope.currentPage = {
			name: 'statistics'
		};

        if(window.require){
            $scope.ExcellentExport = require('excellentexport');
        }

		$scope._ = window._;

		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		$scope.baseUrl = '/camunda/api/engine/engine/default';
		// $scope.baseUrl = "https://test-flow.kcell.kz/engine-rest/engine/default";
        $scope.report_ready = false;

        if($rootScope.authentication){
            $http.get($scope.baseUrl+'/user/'+$rootScope.authentication.name+'/profile').then(
                function(userProfile){
                    $rootScope.authUser = userProfile.data;
                    $http.get($scope.baseUrl+'/group?member='+$rootScope.authUser.id).then(
                        function(groups){
                            $rootScope.authUser.groups = groups.data;
                        },
                        function(error){
                            console.log(error.data);
                        }
                    );
                },
                function(error){
                    console.log(error.data);
                }
            );
        }

        $rootScope.hasGroup = function(group){
            if($rootScope.authUser && $rootScope.authUser.groups){
                return _.some($rootScope.authUser.groups, function(value){
                    return value.id === group;
                });
            } else {
                return false;
            }
        }

		$scope.reportsMap = {
            'revision-open-tasks': {name: 'Revision open tasks'},
            'invoice-open-tasks': {name: 'Monthly Act open tasks'}
        };

        $scope.reports = [
            'revision-open-tasks',
            'invoice-open-tasks'
        ];

		$scope.currentReport = $stateParams.report || 'revision-open-tasks';

        $scope.task = $stateParams.task;
        $scope.region = $stateParams.region;

        $http.get('/api/catalogs').then(
            function (result) {
                angular.extend($scope, result.data);
            },
            function (error) {
                console.log(error.data);
            }
        );

        $scope.getJrRegion = function (jrNumber) {
            if (jrNumber) {
                if (jrNumber.startsWith("Alm")) {
                    return 'almaty';
                } else if (jrNumber.startsWith("East")) {
                    return 'east';
                } else if (jrNumber.startsWith("N&C")) {
                    return 'north_central';
                } else if (jrNumber.startsWith("South")) {
                    return 'south';
                } else if (jrNumber.startsWith("West")) {
                    return 'west';
                } else if (jrNumber.startsWith("Astana")) {
                    return 'astana';
                } else {
                    return 'no_region';
                }
            } else {
                return 'no_region';
            }
        };

        $scope.getInvoiceRegion = function (invoiceNumber) {
            if (invoiceNumber) {
                if (invoiceNumber.endsWith("Alm")) {
                    return 'almaty';
                } else if (invoiceNumber.endsWith("East")) {
                    return 'east';
                } else if (invoiceNumber.endsWith("N&C")) {
                    return 'north_central';
                } else if (invoiceNumber.endsWith("South")) {
                    return 'south';
                } else if (invoiceNumber.endsWith("West")) {
                    return 'west';
                } else if (invoiceNumber.endsWith("Astana")) {
                    return 'astana';
                } else {
                    return 'no_region';
                }
            } else {
                return 'no_region';
            }
        };

        $scope.filterRegion = function(task){
            if($scope.region){
                return $scope.getJrRegion(task.variables.jrNumber.value) === $scope.region;
            } else {
                return true;
            }
        }

        $scope.getProcessDefinition = function(){
            if($scope.currentReport === 'revision-open-tasks'){
                return 'Revision';
            } else if($scope.currentReport === 'invoice-open-tasks'){
                return 'Invoice';
            }
        }


        $scope.updateTaskDefinitions = function(){
            var processDefinition = $scope.getProcessDefinition();

            $http.get($scope.baseUrl + '/process-definition/key/' + processDefinition + '/xml')
            .then(function(response) {
                var domParser = new DOMParser();

                var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

                function getUserTasks(xml) {
                    var namespaces = {
                        bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
                    };

                    var userTaskNodes = getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]);

                    function getElementsByXPath(doc, xpath, namespaceFn, parent) {
                        let results = [];
                        let query = doc.evaluate(xpath,
                            parent || doc,
                            namespaceFn,
                            XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
                        for (let i=0, length=query.snapshotLength; i<length; ++i) {
                            results.push(query.snapshotItem(i));
                        }
                        return results;
                    }

                    return userTaskNodes.map(node => {
                        var id = node.id;
                        var name = node.attributes["name"] && node.attributes["name"].textContent;
                        var description = getElementsByXPath(
                            xml,
                            'bpmn:documentation/text()',
                            prefix => namespaces[prefix],
                            node
                        )[0];

                        description = description && description.textContent;

                        return {
                            "id" : id,
                            "name" : name,
                            "description": description
                        };
                    });
                }

                var userTasks = getUserTasks(xml);
                var userTasksMap = _.keyBy(userTasks, 'id');
                $scope.userTasksMap = userTasksMap;
            });
        }

    	if ($scope.task) {
            var query = {
                taskDefinitionKey: $scope.task,
                processDefinitionKey: $scope.getProcessDefinition(),
                unfinished: true
            };

			$http.post($scope.baseUrl + '/history/task', query).then(function(response) {
				var tasks = response.data;
                var processInstanceIds = _.map(tasks, 'processInstanceId');
                return $http.post($scope.baseUrl + '/history/variable-instance', {
                    processInstanceIdIn: processInstanceIds
                }).then(function(response){
                    var variables = response.data;
                    var variablesByProcessInstance = _.groupBy(variables, 'processInstanceId');
                    return _.map(tasks, function(task) {
                        return _.assign({}, task, {
                            variables: _.keyBy(
                                variablesByProcessInstance[task.processInstanceId],
                                'name'
                            )
                        });
                    });
                });
			}).then(function (tasks) {
                $scope.tasks = tasks;
            });

		} else {
            $scope.updateTaskDefinitions();

    		$scope.kcellTasks = {
                'revision-open-tasks':[
                    'modify_jr', //modify_jr
                    'approve_jr_regions', //approve_jr_regions
                    'check_power', //check_power
                    'approve_transmission_works', //approve_transmission_works
                    'approve_jr_budget', //approve_jr_budget
                    'approve_jr', //approve_jr
                    'update_leasing_status_general', //update_leasing_status_general
                    'update_leasing_status_special', //update_leasing_status_special
                    //'Task_0s5v6wl',
                    'approve_material_list_region', //approve_material_list_region
                    'approve_material_list_center', //approve_material_list_center
                    'approve_material_list_center1',
                    'validate_tr', //validate_tr
                    'set_materials_dispatch_status', //set_materials_dispatch_status
                    'verify_works', //verify_works
                    //'UserTask_0xsau1t',
                    'accept_work_initiator', //accept_work_initiator
                    'accept_work_maintenance_group', //accept_work_maintenance_group
                    'accept_work_planning_group', //accept_work_planning_group
                    'sign_region_head', //accept_work_planning_group
                    'attach-scan-copy-of-acceptance-form'],
                 'invoice-open-tasks': [
                    'ma_check_region',
                    'ma_sign_region_head',
                    'ma_sign_region_manager',
                    'ma_check_budget',
                    'ma_check_centralgroup_tech',
                    'ma_sign_budget',
                    'ma_check_centralgroup',
                    'ma_sign_head1',
                    'ma_sign_head2',
                    'ma_sign_manager',
                    'ma_sign_cto',
                    'ma_print_version',
                    'ma_invoice_number'
                ]
            };

    		$scope.contractorTasks = {
                'revision-open-tasks':[
                    'upload_tr_contractor', //upload_tr_contractor
                    'attach_material_list_contractor', //attach_material_list_contractor
                    'fill_applied_changes_info' //fill_applied_changes_info
                ],
                'invoice-open-tasks': [
                    'ma_sendtobranch',
                    'ma_modify'
                ]
            };

            if ($scope.currentReport === 'revision-open-tasks') {
                var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', {
                    "processDefinitionKey": "Revision",
                    "unfinished": true
                }).then(function(response) {
                    var processInstances = _.keyBy(response.data, 'id');
                    return $http.post($scope.baseUrl + '/history/variable-instance', {
                        variableName: 'jrNumber',
                        processInstanceIdIn: _.keys(processInstances)
                    }).then(function(response){
                        var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
                        var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
                        var result = _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'jrNumber': valueByProcessInstance[id]}));
                        return result;
                    });
                });

                var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', {
                    "processDefinitionKey": 'Revision',
                    "unfinished": true
                }).then(function(response) {
                    return response.data;
                });

                $q.all([processInstancesPromise, taskInstancesPromise])
                    .then(function(results) {
                        var processInstances = results[0];
                        var taskInstances = results[1];

                        var taskInstancesByDefinition = _.groupBy(
                            taskInstances,
                            'taskDefinitionKey'
                        );

                        var tasksByIdAndRegionGrouped = _.mapValues(
                            taskInstancesByDefinition,
                            function(tasks) {
                                return _.groupBy(
                                    tasks,
                                    function(task) {
                                        var pid = task.processInstanceId;
                                        if (processInstances[pid]) {
                                            return $scope.getJrRegion(processInstances[pid].jrNumber);
                                        } else {
                                            return 'no_processinstance';
                                        }
                                    }
                                );
                            }
                        );

                        var tasksByIdAndRegionCounted = _.mapValues(
                            tasksByIdAndRegionGrouped,
                            function(tasks) { return _.mapValues(tasks, 'length'); }
                        );

                        $scope.tasksByIdAndRegionCounted = tasksByIdAndRegionCounted;
                    });
            } else if($scope.currentReport === 'invoice-open-tasks'){

                var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', {
                    "processDefinitionKey": "Invoice",
                    "unfinished": true
                }).then(function(response) {
                    var processInstances = _.keyBy(response.data, 'id');
                    return $http.post($scope.baseUrl + '/history/variable-instance', {
                        variableName: 'invoiceNumber',
                        processInstanceIdIn: _.keys(processInstances)
                    }).then(function(response){
                        var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
                        var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
                        var result = _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'invoiceNumber': valueByProcessInstance[id]}));
                        return result;
                    });
                });

                var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', {
                    "processDefinitionKey": 'Invoice',
                    "unfinished": true
                }).then(function(response) {
                    return response.data;
                });

                $q.all([processInstancesPromise, taskInstancesPromise])
                    .then(function(results) {
                        var processInstances = results[0];
                        var taskInstances = results[1];

                        var taskInstancesByDefinition = _.groupBy(
                            taskInstances,
                            'taskDefinitionKey'
                        );

                        var tasksByIdAndRegionGrouped = _.mapValues(
                            taskInstancesByDefinition,
                            function(tasks) {
                                return _.groupBy(
                                    tasks,
                                    function(task) {
                                        var pid = task.processInstanceId;
                                        if (processInstances[pid]) {
                                            return $scope.getInvoiceRegion(processInstances[pid].invoiceNumber);
                                        } else {
                                            return 'no_processinstance';
                                        }
                                    }
                                );
                            }
                        );

                        var tasksByIdAndRegionCounted = _.mapValues(
                            tasksByIdAndRegionGrouped,
                            function(tasks) { return _.mapValues(tasks, 'length'); }
                        );

                        $scope.tasksByIdAndRegionCounted = tasksByIdAndRegionCounted;
                    });
            }
		}

		$scope.selectReport = function(report){
			$location.url($location.path() + "?report=" + report);
		}



        $scope.downloadReport = function(){
            if($scope.report_ready){
                return $scope.ExcellentExport.convert({anchor: 'reportClick',format: 'xlsx',filename: 'report'}, [{name: 'Sheet Name Here 1',from: {table: 'reportTable'}}]);
            } else {
                if($rootScope.authentication.name === 'demo' || $rootScope.authentication.name === 'Evgeniy.Semenov@kcell.kz' || $rootScope.authentication.name === 'Yernaz.Kalingarayev@kcell.kz'){
                    $http.get('/camunda/reports/report').then(function(response) {
                        $scope.reportList = response.data;
                        $scope.report_ready = true;
                    });
                } 
            }
        }
	}]);
});
