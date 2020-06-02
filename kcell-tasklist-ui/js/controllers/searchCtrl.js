define(['./module', 'jquery', 'moment', 'camundaSDK'], function (app, $, moment, CamSDK) {
    'use strict';
    return app.controller('searchCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q', '$location', '$timeout', 'AuthenticationService', 'exModal', '$state', 'StartProcessService', 'SearchCurrentSelectedProcessService', 'disconnectSelectedProcessService',
        function ($scope, $rootScope, $http, $routeParams, $q, $location, $timeout, AuthenticationService, exModal, $state, StartProcessService, SearchCurrentSelectedProcessService, disconnectSelectedProcessService) {
            var camClient = new CamSDK.Client({
                mock: false,
                apiUri: '/camunda/api/engine/'
            });
            $scope.tabs = {};
            angular.forEach($rootScope.projects, function (value, processKey) {
                $scope.tabs[processKey] = false;
            });
            $scope.isProjectAvailable =  function(projectKey){
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

            $scope.convertStringToDate = function(date_string) {
                var result = date_string.split(" - ");
                if (result.length===2) {
                    var dateParts_from = result[0].split(".");
                    if (dateParts_from[2].length===2) {
                        dateParts_from[2]  = "20" + dateParts_from[2];
                    }
                    var dateObject_from = new Date(+dateParts_from[2], dateParts_from[1] - 1, +dateParts_from[0]);
                    var dateParts_to = result[1].split(".");
                    if (dateParts_to[2].length===2) {
                        dateParts_to[2]  = "20" + dateParts_to[2];
                    }
                    var dateObject_to= new Date(+dateParts_to[2], dateParts_to[1] - 1, +dateParts_to[0]);
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
                var tbl = document.getElementById( 'revisionsTable');
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
                        processDefinitionId : processDefinitionId,
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
                                        } else console.log(asynCall1, asynCall2,asynCall3);
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
                              } else console.log(asynCall1, asynCall2,asynCall3);
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
                                        } else console.log(asynCall1, asynCall2,asynCall3);
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
                                } else console.log(asynCall1, asynCall2,asynCall3);
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
                }).then(function(results){
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

                    $scope.demandSearch = function(refresh) {
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

        }]);
});