define(['./module'], function(module){
	'use strict';
	module.directive('invoiceDetail', function ($rootScope, $http) {
		return {
			restrict: 'E',
			scope: {
				invoice: '='
			},
			link: function(scope, element, attrs) {
				scope.catalogs = {};

                scope.processInstanceId = undefined;
                scope.pkey = undefined;
                scope.selectedWorks = [];
                var selectedWorksMap = {};      

                var Big;
                if(window.require){
                    Big = require('big-js')
                }
                scope.totalPrice = Big('0.0');

                function transformToArray(){
                    for(var propt in scope.invoice.selectedRevisions.value){
                        _.forEach(scope.invoice.selectedRevisions.value[propt].works, function (work) {
                            var processDetailCopy = angular.copy(scope.invoice.selectedRevisions.value[propt]);
                            processDetailCopy.quantity = work.quantity;
                            delete processDetailCopy.works;

                            var prices = _.find(scope.invoice.selectedRevisions.value[propt].workPrices, function(price){
                                return price.sapServiceNumber === work.sapServiceNumber;
                            });
                            processDetailCopy.workPrices = prices;
                            scope.totalPrice = scope.totalPrice.plus(Number(prices.total));

                            if(!selectedWorksMap[work.sapServiceNumber]){
                                selectedWorksMap[work.sapServiceNumber] = [processDetailCopy];
                            } else {
                                selectedWorksMap[work.sapServiceNumber].push(processDetailCopy);
                            }
                        });
                    }
                    _.forEach(selectedWorksMap, function(value, key) {
                        scope.selectedWorks.push({ key: key, value: value});
                    });
                    scope.total = scope.totalPrice.toFixed(2);
                }                

				scope.getCatalogs = function(){
	                $http.get('/api/catalogs').then(
	                    function (result) {
	                    	angular.extend(scope.catalogs, result.data);
	                    },
	                    function (error) {
	                        console.log(error.data);
	                    }
	                );
                    transformToArray();
				}

	            scope.toggleProcessView = function(processInstanceId, key){
                    if(scope.processInstanceId === processInstanceId){
                       	scope.processInstanceId = undefined;
                        scope.jobModel = {};
                    } else {
                        scope.jobModel = {};
                        scope.processInstanceId = processInstanceId;
                        scope.pkey = key;
                        scope.jobModel.state = 'ACTIVE';
                        $http({
                            method: 'GET',
                            headers:{'Accept':'application/hal+json, application/json; q=0.5'},
                            url: '/camunda/api/engine/engine/default/task?processInstanceId='+processInstanceId,
                        }).then(
                                function(tasks){
                                    var processInstanceTasks = tasks.data._embedded.task;
                                    if(processInstanceTasks && processInstanceTasks.length > 0){
                                        processInstanceTasks.forEach(function(e){
                                            if(e.assignee && tasks.data._embedded.assignee){
                                                for(var i=0;i<tasks.data._embedded.assignee.length;i++){
                                                    if(tasks.data._embedded.assignee[i].id === e.assignee){
                                                        e.assigneeObject = tasks.data._embedded.assignee[i];
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    $http.get('/camunda/api/engine/engine/default/history/variable-instance?deserializeValues=false&processInstanceId='+processInstanceId).then(
                                            function(result){
                                                var workFiles = [];
                                                result.data.forEach(function(el){
                                                    scope.jobModel[el.name] = el;
                                                    if(el.type === 'File' || el.type === 'Bytes'){
                                                        scope.jobModel[el.name].contentUrl = '/camunda/api/engine/engine/default/history/variable-instance/'+el.id+'/data';
                                                    }
                                                    if(el.type === 'Json'){
                                                        scope.jobModel[el.name].value = JSON.parse(el.value);
                                                    }
                                                    if(el.name.startsWith('works_') && el.name.includes('_file_')){
                                                        workFiles.push(el);
                                                    }
                                                });
                                                workFiles.forEach(function(file){
                                                    var workIndex = file.name.split('_')[1];
                                                    if (!scope.jobModel.jobWorks.value[workIndex].files) {
                                                        scope.jobModel.jobWorks.value[workIndex].files = [];
                                                    }
                                                    scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                                });
                                                scope.jobModel.tasks = processInstanceTasks;
    						            		angular.extend(scope.jobModel, scope.catalogs);
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
                };
	        },
			templateUrl: './js/directives/invoiceDetail.html'
		};
	});
});