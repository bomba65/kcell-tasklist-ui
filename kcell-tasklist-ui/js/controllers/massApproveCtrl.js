define(['./module', 'lodash', 'big-js'], function(module, _, Big){
	'use strict';
	return module.controller('MassApproveCtrl', ['$scope', '$rootScope', 'toasty', 'AuthenticationService', '$stateParams', '$timeout', '$location', 'exModal', '$http', '$state', function($scope, $rootScope, toasty, AuthenticationService, $stateParams, $timeout, $location, exModal, $http, $state) {
        const baseUrl = '/camunda/api/engine/engine/default';
        const defKey = $stateParams.defKey;
        if (!defKey) return;

        $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name+ "/profile").then(function(result){
            $rootScope.authentication.assigneeName = (result.data.firstName ? result.data.firstName : "") + " " + (result.data.lastName ? result.data.lastName : "");
        });

        $scope.override = function(fields, fieldToOverRide) {
            return fields.filter(f=>f.name === fieldToOverRide && f.override).length > 0;
        }

        $scope.onSelect = function(field, instance){
            if(field.name === "inAndOut") {
                if (field.dependants && field.dependants.length>0 && $scope.taskData[instance.taskId][field.name]){
                    field.dependants.forEach(function(dependant){
                        if(dependant.fieldName === "identifierServiceName" && instance[dependant.fieldName]){
                            if(!$scope.taskData[instance.taskId][dependant.fieldName]) {
                                $scope.taskData[instance.taskId][dependant.fieldName] = instance[dependant.fieldName];
                            }
                            console.log($scope.taskData[instance.taskId][field.name], $scope.taskData[instance.taskId][dependant.fieldName])
                            instance[dependant.fieldName] = $scope.taskData[instance.taskId][field.name] + ' ' + $scope.taskData[instance.taskId][dependant.fieldName];
                        } else if(dependant.fieldName === "abonentTarif" && instance[dependant.fieldName]) {
                            if(!$scope.taskData[instance.taskId][dependant.fieldName]) {
                                $scope.taskData[instance.taskId][dependant.fieldName] = instance[dependant.fieldName];
                            }
                            if($scope.taskData[instance.taskId][field.name] === "Incoming") {
                                instance[dependant.fieldName] = '0';
                            }
                            if($scope.taskData[instance.taskId][field.name] === "Outgoing") {
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
                _.forOwn(groupedByDefs, function(val, key) {
                    if (!val.length) return;
                    var definition = {
                        id: key,
                        name: val[0].name,
                        instances: [],
                        tasks: [],
                        configs: {}
                    };
                    _.each(val, function(v) {
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

                var pids = _.flatMap(response.data, function (v) { return v.processInstanceId; });
                $http({
                    method: 'POST',
                    headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                    data: {processInstanceIds: pids},
                    url: baseUrl + '/history/process-instance'
                }).then(
                    function(processes){
                        angular.forEach($scope.definitions, function(d){
                            angular.forEach(d.tasks, function(t){
                                var process = _.find(processes.data, function(p){ return p.id === t.pid });
                                t.processBusinessKey = process.businessKey;
                            });
                        });
                    },
                    function(error){
                        console.log(error.data);
                    }
                );

                var tids = _.flatMap(response.data, function (v) { return v.id; });
                var counter =0;
                // $scope.closeDate = new Date();
                $scope.closeDate = new Date();
                tids.forEach(function(tid) {
                    $http.get(baseUrl + "/task/" + tid + "/form-variables?deserializeValues=false").then(function (response) {
                        var procFields = [];
                        var fields = {};
                        _.forOwn(response.data, function(value, key) {
                            if(value.type === 'Json' || key === 'massApproveConfigs'){
                                fields[key] = JSON.parse(value.value);
                            } else fields[key] = value.value;
                        });
                        for (var i = 0; i < $scope.definitions.length; i++) {
                            var found = false;
                            for(var j = 0; j < $scope.definitions[i].tasks.length; j++) {
                                
                                if ($scope.definitions[i].tasks[j].taskId === tid) {
                                    $scope.definitions[i].configs = fields.massApproveConfigs;
                                    delete(fields.massApproveConfigs);
                                    $scope.definitions[i].tasks[j] = _.assign($scope.definitions[i].tasks[j], fields);
                                }
                            }
                            if (found) break;
                        }
                        counter++;
                        if (counter === tids.length) {
                            $scope.allVariablesAssign = true;
                        }
                    });
                });
            }
        });

        var waiting = 0;

        $scope.submitThem = function() {
            waiting = 0;
            for (var i = 0; i < $scope.definitions.length; i++) {
                const definition = $scope.definitions[i];
                var mandatoryFields = definition.configs.table.fields.filter(function(field){return field.notNull});
                for (var j = 0; j < definition.tasks.length; j++) {
                    const instance = $scope.definitions[i].tasks[j];
                    if (!instance.resolution) continue;

                    var emptyFields = mandatoryFields.filter(function(field) {
                        return instance[field.name] ? false : $scope.taskData[instance.taskId][field.name] ?  false : true;
                    });
                    console.log(emptyFields, mandatoryFields);
                    if(emptyFields.length>0) continue;

                    waiting++;
                    // Insert resolution
                    var resName = defKey + "TaskResult";
                    var resValue = "";
                    if (definition.configs.resolutionVariable && definition.configs.resolutionVariable.length) {
                        resName = definition.configs.resolutionVariable;
                    }
                    if (instance.resolution && instance.resolution.length) {
                        resValue = instance.resolution;
                    }
                    console.log(baseUrl+"/task/"+instance.taskId+"/variables/"+resName);
                    $http.put(baseUrl+"/task/"+instance.taskId+"/variables/"+resName, {value: resValue, type: "String"}).then(function() {
                        var variables = {};
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

                        definition.configs.table.fields.filter(field => !field.override).forEach(field=>{
                            variables[field.name] = {
                                value: instance[field.name], // || $scope.taskData[instance.taskId][field.name],
                                type: "String"
                            };
                        });

                        console.log('variables',variables);
                        console.log('instance',instance);

                        // Update or Insert resolutions
                        let ressName = "resolutions";
                        if (definition.configs.historyVariable && definition.configs.historyVariable.length) {
                            ressName = definition.configs.historyVariable;
                        }
                        var resolutions = [];
                        if (instance[ressName]) {
                            resolutions = instance[ressName];
                        }
                        resolutions.push({processInstanceId: instance.pid, assignee: $rootScope.authentication.name, assigneeName: $rootScope.authentication.assigneeName, resolution: instance.resolution, comment: instance.comment || comValue, taskId: instance.taskId});
                        variables[ressName] = {
                            value: JSON.stringify(resolutions),
                            type: "Json"
                        };
                        var dateName = defKey + "TaskCloseDate";
                        console.log('$scope.closeDate')
                        console.log($scope.closeDate)
                        variables[dateName] = {
                            value: new Date($scope.closeDate),
                            type: "Date"
                        };
                        //$http.post(baseUrl+"/task/"+instance.taskId+"/complete", {variables: variables}).then(function() {
                        $http.post(baseUrl+"/task/"+instance.taskId+"/submit-form", {variables: variables}).then(function() {
                            waiting--;
                            refreshPage();
                        });
                    });
                }
            }
        }

        $scope.massTableField =  function(instance,f){
            //console.log('massTableField', instance,f);
            if(f.indexOf(':') !== -1) {
                var result = undefined;
                while(f.indexOf(':') !== -1){
                    var property = f.substring(0,f.indexOf(':'));
                    f = f.substring(f.indexOf(':')+1,f.length);
                    if (instance.hasOwnProperty(property)) result = instance[property];
                } 
                if (result && result.hasOwnProperty(f)) {
                    return result[f];
                }
            } else {
                return instance[f];
            }
        }

        function refreshPage() {
            if (waiting == 0) {
                $state.go("tasks", {}, {reload: true});
            }
        }
	}]);
});