define(['./module','jquery'], function(app,$){
	'use strict';
	return app.controller('statisticsCtrl', ['$scope', '$rootScope', '$http', '$routeParams', '$q',
			                         function($scope,   $rootScope,   $http,   $routeParams,   $q) {
		$rootScope.currentPage = {
			name: 'statistics'
		};

		$scope._ = window._;

		$scope.baseUrl = '/camunda/api/engine/engine/default';
		// $scope.baseUrl = "https://test-flow.kcell.kz/engine-rest/engine/default";

		$scope.reportsMap = {
            'revision-open-tasks': {name: 'Revision open tasks'}
        };

        $scope.reports = [
        	'revision-open-tasks'
        ];

		$scope.currentReport = $routeParams.report || 'revision-open-tasks';

        $scope.task = $routeParams.task;

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

        if ($scope.currentReport === 'revision-open-tasks') {
        	if ($scope.task) {
				$http.post($scope.baseUrl + '/history/task', {
                    taskDefinitionKey: $scope.task,
                    processDefinitionKey: 'Revision',
                    unfinished: true
				}).then(function(response) {
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
                    console.log(tasks);
                    $scope.tasks = tasks;
                });

			} else {
        		$scope.kcellTasks = [
                    'UserTask_1ru64f6',
                    'UserTask_11b2osi',
                    'Task_1xhzfxw',
                    'UserTask_1qksldt',
                    'UserTask_1tpn4q3',
                    'UserTask_1qf7rmc',
                    'UserTask_1uw9qzb',
                    'Task_0euindd',
                    'Task_0s5v6wl',
                    'UserTask_12n8eyi',
                    'UserTask_1n39kzy',
                    'Task_0jxwgbt',
                    'UserTask_1g0uit4',
                    'UserTask_0ib18ut',
                    'UserTask_0xsau1t',
                    'UserTask_1i7na4a',
                    'UserTask_0m3wppw',
                    'Task_1kwxxw1'
                ];

        		$scope.contractorTasks = [
                    'UserTask_0syren9',
                    'UserTask_14yc5q6',
                    'UserTask_1wkrl5k',
                    'UserTask_0rj3nbv'
                ];

                var userTasksPromise = $http.get($scope.baseUrl + '/process-definition/key/Revision/xml')
					.then(function(response) {
                        var domParser = new DOMParser();

                        var xml = domParser.parseFromString(response.data.bpmn20Xml, 'application/xml');

                        function getUserTasks(xml) {
                            var namespaces = {
                                bpmn: 'http://www.omg.org/spec/BPMN/20100524/MODEL'
                            };

                            var userTaskNodes = getElementsByXPath(xml, '//bpmn:userTask', prefix => namespaces[prefix]);

                            function getElementsByXPath(doc, xpath, namespaceFn, parent)
                            {
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

						return getUserTasks(xml);

                    });

                var processInstancesPromise = $http.post($scope.baseUrl + '/history/process-instance', {
                    "processDefinitionKey": "Revision"
				}).then(function(response) {
					var processInstances = _.keyBy(response.data, 'id');
					return $http.post($scope.baseUrl + '/history/variable-instance', {
                        variableName: 'jrNumber',
                        processInstanceIdIn: _.keys(processInstances)
					}).then(function(response){
						var variablesByProcessInstance = _.keyBy(response.data, 'processInstanceId');
						var valueByProcessInstance = _.mapValues(variablesByProcessInstance, 'value');
						return _.mapValues(processInstances, (pi, id) => _.assign({}, pi, {'jrNumber': valueByProcessInstance[id]}))
					});
				});

                var taskInstancesPromise = $http.post($scope.baseUrl + '/history/task', {
                    "processDefinitionKey": 'Revision',
                    "unfinished": true
				}).then(function(response) {
					return response.data;
				});

				$q.all([userTasksPromise, processInstancesPromise, taskInstancesPromise])
					.then(function(results) {
                        var userTasks = results[0];
                        var processInstances = results[1];
                        var taskInstances = results[2];

                        var userTasksMap = _.keyBy(userTasks, 'id');

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
                        $scope.userTasksMap = userTasksMap;
					});
			}
		}
	}]);
});
