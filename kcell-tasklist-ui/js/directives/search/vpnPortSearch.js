define(['../module'], function (module) {
    'use strict';
    module.directive('vpnPortSearch', ['$http', '$timeout', '$q', '$rootScope', 'exModal', 'toasty', function ($http, $timeout, $q, $rootScope, exModal, toasty) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                $http.get('/camunda/camunda-process/vpn-port-process/task-list').then(response => {
                    scope.taskList = response.data;
                })

                scope.formData = {
                    process: undefined,
                    channel: undefined,
                    requestType: undefined,
                    currentActivity: undefined,
                    priority: undefined,
                    dateCreated: undefined,
                    requestNumber: undefined,
                    portId: undefined,
                    vpnId: undefined
                }

                var processList = [];

                scope.search = function () {
                    scope.setCurrentPage(1);

                    var queryParams = Object.keys(scope.formData)
                        .map(function (key) {
                            var value = scope.formData[key];
                            if (value !== null && value !== undefined && value !== '') {
                                return encodeURIComponent(key) + '=' + encodeURIComponent(value);
                            }
                            return null;
                        })
                        .filter(function (param) {
                            return param !== null;
                        })
                        .join('&');
                    $http.get('/camunda/camunda-process/vpn-port-process?' + queryParams).then(response => {
                        scope.processInstances = response.data;
                        for (var i = 0; i < scope.processInstances.length; i++) {
                            scope.processInstances[i].processInstance = JSON.parse(scope.processInstances[i].processInstance);
                            scope.processInstances[i].dateCreated = new Date(scope.processInstances[i].processInstance.startTime).toLocaleDateString();

                            scope.processInstances[i].vpnId = JSON.parse(scope.processInstances[i].vpnId).filter(e => e !== undefined && e !== 'null');
                            scope.processInstances[i].portId = JSON.parse(scope.processInstances[i].portId).filter(e => e !== undefined && e !== 'null');

                            scope.processInstances[i].vpnId = (scope.processInstances[i].vpnId.length > 0) ? scope.processInstances[i].vpnId.join(', ') : '';
                            scope.processInstances[i].portId = (scope.processInstances[i].portId.length > 0) ? scope.processInstances[i].portId.join(', ') : '';

                            scope.processInstances[i].currentActivities = JSON.parse(scope.processInstances[i].taskActivities)
                                .filter(e => e.deleteReason === null);

                            scope.processInstances[i].currentActivitiesNames = scope.processInstances[i].currentActivities.map(e => e.name).join(", ");
                            scope.processInstances[i].currentActivitiesAssignees = scope.processInstances[i].currentActivities.map(e => e.assignee).join(", ");

                            var currentTasksDates = scope.processInstances[i].currentActivities.map(e => e.startTime);
                            var dateUpdated = currentTasksDates.length > 0 ? Math.max(...currentTasksDates) : undefined;
                            scope.processInstances[i].dateUpdated = (dateUpdated !== undefined) ? new Date(dateUpdated).toLocaleDateString()
                                : new Date(scope.processInstances[i].processInstance.endTime).toLocaleDateString();

                            if (scope.processInstances[i].channel === 'VPN') {
                                var servicesString = searchPropValue(scope.processInstances[i], "Services", "preModifiedAddedServices");
                                if (servicesString) {
                                    scope.processInstances[i].vpns = JSON.parse(servicesString)
                                }
                            } else {
                                var portsString = searchPropValue(scope.processInstances[i], "Ports", null);
                                if (portsString) {
                                    scope.processInstances[i].ports = JSON.parse(portsString)
                                }
                            }

                            scope.processInstances[i].inputTheProvidersNorFiles = scope.processInstances[i].inputTheProvidersNorFiles ? JSON.parse(scope.processInstances[i].inputTheProvidersNorFiles) : null;

                            scope.processInstances[i].resolutions = scope.processInstances[i].resolutions ? JSON.parse(scope.processInstances[i].resolutions) : null;
                        }

                        scope.processInstances.sort((a, b) => b.processInstance.startTime - a.processInstance.startTime);
                    })
                };

                scope.clearFilter = function () {
                    scope.formData = {
                        process: undefined,
                        channel: undefined,
                        requestType: undefined,
                        currentActivity: undefined,
                        priority: undefined,
                        dateCreated: undefined,
                        requestNumber: undefined,
                        portId: undefined,
                        vpnId: undefined
                    }
                }

                scope.sortColumn = function (column) {
                    if (scope.sortBy === column) {
                        scope.reverse = !scope.reverse;
                    } else {
                        scope.sortBy = column;
                        scope.reverse = false;
                    }
                };

                scope.itemsPerPage = 20;
                scope.currentPage = 1;

                scope.$watch('processInstances', function () {
                    if (!scope.processInstances) return;

                    scope.totalPages = Math.ceil(scope.processInstances.length / scope.itemsPerPage);
                    scope.pages = [];
                    for (var i = 1; i <= scope.totalPages; i++) {
                        scope.pages.push(i);
                    }
                    var startIndex = (scope.currentPage - 1) * scope.itemsPerPage;
                    var endIndex = startIndex + scope.itemsPerPage;
                    scope.pagedProcessInstances = [...scope.processInstances].slice(startIndex, endIndex);
                });

                scope.setCurrentPage = function (page) {
                    if (page >= 1 && page <= scope.totalPages) {
                        scope.currentPage = page;
                    }
                };

                scope.$watchGroup(['currentPage', 'sortBy', 'reverse'], function () {
                    if (!scope.processInstances) return;

                    var startIndex = (scope.currentPage - 1) * scope.itemsPerPage;
                    var endIndex = startIndex + scope.itemsPerPage;
                    var sortedData = scope.processInstances.slice(0); // Copy the data array

                    if (scope.sortBy) {
                        sortedData.sort(function (a, b) {
                            var valueA = getObjectPropertyValue(a, scope.sortBy);
                            var valueB = getObjectPropertyValue(b, scope.sortBy);
                            if (valueA < valueB) {
                                return scope.reverse ? -1 : 1;
                            } else if (valueA > valueB) {
                                return scope.reverse ? 1 : -1;
                            }
                            return 0;
                        });
                    }

                    scope.pagedProcessInstances = sortedData.slice(startIndex, endIndex);
                });

                function getObjectPropertyValue(obj, path) {
                    var properties = path.split('.');
                    var value = obj;
                    for (var i = 0; i < properties.length; i++) {
                        if (value && value.hasOwnProperty(properties[i])) {
                            value = value[properties[i]];
                        } else {
                            value = null;
                            break;
                        }
                    }
                    return value;
                }

                function searchPropValue(obj, searchString, excludeSearchString) {
                    for (var prop in obj) {
                        if (prop.includes(searchString) && prop !== excludeSearchString) {
                            return obj[prop];
                        }
                    }
                    return undefined; // Return undefined if no matching property is found
                }

                scope.openProcessCardModal = function (process) {
                    exModal.open({
                        scope: {
                            process: process,
                            addressToString: scope.addressToString,
                            downloadFile: scope.downloadFile,
                            showHistory: scope.showHistory,
                            showDiagram: scope.showDiagram
                        },
                        templateUrl: './js/partials/vpnPortProcessCardModal.html',
                        size: 'hg'
                    }).then(function (results) {
                    });
                }

                scope.addressToString = function (address) {
                    let result = "";
                    if (address) {
                        if (address.city_id) {
                            if (address.city_id.district_id) {
                                if (address.city_id.district_id.oblast_id) {
                                    if (address.city_id.district_id.oblast_id.name) {
                                        result += address.city_id.district_id.oblast_id.name + ' ';
                                    }
                                }
                                if (address.city_id.district_id.name) {
                                    result += address.city_id.district_id.name + ' ';
                                }
                            }
                            if (address.city_id.name) {
                                result += address.city_id.name + ' ';
                            }
                        }
                        if (address.street) {
                            result += address.street + ' ';
                        }
                        if (address.building) {
                            result += address.building;
                        }
                    }

                    return result;
                }

                scope.downloadFile = function(file) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + file.path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error);
                    });
                };

                scope.exportToExcel = function() {
                    var worksheet = XLSX.utils.table_to_sheet(document.getElementById('excelTable'));
                    var workbook = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');
                    var excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
                    saveAsExcel(excelBuffer, 'data.xlsx');
                };

                function saveAsExcel(buffer, filename) {
                    var blob = new Blob([buffer], { type: 'application/octet-stream' });
                    saveAs(blob, filename);
                }

                function saveAs(blob, filename) {
                    var link = document.createElement('a');
                    link.href = URL.createObjectURL(blob);
                    link.download = filename;
                    link.click();
                }
            },
            templateUrl: './js/directives/search/vpnPortSearch.html'
        };
    }]);
});