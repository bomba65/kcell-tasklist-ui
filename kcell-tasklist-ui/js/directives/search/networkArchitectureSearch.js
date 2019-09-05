define(['../module', 'moment'], function (module, moment) {
    'use strict';
    module.directive('networkArchitectureSearch', ['$http', '$timeout', '$q', '$rootScope', 'exModal', function ($http, $timeout, $q, $rootScope, exModal) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                var baseUrl = '/camunda/api/engine/engine/default';
                var catalogs = {};
                $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
                    function (result) {
                        angular.extend(scope, result.data);
                        angular.extend(catalogs, result.data);

                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                var processList = [];
                $http.get('/camunda/api/engine/engine/default/process-definition?latest=true').then(
                    function (response) {
                        angular.forEach(response.data, function (value) {
                            processList.push(value.key);
                        });
                    }
                );
                var allKWMSProcesses = {
                    Revision: {
                        title: "Revision", value: false
                    },
                    Invoice: {
                        title: "Monthly Act", value: false
                    },
                    Dismantle: {
                        title: "Dismantle", value: false
                    },
                    Replacement: {
                        title: "Replacement", value: false
                    }
                };
                scope.KWMSProcesses = {};
                scope.regionsMap = {
                    'alm': 'Almaty',
                    'astana': 'Astana',
                    'nc': 'North & Center',
                    'east': 'East',
                    'south': 'South',
                    'west': 'West'
                };
                scope.contractorShortName = {
                    '4': 'LSE'
                };
                scope.reasonShortName = {
                    '1': 'P&O',
                    '2': 'TNU',
                    '3': 'S&FM',
                    '4': 'SAO'
                };
                scope.processInstancesTotal = 0;
                scope.processInstancesPages = 0;
                scope.selectedProcessInstances = [];
                scope.shownPages = 0;
                scope.xlsxPreparedRevision = false;
                //scope.daWtf = 'dfdsfds';
                scope.onlyProcessActive = '';

                scope.taskUserSelected = function ($item, $model, $label) {
                    //scope.initiator = $item;
                    //scope.initiatorId = $item.id;
                };
                var KWMSproject = _.find($rootScope.projects, {'key': 'NetworkInfrastructure'});
                angular.forEach(allKWMSProcesses, function (value, processKey) {
                    if (_.find(KWMSproject.processes, {'key': processKey})) {
                        scope.KWMSProcesses[processKey] = value;
                    }
                });
                function isEmpty(obj) {
                    return Object.keys(obj).length === 0;
                }

                scope.KWMSProcessesCount = Object.keys(scope.KWMSProcesses).length;
                if (scope.KWMSProcessesCount === 1) {
                    angular.forEach(scope.KWMSProcesses, function (value, processKey) {
                        value.value = true;
                    });
                }
                function noProcessSelection(newVal) {
                    var filtered = Object.fromEntries(Object.entries(newVal).filter(([k, v]) => v.value === false));
                    if ((filtered.Revision || filtered.Invoice) && !filtered.leasing && !filtered.Dismantle && !filtered.Replacement) {
                        scope.RevisionOrMonthlyAct = true;
                    }
                    if (Object.keys(filtered).length === 1) {
                        scope.onlyProcessActive = Object.keys(filtered)[0];
                    }
                }
                scope.$watch('KWMSProcesses', function (newVal, oldVal) {
                    scope.selectedProcessInstances = [];
                    scope.RevisionOrMonthlyAct = false;
                    var filtered = Object.fromEntries(Object.entries(newVal).filter(([k, v]) => v.value === true));
                    scope.onlyProcessActive = '';
                    if (isEmpty(filtered)) {
                        angular.forEach(newVal, function (process, key) {
                            scope.selectedProcessInstances.push(key);
                        });
                        noProcessSelection(newVal);
                    } else {
                        if (Object.keys(filtered).length === 1) {
                            // only one process active;
                            scope.onlyProcessActive = Object.keys(filtered)[0];
                        }
                        if ((filtered.Revision || filtered.Invoice) && !filtered.Dismantle && !filtered.Replacement) {
                            scope.RevisionOrMonthlyAct = true;
                        }
                        angular.forEach(filtered, function (process, key) {
                             scope.selectedProcessInstances.push(key);
                        });
                    }
                    if (scope.onlyProcessActive!=='Revision') {
                        //clear Revision-only filters
                        scope.filter.validityDateRange = undefined;
                        $(".calendar-range-readonly").each(function () {
                            if ($(this).data('daterangepicker')) {
                                $(this).data('daterangepicker').setStartDate(moment());
                                $(this).data('daterangepicker').setEndDate(moment());
                            }
                        });
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.priority = undefined;
                        scope.filter.activityId = undefined;
                        scope.filter.workType = undefined;
                        scope.filter.workName = undefined;
                        scope.filter.mainContract = 'All';
                        scope.filter.participation = undefined;
                        scope.filter.currentAssignee = undefined;
                    }
                    if (scope.onlyProcessActive!=='Dismantle') {
                        scope.filter.dismantlingInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive!=='Replacement') {
                        scope.filter.replacementInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive!=='Revision' && scope.onlyProcessActive!=='Dismantle' && scope.onlyProcessActive!=='Replacement') {
                        scope.filter.requestedDateRange = undefined;
                        scope.filter.requestor = undefined;
                    }
                    if (scope.onlyProcessActive!=='Invoice') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive==='Invoice') {
                        // siteId&sitename are common filters except if Invoice selected
                        scope.filter.siteId = undefined;
                        scope.filter.sitename = undefined;
                    }
                }, true);

                var regionGroupsMap = {
                    'alm_kcell_users': 'alm',
                    'astana_kcell_users': 'astana',
                    'nc_kcell_users': 'nc',
                    'east_kcell_users': 'east',
                    'south_kcell_users': 'south',
                    'west_kcell_users': 'west'
                }
                function downloadXML(process){
                    $http.get(baseUrl + '/process-definition/key/' + process + '/xml')
                    .then(function (response) {
                        var domParser = new DOMParser();

                        var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

                        function getUserTasks(xml) {
                            var namespaces = {
                                bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
                            };

                            var userTaskNodes = [
                                ...getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]) ,
                                ...getElementsByXPath(xml, '//bpmn:intermediateCatchEvent', prefix => namespaces[prefix])
                            ];

                            function getElementsByXPath(doc, xpath, namespaceFn, parent) {
                                let results = [];
                                let query = doc.evaluate(xpath,
                                    parent || doc,
                                    namespaceFn,
                                    XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
                                for (let i = 0, length = query.snapshotLength; i < length; ++i) {
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
                                    "id": id,
                                    "name": name,
                                    "description": description
                                };
                            });
                        }

                        if(process === 'Revision'){
                            var excludeTasks = [
                                'signpr_by_center',
                                'signpr_by_manager',
                                'signpr_by_budgetowner',
                                'signpr_by_cto',
                                'signpr_by_cfo',
                                'signpr_by_ceo',
                                'Task_1ix12n7',
                                'Task_1uvnb7n',
                                'Task_12eq7hi',
                                'Task_1wf6n5j',
                                'Task_1puv0a9',
                                'Task_1m2xspc',
                                'Task_1mb15j2',
                                'Task_1mocj2s',
                                'Task_1gjdn28',
                                'IntermediateThrowEvent_wait_po_pr_creation'
                            ];

                            var userTasks = getUserTasks(xml);
                            var includedUserTasks = _.filter(userTasks, function (task) {
                                return excludeTasks.indexOf(task.id) === -1;
                            });
                            scope.includedUserTasks = includedUserTasks;
                            var userTasksMap = _.keyBy(includedUserTasks, 'id');
                            scope.userTasksMap = userTasksMap;                            
                        } else if(process === 'sdr_srr_request') {
                            scope.dismantleUserTasks = getUserTasks(xml);
                        }
                    });
                }                

                scope.filter = {
                    businessKeyFilterType: 'eq',
                    unfinished: false,
                    page: 1,
                    maxResults: 20
                };
                scope.processInstances = [];
                scope.currentDate = new Date();
                scope.filter.beginYear = scope.currentDate.getFullYear() - 1;
                scope.filter.endYear = scope.currentDate.getFullYear();
                scope.years = [];
                var currentDate = new Date;
                scope.InvoicePeriodYears = [currentDate.getFullYear(), currentDate.getFullYear()-1, currentDate.getFullYear()-2];
                scope.InvoicePeriodMonths = [
                    {id: 'jan', label: 'January', order: '1'},
                    {id: 'feb', label: 'February', order: '2'},
                    {id: 'mar', label: 'March', order: '3'},
                    {id: 'apr', label: 'April', order: '4'},
                    {id: 'may', label: 'May', order: '5'},
                    {id: 'jun', label: 'June', order: '6'},
                    {id: 'jul', label: 'July', order: '7'},
                    {id: 'aug', label: 'August', order: '8'},
                    {id: 'sep', label: 'September', order: '9'},
                    {id: 'oct', label: 'October', order: '10'},
                    {id: 'nov', label: 'November', order: '11'},
                    {id: 'dec', label: 'December', order: '12'}
                ];
                for (var year = 2017; year <= scope.filter.endYear; year++) {
                    scope.years.push(year);
                }
                scope.siteSelected = function ($item) {
                    scope.filter.sitename = $item.site_name;
                };

                scope.siteIdSelected = function ($item) {
                    scope.filter.siteId = $item.name;
                };

                scope.getSite = function (val) {
                    return $http.get('/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name=' + val).then(
                        function (response) {
                            var sites = _.flatMap(response.data._embedded.sites, function (s) {
                                if (s.params.site_name) {
                                    return s.params.site_name.split(',').map(function (sitename) {
                                        return {
                                            name: s.name,
                                            id: s._links.self.href.substring(s._links.self.href.lastIndexOf('/') + 1),
                                            site_name: sitename
                                        };
                                    })
                                } else {
                                    return [];
                                }
                            });
                            return sites;
                        }
                    );
                };
                scope.getSiteId = function (val) {
                    return $http.get('/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name=' + val).then(
                        function (response) {
                            var sites = _.flatMap(response.data._embedded.sites, function (s) {
                                if (s.name) {
                                    return {
                                        name: s.name,
                                        id: s._links.self.href.substring(s._links.self.href.lastIndexOf('/') + 1)
                                    };
                                } else {
                                    return [];
                                }
                            });
                            return sites;
                        }
                    );
                };
                scope.getXlsxProcessInstances = function () {
                    var fileName = scope.onlyProcessActive.toLowerCase() + '-search-result.xlsx';
                    if (scope.xlsxPreparedRevision) {
                        var tbl = document.getElementById('xlsxRevisionsTable');
                        var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});
                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                        return XLSX.writeFile(wb, fileName);
                    } else {
                        getProcessInstances(scope.lastSearchParamsRevision, 'xlsxProcessInstances');
                        scope.xlsxPreparedRevision = true;
                    }

                }
                scope.search = function (refreshPages) {
                    var asynCall1 = false;
                    var asynCall2 = false;
                    var asynCall3 = false;
                    var asynCall4 = false;
                    if (refreshPages) {
                        scope.filter.page = 1;
                        scope.piIndex = undefined;
                        scope.xlsxPreparedRevision = false;
                    }
                    var selectedProcessInstances = [];
                    angular.forEach(scope.selectedProcessInstances, function(item){
                        if (item === 'Dismantle' || item === 'Replacement') {
                            if(selectedProcessInstances.indexOf('sdr_srr_request')===-1){
                                selectedProcessInstances.push('sdr_srr_request')
                            }
                        } else {
                            selectedProcessInstances.push(item);
                        }
                    });

                    var excludeProcesses = processList.filter(function (item) {
                        return selectedProcessInstances.indexOf(item) === -1;
                    });

                    var filter = {
                        sorting: [{sortBy: "startTime", sortOrder: "desc"}],
                        variables: [],
                        activeActivityIdIn : [],
                        processInstanceBusinessKeyLike: '%-%',
                        processDefinitionKeyNotIn: excludeProcesses

                    }
                    if(scope.selectedProcessInstances.indexOf('Dismantle')!==-1 && scope.selectedProcessInstances.indexOf('Replacement')===-1){
                        filter.variables.push({"name": "requestType", "operator": "eq", "value": "dismantle"});
                    }
                    if(scope.selectedProcessInstances.indexOf('Replacement')!==-1 && scope.selectedProcessInstances.indexOf('Dismantle')===-1){
                        filter.variables.push({"name": "requestType", "operator": "eq", "value": "replacement"});
                    }
                    if (scope.filter.unfinished) {
                        filter.unfinished = true;
                    }
                    if (scope.filter.finished) {
                        filter.finished = true;
                    }
                    if (scope.filter.region && scope.filter.region !== 'all') {
                        filter.variables.push({"name": "siteRegion", "operator": "eq", "value": scope.filter.region});
                    }
                    if (scope.filter.siteId) {
                        filter.variables.push({"name": "siteName", "operator": "eq", "value": scope.filter.siteId});
                    }
                    if (scope.filter.sitename) {
                        filter.variables.push({"name": "site_name", "operator": "eq", "value": scope.filter.sitename});
                    }
                    if (scope.filter.businessKey) {
                        if (scope.filter.businessKeyFilterType === 'eq') {
                            filter.processInstanceBusinessKey = scope.filter.businessKey;
                        } else {
                            filter.processInstanceBusinessKeyLike = '%'+scope.filter.businessKey+'%';
                        }
                    }
                    if (scope.filter.workType) {
                        if (scope.onlyProcessActive==='Invoice')
                            filter.variables.push({"name": "workType", "operator": "eq", "value": scope.filter.workType});
                        else if (scope.onlyProcessActive==='Revision')
                            filter.variables.push({"name": "reason", "operator": "eq", "value": scope.filter.workType});
                    }
                    if (scope.filter.unfinished) {
                        filter.unfinished = true;
                    } else {
                        delete filter.unfinished;
                    }
                    if (scope.filter.beginYear) {
                        filter.startedAfter = scope.filter.beginYear + '-01-01T00:00:00.000+0600';
                    }
                    if (scope.filter.endYear) {
                        filter.startedBefore = (Number(scope.filter.endYear) + 1) + '-01-01T00:00:00.000+0600';
                    }
                    if (scope.filter.currentAssignee) {
                        var activityParams = {
                            activityType: 'userTask',
                            taskAssignee: scope.filter.currentAssignee.id,
                            unfinished: true,
                        };
                        $http({
                            method: 'POST',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            data: activityParams,
                            url: baseUrl + '/history/activity-instance'
                        }).then(
                            function(tasks){
                                if (!filter.processInstanceIds) filter.processInstanceIds = _.map(tasks.data, 'processInstanceId');
                                else filter.processInstanceIds = filter.processInstanceIds.filter(value => -1 !== _.map(tasks.data, 'processInstanceId').indexOf(value));

                                asynCall1 = true;
                                if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                                    scope.lastSearchParamsRevision = filter;
                                    getProcessInstances(filter, 'processInstances');
                                    asynCall1 = false;
                                }
                            },
                            function(error){
                                console.log(error.data);
                            }
                        );

                    } else {
                        asynCall1 = true;
                    }
                    if (scope.filter.monthOfFormalPeriod && scope.filter.yearOfFormalPeriod) {
                        filter.variables.push({
                            "name": "monthOfFormalPeriod",
                            "operator": "eq",
                            "value": scope.filter.monthOfFormalPeriod
                        });
                        filter.variables.push({
                            "name": "yearOfFormalPeriod",
                            "operator": "eq",
                            "value": scope.filter.yearOfFormalPeriod
                        });
                    }

                    if (scope.filter.requestedDateRange) {
                        var results = scope.convertStringToDate(scope.filter.requestedDateRange);
                        if(scope.onlyProcessActive==='Revision'){
                            if (results.length === 2) {
                                filter.variables.push({
                                    "name": "requestedDate",
                                    "operator": "gteq",
                                    "value": results[0]
                                });
                                filter.variables.push({
                                    "name": "requestedDate",
                                    "operator": "lteq",
                                    "value": results[1]
                                });
                            }                            
                        } else if(scope.onlyProcessActive==='Dismantle'){
                            if (results.length === 2) {
                                filter.variables.push({
                                    "name": "sdrCreationDate",
                                    "operator": "gteq",
                                    "value": results[0]
                                });
                                filter.variables.push({
                                    "name": "sdrCreationDate",
                                    "operator": "lteq",
                                    "value": results[1]
                                });
                            }
                        } else if(scope.onlyProcessActive==='Replacement'){
                            if (results.length === 2) {
                                filter.variables.push({
                                    "name": "srrCreationDate",
                                    "operator": "gteq",
                                    "value": results[0]
                                });
                                filter.variables.push({
                                    "name": "srrCreationDate",
                                    "operator": "lteq",
                                    "value": results[1]
                                });
                            }
                        }
                    }
                    if (scope.filter.validityDateRange) {
                        var results = scope.convertStringToDate(scope.filter.validityDateRange);
                        if (results.length === 2) {
                            filter.variables.push({
                                "name": "validityDate",
                                "operator": "gteq",
                                "value": results[0]
                            });

                            filter.variables.push({
                                "name": "validityDate",
                                "operator": "lteq",
                                "value": results[1]
                            });
                        }
                    }
                    if (scope.filter.priority) {
                        filter.variables.push({"name": "priority", "operator": "eq", "value": scope.filter.priority});
                    }
                    if (scope.filter.workName) {
                        filter.variables.push({
                            "name": "workTitlesForSearch",
                            "operator": "like",
                            "value": "%" + scope.filter.workName + "%"
                        });
                    }
                    if (scope.filter.dismantlingInitiator) {
                        filter.variables.push({
                            "name": "dismantlingInitiator",
                            "operator": "eq",
                            "value": scope.filter.dismantlingInitiator
                        });
                    }
                    if (scope.filter.replacementInitiator) {
                        filter.variables.push({
                            "name": "replacementInitiator",
                            "operator": "eq",
                            "value": scope.filter.replacementInitiator
                        });
                    }
                    if (scope.filter.requestor) {
                        if (scope.filter.participation === 'participant') {
                            $http.post(baseUrl + '/history/task', {taskAssignee: scope.filter.requestor}).then(
                                function (result) {
                                    if (!filter.processInstanceIds) filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                                    else filter.processInstanceIds = filter.processInstanceIds.filter(value => -1 !== _.map(result.data, 'processInstanceId').indexOf(value));
                                    asynCall4 = true;
                                    if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                                        scope.lastSearchParamsRevision = filter;
                                        getProcessInstances(filter, 'processInstances');
                                        asynCall4 = false;
                                    }
                                },
                                function (error) {
                                    console.log(error.data)
                                }
                            );
                        } else {
                            filter.startedBy = scope.filter.requestor;
                            asynCall4 = true;
                        }
                    } else {
                        asynCall4 = true;
                    }

                    if (scope.filter.initiator) {
                        $http.post(baseUrl + '/history/task', {taskAssignee: scope.filter.initiator.id}).then(
                            function (result) {
                                filter.startedBy = scope.filter.initiator.id;
                                scope.lastSearchParamsRevision = filter;
                                asynCall2 = true;
                                if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                                    scope.lastSearchParamsRevision = filter;
                                    getProcessInstances(filter, 'processInstances');
                                    asynCall2 = false;
                                }
                            },
                            function (error) {
                                console.log(error.data)
                            }
                        );
                    } else {
                        asynCall2 = true;
                    }


                    if (scope.filter.activityId && scope.onlyProcessActive==='Revision') {
                        filter.activeActivityIdIn.push(scope.filter.activityId);
                    }
                    if (scope.filter.dismantleActivityId && (scope.onlyProcessActive==='Dismantle' || scope.onlyProcessActive==='Replacement')) {
                        filter.activeActivityIdIn.push(scope.filter.dismantleActivityId);
                    }
                    if (scope.filter.mainContract && scope.filter.mainContract !== 'All') {
                        filter.variables.push({
                            "name": "mainContract",
                            "operator": "eq",
                            "value": scope.filter.mainContract
                        });
                    }
                    asynCall3 = true;
                    if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                        scope.lastSearchParamsRevision = filter;
                        getProcessInstances(filter, 'processInstances');
                        asynCall3 = false;
                    }
                };

                scope.clearFilters = function () {
                    scope.filter.region = 'all';
                    scope.filter.initiator = undefined;
                    scope.filter.period = undefined;
                    scope.filter.siteId = undefined;
                    scope.filter.sitename = undefined;
                    scope.filter.businessKey = undefined;
                    scope.filter.workType = undefined;
                    scope.filter.participation = undefined;
                    scope.filter.currentAssignee = undefined;
                    scope.filter.beginYear = scope.currentDate.getFullYear();
                    scope.filter.endYear = scope.currentDate.getFullYear();
                    scope.filter.requestedDateRange = undefined;
                    scope.filter.validityDateRange = undefined;
                    $(".calendar-range-readonly").each(function () {
                        $(this).data('daterangepicker').setStartDate(moment());
                        $(this).data('daterangepicker').setEndDate(moment());
                    });
                    scope.filter.requestor = undefined;
                    scope.filter.sitename = undefined;
                    scope.filter.priority = undefined;
                    scope.filter.activityId = undefined;
                    scope.filter.workName = undefined;
                    scope.filter.businessKeyFilterType = 'eq';
                    scope.filter.unfinished = false;
                    scope.filter.finished = false;
                    scope.filter.mainContract = 'All';
                    scope.filter.monthOfFormalPeriod = undefined;
                    scope.filter.yearOfFormalPeriod = undefined;
                }

                function getProcessInstances(filter, processInstances) {
                    console.log('filter params', filter);
                    $http({
                        method: 'POST',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        data: filter,
                        url: baseUrl + '/history/process-instance/count'
                    }).then(
                        function (result) {
                            scope[processInstances + 'Total'] = result.data.count;
                            scope[processInstances + 'Pages'] = Math.floor(result.data.count / scope.filter.maxResults) + ((result.data.count % scope.filter.maxResults) > 0 ? 1 : 0);
                        },
                        function (error) {
                            console.log(error.data);
                            scope[processInstances + 'Total'] = 0;
                            scope[processInstances + 'Pages'] = 0;
                        });
                    $http({
                        method: 'POST',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        data: filter,
                        url: baseUrl + '/history/process-instance?firstResult=' + (processInstances === 'processInstances' ? (scope.filter.page - 1) * scope.filter.maxResults + '&maxResults=' + scope.filter.maxResults : '')
                    }).then(
                        function (result) {
                            scope[processInstances] = result.data;
                            var variables = ['siteRegion', 'site_name', 'contractor', 'reason', 'requestedDate', 'validityDate', 'jobWorks', 'explanation', 'workType'];

                            if(scope.selectedProcessInstances.indexOf('Dismantle')!==-1 || scope.selectedProcessInstances.indexOf('Replacement')!==-1){
                                variables.push('requestType');
                            }

                            if (scope[processInstances].length > 0) {
                                angular.forEach(scope[processInstances], function (el) {
                                    if (el.durationInMillis) {
                                        el['executionTime'] = Math.floor(el.durationInMillis / (1000 * 60 * 60 * 24));
                                    } else {
                                        var startTime = new Date(el.startTime);
                                        el['executionTime'] = Math.floor(((new Date().getTime()) - startTime.getTime()) / (1000 * 60 * 60 * 24));
                                    }
                                    if (!scope.profiles[el.startUserId]) {
                                        $http.get(baseUrl + '/user/' + el.startUserId + '/profile').then(
                                            function (result) {
                                                scope.profiles[el.startUserId] = result.data;
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );
                                    }
                                });

                                _.forEach(variables, function (variable) {
                                    var varSearchParams = {
                                        processInstanceIdIn: _.map(scope[processInstances], 'id'),
                                        variableName: variable
                                    };
                                    $http({
                                        method: 'POST',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        data: varSearchParams,
                                        url: baseUrl + '/history/variable-instance?deserializeValues=false'
                                    }).then(
                                        function (vars) {
                                            scope[processInstances].forEach(function (el) {
                                                var f = _.filter(vars.data, function (v) {
                                                    return v.processInstanceId === el.id;
                                                });
                                                if (f && f[0]) {
                                                    if (f[0].type === 'Json') {
                                                        el[variable] = JSON.parse(f[0].value);
                                                    } else {
                                                        el[variable] = f[0].value;
                                                    }
                                                }
                                            });
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );
                                });
                                var activeProcessInstances = _.filter(scope[processInstances], function (pi) {
                                    return pi.state === 'ACTIVE';
                                });
                                var taskSearchParams = {
                                    processInstanceBusinessKeyIn: _.map(activeProcessInstances, 'businessKey'),
                                    active: true
                                };
                                $http({
                                    method: 'POST',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    data: taskSearchParams,
                                    url: baseUrl + '/task'
                                }).then(
                                    function (tasks) {
                                        angular.forEach(scope[processInstances], function (el) {
                                            var f = _.filter(tasks.data, function (t) {
                                                return t.processInstanceId === el.id;
                                            });                                            
                                            if (f && f.length > 0) {
                                                el['tasks'] = f;
                                                _.forEach(el.tasks, function (task) {
                                                    if (task.assignee && !scope.profiles[task.assignee]) {
                                                        $http.get(baseUrl + '/user/' + task.assignee + '/profile').then(
                                                            function (result) {
                                                                scope.profiles[task.assignee] = result.data;
                                                            },
                                                            function (error) {
                                                                console.log(error.data);
                                                            }
                                                        );
                                                    }
                                                });
                                            }
                                        });
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );
                                angular.forEach(activeProcessInstances, function (pi) {
                                    $http.get(baseUrl + '/process-instance/' + pi.id + '/activity-instances').then(
                                        function (result) {
                                            pi.otherActivities = [];
                                            _.forEach(result.data.childActivityInstances, function (firstLevel) {
                                                if (firstLevel.activityType === 'subProcess') {
                                                    _.forEach(firstLevel.childActivityInstances, function (secondLevel) {
                                                        if (secondLevel.activityType !== 'userTask' && secondLevel.activityType !== 'multiInstanceBody') {
                                                            pi.otherActivities.push(secondLevel);
                                                        }
                                                    });
                                                } else if (firstLevel.activityType !== 'userTask' && firstLevel.activityType !== 'multiInstanceBody') {
                                                    pi.otherActivities.push(firstLevel);
                                                }
                                            });
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );
                                });
                            }
                        },
                        function (error) {
                            console.log(error.data);
                            scope[processInstances] = [];
                        });
                }

                scope.getPageInstances = function() {
                    if (scope.processInstances.length !== 0) {
                        return scope.processInstances.slice(
                            (scope.filter.page - 1) * scope.filter.maxResults,
                            scope.filter.page * scope.filter.maxResults
                        );
                    }
                    return [];
                }
                scope.nextPage = function () {
                    scope.filter.page++;
                    scope.search(false);
                    scope.piIndex = undefined;
                }

                scope.prevPage = function () {
                    scope.filter.page--;
                    scope.search(false);
                    scope.piIndex = undefined;
                }

                scope.selectPage = function (page) {
                    scope.filter.page = page;
                    scope.search(false);
                    scope.piIndex = undefined;
                }

                scope.getPages = function () {
                    var array = [];
                    if (scope.processInstancesPages < 8) {
                        for (var i = 1; i <= scope.processInstancesPages; i++) {
                            array.push(i);
                        }
                    } else {
                        var decrease = scope.filter.page - 1;
                        var increase = scope.filter.page + 1;
                        array.push(scope.filter.page);
                        while (increase - decrease < 8) {
                            if (decrease > 0) {
                                array.unshift(decrease--);
                            }
                            if (increase < scope.processInstancesPages) {
                                array.push(increase++);
                            }
                        }
                    }
                    return array;
                }
                scope.toggleProcess = function (process) {
                    if (scope.KWMSProcesses[process].value) {
                        scope.KWMSProcesses[process].value = false;
                    } else {
                        scope.KWMSProcesses[process].value = true;
                        if(process === 'Revision' && !scope.KWMSProcesses[process].downloaded){
                            downloadXML('Revision');
                            scope.KWMSProcesses[process].downloaded = true;
                        } else if((process === 'Dismantle' || process === 'Replacement') && !scope.KWMSProcesses[process].downloaded){
                            downloadXML('sdr_srr_request');
                            scope.KWMSProcesses[process].downloaded = true;
                        }
                    }
                };
                scope.toggleProcessViewRevision = function (index, processDefinitionKey, processDefinitionId, businessKey) {
                    scope.showDiagramView = false;
                    scope.diagram = {};
                    if (scope.piIndex === index) {
                        scope.piIndex = undefined;
                    } else {
                        scope.piIndex = index;
                        scope.jobModel = {
                            state: scope.processInstances[index].state,
                            processDefinitionKey: processDefinitionKey,
                            startTime: {value: scope.processInstances[index].startTime}
                        };
                        $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: baseUrl + '/task?processInstanceId=' + scope.processInstances[index].id,
                        }).then(
                            function (tasks) {
                                var asynCall1 = false;
                                var asynCall2 = false;
                                var asynCall3 = false;
                                var processInstanceTasks = tasks.data._embedded.task;
                                if (processInstanceTasks && processInstanceTasks.length > 0) {
                                    var groupasynCalls = 0;
                                    var maxGroupAsynCalls = processInstanceTasks.length;
                                    processInstanceTasks.forEach(function (e) {
                                        if (e.assignee && tasks.data._embedded.assignee) {
                                            for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
                                                if (tasks.data._embedded.assignee[i].id === e.assignee) {
                                                    e.assigneeObject = tasks.data._embedded.assignee[i];
                                                }
                                            }
                                        }
                                        $http({
                                            method: 'GET',
                                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                            url: baseUrl + '/task/' + e.id
                                        }).then(
                                            function (taskResult) {
                                                if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                                    e.group = taskResult.data._embedded.group[0].id;
                                                    groupasynCalls += 1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        } else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    console.log('vtoroi', groupasynCalls, maxGroupAsynCalls);
                                                    groupasynCalls += 1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        } else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                }
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );

                                    });

                                } else {
                                    asynCall1 = true;
                                    if (asynCall1 && asynCall2) {
                                        openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                        asynCall1 = false;
                                    }
                                }
                                $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstances[index].id).then(
                                    function (result) {
                                        var workFiles = [];
                                        result.data.forEach(function (el) {
                                            scope.jobModel[el.name] = el;
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                scope.jobModel[el.name].value = JSON.parse(el.value);
                                            }
                                            if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                workFiles.push(el);
                                            }
                                        });
                                        if (scope.jobModel['siteWorksFiles']) {
                                            _.forEach(scope.jobModel['siteWorksFiles'].value, function (file) {
                                                var workIndex = file.name.split('_')[1];
                                                if (!scope.jobModel.jobWorks.value[workIndex].files) {
                                                    scope.jobModel.jobWorks.value[workIndex].files = [];
                                                }
                                                if (_.findIndex(scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                                    return f.name == file.name;
                                                }) < 0) {
                                                    scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                }
                                            });
                                        }
                                        _.forEach(workFiles, function (file) {
                                            var workIndex = file.name.split('_')[1];
                                            if (!scope.jobModel.jobWorks.value[workIndex].files) {
                                                scope.jobModel.jobWorks.value[workIndex].files = [];
                                            }
                                            if (_.findIndex(scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                                return f.name == file.name;
                                            }) < 0) {
                                                scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                            }
                                        });
                                        if (scope.jobModel.resolutions && scope.jobModel.resolutions.value) {
                                            $q.all(scope.jobModel.resolutions.value.map(function (resolution) {
                                                return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                            })).then(function (tasks) {
                                                tasks.forEach(function (e, index) {
                                                    if (e.data.length > 0) {
                                                        scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                        try {
                                                            scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                        } catch (e) {
                                                            console.log(e);
                                                        }
                                                    }
                                                });
                                                asynCall2 = true;
                                                if (asynCall1 && asynCall2) {
                                                    openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                    asynCall2 = false;
                                                } else console.log('aynCall 1 problem');
                                            });
                                        }

                                        //scope.jobModel.tasks = processInstanceTasks;
                                        angular.extend(scope.jobModel, catalogs);
                                        scope.jobModel.tasks = processInstanceTasks;


                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );

                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }
                };

                function openProcessCardModalRevision(processDefinitionId, businessKey, index) {
                    exModal.open({
                        scope: {
                            jobModel: scope.jobModel,
                            getStatus: scope.getStatus,
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails: scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            download: function (file) {
                                $http({
                                    method: 'GET',
                                    url: '/camunda/uploads/get/' + file.path,
                                    transformResponse: []
                                }).then(function (response) {
                                    document.getElementById('fileDownloadIframe').src = response.data;
                                }, function (error) {
                                    console.log(error);
                                });
                            },
                            isFileVisible: function (file) {
                                return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
                            },
                            getDictNameById: function (dictionary, id) {
                                return _.find(dictionary, function (dict) {
                                    return dict.id === id;
                                });
                            },
                            compareDate: new Date('2019-02-05T06:00:00.000'),


                        },
                        templateUrl: './js/partials/processCardModal.html',
                        size: 'lg'
                    }).then(function (results) {
                    });
                }

                scope.toggleProcessViewDismantle = function (index, processDefinitionKey, processDefinitionId, businessKey) {
                    scope.showDiagramView = false;
                    scope.diagram = {};
                    if (scope.piIndex === index) {
                        scope.piIndex = undefined;
                    } else {
                        scope.piIndex = index;
                        scope.dismantleInfo = {
                        processDefinitionKey: processDefinitionKey,
                        startTime: {value: scope.processInstances[index].startTime}
                    };
                    $http({
                        method: 'GET',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        url: baseUrl + '/task?processInstanceBusinessKey=' + businessKey,
                    }).then(
                        function (tasks) {
                            var asynCall1 = false;
                            var asynCall2 = false;
                            var asynCall3 = false;
                            var processInstanceTasks = tasks.data._embedded.task;
                            if (processInstanceTasks && processInstanceTasks.length > 0) {
                                var groupasynCalls = 0;
                                var maxGroupAsynCalls = processInstanceTasks.length;
                                processInstanceTasks.forEach(function (e) {
                                    if (e.assignee && tasks.data._embedded.assignee) {
                                        for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
                                            if (tasks.data._embedded.assignee[i].id === e.assignee) {
                                                e.assigneeObject = tasks.data._embedded.assignee[i];
                                            }
                                        }
                                    }
                                    $http({
                                        method: 'GET',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        url: baseUrl + '/task/' + e.id
                                    }).then(
                                        function (taskResult) {
                                            if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                                e.group = taskResult.data._embedded.group[0].id;
                                                groupasynCalls+=1;
                                                if (groupasynCalls === maxGroupAsynCalls) {
                                                    asynCall1 = true;
                                                    if (asynCall1 && asynCall2) {
                                                        openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                        asynCall1 = false;
                                                    }  else console.log('asynCall 2 problem');
                                                } else {
                                                    console.log(groupasynCalls, maxGroupAsynCalls);

                                                }
                                            } else {
                                                groupasynCalls+=1;
                                                if (groupasynCalls === maxGroupAsynCalls) {
                                                    asynCall1 = true;
                                                    if (asynCall1 && asynCall2) {
                                                        openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                        asynCall1 = false;
                                                    }  else console.log('asynCall 2 problem');
                                                } else {
                                                    console.log(groupasynCalls, maxGroupAsynCalls);

                                                }
                                            }
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );

                                });

                            } else {
                                asynCall1 = true;
                                if (asynCall1 && asynCall2) {
                                    openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                    asynCall1 = false;
                                }
                            }
                            $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstances[index].id).then(
                                function (result) {
                                    var workFiles = [];
                                    result.data.forEach(function (el) {
                                        console.log('!!!!!');

                                        scope.dismantleInfo[el.name] = el;
                                        if (el.type === 'File' || el.type === 'Bytes') {
                                            scope.dismantleInfo[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                        }
                                        if (el.type === 'Json') {
                                            scope.dismantleInfo[el.name].value = JSON.parse(el.value);
                                        }
                                    });
                                    if (scope.dismantleInfo.resolutions && scope.dismantleInfo.resolutions.value) {

                                        $q.all(scope.dismantleInfo.resolutions.value.map(function (resolution) {
                                            return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                        })).then(function (tasks) {
                                            tasks.forEach(function (e, index) {
                                                if (e.data.length > 0) {
                                                    scope.dismantleInfo.resolutions.value[index].taskName = e.data[0].name;
                                                    try {
                                                        scope.dismantleInfo.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                    } catch (e) {
                                                        console.log(e);
                                                    }
                                                }
                                            });
                                            asynCall2 = true;
                                            if (asynCall1 && asynCall2) {
                                                openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                asynCall2 = false;
                                            } else console.log('asynCall 1 problem');
                                        });
                                    }
                                    scope.dismantleInfo.tasks = processInstanceTasks;
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );

                        },
                        function (error) {
                            console.log(error.data);
                        }
                    );
                }
            };
            function openProcessCardModalDismantle(processDefinitionId, businessKey, index) {
                exModal.open({
                    scope: {
                        dismantleInfo: scope.dismantleInfo,
                        showDiagram:scope.showDiagram,
                        showHistory: scope.showHistory,
                        hasGroup: scope.hasGroup,
                        showGroupDetails:scope.showGroupDetails,
                        processDefinitionId: processDefinitionId,
                        piIndex: scope.piIndex,
                        $index: index,
                        businessKey: businessKey,
                        download: function(path) {
                            $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
                            then(function(response) {
                                document.getElementById('fileDownloadIframe').src = response.data;
                            }, function(error){
                                console.log(error);
                            });
                        }
                    },
                    templateUrl: './js/partials/dismantleCardModal.html',
                    size: 'lg'
                }).then(function(results){
                }); 
            }


            },
            templateUrl: './js/directives/search/networkArchitectureSearch.html'
        };
    }]);
});