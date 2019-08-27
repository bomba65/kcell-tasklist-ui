define(['../module', 'moment'], function (module, moment) {
    'use strict';
    module.directive('deliveryPortalSearch', ['$http', '$rootScope', '$timeout', '$q', 'exModal', 'SearchCurrentSelectedProcessService',
        function ($http, $rootScope, $timeout, $q, exModal, SearchCurrentSelectedProcessService) {
            return {
                restrict: 'E',
                scope: false,
                link: function (scope, el, attrs) {
                    scope.$watch('tabs.DP', function (switcher) {
                        if (switcher) {
                            $timeout(function() {
                                $("[elastic-textarea]")[0].style.height = 'auto';
                                $("[elastic-textarea]")[0].style.height = ($("[elastic-textarea]")[0].scrollHeight) + 'px';
                            });
                        }
                    });
                    var baseUrl = '/camunda/api/engine/engine/default';
                    var catalogs = {};
                    var excludeTasksMap = {
                        'PBX': [
                            'checkQuestionnaire'
                        ],
                        'freephone': [
                            'UserTask_0a7shjt',
                            'Task_09uj6n6',
                            'UserTask_0woyg7k',
                            'UserTask_1xsquz3',
                            'Task_1iyjmab',
                            'UserTask_14t2lw4',
                            'UserTask_1jrkuqn'
                        ],
                        'bulksmsConnectionKAE': [
                            'Task_0km85gh',
                            'Task_1cagjud',
                            'Task_1a4tell',
                            'Task_0807llk',
                            'Task_0zconst',
                            'Task_1ffdpes',
                            'Task_1q0yosd',
                            'Task_0gv08f6',
                            'Task_1uz37wa',
                            'Task_065funq',
                            'UserTask_1nk8ri5',
                            'UserTask_1xq5pie',
                            'UserTask_012elxb',
                            'UserTask_01gopqr',
                            'UserTask_18jefcx',
                            'UserTask_1iy9zim',
                            'Task_1qswlt4',
                            'SendTask_1wfg3ue',
                            'Task_1257fik',
                            'Task_0d7igwz'
                        ],
                        'AftersalesPBX': [],
                        'revolvingNumbers': []
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
                    scope.userTasksDP = {};
                    scope.userTasksMapDP = {};
                    scope.clientBINMap = {};
                    scope.binPattern = /^(?:\d{12}|\w+@\w+\.\w{2,3})$/;
                    scope.getBIN = function (val) {
                        if (scope.onlyOneProcessActiveName !== 'AftersalesPBX') return scope.clientBINs;
                        else return scope.aftersalesClientBINs;
                    };

                    scope.filterDP = {
                        processDefinitionKey: '',
                        processDefinitions: [{name: 'PBX', value: 'PBX'}, {
                            name: 'Подключение IVR',
                            value: 'freephone'
                        }, {name: 'BulkSMS', value: 'bulksmsConnectionKAE'}, {
                            name: 'Aftersales PBX',
                            value: 'AftersalesPBX'
                        }, {name: 'PBX Revolving Numbers', value: 'revolvingNumbers'}],
                        processDefinitionActivities: {},
                        activityId: '',
                        businessKey: '',
                        //businessKeyFilterType: 'eq',
                        initiatorId: '',
                        unfinished: false,
                        page: 1,
                        maxResults: 20
                    };
                    scope.salesReprSelected = function ($item) {
                        scope.filterDP.salesRepr = $item.name;
                        scope.filterDP.salesReprId = $item.id;
                    };
                    scope.getXlsxProcessInstancesDP = function () {
                        //return $scope.ExcellentExport.convert({anchor: 'xlsxClick',format: 'xlsx',filename: 'delivery-portal'}, [{name: 'Process Instances',from: {table: 'xlsxDeliveryPortalTable'}}]);

                        var tbl = document.getElementById('xlsxDeliveryPortalTable');
                        var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});

                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                        return XLSX.writeFile(wb, 'delivery-portal-search-result.xlsx');
                    };
                    scope.mapChangeType = {
                        disconnectProcess: "Процесс отключения услуги",
                        changeOfficialClientCompanyName: "Изменение Юридического наименования компании",
                        changeContractNumber: "Изменение номера договора",
                        disconnectOperator: "Отключение оператора",
                        connectOperator: "Добавление оператора",
                        changeConnectionType: "Изменение типа подключения",
                        changeIpNumber: "Изменение IP адреса",
                        changeIdentifier: "Изменение заголовка",
                        changeSmsServiceType: "Подключение МО",
                        changeProvider: "Смена провайдера",
                        changeTransmitNumber: "Смена номера переадресации"
                    }
                    scope.binSelected = function ($item) {
                        scope.filterDP.bin = $item;
                    };
                    var allDPProcesses = {
                        bulksmsConnectionKAE: {
                            title: "bulkSms", value: false
                        },
                        freephone: {
                            title: "freephone", value: false
                        },
                        revolvingNumbers: {
                            title: "revolving numbers", value: false
                        },
                        PBX: {
                            title: "pbx", value: false
                        },
                        AftersalesPBX: {
                            title: "aftersales pbx", value: false
                        }
                    };
                    scope.DPProcesses = {};
                    var dpProject = _.find($rootScope.projects, {'key': 'DeliveryPortal'});
                    angular.forEach(allDPProcesses, function (value, processKey) {
                       if (_.find(dpProject.processes, {'key': processKey})) {
                           scope.DPProcesses[processKey] = value;
                       }
                    });
                    scope.DPProcessesCount = Object.keys(scope.DPProcesses).length;
                    if (scope.DPProcessesCount===1) {
                        angular.forEach(scope.DPProcesses, function (value, processKey) {
                            value.value = true;
                        });
                    } else if (scope.DPProcessesCount===0) {
                        scope.filterDP.processDefinitions = [];
                    }
                    function noProcessSelection() {
                        if (scope.DPProcesses.PBX || scope.DPProcesses.AftersalesPBX || scope.DPProcesses.revolvingNumbers)
                            scope.freephoneOrBulkSms = false;
                        else scope.freephoneOrBulkSms = true;

                        if (scope.DPProcesses.PBX || scope.DPProcesses.freephone || scope.DPProcesses.bulksmsConnectionKAE)
                            scope.aftersalesPBXorRevolving = false;
                        else scope.aftersalesPBXorRevolving = true;

                        if (scope.freephoneOrBulkSms || scope.aftersalesPBXorRevolving) scope.pbx = false;
                        else scope.pbx = true;
                    }
                    scope.onlyOneProcessActive = false;
                    scope.onlyOneProcessActiveName = '';
                    scope.processInstancesDP = [];
                    scope.processInstancesDPTotal = 0;
                    scope.processInstancesDPPages = 0;
                    scope.selectedProcessInstancesDP = [];
                    if (true) {
                        scope.clientBINs = [];
                        scope.aftersalesClientBINs = [];
                        //if (scope.clientBINs.length == 0) { // Initial load only; if previously between project switches did not load bins
                        // since processDefinitionKey is available in Camunda only from version 7.9, which is not our current version
                        // omitted looping through processDefinitionKeys and passing down to each request in a loop
                        $http({
                            method: 'POST',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            data: {
                                variableName: 'clientBIN'
                            },
                            url: baseUrl + '/history/variable-instance'
                        }).then(response => {
                            //scope.clientBINs = [];
                            response.data.forEach(r => {
                                //scope.clientBINMap[r.value] = true;
                                let bin = Number(r.value);
                                if (scope.clientBINs.indexOf(bin) === -1) scope.clientBINs.push(bin);
                                if (r.processDefinitionKey === 'AftersalesPBX' && scope.aftersalesClientBINs.indexOf(bin) === -1) {
                                    scope.aftersalesClientBINs.push(bin);
                                }
                            });
                            //scope.clientBINs = Object.keys(scope.clientBINMap);
                        }).catch(e => null);
                        //}
                    }
                    scope.clearFiltersDP = function () {
                        scope.filterDP.bin = undefined;
                        scope.filterDP.processesDP = [];
                        scope.filterDP.shortNumber = undefined;
                        scope.filterDP.participation = undefined;
                        scope.filterDP.businessKey = undefined;
                        scope.filterDP.startDate = undefined;
                        scope.filterDP.endDate = undefined;
                        scope.filterDP.initiator = undefined;
                        scope.filterDP.initiatorId = undefined;
                        scope.filterDP.activityId = undefined;
                        //scope.filterDP.businessKeyFilterType = 'eq';
                        if (scope.onlyOneProcessActiveName===''){
                            scope.filterDP.processDefinitionKey = undefined;
                        }
                        scope.filterDP.processDefinitionActivities = {};
                        scope.filterDP.unfinished = false;
                        scope.filterDP.finished = false;
                        scope.filterDP.ticName = undefined;
                        scope.filterDP.salesRepr = undefined;
                        scope.filterDP.salesReprId = undefined;
                        scope.filterDP.ip = undefined;
                        scope.filterDP.connectionPoint = undefined;
                        scope.filterDP.pbxNumbers = undefined;
                        scope.filterDP.createdDateRange = undefined;
                        scope.filterDP.callerID = undefined;
                        scope.filterDP.callbackNumber = undefined;
                        $('input[name="multipleDate"]').val('');
                        $timeout(function () {
                            if ( $('input[name="multipleDate"]').data('daterangepicker')){
                                $('input[name="multipleDate"]').data('daterangepicker').setStartDate(moment());
                                $('input[name="multipleDate"]').data('daterangepicker').setEndDate(moment());
                            }
                        });
                        $('#bin').val('');
                    };
                    scope.filterDP.processDefinitions.forEach(def => {
                        $http.get(baseUrl + '/process-definition/key/' + def.value + '/xml')
                            .then(function (response) {
                                if (response) {
                                    var domParser = new DOMParser();
                                    var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');
                                    var userTasks = getUserTasks(xml);
                                    var excludeTasks = excludeTasksMap[def.value];
                                    var includedUserTasks = _.filter(userTasks, function (task) {
                                        return excludeTasks.indexOf(task.id) === -1;
                                    });
                                    scope.userTasksDP[def.value] = includedUserTasks;
                                    var userTasksMap = _.keyBy(includedUserTasks, 'id');
                                    scope.userTasksMapDP[def.value] = userTasksMap;
                                }
                            })
                            .catch(e => console.log("error: ", e));
                    });

                    function isEmpty(obj) {
                        return Object.keys(obj).length === 0;
                    }

                    scope.$watch('DPProcesses', function (newVal, oldVal) {
                        $timeout(function() {
                            $("[elastic-textarea]")[0].style.height = 'auto';
                            $("[elastic-textarea]")[0].style.height = ($("[elastic-textarea]")[0].scrollHeight) + 'px';
                        });
                        var filtered = Object.fromEntries(Object.entries(newVal).filter(([k, v]) => v.value === true));
                        scope.onlyOneProcessActive = false;
                        scope.onlyOneProcessActiveName = '';
                        if (isEmpty(filtered)) {
                            angular.forEach(newVal, function (process, key) {
                                scope.selectedProcessInstancesDP.push({name: key, value: key});
                            });
                            noProcessSelection();
                        } else {
                            if (Object.keys(filtered).length === 1) {
                                // only one process active;
                                scope.onlyOneProcessActive = true;
                                scope.onlyOneProcessActiveName = Object.keys(filtered)[0];
                                scope.selectedProcessInstancesDP = [];
                            }
                            var freephoneOrBulkSms = false;
                            if (filtered.bulksmsConnectionKAE || filtered.freephone) freephoneOrBulkSms = true;
                            else freephoneOrBulkSms = false;
                            scope.freephoneOrBulkSms = freephoneOrBulkSms;

                            var aftersalesPBXorRevolving = false;
                            if (!filtered.PBX && (filtered.AftersalesPBX ||
                                filtered.revolvingNumbers)) aftersalesPBXorRevolving = true;
                            scope.aftersalesPBXorRevolving = aftersalesPBXorRevolving;

                            if (filtered.PBX)  scope.pbx = filtered.PBX;
                            else scope.pbx = false;


                        }


                        if (!scope.onlyOneProcessActive) {
                            scope.filterDP.activityId = undefined;
                        } else {
                            if (!(scope.onlyOneProcessActiveName==='AftersalesPBX')) {
                                scope.filterDP.salesRepr = undefined;
                                scope.filterDP.ip = undefined;
                            } else if (!(scope.onlyOneProcessActiveName==='revolvingNumbers'))  {
                                scope.filterDP.callbackNumber = undefined;
                                scope.filterDP.callerID = undefined;
                            }
                        }

                        angular.forEach(filtered, function (process, key) {
                            scope.selectedProcessInstancesDP.push({name: key, value: key});
                        });
                        scope.filterDP.processDefinitionKey = scope.onlyOneProcessActiveName;

                        if(scope.aftersalesPBXorRevolving || !scope.freephoneOrBulkSms || filtered.PBX) {
                            scope.filterDP.shortNumber = undefined;
                        }
                        if (!scope.aftersalesPBXorRevolving || scope.freephoneOrBulkSms || filtered.PBX) {
                            scope.filterDP.ticName = undefined;
                            scope.filterDP.connectionPoint = undefined;
                            scope.filterDP.pbxNumbers = undefined;
                        }

                    }, true);
                    
                    scope.toggleProcess = function (process) {
                        if (scope.DPProcesses[process].value) scope.DPProcesses[process].value = false;
                        else scope.DPProcesses[process].value = true;
                    };

                    function getProcessInstancesDP(filter, processInstancesDP) {
                        /*
                        if (filter.processDefinitionKey) {
                            var defs = scope.filterDP.processDefinitions.filter(def => filter.processDefinitionKey ? filter.processDefinitionKey === def.value : true);
                        } else {
                            var defs = scope.filterDP.processDefinitions.filter(def => filter.processDefinitionKey ? filter.processDefinitionKey === def.value : true);

                        }
                         */
                        var defs = scope.selectedProcessInstancesDP;
                        var instanceCount = 0;
                        var result = [];

                        function mapOrder(array, key) {
                            var order = {};
                            array.sort(function (a, b) {
                                var A = a[key], B = b[key];
                                if (a.processDefinitionId.indexOf("freephone") > -1) {
                                    order = freephoneTaskOrder;
                                } else if (a.processDefinitionId.indexOf("bulksmsConnectionKAE") > -1) {
                                    order = bulkSMSTaskOrder;
                                } else if (a.processDefinitionId.indexOf("PBX") > -1) {
                                    order = PBXTaskOrder;
                                }
                                if (order[A] > order[B]) {
                                    return 1;
                                } else {
                                    return -1;
                                }
                            });
                            return array;
                        };

                        var freephoneTaskOrder = {
                            add_cost_and_status_of_number: 0,
                            match_identifier_with_client: 1,
                            create_application_to_connection: 2,
                            confirmBillingSystemsDeliveryIdentifiers: 3,
                            title_create_from_PC: 4,
                            confirm_other_operators_creation_short_number: 5,
                            check_other_operators_creation_short_number: 6,
                            offnetSettings: 7,
                            UserTask_0djlmd5: 8,
                            UserTask_1slm9vl: 9,
                            UserTask_1srxk5z: 10,
                            create_SIP_trank_on_SBC: 11,
                            firewall_access_confirm: 12,
                            correct_access_to_firewall: 13,
                            confirmTheSettingsAreCorrect: 14,
                            send_SIP_trank_preferences_to_client: 15,
                            confirmSuccessOfTestsWithVPN: 16,
                            start_tv_check: 17,
                            result_tv_check: 18,
                            confirm_client_agreement_to_connect: 19,
                            confirm_last_mile_start_construction: 20,
                            fillConnectionInformation: 21,
                            confirm_last_mile_finish_construction: 22,
                            confirm_setting_number_on_MSS: 23,
                            confirmSuccessOfTestsW: 24,
                        };

                        var bulkSMSTaskOrder = {
                            addCostAndStatusOfNumbers: 0,
                            matchIdentifierWithClient: 1,
                            createApplicationToConnection: 2,
                            confirmBillingSystemsDeliveryIdentifiers: 3,
                            createConnectionForm: 4,
                            createTitlePC: 5,
                            confirmOtherOperatorsCreationShortNumber: 6,
                            checkOtherOperatorsCreationShortNumber: 7,

                            smsbulkAccountInfo: 8,
                            markTheConnecttionForm: 9,
                            "firewallAccessConfirm.html": 10,
                            correctAccessToFirewall: 11,
                            sendPreferencesToClient: 12,
                            confirmSuccessOfTests: 13,
                            confirmGeneralReadiness: 14,
                            shareWithClient: 15,
                        };

                        var PBXTaskOrder = {
                            checkQuestionnaire: 0,
                            confirmCheckingTSScheme: 1,
                            modifyQuestionnaire: 2,
                            confirmAgreementTSScheme: 3,
                            fillLegalInformation: 4,
                            confirmInTIC: 5,
                            confirmCreatedRoot: 6,
                            confirmCreatedPOI: 7,
                            confirmNumbersInTIC: 8,
                            confirmAdditionIP: 9,
                            confirmSendingTest: 10,
                            confirmTestingSuccess: 11,
                            modifyConnectionSettings: 12,
                            importClientTestCallData: 13,
                            importBillingTestCallData: 14,
                            confirmTestingBilling: 15,
                            confirmStartService: 16,
                            confirmCommercialService: 17,
                            createRequestTCF: 18,
                            confirmTCFRequest: 19,
                            confirmContractReceiption: 20,
                            e1confirmCheckingTSScheme: 21,
                            confirmReturn: 22,
                            confirmPaybackRisk: 23,
                            e1confirmAgreementTUScheme: 24,
                            e1ModifyQuestionnaire: 25,
                            e1downloadContract: 26,
                            confirmTheEnd: 27,
                            confirmTheChannel: 28,
                            e1fillLegalInformation: 29,
                            e1confirmLegalInformation: 30,
                            e1confirmCreatedRoot: 31,
                            e1confirmCreatedPOI: 32,
                            e1confirmInTIC: 33,
                            e1confirmNumbersInTIC: 34,
                            e1confirmTestingBilling: 35,
                            e1createRequestTCF: 36
                        }
                        scope.afterSalesIvrSmsDefinitionId = undefined;
                        $http.get(baseUrl + '/process-definition/key/after-sales-ivr-sms').then(function (response) {
                            if (response && response.data) {
                                if (response.data.id) {
                                    scope.afterSalesIvrSmsDefinitionId = response.data.id;
                                }
                            }
                        });

                        scope[processInstancesDP] = [];
                        if (filter.startedBy !== undefined & filter.processInstanceIds !== undefined) {
                            var processInstanceList = defs.reduce((r, e) => r.push(
                                $http({
                                    method: 'POST',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    data: {
                                        ...filter,
                                        processInstanceIds: undefined,
                                        ...{processDefinitionKey: e.value}
                                    },
                                    url: baseUrl + '/history/process-instance/'
                                }),
                                $http({
                                    method: 'POST',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    data: {
                                        ...filter,
                                        startedBy: undefined,
                                        ...{processDefinitionKey: e.value}
                                    },
                                    url: baseUrl + '/history/process-instance/'
                                })
                                ) && r,
                                [])
                        } else {
                            var processInstanceList = defs.reduce((r, e) => r.push(
                                $http({
                                    method: 'POST',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    data: {
                                        ...filter,
                                        ...{processDefinitionKey: e.value}
                                    },
                                    url: baseUrl + '/history/process-instance'
                                })
                                ) && r,
                                [])
                        }
                        $q.all(processInstanceList.map(def => {
                            return def;
                        }))
                            .then(responseResult => {
                                    //console.log('responseResult', responseResult);
                                    responseResult.forEach(r => {
                                        result.push(...r.data);
                                    });
                                    //console.log('result', result);
                                    // exclude duplicates, since user can be as initiator and as participant of the same process instances: this is the case when it duplicates
                                    scope[processInstancesDP] = result.filter((obj, index, self) => index === self.findIndex((t) => t.id === obj.id));

                                    // order by by startTime descending
                                    scope[processInstancesDP] = scope[processInstancesDP].sort(function (a, b) {
                                        return new Date(b.startTime) - new Date(a.startTime);
                                    });

                                    if (scope[processInstancesDP].length > 0) {
                                        var activeProcessInstancesDP = _.filter(scope[processInstancesDP], function (pi) {
                                            return pi.state === 'ACTIVE';
                                        });

                                        var taskSearchParams = {
                                            processInstanceBusinessKeyIn: _.map(activeProcessInstancesDP, 'businessKey'),
                                            active: true
                                        };
                                        $http({
                                            method: 'POST',
                                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                            data: taskSearchParams,
                                            url: baseUrl + '/task'
                                        }).then(
                                            function (tasks) {
                                                scope[processInstancesDP].forEach(function (el) {
                                                    var f = _.filter(tasks.data, function (t) {
                                                        return t.processInstanceId === el.id;
                                                    });
                                                    if (f && f.length > 0) {
                                                        //el['tasks'] = f;
                                                        el['tasks'] = mapOrder(f, 'taskDefinitionKey');
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
                                    }

                                    if (scope.selectedProcessInstancesDP.some(e => e.value == 'AftersalesPBX')) {
                                        $q.all(scope[processInstancesDP].map(instance => {
                                            return $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + instance.id);
                                        })).then(allResponses => {
                                            var responses = [];
                                            allResponses.forEach(r => responses.push(...r.data));
                                            responses = _.groupBy(responses, r => r.processInstanceId);
                                            for (let r in responses) {
                                                let instance = scope[processInstancesDP].find(e => e.id === r);
                                                if (instance) {
                                                    responses[r].forEach(v => {
                                                        if (v.name === 'legalInfo') instance.legalInfo = JSON.parse(v.value);
                                                        else if (v.name === 'techSpecs') instance.techSpecs = JSON.parse(v.value);
                                                        else if (v.name === 'clientBIN') instance.bin = v.value;
                                                    });
                                                    if (!instance.techSpecs) instance.techSpecs = {};
                                                    if (!instance.techSpecs.sip) instance.techSpecs.sip = {};
                                                    var ok = true;
                                                    if (scope.filterDP.ticName && (!instance.legalInfo.ticName || !instance.legalInfo.ticName.includes(scope.filterDP.ticName))) ok = false;
                                                    if (scope.filterDP.pbxNumbers && (!instance.techSpecs.pbxNumbers || !instance.techSpecs.pbxNumbers.includes(scope.filterDP.pbxNumbers))) ok = false;
                                                    if (scope.filterDP.salesReprId && scope.filterDP.salesReprId !== instance.legalInfo.salesReprId) ok = false;
                                                    if (scope.filterDP.connectionPoint
                                                        && scope.filterDP.connectionPoint !== instance.techSpecs.connectionPoint
                                                        && scope.filterDP.connectionPoint !== instance.techSpecs.connectionPointNew) ok = false;
                                                    if (scope.filterDP.ip
                                                        && (!instance.techSpecs.sip.curPublicVoiceIP || !instance.techSpecs.sip.curPublicVoiceIP.includes(scope.filterDP.ip))
                                                        && (!instance.techSpecs.sip.newPublicVoiceIP || !instance.techSpecs.sip.newPublicVoiceIP.includes(scope.filterDP.ip))
                                                        && (!instance.techSpecs.sip.curSignalingIP || !instance.techSpecs.sip.curSignalingIP.includes(scope.filterDP.ip))
                                                        && (!instance.techSpecs.sip.newSignalingIP || !instance.techSpecs.sip.newSignalingIP.includes(scope.filterDP.ip))) ok = false;

                                                    instance.isOk = ok;
                                                }
                                            }

                                            scope[processInstancesDP] = scope[processInstancesDP].filter(e => e.isOk);

                                            instanceCount = scope[processInstancesDP].length;
                                            //console.log('instanceCount', instanceCount);
                                            scope[processInstancesDP + 'Total'] = instanceCount;
                                            scope[processInstancesDP + 'Pages'] = Math.floor(instanceCount / scope.filterDP.maxResults) + ((instanceCount % scope.filterDP.maxResults) > 0 ? 1 : 0);
                                        });
                                    } else {

                                        instanceCount = scope[processInstancesDP].length;
                                        scope[processInstancesDP + 'Total'] = instanceCount;
                                        scope[processInstancesDP + 'Pages'] = Math.floor(instanceCount / scope.filterDP.maxResults) + ((instanceCount % scope.filterDP.maxResults) > 0 ? 1 : 0);

                                        if (scope[processInstancesDP].length > 0) {
                                            scope[processInstancesDP].forEach(instance => {
                                                if (['freephone', 'bulksmsConnectionKAE', 'AftersalesPBX', 'revolvingNumbers'].indexOf(instance.processDefinitionKey) > -1) {
                                                    $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + instance.id).then(
                                                        function (result) {
                                                            if (scope.selectedProcessInstancesDP.some(e => e.value == 'revolvingNumbers')) {
                                                                result.data.forEach(function (el) {
                                                                    if (el.name === 'clientBIN') instance.bin = el.value;
                                                                    else if (el.name === 'legalInfo') instance.legalInfo = JSON.parse(el.value);
                                                                    else if (el.name === 'techSpecs') instance.techSpecs = JSON.parse(el.value);
                                                                    if (el.name === 'identifiers') {
                                                                        instance.identifiers = JSON.parse(el.value).map(identifier => identifier.title).toString();
                                                                    }
                                                                });
                                                            } else {
                                                                result.data.forEach(function (el) {
                                                                    if (el.name === 'clientBIN') {
                                                                        instance.bin = el.value;
                                                                    }
                                                                    if (el.name === 'identifiers') {
                                                                        instance.identifiers = JSON.parse(el.value).map(identifier => identifier.title).toString();
                                                                    }
                                                                });
                                                            }
                                                        },
                                                        function (error) {
                                                            console.log(error.data);
                                                        }
                                                    );
                                                } else if (scope.selectedProcessInstancesDP.some(e => e.value == 'PBX')) {
                                                    $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + instance.id).then(
                                                        function (result) {
                                                            result.data.forEach(function (el) {
                                                                if (el.name === 'customerInformation') {
                                                                    instance.bin = JSON.parse(el.value).bin;
                                                                }
                                                            });
                                                        },
                                                        function (error) {
                                                            console.log(error.data);
                                                        }
                                                    );
                                                }
                                            });
                                        }
                                    }
                                },
                                function (error) {
                                    console.log(error.data);
                                });
                    }

                    scope.searchDP = function (refreshPages) {
                        scope.toggleIndexAftersales = undefined;
                        if (refreshPages) {
                            scope.filterDP.page = 1;
                            scope.piIndex = undefined;
                            scope.xlsxPreparedDP = false;
                        }

                        scope.xlsxPreparedDP = true;

                        if (scope.onlyOneProcessActive) {
                            scope.filterDP.processDefinitionKey = scope.onlyOneProcessActiveName;
                        }

                        var filter = {
                            processDefinitionKey: scope.filterDP.processDefinitionKey,
                            activeActivityIdIn: [],
                            variables: []
                        };
                        if (scope.filterDP.processDefinitionKey === 'AftersalesPBX') {
                            filter.processDefinitionKey = 'AftersalesPBX';
                            if (scope.filterDP.participation && scope.filterDP.participation.length && scope.filterDP.participation !== 'all') {
                                scope.filterDP.initiator = {id: $rootScope.authentication.name};
                            } else scope.filterDP.initiator = undefined;

                            if (scope.filterDP.salesRepr && scope.filterDP.salesRepr.length && !scope.filterDP.salesReprId) return;
                        }

                        if (scope.filterDP.processDefinitionKey === 'revolvingNumbers') {
                            filter.processDefinitionKey = 'revolvingNumbers';
                            if (scope.filterDP.participation && scope.filterDP.participation.length && scope.filterDP.participation !== 'all') {
                                scope.filterDP.initiator = {id: $rootScope.authentication.name};
                            } else scope.filterDP.initiator = undefined;

                            if (scope.filterDP.salesRepr && scope.filterDP.salesRepr.length && !scope.filterDP.salesReprId) return;

                            if (scope.filterDP.salesReprId) {
                                filter.variables.push({
                                    "name": "searchSalesReprId",
                                    "operator": "like",
                                    "value": "%" + scope.filterDP.salesReprId + "%"
                                });
                            }

                            if (scope.filterDP.callerID) {
                                filter.variables.push({
                                    "name": "searchCallerID",
                                    "operator": "like",
                                    "value": "%" + scope.filterDP.callerID + "%"
                                });
                            }

                            if (scope.filterDP.connectionPoint) {
                                filter.variables.push({
                                    "name": "searchConnectionPoint",
                                    "operator": "eq",
                                    "value": scope.filterDP.connectionPoint
                                });
                            }

                            if (scope.filterDP.callbackNumber) {
                                filter.variables.push({
                                    "name": "callbackNumber",
                                    "operator": "like",
                                    "value": "%" + scope.filterDP.callbackNumber + "%"
                                });
                            }

                            if (scope.filterDP.pbxNumbers) {
                                filter.variables.push({
                                    "name": "searchPbxNumbers",
                                    "operator": "like",
                                    "value": "%" + scope.filterDP.pbxNumbers + "%"
                                });
                            }
                        }

                        if (scope.filterDP.businessKey) {
                            filter.processInstanceBusinessKeyLike = '%' + scope.filterDP.businessKey + '%';
                        }
                        if (scope.filterDP.unfinished) {
                            filter.unfinished = true;
                        }
                        if (scope.filterDP.finished) {
                            filter.finished = true;
                        }

                        if (scope.filterDP.createdDateRange) {
                            var results = scope.convertStringToDate(scope.filterDP.createdDateRange);
                            if (results.length === 2) {
                                filter.startedAfter = results[0];
                                filter.startedBefore = results[1];
                            }
                        }
                        if (scope.filterDP.activityId) {
                            filter.activeActivityIdIn.push(scope.filterDP.activityId);
                        }

                        if (scope.filterDP.shortNumber) {
                            filter.variables.push({
                                "name": "finalIDs",
                                "operator": "like",
                                "value": "%" + scope.filterDP.shortNumber + "%"
                            });
                        }
                        if (scope.filterDP.bin) {
                            filter.variables.push({
                                "name": "clientBIN",
                                "operator": "eq",
                                "value": scope.filterDP.bin.toString()
                            });
                        } else if ($('#bin').val()) {
                            filter.variables.push({
                                "name": "clientBIN",
                                "operator": "like",
                                "value": "%" + $('#bin').val() + "%"
                            });
                        }
                        if (scope.filterDP.initiator) {
                            if (scope.filterDP.participation === 'initiator') {
                                filter.startedBy = scope.filterDP.initiator.id;
                                scope.lastSearchParamsDP = filter;
                                getProcessInstancesDP(filter, 'processInstancesDP');
                            } else if (scope.filterDP.participation === 'participant') {
                                $http.post(baseUrl + '/history/task', {taskAssignee: scope.filterDP.initiator.id}).then(
                                    function (result) {
                                        filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                                        scope.lastSearchParamsDP = filter;
                                        getProcessInstancesDP(filter, 'processInstancesDP');
                                    },
                                    function (error) {
                                        console.log(error.data)
                                    }
                                );
                            } else {
                                $http.post(baseUrl + '/history/task', {taskAssignee: scope.filterDP.initiator.id}).then(
                                    function (result) {
                                        filter.processInstanceIds = _.map(result.data, 'processInstanceId');
                                        filter.startedBy = scope.filterDP.initiator.id;
                                        scope.lastSearchParamsDP = filter;
                                        getProcessInstancesDP(filter, 'processInstancesDP');
                                    },
                                    function (error) {
                                        console.log(error.data)
                                    }
                                );

                            }
                        } else {
                            scope.lastSearchParamsDP = filter;
                            getProcessInstancesDP(filter, 'processInstancesDP');
                        }

                    };
                    scope.getPageInstancesDP = function () {
                        if (scope.processInstancesDP.length !== 0) {
                            return scope.processInstancesDP.slice(
                                (scope.filterDP.page - 1) * scope.filterDP.maxResults,
                                scope.filterDP.page * scope.filterDP.maxResults
                            );
                        }
                        return [];
                    };
                    scope.nextPageDP = function () {
                        scope.filterDP.page++;
                        scope.piIndex = undefined;
                    };

                    scope.prevPageDP = function () {
                        scope.filterDP.page--;
                        scope.piIndex = undefined;
                    };

                    scope.selectPageDP = function (page) {
                        scope.filterDP.page = page;
                        scope.piIndex = undefined;
                    };

                    scope.getPagesDP = function () {
                        var array = [];
                        if (scope.processInstancesDPPages < 8) {
                            for (var i = 1; i <= scope.processInstancesDPPages; i++) {
                                array.push(i);
                            }
                        } else {
                            var decrease = scope.filterDP.page - 1;
                            var increase = scope.filterDP.page + 1;
                            array.push(scope.filterDP.page);
                            while (increase - decrease < 8) {
                                if (decrease > 0) {
                                    array.unshift(decrease--);
                                }
                                if (increase < scope.processInstancesDPPages) {
                                    array.push(increase++);
                                }
                            }
                        }
                        return array;
                    };

                    scope.toggleProcessViewModalDP = function (rowIndex, processDefinitionKey, processDefinitionId, businessKey) {
                        scope.toggleIndexAftersales = undefined;
                        var index = (scope.filterDP.page - 1) * scope.filterDP.maxResults + rowIndex;
                        if (scope.piIndex === index) {
                            scope.piIndex = undefined;
                        } else {
                            scope.piIndex = index;
                            scope.jobModel = {
                                state: scope.processInstancesDP[index].state,
                                processDefinitionKey: processDefinitionKey,
                                processDefinitionId: processDefinitionId,
                                businessKey: businessKey
                            };
                            var asProcess = processDefinitionKey === 'freephone' || processDefinitionKey === 'bulksmsConnectionKAE';
                            scope.currentProcessInstance = SearchCurrentSelectedProcessService(scope.processInstancesDP[index]);
                            $http({
                                method: 'GET',
                                headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                url: baseUrl + '/task?processInstanceId=' + scope.processInstancesDP[index].id,
                            }).then(
                                function (tasks) {
                                    var asynCall1 = false;
                                    var asynCall2 = false;
                                    var asynCall3 = false;
                                    var asynCall4 = false;
                                    var asynCall5 = false;
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
                                                    asynCall4 = true;
                                                    if (asynCall1 && asynCall4 && ((asynCall2 && asynCall3) || !asProcess) && asynCall5) {
                                                        openProcessCardModal();
                                                        asynCall1 = false;
                                                        asynCall2 = false;
                                                        asynCall3 = false;
                                                        asynCall4 = false;
                                                        asynCall5 = false;
                                                    } else {
                                                        console.log(asynCall1,asynCall4, asynCall5, asProcess);
                                                    }
                                                },
                                                function (error) {
                                                    console.log(error.data);
                                                }
                                            );
                                        });
                                    } else {
                                        asynCall4 = true;
                                    }
                                    $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstancesDP[index].id).then(
                                        function (result) {
                                            result.data.forEach(function (el) {
                                                scope.jobModel[el.name] = el;
                                                if (el.type !== 'Json' && (el.value || el.value === "" || el.type === 'Boolean')) {
                                                    scope.jobModel[el.name] = el.value;
                                                }
                                                if (el.type === 'File' || el.type === 'Bytes') {
                                                    scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                                }
                                                if (el.name === 'techSpecs' && el.type === 'String') {
                                                    scope.jobModel[el.name] = JSON.parse(el.value);
                                                }
                                                if (el.type === 'Json') {
                                                    if (el.name === 'resolutions') {
                                                        scope.jobModel[el.name].value = JSON.parse(el.value);
                                                    } else if (['contractScanCopyFileName', 'applicationScanCopyFileName', 'vpnQuestionnaireFileName'].indexOf(el.name) > -1) {
                                                        if (!scope.jobModel.files) {
                                                            scope.jobModel.files = [];
                                                        }
                                                        scope.jobModel.files.push(JSON.parse(el.value));
                                                    } else {
                                                        scope.jobModel[el.name] = JSON.parse(el.value);
                                                    }
                                                }
                                                if (el.name === 'starter') {
                                                    scope.jobModel.starterName = el.value;
                                                    $http.get('/camunda/api/engine/engine/default/user/' + el.value + '/profile').then(
                                                        function (result) {
                                                            scope.jobModel.starterName = result.data.firstName + ' ' + result.data.lastName;
                                                        },
                                                        function (error) {
                                                            console.log(error.data);
                                                        }
                                                    );
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
                                                    asynCall5 = true;
                                                    if (asynCall1 && asynCall4 && ((asynCall2 && asynCall3) || !asProcess) && asynCall5) {
                                                        openProcessCardModal();
                                                        asynCall1 = false;
                                                        asynCall2 = false;
                                                        asynCall3 = false;
                                                        asynCall4 = false;
                                                        asynCall5 = false;
                                                    } else {
                                                        //console.log(asynCall1,asynCall4, asynCall5, asProcess);
                                                    }
                                                });
                                            }
                                            angular.extend(scope.jobModel, catalogs);
                                            scope.jobModel.showTarif = true;
                                            scope.jobModel.tasks = processInstanceTasks;
                                            asynCall1 = true;
                                            if (asynCall1 && asynCall4 && ((asynCall2 && asynCall3) || !asProcess) && asynCall5) {
                                                openProcessCardModal();
                                                asynCall1 = false;
                                                asynCall2 = false;
                                                asynCall3 = false;
                                                asynCall4 = false;
                                                asynCall5 = false;
                                            } else {
                                                //console.log(asynCall1,asynCall4, asynCall5, asProcess);
                                            }
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }

                                    );
                                    if (asProcess) {
                                        /// aftersales start
                                        $http({
                                            method: 'POST',
                                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                            data: {
                                                sorting: [{sortBy: "startTime", sortOrder: "desc"}],
                                                variables: [{
                                                    "name": "relatedProcessInstanceId",
                                                    "operator": "eq",
                                                    "value": scope.processInstancesDP[index].id
                                                }],
                                                processDefinitionKey: 'after-sales-ivr-sms'
                                            },
                                            url: baseUrl + '/history/process-instance'
                                        }).then(function (response) {
                                                if (response && response.data) {
                                                    scope.processInstancesAftersales = angular.copy(response.data);
                                                    if (scope.processInstancesAftersales.length > 0) {
                                                        scope.disableAftersalesStart = scope.processInstancesAftersales.filter(function (pi) {
                                                            return ['EXTERNALLY_TERMINATED', 'INTERNALLY_TERMINATED', 'COMPLETED'].indexOf(pi.state) === -1
                                                        }).length > 0;
                                                        var lengthAS = scope.processInstancesAftersales.length;
                                                        var counterAS = 0;
                                                        scope.processInstancesAftersales.forEach(function (instance) {
                                                            $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + instance.id).then(
                                                                function (result) {
                                                                    instance.changeTypes = [];
                                                                    result.data.forEach(function (el) {
                                                                        if (el.name === 'relatedProcessInstanceVariables') {
                                                                            var relPiVars = JSON.parse(el.value);
                                                                            Object.keys(relPiVars).forEach(function (name) {
                                                                                if (name !== "identifiers") {
                                                                                    instance[name] = relPiVars[name];
                                                                                }
                                                                            });
                                                                        } else {
                                                                            //if(el.name == "identifiers"){
                                                                            if (el.type == "Json") {
                                                                                if (['contractScanCopyFileName', 'applicationScanCopyFileName'].indexOf(el.name) > -1) {
                                                                                    if (!instance.files) {
                                                                                        instance.files = [];
                                                                                    }
                                                                                    instance.files.push(JSON.parse(el.value));
                                                                                } else {
                                                                                    instance[el.name] = JSON.parse(el.value);
                                                                                }
                                                                            } else {
                                                                                instance[el.name] = el.value;
                                                                            }
                                                                        }
                                                                        ;
                                                                        instance.showTarif = true;
                                                                        if (["disconnectProcess", "changeOfficialClientCompanyName", "changeContractNumber", "disconnectOperator", "connectOperator", "changeConnectionType", "changeIpNumber", "changeIdentifier", "changeSmsServiceType", "changeProvider", "changeTransmitNumber"].indexOf(el.name) > -1) {
                                                                            if (el.value) {
                                                                                instance.changeTypes.push(scope.mapChangeType[el.name]);
                                                                                if (el.name === "disconnectProcess") {
                                                                                    if (instance.state === "COMPLETED") {
                                                                                        scope.disableAftersalesStart = true;
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                    if (lengthAS - 1 === counterAS) asynCall3 = true;
                                                                    if (asynCall1 && asynCall4 && asynCall2 && asynCall3 && asynCall5) {
                                                                        openProcessCardModal();
                                                                        asynCall1 = false;
                                                                        asynCall2 = false;
                                                                        asynCall3 = false;
                                                                        asynCall4 = false;
                                                                        asynCall5 = false;
                                                                    } else {
                                                                        //console.log(asynCall1,asynCall4, asynCall5);
                                                                    }
                                                                    counterAS += 1;
                                                                },
                                                                function (error) {
                                                                    console.log(error.data);
                                                                }
                                                            )
                                                        });
                                                    } else {
                                                        asynCall3 = true;
                                                        if (asynCall1 && asynCall4 && asynCall2 && asynCall3 && asynCall5) {
                                                            openProcessCardModal();
                                                            asynCall1 = false;
                                                            asynCall2 = false;
                                                            asynCall3 = false;
                                                            asynCall4 = false;
                                                            asynCall5 = false;
                                                        } else {
                                                            //console.log(asynCall1,asynCall4, asynCall5);
                                                        }

                                                        scope.disableAftersalesStart = false;
                                                    }
                                                    asynCall2 = true;
                                                    if (asynCall1 && asynCall4 && asynCall2 && asynCall3 && asynCall5) {
                                                        openProcessCardModal();
                                                        asynCall1 = false;
                                                        asynCall2 = false;
                                                        asynCall3 = false;
                                                        asynCall4 = false;
                                                        asynCall5 = false;
                                                    }
                                                }

                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );
                                        /// aftersales end
                                    }
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        }
                    };

                    function openProcessCardModal() {
                        var template = './js/partials/';
                        if (scope.jobModel.processDefinitionKey === 'freephone') {
                            template += 'infoFreephoneSearch.html';
                        } else if (scope.jobModel.processDefinitionKey === 'bulksmsConnectionKAE') {
                            template += 'infoBulksmsSearch.html';
                        } else if (scope.jobModel.processDefinitionKey === 'PBX') {
                            template += 'infoPBXSearch.html';
                        } else if (scope.jobModel.processDefinitionKey === 'AftersalesPBX') {
                            template += 'infoAftersalesPBXSearch.html';
                        } else if (scope.jobModel.processDefinitionKey === 'revolvingNumbers') {
                            template += 'infoRevolvingNumbersSearch.html';
                        }
                        exModal.open({
                            scope: {
                                jobModel: scope.jobModel,
                                diagram: [],
                                processInstancesAftersales: scope.processInstancesAftersales,
                                showGroupDetails:scope.showGroupDetails,
                                getStatus: scope.getStatus,
                                showDiagram: scope.showDiagram,
                                showHistory: scope.showHistory,
                                startProcess: scope.startProcess,
                                afterSalesIvrSmsDefinitionId: scope.afterSalesIvrSmsDefinitionId,
                                disableAftersalesStart : scope.disableAftersalesStart,
                                startDisconnectProcess: scope.startDisconnectProcess,
                                toggleIndexAftersales: scope.toggleIndexAftersales,
                                toggleInfoAftersales: scope.toggleInfoAftersales,
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
                            controller: DPModalController,
                            templateUrl: template,
                            size: 'lg'
                        }).then(function(results){
                        });

                    }

                    DPModalController.$inject = ['scope', '$http', '$timeout'];

                    function DPModalController(scope, $http) {
                        scope.toggleProcessViewAftersales = function (index) {
                            if (scope.toggleIndexAftersales === index) {
                                scope.toggleIndexAftersales = undefined;
                            } else {
                                scope.toggleIndexAftersales = index;
                                scope.toggleInfoAftersales = scope.processInstancesAftersales[index];
                                scope.toggleInfoAftersales.changeIdentifierType = false;
                                if (scope.processInstancesAftersales[index].state === "ACTIVE") {
                                    $http({
                                        method: 'GET',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        url: baseUrl + '/process-instance/' + scope.processInstancesAftersales[index].id + '/activity-instances',
                                    }).then(
                                        function (tasks) {
                                            var callActivityTasks = tasks.data.childActivityInstances;
                                            scope.toggleInfoAftersales.callActivityTasks = callActivityTasks;


                                        },
                                        function (error) {
                                            console.log(error.data);
                                            scope.toggleInfoAftersales.callActivityTasks = undefined;
                                        }
                                    );
                                }
                                $http({
                                    method: 'GET',
                                    headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                    url: baseUrl + '/task?processInstanceBusinessKey=' + scope.processInstancesAftersales[index].businessKey,
                                }).then(
                                    function (tasks) {
                                        var processInstanceTasks = tasks.data._embedded.task;
                                        if (processInstanceTasks && processInstanceTasks.length > 0 && scope.processInstancesAftersales[index].state === "ACTIVE") {
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
                                            scope.toggleInfoAftersales.tasks_decr = processInstanceTasks;
                                        } else {
                                            scope.toggleInfoAftersales.tasks_decr = {};
                                        }
                                        $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstancesAftersales[index].id).then(
                                            function (result) {
                                                result.data.forEach(function (el) {
                                                    if (el.type === 'Json' && el.name === 'resolutions') {
                                                        scope.toggleInfoAftersales[el.name] = el;
                                                        scope.toggleInfoAftersales[el.name].value = JSON.parse(el.value);
                                                    }
                                                });
                                                if (scope.toggleInfoAftersales.resolutions && scope.toggleInfoAftersales.resolutions.value) {
                                                    $q.all(scope.toggleInfoAftersales.resolutions.value.map(function (resolution) {
                                                        return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                                    })).then(function (tasks) {
                                                        tasks.forEach(function (e, index) {
                                                            if (e.data.length > 0) {
                                                                scope.toggleInfoAftersales.resolutions.value[index].taskName = e.data[0].name;
                                                                try {
                                                                    scope.toggleInfoAftersales.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                                } catch (e) {
                                                                    console.log(e);
                                                                }
                                                            }
                                                        });
                                                    });
                                                }
                                                //angular.extend(scope.toggleInfoAftersales, catalogs);
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
                    }

                },
                templateUrl: './js/directives/search/deliveryPortalSearch.html'
            };
        }]);
});