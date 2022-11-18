define(['./module', 'camundaSDK', 'lodash', 'big-js', 'jquery', 'moment'], function (module, CamSDK, _, Big, $, moment) {
    'use strict';
return module.controller('mainCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$routeParams', '$timeout', '$location', 'exModal', '$http', '$state', 'StartProcessService', function ($scope, $rootScope, toasty, AuthenticationService, $routeParams, $timeout, $location, exModal, $http, $state, StartProcessService) {
    $rootScope.currentPage = {
        name: 'tasks'
    };

    var camClient = new CamSDK.Client({
        mock: false,
        apiUri: '/camunda/api/engine/'
    });

    var Authentication = function (data) {
        angular.extend(this, data);
    }

    $scope.view = {
        page: 1,
        maxResults: 20
    }

    var baseUrl = '/camunda/api/engine/engine/default';

    $scope.selectedView = 'task';
    $scope.camForm = null;

    $rootScope.logout = function () {
        AuthenticationService.logout().then(function () {
            $scope.authentication = null;
        });
    }

    if ($routeParams.task) {
        $scope.tryToOpen = {
            id: $routeParams.task
        }
    }
    $scope.catalogs = {};

    $scope.reverseOrder = false;
    $scope.fieldName = 'businessKey';
    $scope.fieldFilter = {};
    $scope.visibilityFilter = {};

    $scope.invoiceContractorFilter = {};
    $scope.currentDate = new Date();
    $scope.invoiceContractorFilter.beginYear = $scope.currentDate.getFullYear() - 1;
    $scope.invoiceContractorFilter.endYear = $scope.currentDate.getFullYear();
    $scope.years = [];
    for (var year = 2017; year <= $scope.invoiceContractorFilter.endYear; year++) {
        $scope.years.push(year);
    }
    $scope.regionsMap = {
        'alm': 'Almaty',
        'astana': 'Astana',
        'nc': 'North & Center',
        'east': 'East',
        'south': 'South',
        'west': 'West'
    };
    $scope.contractorsSearchResults = [];

    $scope.setVisibilityFilter = function (fieldName) {
        //$scope.fieldName = fieldName;
        $scope.fieldFilter[fieldName] = $scope.visibilityFilter[fieldName] ? undefined : $scope.fieldFilter[fieldName];
        $scope.visibilityFilter[fieldName] = $scope.visibilityFilter[fieldName] ? false : true;
    };

    $scope.orderByFieldName = function (fieldName) {
        if ($scope.fieldName == fieldName) {
            $scope.reverseOrder = !$scope.reverseOrder;
        } else {
            $scope.reverseOrder = false;
            $scope.fieldName = fieldName;
        }
    };

    $scope.filterByFields = function () {
        if (Object.keys($scope.fieldFilter).length !== 0) {
            return Object.keys($scope.fieldFilter).filter(fieldName =>
                $scope.fieldFilter[fieldName]
            ).reduce(function (obj, val, i) {
                    obj[val] = $scope.fieldFilter[val];
                    return obj;
                },
                {});
        }
        return undefined;
    };
    $scope.isRegionFiltersVisible = function () {
        return (($rootScope.isProcessAvailable('Revision') && $rootScope.isProcessVisible('Revision')) || ($rootScope.isProcessAvailable('Invoice') && $rootScope.isProcessVisible('Invoice'))) && $rootScope.hasGroup('head_kcell_users');
    }

    $scope.selectFilter = function (filter) {
        $scope.currentTask = undefined;
        $scope.currentFilter = filter;
        $location.search({task: undefined});
        $scope.currentRegionFilter = undefined;
        if (filter === 'search') {
            $scope.selectedView = 'search';
        } else {
            $scope.selectedView = 'task';
            $scope.view.page = 1;
            loadTasks();
        }
    }

    $scope.openTask = function (taskId) {
        $scope.selectedView = 'task';
        $state.go('tasks.task', {id: taskId});
    }

    $scope.$on('getTaskListEvent', function (event, data) {
        getTaskList();
    });
    $scope.startProcess = function (id) {
        StartProcessService(id);
    }
    $scope.nextTasks = function(){
        $scope.view.page++;
        loadTasks();
    }

    $scope.prevTasks = function () {
        $scope.view.page--;
        loadTasks();
    }

    $scope.setSelectedProcessKey = function (key) {
        if ($scope.selectedProcessKey === key) {
            $scope.selectedProcessKey = undefined;
        } else {
            $scope.selectedProcessKey = key;
        }
        $scope.currentTaskGroup = undefined;
    }

    $scope.setSelectedTaskGroupName = function (currentTaskGroup) {

        $scope.currentTask = undefined;
        $location.search({task: undefined});
        $scope.currentTaskGroup = currentTaskGroup;

        if (currentTaskGroup.taskDefinitionKey.indexOf("massApprove") === 0) {
            $state.go('tasks.massapprove', {defKey: currentTaskGroup.taskDefinitionKey});
            $scope.selectedView = 'massapprove';
        } else {
            $scope.selectedView = 'tasks';

            var varSearchParams = {processInstanceIds: _.map($scope.currentTaskGroup.tasks, 'processInstanceId')};
            $http({
                method: 'POST',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                data: varSearchParams,
                url: baseUrl + '/history/process-instance'
            }).then(
                function(processes){
                    angular.forEach($scope.currentTaskGroup.tasks, function(task){
                        var process = _.find(processes.data, function(p){ return p.id === task.processInstanceId });
                        task.processBusinessKey = process.businessKey;
                        task.processStartUserId = process.startUserId;
                        task.processStartTime = process.startTime;
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
            if ($rootScope.selectedProcess.key === 'Revision') {
                $scope.getAdditionalVariablesToTasks(['site_name']);
            }
            if ($rootScope.selectedProcess.key === 'leasing') {
                $scope.getAdditionalVariablesToTasks(['ncpID', 'siteName']);
            }
        }
    }

    $scope.getAdditionalVariablesToTasks = function(variableList){
        _.forEach(variableList, function(variable) {
            var varSearchParams = {processInstanceIdIn: _.map($scope.currentTaskGroup.tasks, 'processInstanceId'), variableName: variable};
            $http({
                method: 'POST',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                data: varSearchParams,
                url: baseUrl + '/variable-instance'
            }).then(
                function (vars) {
                    angular.forEach($scope.currentTaskGroup.tasks, function (task) {
                        var f = _.find(vars.data, function (v) {
                            return v.processInstanceId === task.processInstanceId;
                        });
                        if (f) {
                            task[variable] = f.value;
                        }
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
        });

    }

    $scope.getSite = function (val) {
        return $http.get('/camunda/sites/name/contains/'+val).then(
            function(response){
                console.log(response)
                response.data.forEach(function(e){
                    e.name = e.site_name;
                });
                return response.data;
            });
    };

    $scope.siteSelected = function ($item) {
        $scope.siteName = $item.name;
        $scope.site = $item.siteid;
        $scope.site_name = $item.site_name;
    };

    function getAllTaskNodes() {
        $http.get(baseUrl + '/process-definition/key/Revision/xml')
            .then(function (response) {
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

                var userTasks = getUserTasks(xml);
                var userTasksMap = _.keyBy(userTasks, function (v) {
                    return v.id
                })
                var userTasksFiltered = _.filter(userTasksMap, function (v) {
                    if (v.description) {
                        return v.description.indexOf('{') < 0
                    }
                })
                $scope.taskIds = [{id: 'all', name: 'All'}]
                $scope.taskIds.push({id: 'accept_work_planning_group', name: 'Accept and sign by planning group'});
                $scope.taskIds.push({id: 'accept_work_maintenance_group', name: 'Accept and sign by maintanance group'});
                $scope.taskIds = $scope.taskIds.concat(userTasksFiltered);
            });
    }
    $scope.searchForContractors = function(refresh){
        $scope.searchProcessesForContractors(refresh);
        // if($scope.accepted){
        //  $scope.searchProcessesForContractors();
        // } else {
        //  $scope.searchTasksForContractors();
        // }
    }

    // $scope.searchTasksForContractors = function(){
    //  var queryParams = {processDefinitionKey:'Revision'};
    //  if($scope.site && $scope.site_name){
    //      queryParams.processVariables = [{name:"site", value:$scope.site, operator: "eq"}];
    //  }
    //  if($scope.taskId !== 'all'){
    //      queryParams.taskDefinitionKey = $scope.taskId;
    //  } else {
    //      var taskIdList = _.filter($scope.taskIds, function(n) {
    //        return n.id !== 'all';
    //      });
    //      queryParams.taskDefinitionKeyIn = _.map(taskIdList, 'id');
    //  }
    //
    //  if($scope.priority === 'emergency'){
    //      queryParams.priority = 100;
    //  }
    //  queryParams.processVariables = [];
    //  if($scope.bussinessKey){
    //      queryParams.processVariables.push({name:"jrNumber", value:$scope.bussinessKey, operator: "eq"});
    //  }
    //  queryParams.processVariables.push({name:"contractorJobAssignedDate", value:new Date(), operator: "lteq"});
    //
    //  queryParams.processVariables.push({name:"contractor", value: 4, operator: "eq"});
    //
    //  queryParams.candidateUser = $rootScope.authUser.id;
    //  queryParams.includeAssignedTasks = true;
    //  $scope.searchResults = [];
    //  $http({
    //      method: 'POST',
    //      headers:{'Accept':'application/hal+json, application/json; q=0.5'},
    //      data: queryParams,
    //      url: baseUrl+'/task'
    //  }).then(
    //      function(results){
    //          $scope.searchResults = _.filter(results.data, function(d) {
    //              return !d.assignee || d.assignee === $rootScope.authUser.id;
    //          });
    //
    //          if($scope.searchResults.length > 0){
    //              _.forEach(['jrNumber','site_name', 'validityDate'], function(variable) {
    //                  var varSearchParams = {processInstanceIdIn: _.map($scope.searchResults, 'processInstanceId'), variableName: variable};
    //                  $http({
    //                      method: 'POST',
    //                      headers:{'Accept':'application/hal+json, application/json; q=0.5'},
    //                      data: varSearchParams,
    //                      url: baseUrl+'/variable-instance'
    //                  }).then(
    //                      function(vars){
    //                          $scope.searchResults.forEach(function(el) {
    //                              var f =  _.filter(vars.data, function(v) {
    //                                  return v.processInstanceId === el.processInstanceId;
    //                              });
    //                              if(f){
    //                                  el[variable] = f[0].value;
    //                              }
    //                          });
    //                      },
    //                      function(error){
    //                          console.log(error.data);
    //                      }
    //                  );
    //              });
    //          }
    //      },
    //      function(error){
    //          console.log(error.data);
    //      }
    //  );
    // };
    $scope.getExcelFile = function () {
        if($scope.xlsxPreparedRevision) {
            var tbl = document.getElementById('revisionsSearchTask');
            var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY', cellDates:true}); 
            var wb = XLSX.utils.book_new();

            XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
            return XLSX.writeFile(wb, 'revision-search-task-result.xlsx');
        } else {
            $scope.searchProcessesForContractors(false, true);
            $scope.xlsxPreparedRevision = true;
        }

    }

    $scope.searchFilter = {
        page: 1,
        pageSize: 20
    };

    $scope.processInstancesTotal = 0;
    $scope.processInstancesPages = 0;

    $scope.nextPage = function () {
        $scope.searchFilter.page++;
        $scope.searchForContractors(false);
        $scope.piIndex = undefined;
    }

    $scope.prevPage = function () {
        $scope.searchFilter.page--;
        $scope.searchForContractors(false);
        $scope.piIndex = undefined;
    }

    $scope.selectPage = function (page) {
        $scope.searchFilter.page = page;
        $scope.searchForContractors(false);
        $scope.piIndex = undefined;
    }

    $scope.getPages = function () {
        var array = [];
        if ($scope.processInstancesPages < 8) {
            for (var i = 1; i <= $scope.processInstancesPages; i++) {
                array.push(i);
            }
        } else {
            var decrease = $scope.searchFilter.page - 1;
            var increase = $scope.searchFilter.page + 1;
            array.push($scope.searchFilter.page);
            while (increase - decrease < 8) {
                if (decrease > 0) {
                    array.unshift(decrease--);
                }
                if (increase < $scope.processInstancesPages) {
                    array.push(increase++);
                }
            }
        }
        return array;
    }

    $scope.contractorTasksNames = ['Attach Material List', 'Upload TR', 'Fill Applied Changes Info', 'Attach Additional Material List', 'Upload Additional TR'];
    $scope.contractorTasksIds = ['attach_material_list_contractor', 'upload_tr_contractor', 'fill_applied_changes_info', 'attach_additional_material_list_contractor', 'upload_additional_tr_contractor'];

    $scope.searchProcessesForContractors = async function(refresh, skipPagination){
        var queryParams = {processDefinitionKey: 'Revision', variables: []};
        var taskDefKey;

        if(refresh){
            $scope.searchFilter = {
                page: 1,
                pageSize: 20
            };
            $scope.xlsxPreparedRevision = false;
        }

        if(!skipPagination){
            $scope.xlsxPreparedRevision = false;
            $scope.xlsFilter = $scope.searchFilter;
        }

        if($rootScope.hasGroup('hq_contractor_lse') || $rootScope.hasGroup('astana_contractor_lse') || $rootScope.hasGroup('nc_contractor_lse') || $rootScope.hasGroup('alm_contractor_lse') || $rootScope.hasGroup('east_contractor_lse') || $rootScope.hasGroup('south_contractor_lse') || $rootScope.hasGroup('west_contractor_lse')){
            $scope.contractorCode = 4;
        } else if($rootScope.hasGroup('hq_contractor_arlan') || $rootScope.hasGroup('astana_contractor_arlan') || $rootScope.hasGroup('nc_contractor_arlan') || $rootScope.hasGroup('alm_contractor_arlan') || $rootScope.hasGroup('east_contractor_arlan') || $rootScope.hasGroup('south_contractor_arlan') || $rootScope.hasGroup('west_contractor_arlan')){
            $scope.contractorCode = 8;
        } else if($rootScope.hasGroup('hq_contractor_logycom') || $rootScope.hasGroup('astana_contractor_logycom') || $rootScope.hasGroup('nc_contractor_logycom') || $rootScope.hasGroup('alm_contractor_logycom') || $rootScope.hasGroup('east_contractor_logycom') || $rootScope.hasGroup('south_contractor_logycom') || $rootScope.hasGroup('west_contractor_logycom')){
            $scope.contractorCode = 7;
        } else if($rootScope.hasGroup('hq_contractor_alta') || $rootScope.hasGroup('astana_contractor_alta') || $rootScope.hasGroup('nc_contractor_alta') || $rootScope.hasGroup('alm_contractor_alta') || $rootScope.hasGroup('east_contractor_alta') || $rootScope.hasGroup('south_contractor_alta') || $rootScope.hasGroup('west_contractor_alta')){
            $scope.contractorCode = 6;
        }

        queryParams.variables.push({name:"contractor", value: $scope.contractorCode, operator: "eq"});
        if(!$scope.accepted){
            queryParams.variables.push({name:"contractorJobAssignedDate", value:new Date(), operator: "lteq"});
            if($scope.taskId && $scope.taskId !== 'all'){
                taskDefKey = $scope.taskId;
                queryParams.activityIdIn = [$scope.taskId];
            }
        }
        if ($scope.site && $scope.site_name) {
            queryParams.variables.push({name: "siteName", value: $scope.site, operator: "eq"});
        }
        if ($scope.priority === 'emergency') {
            queryParams.variables.push({name: "priority", value: "emergency", operator: "eq"});
        }
        if ($scope.bussinessKey) {
            queryParams.businessKey = $scope.bussinessKey;
        }
        if ($scope.accepted) {
            queryParams.activityIdIn = ['attach-scan-copy-of-acceptance-form', 'intermediate_wait_acts_passed', 'intermediate_wait_invoiced']
        }
        if ($rootScope.hasGroup('hq_contractor_lse')) {
            // all values
        } else if ($rootScope.hasGroup('astana_contractor_lse') && $rootScope.hasGroup('nc_contractor_lse')) {
            if (!$scope.region || ['astana', 'nc'].indexOf($scope.region) === -1) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
                $scope.region = 'astana';
            } else {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.region});
            }
        } else if ($rootScope.hasGroup('astana_contractor_lse') || $rootScope.hasGroup('astana_contractor_arlan') || $rootScope.hasGroup('astana_contractor_alta') || $rootScope.hasGroup('astana_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
        } else if ($rootScope.hasGroup('nc_contractor_lse') || $rootScope.hasGroup('nc_contractor_arlan') || $rootScope.hasGroup('nc_contractor_alta') || $rootScope.hasGroup('nc_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'nc'});
        } else if ($rootScope.hasGroup('alm_contractor_lse') || $rootScope.hasGroup('alm_contractor_arlan') || $rootScope.hasGroup('alm_contractor_alta') || $rootScope.hasGroup('alm_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'alm'});
        } else if ($rootScope.hasGroup('east_contractor_lse') || $rootScope.hasGroup('east_contractor_arlan') || $rootScope.hasGroup('east_contractor_alta') || $rootScope.hasGroup('east_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'east'});
        } else if ($rootScope.hasGroup('south_contractor_lse') || $rootScope.hasGroup('south_contractor_arlan') || $rootScope.hasGroup('south_contractor_alta') || $rootScope.hasGroup('south_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'south'});
        } else if ($rootScope.hasGroup('west_contractor_lse') || $rootScope.hasGroup('west_contractor_arlan') || $rootScope.hasGroup('west_contractor_alta') || $rootScope.hasGroup('west_contractor_logycom')) {
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'west'});
        }

        if(skipPagination){
            $scope.processSearchResultsXsl = [];
        } else {
            $scope.processSearchResults = [];
        }
        $http({
            method: 'POST',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            data: queryParams,
            url: baseUrl + '/process-instance/count'
        }).then(function(countResults){
            $scope.processInstancesTotal = countResults.data.count;
            $scope.processInstancesPages = Math.floor(countResults.data.count / $scope.searchFilter.pageSize) + ((countResults.data.count % $scope.searchFilter.pageSize) > 0 ? 1 : 0)
            $http({
                method: 'POST',
                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                data: queryParams,
                url: baseUrl + '/process-instance?firstResult=' + (!skipPagination ? (($scope.searchFilter.page - 1) * $scope.searchFilter.pageSize + '&maxResults=' + $scope.searchFilter.pageSize) : '')
            }).then(function(results){
                var processSearchResults = 'processSearchResults';
                if(skipPagination){
                    processSearchResults = 'processSearchResultsXls';
                }
                $scope[processSearchResults] = results.data;
                if($scope[processSearchResults].length > 0){
                    _.forEach(['site_name', 'priority', 'acceptanceDate', 'contractorJobAssignedDate','starter'], function(variable) {
                        var varSearchParams = {processInstanceIdIn: _.map($scope[processSearchResults], 'id'), variableName: variable};
                        $http({
                            method: 'POST',
                            headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                            data: varSearchParams,
                            url: baseUrl+'/variable-instance'
                        }).then(
                            function(vars){
                                $scope[processSearchResults].forEach(function(el) {
                                    var f =  _.filter(vars.data, function(v) {
                                        return v.processInstanceId === el.id;
                                    });
                                    if(f){
                                        if (variable === 'acceptanceDate') {
                                            el[variable] = f[0] !== void(0) ? f[0].value : null;
                                        } else {
                                            el[variable] = f[0].value;
                                            if(variable === 'starter'){
                                                $http.get(baseUrl + '/user/' + f[0].value + '/profile').then(
                                                    function (result) {
                                                        el[variable] = result.data.firstName + " " + result.data.lastName;
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            }
                                        }


                                    }
                                });

                            },
                            function(error){
                                console.log(error.data);
                            }
                        );
                    });
                    if(!$scope.accepted) {
                        $scope[processSearchResults].forEach(function(el) {
                            $http({
                                method: 'GET',
                                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                                url: baseUrl+'/process-instance/' + el.id + '/activity-instances'
                            }).then(function(activities){
                                if(!el.tasks){
                                    el.tasks = [];
                                }
                                _.forEach(activities.data.childActivityInstances, function (firstLevel) {
                                    if (firstLevel.activityType === 'subProcess') {
                                        _.forEach(firstLevel.childActivityInstances, function (secondLevel) {
                                            if(secondLevel.childActivityInstances && secondLevel.childActivityInstances.length > 0) {
                                                _.forEach(secondLevel.childActivityInstances, function (thirdLevel) {
                                                    if (thirdLevel.activityName && $scope.contractorTasksIds.indexOf(thirdLevel.activityId) === -1) {
                                                        el.tasks.push({id: thirdLevel.id, name: thirdLevel.activityName});
                                                    }
                                                });
                                            } else {
                                                if (secondLevel.activityName && $scope.contractorTasksIds.indexOf(secondLevel.activityId) === -1) {
                                                    el.tasks.push({id: secondLevel.id, name: secondLevel.activityName});
                                                }
                                            }
                                        });
                                    } else {
                                        if(firstLevel.activityName && $scope.contractorTasksIds.indexOf(firstLevel.activityId) === -1){
                                            el.tasks.push({id: firstLevel.id, name:firstLevel.activityName});
                                        }
                                    }
                                });
                            });

                            $http({
                                method: 'POST',
                                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                                data: {processInstanceId: el.id, taskDefinitionKeyIn:$scope.contractorTasksIds},
                                url: baseUrl+'/task'
                            }).then(
                                function(tasks){
                                    if(!el.tasks){
                                        el.tasks = [];
                                    }
                                    _.forEach(tasks.data, function (task) {
                                        el.tasks.push({id: task.id, name:task.name});
                                    });
                                }
                            );

                        });
                    } else {
                        $scope[processSearchResults].forEach(function(el) {
                            $http({
                                method: 'GET',
                                headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                                url: baseUrl+'/process-instance/' + el.id + '/activity-instances'
                            }).then(function(activities){
                                console.log(activities);
                                activities.data.childActivityInstances.forEach(function(act) {
                                    if(['attach-scan-copy-of-acceptance-form','intermediate_wait_acts_passed','intermediate_wait_invoiced'].indexOf(act.activityId)!==-1){
                                        el.activityName = act.activityName;
                                    }
                                });
                            });
                        });
                    }
                }
            });
        });
    }

    $scope.toggleProcessViewRevision = async function (p) {
        $scope.jobModel = {
            state: 'Active',
            processDefinitionKey: 'Revision',
            startTime: p.requestedDate
        };
        await $http({
            method: 'GET',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            url: baseUrl + '/task?processInstanceId=' + p.id,
        }).then(
            async function (tasks) {
                var processInstanceTasks = tasks.data._embedded.task;
                if (processInstanceTasks && processInstanceTasks.length > 0) {
                    for (var k = 0; k < processInstanceTasks.length; k++) {
                        var e = processInstanceTasks[k]
                        if (e.assignee && tasks.data._embedded.assignee) {
                            for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
                                if (tasks.data._embedded.assignee[i].id === e.assignee) {
                                    e.assigneeObject = tasks.data._embedded.assignee[i];
                                }
                                await $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task/' + e.id
                                }).then(
                                    function (taskResult) {
                                        if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                            e.group = taskResult.data._embedded.group[0].id;
                                        }
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );
                            }
                        }
                        await $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: baseUrl + '/task/' + e.id
                        }).then(
                            function (taskResult) {
                                if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                    e.group = taskResult.data._embedded.group[0].id;
                                }
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    };
                    $scope.jobModel.tasks = processInstanceTasks;
                }
                await $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + p.id).then(
                    async function (result) {
                        var workFiles = [];
                        result.data.forEach(function (el) {
                            $scope.jobModel[el.name] = el;
                            if (el.type === 'File' || el.type === 'Bytes') {
                                $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                            }
                            if (el.type === 'Json') {
                                $scope.jobModel[el.name].value = JSON.parse(el.value);
                            }
                            if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                workFiles.push(el);
                            }
                        });
                        if ($scope.jobModel['siteWorksFiles']) {
                            _.forEach($scope.jobModel['siteWorksFiles'].value, function (file) {
                                var workIndex = file.name.split('_')[1];
                                if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                    $scope.jobModel.jobWorks.value[workIndex].files = [];
                                }
                                if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                    return f.name == file.name;
                                }) < 0) {
                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                }
                            });
                        }
                        _.forEach(workFiles, function (file) {
                            var workIndex = file.name.split('_')[1];
                            if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                $scope.jobModel.jobWorks.value[workIndex].files = [];
                            }
                            if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                return f.name == file.name;
                            }) < 0) {
                                $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                            }
                        });
                        angular.extend($scope.jobModel, $scope.catalogs);
                        // for(var i = 0; i< processInstanceTasks.length;i++) {
                        //  processInstanceTasks[i].assigneeObject = await $scope.getUserById(processInstanceTasks[i])
                        // }
                        // $scope.jobModel.tasks = processInstanceTasks;

                        openProcessCardModalRevision(p);
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                console.log('processInstanceTasks: ', processInstanceTasks)

            },
            function (error) {
                console.log(error.data);
            }
        );
    }

    $scope.getUserById = async function (task) {
        var user = null
        if (task.assignee) {
            user = await $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/user/?id=' + task.assignee
            }).then(function (results) {
                // var index = _.findIndex($scope.jobModel.tasks, function(v){
                //  return v.processInstanceId = task.processInstanceId
                // })
                // $scope.jobModel.tasks[index].assigneeObject = results.data[0]
                return results.data[0]
            })
        }
        return user
    }
    // open modal on task search with process info
    // $scope.openTaskCardModalRevision = async function(task) {
    //  await $scope.searchProcessesForContractors(true)
    //  var process = _.filter($scope.processSearchResults, function(v){
    //      return v.id === task.processInstanceId
    //  })
    //  await $scope.toggleProcessViewRevision(process[0], true);
    //  await $scope.getUserById(task);
    //  openProcessCardModalRevision(process[0])
    //
    // }
    $scope.showGroupDetails = function (group) {
        $http({
            method: 'GET',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            url: baseUrl + '/user?memberOfGroup=' + group
        }).then(
            function (result) {
                exModal.open({
                    scope: {
                        members: result.data,
                    },
                    templateUrl: './js/partials/members.html',
                    size: 'md'
                }).then(function (results) {
                });
            },
            function (error) {
                console.log(error.data);
            }
        );
    };

    function openProcessCardModalRevision(p) {
        exModal.open({
            scope: {
                jobModel: $scope.jobModel,
                getStatus: $scope.getStatus,
                hasGroup: $scope.hasGroup,
                showGroupDetails: $scope.showGroupDetails,

                processDefinitionId: p.definitionId,
                businessKey: p.businessKey,
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
                compareDate: new Date('2019-02-05T06:00:00.000')
            },
            templateUrl: 'js/partials/processCardModal.html',
            size: 'lg'
        }).then(function (results) {
        });
    }

    $scope.getVariableLabel = function(key){
        return _.keyBy($scope.currentFilter.properties.variables, 'name')[key].label;
    }

    $scope.firstLevel = "open";
    $scope.secondLevel = "closed";

    $scope.collapseLevels = function (levelName) {
        if (levelName === "firstLevel") {
            $scope.firstLevel = $scope.firstLevel === 'open' ? 'closed' : 'open';
        } else if (levelName === "secondLevel") {
            $scope.secondLevel = $scope.secondLevel === 'open' ? 'closed' : 'open';
        }
    }

    $scope.collapseProject = function (project) {
        $rootScope.updateSelectedProject(project);
        $rootScope.updateSelectedProcess(undefined);
        $rootScope.updateSelectedTask(undefined);
        $scope.currentFilter = undefined;
        $scope.taskGroups = {};
        $scope.secondLevel = "closed";
    }

    $scope.collapseProcess = function (process) {
        $rootScope.updateSelectedProcess(process);
        if ($rootScope.selectedTask && process !== $rootScope.selectedTask.key) {
            $rootScope.updateSelectedTask(undefined);
        }
        $scope.currentFilter = undefined;
        $scope.taskGroups = {};
        getTaskList();
        $scope.secondLevel = "closed";
    }
    $scope.collapseTask = function (task) {
        $rootScope.updateSelectedTask(task);
        $scope.collapseLevels('secondLevel');
    }

    $scope.clearContractorFilters = function () {
        $scope.invoiceContractorFilter = {
            beginYear: $scope.currentDate.getFullYear() - 1,
            endYear: $scope.currentDate.getFullYear(),
            region: 'all',
            businessKeyFilterType: 'eq',
            businessKey: undefined,
            workType: undefined,
            unfinished: undefined
        }
    }

    $scope.searchContractorInvoices = function () {
        var queryParams = {processDefinitionKey: 'Invoice', variables: []};
        if ($scope.invoiceContractorFilter.beginYear) {
            queryParams.startedAfter = $scope.invoiceContractorFilter.beginYear + '-01-01T00:00:00.000+0600';
        }
        if ($scope.invoiceContractorFilter.endYear) {
            queryParams.startedBefore = (Number($scope.invoiceContractorFilter.endYear) + 1) + '-01-01T00:00:00.000+0600';
        }
        if ($scope.invoiceContractorFilter.businessKey) {
            if ($scope.invoiceContractorFilter.businessKeyFilterType === 'eq') {
                queryParams.processInstanceBusinessKey = $scope.invoiceContractorFilter.businessKey;
            } else {
                queryParams.processInstanceBusinessKeyLike = '%' + $scope.invoiceContractorFilter.businessKey + '%';
            }
        }
        if($scope.invoiceContractorFilter.workType){
            queryParams.variables.push({name:"workType", value:$scope.invoiceContractorFilter.workType+'', operator: "eq"});
        }
        if ($scope.invoiceContractorFilter.unfinished) {
            queryParams.unfinished = $scope.invoiceContractorFilter.unfinished;
        }

        var isHQ = $rootScope.hasGroup('hq_contractor_lse') || $rootScope.hasGroup('hq_contractor_arlan') || $rootScope.hasGroup('hq_contractor_logycom') || $rootScope.hasGroup('hq_contractor_alta');
        if(isHQ && $scope.invoiceContractorFilter.region!=='all'){
            queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.invoiceContractorFilter.region});
        } else {
            if ($rootScope.hasGroup('astana_contractor_lse') && $rootScope.hasGroup('nc_contractor_lse')) {
                if (!$scope.invoiceContractorFilter.region || ['astana', 'nc'].indexOf($scope.invoiceContractorFilter.region) === -1) {
                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
                    $scope.invoiceContractorFilter.region = 'astana';
                } else {
                    queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": $scope.invoiceContractorFilter.region});
                }
            }  else if ($rootScope.hasGroup('astana_contractor_lse') || $rootScope.hasGroup('astana_contractor_arlan') || $rootScope.hasGroup('astana_contractor_alta') || $rootScope.hasGroup('astana_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'astana'});
            } else if ($rootScope.hasGroup('nc_contractor_lse') || $rootScope.hasGroup('nc_contractor_arlan') || $rootScope.hasGroup('nc_contractor_alta') || $rootScope.hasGroup('nc_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'nc'});
            } else if ($rootScope.hasGroup('alm_contractor_lse') || $rootScope.hasGroup('alm_contractor_arlan') || $rootScope.hasGroup('alm_contractor_alta') || $rootScope.hasGroup('alm_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'alm'});
            } else if ($rootScope.hasGroup('east_contractor_lse') || $rootScope.hasGroup('east_contractor_arlan') || $rootScope.hasGroup('east_contractor_alta') || $rootScope.hasGroup('east_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'east'});
            } else if ($rootScope.hasGroup('south_contractor_lse') || $rootScope.hasGroup('south_contractor_arlan') || $rootScope.hasGroup('south_contractor_alta') || $rootScope.hasGroup('south_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'south'});
            } else if ($rootScope.hasGroup('west_contractor_lse') || $rootScope.hasGroup('west_contractor_arlan') || $rootScope.hasGroup('west_contractor_alta') || $rootScope.hasGroup('west_contractor_logycom')) {
                queryParams.variables.push({"name": "siteRegion", "operator": "eq", "value": 'west'});
            }
        }

        var contractorCode = '';
        if($rootScope.hasGroup('hq_contractor_lse') || $rootScope.hasGroup('astana_contractor_lse') || $rootScope.hasGroup('nc_contractor_lse') || $rootScope.hasGroup('alm_contractor_lse') || $rootScope.hasGroup('east_contractor_lse') || $rootScope.hasGroup('south_contractor_lse') || $rootScope.hasGroup('west_contractor_lse')){
            contractorCode = 'lse';
        } else if($rootScope.hasGroup('hq_contractor_arlan') || $rootScope.hasGroup('astana_contractor_arlan') || $rootScope.hasGroup('nc_contractor_arlan') || $rootScope.hasGroup('alm_contractor_arlan') || $rootScope.hasGroup('east_contractor_arlan') || $rootScope.hasGroup('south_contractor_arlan') || $rootScope.hasGroup('west_contractor_arlan')){
            contractorCode = 'arlan';
        } else if($rootScope.hasGroup('hq_contractor_logycom') || $rootScope.hasGroup('astana_contractor_logycom') || $rootScope.hasGroup('nc_contractor_logycom') || $rootScope.hasGroup('alm_contractor_logycom') || $rootScope.hasGroup('east_contractor_logycom') || $rootScope.hasGroup('south_contractor_logycom') || $rootScope.hasGroup('west_contractor_logycom')){
            contractorCode = 'logycom';
        } else if($rootScope.hasGroup('hq_contractor_alta') || $rootScope.hasGroup('astana_contractor_alta') || $rootScope.hasGroup('nc_contractor_alta') || $rootScope.hasGroup('alm_contractor_alta') || $rootScope.hasGroup('east_contractor_alta') || $rootScope.hasGroup('south_contractor_alta') || $rootScope.hasGroup('west_contractor_alta')){
            contractorCode = 'alta';
        }
        queryParams.variables.push({"name": "contractor", "operator": "eq", "value": contractorCode});

        $scope.piIndex = undefined;
        $http({
            method: 'POST',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            data: queryParams,
            url: baseUrl + '/history/process-instance'
        }).then(function (results) {
            $scope.contractorsSearchResults = results.data;
            if($scope.contractorsSearchResults.length > 0){
                _.forEach(['yearOfFormalPeriod', 'workType', 'monthActNumber'], function(variable) {
                    var varSearchParams = {processInstanceIdIn: _.map($scope.contractorsSearchResults, 'id'), variableName: variable};
                    $http({
                        method: 'POST',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        data: varSearchParams,
                        url: baseUrl + '/history/variable-instance'
                    }).then(
                        function (vars) {
                            $scope.contractorsSearchResults.forEach(function (el) {
                                var f = _.filter(vars.data, function (v) {
                                    return v.processInstanceId === el.id;
                                });
                                if (f) {
                                    el[variable] = f[0].value;
                                }
                            });
                        },
                        function (error) {
                            console.log(error.data);
                        }
                    );
                });
            }
        });
    }

    $scope.toggleProcessView = function(index, id, processDefinitionKey){
        if($scope.piIndeqx === index){
            $scope.piIndex = undefined;
        } else {
            $scope.piIndex = index;
            $scope.jobModel = {state: $scope.contractorsSearchResults[index].state, processDefinitionKey: processDefinitionKey};
            $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/task?processInstanceId=' + id,
            }).then(
                function (tasks) {
                    var processInstanceTasks = tasks.data._embedded.task;
                    if (processInstanceTasks && processInstanceTasks.length > 0) {
                        processInstanceTasks.forEach(function (e) {
                            if (e.assignee && tasks.data._embedded.assignee) {
                                for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
                                    if (tasks.data._embedded.assignee[i].id === e.assignee) {
                                        e.assigneeObject = tasks.data._embedded.assignee[i];
                                    }
                                    $http({
                                        method: 'GET',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        url: baseUrl + '/task/' + e.id
                                    }).then(

                                        function(taskResult){
                                            if(taskResult.data._embedded && taskResult.data._embedded.group){
                                                e.group = taskResult.data._embedded.group[0].id;
                                            }
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );
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
                                    }
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        });
                        $scope.jobModel.tasks = processInstanceTasks;
                    }
                    $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + id).then(
                        function (result) {
                            $scope.jobModel.files = [];
                            result.data.forEach(function (el) {
                                $scope.jobModel[el.name] = el;
                                if (el.type === 'File' || el.type === 'Bytes') {
                                    $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                }
                                if (el.type === 'Json') {
                                    if (el.name === 'attachInvoiceFileName') {
                                        $scope.jobModel.files.push(JSON.parse(el.value));
                                    }
                                    if (el.name === 'signedScanCopyFileName') {
                                        $scope.jobModel.files.push(JSON.parse(el.value));
                                    } else {
                                        $scope.jobModel[el.name].value = JSON.parse(el.value);
                                    }
                                }
                            });
                            if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                    return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                })).then(function (tasks) {
                                    tasks.forEach(function (e, index) {
                                        if (e.data.length > 0) {
                                            $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                            try {
                                                $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                            } catch (e) {
                                                console.log(e);
                                            }
                                        }
                                    });
                                });
                            }
                            angular.extend($scope.jobModel, $scope.catalogs);
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
    }

    $scope.getProcessDefinitions = function (project) {
        var processList = _.map(project.processes, 'key');
        return _.filter($scope.processDefinitions, function (pd) {
            return processList.indexOf(pd.key) !== -1 && pd.key !== 'after-sales-ivr-sms';
        });
    }
    $scope.getAllProcessDefinitions = function (projects) {
        var processList = [];
        angular.forEach(projects, function (project) {
            processList += _.map(project.processes, 'key');
        });
        return _.filter($scope.processDefinitions, function (pd) {
            return processList.indexOf(pd.key) !== -1 && pd.key !== 'after-sales-ivr-sms';
        });
    }

    function getTaskList() {
        $scope.projects = angular.copy($rootScope.projects);
        $http.get(baseUrl + '/filter?resoureType=Task').then(
            function (result) {
                var filters = result.data;
                angular.forEach($scope.projects, function (project) {
                    if ($rootScope.selectedProject.key && $rootScope.selectedProject.key === project.key) {
                        angular.forEach(project.processes, function (process) {
                            if ($rootScope.selectedProcess.key && $rootScope.selectedProcess.key === process.key) {
                                var allTasks = 0;
                                var claimed = 0;
                                var groupTasks = 0;
                                process.filters = [];
                                var selectedProcessDefinitionKeyMap = [process.key];
                                if (process.subprocesses && process.subprocesses.length > 0) {
                                    selectedProcessDefinitionKeyMap = _.concat(selectedProcessDefinitionKeyMap, _.map(process.subprocesses, 'key'));
                                }
                                var selectedQuery = {'processDefinitionKeyIn': selectedProcessDefinitionKeyMap};
                                if (process.businessKeyLike) {
                                    selectedQuery.processInstanceBusinessKeyLike = process.businessKeyLike;
                                }

                                angular.forEach(filters, function (filter) {
                                    if (!filter.properties.processDefinitionKey || filter.properties.processDefinitionKey === process.key) {
                                        $http.post(baseUrl + '/filter/' + filter.id + '/count', selectedQuery, {headers: {'Content-Type': 'application/json'}}).then(
                                            function (results) {
                                                var tmpfilter = angular.copy(filter);
                                                tmpfilter.itemCount = results.data.count;
                                                if (filter.name === 'All Tasks') {
                                                    allTasks = results.data.count;
                                                } else if (filter.name === 'My Claimed Tasks' || filter.name === 'My Unclaimed Tasks') {
                                                    claimed += results.data.count;
                                                }
                                                if (filter.name === 'My Group Tasks') {
                                                    groupTasks = results.data.count;
                                                }
                                                process.itemCount = Math.max(allTasks, claimed, groupTasks);
                                                process.filters.push(tmpfilter);
                                                if ($rootScope.selectedProcess && process.key === $rootScope.selectedProcess.key) {
                                                    $rootScope.selectedProcess.itemCount = process.itemCount;
                                                    $rootScope.selectedProcess.filters = process.filters;
                                                }
                                                project.itemCount = project.itemCount ? project.itemCount + results.data.count : results.data.count;
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );
                                    }
                                });
                            }
                        });
                    }
                });

                loadTasks();
            },
            function (error) {
                console.log(error.data);
            }
        );
    }

    function loadTasks() {
        if ($rootScope.selectedTask && $rootScope.selectedProcess && $rootScope.selectedTask === $rootScope.selectedProcess && $scope.secondLevel === "closed") {
            $scope.collapseLevels('secondLevel');
        }

        if ($scope.currentFilter) {
            var processDefinitionKeyMap = [$rootScope.selectedProcess.key];
            if ($rootScope.selectedProcess.subprocesses && $rootScope.selectedProcess.subprocesses.length > 0) {
                var subprocessDefinitionKeyMap = _.map($rootScope.selectedProcess.subprocesses, 'key');
                processDefinitionKeyMap = _.concat(processDefinitionKeyMap, subprocessDefinitionKeyMap);
            }

            var queryData = {sorting:[{"sortBy":"created","sortOrder":"desc"}],'processDefinitionKeyIn':processDefinitionKeyMap};
            if($rootScope.selectedProcess.businessKeyLike){
                queryData.processInstanceBusinessKeyLike = $rootScope.selectedProcess.businessKeyLike;
            }
            $scope.taskGroups = {};

            $http({
                method: 'POST',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                data: queryData,
                url: baseUrl + '/filter/' + $scope.currentFilter.id + '/list'
            }).then(
                function (results) {
                    try {
                        $scope.tasks = results.data._embedded.task;
                        var selectedTask;

                        if ($scope.tasks && $scope.tasks.length > 0) {
                            $scope.tasks.forEach(function (e) {
                                if (e.assignee) {
                                    for (var i = 0; i < results.data._embedded.assignee.length; i++) {
                                        if (results.data._embedded.assignee[i].id === e.assignee) {
                                            e.assigneeObject = results.data._embedded.assignee[i];
                                        }
                                    }
                                }
                                if(!$scope.taskGroups[e.name]){
                                    $scope.taskGroups[e.name] = {tasks:[], taskDefinitionKey: e.taskDefinitionKey, name: e.name};
                                }
                                $scope.taskGroups[e.name].tasks.push(e);

                                if ($scope.tryToOpen && e.assignee === $rootScope.authentication.name && e.processInstanceId === $scope.tryToOpen.id) {
                                    selectedTask = e;
                                }
                            });
                        }
                        if (selectedTask) {
                            $state.go('tasks.task', {id: selectedTask.id});
                        } else if ($scope.tryToOpen) {
                            $http.get(baseUrl + '/task/' + $scope.tryToOpen.id).then(
                                function (taskResult) {
                                    $scope.tryToOpen = undefined;
                                    if (taskResult.data.assignee) {
                                        $http.get(baseUrl + '/user/' + taskResult.data.assignee + '/profile').then(
                                            function (userResult) {
                                                taskResult.data.assigneeObject = userResult.data;
                                                $state.go('tasks.task', {id: taskResult.data.id});
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );
                                    } else {
                                        $state.go('tasks.task', {id: taskResult.data.id});
                                    }
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        }
                    } catch (e) {
                        console.log(e);
                    }
                },
                function (error) {
                    console.log(error.data);
                }
            );
        }
    }


    $scope.closeDiv = function (level) {
        if ($scope.currentFilter && $scope.firstLevel === 'open') {
            $scope.collapseLevels('firstLevel');
        }
    }

    $scope.openDiv = function (level) {
        if ($scope.currentFilter && $scope.firstLevel === 'closed') {
            $scope.collapseLevels('firstLevel');
        }
    }

    $scope.selectFilter = function (filter) {
        $scope.currentTask = undefined;
        $scope.currentFilter = filter;
        $location.search({task: undefined});
        if (filter === 'search') {
            $scope.selectedView = 'search';
            if ($rootScope.selectedProcess.key === 'Revision') {
                getAllTaskNodes();
            }
        } else {
            $scope.selectedView = 'task';
            $scope.view.page = 1;
            loadTasks();
        }
        if ($scope.firstLevel === 'open') {
            $scope.collapseLevels('firstLevel');
        }
    }


    getTaskList();

    $scope.getTaskList = getTaskList;

    $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
        function (result) {
            angular.extend($scope.catalogs, result.data);
        },
        function (error) {
            console.log(error.data);
        }
    );
    /* ------------------------------------- Tasks new logic ------------------------------*/

    /* ------------------------------------- Process catalog ------------------------------*/

    var catalogManagersGroupIds = {
        'Demand': ['demand_uat_catalog_managers'],
        'leasing': ['leasing_catalog_managers']

    };

    $scope.isUserCatalogManager = function (processId) {
        if (catalogManagersGroupIds.hasOwnProperty(processId)) {
            for (var groupId of catalogManagersGroupIds[processId]) {
                if ($rootScope.hasGroup(groupId)) return true;
            }
        }
        return false;
    };


/* ------------------------------------- Process catalog ------------------------------*/

    }]).controller('loginCtrl', ['$scope', '$rootScope', 'AuthenticationService', '$location', 'translateWithDefault', function ($scope, $rootScope, AuthenticationService, $location, translateWithDefault) {
        if ($rootScope.authentication) {
            return $location.path('/');
        }
        $rootScope.apps = {
            welcome: {
                label: 'Welcome'
            },
            admin: {
                label: 'Admin'
            },
            cockpit: {
                label: 'Cockpit'
            }
        };
        var loginErrorsTranslation = translateWithDefault({
            LOGIN_ERROR_MSG: 'Wrong credentials or missing access rights to application',
            LOGIN_FAILED: 'Login Failed'
        });
        $rootScope.showBreadcrumbs = false;
        var autofocusField = $('form[name="signinForm"] [autofocus]')[0];
        if (autofocusField) {
            autofocusField.focus();
        }
        $scope.login = function () {
            AuthenticationService.login($scope.username, $scope.password).then(function () {
            }).catch(function (error) {
                delete $scope.username;
                delete $scope.password;
                return loginErrorsTranslation.then(function (loginError) {
                    console.log((error.data && error.data.message) || loginError.LOGIN_ERROR_MSG);
                });
            });
        };
}]).controller('filesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal', function ($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal) {
        var camClient = new CamSDK.Client({
                mock: false,
                apiUri: '/camunda/api/engine/'
            });

            $rootScope.currentPage = {
                name: 'processes'
            };
}]).controller('statisticsCtrl', ['$scope', '$rootScope', '$filter', '$http', '$state', '$stateParams', '$q', '$location', 'AuthenticationService',
    function ($scope, $rootScope, $filter, $http, $state, $stateParams, $q, $location, AuthenticationService) {


        if(!$rootScope.hasGroup('statistics_revision') && !$rootScope.hasGroup('statistics_monthlyact') && !$rootScope.hasGroup('infrastructure_revision_users') && !$rootScope.hasGroup('infrastructure_monthly_act_users')){
            $state.go('tasks');
        }

        $rootScope.currentPage = {
            name: 'statistics'
        };

        if (window.require) {
            $scope.XLSX = require('xlsx');
        }

        $scope._ = window._;

        $rootScope.logout = function () {
            AuthenticationService.logout().then(function () {
                $scope.authentication = null;
            });
        }

        $scope.baseUrl = '/camunda/api/engine/engine/default';
        $scope.report_ready = false;

        $scope.reportsMap = {
            'revision-open-tasks': {name: 'Revision Works Statistics Grouped', process: 'Revision'},
            'invoice-open-tasks': {name: 'Monthly Act open tasks', process: 'Invoice'},
            '4gSharing-open-tasks': {name: '4G Site Sharing open tasks', process: 'SiteSharingTopProcess'}
        };

        $scope.reports = [
            'revision-open-tasks',
            'invoice-open-tasks',
            '4gSharing-open-tasks'
        ];

        $scope.contractList = [
            { id: 'all', value: 'All' },
            { id: 'old', value: 'Old' },
            { id: 'new', value: 'New' },
        ];

        $scope.subContractorList = [
            { id: 'all', value: 'All' },
            { id: 'KR', value: 'Kcell_region' },
            { id: 'Logycom', value: 'Logycom' },
            { id: 'ALTA_Tel', value: 'Alta Tel' },
            { id: 'Arlan_SI', value: 'Arlan Si' },
            { id: 'Line_Eng', value: 'Line Eng' },
            { id: '##', value: 'Not Assigned'},
        ];

        $scope.regionList = [
            { id: 'all', value: 'All' },
            { id: 'Alm', value: 'Almaty' },
            { id: 'Astana', value: 'Astana' },
            { id: 'N&C', value: 'N&C' },
            { id: 'East', value: 'East' },
            { id: 'South', value: 'South' },
            { id: 'West', value: 'West' },
        ];

        $scope.unitList = [
            { id: 'all', value: 'All' },
            { id: 'P&O', value: 'P&O' },
            { id: 'SAO', value: 'SAO' },
            { id: 'S&FM', value: 'S&FM' },
            { id: 'TNU', value: 'TNU' },
            { id: 'RO', value: 'Roll-out' },
        ];

        $scope.headList = [
            { id: 'almaty', value: 'Almaty', code: 'Alm' },
            { id: 'astana', value: 'Astana', code: 'Astana' },
            { id: 'north_central', value: 'North&Central', code: 'N&C' },
            { id: 'east', value: 'East', code: 'East' },
            { id: 'south', value: 'South', code: 'South' },
            { id: 'west', value: 'West', code: 'West' },
        ];

        $scope.revisionTaskDisplay = {
            'approve_jr_regions': 'Wait Region Head Approval',
            'check_power': 'Wait Power Checking',
            'approve_jr': 'Wait Central Unit Approval',
            'approve_transmission_works': 'Wait Central Unit Approval (Transmission works)',
            'approve_jr_budget': 'Wait Budget Approval (Transmission works)',
            'update_leasing_status_special': 'Wait Central Acquisition Approval',
            'update_leasing_status_general': 'Wait Region Acquisition Approval',
            'modify_jr': 'Wait Modify JR',
            'approve_material_list_region': 'Wait Material List Approval by Initiator',
            'approve_material_list_center_po': 'Wait Material List Approval by Central Unit (P&O)',
            'approve_material_list_center_tr': 'Wait Material List Approval by Central Unit (TNU)',
            'approve_material_list_center_fm': 'Wait Material List Approval by Central Unit (S&FM)',
            'approve_material_list_center_op': 'Wait Material List Approval by Central Unit (SAO)',
            'approve_material_list_center1': 'Wait Material List Approval by Central Unit',
            'approve_material_list_tnu_region': 'Wait Material List Approval by TNU (Region)',
            'validate_tr': 'Wait TR Validation', //validate_tr
            'validate_tr_bycenter_po': 'Wait TR Validation by Central unit (P&O)',
            'validate_tr_bycenter_tr': 'Wait TR Validation by Central unit (TNU)',
            'validate_tr_bycenter_fm': 'Wait TR Validation by Central unit (S&FM)',
            'validate_tr_bycenter_op': 'Wait TR Validation by Central unit (SAO)',
            'set_materials_dispatch_status_alm': 'Wait Materials Dispatch (Alm)',
            'set_materials_dispatch_status_astana': 'Wait Materials Dispatch (Astana)',
            'set_materials_dispatch_status_atyrau': 'Wait Materials Dispatch (Atyrau)',
            'set_materials_dispatch_status_aktau': 'Wait Materials Dispatch (Aktau)',
            'set_materials_dispatch_status_aktobe': 'Wait Materials Dispatch (Aktobe)',
            'set_materials_dispatch_status': 'Wait Materials Dispatch',
            'approve_additional_material_list_region': 'Wait Additional Material List Approval by Initiator',
            'approve_additional_material_list_tnu_region': 'Wait Additional Material List Approval by TNU (Region)',
            'approve_additional_material_list_center1': 'Wait Additional Material List Approval by Central Unit',
            'approve_additional_material_list_center_po': 'Wait Additional Material List Approval by Central Unit (P&O)',
            'approve_additional_material_list_center_tr': 'Wait Additional Material List Approval by Central Unit (TNU)',
            'approve_additional_material_list_center_fm': 'Wait Additional Material List Approval by Central Unit (S&FM)',
            'approve_additional_material_list_center_op': 'Wait Additional Material List Approval by Central Unit (SAO)',
            'validate_additional_tr': 'Wait Additional TR Validation',
            'validate_additional_tr_bycenter_po': 'Wait Additional TR Validation by Central unit (P&O)',
            'validate_additional_tr_bycenter_tr': 'Wait Additional TR Validation by Central unit (TNU)',
            'validate_additional_tr_bycenter_fm': 'Wait Additional TR Validation by Central unit (S&FM)',
            'validate_additional_tr_bycenter_op': 'Wait Additional TR Validation by Central unit (SAO)',
            'set_additional_materials_dispatch_status_alm': 'Wait Additional Materials Dispatch (Alm)',
            'set_additional_materials_dispatch_status_astana': 'Wait Additional Materials Dispatch (Astana)',
            'set_additional_materials_dispatch_status_atyrau': 'Wait Additional Materials Dispatch (Atyrau)',
            'set_additional_materials_dispatch_status_aktau': 'Wait Additional Materials Dispatch (Aktau)',
            'set_additional_materials_dispatch_status_aktobe': 'Wait Additional Materials Dispatch (Aktobe)',
            'set_additional_materials_dispatch_status': 'Wait Additional Materials Dispatch',
            'verify_works': 'Wait Verify Works',
            'accept_work_initiator': 'Wait Acceptance of Performed Works by Initiator',
            'accept_work_maintenance_group': 'Wait Acceptance of Performed Works by Maintenance Group',
            'accept_work_planning_group': 'Wait Acceptance of Performed Works by Planing Group',
            'sign_region_head': 'Wait Acceptance of Performed Works by Region Head',
            'attach-scan-copy-of-acceptance-form': 'Wait Attach of scan copy of Acceptance Form',
            'upload_tr_contractor': 'Wait for Upload TR', //upload_tr_contractor
            'upload_additional_tr_contractor': 'Wait Additional for Upload TR',
            'attach_material_list_contractor': 'Wait for Attach Material List by Contractor', //attach_material_list_contractor
            'attach_additional_material_list_contractor': 'Wait for Additional Attach Material List by Contractor',
            'fill_applied_changes_info': 'Wait for Fill Applied Changes Info' //fill_applied_changes_info
        }

        $scope.contractFilter = $stateParams.contractFilter ? $stateParams.contractFilter : null;
        $scope.regionFilter = $stateParams.regionFilter ? $stateParams.regionFilter : null;
        $scope.subContractorFilter = $stateParams.subContractorFilter ? $stateParams.subContractorFilter: null;
        $scope.unitFilter = $stateParams.unitFilter ? $stateParams.unitFilter : null;
        $scope.currentReport = $stateParams.report;
        $scope.reverseOrder = false;
        $scope.fieldName = 'Region';
        $scope.task = $stateParams.task;

        $scope.filter = {};
        if ($stateParams.reason) {
            $scope.filter.reason = $stateParams.reason;
        }
        if ($stateParams.mainContract) {
            $scope.filter.mainContract = $stateParams.mainContract;
        }
        $scope.region = $stateParams.region;
        $scope.subContractor = $stateParams.subContractor;
        $scope.unitR = $stateParams.unitR;

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
                } else if (jrNumber.startsWith("E-")) {
                    return 'east';
                } else if (jrNumber.startsWith("N&C")) {
                    return 'north_central';
                } else if (jrNumber.startsWith("South")) {
                    return 'south';
                } else if (jrNumber.startsWith("S-")) {
                    return 'south';
                } else if (jrNumber.startsWith("West")) {
                    return 'west';
                } else if (jrNumber.startsWith("W-")) {
                    return 'west';
                } else if (jrNumber.startsWith("Astana")) {
                    return 'astana';
                } else if (jrNumber.startsWith("Ast-")) {
                    return 'astana';
                } else {
                    return 'no_region';
                }
            } else {
                return 'no_region';
            }
        };

        $scope.getSubContractor = function (jrNumber) {
            if (jrNumber) {
                if (jrNumber.indexOf("Arlan_SI") !== -1) {
                    return 'arlan_si';
                } else if (jrNumber.indexOf("Logycom") !== -1) {
                    return 'logycom';
                } else if ((jrNumber.indexOf("LSE") !== -1) || (jrNumber.indexOf("Line_Eng") !== -1)) {
                    return 'line_eng';
                } else if (jrNumber.indexOf("ALTA_Tel") !== -1) {
                    return 'alta_tel';
                } else if (jrNumber.indexOf("KR") !== -1) {
                    return 'kcell_region';
                } else {
                    return 'not_assigned'
                }
            } else {
                return null
            }
        };

        $scope.getUnit = function(jrNumber) {
            if (jrNumber) {
                if (jrNumber.indexOf("SAO") !== -1) {
                    return 'sao';
                } else if (jrNumber.indexOf("P&O") !== -1) {
                    return 'po';
                } else if (jrNumber.indexOf("TNU") !== -1) {
                    return 'tnu';
                } else if (jrNumber.indexOf("S&FM") !== -1) {
                    return 'sfm';
                } else if (jrNumber.indexOf("RO") !== -1) {
                    return 'ro'
                }
            }
        };

        $scope.orderByFieldName = function (fieldName) {
            console.log(fieldName)
            if ($scope.fieldName == fieldName) {
                $scope.reverseOrder = !$scope.reverseOrder;
            } else {
                $scope.reverseOrder = false;
                $scope.fieldName = fieldName;
            }
        };

        $scope.orderMaFunction = function (task) {
            if ($scope.fieldName === 'Region') {
                return $scope.getInvoiceRegion(task.variables.invoiceNumber.value);
            } else if ($scope.fieldName === 'Act') {
                if (task.variables.monthActNumber) {
                    return task.variables.monthActNumber.value;
                } else {
                    return task.variables.invoiceNumber.value;
                }
            } else if ($scope.fieldName === 'Typeofwork') {
                return $scope.reasonsTitle[task.variables.workType.value];
            } else if ($scope.fieldName === 'Period') {
                return task.variables.monthOfFormalPeriod.value + ' ' + task.variables.yearOfFormalPeriod.value;
            } else if ($scope.fieldName === 'Contractor') {
                return 'TOO Line System Engineering';
            } else if ($scope.fieldName === 'Requestedby') {
                return task.variables.starter.value;
            } else if ($scope.fieldName === 'Currentassignee') {
                if (task.assignee) {
                    return task.assignee;
                } else {
                    return task.groupId;
                }
            }
        }

        $scope.orderByRevisionFieldName = function (revisionFieldName) {
            if ($scope.revisionFieldName == revisionFieldName) {
                $scope.revisionReverseOrder = !$scope.revisionReverseOrder;
            } else {
                $scope.revisionReverseOrder = false;
                $scope.revisionFieldName = revisionFieldName;
            }
        };

        $scope.orderRevisionFunction = function (task) {
            if ($scope.revisionFieldName === 'Region') {
                return $scope.getJrRegion(task.variables.jrNumber.value);
            } else if ($scope.revisionFieldName === 'site_name') {
                return task.variables.site_name.value;
            } else if ($scope.revisionFieldName === 'jrNumber') {
                return task.variables.jrNumber.value;
            } else if ($scope.revisionFieldName === 'contractor') {
                return $scope.contractorsTitle[task.variables.contractor.value];
            } else if ($scope.revisionFieldName === 'reason') {
                return $scope.reasonsTitle[task.variables.reason.value];
            } else if ($scope.revisionFieldName === 'project') {
                if (task.variables.project) {
                    return task.variables.project.value;
                } else {
                    return 'Z';
                }
            } else if ($scope.revisionFieldName === 'starter') {
                return task.variables.starter.value;
            } else if ($scope.revisionFieldName === 'requestedDate') {
                return task.variables.requestedDate.value;
            } else if ($scope.revisionFieldName === 'validityDate') {
                return task.variables.validityDate.value;
            } else if ($scope.revisionFieldName === 'assignee') {
                return task.assignee ? task.assignee : task.groupId;
            }
        }

        $scope.getInvoiceRegion = function (invoiceNumber) {
            if (invoiceNumber.endsWith('-RO-1')) {
                invoiceNumber = invoiceNumber.replace('-RO-1', '');
            }
            if (invoiceNumber.endsWith('-RO-2')) {
                invoiceNumber = invoiceNumber.replace('-RO-2', '');
            }
            if (invoiceNumber.endsWith('-RO-3')) {
                invoiceNumber = invoiceNumber.replace('-RO-3', '');
            }
            if (invoiceNumber.endsWith('-RO-4')) {
                invoiceNumber = invoiceNumber.replace('-RO-4', '');
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

        $scope.filterRegion = function (task) {
            if ($scope.region) {
                if ($scope.getProcessDefinition() === 'Revision') {
                    return $scope.getJrRegion(task.variables.jrNumber.value) === $scope.region;
                } else if ($scope.getProcessDefinition() === 'Invoice') {
                    return $scope.getInvoiceRegion(task.variables.invoiceNumber.value) === $scope.region;
                }
            } else {
                return true;
            }
        }

        $scope.filterSubContractor = function (task) {
            if ($scope.subContractor) {
                if ($scope.getProcessDefinition() === 'Revision') {
                    console.log($scope.getSubContractor(task.variables.jrNumber.value) + ' || ' + $scope.subContractor)
                    return $scope.getSubContractor(task.variables.jrNumber.value) === $scope.subContractor;
                }
            } else {
                return true;
            }
        }

        $scope.filterUnitR = function (task) {
            if ($scope.unitR) {
                if ($scope.getProcessDefinition() === 'Revision') {
                    return $scope.getUnit(task.variables.jrNumber.value) === $scope.unitR;
                }
            } else {
                return true;
            }
        }

        $scope.getProcessDefinition = function () {
            if ($scope.currentReport === 'revision-open-tasks') {
                return 'Revision';
            } else if ($scope.currentReport === 'invoice-open-tasks') {
                return 'Invoice';
            } else if ($scope.currentReport === '4gSharing-open-tasks') {
                return 'SiteSharingTopProcess';
            }
        }

        $scope.regions = ['.almaty', '.astana', '.north_central', '.east', '.south', '.west'];
        $scope.regionsFiltered = ['.almaty', '.astana', '.north_central', '.east', '.south', '.west'];
        $scope.subContractors = ['.kcell_region', '.logycom', '.alta_tel', '.arlan_si', '.line_eng', '.not_assigned'];
        $scope.unitsR = ['.sao', '.po', '.sfm', '.tnu', '.ro'];

        $scope.checkRegionView = function(region){
            if($rootScope.hasGroup('head_kcell_users') || $rootScope.hasGroup('statistics_revision') || $rootScope.hasGroup('statistics_monthlyact')){
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
            } else if($rootScope.contractorRegion('logycom')) {
                return region === '.logycom';
            } else if($rootScope.contractorRegion('alta')) {
                return region === '.alta_tel';
            } else if($rootScope.contractorRegion('arlan')) {
                return region === '.arlan_si';
            } else if($rootScope.contractorRegion('lse')) {
                return region === '.line_eng';
            }
            else {
                return false;
            }
        }

        $scope.contractFilterSelected = function(val) {
            $scope.contractFilter = val;
            $scope.drowStatistics();
        }

        $scope.regionFilterSelected = function(val) {
            $scope.regionFilter = val;
            $scope.headRegion = _.find($scope.headList, r => r.code === val)
            console.log($scope.headRegion)
            if (val !== 'all') {
                $scope.regionsFiltered = $scope.regions.filter(el => el.indexOf($scope.headRegion.id) !== -1)
            } else {
                $scope.regionsFiltered = [...$scope.regions];
            }
            console.log($scope.regionsFiltered)
            $scope.drowStatistics();
        }

        $scope.subContractorFilterSelected = function(val) {
            $scope.subContractorFilter = val;
            $scope.drowStatistics();
        }

        $scope.unitFilterSelected = function(val) {
            $scope.unitFilter = val;
            $scope.drowStatistics();
        }

        $scope.updateTaskDefinitions = function () {
            var processDefinition = $scope.getProcessDefinition();

            $http.get($scope.baseUrl + '/process-definition/key/' + processDefinition + '/xml')
                .then(function (response) {
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

                    var userTasks = getUserTasks(xml);
                    var userTasksMap = _.keyBy(userTasks, 'id');
                    $scope.userTasksMap = userTasksMap;
                });
        }


        $scope.drowStatistics = function () {
            if ($scope.currentReport === '4gSharing-open-tasks') {
                $http.get('/asset-management/api/plans/search/findCurrentPlanSites').then(function (response) {
                    var currentPlans = _.flatMap(_.filter(response.data._embedded.plans, function (plan) {
                        return plan.status !== 'site_sharing_complete';
                    }), function (plan) {
                        if (plan.status !== 'site_sharing_complete') {
                            var range = [];
                            _.forEach(plan.params.shared_sectors, function (sector) {
                                if (!_.includes(range, sector.enodeb_range)) {
                                    range.push(sector.enodeb_range);
                                }
                            });
                            return {
                                "site_id": plan.site_id,
                                "status": plan.status,
                                "host": plan.params.host,
                                "range": range
                            };
                        } else return null;
                    });


                    if (currentPlans) {
                        console.log(currentPlans);
                        //----------------------------------------------------------
                        var siteCountByType = {
                            'planed': {"band800": 0, "band1800": 0, "band2100": 0, "all": 0},
                            'onair': {"band800": 0, "band1800": 0, "band2100": 0, "all": 0},
                            'kcellHost': {"band800": 0, "band1800": 0, "band2100": 0, "all": 0},
                            'beelineHost': {"band800": 0, "band1800": 0, "band2100": 0, "all": 0}
                        };
                        // для подсчета статистики по шеринг сайтам
                        //-----------------------------------------------------------
                        _.flatMap(currentPlans, function (plan) {
                            _.forEach([800, 1800, 2100, 'all'], function (band) {
                                if (band === 'all') {
                                    siteCountByType.planed.all += 1;
                                    if (plan.status === 'site_on_air') {
                                        siteCountByType.onair.all += 1;
                                    } else if (plan.status === 'site_accepted_service') {
                                        if (plan.host.toLowerCase() === 'beeline') {
                                            siteCountByType.beelineHost.all += 1;
                                        } else if (plan.host.toLowerCase() === 'kcell') {
                                            siteCountByType.kcellHost.all += 1;
                                        }
                                    }
                                } else if (_.includes(plan.range, band)) {
                                    siteCountByType.planed['band' + band] += 1;
                                    if (plan.status === 'site_on_air') {
                                        siteCountByType.onair['band' + band] += 1;
                                    } else if (plan.status === 'site_accepted_service') {
                                        if (plan.host.toLowerCase() === 'beeline') {
                                            siteCountByType.beelineHost['band' + band] += 1;
                                        } else if (plan.host.toLowerCase() === 'kcell') {
                                            siteCountByType.kcellHost['band' + band] += 1;
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
                for (let pid of ['BeelineHostBeelineSite', 'BeelineHostKcellSite', 'KcellHostBeelineSite', 'KcellHostKcellSite', 'ReplanSharedSiteAddressPlan']) {
                    var userTasksPromise = $http.get($scope.baseUrl + '/process-definition/key/' + pid + '/xml')
                        //var userTasksPromise = $http.get($scope.baseUrl + '/process-definition/key/Revision/xml')
                        .then(function (response) {
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

                            var userTasks = getUserTasks(xml);
                            var userTasksMap = _.keyBy(userTasks, 'id');
                            //$scope.userTasksMap = userTasksMap;
                            $scope.userTasksMap = _.assign($scope.userTasksMap, userTasksMap);
                        });
                }

                if ($scope.task) {
                    $http.post($scope.baseUrl + '/history/task', {
                        taskDefinitionKey: $scope.task,
                        unfinished: true
                    }).then(function (response) {
                        var tasks = response.data;
                        var processInstanceIds = _.map(tasks, 'processInstanceId');
                        return $http.post($scope.baseUrl + '/history/variable-instance/?deserializeValues=false', {
                            processInstanceIdIn: processInstanceIds
                        }).then(function (response) {
                            var variables = _.flatMap(response.data, function (pi) {
                                if (pi.type === 'Json') {
                                    return {
                                        "name": pi.name,
                                        "value": JSON.parse(pi.value),
                                        "processInstanceId": pi.processInstanceId
                                    }
                                } else return {
                                    "name": pi.name,
                                    "value": pi.value,
                                    "processInstanceId": pi.processInstanceId
                                }
                            })
                            var variablesByProcessInstance = _.groupBy(variables, 'processInstanceId');

                            return _.map(tasks, function (task) {
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
                        for (let i = 0; i < $scope.tasks.length; i++) {
                            $http.get(`${$scope.baseUrl}/task/${$scope.tasks[i].id}/identity-links`).then((response) => {
                                var groups = response.data
                                var groupList = ''
                                for (let j = 0; j < groups.length; j++) {
                                    if (groups[j].groupId) {
                                        groupList += groups[j].groupId
                                        if (groups.length > 1) {
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
                            "pid": "BeelineHostBeelineSite",
                            "tasks": [
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
                            ]
                        },
                        {
                            "title": "Beeline Host - Kcell Site",
                            "pid": "BeelineHostKcellSite",
                            "tasks": [
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
                            ]
                        },
                        {
                            "title": "Kcell Host - Beeline Site",
                            "pid": "KcellHostBeelineSite",
                            "tasks": [
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
                            ]
                        },
                        {
                            "title": "Kcell Host - Kcell Site",
                            "pid": "KcellHostKcellSite",
                            "tasks": [
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
                            "pid": "ReplanSharedSiteAddressPlan",
                            "tasks": [
                                'rss_task_update_shared_site_address_plan',
                                'rss_task_accept_or_reject_address_plan_modification'
                            ]
                        }

                    ];


                    var processInstances = {};
                    var taskInstances = [];
                    for (let sharingPiD of ['BeelineHostBeelineSite', 'BeelineHostKcellSite', 'KcellHostBeelineSite', 'KcellHostKcellSite', 'ReplanSharedSiteAddressPlan']) {
                        var processInstancesPromise = $http.post($scope.baseUrl + '/process-instance', {
                            "processDefinitionKey": sharingPiD,
                            "unfinished": true
                        }).then(function (response) {
                            if (response.data.length > 0) {
                                var processInstances = _.keyBy(response.data, 'id');

                                return $http.post($scope.baseUrl + '/history/variable-instance/?deserializeValues=false', {
                                    variableName: 'sharingPlan',
                                    processInstanceIdIn: _.keys(processInstances)
                                }).then(function (response) {
                                    var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
                                    var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
                                    var result = _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'sharingPlan': JSON.parse(valueByProcessInstance[id])}));
                                    // console.log(result);
                                    return result;
                                });
                            } else {
                                return [];
                            }
                            ;

                        });
                        var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', {
                            "processDefinitionKey": sharingPiD,
                            "unfinished": true
                        }).then(function (response) {
                            return response.data;
                        });
                        $q.all([processInstancesPromise, taskInstancesPromise])
                            .then(function (results) {
                                processInstances = _.assign(processInstances, results[0]);
                                //taskInstances = _.assign(taskInstances, results[1]);
                                taskInstances = _.concat(taskInstances, results[1]);
                                var taskInstancesByDefinition = _.groupBy(
                                    taskInstances,
                                    'taskDefinitionKey'
                                );

                                var tasksByIdAndRegionGrouped = _.mapValues(
                                    taskInstancesByDefinition,
                                    function (tasks) {
                                        return _.groupBy(
                                            tasks,
                                            function (task) {
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
                                    function (tasks) {
                                        return _.mapValues(tasks, 'length');
                                    }
                                );
                                $scope.tasksByIdAndRegionCounted = tasksByIdAndRegionCounted;

                                let a = Object.keys(tasksByIdAndRegionCounted);
                                let newJson = {};
                                for (let i = 0; i < a.length; i++) {
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
            } else {
                console.log("HERE");
                if ($scope.task) {
                    let query = {
                        taskDefinitionKey: $scope.task,
                        processDefinitionKey: $scope.getProcessDefinition(),
                        unfinished: true
                    };

                    if ($scope.task === 'no_task') {
                        delete query.taskDefinitionKey;
                    }

                    if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                        query.variables = []
                        query.activityIdIn = [$scope.task]
                    } else {
                        query.processVariables = []
                    }

                    if ($scope.currentReport === 'revision-open-tasks' && ($scope.task.endsWith('_po') || $scope.task.endsWith('_op') || $scope.task.endsWith('_tr') || $scope.task.endsWith('_fm'))) {
                        query.taskDefinitionKey = $scope.task.substring(0, $scope.task.length - 3);

                        var revisionTaskName = {
                            'approve_material_list_center_po': 'Approve Material List by "P&O"',
                            'approve_material_list_center_tr': 'Approve Material List by "Transmission"',
                            'approve_material_list_center_fm': 'Approve Material List by "S&FM"',
                            'approve_material_list_center_op': 'Approve Material List by "Operation"',
                            'validate_tr_bycenter_po': 'Validate TR by Center by "P&O"',
                            'validate_tr_bycenter_tr': 'Validate TR by Center by "Transmission"',
                            'validate_tr_bycenter_fm': 'Validate TR by Center by "S&FM"',
                            'validate_tr_bycenter_op': 'Validate TR by Center by "Operation"',
                            'validate_additional_tr_bycenter_po': 'Validate Additional TR by Center by "P&O"',
                            'validate_additional_tr_bycenter_tr': 'Validate Additional TR by Center by "Transmission"',
                            'validate_additional_tr_bycenter_fm': 'Validate Additional TR by Center by "S&FM"',
                            'validate_additional_tr_bycenter_op': 'Validate Additional TR by Center by "Operation"',
                            'approve_additional_material_list_center_po': 'Approve Additional Material List by "P&O"',
                            'approve_additional_material_list_center_tr': 'Approve Additional Material List by "Transmission"',
                            'approve_additional_material_list_center_fm': 'Approve Additional Material List by "S&FM"',
                            'approve_additional_material_list_center_op': 'Approve Additional Material List by "Operation"',
                        }

                        query.taskName = revisionTaskName[$scope.task];
                    }
                    if ($scope.contractFilter !== null && $scope.contractFilter !== 'all') {
                        if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                            query.variables.push({
                                name: 'mainContract',
                                operator: $scope.contractFilter === 'new' ? 'eq' : 'neq',
                                value: 'Roll-outRevision2020'
                            })
                        } else {
                            query.processVariables.push({
                                name: 'mainContract',
                                operator: $scope.contractFilter === 'new' ? 'eq' : 'neq',
                                value: 'Roll-outRevision2020'
                            })
                        }
                    }
                    // var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', taskQuery).then(function (response) {
                    //     return response.data;
                    // });
                    if ($scope.filter.reason) {
                        if ($scope.getProcessDefinition() === 'Revision') {
                            if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                                query.variable = [{
                                    name: 'reason',
                                    operator: 'eq',
                                    value: $scope.filter.reason
                                }];
                            } else {
                                query.processVariables = [{
                                    name: 'reason',
                                    operator: 'eq',
                                    value: $scope.filter.reason
                                }];
                            }
                        } else if ($scope.getProcessDefinition() === 'Invoice') {
                            query.processVariables = [{name: 'workType', operator: 'eq', value: $scope.filter.reason}];
                        }
                    }
                    // if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                    //     query.processVariables.push({
                    //         name: 'mainContract',
                    //         operator: 'eq',
                    //         value: $scope.filter.mainContract
                    //     });
                    // }
                    if ($scope.region) {
                        var region = $scope.region === 'north_central' ? 'nc' : ($scope.region === 'almaty' ? 'alm' : $scope.region);
                        if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                            query.variables.push({name: 'siteRegion', operator: 'eq', value: region});
                        } else {
                            query.processVariables.push({name: 'siteRegion', operator: 'eq', value: region});
                        }
                    }
                    if ($scope.subContractor && ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed')) {
                        var contractorMap = {'all': 'all',
                        'kcell_region': 'KR',
                        'logycom': 'Logycom',
                        'alta_tel': 'ALTA_Tel',
                        'arlan_si': 'Arlan_SI',
                        'line_eng': 'Line_Eng',
                        'not_assigned': '##'
                        };

                        query.variables.push({
                            name: 'jrNumber',
                            operator: 'like',
                            value: '%-' + contractorMap[$scope.subContractor] + '-%'
                        });
                    }
                    if ($scope.task === 'no_task') {
                        var searchString ='';
                        if ($scope.regionFilter && $scope.regionFilter !== 'all') {
                            searchString = $scope.regionFilter.replace('Astana','Ast') + '%';
                        }
                        if ($scope.subContractorFilter && $scope.subContractorFilter !== 'all') {
                            if ($scope.regionFilter && $scope.regionFilter !== 'all') {
                                searchString = $scope.regionFilter.replace('Astana','Ast') + '-' + $scope.subContractorFilter + '%'
                            } else {
                                searchString = '%' + $scope.subContractorFilter + '%'
                            }
                        }
                        if ($scope.unitFilter && $scope.unitFilter !== 'all') {
                            if ($scope.subContractorFilter && $scope.subContractorFilter !== 'all') {
                                if ($scope.regionFilter && $scope.regionFilter !== 'all') {
                                    searchString = $scope.regionFilter + '-' + $scope.subContractorFilter + '-' + $scope.unitFilter + '%'
                                } else {
                                    searchString = '%' + $scope.subContractorFilter + '-' + $scope.unitFilter + '%'
                                }
                            } else {
                                if ($scope.regionFilter && $scope.regionFilter !== 'all') {
                                    searchString = $scope.regionFilter + '-%-' + $scope.unitFilter + '%'
                                } else {
                                    searchString = '%' + $scope.unitFilter + '%'
                                }
                            }

                        }

                        query.processVariables.push({
                            name: 'jrNumber',
                            operator: 'like',
                            value: searchString
                        });
                    }
                    console.log(query)

                    var filterUrl = $scope.baseUrl + '/history/task';

                    if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                        filterUrl = $scope.baseUrl + '/process-instance';
                    }

                    $http.post(filterUrl, query).then(function (response) {
                        var tasks = response.data;
                        var processInstanceIds = _.map(tasks, 'processInstanceId');
                        if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                            processInstanceIds = _.map(tasks, 'id');
                        }
                        console.log(processInstanceIds);
                        if(processInstanceIds.length > 0){
                            return $http.post($scope.baseUrl + '/history/variable-instance', {
                                processInstanceIdIn: processInstanceIds
                            }).then(function (response) {
                                var variables = response.data;
                                var variablesByProcessInstance = _.groupBy(variables, 'processInstanceId');
                                console.log(variablesByProcessInstance);
                                console.log(tasks);

                                if ($scope.task === 'intermediate_wait_invoiced' || $scope.task === 'intermediate_wait_acts_passed') {
                                    return _.map(tasks, function (task) {
                                        return _.assign({}, task, {
                                            variables: _.keyBy(
                                                variablesByProcessInstance[task.id],
                                                'name'
                                            )
                                        });
                                    });
                                } else {
                                    return _.map(tasks, function (task) {
                                        return _.assign({}, task, {
                                            variables: _.keyBy(
                                                variablesByProcessInstance[task.processInstanceId],
                                                'name'
                                            )
                                        });
                                    });
                                }
                            });
                        }
                    }).then(function (tasks) {
                        console.log(tasks);
                        $scope.tasks = tasks.filter(function (task) {
                            var jr = task.variables.jrNumber.value;
                            jr = jr.replace('Ast-','Astana-')
                            return ($scope.regionFilter ? ($scope.regionFilter === 'all' ? true : jr.indexOf($scope.regionFilter) > -1) : true) &&
                                ($scope.subContractorFilter ? ($scope.subContractorFilter === 'all' ? true : jr.indexOf($scope.subContractorFilter) > -1) : true) &&
                                ($scope.unitFilter ? ($scope.unitFilter === 'all' ? true : jr.indexOf($scope.unitFilter) > -1) : true)
                        })
                        for (let i = 0; i < $scope.tasks.length; i++) {
                            if ($scope.task !== 'intermediate_wait_invoiced' && $scope.task !== 'intermediate_wait_acts_passed') {
                                $http.get(`${$scope.baseUrl}/task/${$scope.tasks[i].id}/identity-links`).then((response) => {
                                    var groups = response.data
                                    var groupList = ''
                                    for (let j = 0; j < groups.length; j++) {
                                        if (groups[j].groupId) {
                                            groupList += groups[j].groupId
                                            if (groups.length > 1) {
                                                groupList += ', '
                                            }
                                        }
                                    }
                                    $scope.tasks[i].groupId = groupList
                                })
                            }
                        }
                    });


                } else if ($scope.currentReport) {
                    $scope.updateTaskDefinitions();

                    $scope.revisionTaskDisplay = {
                        'approve_jr_regions': 'Wait Region Head Approval',
                        'check_power': 'Wait Power Checking',
                        'approve_jr': 'Wait Central Unit Approval',
                        'approve_transmission_works': 'Wait Central Unit Approval (Transmission works)',
                        'approve_optical_transmission_works': 'Wait Central Unit Approval (Transmission works - optical)',
                        'approve_jr_budget': 'Wait Budget Approval (Transmission works)',
                        'update_leasing_status_special': 'Wait Central Acquisition Approval',
                        'update_leasing_status_general': 'Wait Region Acquisition Approval',
                        'modify_jr': 'Wait Modify JR',
                        'attach_ml_inhouse': 'Wait for material list attach by Kcell',
                        'approve_material_list_region': 'Wait Material List Approval by Initiator',
                        'approve_material_list_tnu_region': 'Wait Material List Approval by TNU (Region)',
                        'approve_material_list_center_po': 'Wait Material List Approval by Central Unit (P&O)',
                        'approve_material_list_center_op': 'Wait Material List Approval by Central Unit (SAO)',
                        'approve_material_list_center_tr': 'Wait Material List Approval by Central Unit (TNU)',
                        'approve_material_list_center_fm': 'Wait Material List Approval by Central Unit (S&FM)',
                        'approve_material_list_center_ro': 'Wait Material List Approval by Central Unit (Roll-out)',
                        'approve_material_list_center1': 'Wait Material List Approval by Center', //нет в задаче 1324
                        'check_ml_rollout': 'Wait Check Material List by Center Rollout Group',
                        'attach_tr_inhouse': 'Wait Upload TR by Initiator',
                        'validate_tr': 'Wait TR Validation', //validate_tr
                        'validate_tr_bycenter_po': 'Wait TR Validation by Central unit (P&O)',
                        'validate_tr_bycenter_op': 'Wait TR Validation by Central unit (SAO)',
                        'validate_tr_bycenter_tr': 'Wait TR Validation by Central unit (TNU)',
                        'validate_tr_bycenter_fm': 'Wait TR Validation by Central unit (S&FM)',
                        'validate_tr_bycenter_ro': 'Wait TR Validation by Central unit (Roll-out)',
                        'check_tr_rollout': 'Wait TR Check by Center Rollout Group',
                        'set_materials_dispatch_status_alm': 'Wait Materials Dispatch (Alm)',
                        'set_materials_dispatch_status_astana': 'Wait Materials Dispatch (Astana)',
                        'set_materials_dispatch_status_atyrau': 'Wait Materials Dispatch (Atyrau)',
                        'set_materials_dispatch_status_aktau': 'Wait Materials Dispatch (Aktau)',
                        'set_materials_dispatch_status_aktobe': 'Wait Materials Dispatch (Aktobe)',
                        'attach_ml_inhouse_additional': 'Wait additional material list attach by Kcell',
                        'set_materials_dispatch_status': 'Wait Materials Dispatch',
                        'approve_additional_material_list_region': 'Wait Additional Material List Approval by Initiator',
                        'approve_additional_material_list_tnu_region': 'Wait Additional Material List Approval by TNU (Region)',
                        'approve_additional_material_list_center1': 'Wait Additional Material List Approval by Central Unit',
                        'approve_additional_material_list_center_po': 'Wait Additional Material List Approval by Central Unit (P&O)',
                        'approve_additional_material_list_center_tr': 'Wait Additional Material List Approval by Central Unit (TNU)',
                        'approve_additional_material_list_center_fm': 'Wait Additional Material List Approval by Central Unit (S&FM)',
                        'approve_additional_material_list_center_op': 'Wait Additional Material List Approval by Central Unit (SAO)',
                        'approve_additional_material_list_center_ro': 'Wait Additional Material List Approval by Central Unit (Roll-out)',
                        'check_ml_rollout_additional': 'Wait Additional Material List Check by Center Rollout Group',
                        'attach_tr_inhouse_additional': 'Wait Upload Additional TR by Initiator',
                        'validate_additional_tr': 'Wait Additional TR Validation',
                        'validate_additional_tr_bycenter_po': 'Wait Additional TR Validation by Central unit (P&O)',
                        'validate_additional_tr_bycenter_tr': 'Wait Additional TR Validation by Central unit (TNU)',
                        'validate_additional_tr_bycenter_fm': 'Wait Additional TR Validation by Central unit (S&FM)',
                        'validate_additional_tr_bycenter_op': 'Wait Additional TR Validation by Central unit (SAO)',
                        'validate_additional_tr_bycenter_ro': 'Wait Additional TR Validation by Central unit (Roll-out)',
                        'check_tr_rollout_additional': 'Wait Additional TR Check by Center Rollout Group',
                        'set_additional_materials_dispatch_status_alm': 'Wait Additional Materials Dispatch (Alm)',
                        'set_additional_materials_dispatch_status_astana': 'Wait Additional Materials Dispatch (Astana)',
                        'set_additional_materials_dispatch_status_atyrau': 'Wait Additional Materials Dispatch (Atyrau)',
                        'set_additional_materials_dispatch_status_aktau': 'Wait Additional Materials Dispatch (Aktau)',
                        'set_additional_materials_dispatch_status_aktobe': 'Wait Additional Materials Dispatch (Aktobe)',
                        'set_additional_materials_dispatch_status': 'Wait Additional Materials Dispatch',
                        'verify_works': 'Wait Verify Works',
                        'accept_work_initiator': 'Wait Acceptance of Performed Works by Initiator',
                        'accept_work_maintenance_group': 'Wait Acceptance of Performed Works by Maintenance Group',
                        'accept_work_planning_group': 'Wait Acceptance of Performed Works by Planing Group',
                        'sign_region_head': 'Wait Acceptance of Performed Works by Region Head',
                        'attach-scan-copy-of-acceptance-form': 'Wait Attach of scan copy of Acceptance Form',
                        'check_docs': 'Wait Check documents by Center Group',
                        'intermediate_wait_invoiced': 'Final Accepted, waiting prep. Fin act (Revision)',
                        'intermediate_wait_acts_passed': 'Final Accepted, waiting prep. Fin act (Roll-out)',
                        'upload_tr_contractor': 'Wait for Upload TR', //upload_tr_contractor
                        'upload_additional_tr_contractor': 'Wait for Upload Additional TR',
                        'attach_material_list_contractor': 'Wait for Attach Material List by Contractor', //attach_material_list_contractor
                        'attach_additional_material_list_contractor': 'Wait for Additional Material List by Contractor',
                        'fill_applied_changes_info': 'Wait for Fill Applied Changes Info' //fill_applied_changes_info
                    }

                    $scope.kcellTasks = {
                        'revision-open-tasks': {
                            'revisionJobRequestApprovalSubprocessTasks': [
                                'approve_jr_regions', //approve_jr_regions
                                'check_power', //check_power
                                'approve_jr', //approve_jr
                                'approve_transmission_works', //approve_transmission_works
                                'approve_optical_transmission_works',
                                'approve_jr_budget', //approve_jr_budget
                                'update_leasing_status_special', //update_leasing_status_special
                                'update_leasing_status_general', //update_leasing_status_general
                                'modify_jr' //modify_jr
                            ],
                            'revisionMaterialsPreparationTasks': [
                                'attach_ml_inhouse',
                                'approve_material_list_region', //approve_material_list_region
                                'approve_material_list_tnu_region',
                                'approve_material_list_center_po', //approve_material_list_center P&O
                                'approve_material_list_center_op', //approve_material_list_center Operation
                                'approve_material_list_center_tr', //approve_material_list_center Transmission
                                'approve_material_list_center_fm', //approve_material_list_center S&FM
                                'approve_material_list_center_ro',
                                'approve_material_list_center1',
                                'check_ml_rollout',
                                'attach_tr_inhouse',
                                'validate_tr', //validate_tr
                                'validate_tr_bycenter_po',
                                'validate_tr_bycenter_op',
                                'validate_tr_bycenter_tr',
                                'validate_tr_bycenter_fm',
                                'validate_tr_bycenter_ro',
                                'check_tr_rollout',
                                'set_materials_dispatch_status_alm',
                                'set_materials_dispatch_status_astana',
                                'set_materials_dispatch_status_atyrau',
                                'set_materials_dispatch_status_aktau',
                                'set_materials_dispatch_status_aktobe',
                                'attach_ml_inhouse_additional',
                                'approve_additional_material_list_region',
                                'approve_additional_material_list_tnu_region',
                                'approve_additional_material_list_center1',
                                'approve_additional_material_list_center_po',
                                'approve_additional_material_list_center_op',
                                'approve_additional_material_list_center_tr',
                                'approve_additional_material_list_center_fm',
                                'approve_additional_material_list_center_ro',
                                'check_ml_rollout_additional',
                                'attach_tr_inhouse_additional',
                                'validate_additional_tr',
                                'validate_additional_tr_bycenter_po',
                                'validate_additional_tr_bycenter_op',
                                'validate_additional_tr_bycenter_tr',
                                'validate_additional_tr_bycenter_fm',
                                'validate_additional_tr_bycenter_ro',
                                'check_tr_rollout_additional',
                                'set_additional_materials_dispatch_status_alm',
                                'set_additional_materials_dispatch_status_astana',
                                'set_additional_materials_dispatch_status_atyrau',
                                'set_additional_materials_dispatch_status_aktau',
                                'set_additional_materials_dispatch_status_aktobe',
                                'verify_works'
                            ],
                            'waitWorksVerificationPermitTeamTasks': [],
                            'revisionAcceptWorksTasks': [
                                'accept_work_initiator',
                                'accept_work_maintenance_group',
                                'accept_work_planning_group',
                                'sign_region_head',
                                'attach-scan-copy-of-acceptance-form',
                                'check_docs',
                                'intermediate_wait_invoiced',
                                'intermediate_wait_acts_passed',
                            ]
                        },
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
                        'revision-open-tasks': [
                            'attach_material_list_contractor', //attach_material_list_contractor
                            'attach_additional_material_list_contractor',
                            'upload_tr_contractor', //upload_tr_contractor
                            'upload_additional_tr_contractor',
                            'fill_applied_changes_info' //fill_applied_changes_info
                        ],
                        'invoice-open-tasks': [
                            'ma_sendtobranch',
                            'ma_modify'
                        ]
                    };

                    if ($scope.currentReport === 'revision-open-tasks') {
                        console.log('fklsjlkfjsdfds');
                        var processQuery = {
                            "processDefinitionKey": "Revision",
                            "unfinished": true,
                            "variables": []
                        };
                        if ($scope.filter.reason) {
                            processQuery.variables.push({name: 'reason', operator: 'eq', value: $scope.filter.reason});
                        }
                        if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                            processQuery.variables.push({
                                name: 'mainContract',
                                operator: 'eq',
                                value: $scope.filter.mainContract
                            });
                        }
                        var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', processQuery).then(function (response) {
                            var processInstances = _.keyBy(response.data, 'id');
                            return $http.post($scope.baseUrl + '/history/variable-instance', {
                                variableName: 'jrNumber',
                                processInstanceIdIn: _.keys(processInstances)
                            }).then(function (response) {
                                var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
                                var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
                                var result = _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'jrNumber': valueByProcessInstance[id]}));
                                return result;
                            });
                        });

                        var taskQuery = {
                            "processDefinitionKey": 'Revision',
                            "unfinished": true,
                            "processVariables": []
                        };
                        // if ($scope.filter.reason) {
                        //     taskQuery.processVariables.push({
                        //         name: 'reason',
                        //         operator: 'eq',
                        //         value: $scope.filter.reason
                        //     });
                        // }
                        // if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                        //     taskQuery.processVariables.push({
                        //         name: 'mainContract',
                        //         operator: 'eq',
                        //         value: $scope.filter.mainContract
                        //     });
                        // }
                        if ($scope.contractFilter !== null && $scope.contractFilter !== 'all') {
                            taskQuery.processVariables.push({
                                name: 'mainContract',
                                operator: $scope.contractFilter === 'new' ? 'eq' : 'neq',
                                value: 'Roll-outRevision2020'
                            })
                        }

                        var intermediate_wait_invoicedPromise = $http.get($scope.baseUrl + '/history/activity-instance?activityId=intermediate_wait_invoiced&unfinished=true').then(function (activityResult) {
                            return activityResult.data;
                        });

                        var intermediate_wait_acts_passedPromise = $http.get($scope.baseUrl + '/history/activity-instance?activityId=intermediate_wait_acts_passed&unfinished=true').then(function (activityResult) {
                            return activityResult.data;
                        });
                        var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', taskQuery).then(function (response) {
                            return response.data;
                        });

                        $q.all([processInstancesPromise, taskInstancesPromise, intermediate_wait_invoicedPromise, intermediate_wait_acts_passedPromise])
                            .then(async function (results) {
                                var processInstances = results[0];
                                var taskInstances = results[1];
                                var intermediate_wait_invoicedInstances = results[2];
                                var intermediate_wait_acts_passedInstances = results[3];

                                angular.forEach(taskInstances,function (t) {
                                    if (['approve_material_list_center', 'validate_tr_bycenter', 'approve_additional_material_list_center', 'validate_additional_tr_bycenter'].indexOf(t.taskDefinitionKey) !== -1) {
                                        if (t.name.indexOf('P&O') != -1) {
                                            t.taskDefinitionKey = t.taskDefinitionKey + '_po'
                                        } else if (t.name.indexOf('Transmission') != -1) {
                                            t.taskDefinitionKey = t.taskDefinitionKey + '_tr'
                                        } else if (t.name.indexOf('S&FM') != -1) {
                                            t.taskDefinitionKey = t.taskDefinitionKey + '_fm'
                                        } else if (t.name.indexOf('Operation') != -1) {
                                            t.taskDefinitionKey = t.taskDefinitionKey + '_op'
                                        } else if (t.name.indexOf('Roll-out')) {
                                            t.taskDefinitionKey = t.taskDefinitionKey + '_ro'
                                        }
                                    }
                                });
                                taskInstances = taskInstances.filter(function (task) {
                                    var pid = task.processInstanceId;
                                    var jr = processInstances[pid].jrNumber;
                                    jr = jr.replace('Ast-','Astana-')
                                    return ($scope.regionFilter ? ($scope.regionFilter === 'all' ? true : jr.indexOf($scope.regionFilter) > -1) : true) &&
                                        ($scope.subContractorFilter ? ($scope.subContractorFilter === 'all' ? true : jr.indexOf($scope.subContractorFilter) > -1) : true) &&
                                        ($scope.unitFilter ? ($scope.unitFilter === 'all' ? true : jr.indexOf($scope.unitFilter) > -1) : true)
                                });
                                intermediate_wait_invoicedInstances = intermediate_wait_invoicedInstances.filter(function (task) {
                                    var pid = task.processInstanceId;
                                    var jr = processInstances[pid].jrNumber;
                                    jr = jr.replace('Ast-','Astana-')
                                    return ($scope.regionFilter ? ($scope.regionFilter === 'all' ? true : jr.indexOf($scope.regionFilter) > -1) : true) &&
                                        ($scope.subContractorFilter ? ($scope.subContractorFilter === 'all' ? true : jr.indexOf($scope.subContractorFilter) > -1) : true) &&
                                        ($scope.unitFilter ? ($scope.unitFilter === 'all' ? true : jr.indexOf($scope.unitFilter) > -1) : true)
                                });
                                intermediate_wait_acts_passedInstances = intermediate_wait_acts_passedInstances.filter(function (task) {
                                    var pid = task.processInstanceId;
                                    var jr = processInstances[pid].jrNumber;
                                    jr = jr.replace('Ast-','Astana-')
                                    return ($scope.regionFilter ? ($scope.regionFilter === 'all' ? true : jr.indexOf($scope.regionFilter) > -1) : true) &&
                                        ($scope.subContractorFilter ? ($scope.subContractorFilter === 'all' ? true : jr.indexOf($scope.subContractorFilter) > -1) : true) &&
                                        ($scope.unitFilter ? ($scope.unitFilter === 'all' ? true : jr.indexOf($scope.unitFilter) > -1) : true)
                                });

                                var taskInstancesByDefinition = _.groupBy(
                                    taskInstances,
                                    'taskDefinitionKey'
                                );

                                var intermediate_wait_acts_passedInstancesByDefinition = _.groupBy(
                                    intermediate_wait_acts_passedInstances,
                                    'activityId'
                                );

                                var intermediate_wait_invoicedByDefinition = _.groupBy(
                                    intermediate_wait_invoicedInstances,
                                    'activityId'
                                );

                                taskInstancesByDefinition['intermediate_wait_invoiced'] = intermediate_wait_invoicedByDefinition['intermediate_wait_invoiced'];
                                taskInstancesByDefinition['intermediate_wait_acts_passed'] = intermediate_wait_acts_passedInstancesByDefinition['intermediate_wait_acts_passed'];

                                var tasksByIdAndRegionGrouped = _.mapValues(
                                    taskInstancesByDefinition,
                                    function (tasks) {
                                        return _.groupBy(
                                            tasks,
                                            function (task) {
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

                                var tasksByIdAndContractorGrouped = _.mapValues(
                                    taskInstancesByDefinition,
                                    function (tasks) {
                                        return _.groupBy(
                                            tasks,
                                            function (task) {
                                                var pid = task.processInstanceId;
                                                if (processInstances[pid]) {
                                                    return $scope.getSubContractor(processInstances[pid].jrNumber);
                                                }
                                            }
                                        );
                                    }
                                );

                                var tasksByIdAndUnitGrouped = _.mapValues(
                                    taskInstancesByDefinition,
                                    function (tasks) {
                                        return _.groupBy(
                                            tasks,
                                            function (task) {
                                                var pid = task.processInstanceId;
                                                if (processInstances[pid]) {
                                                    return $scope.getUnit(processInstances[pid].jrNumber);
                                                } else {
                                                    return 'no_processinstance';
                                                }
                                            }
                                        );
                                    }
                                );

                                var tasksByIdAndUnitCounted = _.mapValues(
                                    tasksByIdAndUnitGrouped,
                                    function (tasks) {
                                        return _.mapValues(tasks, 'length')
                                    }
                                );
                                $scope.tasksByIdAndUnitCounted = tasksByIdAndUnitCounted;
                                var tasksByIdAndContractorCounted = _.mapValues(
                                    tasksByIdAndContractorGrouped,
                                    function (tasks) {
                                        return _.mapValues(tasks, 'length')
                                    }
                                );
                                $scope.tasksByIdAndContractorCounted = tasksByIdAndContractorCounted;

                                var tasksByIdAndRegionCounted = _.mapValues(
                                    tasksByIdAndRegionGrouped,
                                    function (tasks) {
                                        return _.mapValues(tasks, 'length');
                                    }
                                );
                                $scope.tasksByIdAndRegionCounted = tasksByIdAndRegionCounted;

                                let a = Object.keys(tasksByIdAndRegionCounted);
                                let newJson = {};
                                let regionJson = {};
                                let subContractorJson = {};
                                let unitJson = {};
                                var finalTasksCounter = 0;
                                var finalRegionCounter = 0;
                                var finalSubContractorCounter = 0;
                                var finalUnitCounter = 0;

                                for (let i = 0; i < a.length; i++) {
                                    if ($scope.revisionTaskDisplay[a[i]]) {
                                        let counter = 0;
                                        let b = Object.values(tasksByIdAndRegionCounted[a[i]]);
                                        let c = Object.keys(tasksByIdAndRegionCounted[a[i]]);

                                        b.forEach(j => {
                                            counter += j;
                                            finalTasksCounter = finalTasksCounter + j;
                                        });
                                        c.forEach(k => {
                                            regionJson[k] = regionJson[k] ? regionJson[k] + tasksByIdAndRegionCounted[a[i]][k] : tasksByIdAndRegionCounted[a[i]][k];
                                            finalRegionCounter = finalRegionCounter + tasksByIdAndRegionCounted[a[i]][k];
                                        });
                                        newJson[a[i]] = counter;
                                    }
                                }
                                $scope.totalCounter = newJson;

                                let a1 = Object.keys(tasksByIdAndContractorCounted);
                                for (let i = 0; i < a1.length; i++) {
                                    if ($scope.revisionTaskDisplay[a1[i]]) {
                                        let counter = 0;
                                        let b = Object.values(tasksByIdAndContractorCounted[a1[i]]);
                                        let c = Object.keys(tasksByIdAndContractorCounted[a1[i]]);

                                        b.forEach(j => {
                                            counter += j;
                                            // finalTasksCounter = finalTasksCounter + j;
                                        });
                                        c.forEach(k => {
                                            subContractorJson[k] = subContractorJson[k] ? subContractorJson[k] + tasksByIdAndContractorCounted[a1[i]][k] : tasksByIdAndContractorCounted[a1[i]][k];
                                            finalSubContractorCounter = finalSubContractorCounter + tasksByIdAndContractorCounted[a1[i]][k];
                                        });
                                        newJson[a[i]] = counter;
                                    }
                                }
                                let a2 = Object.keys(tasksByIdAndUnitCounted);
                                for (let i = 0; i < a2.length; i++) {
                                    if ($scope.revisionTaskDisplay[a2[i]]) {
                                        let counter = 0;
                                        let b = Object.values(tasksByIdAndUnitCounted[a2[i]]);
                                        let c = Object.keys(tasksByIdAndUnitCounted[a2[i]]);

                                        b.forEach(j => {
                                            counter += j;
                                            // finalTasksCounter = finalTasksCounter + j;
                                        });
                                        c.forEach(k => {
                                            unitJson[k] = unitJson[k] ? unitJson[k] + tasksByIdAndUnitCounted[a2[i]][k] : tasksByIdAndUnitCounted[a2[i]][k];
                                            finalUnitCounter = finalUnitCounter + tasksByIdAndUnitCounted[a2[i]][k];
                                        });
                                    }
                                }
                                $scope.regionCounter = regionJson;
                                $scope.subContractorCounter = subContractorJson;
                                $scope.unitCounter = unitJson;
                                $scope.finalRegionCounter = finalRegionCounter;
                                $scope.finalTasksCounter = finalTasksCounter;
                            });
                    } else if ($scope.currentReport === 'invoice-open-tasks') {
                        var processQuery = {
                            "processDefinitionKey": "Invoice",
                            "unfinished": true,
                            "variables": []
                        };
                        if ($scope.filter.reason) {
                            processQuery.variables.push({
                                name: 'workType',
                                operator: 'eq',
                                value: $scope.filter.reason
                            });
                        }
                        if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                            processQuery.variables.push({
                                name: 'mainContract',
                                operator: 'eq',
                                value: $scope.filter.mainContract
                            });
                        }

                        var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', processQuery).then(function (response) {
                            var processInstances = _.keyBy(response.data, 'id');
                            return $http.post($scope.baseUrl + '/history/variable-instance', {
                                variableName: 'invoiceNumber',
                                processInstanceIdIn: _.keys(processInstances)
                            }).then(function (response) {
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
                        if ($scope.filter.reason) {
                            taskQuery.processVariables.push({
                                name: 'workType',
                                operator: 'eq',
                                value: $scope.filter.reason
                            });
                        }
                        if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                            taskQuery.processVariables.push({
                                name: 'mainContract',
                                operator: 'eq',
                                value: $scope.filter.mainContract
                            });
                        }

                        var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', taskQuery).then(function (response) {
                            return response.data;
                        });

                        $q.all([processInstancesPromise, taskInstancesPromise])
                            .then(function (results) {
                                var processInstances = results[0];
                                var taskInstances = results[1];

                                var taskInstancesByDefinition = _.groupBy(
                                    taskInstances,
                                    'taskDefinitionKey'
                                );
                                console.log(taskInstancesByDefinition)

                                var tasksByIdAndRegionGrouped = _.mapValues(
                                    taskInstancesByDefinition,
                                    function (tasks) {
                                        return _.groupBy(
                                            tasks,
                                            function (task) {
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
                                    function (tasks) {
                                        return _.mapValues(tasks, 'length');
                                    }
                                );

                                $scope.tasksByIdAndRegionCounted = tasksByIdAndRegionCounted;

                                let a = Object.keys(tasksByIdAndRegionCounted);
                                let newJson = {};
                                let regionJson = {};
                                var finalTasksCounter = 0;
                                var finalRegionCounter = 0;

                                for (let i = 0; i < a.length; i++) {
                                    let counter = 0;
                                    let b = Object.values(tasksByIdAndRegionCounted[a[i]]);
                                    let c = Object.keys(tasksByIdAndRegionCounted[a[i]]);

                                    b.forEach(i => {
                                        counter += i;
                                        finalTasksCounter = finalTasksCounter + i;
                                    })
                                    c.forEach(k => {
                                        regionJson[k] = regionJson[k] ? regionJson[k] + tasksByIdAndRegionCounted[a[i]][k] : tasksByIdAndRegionCounted[a[i]][k];
                                        finalRegionCounter = finalRegionCounter + tasksByIdAndRegionCounted[a[i]][k];
                                    })

                                    newJson[a[i]] = counter;
                                }

                                $scope.totalCounter = newJson;
                                $scope.regionCounter = regionJson;
                                $scope.finalRegionCounter = finalRegionCounter;
                                $scope.finalTasksCounter = finalTasksCounter;
                            });
                    }
                }

            }
        }
        $scope.drowStatistics();


        $scope.getSiteRegion = function (siteID) {
            //console.log(siteID);
            if (siteID) {
                if (siteID.startsWith('0')) {
                    return 'almaty';
                } else if (siteID.startsWith('1') || siteID.startsWith('2')) {
                    if (siteID.startsWith('11')) {
                        return 'astana';
                    } else {
                        return 'north_central';
                    }
                } else if (siteID.startsWith('3')) {
                    return 'east';
                } else if (siteID.startsWith('4')) {
                    return 'south';
                } else {
                    return 'west';
                }
            } else {
                return 'no_region';
            }
        };

        $scope.selectReport = function (report) {
            $location.url($location.path() + "?report=" + report);
        }

        $scope.selectReason = function (reason) {
            var path = $location.path() + "?report=" + $scope.currentReport;
            if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                path = path + '&mainContract=' + $scope.filter.mainContract;
            }
            if (reason == 'all') {
                $location.url(path);
            } else {
                $location.url(path + "&reason=" + reason);
            }
        }

        $scope.selectMainStatus = function () {
            var path = $location.path() + "?report=" + $scope.currentReport;
            if ($scope.filter.reason && $scope.filter.reason !== 'all') {
                path = path + "&reason=" + $scope.filter.reason;
            }
            if ($scope.filter.mainContract && $scope.filter.mainContract !== 'all') {
                $location.url(path + "&mainContract=" + $scope.filter.mainContract);
            }
        }

        $scope.downloadTechnicalReport = function () {
            if ($rootScope.hasGroup('revision_reports') || 'Evgeniy.Semenov@kcell.kz' == $rootScope.authentication.name) {
                $http.get('/camunda/reports/report').then(function (response) {
                    var data = response.data;

                    angular.forEach(data, function (d) {
                        d[6] = $filter('date')(d[6], "yyyy-MM-dd");
                        d[8] = $filter('date')(d[8], "yyyy-MM-dd");
                        d[9] = $filter('date')(d[9], "yyyy-MM-dd");
                        d[10] = $filter('date')(d[10], "yyyy-MM-dd");
                        d[11] = $filter('date')(d[11], "yyyy-MM-dd");
                        d[12] = $filter('date')(d[12], "yyyy-MM-dd");
                        d[13] = $filter('date')(d[13], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Material List Signing Date", "Accept by Initiator"
                        , "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity", "Comments", "Customer Material"
                        , "Process State", "JR Status", "Detailed status", "Reason"]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader: true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'technical-report-by-works.xlsx');
                });
            }
        }

        $scope.downloadTechnicalByJobsReport = function () {
            if ($rootScope.hasGroup('revision_reports') || 'Evgeniy.Semenov@kcell.kz' == $rootScope.authentication.name) {
                $http.get('/camunda/reports/technical-report-by-jobs').then(function (response) {
                    var data = response.data;

                    angular.forEach(data, function (d) {
                        d[6] = $filter('date')(d[6], "yyyy-MM-dd");
                        d[8] = $filter('date')(d[8], "yyyy-MM-dd");
                        d[9] = $filter('date')(d[9], "yyyy-MM-dd");
                        d[10] = $filter('date')(d[10], "yyyy-MM-dd");
                        d[11] = $filter('date')(d[11], "yyyy-MM-dd");
                        d[12] = $filter('date')(d[12], "yyyy-MM-dd");
                        d[13] = $filter('date')(d[13], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Material List Signing Date", "Accept by Initiator"
                        , "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity", "Comments", "Customer Material"
                        , "Process State", "JR Status", "Detailed status", "Reason", "Job list"]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader: true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'technical-report-by-jobs.xlsx');
                });
            }
        }

        $scope.downloadFinancialReport = function () {
            if ($rootScope.hasGroup('revision_reports')) {
                $http.get('/camunda/reports/financialreport').then(function (response) {
                    var data = response.data;
                    angular.forEach(data, function (d) {
                        d[8] = $filter('date')(d[8], "yyyy-MM-dd");
                        d[10] = $filter('date')(d[10], "yyyy-MM-dd");
                        d[13] = $filter('date')(d[13], "yyyy-MM-dd");
                        d[14] = $filter('date')(d[14], "yyyy-MM-dd");
                        d[15] = $filter('date')(d[15], "yyyy-MM-dd");
                        d[16] = $filter('date')(d[16], "yyyy-MM-dd");
                        d[17] = $filter('date')(d[17], "yyyy-MM-dd");
                        d[35] = $filter('date')(d[35], "yyyy-MM-dd");
                        d[38] = $filter('date')(d[38], "yyyy-MM-dd");
                    });
                    
                    data.splice(0, 0, ["Contract", "Year", "Month", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Related to the", "Project"
                        , "Material List Signing Date", "Accept by Initiator", "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity"
                        , "Materials from", "Job reason", "Type of expenses", "Comments", "Customer Material", "Process State", "JR Status", "Detailed status", "Reason", "Price (without transportation)"
                        , "Price (with transportation)", "Monthly act #", "JO#", "PR#", "PR Total Value", "PR Status", "PR Approval date", "PO#", "Invoice No", "Invoice date"
                    ]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader: true});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'extended-report-by-works.xlsx');
                });
            }
        }
        $scope.downloadExtendedByJobsReport = function () {
            if ($rootScope.hasGroup('revision_reports')) {
                $http.get('/camunda/reports/extended-report-by-jobs').then(function (response) {
                    var data = response.data;

                    angular.forEach(data, function (d) {
                        d[8] = $filter('date')(d[8], "yyyy-MM-dd");
                        d[10] = $filter('date')(d[10], "yyyy-MM-dd");
                        d[13] = $filter('date')(d[13], "yyyy-MM-dd");
                        d[14] = $filter('date')(d[14], "yyyy-MM-dd");
                        d[15] = $filter('date')(d[15], "yyyy-MM-dd");
                        d[16] = $filter('date')(d[16], "yyyy-MM-dd");
                        d[17] = $filter('date')(d[17], "yyyy-MM-dd");
                        d[35] = $filter('date')(d[35], "yyyy-MM-dd");
                        //d[34] =  $filter('date')(d[34], "yyyy-MM-dd");
                        d[38] = $filter('date')(d[38], "yyyy-MM-dd");
                    });

                    data.splice(0, 0, ["Contract", "Year", "Month", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Related to the", "Project"
                        , "Material List Signing Date", "Accept by Initiator", "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity", "Materials from"
                        , "Job reason", "Type of expenses", "Comments", "Customer Material", "Process State", "JR Status", "Detailed status", "Reason", "Price (without transportation)"
                        , "Price (with transportation)", "Monthly act #", "JO#", "PR#", "PR Total Value", "PR Status", "PR Approval date", "PO#", "Invoice No", "Invoice date", "Job list"
                    ]);

                    var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader: true});
                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                    return XLSX.writeFile(wb, 'extended-report-by-jobs.xlsx');
                });
            }
        }
    }]).controller('leasingStatisticsCtrl', ['$scope', '$rootScope', '$filter', '$http', '$state', '$stateParams', '$q', '$location', 'AuthenticationService',
        function ($scope, $rootScope, $filter, $http, $state, $stateParams, $q, $location, AuthenticationService) {

            if(!$rootScope.hasGroup('statistics_rollout') && !$rootScope.hasGroup('infrastructure_leasing_users')){
                $state.go('tasks');
            }

            $rootScope.currentPage = {
                name: 'statistics'
            };

            if (window.require) {
                $scope.XLSX = require('xlsx');
            }

            $scope._ = window._;

            $rootScope.logout = function () {
                AuthenticationService.logout().then(function () {
                    $scope.authentication = null;
                });
            }

            $scope.baseUrl = '/camunda/api/engine/engine/default';
            $scope.report_ready = false;

            $scope.reportsMap = {
                'revision-open-tasks': {name: 'Revision Works Statistics Grouped', process: 'Revision'},
                'invoice-open-tasks': {name: 'Monthly Act open tasks', process: 'Invoice'},
                '4gSharing-open-tasks': {name: '4G Site Sharing open tasks', process: 'SiteSharingTopProcess'}
            };

            $scope.reports = [
                'revision-open-tasks',
                'invoice-open-tasks',
                '4gSharing-open-tasks'
            ];

            $scope.currentReport = $stateParams.report;
            $scope.reverseOrder = false;
            $scope.fieldName = 'Region';
            $scope.task = $stateParams.task;

            $scope.filter = {};
            if ($stateParams.reason) {
                $scope.filter.reason = $stateParams.reason;
            }
            if ($stateParams.mainContract) {
                $scope.filter.mainContract = $stateParams.mainContract;
            }
            $scope.region = $stateParams.region;

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

            $scope.orderByFieldName = function (fieldName) {
                console.log(fieldName)
                if ($scope.fieldName == fieldName) {
                    $scope.reverseOrder = !$scope.reverseOrder;
                } else {
                    $scope.reverseOrder = false;
                    $scope.fieldName = fieldName;
                }
            };

            $scope.orderMaFunction = function (task) {
                if ($scope.fieldName === 'Region') {
                    return $scope.getInvoiceRegion(task.variables.invoiceNumber.value);
                } else if ($scope.fieldName === 'Act') {
                    if (task.variables.monthActNumber) {
                        return task.variables.monthActNumber.value;
                    } else {
                        return task.variables.invoiceNumber.value;
                    }
                } else if ($scope.fieldName === 'Typeofwork') {
                    return $scope.reasonsTitle[task.variables.workType.value];
                } else if ($scope.fieldName === 'Period') {
                    return task.variables.monthOfFormalPeriod.value + ' ' + task.variables.yearOfFormalPeriod.value;
                } else if ($scope.fieldName === 'Contractor') {
                    return 'TOO Line System Engineering';
                } else if ($scope.fieldName === 'Requestedby') {
                    return task.variables.starter.value;
                } else if ($scope.fieldName === 'Currentassignee') {
                    if (task.assignee) {
                        return task.assignee;
                    } else {
                        return task.groupId;
                    }
                }
            }

            $scope.orderByRevisionFieldName = function (revisionFieldName) {
                if ($scope.revisionFieldName == revisionFieldName) {
                    $scope.revisionReverseOrder = !$scope.revisionReverseOrder;
                } else {
                    $scope.revisionReverseOrder = false;
                    $scope.revisionFieldName = revisionFieldName;
                }
            };

            $scope.orderRevisionFunction = function (task) {
                if ($scope.revisionFieldName === 'Region') {
                    return $scope.getJrRegion(task.variables.jrNumber.value);
                } else if ($scope.revisionFieldName === 'site_name') {
                    return task.variables.site_name.value;
                } else if ($scope.revisionFieldName === 'jrNumber') {
                    return task.variables.jrNumber.value;
                } else if ($scope.revisionFieldName === 'contractor') {
                    return $scope.contractorsTitle[task.variables.contractor.value];
                } else if ($scope.revisionFieldName === 'reason') {
                    return $scope.reasonsTitle[task.variables.reason.value];
                } else if ($scope.revisionFieldName === 'project') {
                    if (task.variables.project) {
                        return task.variables.project.value;
                    } else {
                        return 'Z';
                    }
                } else if ($scope.revisionFieldName === 'starter') {
                    return task.variables.starter.value;
                } else if ($scope.revisionFieldName === 'requestedDate') {
                    return task.variables.requestedDate.value;
                } else if ($scope.revisionFieldName === 'validityDate') {
                    return task.variables.validityDate.value;
                } else if ($scope.revisionFieldName === 'assignee') {
                    return task.assignee ? task.assignee : task.groupId;
                }
            }

            $scope.getInvoiceRegion = function (invoiceNumber) {
                if (invoiceNumber.endsWith('-RO-1')) {
                    invoiceNumber = invoiceNumber.replace('-RO-1', '');
                }
                if (invoiceNumber.endsWith('-RO-2')) {
                    invoiceNumber = invoiceNumber.replace('-RO-2', '');
                }
                if (invoiceNumber.endsWith('-RO-3')) {
                    invoiceNumber = invoiceNumber.replace('-RO-3', '');
                }
                if (invoiceNumber.endsWith('-RO-4')) {
                    invoiceNumber = invoiceNumber.replace('-RO-4', '');
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

            $scope.filterRegion = function (task) {
                if ($scope.region) {
                    if ($scope.getProcessDefinition() === 'Revision') {
                        return $scope.getJrRegion(task.variables.jrNumber.value) === $scope.region;
                    } else if ($scope.getProcessDefinition() === 'Invoice') {
                        return $scope.getInvoiceRegion(task.variables.invoiceNumber.value) === $scope.region;
                    }
                } else {
                    return true;
                }
            }

            $scope.getProcessDefinition = function () {
                if ($scope.currentReport === 'revision-open-tasks') {
                    return 'Revision';
                } else if ($scope.currentReport === 'invoice-open-tasks') {
                    return 'Invoice';
                } else if ($scope.currentReport === '4gSharing-open-tasks') {
                    return 'SiteSharingTopProcess';
                }
            }

            $scope.regions = ['.almaty', '.east', '.west', '.north_central', '.south', '.astana', '.no_region',];
            $scope.checkRegionView = function (region) {
                if ($rootScope.hasGroup('head_kcell_users')) {
                    return true;
                } else if ($rootScope.hasGroup('alm_kcell_users')) {
                    return region === '.almaty';
                } else if ($rootScope.hasGroup('astana_kcell_users')) {
                    return region === '.astana';
                } else if ($rootScope.hasGroup('east_kcell_users')) {
                    return region === '.east';
                } else if ($rootScope.hasGroup('nc_kcell_users')) {
                    return region === '.north_central';
                } else if ($rootScope.hasGroup('south_kcell_users')) {
                    return region === '.south';
                } else if ($rootScope.hasGroup('west_kcell_users')) {
                    return region === '.west';
                } else {
                    return false;
                }
            }

            $scope.updateTaskDefinitions = function () {
                var processDefinition = $scope.getProcessDefinition();

                $http.get($scope.baseUrl + '/process-definition/key/' + processDefinition + '/xml')
                    .then(function (response) {
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

                        var userTasks = getUserTasks(xml);
                        var userTasksMap = _.keyBy(userTasks, 'id');
                        $scope.userTasksMap = userTasksMap;
                    });
            }

            if (true) {

                $scope.currentView = 'statistics';

                $scope.processIntancesList = [];
                $scope.ncpMap = {};
                $scope.sitenameMap = {};
                $scope.siteTypeMap = {};
                $scope.projectMap = {};
                $scope.reasonMap = {};
                $scope.addressMap = {};
                $scope.generalStatusUpdatedDateMap = {};

                $scope.generalStatuses = [
                    {id: 'Planning in progress', name: 'Planning in progress'},
                    {id: 'Provisional approval by Power, Transmission & Leasing', name: 'Provisional approval', substatuses: [
                        {id: 'Waiting regional Power approval', name: 'Waiting regional Power approval'}, 
                        {id: 'Waiting regional Transmission approval', name: 'Waiting regional Transmission approval'},
                        {id: 'Waiting Leasing approval', name: 'Waiting Leasing approval'}
                    ]},
                    {id: 'Leasing in progress', name: 'Leasing in progress', substatuses: [
                        {id: 'Leasing for Candidate', name: 'Leasing of Candidate'}, 
                        {id: 'Leasing for Far End', name: 'Leasing of Far End'}
                    ]},
                    {id: 'Contract approval in progress', name: 'Contract approval in progress'},
                    {id: 'Power in progress', name: 'Power in progress'},
                    {id: 'Waiting TSD, RSD, VSD', name: 'Waiting TSD, RSD, VSD'},
                    {id: 'Installation in progress', name: 'Installation in progress', substatuses: [
                        {id: 'Instalation problem', name: 'Installation problem'}, 
                        {id: 'Leasing problem', name: 'Leasing problem'},
                        {id: 'Transmission problem', name: 'Transmission problem'},
                        {id: 'SSID in progress', name: 'SSID in progress'},
                        {id: 'Installation in progress', name: 'Installation in progress'},
                        {id: 'Installation finish', name: 'Installation finish'}
                    ]},
                    {id: 'On-Air', name: 'On Air'}
                ];

                $scope.regions = [
                    {id: 'almaty', name: 'Almaty'},
                    {id: 'astana', name: 'Astana'},
                    {id: 'nc', name: 'North&Central'},
                    {id: 'east', name: 'East'},
                    {id: 'south', name: 'South'},
                    {id: 'west', name: 'West'},
                    {id: 'ericsson', name: 'Ericsson'},
                    {id: 'zte', name: 'ZTE'},
                    {id: 'total', name: 'Total'}
                ];

                $scope.dictionary = {};
                $http.get('/api/leasingCatalogs?version=1').then(
                    function(result){
                        angular.extend($scope.dictionary, result.data);
                        $scope.dictionary.projectList = [];
                        for(var i=0; i<$scope.dictionary.projects.length;i++){
                            for(var j=0; j<$scope.dictionary.projects[i].project.length;j++){
                                $scope.dictionary.projectList.push($scope.dictionary.projects[i].project[j]);
                            }
                        }
                    },
                    function(error){
                        console.log(error);
                    }
                );
                $scope.regionsMap = _.mapValues(_.keyBy($scope.regions, 'id'), 'name');

                $scope.init = function(){
                    console.log('init');
                    $scope.fullPIDsByRegions = {};

                    var processQuery = {
                        "processDefinitionKey": "leasing",
                    };
                    if($scope.project){
                        processQuery.variables = [{name: 'projectForSearch', operator: 'eq', value: $scope.project}];
                    }
                    var processInstancesPromise = $http.post($scope.baseUrl + '/process-instance', processQuery).then(function (response) {
                        return response.data;
                    });

                    var ncpRejectedProcessQuery = {
                        "processDefinitionKey": "leasing",
                        "executedActivityIdIn": ["EndEvent_1u7wxvp"],
                        "finished": true
                    };
                    if($scope.project){
                        ncpRejectedProcessQuery.variables = [{name: 'projectForSearch', operator: 'eq', value: $scope.project}];
                    }
                    var ncpRejectedProcessInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', ncpRejectedProcessQuery).then(function (response) {
                        return response.data;
                    });

                    var onAirProcessQuery = {
                        "processDefinitionKey": "leasing",
                        "executedActivityIdIn": ["EndEvent_0miie5o"],
                        "finished": true
                    };
                    if($scope.project){
                        onAirProcessQuery.variables = [{name: 'projectForSearch', operator: 'eq', value: $scope.project}];
                    }
                    var onAirProcessInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', onAirProcessQuery).then(function (response) {
                        return response.data
                    });

                    $q.all([processInstancesPromise, ncpRejectedProcessInstancesPromise, onAirProcessInstancesPromise]).then(function (results) {

                        // ********************* Active instances ******************************************************
                        var PIDsArray = [];
                        var processInstances = results[0];
                        $scope.businessKeyMap = _.mapValues(_.keyBy(processInstances, 'id'), 'businessKey');                    
                        processInstances.forEach(p => {
                            PIDsArray.push(p.id);
                        });                    
                        
                        // var variables = ['generalStatus', 'installationStatus', 'rbsType', 'region']

                        if(PIDsArray.length > 0){
                            var generalStatusesVarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'generalStatus'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });

                            var generalSubStatus1VarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'generalSubStatus1'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });

                            var generalSubStatus2VarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'generalSubStatus2'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });

                            var setInstStatusFromUDBVarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'setInstStatusFromUDB'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });
    /*
                            var installationStatusesVarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'installationStatus'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });
    */
                            var rbsTypesVarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'rbsType'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });
                            
                            var regionsVarsPromise = $http.post($scope.baseUrl + '/variable-instance', {
                                processInstanceIdIn: PIDsArray,
                                variableName: 'region'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });                        

                            $q.all([generalStatusesVarsPromise, /*installationStatusesVarsPromise,*/ rbsTypesVarsPromise, regionsVarsPromise, generalSubStatus1VarsPromise, generalSubStatus2VarsPromise, setInstStatusFromUDBVarsPromise]).then(function (results) {
                                var fullPIDs = PIDsArray.map( p => {
                                    return { 
                                        pid : p,
                                        status: 'Active',
                                        generalStatus: results[0][p],
                                        //installationStatus: results[1][p],
                                        rbsType: results[1][p],
                                        region: results[2][p],
                                        generalSubStatus1: results[3][p],
                                        generalSubStatus2: results[4][p],
                                        instStatusFromUDB: results[5][p]
                                    }
                                });
                                $scope.fullPIDsByRegions = _.groupBy(fullPIDs, 'region')
                                var fullPIDsFilteredEricsson = _.filter(fullPIDs, p => {
                                    if (p.rbsType?.startsWith(2) || p.rbsType?.startsWith(3) || p.rbsType?.startsWith(6)) {
                                        return true
                                    } return false
                                })
                                var fullPIDsFilteredZTE = _.filter(fullPIDs, p => {
                                    if (p.rbsType?.startsWith(8)) {
                                        return true
                                    } return false
                                })
                                $scope.fullPIDsByRegions.ericsson = fullPIDsFilteredEricsson;
                                $scope.fullPIDsByRegions.zte = fullPIDsFilteredZTE;
                                $scope.fullPIDsByRegions.total = fullPIDs;

                                $scope.regions.forEach(r => {
                                    if($scope.fullPIDsByRegions[r.id]){
                                        $scope.fullPIDsByRegions[r.id].generalStatuses =  _.groupBy($scope.fullPIDsByRegions[r.id], 'generalStatus');

                                        if($scope.fullPIDsByRegions[r.id].generalStatuses && $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress']){
                                            var pIds = [];
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses = {};
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].forEach(p => {
                                                pIds.push(p);
                                            });
                                            pIds.forEach(p => {
                                                if(p.generalSubStatus1 === 'Leasing for Candidate'){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Candidate']){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Candidate'] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Candidate'].push(p);
                                                }
                                                if(p.generalSubStatus2 === 'Leasing for Far End'){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Far End']){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Far End'] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Leasing in progress'].substatuses['Leasing for Far End'].push(p);
                                                }
                                            });
                                        }
                                        if($scope.fullPIDsByRegions[r.id].generalStatuses && $scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress']){
                                            var instIds = [];
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress'].substatuses = {};
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress'].forEach(p => {
                                                instIds.push(p);
                                            });
                                            instIds.forEach(p => {
                                                if(p.instStatusFromUDB){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress'].substatuses[p.instStatusFromUDB]){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress'].substatuses[p.instStatusFromUDB] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Installation in progress'].substatuses[p.instStatusFromUDB].push(p);
                                                }
                                            });
                                        }
                                        if($scope.fullPIDsByRegions[r.id].generalStatuses && $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing']){
                                            var pIds = [];
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses = {};
                                            $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].forEach(p => {
                                                pIds.push(p);
                                            });
                                            pIds.forEach(p => {
                                                if(p.generalSubStatus1 === 'Waiting regional Power approval'){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Power approval']){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Power approval'] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Power approval'].push(p);
                                                }
                                                if(p.generalSubStatus1 === 'Waiting Leasing approval'){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting Leasing approval']){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting Leasing approval'] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting Leasing approval'].push(p);
                                                }
                                                if(p.generalSubStatus2 === 'Waiting regional Transmission approval'){
                                                    if(!$scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Transmission approval']){
                                                        $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Transmission approval'] = [];
                                                    }
                                                    $scope.fullPIDsByRegions[r.id].generalStatuses['Provisional approval by Power, Transmission & Leasing'].substatuses['Waiting regional Transmission approval'].push(p);
                                                }
                                            });
                                        }
                                    }
                                });
                            });
                        }

                        // ******************** ncp rejected instances ******************************************************************** 

                        var rejectedPIDsArray = [];
                        var rejectedProcessInstances = results[1];
                        console.log(results[1]);
                        $scope.businessKeyMap = {...$scope.businessKeyMap, ..._.mapValues(_.keyBy(rejectedProcessInstances, 'id'), 'businessKey')};
                        $scope.ncpInsertDateMap = _.mapValues(_.keyBy(rejectedProcessInstances, 'id'), 'startTime');
                        rejectedProcessInstances.forEach(p => {
                            rejectedPIDsArray.push(p.id);
                        });
                        console.log(rejectedPIDsArray.length);
                        
                        // var variables = ['rbsType', 'region']

                        if(rejectedPIDsArray.length > 0){
                            var rejectedRbsTypesVarsPromise = $http.post($scope.baseUrl + '/history/variable-instance', {
                                processInstanceIdIn: rejectedPIDsArray,
                                variableName: 'rbsType'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });
                            
                            var rejectedRegionsVarsPromise = $http.post($scope.baseUrl + '/history/variable-instance', {
                                processInstanceIdIn: rejectedPIDsArray,
                                variableName: 'region'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });

                            $q.all([rejectedRbsTypesVarsPromise, rejectedRegionsVarsPromise]).then(function (results) {
                                var rejectedFullPIDs = rejectedPIDsArray.map( p => {
                                    return { 
                                        pid : p,
                                        status: 'Closed',
                                        rbsType: results[0][p],
                                        region: results[1][p]
                                    }
                                });
                                $scope.rejectedFullPIDsByRegions = _.groupBy(rejectedFullPIDs, 'region')
                                var rejectedFullPIDsFilteredEricsson = _.filter(rejectedFullPIDs, p => {
                                    if (p.rbsType.startsWith(2) || p.rbsType.startsWith(3) || p.rbsType.startsWith(6)) {
                                        return true
                                    } return false
                                })
                                var rejectedFullPIDsFilteredZTE = _.filter(rejectedFullPIDs, p => {
                                    if (p.rbsType.startsWith(8)) {
                                        return true
                                    } return false
                                })
                                $scope.rejectedFullPIDsByRegions.ericsson = rejectedFullPIDsFilteredEricsson;
                                $scope.rejectedFullPIDsByRegions.zte = rejectedFullPIDsFilteredZTE;
                                $scope.rejectedFullPIDsByRegions.total = rejectedFullPIDs;
                            });
                        }

                        // ******************** on air closed instances ******************************************************************** 

                        var onAirPIDsArray = [];
                        var onAirProcessInstances = results[2];
                        $scope.businessKeyMap = {...$scope.businessKeyMap, ..._.mapValues(_.keyBy(onAirProcessInstances, 'id'), 'businessKey')};
                        $scope.ncpInsertDateMap = {...$scope.ncpInsertDateMap, ..._.mapValues(_.keyBy(onAirProcessInstances, 'id'), 'startTime')};
                        onAirProcessInstances.forEach(p => {
                            onAirPIDsArray.push(p.id);
                        });
                        
                        // var variables = ['rbsType', 'region']

                        if(onAirProcessInstances.length > 0){
                            var onAirRbsTypesVarsPromise = $http.post($scope.baseUrl + '/history/variable-instance', {
                                processInstanceIdIn: onAirPIDsArray,
                                variableName: 'rbsType'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });
                            
                            var onAirRegionsVarsPromise = $http.post($scope.baseUrl + '/history/variable-instance', {
                                processInstanceIdIn: onAirPIDsArray,
                                variableName: 'region'
                            }).then(function (response) {
                                return _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                            });

                            $q.all([onAirRbsTypesVarsPromise, onAirRegionsVarsPromise]).then(function (results) {
                                var onAirFullPIDs = onAirPIDsArray.map( p => {
                                    return { 
                                        pid : p,
                                        status: 'Closed',
                                        rbsType: results[0][p],
                                        region: results[1][p]
                                    }
                                });
                                $scope.onAirFullPIDsByRegions = _.groupBy(onAirFullPIDs, 'region')
                                var onAirFullPIDsFilteredEricsson = _.filter(onAirFullPIDs, p => {
                                    if (p.rbsType.startsWith(2) || p.rbsType.startsWith(3) || p.rbsType.startsWith(6)) {
                                        return true
                                    } return false
                                })
                                var onAirFullPIDsFilteredZTE = _.filter(onAirFullPIDs, p => {
                                    if (p.rbsType.startsWith(8)) {
                                        return true
                                    } return false
                                })
                                $scope.onAirFullPIDsByRegions.ericsson = onAirFullPIDsFilteredEricsson;
                                $scope.onAirFullPIDsByRegions.zte = onAirFullPIDsFilteredZTE;
                                $scope.onAirFullPIDsByRegions.total = onAirFullPIDs;
                            });
                        }
                    });

                    $scope.showTable = function(status, region, list, list2){
                        $scope.processIntancesList = [];

                        $scope.currentView = 'table';
                        $scope.status = status;
                        $scope.region = region;

                        if(list && list.length > 0){
                            list.forEach(l => {
                                $scope.processIntancesList.push({
                                    id: l.pid,
                                    region: l.region,
                                    rbsType: l.rbsType,
                                    status: l.status,
                                    generalSubStatus1: l.generalSubStatus1,
                                    generalSubStatus2: l.generalSubStatus2,
                                    instStatusFromUDB: l.instStatusFromUDB
                                });
                            });
                        }
                        if(list2 && list2.length > 0){
                            list2.forEach(l => {
                                $scope.processIntancesList.push({
                                    id: l.pid,
                                    region: l.region,
                                    rbsType: l.rbsType,
                                    status: l.status,
                                    generalSubStatus1: l.generalSubStatus1,
                                    generalSubStatus2: l.generalSubStatus2,
                                    instStatusFromUDB: l.instStatusFromUDB
                                });
                            });
                        }
                        var procInstIdArray = [];
                        $scope.processIntancesList.forEach(p => {
                            procInstIdArray.push(p.id);
                        });

                        $http.post($scope.baseUrl + '/history/variable-instance', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'ncpID'
                        }).then(function (response) {
                            $scope.ncpMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                        });

                        $http.post($scope.baseUrl + '/history/variable-instance', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'siteName'
                        }).then(function (response) {
                            $scope.sitenameMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                        });

                        $http.post($scope.baseUrl + '/history/variable-instance?deserializeValues=false', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'siteType'
                        }).then(function (response) {
                            $scope.siteTypeMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), function(p){
                                return p.value ? JSON.parse(p.value).name : '';
                            });
                        });
                        $http.post($scope.baseUrl + '/history/variable-instance?deserializeValues=false', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'project'
                        }).then(function (response) {
                            $scope.projectMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), function(p){
                                return p.value ? JSON.parse(p.value).name : '';
                            });
                        });

                        $http.post($scope.baseUrl + '/variable-instance', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'generalStatusUpdatedDate'
                        }).then(function (response) {
                            $scope.generalStatusUpdatedDateMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value');
                        });

                        $http.post($scope.baseUrl + '/history/variable-instance?deserializeValues=false', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'reason'
                        }).then(function (response) {
                            $scope.reasonMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), function(p){
                                return p.value ? JSON.parse(p.value).reason : '';
                            });
                        });

                        $http.post($scope.baseUrl + '/history/variable-instance?deserializeValues=false', {
                            processInstanceIdIn: procInstIdArray,
                            variableName: 'address'
                        }).then(function (response) {
                            $scope.addressMap = _.mapValues(_.keyBy(response.data, 'processInstanceId'), function(p){
                                if (p.value) {
                                    const obj = JSON.parse(p.value);
                                    return obj.cn_addr_oblast + ', ' + obj.cn_addr_city + ', '
                                        + obj.cn_addr_street + ', ' + obj.cn_addr_building;
                                } else {
                                 return '';
                                }
                            });
                        });

                        var activeProcInstIdArray = [];
                        $scope.processIntancesList.forEach(p => {
                            if(p.status === 'Active'){
                                $http.post($scope.baseUrl + '/task', {
                                    processInstanceId: p.id,
                                }).then(function (response) {
                                    p.tasks = response.data;
                                    if(p.tasks.length > 0){
                                        p.tasks.forEach(t => {
                                            $http.get($scope.baseUrl + '/task/' + t.id + '/identity-links').then((response) => {
                                                var groups = response.data
                                                var groupList = ''
                                                for (let j = 0; j < groups.length; j++) {
                                                    if (groups[j].groupId) {
                                                        groupList += groups[j].groupId
                                                        if (groups.length > 1) {
                                                            groupList += ', '
                                                        }
                                                    }
                                                }
                                                t.groupId = groupList;
                                            });
                                        });                                    
                                    }
                                });
                                activeProcInstIdArray.push(p.id);
                            }
                        });

                        $http.post($scope.baseUrl + '/variable-instance', {
                            processInstanceIdIn: activeProcInstIdArray,
                            variableName: 'ncpInsertDate'
                        }).then(function (response) {
                            $scope.ncpInsertDateMap = {...$scope.ncpInsertDateMap, ..._.mapValues(_.keyBy(response.data, 'processInstanceId'), 'value')};
                        });
                    }
                }
                $scope.init();

                $scope.$watch('project', function (new_project, old_project) {
                    if(new_project!==old_project){
                        $scope.init();
                    }    
                });

                $scope.change = function(p){
                    if(p){
                       $scope.init(); 
                    }
                }
            }
            

            $scope.getSiteRegion = function (siteID) {
                //console.log(siteID);
                if (siteID) {
                    if (siteID.startsWith('0')) {
                        return 'almaty';
                    } else if (siteID.startsWith('1') || siteID.startsWith('2')) {
                        if (siteID.startsWith('11')) {
                            return 'astana';
                        } else {
                            return 'north_central';
                        }
                    } else if (siteID.startsWith('3')) {
                        return 'east';
                    } else if (siteID.startsWith('4')) {
                        return 'south';
                    } else {
                        return 'west';
                    }
                } else {
                    return 'no_region';
                }
            };

            $scope.selectReport = function (report) {
                $location.url($location.path() + "?report=" + report);
            }

            $scope.selectReason = function (reason) {
                var path = $location.path() + "?report=" + $scope.currentReport;
                if ($scope.filter.mainContract && $scope.filter.mainContract !== 'All') {
                    path = path + '&mainContract=' + $scope.filter.mainContract;
                }
                if (reason == 'all') {
                    $location.url(path);
                } else {
                    $location.url(path + "&reason=" + reason);
                }
            }

            $scope.selectMainStatus = function () {
                var path = $location.path() + "?report=" + $scope.currentReport;
                if ($scope.filter.reason && $scope.filter.reason !== 'all') {
                    path = path + "&reason=" + $scope.filter.reason;
                }
                if ($scope.filter.mainContract && $scope.filter.mainContract !== 'all') {
                    $location.url(path + "&mainContract=" + $scope.filter.mainContract);
                }
            }

            $scope.getRolloutXlsx = (tableName) => {
                    var tbl = document.getElementById(tableName);
                    var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                    return XLSX.writeFile(wb, 'rollout-statistics.xlsx');
            };

            $scope.downloadExtendedByJobsReport = function () {
                if ($rootScope.hasGroup('revision_reports')) {
                    $http.get('/camunda/reports/extended-report-by-jobs').then(function (response) {
                        var data = response.data;

                        angular.forEach(data, function (d) {
                            d[8] = $filter('date')(d[8], "yyyy-MM-dd");
                            d[10] = $filter('date')(d[10], "yyyy-MM-dd");
                            d[13] = $filter('date')(d[13], "yyyy-MM-dd");
                            d[14] = $filter('date')(d[14], "yyyy-MM-dd");
                            d[15] = $filter('date')(d[15], "yyyy-MM-dd");
                            d[16] = $filter('date')(d[16], "yyyy-MM-dd");
                            d[17] = $filter('date')(d[17], "yyyy-MM-dd");
                            d[35] = $filter('date')(d[35], "yyyy-MM-dd");
                            //d[34] =  $filter('date')(d[34], "yyyy-MM-dd");
                            d[38] = $filter('date')(d[38], "yyyy-MM-dd");
                        });

                        data.splice(0, 0, ["Contract", "Year", "Month", "Region", "Sitename", "JR No", "JR To", "JR Reason", "Requested Date", "Requested By", "Validity Date", "Related to the", "Project"
                            , "Material List Signing Date", "Accept by Initiator", "Accept by Work Maintenance", "Accept by Work Planning", "Acceptance Date", "Job Description", "Quantity"
                            , "Job reason", "Type of expenses", "Comments", "Customer Material", "Process State", "JR Status", "Detailed status", "Reason", "Price (without transportation)"
                            , "Price (with transportation)", "Monthly act #", "JO#", "PR#", "PR Total Value", "PR Status", "PR Approval date", "PO#", "Invoice No", "Invoice date", "Job list"
                        ]);

                        var ws = XLSX.utils.json_to_sheet(response.data, {skipHeader: true});
                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                        return XLSX.writeFile(wb, 'extended-report-by-jobs.xlsx');
                    });
                }
            }
        }]).controller('TaskCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', function ($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
    var camClient = new CamSDK.Client({
        mock: false,
        apiUri: '/camunda/api/engine/'
    });

    $scope.hasAssignPermission = false;
    $scope.control = {};

    var Authentication = function (data) {
        angular.extend(this, data);
    }
    init();

    $scope.$watchGroup(['selectedProject', 'selectedProcess'], function(newValues, oldValues, scope) {
        if((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)){
            if($scope.processDefinitionKey && !_.some($rootScope.getCurrentProcesses(), function(pd){ return pd.key === $scope.processDefinitionKey})){
                $state.go('tasks');
            }
        }
    }, true);

    function disableForm() {
        $("[name=kcell_form]").css("pointer-events", "none");
        $("[name=kcell_form]").css("opacity", "0.4");
    }

    function addFormButton(err, camForm, evt) {
        if (err) {
            throw err;
        }
        var $submitBtn = $('<button type="submit" class="btn btn-primary complete-btn" id="taskCompleteButton">Complete</button>').click(function (e) {
            $scope.view.submitted = true;
            if ($scope.kcell_form.$valid) {
                var button = $(this);
                button.attr('disabled', true);
                if ($scope.preSubmit) {
                    $scope.preSubmit().then(
                        function (result) {
                            camForm.submit(function (err) {
                                if (err) {
                                    $submitBtn.removeAttr('disabled');
                                    toasty.error({title: "Error", msg: err});
                                    e.preventDefault();
                                    throw err;
                                } else {
                                    toasty.success({title: "Info", msg: " Your form has been successfully processed"});
                                    $scope.preSubmit = undefined;
                                    $('#taskElement').html('');

                                    $scope.currentTask = undefined;
                                    $scope.$parent.getTaskList();
                                    $location.search({});
                                    $scope.submitted = false;
                                }
                            });
                        },
                        function (err) {
                            e.preventDefault();
                            if (err) {
                                $submitBtn.removeAttr('disabled');
                                toasty.error({title: "Error", msg: err});
                                throw err;
                            } else {
                                button.prop('disabled', false);
                            }
                        }
                    );
                } else {
                    camForm.submit(function (err) {
                        if (err) {
                            $submitBtn.removeAttr('disabled');
                            toasty.error({title: "Error", msg: err});
                            e.preventDefault();
                            throw err;
                        } else {
                            toasty.success({title: "Info", msg: " Your form has been successfully processed"});
                            $('#taskElement').html('');
                            $scope.currentTask = undefined;
                            $scope.$parent.getTaskList();
                            $location.search({});
                            $scope.preSubmit = undefined;
                            $scope.submitted = false;
                        }
                    });
                }
            } else {
                console.log($scope.kcell_form);
                toasty.error({title: "Error", msg: "Please fill required fields"});
            }
        });
        camForm.formElement.append($submitBtn);
    }

    var baseUrl = '/camunda/api/engine/engine/default';
    function init(){
        $http({
            method: 'GET',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            url: '/camunda/api/engine/engine/default/task/' + $stateParams.id
        }).then(
            function (result) {
                if (result.data._embedded.user && result.data._embedded.user.length > 0) {
                    result.data.assigneeObject = result.data._embedded.user[0];
                }
                if(result.data._embedded.identityLink && result.data._embedded.identityLink.length > 0){
                    result.data.candidateObject = result.data._embedded.identityLink.find(function (el) { return el.type === 'candidate'; });
                }
                if (result.data._embedded.processDefinition && result.data._embedded.processDefinition[0].key) {
                    $scope.processDefinitionKey = result.data._embedded.processDefinition[0].key;
                }
                initData(result.data);
            },
            function (error) {
                $http.get('/camunda/api/engine/engine/default/history/task?taskId=' + $stateParams.id).then(
                    function (result) {
                        if (result.data && result.data[0]) {
                            initHistoryData(result.data[0]);
                        }
                    },
                    function (error) {
                        console.log('Not found in history');
                    }
                );
            }
        );
    }
    function initData(task){
        $scope.selectedTab = 'form';
        $scope.currentTask = task;
        $scope.$parent.currentTask = task;
        $scope.view.submitted = false;
        $scope.view.variablesFetched = false;
        var taskId = task.id;
        $('#taskElement').html('');
        $scope.isHistoryOpen = false;
        if (task.assignee === $rootScope.authentication.name) {
            $http.get(baseUrl + '/task/' + taskId + '/form').then(
                function (taskFormInfo) {
                    var url = taskFormInfo.data.key.replace('embedded:app:', taskFormInfo.data.contextPath + '/');
                    new CamSDK.Form({
                        client: camClient,
                        formUrl: url,
                        taskId: taskId,
                        containerElement: $('#taskElement'),
                        done: addFormButton
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
        } else {
            $http.get(baseUrl + '/task/' + taskId + '/form').then(
                function (taskFormInfo) {
                    $scope.isHistoryOpen = true;
                    var url = taskFormInfo.data.key.replace('embedded:app:', taskFormInfo.data.contextPath + '/');
                    new CamSDK.Form({
                        client: camClient,
                        formUrl: url,
                        taskId: taskId,
                        containerElement: $('#taskElement'),
                        done: disableForm
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
        }
    }
    function initHistoryData(task){
        var variableQuery = {
            processInstanceId: task.processInstanceId,
            variableName: 'resolutions'
        };
        $http.post('/camunda/api/engine/engine/default/history/variable-instance?deserializeValues=false', variableQuery).then(
            function(result){
                if(result.data && result.data[0] && result.data[0].value){
                    $scope.resolution = JSON.parse(result.data[0].value).find(function (el) { return el.taskId === $stateParams.id; });
                    $scope.historyTask = task;
                }
            }
        );
    }

    $scope.claim = function (task) {
        $http.post(baseUrl + '/task/' + task.id + '/claim', {userId: $rootScope.authentication.name}).then(
            function () {
                $scope.tryToOpen = {};
                $scope.$parent.getTaskList();
                task.assigneeObject = $rootScope.authUser;
                task.assignee = $rootScope.authentication.name;
                init();
            },
            function (error) {
                console.log(error.data);
            }
        );
    }
    $scope.unclaim = function (task) {
        $http.post(baseUrl + '/task/' + task.id + '/unclaim').then(
            function () {
                $scope.tryToOpen = {
                    id: task.id
                };
                $scope.$parent.getTaskList();
                init();
            },
            function (error) {
                console.log(error.data);
            }
        );
    }
    $scope.assignmentInProgress = false;
    $scope.dispayAssignField = function () {
        $scope.assignmentInProgress = true;
        $scope.getLeasingUsers();
    }


    $scope.getLeasingUsers = function () {
        $scope.leasingUserList = [];
        var link = '/camunda/api/engine/engine/default/user?memberOfGroup=' + $scope.currentTask.candidateObject.groupId;
        console.log("123", link);
        $http.get(link).then(
            function (response) {
                $scope.leasingUserList = response.data;

            });
    }

    $scope.getTaskAssigneeUserList = function (val) {
        $scope.newAssigneeId = null;
        var link = '/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%');
        var lastNameLink = '/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%');


        if ($scope.processDefinitionKey === 'Revision' && !$scope.hasGroup('revision_managers')) {
            link = link + `&memberOfGroup=` + $scope.currentTask.candidateObject.groupId;
            lastNameLink = lastNameLink + `&memberOfGroup=` + $scope.currentTask.candidateObject.groupId;
        }
        var users = $http.get(link).then(
            function (response) {
                var usersByFirstName = _.flatMap(response.data, function (s) {
                    if (s.id) {
                        return s.id.split(',').map(function (user) {
                            return {
                                id: s.id,
                                email: (s.email ? s.email.substring(s.email.lastIndexOf('/') + 1) : s.email),
                                firstName: s.firstName,
                                lastName: s.lastName,
                                name: s.firstName + ' ' + s.lastName
                            };
                        })
                    } else {
                        return [];
                    }
                });

                return $http.get(lastNameLink).then(
                    function (response) {
                        var usersByLastName = _.flatMap(response.data, function (s) {
                            if (s.id) {
                                return s.id.split(',').map(function (user) {
                                    return {
                                        id: s.id,
                                        email: s.email.substring(s.email.lastIndexOf('/') + 1),
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

    $scope.assign = function (userId, task) {
        $scope.assignmentInProgress = false;
        $http.post(baseUrl + '/task/' + task.id + '/claim', {userId: userId}).then(
            function () {
                $scope.tryToOpen = {};
                $scope.$parent.getTaskList();
                //task.assigneeObject = $rootScope.authUser;
                //task.assignee = $rootScope.authentication.name;
                init();
            },
            function (error) {
                console.log(error.data);
            }
        );
    }
    $scope.assignSelected = function () {
        $scope.assign($scope.newLeasingAssignee, $scope.currentTask);
    }
    $scope.selectedTab = 'form';
    $scope.selectTab = function (tab) {
        $scope.selectedTab = tab;
        if (tab === 'diagram') {
            $scope.getDiagram();
        }
    }
    $scope.getDiagram = function () {
        $http.get(baseUrl + '/process-definition/' + $scope.currentTask.processDefinitionId + '/xml').then(
            function (result) {
                $timeout(function () {
                    $scope.$apply(function () {
                        $scope.diagram = {
                            xml: result.data.bpmn20Xml,
                            task: $scope.currentTask
                        };
                    });
                });
            },
            function (error) {
                console.log(error.data);
            }
        );
    }

    $scope.highlightTask = function () {
        $http({
            method: 'GET',
            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
            url: baseUrl + '/task?processInstanceId=' + $scope.processInstanceId
        }).then(
            function (tasks) {
                var processInstanceTasks = tasks.data._embedded.task;
                if (processInstanceTasks && processInstanceTasks.length > 0) {
                    processInstanceTasks.forEach((task => {
                        $scope.control.highlight(task.taskDefinitionKey);
                    }));
                } else {
                    $scope.control.highlight($scope.diagram.task.taskDefinitionKey);
                }
            },
            function (error) {
                console.log(error.data);
            }
        );
    };

    $scope.assignLinkEnabled = function (processDefinitionKey) {
        if (processDefinitionKey === 'Revision') {
            if ($scope.hasGroup('revision_managers')) {
                return true;
            } else {
                return $scope.currentTask.candidateObject && (($scope.currentTask.candidateObject.groupId === 'hq_leasing' && $scope.hasGroup('hq_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'nc_leasing' && $scope.hasGroup('nc_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'astana_leasing' && $scope.hasGroup('astana_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'alm_leasing' && $scope.hasGroup('alm_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'south_leasing' && $scope.hasGroup('south_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'east_leasing' && $scope.hasGroup('east_leasing'))
                    || ($scope.currentTask.candidateObject.groupId === 'west_leasing' && $scope.hasGroup('west_leasing')));
            }

        } else if (processDefinitionKey === 'Invoice') {
            return $scope.hasGroup('monthly_act_managers');
        } else if (processDefinitionKey === 'Demand') {
            return $scope.hasGroup('demand_uat_users');
        }
    };
}]).controller('processesCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal',
        function ($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal) {

            var camClient = new CamSDK.Client({
                mock: false,
                apiUri: '/camunda/api/engine/'
            });

            $rootScope.currentPage = {
                name: 'processes'
            };

            $scope._ = window._;
            $scope.currentPI = [];
            $scope.participations = [{key: 'initiator', label: 'I am Inititator'}, {
                key: 'participant',
                label: 'I am Participant'
            }];

            $rootScope.logout = function () {
                AuthenticationService.logout().then(function () {
                    $scope.authentication = null;
                });
            }

            var baseUrl = '/camunda/api/engine/engine/default';
            $scope.processDefinitions = $rootScope.getCurrentProcesses();
            $scope.$watchGroup(['selectedProject', 'selectedProcess'], function (newValues, oldValues, scope) {
                if ((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)) {
                    $scope.processDefinitions = $rootScope.getCurrentProcesses();
                    if ($scope.filter.processDefinitionKey && !_.some($scope.processDefinitions, function (pd) {
                        return pd.key === $scope.filter.processDefinitionKey
                    })) {
                        if ($scope.processDefinitions.length > 0) {
                            $scope.filter.processDefinitionKey = $scope.processDefinitions[0].key;
                            $scope.checkParticipation();
                        }
                        $scope.processInstances = undefined;
                    }
                }
            }, true);

            $scope.filter = {
                processDefinitionKey: $scope.processDefinitions[0].key,
                participation: 'initiator',
                startedBy: $rootScope.authentication.name,
                startedAfter: undefined,
                startedBefore: undefined,
                unfinished: true,
                page: 1,
                maxResults: 20
            };

            var catalogs = {};

            $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
                function (result) {
                    angular.extend(catalogs, result.data);
                },
                function (error) {
                    console.log(error.data);
                }
            );

            $scope.search = function (refreshPages) {
                if (refreshPages) {
                    $scope.filter.page = 1;
                    $scope.piIndex = undefined;
                }
                $scope.currentPD = $scope.filter.processDefinitionKey;
                var filter = {
                    processDefinitionKey: $scope.filter.processDefinitionKey,
                    sorting: [{sortBy: "startTime", sortOrder: "desc"}]
                }
                if ($scope.filter.businessKey) {
                    filter.processInstanceBusinessKey = $scope.filter.businessKey;
                }
                if ($scope.filter.unfinished) {
                    filter.unfinished = true;
                } else {
                    delete filter.unfinished;
                }
                if ($scope.filter.participation === 'initiator') {
                    filter.startedBy = $rootScope.authentication.name;
                    getProcessInstances(filter);
                } else if ($scope.filter.participation === 'participant') {
                    $http.post(baseUrl + '/history/task', {taskAssignee: $rootScope.authentication.name}).then(
                        function (result) {
                            filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                            getProcessInstances(filter);
                        },
                        function (error) {
                            console.log(error.data)
                        }
                    );
                } else if ($scope.filter.participation) {
                    console.log($scope.filter.participation);
                    getProcessInstances(filter);
                }
            };

            $scope.nextPage = function () {
                $scope.filter.page++;
                $scope.search(false);
                $scope.piIndex = undefined;
            }

            $scope.prevPage = function () {
                $scope.filter.page--;
                $scope.search(false);
                $scope.piIndex = undefined;
            }

            $scope.checkParticipation = function () {
                if ($scope.filter.processDefinitionKey !== 'Revision' && $scope.filter.participation === 'all') {
                    $scope.filter.participation = undefined;
                }
            }

            function getProcessInstances(filter) {
                $http.post(baseUrl + '/history/process-instance?firstResult=' + ($scope.filter.page - 1) * $scope.filter.maxResults + '&maxResults=' + $scope.filter.maxResults, filter).then(
                    function (result) {
                        $scope.processInstances = result.data;
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
            }

            $scope.showProcessInfo = function (index, processInstanceId, businessKey, processDefinitionKey, userId, startDate) {
                $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                    processDefinitionKey: processDefinitionKey,
                    processInstanceId: processInstanceId,
                }).then(async function (result) {
                        $scope.businessKey = businessKey;
                        $scope.userFullName = null;
                        $scope.startDate = startDate
                        await $http.get('/camunda/api/engine/engine/default/user/' + userId + '/profile').then(
                            function (result) {
                                $scope.userFullName = result.data.firstName + ' ' + result.data.lastName;
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                        var vars = {};
                        result.data.forEach(function (v) {
                            vars[v.name] = v.value;
                        })
                        $scope.processVars = Object.assign({}, vars)
                        openPaymentProcessCardModal()
                    },
                    function (error) {
                        console.log(error.data);
                    });
            };
            function openPaymentProcessCardModal() {
                const processesName = {
                    '1': 'Создание ID Customer',
                    '5': 'Процесс 5',
                    '2': 'Создание FA/BA',
                    '6': 'Процесс 6',
                    '3': 'Создание PPB',
                    '7': 'Процесс 7',
                    '4': 'Изменение кредитного лимита',
                    '8': 'Процесс 8',
                };
                exModal.open({
                    scope: {
                        vars: $scope.processVars,
                        attached: JSON.parse($scope.processVars.files),
                        businessKey: $scope.businessKey,
                        startDate: $scope.startDate,
                        fullName: $scope.userFullName,
                        processesName: processesName,
                        download: function(path) {
                            $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
                            then(function(response) {
                                document.getElementById('fileDownloadIframe').src = response.data;
                            }, function(error){
                                console.log(error);
                            });
                        },
                    },
                    templateUrl: './js/partials/rpaProcessCardModal.html',
                    size: 'hg'
                }).then(function (results) {
                });
            }
            $scope.toggleProcessView = function (index, processDefinitionKey) {
                if (processDefinitionKey === 'SiteSharingTopProcess') {
                    $scope.showDiagramView = false;
                    $scope.diagram = {};
                    if ($scope.piIndex === index) {
                        $scope.piIndex = undefined;
                    } else {
                        $scope.piIndex = index;
                        $scope.jobModel = {state: $scope.processInstances[index].state};
                        console.log('$scope.processInstances[index]');
                        console.log($scope.processInstances[index]);
                        $http.get(baseUrl + '/process-instance?superProcessInstance=' + $scope.processInstances[index].id + '&active=true').then(
                            function (result) {
                                if (result.data.length > 0) {
                                    $scope.currentPI[index] = result.data[0];
                                } else {
                                    $scope.currentPI[index] = $scope.processInstances[index];
                                }
                                $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task?processInstanceId=' + $scope.currentPI[index].id,
                                }).then(
                                    function (tasks) {
                                        var processInstanceTasks = tasks.data._embedded.task;
                                        if (processInstanceTasks && processInstanceTasks.length > 0) {
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
                                                        }
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            });
                                        }
                                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.currentPI[index].id).then(
                                            function (result) {
                                                var workFiles = [];
                                                result.data.forEach(function (el) {
                                                    $scope.jobModel[el.name] = el;
                                                    if (el.type === 'File' || el.type === 'Bytes') {
                                                        $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                                    }
                                                    if (el.type === 'Json') {
                                                        $scope.jobModel[el.name].value = JSON.parse(el.value);
                                                    }
                                                    if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                        workFiles.push(el);
                                                    }
                                                });
                                                console.log($scope.jobModel);
                                                workFiles.forEach(function (file) {
                                                    var workIndex = file.name.split('_')[1];
                                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
                                                    }
                                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                });
                                                if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                                    })).then(function (tasks) {
                                                        tasks.forEach(function (e, index) {
                                                            if (e.data.length > 0) {
                                                                $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                                try {
                                                                    $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                                } catch (e) {
                                                                    console.log(e);
                                                                }
                                                            }
                                                        });
                                                    });
                                                }
                                                angular.extend($scope.jobModel, catalogs);
                                                $scope.jobModel.tasks = processInstanceTasks;
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
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }

                } else if (processDefinitionKey === 'freephone' || processDefinitionKey === 'bulksmsConnectionKAE') {
                    $scope.showDiagramView = false;
                    $scope.diagram = {};
                    if ($scope.piIndex === index) {
                        $scope.piIndex = undefined;
                    } else {
                        $scope.piIndex = index;
                        $scope.jobModel = {state: $scope.processInstances[index].state};
                        console.log('$scope.processInstances[index]');
                        console.log($scope.processInstances[index]);
                        $http.get(baseUrl + '/process-instance?superProcessInstance=' + $scope.processInstances[index].id + '&active=true').then(
                            function (result) {
                                if (result.data.length > 0) {
                                    $scope.currentPI[index] = result.data[0];
                                    console.log($scope.currentPI)
                                } else {
                                    $scope.currentPI[index] = $scope.processInstances[index];
                                }
                                $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task?processInstanceId=' + $scope.currentPI[index].id,
                                }).then(
                                    function (tasks) {
                                        var processInstanceTasks = tasks.data._embedded.task;
                                        if (processInstanceTasks && processInstanceTasks.length > 0) {
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
                                                        }
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            });
                                        }
                                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.currentPI[index].id).then(
                                            function (result) {
                                                var workFiles = [];
                                                result.data.forEach(function (el) {
                                                    $scope.jobModel[el.name] = el;
                                                    if (el.type !== 'Json' && (el.value || el.value === "" || el.type === 'Boolean')) {
                                                        $scope.jobModel[el.name] = el.value;
                                                    }
                                                    if (el.type === 'File' || el.type === 'Bytes') {
                                                        $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                                    }
                                                    if (el.type === 'Json') {
                                                        if (el.name === 'resolutions') {
                                                            $scope.jobModel[el.name].value = JSON.parse(el.value);
                                                        } else if (['contractScanCopyFileName', 'applicationScanCopyFileName'].indexOf(el.name) > -1) {
                                                            if (!$scope.jobModel.files) {
                                                                $scope.jobModel.files = [];
                                                            }
                                                            $scope.jobModel.files.push(JSON.parse(el.value));
                                                        } else {
                                                            $scope.jobModel[el.name] = JSON.parse(el.value);
                                                        }
                                                    }
                                                });
                                                console.log($scope.jobModel);
                                                if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                                    })).then(function (tasks) {
                                                        tasks.forEach(function (e, index) {
                                                            if (e.data.length > 0) {
                                                                $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                                try {
                                                                    $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                                } catch (e) {
                                                                    console.log(e);
                                                                }
                                                            }
                                                        });
                                                    });
                                                }
                                                // angular.extend($scope.jobModel, catalogs);
                                                $scope.jobModel.showTarif = true;
                                                $scope.jobModel.tasks = processInstanceTasks;
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
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }
                } else if (processDefinitionKey === 'UAT') {
                    $scope.printDiv = function (forPrint) {
                        var printContents = document.getElementById('forPrint').innerHTML;
                        var popupWin = window.open('', 'PRINT', 'height=400,width=600');
                        popupWin.document.open();
                        popupWin.document.write('<html><head><link href="css/bootstrap.min.css" rel="stylesheet"><link href="css/styles.css" rel="stylesheet" type="text/css"></head><body onload="window.print()">' + printContents + '</body></html>');
                        popupWin.document.close();
                    }

                    $scope.showDiagramView = false;
                    $scope.diagram = {};
                    if ($scope.piIndex === index) {
                        $scope.piIndex = undefined;
                    } else {
                        $scope.piIndex = index;

                        $scope.jobModel = {state: $scope.processInstances[index].state};
                        $scope.jobModel['businessKeyUAT'] = {value: $scope.processInstances[index].businessKey};
                        $http.get(baseUrl + '/process-instance?superProcessInstance=' + $scope.processInstances[index].id + '&active=true').then(
                            function (result) {
                                if (result.data.length > 0) {
                                    $scope.currentPI[index] = result.data[0];
                                    console.log($scope.currentPI)
                                } else {
                                    $scope.currentPI[index] = $scope.processInstances[index];
                                }
                                $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task?processInstanceId=' + $scope.currentPI[index].id,
                                }).then(
                                    function (tasks) {
                                        var processInstanceTasks = tasks.data._embedded.task;
                                        if (processInstanceTasks && processInstanceTasks.length > 0) {
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
                                                        }
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            });
                                        }
                                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.currentPI[index].id).then(
                                            function (result) {
                                                var workFiles = [];
                                                result.data.forEach(function (el) {
                                                    $scope.jobModel[el.name] = el;
                                                    if (el.type === 'File' || el.type === 'Bytes') {
                                                        $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                                    }
                                                    if (el.type === 'Json') {
                                                        $scope.jobModel[el.name].value = JSON.parse(el.value);
                                                    }
                                                    if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                        workFiles.push(el);
                                                    }
                                                });
                                                console.log($scope.jobModel);
                                                workFiles.forEach(function (file) {
                                                    var workIndex = file.name.split('_')[1];
                                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
                                                    }
                                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                });

                                                $http.get(baseUrl + '/history/process-instance/' + $scope.currentPI[index].id).then(function (pi) {
                                                    if (pi.data.startTime) $scope.jobModel.startTime = {value: new Date(pi.data.startTime)};
                                                    if (pi.data.endTime) $scope.jobModel.endTime = {value: new Date(pi.data.endTime)};
                                                });

                                                if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/user/" + resolution.assignee + "/profile");
                                                    })).then(function (profiles) {
                                                        profiles.forEach(function (e, index) {
                                                            // $scope.jobModel.resolutions[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                                                            $scope.jobModel.resolutions.value[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                                                        });
                                                    });

                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                                    })).then(function (tasks) {
                                                        tasks.forEach(function (e, index) {
                                                            if (e.data.length > 0) {
                                                                $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                                try {
                                                                    $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                                } catch (e) {
                                                                    console.log(e);
                                                                }
                                                            }
                                                        });
                                                    });
                                                }
                                                $scope.jobModel.tasks = processInstanceTasks;
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
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }
                } else if (processDefinitionKey === 'Demand') {
                    $scope.showDiagramView = false;
                    $scope.diagram = {};
                    if ($scope.piIndex === index) {
                        $scope.piIndex = undefined;
                    } else {
                        $scope.piIndex = index;
                        $scope.jobModel = {state: $scope.processInstances[index].state};
                        $http.get(baseUrl + '/process-instance?superProcessInstance=' + $scope.processInstances[index].id + '&active=true').then(
                            function (result) {
                                if (result.data.length > 0) {
                                    $scope.currentPI[index] = result.data[0];
                                    console.log($scope.currentPI)
                                } else {
                                    $scope.currentPI[index] = $scope.processInstances[index];
                                }
                                $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task?processInstanceId=' + $scope.currentPI[index].id,
                                }).then(
                                    function (tasks) {
                                        var processInstanceTasks = tasks.data._embedded.task;
                                        if (processInstanceTasks && processInstanceTasks.length > 0) {
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
                                                        }
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
                                            });
                                        }
                                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.currentPI[index].id).then(
                                            function (result) {
                                                var workFiles = [];
                                                result.data.forEach(function (el) {
                                                    $scope.jobModel[el.name] = el;
                                                    if (el.type === 'File' || el.type === 'Bytes') {
                                                        $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                                    }
                                                    if (el.type === 'Json') {
                                                        $scope.jobModel[el.name].value = JSON.parse(el.value);
                                                    }
                                                    if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                        workFiles.push(el);
                                                    }
                                                });
                                                workFiles.forEach(function (file) {
                                                    var workIndex = file.name.split('_')[1];
                                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
                                                    }
                                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                });

                                                $http.get(baseUrl + '/history/process-instance/' + $scope.currentPI[index].id).then(function (pi) {
                                                    if (pi.data.startTime) $scope.jobModel.startTime = {value: new Date(pi.data.startTime)};
                                                    if (pi.data.endTime) $scope.jobModel.endTime = {value: new Date(pi.data.endTime)};
                                                });

                                                if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/user/" + resolution.assignee + "/profile");
                                                    })).then(function (profiles) {
                                                        profiles.forEach(function (e, index) {
                                                            // $scope.jobModel.resolutions[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                                                            $scope.jobModel.resolutions.value[index].assigneeName = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                                                        });
                                                    });

                                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                                    })).then(function (tasks) {
                                                        tasks.forEach(function (e, index) {
                                                            if (e.data.length > 0) {
                                                                $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                                try {
                                                                    $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                                } catch (e) {
                                                                    console.log(e);
                                                                }
                                                            }
                                                        });
                                                    });
                                                }
                                                $scope.jobModel.tasks = processInstanceTasks;
                                                console.log("===> ", $scope.jobModel);
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
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }
                } else {
                    $scope.showDiagramView = false;
                    $scope.diagram = {};
                    if ($scope.piIndex === index) {
                        $scope.piIndex = undefined;
                    } else {
                        $scope.piIndex = index;
                        $scope.jobModel = {
                            state: $scope.processInstances[index].state,
                            processDefinitionKey: processDefinitionKey
                        };
                        $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: baseUrl + '/task?processInstanceId=' + $scope.processInstances[index].id,
                        }).then(
                            function (tasks) {
                                var processInstanceTasks = tasks.data._embedded.task;
                                if (processInstanceTasks && processInstanceTasks.length > 0) {
                                    processInstanceTasks.forEach(function (e) {
                                        if (e.assignee && tasks.data._embedded.assignee) {
                                            for (var i = 0; i < tasks.data._embedded.assignee.length; i++) {
                                                if (tasks.data._embedded.assignee[i].id === e.assignee) {
                                                    e.assigneeObject = tasks.data._embedded.assignee[i];
                                                }
                                                $http({
                                                    method: 'GET',
                                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                                    url: baseUrl + '/task/' + e.id
                                                }).then(
                                                    function (taskResult) {
                                                        if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                                            e.group = taskResult.data._embedded.group[0].id;
                                                        }
                                                    },
                                                    function (error) {
                                                        console.log(error.data);
                                                    }
                                                );
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
                                                }
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );
                                    });
                                }
                                $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.processInstances[index].id).then(
                                    function (result) {
                                        var workFiles = [];
                                        $scope.jobModel.files = [];
                                        result.data.forEach(function (el) {
                                            $scope.jobModel[el.name] = el;
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                if (el.name === 'attachInvoiceFileName') {
                                                    $scope.jobModel.files.push(JSON.parse(el.value));
                                                }
                                                if (el.name === 'signedScanCopyFileName') {
                                                    $scope.jobModel.files.push(JSON.parse(el.value));
                                                } else {
                                                    $scope.jobModel[el.name].value = JSON.parse(el.value);
                                                }
                                            }
                                            if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                workFiles.push(el);
                                            }
                                        });
                                        if ($scope.jobModel['siteWorksFiles']) {
                                            _.forEach($scope.jobModel['siteWorksFiles'].value, function (file) {
                                                var workIndex = file.name.split('_')[1];
                                                if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                                    $scope.jobModel.jobWorks.value[workIndex].files = [];
                                                }
                                                if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                                    return f.name == file.name;
                                                }) < 0) {
                                                    $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                }
                                            });
                                        }
                                        _.forEach(workFiles, function (file) {
                                            var workIndex = file.name.split('_')[1];
                                            if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                                $scope.jobModel.jobWorks.value[workIndex].files = [];
                                            }
                                            if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                                return f.name == file.name;
                                            }) < 0) {
                                                $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                            }
                                        });
                                        if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                            $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                                return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                            })).then(function (tasks) {
                                                tasks.forEach(function (e, index) {
                                                    if (e.data.length > 0) {
                                                        $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                        try {
                                                            $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                        } catch (e) {
                                                            console.log(e);
                                                        }
                                                    }
                                                });
                                            });
                                        }
                                        angular.extend($scope.jobModel, catalogs);
                                        $scope.jobModel.tasks = processInstanceTasks;
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
                }
            };

            $scope.showDiagram = function (index, processDefinitionKey) {
                if (['SiteSharingTopProcess', 'freephone', 'bulksmsConnectionKAE'].indexOf(processDefinitionKey) !== -1) {
                    console.log(index)
                    console.log($scope.currentPI[index])
                    if ($scope.currentPI[index].definitionId) {
                        var processDefinitionId = $scope.currentPI[index].definitionId;
                    } else {
                        var processDefinitionId = $scope.currentPI[index].processDefinitionId;
                    }
                } else {
                    var processDefinitionId = $scope.processInstances[index].processDefinitionId;
                }
                $scope.showDiagramView = true;
                getDiagram(processDefinitionId);
            }

            function getDiagram(processDefinitionId) {
                $http.get(baseUrl + '/process-definition/' + processDefinitionId + '/xml').then(
                    function (result) {
                        $timeout(function () {
                            $scope.$apply(function () {
                                $scope.diagram = {
                                    xml: result.data.bpmn20Xml,
                                    tasks: ($scope.jobModel.tasks && $scope.jobModel.tasks.length > 0) ? $scope.jobModel.tasks : undefined
                                };
                            });
                        });
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
            }

            $scope.highlightTask = function () {
                $scope.diagram.tasks.forEach((task => {
                    $scope.control.highlight(task.taskDefinitionKey);
                }));
            };

            $scope.showGroupDetails = function (group) {
                $http({
                    method: 'GET',
                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                    url: baseUrl + '/user?memberOfGroup=' + group
                }).then(
                    function (result) {
                        exModal.open({
                            scope: {
                                members: result.data,
                            },
                            templateUrl: './js/partials/members.html',
                            size: 'md'
                        }).then(function (results) {
                        });
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
            };

            $scope.showHistory = function (resolutions) {
                exModal.open({
                    scope: {
                        resolutions: resolutions.value,
                        isKcellStaff: $rootScope.hasGroup('kcellUsers'),
                        procDef: $scope.currentPD,
                        download: function (path) {
                            $http({
                                method: 'GET',
                                url: '/camunda/uploads/get/' + path,
                                transformResponse: []
                            }).then(function (response) {
                                document.getElementById('fileDownloadIframe').src = response.data;
                            }, function (error) {
                                console.log(error);
                            });
                        },
                        isFileVisible: function (file) {
                            return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
                        }
                    },
                    templateUrl: './js/partials/resolutionsModal.html',
                    size: 'hg'
                }).then(function (results) {
                });
            };

            $scope.hasPermissionToViewAll = function (processDefinitionKey) {
                if (processDefinitionKey === 'Revision') {
                    return $scope.hasOneOfListedGroup(['revision_managers', 'revision_audit']);
                } else if (processDefinitionKey === 'Invoice') {
                    return $scope.hasOneOfListedGroup(['monthly_act_managers', 'monthly_act_audit']);
                } else return false;
            }
            $scope.hasOneOfListedGroup = function (groups) {
                return _.some(groups, function (group) {
                    return $rootScope.hasGroup(group);
                });
            }

            $scope.hasLinkToTask = function (task, processDefinitionKey, accessGroups) {
                if (task.processDefinitionId.substring(0, task.processDefinitionId.indexOf(':')) === processDefinitionKey) {
                    return $scope.hasOneOfListedGroup(accessGroups);
                } else
                    return false;
            }

            $scope.hasAccessViewTaskDetail = function (pi) {
                return $rootScope.hasGroup('kcellUsers') || ($scope.currentPD === 'Invoice' && pi.startUserId === $rootScope.authUser.id);
            }
        }]).controller('searchCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal', '$state', 'StartProcessService', 'SearchCurrentSelectedProcessService', 'disconnectSelectedProcessService',
    function ($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal, $state, StartProcessService, SearchCurrentSelectedProcessService, disconnectSelectedProcessService) {

        var camClient = new CamSDK.Client({
            mock: false,
            apiUri: '/camunda/api/engine/'
        });
        $scope.tabs = {};
        angular.forEach($rootScope.projects, function (value, processKey) {
            $scope.tabs[processKey] = false;
        });
        $scope.isProjectAvailable = function (projectKey) {
            return !!$rootScope.isProjectAvailable(projectKey);
        };
        var baseUrl = '/camunda/api/engine/engine/default';
        var catalogs = {};

        $rootScope.currentPage = {
            name: 'search'
        };

        if (window.require) {
            $scope.XLSX = require('xlsx');
        }

        $scope._ = window._;
        $scope.regionsMap = {
            'alm': 'Almaty',
            'astana': 'Astana',
            'nc': 'North & Center',
            'east': 'East',
            'south': 'South',
            'west': 'West'
        };
        $scope.contractorShortName = {
            '4': 'LSE'
        };
        $scope.reasonShortName = {
            '1': 'P&O',
            '2': 'TNU',
            '3': 'S&FM',
            '4': 'SAO'
        };
        $scope.profiles = {};

        var regionGroupsMap = {
            'alm_kcell_users': 'alm',
            'astana_kcell_users': 'astana',
            'nc_kcell_users': 'nc',
            'east_kcell_users': 'east',
            'south_kcell_users': 'south',
            'west_kcell_users': 'west'
        }

        $scope.convertStringToDate = function (date_string) {
            var result = date_string.split(" - ");
            if (result.length === 2) {
                var dateParts_from = result[0].split(".");
                if (dateParts_from[2].length === 2) {
                    dateParts_from[2] = "20" + dateParts_from[2];
                }
                var dateObject_from = new Date(+dateParts_from[2], dateParts_from[1] - 1, +dateParts_from[0]);
                var dateParts_to = result[1].split(".");
                if (dateParts_to[2].length === 2) {
                    dateParts_to[2] = "20" + dateParts_to[2];
                }
                var dateObject_to = new Date(+dateParts_to[2], dateParts_to[1] - 1, +dateParts_to[0]);
                dateObject_to.setDate(dateObject_to.getDate() + 1);
                return [dateObject_from, dateObject_to];
            }
        }



        if ($rootScope.authentication) {
            $http.get(baseUrl + '/user/' + $rootScope.authentication.name + '/profile').then(
                function (userProfile) {
                    $rootScope.authUser = userProfile.data;
                    $http.get(baseUrl + '/group?member=' + $rootScope.authUser.id).then(
                        function (groups) {
                            $rootScope.authUser.groups = groups.data;

                            if (!$rootScope.hasGroup('revision_managers') && !$rootScope.hasGroup('revision_audit')) {
                                _.forEach($rootScope.authUser.groups, function (group) {
                                    if (regionGroupsMap[group.id]) {
                                        $scope.filter.region = regionGroupsMap[group.id];
                                    }
                                });
                            }
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

        $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
            function (result) {
                angular.extend($scope, result.data);
                angular.extend(catalogs, result.data);

            },
            function (error) {
                console.log(error.data);
            }
        );

        $rootScope.logout = function () {
            AuthenticationService.logout().then(function () {
                $scope.authentication = null;
            });
        }

        $rootScope.hasGroup = function (group) {
            if ($rootScope.authUser && $rootScope.authUser.groups) {
                return _.some($rootScope.authUser.groups, function (value) {
                    return value.id === group;
                });
            } else {
                return false;
            }
        }

        $scope.getpreparedXlsxProcessInstances = function () {
            var tbl = document.getElementById('revisionsTable');
            var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});

            var wb = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

            return XLSX.writeFile(wb, 'revision-search-result.xlsx');
        }


        $scope.getUsers = function (val) {
            if ($scope.filterDP) $scope.filterDP.salesReprId = undefined;
            var res = val.split(' ');
            var firstName = val;
            var lastName = val;
            if (res.length > 1) {
                //space is there
                firstName = res[0];
                lastName = res[1];
                var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + firstName + '%') + '&lastNameLike=' + encodeURIComponent('%' + lastName + '%')).then(
                    function (response) {
                        //console.log(response.data);
                        var usersByFirstName = _.flatMap(response.data, function (s) {
                            if (s.id) {
                                return s.id.split(',').map(function (user) {
                                    return {
                                        id: s.id,
                                        email: s.email ? s.email.substring(s.email.lastIndexOf('/') + 1) : s.email,
                                        firstName: s.firstName,
                                        lastName: s.lastName,
                                        name: s.firstName + ' ' + s.lastName
                                    };
                                })
                            } else {
                                return [];
                            }
                        });
                        return usersByFirstName;
                    }
                );
                return users;
            } else {
                var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + firstName + '%')).then(
                    function (response) {
                        var usersByFirstName = _.flatMap(response.data, function (s) {
                            if (s.id) {
                                return s.id.split(',').map(function (user) {
                                    return {
                                        id: s.id,
                                        email: s.email ? s.email.substring(s.email.lastIndexOf('/') + 1) : s.email,
                                        firstName: s.firstName,
                                        lastName: s.lastName,
                                        name: s.firstName + ' ' + s.lastName
                                    };
                                })
                            } else {
                                return [];
                            }
                        });
                        //return usersByFirstName;
                        return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + lastName + '%')).then(
                            function (response) {
                                var usersByLastName = _.flatMap(response.data, function (s) {
                                    if (s.id) {
                                        return s.id.split(',').map(function (user) {
                                            return {
                                                id: s.id,
                                                email: s.email ? s.email.substring(s.email.lastIndexOf('/') + 1) : s.email,
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

            }
        };

        $scope.showTaskDetail = function (task) {
            exModal.open({
                scope: {
                    task: task
                },
                templateUrl: './js/partials/taskModal.html',
                controller: TaskModalController,
                size: 'md'
            }).then(function (results) {
            });
        }

        $scope.showWorkDetail = function (jobWorks) {
            exModal.open({
                scope: {
                    jobWorks: jobWorks,
                    worksTitle: $scope.worksTitle
                },
                templateUrl: './js/partials/jobWorksModal.html',
                size: 'lg'
            }).then(function (results) {
            });
        }

        TaskModalController.$inject = ['scope', '$http'];

        function TaskModalController(scope, $http) {
            $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/task/' + scope.task.id
            }).then(
                function (result) {
                    if (result.data._embedded && result.data._embedded.group && result.data._embedded.group.length > 0) {
                        scope.task.group = result.data._embedded.group[0].id;
                        $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: baseUrl + '/user?memberOfGroup=' + result.data._embedded.group[0].id
                        }).then(
                            function (r) {
                                scope.task.members = r.data;
                            }
                        );
                    }
                }
            );
        }

        $scope.toggleProcessView = function (index, processDefinitionKey, processDefinitionId) {
            $scope.showDiagramView = false;
            $scope.diagram = {};
            if ($scope.piIndex === index) {
                $scope.piIndex = undefined;
            } else {
                $scope.piIndex = index;
                $scope.jobModel = {
                    state: $scope.processInstancesDemand[index].state,
                    processDefinitionKey: processDefinitionKey,
                    processDefinitionId: processDefinitionId,
                    startTime: {value: $scope.processInstancesDemand[index].startTime,},
                    endTime: {value: $scope.processInstancesDemand[index].endTime,}
                };
                $http({
                    method: 'GET',
                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                    url: baseUrl + '/task?processInstanceId=' + $scope.processInstancesDemand[index].id,
                }).then(
                    function (tasks) {
                        var asynCall1 = false;
                        var asynCall2 = false;
                        var asynCall3 = false;
                        var processInstanceTasks = tasks.data._embedded.task;
                        if (processInstanceTasks && processInstanceTasks.length > 0) {
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
                                        }
                                        asynCall1 = true;
                                        if (asynCall1 && asynCall2 && asynCall3) {
                                            openProcessCardModalDemand();
                                            asynCall1 = false;
                                        } else console.log(asynCall1, asynCall2, asynCall3);
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );
                            });
                        } else {
                            asynCall1 = true;
                            if (asynCall1 && asynCall2 && asynCall3) {
                                openProcessCardModalDemand();
                                asynCall1 = false;
                            } else console.log(asynCall1, asynCall2, asynCall3);
                        }
                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + $scope.processInstancesDemand[index].id).then(
                            function (result) {
                                var workFiles = [];
                                result.data.forEach(function (el) {
                                    $scope.jobModel[el.name] = el;
                                    if (el.type === 'File' || el.type === 'Bytes') {
                                        $scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                    }
                                    if (el.type === 'Json') {
                                        $scope.jobModel[el.name].value = JSON.parse(el.value);
                                    }
                                    if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                        workFiles.push(el);
                                    }
                                });
                                if ($scope.jobModel['siteWorksFiles']) {
                                    _.forEach($scope.jobModel['siteWorksFiles'].value, function (file) {
                                        var workIndex = file.name.split('_')[1];
                                        if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                            $scope.jobModel.jobWorks.value[workIndex].files = [];
                                        }
                                        if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                            return f.name == file.name;
                                        }) < 0) {
                                            $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                        }
                                    });
                                }
                                _.forEach(workFiles, function (file) {
                                    var workIndex = file.name.split('_')[1];
                                    if (!$scope.jobModel.jobWorks.value[workIndex].files) {
                                        $scope.jobModel.jobWorks.value[workIndex].files = [];
                                    }
                                    if (_.findIndex($scope.jobModel.jobWorks.value[workIndex].files, function (f) {
                                        return f.name == file.name;
                                    }) < 0) {
                                        $scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                    }
                                });
                                if ($scope.jobModel.resolutions && $scope.jobModel.resolutions.value) {
                                    $q.all($scope.jobModel.resolutions.value.map(function (resolution) {
                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                    })).then(function (tasks) {
                                        tasks.forEach(function (e, index) {
                                            if (e.data.length > 0) {
                                                $scope.jobModel.resolutions.value[index].taskName = e.data[0].name;
                                                try {
                                                    $scope.jobModel.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                } catch (e) {
                                                    console.log(e);
                                                }
                                            }
                                        });
                                        asynCall2 = true;
                                        if (asynCall1 && asynCall2 && asynCall3) {
                                            openProcessCardModalDemand();
                                            asynCall1 = false;
                                            asynCall2 = false;
                                            asynCall3 = false;
                                        } else console.log(asynCall1, asynCall2, asynCall3);
                                    });
                                }

                                //$scope.jobModel.tasks = processInstanceTasks;
                                angular.extend($scope.jobModel, catalogs);
                                $scope.jobModel.tasks = processInstanceTasks;
                                asynCall3 = true;
                                if (asynCall1 && asynCall2 && asynCall3) {
                                    openProcessCardModalDemand();
                                    asynCall1 = false;
                                    asynCall2 = false;
                                    asynCall3 = false;
                                } else console.log(asynCall1, asynCall2, asynCall3);
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

        $scope.showGroupDetails = function (group) {
            $http({
                method: 'GET',
                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                url: baseUrl + '/user?memberOfGroup=' + group
            }).then(
                function (result) {
                    exModal.open({
                        scope: {
                            members: result.data,
                        },
                        templateUrl: './js/partials/members.html',
                        size: 'md'
                    }).then(function (results) {
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
        };
        $scope.getStatus = function (state, value) {
            return (state == 'COMPLETED' || state == 'EXTERNALLY_TERMINATED' || state == 'INTERNALLY_TERMINATED') ? 'Closed' : (value == 'accepted' ? 'Accepted & waiting scan attach' : (value == 'scan attached' ? 'Accepted & waiting invoice' : 'In progress'))
        };

        function getDiagram(processDefinitionId, tasks, aftersalesTasks) {
            $http.get(baseUrl + '/process-definition/' + processDefinitionId + '/xml').then(
                function (result) {
                    $timeout(function () {
                        $scope.$apply(function () {
                            $scope.diagram = {
                                xml: result.data.bpmn20Xml,
                                tasks: (tasks && tasks.length > 0) ? tasks : undefined,
                                callActivityTasks: (aftersalesTasks && aftersalesTasks.length > 0) ? aftersalesTasks : undefined,

                            };
                        });
                    });
                },
                function (error) {
                    console.log(error.data);
                }
            );
        }

        $scope.showDiagram = function (processDefinitionId, tasks, aftersalesTasks) {
            console.log(tasks, aftersalesTasks);
            $scope.showDiagramView = true;
            getDiagram(processDefinitionId, tasks, aftersalesTasks);
        }


        $scope.highlightTask = function () {
            if ($scope.diagram.tasks && $scope.diagram.tasks.length) {
                $scope.diagram.tasks.forEach((task => {
                    $scope.control.highlight(task.taskDefinitionKey);
                }));
            }
            /*for AS bulk and freephone - if subprocess*/
            console.log('$scope.diagram.callActivityTasks', $scope.diagram.callActivityTasks);
            if ($scope.diagram.callActivityTasks && $scope.diagram.callActivityTasks.length) {
                $scope.diagram.callActivityTasks.forEach((task => {
                    $scope.control.highlight(task.activityId);
                }));
            }
        };


        $scope.open = function ($event, dateFieldOpened) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope[dateFieldOpened] = true;
        };

        function getUserTasks(xml) {
            var namespaces = {
                bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
            };

            var userTaskNodes = [
                ...getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]),
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

            //console.log('userTaskNodes', userTaskNodes);

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
        };

        $scope.showTaskList = function (tasks) {
            exModal.open({
                scope: {
                    tasks: tasks
                },
                templateUrl: './js/partials/taskListModal.html',
                size: 'lg'
            }).then(function (results) {
            });
        }

        $scope.userSelected = function ($item, $model, $label) {
            $scope.initiator = $item;
            $scope.initiatorId = $item.id;
        };

        function openProcessCardModalDemand() {
            var template = './js/partials/infoDemandSearch.html';
            console.log($scope.jobModel);
            exModal.open({
                scope: {
                    jobModel: $scope.jobModel,
                    diagram: [],
                    getStatus: $scope.getStatus,
                    showDiagram: $scope.showDiagram,
                    showHistory: $scope.showHistory,
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
                },
                templateUrl: template,
                size: 'lg'
            }).then(function (results) {
            });
        }

        $scope.startProcess = function (id) {
            disconnectSelectedProcessService(false);
            StartProcessService(id);
        };
        $scope.startDisconnectProcess = function (id) {
            disconnectSelectedProcessService(true);
            StartProcessService(id);
        };
        // -- Demand Management
        let initDemandUAT = () => {
            $scope.processInstancesDemandPages = 0;
            $scope.processInstancesDemandTotal = 0;
            $scope.clearDemand = function () {
                $scope.filterDemand.businessKey = undefined;
                $scope.filterDemand.demandOwner = undefined;
                $scope.filterDemand.demandOwnerId = undefined;
                $scope.filterDemand.assignee = undefined;
                $scope.filterDemand.assigneeId = undefined;
                $scope.filterDemand.demandName = undefined;
                $scope.filterDemand.activityId = undefined;
                $scope.model_dummy = {};
                $scope.filterDemand.technicalAnalysisValue = 'ignore';
                $scope.filterDemand.createStartDateShowPeriod = false;
                $scope.filterDemand.createCloseDateShowPeriod = false;
                $scope.filterDemand.createPlannedDateShowPeriod = false;
                $scope.filterDemand.createActualDateShowPeriod = false;
                $scope.filterDemand.demandDescription = undefined;
                $scope.filterDemand.productName = undefined;
                $scope.filterDemand.productOffer = undefined;
                $scope.filterDemand.initiativeDep = undefined;
                $scope.filterDemand.status = undefined;
                $scope.filterDemand.createdOperator = undefined;
                $scope.filterDemand.createdStart = undefined;
                $scope.filterDemand.createdEnd = undefined;
                $scope.filterDemand.closedOperator = undefined;
                $scope.filterDemand.closedStart = undefined;
                $scope.filterDemand.closedEnd = undefined;
                $scope.filterDemand.plannedLaunchOperator = undefined;
                $scope.filterDemand.plannedLaunchStart = undefined;
                $scope.filterDemand.plannedLaunchEnd = undefined;
                $scope.filterDemand.actualLaunchOperator = undefined;
                $scope.filterDemand.actualLaunchStart = undefined;
                $scope.filterDemand.actualLaunchEnd = undefined;
                /*$scope.processInstancesDemandTotal = 0;
                $scope.processInstancesDemandPages = 0;
                $scope.processInstancesDemand = undefined;
                $scope.lastSearchParams = undefined;*/
            };
            $scope.selectPageDemand = function (page) {
                $scope.filterDemand.page = page;
                $scope.demandSearch(false);
                $scope.piIndex = undefined;
            };
            $scope.nextPageDemand = function () {
                $scope.filterDemand.page++;
                $scope.demandSearch(false);
                $scope.piIndex = undefined;
            };
            $scope.prevPageDemand = function () {
                $scope.filterDemand.page--;
                $scope.demandSearch(false);
                $scope.piIndex = undefined;
            };
            $scope.getPagesDemand = function () {
                var array = [];
                if ($scope.processInstancesDemandPages < 8) {
                    for (var i = 1; i <= $scope.processInstancesDemandPages; i++) {
                        array.push(i);
                    }
                } else {
                    var decrease = $scope.filterDemand.page - 1;
                    var increase = $scope.filterDemand.page + 1;
                    array.push($scope.filterDemand.page);
                    while (increase - decrease < 8) {
                        if (decrease > 0) {
                            array.unshift(decrease--);
                        }
                        if (increase < $scope.processInstancesDemandPages) {
                            array.push(increase++);
                        }
                    }
                }
                return array;
            };
            $scope.demandTasks = [];
            $http.get(baseUrl + '/process-definition/key/' + 'Demand' + '/xml').then(
                function (response) {
                    if (response) {
                        var domParser = new DOMParser();
                        var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');
                        $scope.demandTasks = getUserTasks(xml);
                        $scope.demandTasks = $scope.demandTasks.map(e => {
                            if (e.id === 'supportiveInputBI') e.name = 'Supportive input BI';
                            else if (e.id === 'supportiveInputBI') e.name = 'Supportive input BI';
                            else if (e.id === 'supportiveInputCPD') e.name = 'Supportive input CPD';
                            else if (e.id === 'supportiveInputHR') e.name = 'Supportive input HR';
                            else if (e.id === 'supportiveInputLD') e.name = 'Supportive input LD';
                            else if (e.id === 'supportiveInputTD') e.name = 'Supportive input TD';
                            else if (e.id === 'supportiveInputWH') e.name = 'Supportive input WH';
                            else if (e.id === 'supportiveInputOther') e.name = 'Supportive input Other';
                            else if (e.id === 'requestInfoCPD') e.name = 'Request info Centralized Procurement Department';
                            else if (e.id === 'requestInfoHR') e.name = 'Request info Human Resources Department';
                            else if (e.id === 'requestInfoOther') e.name = 'Request info Technology Department';
                            return e;
                        });
                    }
                }
            ).catch(e => console.log("error: ", e));
            $scope.demandOptions = [];
            $scope.multiselectSettings = {};
            $scope.demandOptions.push({id: '-100', name: 'Select Activity'});
            $scope.$watch('demandTasks', function (Values) {
                _.forEach(Values, function (val) {
                    $scope.demandOptions.push({id: val.id, name: val.name});
                });
            });
            $scope.translationSettings = {
                buttonDefaultText: "Search Activity"
            };
            $scope.multiselectSettings = {
                enableSearch: true,
                showCheckAll: false,
                showUncheckAll: false,
                displayProp: 'name',
                idProperty: 'id',
                selectionLimit: 1,
                smartButtonMaxItems: 1,
                closeOnSelect: true
            };
            $scope.filterDemand = {
                technicalAnalysisValue: 'ignore',
                page: 1,
                maxResults: 20
            };
            $scope.multiselectEvents = {
                onItemSelect: function (item) {
                    $scope.filterDemand.activityId = [item.id];
                    if (item.id === '-100') $scope.filterDemand.activityId = undefined;
                },
                onItemDeselect: function (item) {
                    $scope.filterDemand.activityId = undefined;
                },
            };
            $scope.model_dummy = {};

            $scope.statusList = [
                'New order',
                'Order modified',
                'CVP approved',
                'Canceled on CVP review',
                'Modifying of General info',
                'Canceled on Requirement check',
                'Business case approved',
                'Canceled on Financial review',
                'IPM approved',
                'IPM approved conditionally',
                'Order planned',
                'Canceled on IPM Committee',
                'Finished on Follow-up',
                'Canceled on Follow-up'
            ];

            $scope.initiativeDepList = [
                "B2B",
                "B2C",
                "CCD",
                "CEO",
                "CPD",
                "FD",
                "HR",
                "LD",
                "TD",
                "NB"
            ];
            $scope.getDemandOwnerUsers = (val) => {
                $scope.filterDemand.demandOwnerId = null;
                return $scope.getUsers(val);
            };
            $scope.demandOwnerSelected = (user) => {
                $scope.filterDemand.demandOwner = user.name;
                $scope.filterDemand.demandOwnerId = user.id;
            };

            $scope.getDemandAssigneeUsers = (val) => {
                $scope.filterDemand.assigneeId = null;
                return $scope.getUsers(val);
            };
            $scope.demandAssigneeSelected = (user) => {
                $scope.filterDemand.assignee = user.name;
                $scope.filterDemand.assigneeId = user.id;
            };

            $scope.demandSearch = function (refresh) {
                if (refresh) {
                    $scope.filterDemand.page = 1;
                    $scope.processIndex = undefined;
                    $scope.xlsxPreparedDemand = false;
                }
                var filter = {
                    processDefinitionKey: 'Demand',
                    sorting: [{sortBy: "startTime", sortOrder: "desc"}],
                    variables: [],
                    processInstanceBusinessKeyLike: '%-%'
                };
                if ($scope.filterDemand.technicalAnalysisValue === "true") {
                    filter.variables.push({
                        name: "searchTechnicalAnalysis",
                        operator: "eq",
                        value: true
                    });
                } else if ($scope.filterDemand.technicalAnalysisValue === "false") {
                    filter.variables.push({
                        name: "searchTechnicalAnalysis",
                        operator: "eq",
                        value: false
                    });
                }


                if ($scope.filterDemand.businessKey) {
                    filter.processInstanceBusinessKeyLike = '%' + $scope.filterDemand.businessKey + '%';
                }

                if ($scope.filterDemand.demandOwnerId) {
                    filter.startedBy = $scope.filterDemand.demandOwnerId;
                }

                if ($scope.filterDemand.activityId) {
                    filter.activeActivityIdIn = $scope.filterDemand.activityId;
                }


                if ($scope.filterDemand.demandOwnerId) {
                    filter.startedBy = $scope.filterDemand.demandOwnerId;
                }

                if ($scope.filterDemand.demandName) {
                    filter.variables.push({
                        name: "searchDemandName",
                        operator: "like",
                        value: '%' + $scope.filterDemand.demandName.toLowerCase() + '%'
                    });
                }

                if ($scope.filterDemand.status) {
                    filter.variables.push({
                        name: "status",
                        operator: "eq",
                        value: $scope.filterDemand.status
                    });
                }

                if ($scope.filterDemand.initiativeDep) {
                    filter.variables.push({
                        name: "searchInitialDep",
                        operator: "eq",
                        value: $scope.filterDemand.initiativeDep
                    });
                }

                if ($scope.filterDemand.productName) {
                    filter.variables.push({
                        name: "searchProductName",
                        operator: "like",
                        value: '%' + $scope.filterDemand.productName.toLowerCase() + '%'
                    });
                }

                if ($scope.filterDemand.productOffer) {
                    filter.variables.push({
                        name: "productOfferNames",
                        operator: "like",
                        value: '%' + $scope.filterDemand.productOffer.toLowerCase() + '%'
                    });
                }


                if ($scope.filterDemand.demandDescription) {
                    filter.variables.push({
                        name: "searchDemandDescription",
                        operator: "like",
                        value: '%' + $scope.filterDemand.demandDescription.toLowerCase() + '%'
                    });
                }

                if ($scope.filterDemand.createStartDateShowPeriod && $scope.filterDemand.createdStart && $scope.filterDemand.createdEnd) {
                    filter.startedAfter = new Date($scope.filterDemand.createdStart);
                    filter.startedBefore = new Date($scope.filterDemand.createdEnd);
                } else if ($scope.filterDemand.createdStart) {
                    filter.startedAfter = new Date($scope.filterDemand.createdStart);
                    let temp = new Date($scope.filterDemand.createdStart);
                    temp.setHours(temp.getHours() + 24);
                    filter.startedBefore = temp;
                }


                if ($scope.filterDemand.createCloseDateShowPeriod && $scope.filterDemand.closedStart && $scope.filterDemand.closedEnd) {
                    filter.finishedAfter = new Date($scope.filterDemand.closedStart);
                    filter.finishedBefore = new Date($scope.filterDemand.closedEnd);
                } else if ($scope.filterDemand.closedStart) {
                    filter.finishedAfter = new Date($scope.filterDemand.closedStart);
                    let temp = new Date($scope.filterDemand.closedStart);
                    temp.setHours(temp.getHours() + 24);
                    filter.finishedBefore = temp;
                }


                if ($scope.filterDemand.createPlannedDateShowPeriod && $scope.filterDemand.plannedLaunchStart && $scope.filterDemand.plannedLaunchEnd) {
                    filter.variables.push({
                        name: "searchPlannedLaunch",
                        operator: 'gteq',
                        value: (new Date($scope.filterDemand.plannedLaunchStart)).getTime()
                    });
                    filter.variables.push({
                        name: "searchPlannedLaunch",
                        operator: 'lteq',
                        value: (new Date($scope.filterDemand.plannedLaunchEnd)).getTime()
                    });
                } else if ($scope.filterDemand.plannedLaunchStart) {
                    filter.variables.push({
                        name: "searchPlannedLaunch",
                        operator: 'eq',
                        value: (new Date($scope.filterDemand.plannedLaunchStart)).getTime()
                    });
                }

                if ($scope.filterDemand.createActualDateShowPeriod && $scope.filterDemand.actualLaunchStart && $scope.filterDemand.actualLaunchEnd) {

                    filter.variables.push({
                        name: "searchActualLaunch",
                        operator: 'gteq',
                        value: (new Date($scope.filterDemand.actualLaunchStart)).getTime()
                    });
                    filter.variables.push({
                        name: "searchActualLaunch",
                        operator: 'lteq',
                        value: (new Date($scope.filterDemand.actualLaunchEnd)).getTime()
                    });
                } else if ($scope.filterDemand.actualLaunchStart) {
                    filter.variables.push({
                        name: "searchActualLaunch",
                        operator: 'eq',
                        value: (new Date($scope.filterDemand.actualLaunchStart)).getTime()
                    });
                }

                if ($scope.filterDemand.assigneeId) {
                    $http.post(baseUrl + '/history/task', {
                        taskAssignee: $scope.filterDemand.assigneeId,
                        unfinished: true
                    }).then(
                        function (result) {
                            filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                            $scope.lastSearchParamsDemand = filter;

                            getDemandProcessInstances(filter);
                        }
                    ).catch(e => console.log("error: ", e));
                } else {
                    $scope.lastSearchParamsDemand = filter;
                    getDemandProcessInstances(filter);
                }
            };


            var getDemandProcessInstances = (filter, full) => {
                $http({
                    method: 'POST',
                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                    data: filter,
                    url: baseUrl + '/history/process-instance/count'
                }).then(
                    function (result) {
                        $scope.processInstancesDemandTotal = result.data.count;
                        $scope.processInstancesDemandPages = Math.floor(result.data.count / $scope.filterDemand.maxResults) + ((result.data.count % $scope.filterDemand.maxResults) > 0 ? 1 : 0);
                    }
                );
                $http({
                    method: 'POST',
                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                    data: filter,
                    url: baseUrl + '/history/process-instance' + (full ? '' : '?firstResult=' + ($scope.filterDemand.page - 1) * $scope.filterDemand.maxResults + '&maxResults=' + $scope.filterDemand.maxResults)
                }).then(
                    function (result) {
                        $scope.processInstancesDemand = result.data;
                        if ($scope.processInstancesDemand.length) {
                            $scope.processInstancesDemand.forEach((e) => {
                                if (!$scope.profiles[e.startUserId]) {
                                    $http.get(baseUrl + '/user/' + e.startUserId + '/profile').then(
                                        function (result) {
                                            $scope.profiles[e.startUserId] = result.data;
                                        }
                                    ).catch(e => console.log("error: ", e));
                                }
                            });
                            let fetchVars = ['generalData', 'demandName', 'status'];
                            fetchVars.forEach((v) => {
                                $http({
                                    method: 'POST',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    data: {
                                        processInstanceIdIn: _.map($scope.processInstancesDemand, 'id'),
                                        variableName: v
                                    },
                                    url: baseUrl + '/history/variable-instance?deserializeValues=false'
                                }).then(
                                    function (result) {
                                        let variables = _.groupBy(result.data, 'processInstanceId');
                                        $scope.processInstancesDemand.forEach((e) => {
                                            if (variables[e.id]) {
                                                variables[e.id].forEach((v) => {
                                                    if (v.type === 'Json') e[v.name] = JSON.parse(v.value);
                                                    else e[v.name] = v.value;
                                                });
                                            }
                                        });
                                    }
                                ).catch(e => console.log("error: ", e));
                            });
                            var activeProcessInstances = $scope.processInstancesDemand.filter((e) => {
                                return e.state === 'ACTIVE';
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
                                function (result) {
                                    let tasks = _.groupBy(result.data, 'processInstanceId');
                                    $scope.processInstancesDemand.forEach((e) => {

                                        if (tasks[e.id]) {
                                            e.tasks = tasks[e.id];
                                            e.tasks.forEach((t) => {
                                                if (t.assignee && !$scope.profiles[t.assignee]) {
                                                    $http.get(baseUrl + '/user/' + t.assignee + '/profile').then(
                                                        function (result) {
                                                            $scope.profiles[t.assignee] = result.data;
                                                        }
                                                    ).catch(e => console.log("error: ", e));
                                                }
                                            });
                                        }
                                    });
                                }
                            ).catch(e => console.log("error: ", e));
                        }
                    }
                ).catch(e => console.log("error: ", e));
            };

            $scope.getDemandXlsx = () => {
                if ($scope.xlsxPreparedDemand) {
                    var tbl = document.getElementById('xlsxDemandTable');
                    var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, 'demand-search-result.xlsx');
                } else {
                    getDemandProcessInstances($scope.lastSearchParamsDemand, true);
                    $scope.xlsxPreparedDemand = true;
                }
            };

        };

        if ($rootScope.isProjectAvailable('DemandUAT')) initDemandUAT();

    }]).controller('minioCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state',
        function ($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {

            if ($scope.authUser.id !== 'demo' && !$rootScope.hasGroup('revisionAdmin')) {
                console.log('this is not demo or revision_admin');
                $scope.goHome();
            }
            ;

            var baseUrl = '/camunda/api/engine/engine/default';

            $rootScope.currentPage = {
                name: 'minio'
            };

            $scope.goHome = function (path) {
                $location.path("/");
            };

            $scope.pathToFile = '';
            $scope.selectedFile = undefined;
            $scope.selected = 'scanCopy';
            $scope.foundProcesses = [];
            $scope.scanCopyFileValue = undefined;
            $scope.processTechnicalUpdates = "";
            var businessKey = "businessKey";

            $scope.changeSelected = function (selected) {
                $scope.selected = selected;
            }

            $scope.selectFile = function (el) {
                $scope.$apply(function () {
                    $scope.selectedFile = el.files[0];
                });
            };

            function uploadFileToMinio(file) {
                if ($scope.pathToFile.length > 1) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/admin/put/' + $scope.pathToFile + '/' + file.name,
                        transformResponse: []
                    })
                        .then(function (response) {
                            $http.put(response.data, file, {headers: {'Content-Type': undefined}}).then(
                                function () {
                                    $scope.clearFile();
                                    $scope.touchedFile = false;
                                    $scope.pathToFile = '';
                                    alert(`${file.name} was successfully uploaded`);
                                },
                                function (error) {
                                    console.log(`Could not upload ${file.name} to ${$scope.pathToFile}`);
                                    console.log(error.data);
                                    alert(`Could not upload ${file.name} to ${$scope.pathToFile}`);
                                }
                            );
                        }, function (error) {
                            console.log(error.data);
                            alert('No such file ' + file.name);
                        });
                }
            }

            $scope.clearFile = function () {
                $scope.selectedFile = null;
                $scope.touchedFile = true;
                angular.element(document.querySelector('#attachedFile')).val(null);
            };

            $scope.uploadFile = function () {
                $scope.touchedFile = true;
                $timeout(function () {
                    $scope.$apply(function () {
                        uploadFileToMinio($scope.selectedFile);
                    });
                });
            };

            $scope.uploadCancel = function () {
                $scope.clearFile();
                $scope.touchedFile = false;
                $scope.goHome();
            };

            $scope.searchBusinessKey = function () {
                if ($scope.businessKey && $scope.businessKey !== '') {
                    businessKey = $scope.businessKey;
                }

                $http.post(baseUrl + '/process-instance', {
                    businessKey: businessKey, processDefinitionKey: 'Revision',
                    active: true, activityIdIn: ['intermediate_wait_invoiced']
                }).then(
                    function (result) {
                        if (result.data.length > 0) {
                            $scope.foundProcesses = result.data;
                            var processInstanceIds = [];
                            processInstanceIds.push($scope.foundProcesses[0].id);

                            $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                                processInstanceIdIn: processInstanceIds,
                                variableName: "scanCopyFile"
                            }).then(
                                function (varResult) {
                                    $scope.scanCopyFileValue = JSON.parse(varResult.data[0].value);
                                },
                                function (error) {
                                    console.log(error.data)
                                }
                            );
                            $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                                processInstanceIdIn: processInstanceIds,
                                variableName: "processTechnicalUpdates"
                            }).then(
                                function (varResult) {
                                    $scope.processTechnicalUpdates = varResult.data[0].value;
                                },
                                function (error) {
                                    console.log(error.data)
                                }
                            );
                        }
                    },
                    function (error) {
                        console.log(error.data)
                    }
                );
            }

            $scope.uploadAcceptanceFile = function () {
                $timeout(function () {
                    $scope.$apply(function () {
                        uploadAcceptanceToMinio($scope.selectedFile);
                    });
                });
            };

            $rootScope.logout = function () {
                AuthenticationService.logout().then(function () {
                    $scope.authentication = null;
                });
            }

            function uploadAcceptanceToMinio(file) {
                if ($scope.foundProcesses.length > 0 && $scope.scanCopyFileValue) {
                    var path = $scope.scanCopyFileValue.value.path;
                    path = path.substring(0, path.lastIndexOf('/'));
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/admin/put/' + path + '/' + file.name,
                        transformResponse: []
                    })
                        .then(function (response) {
                            $http.put(response.data, file, {headers: {'Content-Type': undefined}}).then(
                                function () {
                                    $scope.scanCopyFileValue.value.path = path + '/' + file.name;
                                    $scope.scanCopyFileValue.value.name = file.name;

                                    var processTechnicalUpdates = ($scope.processTechnicalUpdates === "") ? $scope.comment : ($scope.processTechnicalUpdates + " " + $scope.comment);

                                    $http.post(baseUrl + '/process-instance/' + $scope.foundProcesses[0].id + '/variables',
                                        {
                                            "modifications":
                                                {
                                                    "scanCopyFile": {
                                                        "value": JSON.stringify($scope.scanCopyFileValue),
                                                        type: 'Json'
                                                    },
                                                    "processTechnicalUpdates": {
                                                        "value": processTechnicalUpdates,
                                                        type: 'String'
                                                    },
                                                }
                                        }
                                    ).then(
                                        function (result) {
                                            toasty.success("Данные успешно сохранены!");
                                            $scope.foundProcesses = [];
                                            $scope.scanCopyFileValue = undefined;
                                            $scope.selectedFile = undefined;
                                            $scope.businessKey = undefined;
                                            businessKey = 'businessKey';
                                            $scope.comment = undefined;
                                            $scope.processTechnicalUpdates = "";
                                            angular.element(document.querySelector('#attachedAcceptanceFile')).val(null);
                                        },
                                        function (error) {
                                            console.log(error.data)
                                            toasty.success(error.data);
                                        }
                                    );
                                },
                                function (error) {
                                    console.log(`Could not upload ${file.name} to ${path}`);
                                    console.log(error.data);
                                    alert(`Could not upload ${file.name} to ${path}`);
                                }
                            );
                        }, function (error) {
                            console.log(error.data);
                            alert('No such file ' + file.name);
                        });
                }
            }

            //----------------------------------------------------------- Change Activity --------------------------------------

            $scope.$watch('activityProcess', function (activityProcess) {
                $scope.userTasksMap = [];
                $scope.processInstanceId = undefined;
                $scope.cancelActivities = {};
                $scope.activityProcessTechnicalUpdates = "";
                $scope.loadProcessDefinitionActivities();
            }, true);

            $http.get(baseUrl + '/process-definition?latest=true&active=true&startableInTasklist=true').then(
                function (results) {
                    $scope.activityProcessDefinitions = [];
//					if(e.name != 'null'){
                    results.data.forEach(function (e) {
                        $scope.activityProcessDefinitions.push(e);
                    });
                    //				}
                },
                function (error) {
                    console.log(error.data);
                }
            );

            $scope.loadProcessDefinitionActivities = function () {
                if ($scope.activityProcess) {
                    $http.get(baseUrl + '/process-definition/key/' + $scope.activityProcess + '/xml')
                        .then(function (response) {
                            var domParser = new DOMParser();

                            var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

                            function getUserTasks(xml) {
                                var namespaces = {
                                    bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
                                };

                                var userTaskNodes = [
                                    ...getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]),
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

                            var userTasks = getUserTasks(xml);
                            $scope.userTasksMap = userTasks;
                        });
                }
            }

            $scope.searchActivityBusinessKey = function () {
                var activityBusinessKey = 'businessKey';
                if ($scope.activityBusinessKey && $scope.activityBusinessKey !== '') {
                    activityBusinessKey = $scope.activityBusinessKey;
                }

                $http.post(baseUrl + '/process-instance', {
                    businessKey: activityBusinessKey, processDefinitionKey: $scope.activityProcess,
                    active: true
                }).then(
                    function (result) {
                        if (result.data.length > 0) {
                            $scope.foundProcesses = result.data;
                            $scope.processInstanceId = $scope.foundProcesses[0].id;

                            $http.get(baseUrl + '/process-instance/' + $scope.processInstanceId + '/activity-instances').then(
                                function (activityResult) {
                                    $scope.activityProcessActivities = [];
                                    _.forEach(activityResult.data.childActivityInstances, function (firstLevel) {
                                        if (firstLevel.activityType === 'subProcess') {
                                            _.forEach(firstLevel.childActivityInstances, function (secondLevel) {
                                                if (secondLevel.activityType == 'multiInstanceBody') {
                                                    secondLevel.activityName = secondLevel.childActivityInstances[0].activityName;
                                                    $scope.activityProcessActivities.push(secondLevel);
                                                } else {
                                                    $scope.activityProcessActivities.push(secondLevel);
                                                }
                                            });
                                        } else if (firstLevel.activityType == 'multiInstanceBody') {
                                            firstLevel.activityName = firstLevel.childActivityInstances[0].activityName;
                                            $scope.activityProcessActivities.push(firstLevel);
                                        } else {
                                            $scope.activityProcessActivities.push(firstLevel);
                                        }
                                    });
                                },
                                function (error) {
                                    console.log(error.data)
                                }
                            );
                            $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                                processInstanceId: $scope.processInstanceId,
                                variableName: "processTechnicalUpdates"
                            }).then(
                                function (varResult) {
                                    $scope.activityProcessTechnicalUpdates = varResult.data[0].value;
                                },
                                function (error) {
                                    console.log(error.data)
                                }
                            );
                        }
                    },
                    function (error) {
                        console.log(error.data)
                    }
                );
            }

            $scope.moveToActivity = function () {
                var instructions = [];
                for (var property in $scope.cancelActivities) {
                    if ($scope.cancelActivities[property]) {
                        instructions.push({type: 'cancel', activityInstanceId: property});
                    }
                }

                if (instructions.length > 0) {
                    instructions.push({type: 'startBeforeActivity', activityId: $scope.activityTask});

                    var modification = {
                        skipCustomListeners: false,
                        skipIoMappings: false,
                        instructions: instructions
                    }
                    console.log(modification);

                    $http.post(baseUrl + '/process-instance/' + $scope.processInstanceId + '/modification', modification).then(
                        function (result) {

                            var activityProcessTechnicalUpdates = ($scope.activityProcessTechnicalUpdates === "") ? $scope.activityComment : ($scope.activityProcessTechnicalUpdates + " " + $scope.activityComment);
                            $http.post(baseUrl + '/process-instance/' + $scope.processInstanceId + '/variables',
                                {
                                    "modifications":
                                        {
                                            "processTechnicalUpdates": {
                                                "value": activityProcessTechnicalUpdates,
                                                type: 'String'
                                            }
                                        }
                                }
                            ).then(
                                function (result) {
                                    $scope.activityComment = undefined;
                                    $scope.processInstanceId = undefined;
                                    $scope.cancelActivities = {};
                                    $scope.activityBusinessKey = undefined;
                                    toasty.success('Process successfully modified');
                                },
                                function (error) {
                                    console.log(error.data)
                                    toasty.success(error.data);
                                }
                            );
                        },
                        function (error) {
                            toasty.error('Process modification error');
                            console.log(error.data);
                        }
                    );
                } else {
                    toasty.error('At least one cancel activity should be selected');
                }
            }
        }]).controller('MassApproveCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', '$q', '$filter', function ($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state, $q, $filter) {
        const baseUrl = '/camunda/api/engine/engine/default';
        const defKey = $stateParams.defKey;
        if (!defKey) return;

        $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name + "/profile").then(function (result) {
            $rootScope.authentication.assigneeName = (result.data.firstName ? result.data.firstName : "") + " " + (result.data.lastName ? result.data.lastName : "");
            $rootScope.authentication.id = result.data.id;
        });

        $scope.$watchGroup(['selectedProject', 'selectedProcess'], function (newValues, oldValues, scope) {
            if ((newValues[0].key !== oldValues[0].key || newValues[1].key !== oldValues[1].key)) {
                if ($scope.selectedProcessKey && !_.some($rootScope.getCurrentProcesses(), function (pd) {
                    return pd.key === $scope.selectedProcessKey
                })) {
                    $scope.selectedProcessKey = undefined;
                    $scope.currentTaskGroup = undefined;
                    $scope.currentFilter = undefined;
                }
                $state.go("tasks", {}, {reload: true});
            }
        }, true);


        $scope.override = function (fields, fieldToOverRide) {
            return fields.filter(f => f.name === fieldToOverRide && f.override).length > 0;
        }

        $scope.getString = function (val) {
            return typeof val !== 'undefined' && val !== null ? (typeof val === 'string' ? val : val.toString()) : undefined;
        }

        $scope.initTextValue = function (val, field) {
            var result = typeof field.value === 'undefined' ? $scope.getString(val) : $scope.getString(field.value);
            if (typeof result !== 'undefined') {
                if (field.prefixValue && result.indexOf(field.prefixValue) === -1) {
                    result = field.prefixValue + result;
                }
                if (field.postfixValue && result.indexOf(field.postfixValue) === -1) {
                    result = result + field.postfixValue;
                }
            }
            return result;
        }

        $scope.tcfChangeHandler = function (tcfId, definition) {
            angular.forEach(definition.tasks, function (instance) {
                if (defKey === 'massApprove_bulkSMS_confirmAndWriteAmdocsTCF') {
                    //instance.identifierAmdocsID = tcfId;
                    instance.amdocsTcfId = tcfId;
                } else if (defKey === 'massApprove_bulkSMS_confirmAndWriteOrgaTCF') {
                    //instance.identifierOrgaID = tcfId;
                    instance.orgaTcfId = tcfId;
                } else if (defKey === 'massApprove_confirmAndWriteAmdocsTCF') {
                    //instance.identifierAmdocsID = tcfId;
                    instance.amdocsTcfId = tcfId;
                } else if (defKey === 'massApprove_confirmAndWriteOrgaTCF') {
                    //instance.identifierOrgaID = tcfId;
                    instance.orgaTcfId = tcfId;
                }
            });
        }

        $scope.commentChangeHandler = function (commentPC, definition) {
            angular.forEach(definition.tasks, function (instance) {
                if (defKey === 'massApprove_bulkSMS_addedShortNumberForAmdocsNew') {
                    instance.comment = commentPC;
                } else if (defKey === 'massApprove_bulkSMS_addedShortNumberForOrgaNew') {
                    instance.comment = commentPC;
                } else if (defKey === 'massApprove_addedShortNumberForAmdocsNew') {
                    instance.comment = commentPC;
                } else if (defKey === 'massApprove_addedShortNumberForOrgaNew') {
                    instance.comment = commentPC;
                }
            });
        }

        $scope.toggleAllTasks = function (definition) {
            // Since toggling has ONLY 2 possible states(input type = "checkbox") - checked and unchecked
            // DO NOT USE THIS FOR RESOLUTIONS THAT HAVE MORE THAN 2 POSSIBLE VARIABLES!
            // Something like "APPROVE" or "REJECT" is the right way
            // FOR CASES when resolutions have > 2 possible values USE: radio button or select option, but not for checkboxes

            definition.allTasksSelected = !definition.allTasksSelected ? true : false;
            if (definition.configs.resolutions && definition.configs.resolutions.length > 0) {
                if (typeof definition.radioSelectedIndex === 'undefined') {
                    definition.radioSelectedIndex = 0;
                    definition.radioSelectedValue = definition.configs.resolutions[definition.radioSelectedIndex].variable;
                    definition.radioSelectedText = definition.configs.resolutions[definition.radioSelectedIndex].text;
                    angular.forEach(definition.tasks, function (instance) {
                        instance.resolution = definition.radioSelectedValue;
                    });
                } else {
                    definition.radioSelectedIndex = undefined;
                    definition.radioSelectedValue = undefined;
                    definition.radioSelectedText = undefined;
                    angular.forEach(definition.tasks, function (instance) {
                        instance.resolution = null;
                    });
                    /*
                    // Use this part if you use something other than checkbox, which has only 2 values(checked and unchecked)
                    if(definition.radioSelectedIndex < definition.configs.resolutions.length-1){
                        console.log(definition.radioSelectedIndex, definition.configs.resolutions.length);
                        definition.radioSelectedIndex++;
                        definition.radioSelectedValue = definition.configs.resolutions[definition.radioSelectedIndex].variable;
                        definition.radioSelectedText = definition.configs.resolutions[definition.radioSelectedIndex].text;
                        angular.forEach(definition.tasks, function(instance){
                            instance.resolution = definition.radioSelectedValue;
                        });
                    } else {
                        console.log(definition.radioSelectedIndex, definition.configs.resolutions.length);
                        definition.radioSelectedIndex = undefined;
                        definition.radioSelectedValue = undefined;
                        definition.radioSelectedText = undefined;
                        angular.forEach(definition.tasks, function(instance){
                            instance.resolution = null;
                        });
                    }
                    */
                }
            }
        }

        $scope.clearResolution = function (definition, instance) {
            definition.allTasksSelected = false;
            instance.resolution = null;
            definition.radioSelectedIndex = undefined;
            definition.radioSelectedValue = undefined;
            definition.radioSelectedText = undefined;
        }

        $scope.changeResolution = function (selectedValue, definition) {
            if (definition.configs.resolutions[0].variable === selectedValue) {
                if (definition.tasks.length === definition.tasks.filter(instance => selectedValue === instance.resolution).length) {
                    definition.allTasksSelected = true;
                    definition.radioSelectedIndex = 0;
                    definition.radioSelectedValue = definition.configs.resolutions[definition.radioSelectedIndex].variable;
                    definition.radioSelectedText = definition.configs.resolutions[definition.radioSelectedIndex].text;
                }
            } else {
                definition.allTasksSelected = false;
                definition.radioSelectedIndex = undefined;
                definition.radioSelectedValue = undefined;
                definition.radioSelectedText = undefined;
            }
        }

        $scope.onSelect = function (field, instance) {
            if (field.name === "inAndOut") {
                if (field.dependants && field.dependants.length > 0 && $scope.taskData[instance.taskId][field.name]) {
                    field.dependants.forEach(function (dependant) {
                        if ((dependant.fieldName === "identifierServiceName_amdocs" && instance[dependant.fieldName]) || (dependant.fieldName === "identifierServiceName_orga" && instance[dependant.fieldName])) {
                            if (!$scope.taskData[instance.taskId][dependant.fieldName]) {
                                $scope.taskData[instance.taskId][dependant.fieldName] = instance[dependant.fieldName];
                            }
                            instance[dependant.fieldName] = $scope.taskData[instance.taskId][field.name] + ' ' + $scope.taskData[instance.taskId][dependant.fieldName].replace("Incoming ", "").replace("Outgoing ", "");
                        } else if ((dependant.fieldName === "abonentTarif_amdocs" && instance[dependant.fieldName]) || (dependant.fieldName === "abonentTarif_orga" && instance[dependant.fieldName])) {
                            if (!$scope.taskData[instance.taskId][dependant.fieldName]) {
                                $scope.taskData[instance.taskId][dependant.fieldName] = instance[dependant.fieldName];
                            }
                            if ($scope.taskData[instance.taskId][field.name] === "Incoming") {
                                instance[dependant.fieldName] = '0';
                            }
                            if ($scope.taskData[instance.taskId][field.name] === "Outgoing") {
                                instance[dependant.fieldName] = $scope.taskData[instance.taskId][dependant.fieldName];
                            }
                        }
                    })
                }
            }
            /*if(field.name === "comment"){}*/
        };

        $http.post(baseUrl + "/task", {taskDefinitionKey: defKey, active: true}).then(function (response) {
            if (response.data && response.data.length) {
                $scope.definitions = [];
                $scope.taskData = {};

                const groupedByDefs = _.groupBy(response.data, "processDefinitionId");
                _.forOwn(groupedByDefs, function (val, key) {
                    if (!val.length) return;
                    var definition = {
                        id: key,
                        name: val[0].name,
                        instances: [],
                        tasks: [],
                        configs: {},
                        allTasksSelected: false
                    };
                    _.each(val, function (v) {
                        definition.instances.push({
                            id: v.processInstanceId,
                            taskId: v.id
                        });
                        definition.tasks.push({
                            pid: v.processInstanceId,
                            taskId: v.id
                        });
                        $scope.taskData[v.id] = {};
                    });
                    $scope.definitions.push(definition);
                });

                var pids = _.flatMap(response.data, function (v) {
                    return v.processInstanceId;
                });
                $http({
                    method: 'POST',
                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                    data: {processInstanceIds: pids},
                    url: baseUrl + '/history/process-instance'
                }).then(
                    function (processes) {
                        angular.forEach($scope.definitions, function (d) {
                            angular.forEach(d.tasks, function (t) {
                                var process = _.find(processes.data, function (p) {
                                    return p.id === t.pid
                                });
                                t.processBusinessKey = process.businessKey;
                            });
                        });
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );

                var tids = _.flatMap(response.data, function (v) {
                    return v.id;
                });
                var counter = 0;
                $scope.closeDate = new Date();
                $scope.tcfId = "";
                $scope.commentPC = "";

                tids.forEach(function (tid) {
                    $http.get(baseUrl + "/task/" + tid + "/form-variables?deserializeValues=false").then(function (response) {
                        var procFields = [];
                        var fields = {};
                        _.forOwn(response.data, function (value, key) {
                            if (value.type === 'Json' || key === 'massApproveConfigs') {
                                fields[key] = JSON.parse(value.value);
                            } else fields[key] = value.value;
                        });
                        for (var i = 0; i < $scope.definitions.length; i++) {
                            var found = false;
                            for (var j = 0; j < $scope.definitions[i].tasks.length; j++) {

                                if ($scope.definitions[i].tasks[j].taskId === tid) {
                                    $scope.definitions[i].configs = fields.massApproveConfigs;
                                    delete (fields.massApproveConfigs);
                                    $scope.definitions[i].tasks[j] = _.assign($scope.definitions[i].tasks[j], fields);
                                }
                            }
                            if (found) break;
                        }
                        counter++;
                        if (counter === tids.length) {
                            /*
                            console.log('definitions', $scope.definitions);
                            var defMap = {};
                            var noDuplicateDefs = [];
                            Object.assign(noDuplicateDefs, $scope.definitions);

                            console.log('noDuplicateDefs', noDuplicateDefs.length, noDuplicateDefs);

                            for(var i=0; i<noDuplicateDefs.length; i++){
                                console.log('i = ' + i);
                                if (noDuplicateDefs[i]) {

                                    console.log('length = ' + noDuplicateDefs.length, 'i = ' + i, noDuplicateDefs[i]);

                                    defMap[noDuplicateDefs[i].id] = [];

                                    for(var j=i+1; j<noDuplicateDefs.length; j++) {

                                        console.log('j = ' + j);

                                        //if(noDuplicateDefs[i].configs.table.rows){

                                            console.log('rows = ' + noDuplicateDefs[i].configs.table.rows.length);

                                            for(var k=0; k<noDuplicateDefs[i].configs.table.rows.length; k++){
                                                var fieldsA = JSON.parse(JSON.stringify(noDuplicateDefs[i].configs.table.rows[k].fields));
                                                var fieldsB = JSON.parse(JSON.stringify(noDuplicateDefs[j].configs.table.rows[k].fields));
                                                var headersA = noDuplicateDefs[i].configs.table.headers;
                                                var headersB = noDuplicateDefs[j].configs.table.headers;
                                                if( _.isEqual(fieldsA, fieldsB)
                                                    && headersA.length === headersB.length && headersA.every((value, index) => value === headersB[index])
                                                ) {
                                                    console.log('equal', fieldsA, fieldsB);
                                                    defMap[noDuplicateDefs[i].id].push(noDuplicateDefs[j].id);
                                                    //noDuplicateDefs.splice(j, 1);
                                                    noDuplicateDefs[j]=undefined;
                                                }
                                            }
                                        //}
                                    }
                                }
                            }

                            var arr = []

                            console.log('defMap', defMap);

                            Object.keys(defMap).forEach(defId=>{
                                console.log('defId', defId);
                                var defObj = {}
                                $scope.definitions.forEach(def=>{
                                    if(def.id === defId){
                                        Object.assign(defObj, def);
                                        if(defMap[defId].length>0){
                                            console.log('defMap[defId].length',defMap[defId].length);
                                            defMap[defId].forEach(sourceDefId=>{
                                                console.log('sourceDefId', sourceDefId);
                                                $scope.definitions.forEach(targetDef=>{
                                                    if(sourceDefId === targetDef.id){
                                                        console.log('targetDef', targetDef);
                                                        defObj.tasks = defObj.tasks.concat(targetDef.tasks);
                                                        defObj.instances = defObj.instances.concat(targetDef.instances);
                                                    }
                                                })
                                            })
                                        }
                                    }
                                })
                                arr.push(defObj);
                            });

                            console.log('$scope.definitions', $scope.definitions);
                            console.log('arr', arr);
                            console.log('defMap', defMap);
                            $scope.definitions = arr;
                            */
                            $scope.allVariablesAssign = true;
                        }
                    });
                });
            }
        });

        var preSubmitTasks = {
            TCFPost: {
                tasks: ["massApprove_checkFormAmdocsTCF", "massApprove_checkFormOrgaTCF", "massApprove_bulkSMS_checkFormAmdocsTCF", "massApprove_bulkSMS_checkFormOrgaTCF"],
                fire: function (reqBodyTCF) {
                    console.log('request Body TCF', reqBodyTCF);
                    return $http.post("/camunda/sharepoint/contextinfo", {}).then(
                        function (response) {
                            console.log('response Body TCF', response);
                            if (response && response.data) {
                                var contextinfo = response.data.d.GetContextWebInformation.FormDigestValue;
                                reqBodyTCF["FormDigestValue"] = contextinfo;
                                console.log('contextinfo', contextinfo);
                                return $http.post("/camunda/sharepoint/items", reqBodyTCF);
                            }
                        }
                    );
                }
            }
        };

        $scope.submitThem = function () {
            var tasks2submit = [];
            for (var i = 0; i < $scope.definitions.length; i++) {
                const definition = $scope.definitions[i];
                var mandatoryFields = [];
                angular.forEach(definition.configs.table.rows, function (row) {
                    Object.assign(mandatoryFields, row.fields.filter(function (field) {
                        return field.notNull
                    }));
                });

                if (preSubmitTasks['TCFPost'].tasks.indexOf(defKey) > -1) {
                    var processKey = definition.id.substring(0, definition.id.indexOf(':'));
                    console.log('processKey', processKey);
                    var billingTCF = "";
                    var headerBillingName = "";
                    var headerBillingId = "";
                    var ServiceNameRUS = "";
                    var ServiceNameENG = "";
                    var ServiceNameKAZ = "";
                    var commentsTCF = "";
                    var countApproved = 0;
                    var htmlTemplateRow = "";
                    var htmlTemplateHeader = "";
                    var htmlTemplateFooter = '</tbody></table><p><br></p></div>';

                    var requestBodyJSON = {};
                    var metadataBodyJSON = {type: "SP.Data.ICTD_x0020_TCFListItem"};
                    var operatorBodyJSON = {};
                    var billingTypeBodyJSON = {};
                    var operatorResultsJSONArray = [];
                    var billingTypeResultsJSONArray = [];

                    requestBodyJSON["__metadata"] = metadataBodyJSON;

                    if (["massApprove_checkFormAmdocsTCF", "massApprove_bulkSMS_checkFormAmdocsTCF"].indexOf(defKey) > -1) {
                        billingTCF = "amdocs";
                    } else if (["massApprove_checkFormOrgaTCF", "massApprove_bulkSMS_checkFormOrgaTCF"].indexOf(defKey) > -1) {
                        billingTCF = "orga";
                    }

                    if (processKey === "bulksmsConnectionKAE") {
                        ServiceNameRUS = "Bulk SMS";
                        ServiceNameENG = "Bulk SMS";
                        ServiceNameKAZ = "Bulk SMS";
                    }

                    if (processKey === "freephone") {
                        ServiceNameRUS = "Free Phone";
                        ServiceNameENG = "Free Phone";
                        ServiceNameKAZ = "Free Phone";
                    }

                    if (billingTCF === "amdocs") {
                        //commentsTCF = "Проверить корректность заполнения TCF Amdocs";
                        headerBillingName = "Amdocs";
                        headerBillingId = "Amdocs ID";
                        operatorResultsJSONArray.push("Kcell");
                        billingTypeResultsJSONArray.push("Amdocs");
                        operatorBodyJSON["results"] = operatorResultsJSONArray;
                        billingTypeBodyJSON["results"] = billingTypeResultsJSONArray;
                        requestBodyJSON["Operator"] = operatorBodyJSON;
                        requestBodyJSON["BillingType"] = billingTypeBodyJSON;
                    }

                    if (billingTCF === "orga") {
                        //commentsTCF = "Проверить корректность заполнения TCF Orga";
                        headerBillingName = "Orga";
                        headerBillingId = "Orga ID";
                        operatorResultsJSONArray.push("Activ");
                        billingTypeResultsJSONArray.push("Orga");
                        operatorBodyJSON["results"] = operatorResultsJSONArray;
                        billingTypeBodyJSON["results"] = billingTypeResultsJSONArray;
                        requestBodyJSON["Operator"] = operatorBodyJSON;
                        requestBodyJSON["BillingType"] = billingTypeBodyJSON;
                    }

                    htmlTemplateHeader = '<div>' +
                        '<p></p>' +
                        '<table border="0" cellpadding="0" cellspacing="0" style="border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;width:500px;">' +
                        '<tbody>' +
                        '<tr>' +
                        '<td rowspan="2" style="border: 1px dotted #d3d3d3;height: 54px; width: 120px">Service Name</td>' +
                        '<td rowspan="2" style="border: 1px dotted #d3d3d3; width: 60px">Short Number</td>' +
                        '<td colspan="3" style="border: 1px dotted #d3d3d3; width: 220px">' + headerBillingName + '</td>' +
                        '<td rowspan="2" style="border: 1px dotted #d3d3d3; width: 100px">Comments for ICTD</td>' +
                        '</tr>' +
                        '<tr>' +
                        '<td style="border: 1px dotted #d3d3d3; width: 66px">Counter</td>' +
                        '<td style="border: 1px dotted #d3d3d3; width: 66px">Price per counter</td>' +
                        '<td style="border: 1px dotted #d3d3d3;">' + headerBillingId + '</td>' +
                        '</tr>';

                    requestBodyJSON["InitiatorDepartment"] = "B2B";
                    requestBodyJSON["Subject"] = "B2B Short Numbers";
                    //requestBodyJSON["DateDeadline"] = tcfDateValue;
                    requestBodyJSON["Service"] = "Products / Tariffs";
                    requestBodyJSON["RelationWithThirdParty"] = false;
                    requestBodyJSON["TypeForm"] = "Добавление тарифа на новый сервис (New service TCF)";
                    //requestBodyJSON["Comments"] = commentsTCF;
                    requestBodyJSON["ServiceNameRUS"] = ServiceNameRUS;
                    requestBodyJSON["ServiceNameENG"] = ServiceNameENG;
                    requestBodyJSON["ServiceNameKAZ"] = ServiceNameKAZ;
                    requestBodyJSON["DepartmentManagerId"] = {results: [263]};
                    requestBodyJSON["Status"] = "Approved by Department Manager";
                    requestBodyJSON["Created"] = $filter('date')(new Date(), 'yyyy-MM-ddTHH:mm:ss');
                }

                for (var j = 0; j < definition.tasks.length; j++) {
                    const instance = $scope.definitions[i].tasks[j];
                    if (!instance.resolution) continue;
                    if (preSubmitTasks['TCFPost'].tasks.indexOf(defKey) > -1) {
                        if (instance.starter) {
                            if (instance.starter === "Nazym.Muralimova@kcell.kz") {
                                requestBodyJSON["InitiatorId"] = "3034";
                            } else if (instance.starter === "Sagida.Adiyeva@kcell.kz" || instance.starter === "demo") {
                                requestBodyJSON["InitiatorId"] = "987";
                            }
                        }
                    }

                    var emptyFields = mandatoryFields.filter(function (field) {
                        return instance[field.name] ? false : $scope.taskData[instance.taskId][field.name] ? false : true;
                    });

                    if (!(definition.configs.comment ? definition.configs.comment.overrideRowComment : false) && !instance.comment) {
                        emptyFields.push({name: "comment", notNull: true, readOnly: false, save: true, type: "text"});
                    }
                    ;

                    //console.log(emptyFields, mandatoryFields);
                    if (emptyFields.length > 0) continue;

                    // Insert resolution
                    var resName = defKey + "TaskResult";
                    var resValue = "";
                    if (definition.configs.resolutionVariable && definition.configs.resolutionVariable.length) {
                        resName = definition.configs.resolutionVariable;
                    }
                    if (instance.resolution && instance.resolution.length) {
                        resValue = instance.resolution;
                    }
                    //console.log(baseUrl+"/task/"+instance.taskId+"/variables/"+resName, resValue);

                    let variables = {};
                    variables[resName] = {value: resValue, type: "String"};

                    var comName = defKey + "TaskComment";
                    var comValue = "";
                    if (definition.configs.commentVariable && definition.configs.commentVariable.length) {
                        comName = definition.configs.commentVariable;
                    }
                    if (instance.comment && instance.comment.length) {
                        comValue = instance.comment;
                    } else {
                        comValue = $scope.taskData[instance.taskId]['comment'];
                    }

                    variables[comName] = {
                        value: comValue,
                        type: "String"
                    };

                    //definition.configs.table.fields.filter(field => !field.override && (!field.readOnly || field.save)).forEach(field=>{
                    //    variables[field.name] = {
                    //        value: instance[field.name],
                    //        type: "String"
                    //    };
                    //});

                    angular.forEach(definition.configs.table.rows, function (row) {
                        //Object.assign(variables, row.filter(function(field){return field.notNull}));
                        row.fields.filter(field => !field.override && (!field.readOnly || field.save)).forEach(field => {
                            variables[field.name] = {
                                value: instance[field.name],
                                type: "String"
                            };
                        });
                    });
                    if (definition.configs.showTCFID) {
                        if (instance.amdocsTcfId) {
                            variables['amdocsTcfId'] = {
                                value: instance.amdocsTcfId,
                                type: "String"
                            };
                        }
                        if (instance.orgaTcfId) {
                            variables['orgaTcfId'] = {
                                value: instance.orgaTcfId,
                                type: "String"
                            };
                        }
                    }
                    var dateName = defKey + "TaskCloseDate";
                    variables[dateName] = {
                        value: $scope.closeDate || new Date(),
                        type: "Date"
                    };

                    if (preSubmitTasks['TCFPost'].tasks.indexOf(defKey) > -1) {
                        var taskResolutionResult = "";
                        var tcfDateValue = "";
                        var commentValue = "";

                        var serviceNameOutgoingValue = "";
                        var serviceNameIncomingValue = "";
                        var pricePerCounterOutgoingValue = "";
                        var pricePerCounterIncomingValue = "";

                        if (processKey === "bulksmsConnectionKAE") {
                            if (billingTCF === "amdocs") {
                                taskResolutionResult = variables["massApprove_bulkSMS_checkFormAmdocsTCFTaskResult"].value;
                                commentValue = instance["massApprove_bulkSMS_confirmAmdocsTCFTaskComment"];

                                var closeDate = instance["massApprove_bulkSMS_confirmAmdocsTCFTaskCloseDate"];
                                tcfDateValue = $filter('date')(closeDate, 'yyyy-MM-ddTHH:mm:ss');

                                serviceNameOutgoingValue = instance["identifierServiceName_amdocs_outgoing"];
                                serviceNameIncomingValue = instance["identifierServiceName_amdocs_incoming"];
                                pricePerCounterOutgoingValue = instance["abonentTarif_amdocs_outgoing"];
                                pricePerCounterIncomingValue = instance["abonentTarif_amdocs_incoming"];
                                if (!pricePerCounterOutgoingValue || pricePerCounterOutgoingValue === null || pricePerCounterOutgoingValue === "") {
                                    pricePerCounterOutgoingValue = 0;
                                }
                                if (!pricePerCounterIncomingValue || pricePerCounterIncomingValue === null || pricePerCounterIncomingValue === "") {
                                    pricePerCounterIncomingValue = 0;
                                }
                            }
                            if (billingTCF === "orga") {
                                taskResolutionResult = variables["massApprove_bulkSMS_checkFormOrgaTCFTaskResult"].value;
                                commentValue = instance["massApprove_bulkSMS_confirmOrgaTCFTaskComment"];

                                var closeDate = instance["massApprove_bulkSMS_confirmOrgaTCFTaskCloseDate"];
                                tcfDateValue = $filter('date')(closeDate, 'yyyy-MM-ddTHH:mm:ss');

                                serviceNameOutgoingValue = instance["identifierServiceName_orga_outgoing"];
                                serviceNameIncomingValue = instance["identifierServiceName_orga_incoming"];
                                pricePerCounterOutgoingValue = instance["abonentTarif_orga_outgoing"];
                                pricePerCounterIncomingValue = instance["abonentTarif_orga_incoming"];
                                if (!pricePerCounterOutgoingValue || pricePerCounterOutgoingValue === null || pricePerCounterOutgoingValue === "") {
                                    pricePerCounterOutgoingValue = 0;
                                }
                                if (!pricePerCounterIncomingValue || pricePerCounterIncomingValue === null || pricePerCounterIncomingValue === "") {
                                    pricePerCounterIncomingValue = 0;
                                }
                            }
                        }
                        if (processKey === "freephone") {
                            if (billingTCF === "amdocs") {
                                taskResolutionResult = variables["massApprove_checkFormAmdocsTCFTaskResult"].value;
                                commentValue = instance["massApprove_confirmAmdocsTCFTaskComment"];

                                var closeDate = instance["massApprove_confirmAmdocsTCFTaskCloseDate"];
                                tcfDateValue = $filter('date')(closeDate, 'yyyy-MM-ddTHH:mm:ss');

                                serviceNameOutgoingValue = instance["identifierServiceName_amdocs_outgoing"];
                                pricePerCounterOutgoingValue = instance["abonentTarif_amdocs_outgoing"];
                            }
                            if (billingTCF === "orga") {
                                taskResolutionResult = variables["massApprove_checkFormOrgaTCFTaskResult"].value;
                                commentValue = instance["massApprove_confirmOrgaTCFTaskComment"];

                                var closeDate = instance["massApprove_confirmOrgaTCFTaskCloseDate"];
                                tcfDateValue = $filter('date')(closeDate, 'yyyy-MM-ddTHH:mm:ss');

                                serviceNameOutgoingValue = instance["identifierServiceName_orga_outgoing"];
                                pricePerCounterOutgoingValue = instance["abonentTarif_orga_outgoing"];
                            }
                        }

                        console.log('commentValue', commentValue);
                        console.log('serviceNameOutgoingValue', serviceNameOutgoingValue);
                        console.log('serviceNameIncomingValue', serviceNameIncomingValue);
                        console.log('pricePerCounterOutgoingValue', pricePerCounterOutgoingValue);
                        console.log('pricePerCounterIncomingValue', pricePerCounterIncomingValue);

                        if (taskResolutionResult !== "rejected") {
                            countApproved++;
                            var shortNumberValue = $scope.massTableField(instance, "identifier:title");
                            var counterValue = instance["identifierCounter"];

                            requestBodyJSON["DateDeadline"] = tcfDateValue;
                            if (processKey === "freephone") {
                                htmlTemplateRow = htmlTemplateRow.concat("<tr>" +
                                    '<td style="border: 1px dotted #d3d3d3;">' + serviceNameOutgoingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + shortNumberValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + counterValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + pricePerCounterOutgoingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;"></td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + commentValue + '</td>' +
                                    '</tr>'
                                );
                            } else if (processKey === "bulksmsConnectionKAE") {
                                htmlTemplateRow = htmlTemplateRow.concat("<tr>" +
                                    '<td style="border: 1px dotted #d3d3d3;">' + serviceNameOutgoingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + shortNumberValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + counterValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + pricePerCounterOutgoingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;"></td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + commentValue + '</td>' +
                                    '</tr><tr>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + serviceNameIncomingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + shortNumberValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + counterValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + pricePerCounterIncomingValue + '</td>' +
                                    '<td style="border: 1px dotted #d3d3d3;"></td>' +
                                    '<td style="border: 1px dotted #d3d3d3;">' + commentValue + '</td>' +
                                    '</tr>'
                                );
                            }
                        }
                    }

                    tasks2submit.push(function (formId, status, isReceived) {
                        return $http.post(baseUrl + "/task/" + instance.taskId + "/claim", {userId: $rootScope.authentication.id}).then(function () {
                            console.log(111, formId, status, isReceived);
                            if (formId) {
                                variables[formId.name] = {
                                    value: formId.value,
                                    type: formId.type
                                };
                            }

                            if (status) {
                                variables[status.name] = {
                                    value: status.value,
                                    type: status.type
                                };
                            }

                            if (isReceived) {
                                variables[isReceived.name] = {
                                    value: isReceived.value,
                                    type: isReceived.type
                                };
                            }

                            return $http.post(baseUrl + "/task/" + instance.taskId + "/submit-form", {variables: variables});
                        })
                    });
                }
                ;

                if (preSubmitTasks['TCFPost'].tasks.indexOf(defKey) > -1) {
                    console.log('countApproved', countApproved);
                    if (countApproved > 0) {
                        var htmlTemplateTCF = htmlTemplateHeader + htmlTemplateRow + htmlTemplateFooter;
                        requestBodyJSON["Requirments"] = htmlTemplateTCF;

                        //console.log(requestBodyJSON);
                        console.log(JSON.stringify(requestBodyJSON));

                        preSubmitTasks['TCFPost'].fire(requestBodyJSON).then(
                            function (response) {
                                console.log('fire response', response.data.d);
                                if (response && response.data) {

                                    console.log('Id', response.data.d.Id);
                                    if (response.data.d && response.data.d.Id) {
                                        var responseData = angular.copy(response.data.d);
                                        var formId = {};
                                        var status = {};
                                        var isReceived = {};

                                        if (billingTCF === "amdocs") {
                                            status.name = "amdocsTcfFormStatus";
                                            status.type = "String";
                                            status.value = responseData.Status;
                                            //if(status.value.indexOf("Approved") > -1){
                                            if (responseData.Id !== undefined && responseData.Id !== null) {
                                                formId.name = "amdocsTcfFormId";
                                                formId.type = "String";
                                                formId.value = responseData.Id;
                                                isReceived.name = "amdocsTcfFormIdReceived";
                                                isReceived.type = "Boolean";
                                                isReceived.value = true;
                                            } else {
                                                isReceived.name = "amdocsTcfFormIdReceived";
                                                isReceived.type = "Boolean";
                                                isReceived.value = false;
                                            }
                                        }
                                        if (billingTCF === "orga") {
                                            status.name = "orgaTcfFormStatus";
                                            status.type = "String";
                                            status.value = responseData.Status;
                                            console.log('status', status.value);
                                            //if(status.value.indexOf("Approved") > -1){
                                            if (responseData.Id !== undefined && responseData.Id !== null) {
                                                formId.name = "orgaTcfFormId";
                                                formId.type = "String";
                                                formId.value = responseData.Id;
                                                isReceived.name = "orgaTcfFormIdReceived";
                                                isReceived.type = "Boolean";
                                                isReceived.value = true;
                                            } else {
                                                isReceived.name = "orgaTcfFormIdReceived";
                                                isReceived.type = "Boolean";
                                                isReceived.value = false;
                                            }
                                        }

                                        $q.all(tasks2submit.map(
                                            function (taskListener) {
                                                console.log('tasks2submit listener', formId, status, isReceived);
                                                return taskListener(formId, status, isReceived)
                                            })).then(
                                            function () {
                                                $state.go("tasks", {}, {reload: true});
                                            }
                                        );
                                    }
                                }
                            }
                        );
                    } else {
                        $q.all(tasks2submit.map(
                            function (taskListener) {
                                return taskListener()
                            })).then(function () {
                            $state.go("tasks", {}, {reload: true});
                        });
                    }
                } else {
                    $q.all(tasks2submit.map(
                        function (taskListener) {
                            return taskListener()
                        })).then(function () {
                        $state.go("tasks", {}, {reload: true});
                    });
                }
            }
        }

        $scope.massTableField = function (instance, f) {
            //console.log(f, instance);
            if (f.indexOf(':') !== -1) {
                var result = undefined;
                while (f.indexOf(':') !== -1) {
                    var property = f.substring(0, f.indexOf(':'));
                    f = f.substring(f.indexOf(':') + 1, f.length);
                    if (instance.hasOwnProperty(property)) result = instance[property];
                }
                if (result && result.hasOwnProperty(f)) {
                    return result[f];
                }
            } else {
                return instance[f];
            }
        }
    }]);
});