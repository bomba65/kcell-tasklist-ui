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

        $scope.getString = function(val){
            return typeof val !== 'undefined' && val !== null ? (typeof val === 'string' ? val : val.toString()) : undefined;
        }

        $scope.initTextValue = function(val, field) {
            var result = typeof field.value === 'undefined' ? $scope.getString(val) : $scope.getString(field.value);
            if(typeof result !== 'undefined') {
                if(field.prefixValue && result.indexOf(field.prefixValue) === -1) {
                    result = field.prefixValue + result;
                }
                if(field.postfixValue && result.indexOf(field.postfixValue) === -1) {
                    result = result + field.postfixValue;
                }
            }
            return result;
        }

        $scope.tcfChangeHandler = function(tcfId, definition){
            angular.forEach(definition.tasks, function(instance){
                if(defKey === 'massApprove_bulkSMS_confirmAndWriteAmdocsTCF') {
                    //instance.identifierAmdocsID = tcfId;
                    instance.amdocsTcfId = tcfId;
                } else if(defKey === 'massApprove_bulkSMS_confirmAndWriteOrgaTCF') {
                    //instance.identifierOrgaID = tcfId;
                    instance.orgaTcfId = tcfId;
                } else if(defKey === 'massApprove_confirmAndWriteAmdocsTCF') {
                    //instance.identifierAmdocsID = tcfId;
                    instance.amdocsTcfId = tcfId;
                } else if(defKey === 'massApprove_confirmAndWriteOrgaTCF') {
                    //instance.identifierOrgaID = tcfId;
                    instance.orgaTcfId = tcfId;
                }
            });
        }

        $scope.commentChangeHandler = function(commentPC, definition){
            angular.forEach(definition.tasks, function(instance){
                if(defKey === 'massApprove_bulkSMS_addedShortNumberForAmdocsNew') {
                    instance.comment = commentPC;
                } else if(defKey === 'massApprove_bulkSMS_addedShortNumberForOrgaNew') {
                    instance.comment = commentPC;
                } else if(defKey === 'massApprove_addedShortNumberForAmdocsNew') {
                    instance.comment = commentPC;
                } else if(defKey === 'massApprove_addedShortNumberForOrgaNew') {
                    instance.comment = commentPC;
                }
            });
        }

        $scope.toggleAllTasks = function(definition) {
            // Since toggling has ONLY 2 possible states(input type = "checkbox") - checked and unchecked
            // DO NOT USE THIS FOR RESOLUTIONS THAT HAVE MORE THAN 2 POSSIBLE VARIABLES! 
            // Something like "APPROVE" or "REJECT" is the right way
            // FOR CASES when resolutions have > 2 possible values USE: radio button or select option, but not for checkboxes
            
            definition.allTasksSelected = !definition.allTasksSelected ? true : false;
            if(definition.configs.resolutions && definition.configs.resolutions.length>0) {
                if(typeof definition.radioSelectedIndex === 'undefined') {
                    definition.radioSelectedIndex = 0;
                    definition.radioSelectedValue = definition.configs.resolutions[definition.radioSelectedIndex].variable;
                    definition.radioSelectedText = definition.configs.resolutions[definition.radioSelectedIndex].text;
                    angular.forEach(definition.tasks, function(instance){ 
                        instance.resolution = definition.radioSelectedValue;
                    });
                } else {
                    definition.radioSelectedIndex = undefined;
                    definition.radioSelectedValue = undefined;
                    definition.radioSelectedText = undefined;
                    angular.forEach(definition.tasks, function(instance){ 
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

        $scope.clearResolution = function(definition, instance){
            definition.allTasksSelected = false;
            instance.resolution = null;
            definition.radioSelectedIndex = undefined;
            definition.radioSelectedValue = undefined;
            definition.radioSelectedText = undefined;
        }

        $scope.changeResolution = function(selectedValue, definition){
            if(definition.configs.resolutions[0].variable === selectedValue) {
                if(definition.tasks.length === definition.tasks.filter(instance=>selectedValue === instance.resolution).length){
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

        $scope.onSelect = function(field, instance){
            if(field.name === "inAndOut") {
                if (field.dependants && field.dependants.length>0 && $scope.taskData[instance.taskId][field.name]){
                    field.dependants.forEach(function(dependant){
                        if((dependant.fieldName === "identifierServiceName_amdocs" && instance[dependant.fieldName]) || (dependant.fieldName === "identifierServiceName_orga" && instance[dependant.fieldName])){
                            if(!$scope.taskData[instance.taskId][dependant.fieldName]) {
                                $scope.taskData[instance.taskId][dependant.fieldName] = instance[dependant.fieldName];
                            }
                            instance[dependant.fieldName] = $scope.taskData[instance.taskId][field.name] + ' ' + $scope.taskData[instance.taskId][dependant.fieldName].replace("Incoming ", "").replace("Outgoing ", "");
                        } else if((dependant.fieldName === "abonentTarif_amdocs" && instance[dependant.fieldName]) || (dependant.fieldName === "abonentTarif_orga" && instance[dependant.fieldName])) {
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
                        configs: {},
                        allTasksSelected: false
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
                $scope.closeDate = new Date();
                $scope.tcfId = "";
                $scope.commentPC = "";

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

        var waiting = 0;

        $scope.submitThem = function() {
            waiting = 0;
            for (var i = 0; i < $scope.definitions.length; i++) {
                const definition = $scope.definitions[i];

                var mandatoryFields = [];
                angular.forEach(definition.configs.table.rows, function(row){
                    Object.assign(mandatoryFields, row.fields.filter(function(field){return field.notNull}));
                });

                for (var j = 0; j < definition.tasks.length; j++) {
                    const instance = $scope.definitions[i].tasks[j];
                    if (!instance.resolution) continue;

                    var emptyFields = mandatoryFields.filter(function(field) {
                        return instance[field.name] ? false : $scope.taskData[instance.taskId][field.name] ?  false : true;
                    });

                    if (!(definition.configs.comment ? definition.configs.comment.overrideRowComment : false) && !instance.comment){
                        emptyFields.push({name: "comment",notNull: true,readOnly:false,save:true,type:"text"});
                    };

                    //console.log(emptyFields, mandatoryFields);
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
                    //console.log(baseUrl+"/task/"+instance.taskId+"/variables/"+resName, resValue);
                    //$http.put(baseUrl+"/task/"+instance.taskId+"/variables/"+resName, {value: resValue, type: "String"}).then(function() {
                        var variables = {};
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

                        /*
                        definition.configs.table.fields.filter(field => !field.override && (!field.readOnly || field.save)).forEach(field=>{
                            variables[field.name] = {
                                value: instance[field.name],
                                type: "String"
                            };
                        });
                        */
                        angular.forEach(definition.configs.table.rows, function(row){
                            //Object.assign(variables, row.filter(function(field){return field.notNull}));
                            row.fields.filter(field => !field.override && (!field.readOnly || field.save)).forEach(field=>{
                                variables[field.name] = {
                                    value: instance[field.name],
                                    type: "String"
                                };
                            });
                        });

                        if (definition.configs.showTCFID){
                            if(instance.amdocsTcfId){
                                variables['amdocsTcfId'] = {
                                    value: instance.amdocsTcfId,
                                    type: "String"
                                };
                            }
                            if(instance.orgaTcfId){
                                variables['orgaTcfId'] = {
                                    value: instance.orgaTcfId,
                                    type: "String"
                                };
                            }
                        }

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
                        //console.log('$scope.closeDate')
                        //console.log($scope.closeDate)
                        variables[dateName] = {
                            value: $scope.closeDate,
                            type: "Date"
                        };
                        $http.post(baseUrl+"/task/"+instance.taskId+"/submit-form", {variables: variables}).then(function() {
                            waiting--;
                            refreshPage();
                        });
                    //});
                }
            }
        }

        $scope.massTableField =  function(instance,f){
            //console.log(f, instance);
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