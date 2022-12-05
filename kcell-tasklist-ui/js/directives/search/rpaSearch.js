define(['../module'], function(module){
    'use strict';
    module.directive('rpaSearch', ['$http', '$timeout', '$q', '$rootScope', 'exModal', 'toasty', function ($http, $timeout, $q, $rootScope, exModal, toasty) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                var baseUrl = '/camunda/api/engine/engine/default';

                var processList = [];
                $http.get('/camunda/api/engine/engine/default/process-definition?latest=true').then(
                    function (response) {
                        angular.forEach(response.data, function (value) {
                            processList.push(value.key);
                        });
                    }
                );
                var allKWMSProcesses = {
                    SSU: {
                        title: "SSU", value: false
                    }
                };

                scope.currentDate = new Date();
                scope.filter.beginYear = scope.currentDate.getFullYear() - 1;
                scope.filter.endYear = scope.currentDate.getFullYear();
                scope.years = [];
                var currentDate = new Date();
                for (var year = 2020; year <= scope.filter.endYear; year++) {
                    scope.years.push(year);
                }

                scope.KWMSProcesses = {};
            
                scope.processInstancesTotal = 0;
                scope.processInstancesPages = 0;
                scope.selectedProcessInstances = [];
                scope.shownPages = 0;                
                scope.onlyProcessActive = '';

                scope.taskUserSelected = function ($item, $model, $label) {
                    //scope.initiator = $item;
                    //scope.initiatorId = $item.id;
                };
                var KWMSproject = _.find($rootScope.projects, {'key': 'RPA'});
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
                    if (Object.keys(filtered).length === 1) {
                        scope.onlyProcessActive = Object.keys(filtered)[0];
                    }
                }

                scope.$watch('KWMSProcesses', function (newVal, oldVal) {
                    scope.selectedProcessInstances = [];
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
                        angular.forEach(filtered, function (process, key) {
                            scope.selectedProcessInstances.push(key);
                        });
                    }
                    scope.filter.businessKeyFilterType = 'all';
                    scope.filter.businessKey = undefined;
                    scope.filter.requestedDateRange = undefined;
                    scope.filter.requestor = undefined;
                }, true);

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

                scope.titles = {};
                scope.populateSelectFromCatalog = function (containers, id) {
                    $http.get('/camunda/catalogs/api/get/id/' + id).then(function (result) {
                        if (result && result.data && result.data.data && result.data.data.$list) {
                            angular.forEach(containers, function (container) {
                                scope[container] = result.data.data.$list;
                            });
                            angular.forEach(result.data.data.$list, function (item) {
                                if (!scope.titles[containers[0]]) {
                                    scope.titles[containers[0]] = {};
                                }
                                scope.titles[containers[0]][item.id] = item.value;
                            });
                        }


                    });
                };
                scope.populateSelectFromCatalog(['subtypeList'], 123);
                scope.populateSelectFromCatalog(['genderList'], 127);
                scope.populateSelectFromCatalog(['regionList'], 126);
                scope.populateSelectFromCatalog(['templateList'], 125);
                scope.populateSelectFromCatalog(['formatList'], 124);

                scope.search = function (refreshPages) {
                    var asynCall1 = false;
                    var asynCall2 = false;
                    var asynCall3 = false;
                    var asynCall4 = false;
                    if (refreshPages) {
                        scope.filter.page = 1;
                        scope.piIndex = undefined;
                    }
                    var selectedProcessInstances = [];
                    angular.forEach(scope.selectedProcessInstances, function (item) {
                        selectedProcessInstances.push(item);
                    });

                    var excludeProcesses = processList.filter(function (item) {
                        return selectedProcessInstances.indexOf(item) === -1;
                    });

                    var filter = {
                        sorting: [{sortBy: "startTime", sortOrder: "desc"}],
                        variables: [],
                        activeActivityIdIn: [],
                        processInstanceBusinessKeyLike: '%_%',
                        processDefinitionKeyNotIn: excludeProcesses

                    }
                    if (scope.filter.unfinished) {
                        filter.unfinished = true;
                    }
                    if (scope.filter.finished) {
                        filter.finished = true;
                    }
                    if (scope.filter.businessKey) {
                        if (scope.filter.businessKeyFilterType === 'eq') {
                            filter.processInstanceBusinessKey = scope.filter.businessKey;
                        } else {
                            filter.processInstanceBusinessKeyLike = '%'+scope.filter.businessKey+'%';
                        }
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
                                    scope.lastSearchParamsTnu = filter;
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
                    if (scope.filter.participation && (scope.onlyProcessActive==='Revision' || scope.onlyProcessActive==='CreatePR' || scope.onlyProcessActive==='create-new-tsd' || scope.onlyProcessActive==='change-tsd' || scope.onlyProcessActive==='tsd-processing' || scope.onlyProcessActive==='cancel-tsd')) {
                        if(!scope.filter.requestor){
                            toasty.error({title: "Error", msg: 'Please fill field Requestor!'});
                            return;
                        }

                        if (['participant','iamparticipant'].indexOf(scope.filter.participation) !== -1) {
                            $http.post(baseUrl + '/history/task', {taskAssignee: scope.filter.requestor}).then(
                                function (result) {
                                    if (!filter.processInstanceIds) filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                                    else filter.processInstanceIds = filter.processInstanceIds.filter(value => -1 !== _.map(result.data, 'processInstanceId').indexOf(value));
                                    asynCall4 = true;
                                    if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
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
                                scope.lastSearchParamsTnu = filter;
                                asynCall2 = true;
                                if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
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
                    asynCall3 = true;
                    if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                        getProcessInstances(filter, 'processInstances');
                        asynCall3 = false;
                    }
                };

                scope.clearFilters = function () {
                    scope.filter.businessKey = undefined;
                    scope.filter.participation = undefined;
                    scope.filter.currentAssignee = undefined;
                    scope.filter.beginYear = scope.currentDate.getFullYear()-1;
                    scope.filter.endYear = scope.currentDate.getFullYear();
                    
                    scope.filter.requestor = undefined;
                    scope.filter.businessKeyFilterType = 'eq';
                    scope.filter.unfinished = false;
                    scope.filter.finished = false;
                }

                function getProcessInstances(filter, processInstances) {
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
                            console.log(result)
                            scope[processInstances] = result.data;
                            var variables = [];
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
                                        console.log("1111====")
                                        console.log(tasks)
                                        angular.forEach(scope[processInstances], function (el) {
                                            console.log("123====")
                                            console.log(scope[processInstances])
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
                scope.putCurrentUserAsRequestor = function(){
                    if(scope.filter.participation === 'iaminitiator' || scope.filter.participation === 'iamparticipant'){
                        scope.filter.requestor = $rootScope.authentication.name;
                    }
                }
                scope.populateSelectFromCatalogId = function (container, id) {
                    $http.get('/camunda/catalogs/api/get/id/' + id).then(function (result) {
                        if (result && result.data) {
                            scope[container] = _.groupBy(result.data.data.$list, 'id');
                        }
                    });
                }
                scope.showProcessInfo = function (index, processInstanceId, businessKey, processDefinitionKey, userId, startDate) {
                    $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                        processDefinitionKey: processDefinitionKey,
                        processInstanceId: processInstanceId,
                    }).then(async function (result) {
                            scope.businessKey = businessKey;
                            scope.userFullName = null;
                            scope.startDate = startDate
                            await $http.get('/camunda/api/engine/engine/default/user/' + userId + '/profile').then(
                                function (result) {
                                    scope.userFullName = result.data.firstName + ' ' + result.data.lastName;
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                            var vars = {};
                            result.data.forEach(function (v) {
                                vars[v.name] = v.value;
                            })
                            scope.processVars = Object.assign({}, vars)
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
                            vars: scope.processVars,
                            attached: JSON.parse(scope.processVars.files),
                            businessKey: scope.businessKey,
                            startDate: scope.startDate,
                            fullName: scope.userFullName,
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

                scope.populateSelectFromCatalogId('construction_type_id',14);
                scope.populateSelectFromCatalogId('antenna_diameter_id',20);
                scope.populateSelectFromCatalogId('protection_mode_id',44);
                scope.populateSelectFromCatalogId('capacity',45);
                scope.populateSelectFromCatalogId('rau_subband',46);
                scope.populateSelectFromCatalogId('link_type_id',47);
                scope.populateSelectFromCatalogId('polarization_id',48);
                scope.populateSelectFromCatalogId('region',5);
            },
            templateUrl: './js/directives/search/rpaSearch.html'
        };
    }]);
});