define(['./module','jquery'], function(app,$){
	'use strict';
	return app.controller('statisticsCtrl', ['$scope', '$rootScope', '$filter', '$http', '$state', '$stateParams', '$q', '$location', 'AuthenticationService',
			                         function($scope,   $rootScope, $filter, $http, $state,  $stateParams,   $q, $location, AuthenticationService) {

		$rootScope.currentPage = {
			name: 'statistics'
		};

        if(window.require){
            $scope.XLSX = require('xlsx');
        }

		$scope._ = window._;

		$rootScope.logout = function(){
			AuthenticationService.logout().then(function(){
				$scope.authentication = null;
			});
		}

		$scope.baseUrl = '/camunda/api/engine/engine/default';
        $scope.report_ready = false;

		$scope.reportsMap = {
            'revision-open-tasks': {name: 'Revision open tasks', process: 'Revision'},
            'invoice-open-tasks': {name: 'Monthly Act open tasks', process: 'Invoice'},
            '4gSharing-open-tasks': {name: '4G Site Sharing open tasks', process: 'SiteSharingTopProcess'}
        };

        $scope.reports = [
            'revision-open-tasks',
            'invoice-open-tasks',
            '4gSharing-open-tasks'
        ];
        
        $scope.$watchGroup(['selectedProject', 'selectedProcess'], function(newValues, oldValues, scope) {
            if((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)){
                if(!$rootScope.isProjectAvailable('NetworkInfrastructure') || !$rootScope.isProjectVisible('NetworkInfrastructure')){
                    $state.go('tasks');
                }
            }
        }, true);

        $scope.currentReport = $stateParams.report;
        $scope.reverseOrder = false;
        $scope.fieldName = 'Region';
        $scope.task = $stateParams.task;

        $scope.filter = {};
        if($stateParams.reason){
             $scope.filter.reason = $stateParams.reason;
        }
        if($stateParams.mainContract){
             $scope.filter.mainContract = $stateParams.mainContract;
        }
        $scope.region = $stateParams.region;

        $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
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

        $scope.orderByFieldName = function(fieldName) {
            console.log(fieldName)
			if ($scope.fieldName == fieldName) {
				$scope.reverseOrder = !$scope.reverseOrder;
			} else {
				$scope.reverseOrder = false;
				$scope.fieldName = fieldName;
			}
        };

        $scope.orderByRevisionFieldName = function(revisionFieldName) {
            if ($scope.revisionFieldName == revisionFieldName) {
                $scope.revisionReverseOrder = !$scope.revisionReverseOrder;
            } else {
                $scope.revisionReverseOrder = false;
                $scope.revisionFieldName = revisionFieldName;
            }
        };

        $scope.orderRevisionFunction = function(task){
            if($scope.revisionFieldName === 'Region'){
                return $scope.getJrRegion(task.variables.jrNumber.value);
            } else if($scope.revisionFieldName === 'site_name'){
                return task.variables.site_name.value;
            } else if($scope.revisionFieldName === 'jrNumber'){
                return task.variables.jrNumber.value;
            } else if($scope.revisionFieldName === 'contractor'){
                return $scope.contractorsTitle[task.variables.contractor.value];
            } else if($scope.revisionFieldName === 'reason'){
                return $scope.reasonsTitle[task.variables.reason.value];
            } else if($scope.revisionFieldName === 'project'){
                return task.variables.project.value;
            } else if($scope.revisionFieldName === 'starter'){
                return task.variables.starter.value;
            } else if($scope.revisionFieldName === 'requestedDate'){
                return task.variables.requestedDate.value;
            } else if($scope.revisionFieldName === 'validityDate'){
                return task.variables.validityDate.value;
            } else if($scope.revisionFieldName === 'assignee'){
                return task.assignee ? task.assignee : task.groupId;
            }     
            
            
            
            
            
        }
        
        $scope.getInvoiceRegion = function (invoiceNumber) {
            if(invoiceNumber.endsWith('-RO-1')){
                invoiceNumber = invoiceNumber.replace('-RO-1','');
            }
            if(invoiceNumber.endsWith('-RO-2')){
                invoiceNumber = invoiceNumber.replace('-RO-2','');
            }
            if(invoiceNumber.endsWith('-RO-3')){
                invoiceNumber = invoiceNumber.replace('-RO-3','');
            }
            if(invoiceNumber.endsWith('-RO-4')){
                invoiceNumber = invoiceNumber.replace('-RO-4','');
            }
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
                if($scope.getProcessDefinition() === 'Revision'){
                    return $scope.getJrRegion(task.variables.jrNumber.value) === $scope.region;
                } else if($scope.getProcessDefinition() === 'Invoice'){
                    return $scope.getInvoiceRegion(task.variables.invoiceNumber.value) === $scope.region;
                }
            } else {
                return true;
            }
        }

        $scope.getProcessDefinition = function(){
            if($scope.currentReport === 'revision-open-tasks'){
                return 'Revision';
            } else if($scope.currentReport === 'invoice-open-tasks'){
                return 'Invoice';
            } else if($scope.currentReport === '4gSharing-open-tasks'){
                return 'SiteSharingTopProcess';
            }
        }

        $scope.regions = ['.almaty', '.east', '.west', '.north_central', '.south', '.astana', '.no_region',];
        $scope.checkRegionView = function(region){
            if($rootScope.hasGroup('head_kcell_users')){
                return true;
            } else if($rootScope.hasGroup('alm_kcell_users')){
                return region === '.almaty';
            } else if($rootScope.hasGroup('astana_kcell_users')){
                return region === '.astana';
            } else if($rootScope.hasGroup('east_kcell_users')){
                return region === '.east';
            } else if($rootScope.hasGroup('nc_kcell_users')){
                return region === '.north_central';
            } else if($rootScope.hasGroup('south_kcell_users')){
                return region === '.south';
            } else if($rootScope.hasGroup('west_kcell_users')){
                return region === '.west';
            } else {
                return false;
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

        if ($scope.currentReport === '4gSharing-open-tasks') {
            $http.get('/asset-management/api/plans/search/findCurrentPlanSites').then(function(response) {
                ;
                var currentPlans = _.flatMap( _.filter(response.data._embedded.plans, function(plan) { return plan.status !== 'site_sharing_complete'; }), function (plan) {
                    if (plan.status !== 'site_sharing_complete') {
                        var range = [];
                        _.forEach(plan.params.shared_sectors, function(sector) {
                            if (!_.includes(range, sector.enodeb_range)) {
                                range.push(sector.enodeb_range);
                            }
                        });
                        return {"site_id": plan.site_id, "status": plan.status, "host": plan.params.host, "range": range};
                    } else return null;
                });


                if(currentPlans) {
                    console.log(currentPlans);
                    //----------------------------------------------------------
                    var siteCountByType = {'planed':{"band800":0,"band1800":0,"band2100":0, "all": 0}, 'onair':{"band800":0,"band1800":0,"band2100":0, "all": 0}, 'kcellHost':{"band800":0,"band1800":0,"band2100":0, "all": 0}, 'beelineHost':{"band800":0,"band1800":0,"band2100":0, "all": 0}};                
                    // для подсчета статистики по шеринг сайтам
                    //-----------------------------------------------------------
                    _.flatMap(currentPlans, function(plan){
                        _.forEach([800, 1800, 2100, 'all'], function(band) {
                            if (band === 'all') {
                                siteCountByType.planed.all += 1;
                                if(plan.status === 'site_on_air') {
                                    siteCountByType.onair.all += 1;
                                } else if ( plan.status === 'site_accepted_service') {
                                    if(plan.host.toLowerCase() === 'beeline') {
                                        siteCountByType.beelineHost.all += 1;
                                    } else if (plan.host.toLowerCase() === 'kcell') {
                                        siteCountByType.kcellHost.all += 1;
                                    }
                                } 
                            } else if (_.includes(plan.range, band)) {
                                siteCountByType.planed['band'+band] += 1;
                                if(plan.status === 'site_on_air') {
                                    siteCountByType.onair['band'+band] += 1;
                                } else if ( plan.status === 'site_accepted_service') {
                                    if(plan.host.toLowerCase() === 'beeline') {
                                        siteCountByType.beelineHost['band'+band] += 1;
                                    } else if (plan.host.toLowerCase() === 'kcell') {
                                        siteCountByType.kcellHost['band'+band] += 1;
                                    }
                                }
                            }
                        })
                    })
                    console.log(siteCountByType);
                    //$scope.siteCountByType = {"planed":{"800":"4","1800":"6","all":"10"}, "onair":{"800":"1","1800":"3","all":"4"}, "kcellHost":{"800":"2","1800":"2","all":"4"}, "beelineHost":{"800":"3","1800":"2","all":"5"}};
                    $scope.siteCountByType = siteCountByType;
                    //console.log($scope.siteCountByType);
                    //-----------------------------------------------------------
                }
                
            });
            $scope.userTasksMap = {};
            for (let pid of ['BeelineHostBeelineSite', 'BeelineHostKcellSite', 'KcellHostBeelineSite', 'KcellHostKcellSite','ReplanSharedSiteAddressPlan']) {
                var userTasksPromise = $http.get($scope.baseUrl + '/process-definition/key/' + pid +'/xml')
                //var userTasksPromise = $http.get($scope.baseUrl + '/process-definition/key/Revision/xml')
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
                        //$scope.userTasksMap = userTasksMap;
                        $scope.userTasksMap = _.assign($scope.userTasksMap , userTasksMap);
                    });
                }

            if ($scope.task) {
                $http.post($scope.baseUrl + '/history/task', {
                    taskDefinitionKey: $scope.task,
                    unfinished: true
                }).then(function(response) {
                    var tasks = response.data;
                    console.log(tasks)
                    var processInstanceIds = _.map(tasks, 'processInstanceId');
                    return $http.post($scope.baseUrl + '/history/variable-instance/?deserializeValues=false', {
                        processInstanceIdIn: processInstanceIds
                    }).then(function(response){
                        var variables = _.flatMap(response.data, function(pi) {
                            if (pi.type === 'Json') {
                                return {"name": pi.name, "value": JSON.parse(pi.value), "processInstanceId": pi.processInstanceId}
                            } else return {"name": pi.name, "value": pi.value, "processInstanceId": pi.processInstanceId}
                        })
                        var variablesByProcessInstance = _.groupBy(variables, 'processInstanceId');
                        console.log(
                             
                        );
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
                    console.log($scope.tasks);
                    for(let i =0; i<$scope.tasks.length;i++){
                        $http.get(`${$scope.baseUrl}/task/${$scope.tasks[i].id}/identity-links`).then( (response)=> {
                            var groups = response.data
                            var groupList = ''
                            for(let j =0; j<groups.length;j++) {
                                if(groups[j].groupId){
                                    groupList += groups[j].groupId
                                    if(groups.length > 1) {
                                        groupList += ', '
                                    }
                                }
                            }
                            $scope.tasks[i].groupId = groupList
                        })
                    }
                });

            } else {
                $scope.sharingTasks = [
                    {  
                    "title": "Beeline Host - Beeline Site",
                    "pid":"BeelineHostBeelineSite",
                    "tasks" : [
                        'bb_send_avp', 
                        'bb_grant_site_region_leasinggr', 
                        'bb_project_plan_installgr', 
                        'bb_grant_site_accs_ciwill_works',
                        'bb_site_installation_done',
                        'bb_set_cell_data_preparation_status',
                        'bb_configure_site_for_kcell',
                        'bb_test_for_kcell_status',
                        'bb_upload_act_accpt_for_kcell',
                        'bb_check_approval_kcell',
                        'bb_generate_act_of_acceptance_for_print_out',
                        'bb_write_email_rcmu',
                        'bb_accept_rcmu_group',
                        'bb_upload_scan_copy_signed_act_acceptence'
                    ]},
                    {
                    "title": "Beeline Host - Kcell Site",
                    "pid":"BeelineHostKcellSite",
                    "tasks" : [
                        'bk_do_initial_leasing_and_grant_site_access_1',
                        'bk_do_initial_leasing_and_grant_site_access_2',
                        'bk_projectPlanSiteReadySendingToKcell',
                        'bk_approve_by_power_group',
                        'bk_approve_by_transmission_group',
                        'bk_approve_by_planning_group',
                        'bk_approve_by_leasing_group',
                        'bk_approve_by_permit_group',
                        'bk_project_approved',
                        'bk_Update_Transmission_Readiness_Status',
                        'bk_Update_Infrastructure_Revision_JR_Execution_Status',
                        'bk_Set_Installation_Status',
                        'bk_set_cell_Data_Preparation_Status_by_Guest',
                        'bk_configure_site_for_guest_and_update_status',
                        'bk_send_site_for_acceptance_for_guest',
                        'bk_generate_act_for_checking_by_guest',
                        'bk_generate_act_for_print_out_by_guest',
                        'bk_upload_scan_copy_signed_act_acceptence'
                    ]},
                    {
                    "title": "Kcell Host - Beeline Site",
                    "pid":"KcellHostBeelineSite",
                    "tasks" : [
                        'kb_do_initial_leasing_and_grant_site_access_1',
                        'kb_prepare_project_plan',
                        'kb_approve_by_implementation_regional_group',
                        'kb_approve_by_permit_group',
                        'kb_project_plan_for_site_ready_installation_approved',
                        'kb_set_cell_data_preparation_status',
                        'kb_update_transmission_readiness_status',
                        'kb_update_infrastructure_revision_jr_execution_status',
                        'kb_update_rollout_jr_status',
                        'kb_set_cell_data_preparation_status_by_guest',
                        'kb_set_site_integration_for_host_and_guest',
                        'kb_upload_act_of_acceptance_for_guest',
                        'kb_set_accepted_installation_status_by_beeline',
                        'kb_set_accepted_installation_status_by_kcell',
                        'kb_checking_and_approval_by_guest',
                        'kb_generate_act_of_acceptance_for_print_out',
                        'kb_upload_scan_copy_signed_act_acceptance'
                    ]},
                    {
                        "title": "Kcell Host - Kcell Site",
                        "pid":"KcellHostKcellSite",
                        "tasks" : [
                            'kk_do_initial_leasing_and_grant_site_access_2',
                            'kk_do_initial_leasing_and_grant_site_access_1',
                            'kk_prepare_project_plan',
                            'kk_approve_by_power_group',
                            'kk_approve_by_transmission_group',
                            'kk_approve_by_optimization_group',
                            'kk_approve_by_leasing_group',
                            'kk_approve_by_permit_group',
                            'kk_set_cell_data_preparation_status_by_guest',
                            'kk_update_transmission_readiness_status',
                            'kk_update_rollout_jr_status',
                            'kk_update_infrastructure_revision_jr_execution_status',
                            'kk_set_cell_data_preparation_status_by_host',
                            'kk_configure_site_for_host_and_guest',
                            'kk_site_accepted_for_contractor',
                            'kk_upload_act_of_acceptance_for_guest',
                            'kk_checking_and_approval_by_guest_status',
                            'kk_generate_act_for_print_out_by_guest',
                            'kk_upload_scan_copy_signed_act_acceptence'
                        ]
                    },
                    {
                        "title": "Replan Shared Site",
                        "pid":"ReplanSharedSiteAddressPlan",
                        "tasks" : [
                            'rss_task_update_shared_site_address_plan',
                            'rss_task_accept_or_reject_address_plan_modification'
                        ]
                    }
                   
                ];


                
                var processInstances = {};
                var taskInstances = []; 
                for (let sharingPiD of ['BeelineHostBeelineSite', 'BeelineHostKcellSite', 'KcellHostBeelineSite', 'KcellHostKcellSite','ReplanSharedSiteAddressPlan']) {
                    var processInstancesPromise = $http.post($scope.baseUrl + '/process-instance', {
                        "processDefinitionKey": sharingPiD,
                        "unfinished": true
                    }).then(function(response) {
                        if (response.data.length > 0) {
                            var processInstances = _.keyBy(response.data, 'id');
                        
                            return $http.post($scope.baseUrl + '/history/variable-instance/?deserializeValues=false', {
                                variableName: 'sharingPlan',
                                processInstanceIdIn: _.keys(processInstances)
                            }).then(function(response){
                                var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
                                var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
                                var result = _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'sharingPlan': JSON.parse(valueByProcessInstance[id])}));
                                // console.log(result);
                                return result;
                            });
                        } else {
                            return [];
                        };
                        
                    });
                    var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', {
                        "processDefinitionKey": sharingPiD,
                        "unfinished": true
                    }).then(function(response) {
                        return response.data;
                    });

                    $q.all([processInstancesPromise, taskInstancesPromise])
                        .then(function(results) {
                            processInstances = _.assign(processInstances, results[0]);
                            //taskInstances = _.assign(taskInstances, results[1]);
                            taskInstances = _.concat(taskInstances, results[1]);
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
                                                return $scope.getSiteRegion(processInstances[pid].sharingPlan.site_id.toString());
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

                            let a = Object.keys(tasksByIdAndRegionCounted);
                            let newJson = {};
                            for(let i =0; i<a.length;i++){
                                let counter = 0;
                                let b = Object.values(tasksByIdAndRegionCounted[a[i]]);
                                b.forEach(i => {
                                    counter += i;
                                })
                                newJson[a[i]] = counter; 
                            }

                            $scope.totalCounter = newJson;
                            console.log(tasksByIdAndRegionCounted)
                        });
                }
            }
        } else {
            if ($scope.task) {
                var query = {
                    taskDefinitionKey: $scope.task,
                    processDefinitionKey: $scope.getProcessDefinition(),
                    unfinished: true
                };
                if($scope.filter.reason) {
                    if($scope.getProcessDefinition() === 'Revision'){
                        query.processVariables = [{name:'reason', operator:'eq', value:$scope.filter.reason}];
                    } else if($scope.getProcessDefinition() === 'Invoice'){
                        query.processVariables = [{name:'workType', operator:'eq', value:$scope.filter.reason}];
                    }
                }
                if($scope.filter.mainContract && $scope.filter.mainContract !== 'All'){
                    if(query.processVariables){
                        query.processVariables.push({name:'mainContract', operator:'eq', value:$scope.filter.mainContract});
                    } else {
                        query.processVariables = [{name:'mainContract', operator:'eq', value:$scope.filter.mainContract}];
                    }
                }

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
                    for(let i =0; i<$scope.tasks.length;i++){
                        $http.get(`${$scope.baseUrl}/task/${$scope.tasks[i].id}/identity-links`).then( (response)=> {
                            var groups = response.data
                            var groupList = ''
                            for(let j =0; j<groups.length;j++) {
                                if(groups[j].groupId){
                                    groupList += groups[j].groupId
                                    if(groups.length > 1) {
                                        groupList += ', '
                                    }
                                }
                            }
                            $scope.tasks[i].groupId = groupList
                        })
                    }
                });
                

                

            } else if($scope.currentReport) {
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
                        'approve_material_list_region', //approve_material_list_region
                        'approve_material_list_center', //approve_material_list_center
                        'approve_material_list_center1',
                        'validate_tr', //validate_tr
                        'set_materials_dispatch_status', //set_materials_dispatch_status
                        'verify_works', //verify_works
                        'accept_work_initiator', //accept_work_initiator
                        'accept_work_maintenance_group', //accept_work_maintenance_group
                        'accept_work_planning_group', //accept_work_planning_group
                        'sign_region_head', //accept_work_planning_group
                        'attach-scan-copy-of-acceptance-form',
                        'approve_material_list_tnu_region',
                        'validate_tr_bycenter',
                        'approve_additional_material_list_region',
                        'approve_additional_material_list_tnu_region',
                        'approve_additional_material_list_center1',
                        'approve_additional_material_list_center',
                        'validate_additional_tr',
                        'validate_additional_tr_bycenter',
                        'set_additional_materials_dispatch_status'
                    ],
                    'invoice-open-tasks': [
                        'ma_sign_region_head',
                        'ma_sign_region_manager',
                        'ma_sign_central_group_specialist',
                        'ma_print_version',
                        'ma_print_version_tnu',
                        'ma_print_version1',
                        'ma_invoice_number'
                    ]
                };

                $scope.contractorTasks = {
                    'revision-open-tasks':[
                        'upload_tr_contractor', //upload_tr_contractor
                        'upload_additional_tr_contractor',
                        'attach_material_list_contractor', //attach_material_list_contractor
                        'attach_additional_material_list_contractor',
                        'fill_applied_changes_info' //fill_applied_changes_info
                    ],
                    'invoice-open-tasks': [
                        'ma_sendtobranch',
                        'ma_modify'
                    ]
                };

                if ($scope.currentReport === 'revision-open-tasks') {

                    var processQuery = {
                        "processDefinitionKey": "Revision",
                        "unfinished": true,
                        "variables": []
                    };
                    if($scope.filter.reason) {
                        processQuery.variables.push({name:'reason', operator:'eq', value:$scope.filter.reason});
                    }
                    if($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                        processQuery.variables.push({name:'mainContract', operator:'eq', value:$scope.filter.mainContract});
                    }
                    var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', processQuery).then(function(response) {
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

                    var taskQuery = {
                        "processDefinitionKey": 'Revision',
                        "unfinished": true,
                        "processVariables" : []
                    };
                    if($scope.filter.reason) {
                        taskQuery.processVariables.push({name:'reason', operator:'eq', value:$scope.filter.reason});
                    }
                    if($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                        taskQuery.processVariables.push({name:'mainContract', operator:'eq', value:$scope.filter.mainContract});
                    }
                    var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', taskQuery).then(function(response) {
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
                            
                            let a = Object.keys(tasksByIdAndRegionCounted);
                            let newJson = {};
                            for(let i =0; i<a.length;i++){
                                let counter = 0;
                                let b = Object.values(tasksByIdAndRegionCounted[a[i]]);
                                b.forEach(i => {
                                    counter += i;
                                })
                                newJson[a[i]] = counter; 
                            }

                            $scope.totalCounter = newJson;
                        });
                } else if($scope.currentReport === 'invoice-open-tasks'){
                    var processQuery = {
                        "processDefinitionKey": "Invoice",
                        "unfinished": true,
                        "variables": []
                    };
                    if($scope.filter.reason) {
                        processQuery.variables.push({name:'workType', operator:'eq', value:$scope.filter.reason});
                    }
                    if($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                        processQuery.variables.push({name:'mainContract', operator:'eq', value:$scope.filter.mainContract});
                    }

                    var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', processQuery).then(function(response) {
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

                    var taskQuery = {
                        "processDefinitionKey": 'Invoice',
                        "unfinished": true,
                        "processVariables": []
                    };
                    if($scope.filter.reason) {
                        taskQuery.processVariables.push({name:'workType', operator:'eq', value:$scope.filter.reason});
                    }
                    if($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                        taskQuery.processVariables.push({name:'mainContract', operator:'eq', value:$scope.filter.mainContract});
                    }                    

                    var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', taskQuery).then(function(response) {
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
                            console.log(taskInstancesByDefinition)

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

                            let a = Object.keys(tasksByIdAndRegionCounted);
                            let newJson = {};
                            for(let i =0; i<a.length;i++){
                                let counter = 0;
                                let b = Object.values(tasksByIdAndRegionCounted[a[i]]);
                                b.forEach(i => {
                                    counter += i;
                                })
                                newJson[a[i]] = counter; 
                            }

                            $scope.totalCounter = newJson;

                        });
                }
            }

        }

        $scope.getSiteRegion = function (siteID) {
            //console.log(siteID);
            if (siteID) {
                if ( siteID.startsWith('0')) {
                    return 'almaty';
                } else if ( siteID.startsWith('1') || siteID.startsWith('2') ) {
                    if (siteID.startsWith('11')) {
                        return 'astana';
                    } else {
                        return 'north_central';
                    }
                } else if ( siteID.startsWith('3')) {
                    return 'east';
                } else if ( siteID.startsWith('4')) {
                    return 'south';
                } else { 
                    return 'west'; 
                }
            } else {
                return 'no_region';
            }
        };

        $scope.selectReport = function(report){
            $location.url($location.path() + "?report=" + report);
        }

        $scope.selectReason = function(reason){
            var path = $location.path() + "?report=" + $scope.currentReport;
            if($scope.filter.mainContract && $scope.filter.mainContract !== 'All'){
                path = path + '&mainContract=' + $scope.filter.mainContract;
            }
            if(reason == 'all'){
                $location.url(path);
            } else {
                $location.url(path + "&reason=" + reason);
            }
        }

        $scope.selectMainStatus = function(){
            var path = $location.path() + "?report=" + $scope.currentReport;
            if($scope.filter.reason && $scope.filter.reason !== 'all'){
                path = path + "&reason=" + $scope.filter.reason;
            }
            if($scope.filter.mainContract && $scope.filter.mainContract !== 'all'){
                $location.url(path + "&mainContract=" + $scope.filter.mainContract);
            }
        }

        $scope.downloadTechnicalReport = function(){
            if($rootScope.hasGroup('revision_reports') || 'Evgeniy.Semenov@kcell.kz' == $rootScope.authentication.name){
                $http.get('/camunda/reports/report').then(function(response) {
                    var data = response.data;

                    angular.forEach(data, function(d){
                        d[6] =  $filter('date')(d[6], "yyyy-MM-dd");
                        d[8] =  $filter('date')(d[8], "yyyy-MM-dd");
                        d[9] =  $filter('date')(d[9], "yyyy-MM-dd");
                        d[10] =  $filter('date')(d[10], "yyyy-MM-dd");
                        d[11] =  $filter('date')(d[11], "yyyy-MM-dd");
                        d[12] =  $filter('date')(d[12], "yyyy-MM-dd");
                        d[13] =  $filter('date')(d[13], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Material List Signing Date", "Accept by Initiator"
                        , "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity", "Comments", "Customer Material"
                        , "Process State", "JR Status", "Detailed status", "Reason"]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader:true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'technical-report-by-works.xlsx');
                });
            }
        }

        $scope.downloadTechnicalByJobsReport = function(){
            if($rootScope.hasGroup('revision_reports') || 'Evgeniy.Semenov@kcell.kz' == $rootScope.authentication.name){
                $http.get('/camunda/reports/technical-report-by-jobs').then(function(response) {
                    var data = response.data;

                    angular.forEach(data, function(d){
                        d[6] =  $filter('date')(d[6], "yyyy-MM-dd");
                        d[8] =  $filter('date')(d[8], "yyyy-MM-dd");
                        d[9] =  $filter('date')(d[9], "yyyy-MM-dd");
                        d[10] =  $filter('date')(d[10], "yyyy-MM-dd");
                        d[11] =  $filter('date')(d[11], "yyyy-MM-dd");
                        d[12] =  $filter('date')(d[12], "yyyy-MM-dd");
                        d[13] =  $filter('date')(d[13], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Material List Signing Date", "Accept by Initiator"
                        , "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity", "Comments", "Customer Material"
                        , "Process State", "JR Status", "Detailed status", "Reason"]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader:true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'technical-report-by-jobs.xlsx');
                });
            }
        }

        $scope.downloadFinancialReport = function(){
            if($rootScope.hasGroup('revision_reports')){
                $http.get('/camunda/reports/financialreport').then(function(response) {
                    var data = response.data;

                    angular.forEach(data, function(d){
                        d[8] =  $filter('date')(d[8], "yyyy-MM-dd");
                        d[10] =  $filter('date')(d[10], "yyyy-MM-dd");
                        d[13] =  $filter('date')(d[13], "yyyy-MM-dd");
                        d[14] =  $filter('date')(d[14], "yyyy-MM-dd");
                        d[15] =  $filter('date')(d[15], "yyyy-MM-dd");
                        d[16] =  $filter('date')(d[16], "yyyy-MM-dd");
                        d[17] =  $filter('date')(d[17], "yyyy-MM-dd");
                        d[35] =  $filter('date')(d[35], "yyyy-MM-dd");
                        d[38] =  $filter('date')(d[38], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Year", "Month", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Related to the", "Project"
                        , "Material List Signing Date", "Accept by Initiator", "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity"
                        , "Job reason", "Type of expenses", "Comments", "Customer Material", "Process State", "JR Status", "Detailed status", "Reason", "Price (without transportation)"
                        , "Price (with transportation)", "Monthly act #", "JO#", "PR#", "PR Total Value", "PR Status", "PR Approval date", "PO#", "Invoice No", "Invoice date"
                        ]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader:true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'extended-report-by-works.xlsx');
                });
            }
        }
        $scope.downloadExtendedByJobsReport = function(){
            if($rootScope.hasGroup('revision_reports')){
                $http.get('/camunda/reports/extended-report-by-jobs').then(function(response) {
                    var data = response.data;

                    angular.forEach(data, function(d){
                        d[8] =  $filter('date')(d[8], "yyyy-MM-dd");
                        d[10] =  $filter('date')(d[10], "yyyy-MM-dd");
                        d[13] =  $filter('date')(d[13], "yyyy-MM-dd");
                        d[14] =  $filter('date')(d[14], "yyyy-MM-dd");
                        d[15] =  $filter('date')(d[15], "yyyy-MM-dd");
                        d[16] =  $filter('date')(d[16], "yyyy-MM-dd");
                        d[17] =  $filter('date')(d[17], "yyyy-MM-dd");
                        d[35] =  $filter('date')(d[35], "yyyy-MM-dd");
                        //d[34] =  $filter('date')(d[34], "yyyy-MM-dd");
                        d[38] =  $filter('date')(d[38], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Year", "Month", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Related to the", "Project"
                        , "Material List Signing Date", "Accept by Initiator", "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity"
                        , "Job reason", "Type of expenses", "Comments", "Customer Material", "Process State", "JR Status", "Detailed status", "Reason", "Price (without transportation)"
                        , "Price (with transportation)", "Monthly act #", "JO#", "PR#", "PR Total Value", "PR Status", "PR Approval date", "PO#", "Invoice No", "Invoice date"
                        ]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader:true});
                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                    return XLSX.writeFile(wb, 'extended-report-by-jobs.xlsx');
                });
            }
        }
	}]);
});
