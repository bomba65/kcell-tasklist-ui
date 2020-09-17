define(['./module', 'angular', 'bpmn-viewer', 'bpmn-navigated-viewer', 'moment', 'angular-ui-bootstrap'], function (module, angular, BpmnJS, BpmnNavigatedViewer, moment) {
    'use strict';
    module.directive('jobRequest', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                jobModel: '=',
                processInstanceId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                scope.download = function (file) {
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
                scope.isFileVisible = function (file) {
                    return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
                }
                scope.hasGroup = function (group) {
                    return $rootScope.hasGroup(group);
                }
                scope.getDictNameById = function (dictionary, id) {
                    return _.find(dictionary, function (dict) {
                        return dict.id === id;
                    });
                }
                scope.compareDate = new Date('2019-02-05T06:00:00.000');
                scope.$watch('jobModel.requestedDate.value', function (value) {
                    scope.requestedDate = new Date(value);
                });
            },
            templateUrl: './js/directives/revision/jobRequest.html'
        };
    });
    module.directive('resolutionHistory', function ($http, $rootScope) {
        return {
            restrict: 'E',
            scope: {
                resolutions: '=',
                procDef: '='
            },
            link: function (scope, element, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error);
                    });
                }
                scope.isFileVisible = function (file) {
                    return !file.visibility || file.visibility == 'all' || (file.visibility == 'kcell' && $rootScope.hasGroup('kcellUsers'));
                }
                scope.isKcellStaff = $rootScope.hasGroup('kcellUsers');
            },
            templateUrl: './js/partials/resolutions.html'
        };
    });
    module.directive('invoiceDetail', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                invoice: '=',
                print: '=',
                search: '='
            },
            link: function (scope, element, attrs) {
                scope.catalogs = {};
                scope.processInstanceId = undefined;
                scope.pkey = undefined;
                scope.table = undefined;
                scope.selectedWorks = [];
                var selectedWorksMap = {};
                var Big;
                if (window.require) {
                    Big = require('big-js')
                }
                scope.totalPrice = Big('0.0');

                function transformToArray() {
                    for (var propt in scope.invoice.selectedRevisions.value) {
                        _.forEach(scope.invoice.selectedRevisions.value[propt].works, function (work, key) {
                            var processDetailCopy = angular.copy(scope.invoice.selectedRevisions.value[propt]);
                            processDetailCopy.quantity = work.quantity;
                            processDetailCopy.relatedSites = work.relatedSites;
                            angular.forEach(work.relatedSites, function (rs) {
                                processDetailCopy.relatedSitesNames = processDetailCopy.relatedSitesNames ? (processDetailCopy.relatedSitesNames + ', ' + rs.site_name) : rs.site_name;
                            });
                            delete processDetailCopy.works;

                            if (key > 0) {
                                processDetailCopy.alreadyDelayInfoShown = true;
                            }

                            if (scope.invoice.selectedRevisions.value[propt].workPrices) {
                                var prices = _.find(scope.invoice.selectedRevisions.value[propt].workPrices, function (price) {
                                    return price.sapServiceNumber === work.sapServiceNumber;
                                });
                                processDetailCopy.workPrices = prices;
                                scope.totalPrice = scope.totalPrice.plus(Number(prices.total));
                            }

                            if (!selectedWorksMap[work.sapServiceNumber]) {
                                selectedWorksMap[work.sapServiceNumber] = [processDetailCopy];
                            } else {
                                selectedWorksMap[work.sapServiceNumber].push(processDetailCopy);
                            }
                        });
                    }
                    _.forEach(selectedWorksMap, function (value, key) {
                        scope.selectedWorks.push({key: key, value: value});
                    });
                    scope.total = scope.totalPrice.toFixed(2);
                }

                scope.getCatalogs = function () {
                    scope.selectedWorks = [];
                    $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
                        function (result) {
                            angular.extend(scope.catalogs, result.data);
                        },
                        function (error) {
                            console.log(error.data);
                        }
                    );
                    $timeout(function () {
                        transformToArray();
                    }, 1500);
                }

                scope.calculateBasePriceByQuantity = function (work) {
                    var unitWorkPrice = Big(work.unitWorkPrice);
                    var quantity = Big(work.quantity);
                    var result = unitWorkPrice.times(quantity);
                    return result.toFixed(2).replace('.', ',');
                }

                scope.toggleProcessView = function (processInstanceId, key, table) {
                    if (scope.processInstanceId === processInstanceId && scope.pkey === key && scope.table === table) {
                        scope.processInstanceId = undefined;
                        scope.pkey = undefined;
                        scope.table = undefined;
                        scope.jobModel = {};
                    } else {
                        scope.jobModel = {};
                        scope.processInstanceId = processInstanceId;
                        scope.pkey = key;
                        scope.table = table;
                        scope.jobModel.state = 'ACTIVE';
                        $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: '/camunda/api/engine/engine/default/task?processInstanceId=' + processInstanceId,
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
                                    });
                                }
                                $http.get('/camunda/api/engine/engine/default/history/variable-instance?deserializeValues=false&processInstanceId=' + processInstanceId).then(
                                    function (result) {
                                        var workFiles = [];
                                        result.data.forEach(function (el) {
                                            scope.jobModel[el.name] = el;
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                scope.jobModel[el.name].contentUrl = '/camunda/api/engine/engine/default/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                scope.jobModel[el.name].value = JSON.parse(el.value);
                                            }
                                            if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                workFiles.push(el);
                                            }
                                        });
                                        workFiles.forEach(function (file) {
                                            var workIndex = file.name.split('_')[1];
                                            if (!scope.jobModel.jobWorks.value[workIndex].files) {
                                                scope.jobModel.jobWorks.value[workIndex].files = [];
                                            }
                                            scope.jobModel.jobWorks.value[workIndex].files.push(file);
                                        });
                                        scope.jobModel.tasks = processInstanceTasks;
                                        angular.extend(scope.jobModel, scope.catalogs);
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
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
                scope.orderWorks = function (work) {
                    return Number(work.key.replace("RO", "100"));
                }
                scope.orderYear = function (w) {
                    var substring = w.businessKey.substr(0, w.businessKey.lastIndexOf("-"));
                    return Number(substring.substr(substring.lastIndexOf("-") + 1, substring.length));
                }
                scope.orderBusinessKey = function (w) {
                    return Number(w.businessKey.substr(w.businessKey.lastIndexOf("-") + 1, w.businessKey.length));
                }
                scope.commentDelayChanges = function (processInstanceId, businessKey) {
                    var comment = '';
                    var delayedEdited = false;
                    if (scope.invoice.delayChanges.value[processInstanceId] && scope.invoice.delayChanges.value[processInstanceId].length > 0) {
                        if (scope.invoice.delayChanges.value[processInstanceId][scope.invoice.delayChanges.value[processInstanceId].length - 1].comment) {
                            comment = scope.invoice.delayChanges.value[processInstanceId][scope.invoice.delayChanges.value[processInstanceId].length - 1].comment;
                        }
                        delayedEdited = true;
                    } else if (scope.invoice.defaultDelayComments.value[processInstanceId]) {
                        comment = scope.invoice.defaultDelayComments.value[processInstanceId];
                    }

                    exModal.open({
                        scope: {
                            comment: comment,
                            processInstanceId: processInstanceId,
                            businessKey: businessKey
                        },
                        templateUrl: './js/partials/invoice/comment.html',
                        size: 'md'
                    }).then(function (comment) {
                        if (delayedEdited) {
                            var lastline = scope.invoice.delayChanges.value[processInstanceId][scope.invoice.delayChanges.value[processInstanceId].length - 1];
                            lastline.comment = comment;
                            scope.invoice.delayChanges.value[processInstanceId][scope.invoice.delayChanges.value[processInstanceId].length - 1] = lastline;
                        } else {
                            scope.invoice.defaultDelayComments.value[processInstanceId] = comment;
                        }
                    });
                }
                scope.exportToExcel = function (table) {
                    var tbl = document.getElementById(table);
                    var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});

                    var wb = XLSX.utils.book_new();
                    XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');

                    return XLSX.writeFile(wb, (scope.invoice.invoiceNumber.value ? scope.invoice.invoiceNumber.value : scope.invoice.monthActNumber.value) + '-' + table + '.xlsx');
                }
            },
            templateUrl: './js/directives/invoice/invoiceDetail.html'
        };
    });
    module.directive('infoBulksms', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                bulksmsInfo: '='
            },
            link: function (scope, el, attrs) {
                // $http.get("/camunda/api/engine/engine/default/user/" + scope.bulksmsInfo.starter + "/profile").then(function (e) {
                //                 scope.bulksmsInfo.starter = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                //             });
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/bulksms/infoBulksms.html'
        };
    }]);
    module.directive('camWidgetBpmnViewer', ['$q', '$document', '$compile', '$location', '$rootScope', 'search', 'debounce', function ($q, $document, $compile, $location, $rootScope, search, debounce) {

        return {
            scope: {
                diagramData: '=',
                control: '=?',
                disableNavigation: '&',
                onLoad: '&',
                onClick: '&',
                onMouseEnter: '&',
                onMouseLeave: '&'
            },

            templateUrl: './js/directives/cam-widget-bpmn-viewer.html',

            link: function (scope, $element) {

                var definitions;

                scope.grabbing = false;

                // parse boolean
                scope.disableNavigation = scope.$eval(scope.disableNavigation);

                // --- CONTROL FUNCTIONS ---
                scope.control = scope.control || {};

                scope.control.highlight = function (id) {
                    canvas.addMarker(id, 'highlight');

                    $element.find('[data-element-id="' + id + '"]>.djs-outline').attr({
                        rx: '14px',
                        ry: '14px'
                    });
                };

                scope.control.clearHighlight = function (id) {
                    canvas.removeMarker(id, 'highlight');
                };

                scope.control.isHighlighted = function (id) {
                    return canvas.hasMarker(id, 'highlight');
                };

                // config: text, tooltip, color, position
                scope.control.createBadge = function (id, config) {
                    var overlays = viewer.get('overlays');

                    var htmlElement;
                    if (config.html) {
                        htmlElement = config.html;
                    } else {
                        htmlElement = document.createElement('span');
                        if (config.color) {
                            htmlElement.style['background-color'] = config.color;
                        }
                        if (config.tooltip) {
                            htmlElement.setAttribute('tooltip', config.tooltip);
                            htmlElement.setAttribute('tooltip-placement', 'top');
                        }
                        if (config.text) {
                            htmlElement.appendChild(document.createTextNode(config.text));
                        }
                    }

                    var overlayId = overlays.add(id, {
                        position: config.position || {
                            bottom: 0,
                            right: 0
                        },
                        show: {
                            minZoom: -Infinity,
                            maxZoom: +Infinity
                        },
                        html: htmlElement
                    });

                    $compile(htmlElement)(scope);

                    return overlayId;
                };

                // removes all badges for an element with a given id
                scope.control.removeBadges = function (id) {
                    viewer.get('overlays').remove({element: id});
                };

                // removes a single badge with a given id
                scope.control.removeBadge = function (id) {
                    viewer.get('overlays').remove(id);
                };

                scope.control.getViewer = function () {
                    return viewer;
                };

                scope.control.scrollToElement = function (element) {
                    var height, width, x, y;

                    var elem = viewer.get('elementRegistry').get(element);
                    var viewbox = canvas.viewbox();

                    height = Math.max(viewbox.height, elem.height);
                    width = Math.max(viewbox.width, elem.width);

                    x = Math.min(Math.max(viewbox.x, elem.x - viewbox.width + elem.width), elem.x);
                    y = Math.min(Math.max(viewbox.y, elem.y - viewbox.height + elem.height), elem.y);

                    canvas.viewbox({
                        x: x,
                        y: y,
                        width: width,
                        height: height
                    });
                };

                scope.control.getElement = function (elementId) {
                    return viewer.get('elementRegistry').get(elementId);
                };

                scope.control.getElements = function (filter) {
                    return viewer.get('elementRegistry').filter(filter);
                };

                scope.loaded = false;
                scope.control.isLoaded = function () {
                    return scope.loaded;
                };

                scope.control.addAction = function (config) {
                    var container = $element.find('.actions');
                    var htmlElement = config.html;
                    container.append(htmlElement);
                    $compile(htmlElement)(scope);
                };

                scope.control.addImage = function (image, x, y) {
                    return preloadImage(image)
                        .then(
                            function (preloadedElement) {
                                var width = preloadedElement.offsetWidth;
                                var height = preloadedElement.offsetHeight;
                                var imageElement = $document[0].createElementNS('http://www.w3.org/2000/svg', 'image');

                                imageElement.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', image);
                                imageElement.setAttributeNS(null, 'width', width);
                                imageElement.setAttributeNS(null, 'height', height);
                                imageElement.setAttributeNS(null, 'x', x);
                                imageElement.setAttributeNS(null, 'y', y);

                                $document[0].body.removeChild(preloadedElement);
                                canvas._viewport.appendChild(imageElement);

                                return angular.element(imageElement);
                            },
                            function (preloadedElement) {
                                $document[0].body.removeChild(preloadedElement);
                            }
                        );
                };

                function preloadImage(img) {
                    var body = $document[0].body;
                    var deferred = $q.defer();
                    var imageElement = angular.element('<img>')
                        .css('position', 'absolute')
                        .css('left', '-9999em')
                        .css('top', '-9999em')
                        .attr('src', img)[0];

                    imageElement.onload = function () {
                        deferred.resolve(imageElement);
                    };

                    imageElement.onerror = function () {
                        deferred.reject(imageElement);
                    };

                    body.appendChild(imageElement);

                    return deferred.promise;
                }

                var BpmnViewer = BpmnNavigatedViewer;
                if (scope.disableNavigation) {
                    BpmnViewer = Object.getPrototypeOf(Viewer.prototype).constructor;
                }
                var viewer = new BpmnViewer({
                    container: $element[0].querySelector('.diagram-holder'),
                    width: '100%',
                    height: '100%',
                    canvas: {
                        deferUpdate: false
                    }
                });

                var originalShow = viewer.get('overlays').show.bind(viewer.get('overlays'));
                viewer.get('overlays').show = function () {
                    viewer.get('eventBus').fire('overlays.show');
                    originalShow();
                };

                var originalHide = viewer.get('overlays').hide.bind(viewer.get('overlays'));
                viewer.get('overlays').hide = function () {
                    viewer.get('eventBus').fire('overlays.hide');
                    originalHide();
                };

                var showAgain = debounce(function () {
                    viewer.get('overlays').show();
                }, 300);

                var originalViewboxChanged = viewer.get('canvas')._viewboxChanged.bind(viewer.get('canvas'));
                var debouncedOriginal = debounce(function () {
                    originalViewboxChanged();
                    viewer.get('overlays').hide();
                    showAgain();
                }, 0);
                viewer.get('canvas')._viewboxChanged = function () {
                    debouncedOriginal();
                };


                var diagramData = null;
                var canvas = null;

                scope.$watch('diagramData', function (newValue) {
                    if (newValue) {
                        diagramData = newValue;
                        renderDiagram();
                    }
                });

                function renderDiagram() {
                    if (diagramData) {

                        scope.loaded = false;

                        var useDefinitions = (typeof diagramData === 'object');

                        var importFunction = (useDefinitions ? viewer.importDefinitions : viewer.importXML).bind(viewer);

                        importFunction(diagramData, function (err, warn) {

                            var applyFunction = useDefinitions ? function (fn) {
                                fn();
                            } : scope.$apply.bind(scope);

                            applyFunction(function () {
                                if (err) {
                                    scope.error = err;
                                    return;
                                }
                                scope.warn = warn;
                                canvas = viewer.get('canvas');
                                definitions = viewer.definitions;
                                zoom();
                                setupEventListeners();
                                scope.loaded = true;
                                scope.onLoad();
                            });
                        });
                    }
                }

                function zoom() {
                    if (canvas) {
                        var viewbox = JSON.parse(($location.search() || {}).viewbox || '{}')[definitions.id];

                        if (viewbox) {
                            canvas.viewbox(viewbox);
                        } else {
                            canvas.zoom('fit-viewport', 'auto');
                        }
                    }
                }

                var mouseReleaseCallback = function () {
                    scope.grabbing = false;
                    document.removeEventListener('mouseup', mouseReleaseCallback);
                    scope.$apply();
                };

                function setupEventListeners() {
                    var eventBus = viewer.get('eventBus');
                    eventBus.on('element.click', function (e) {
                        // e.element = the model element
                        // e.gfx = the graphical element
                        scope.onClick({element: e.element, $event: e.originalEvent});
                    });
                    eventBus.on('element.hover', function (e) {
                        scope.onMouseEnter({element: e.element, $event: e.originalEvent});
                    });
                    eventBus.on('element.out', function (e) {
                        scope.onMouseLeave({element: e.element, $event: e.originalEvent});
                    });
                    eventBus.on('element.mousedown', function () {
                        scope.grabbing = true;

                        document.addEventListener('mouseup', mouseReleaseCallback);

                        scope.$apply();
                    });
                    eventBus.on('canvas.viewbox.changed', debounce(function (e) {
                        var viewbox = JSON.parse(($location.search() || {}).viewbox || '{}');

                        viewbox[definitions.id] = {
                            x: e.viewbox.x,
                            y: e.viewbox.y,
                            width: e.viewbox.width,
                            height: e.viewbox.height
                        };

                        /*search.updateSilently({
                          viewbox: JSON.stringify(viewbox)
                        });*/

                        var phase = $rootScope.$$phase;
                        if (phase !== '$apply' && phase !== '$digest') {
                            scope.$apply(function () {
                                $location.replace();
                            });
                        } else {
                            $location.replace();
                        }
                    }, 500));
                }

                scope.zoomIn = function () {
                    viewer.get('zoomScroll').zoom(1, {
                        x: $element[0].offsetWidth / 2,
                        y: $element[0].offsetHeight / 2
                    });
                };

                scope.zoomOut = function () {
                    viewer.get('zoomScroll').zoom(-1, {
                        x: $element[0].offsetWidth / 2,
                        y: $element[0].offsetHeight / 2
                    });
                };

                scope.resetZoom = function () {
                    canvas.resized();
                    canvas.zoom('fit-viewport', 'auto');
                };

                scope.control.resetZoom = scope.resetZoom;

                scope.control.refreshZoom = function () {
                    canvas.resized();
                    canvas.zoom(canvas.zoom(), 'auto');
                };
            }
        };
    }]);
    module.directive('trackChange', function () {
        return {
            restrict: 'A',
            priority: 10000,
            link: function (scope, element, attrs, ctrl) {
                var paths = scope.myModelExpression.split('.')
                    .reduce((a, b) => [...a, [...a[a.length - 1], b]], [[]])
                    .map(e => e.join('.'))
                    .reverse()
                    .filter((e, i) => i && e.length);

                element.on('blur keyup change', function () {
                    var [originalPath, original] = paths
                        .map(e => [e, scope.$parent.$eval(e + "._original") || {}])
                        .find(e => e[1]);
                    if (original) {
                        var fieldPath = scope.myModelExpression.slice(originalPath.length + 1);
                        var originalValue = scope.$parent.$eval(fieldPath, original);
                        if (originalValue === scope.myModelValue) {
                            if (element.attr('type') === 'checkbox') {
                                element.parent().removeClass('track-change-unequal');
                            } else {
                                element.removeClass("track-change-unequal");
                            }
                        } else {
                            if (element.attr('type') === 'checkbox') {
                                element.parent().addClass('track-change-unequal');
                            } else {
                                element.addClass("track-change-unequal");
                            }
                        }
                    }
                });
            },
            require: 'ngModel',
            scope: {
                myModelExpression: '@ngModel',
                myModelValue: '=ngModel'
            },
            template: '{{myModelExpression}} = {{myModelValue}}'
        }
    });
    module.directive('requiredFile', function () {
        return {
            require: 'ngModel',
            restrict: 'A',
            link: function (scope, el, attrs, ctrl) {
                function updateValidity(fileRequired) {
                    var valid = (fileRequired && el.val() !== '') || (!fileRequired);
                    ctrl.$setValidity('validFile', valid);
                    ctrl.$setValidity('camVariableType', valid);
                }

                updateValidity(scope.$eval(attrs.requiredFile));
                scope.$watch(attrs.requiredFile, updateValidity);
                el.bind('change', function () {
                    updateValidity(scope.$eval(attrs.requiredFile));
                    scope.$apply(function () {
                        ctrl.$setViewValue(el.val());
                        ctrl.$render();
                    });
                });
            }
        }
    });
    module.directive('elasticTextarea', ['$timeout', function ($timeout) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, el) {
                el[0].setAttribute('style', 'resize:none;height:' + (el[0].scrollHeight) + 'px;overflow-y:hidden;');

                $timeout(function () {
                    el[0].style.height = 'auto';
                    el[0].style.height = (el[0].scrollHeight) + 'px';
                });

                scope.$watch(function () {
                    return el[0].value;
                }, function (value) {
                    $timeout(function () {
                        el[0].style.height = 'auto';
                        el[0].style.height = (el[0].scrollHeight) + 'px';
                    });
                }, true);
            }
        };
    }]);
    module.directive('infoFreephone', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                freephoneInfo: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/freephone/infoFreephone.html'
        };
    }]);
    module.directive('infoPbx', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                pbxInfo: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/infoPbx.html'
        };
    }]);
    module.directive('sharedSitePlan', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                planModel: '=',
                planStatus: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/sharedSitePlan.html'
        };
    }]);
    module.directive('customerInformation', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                ci: '=',
                form: '=',
                view: '=',
                readonly: "=",
                legal: "=",
                start: "="
            },
            link: function (scope, element, attrs) {
                scope.today = new Date();
                scope.today.setHours(0, 0, 0, 0);
                scope.tomorrow = new Date(scope.today);
                scope.tomorrow.setTime(scope.today.getTime() + 86400000);
                scope.ci.companyRegistrationDate = new Date();

                scope.$watch('ci', function (value) {
                    if (value) {

                        if (scope.ci.termContract) scope.ci.termContract = new Date(scope.ci.termContract);

                        if (scope.ci.termContractEnd === undefined) scope.ci.termContractEnd = true;

                        if (scope.form) {
                            scope.form.salesRepresentative.$setValidity('not_selected', true);
                            if (!scope.ci.salesRepresentativeEmail) {
                                if (scope.ci.salesRepresentativeId) scope.ci.salesRepresentativeEmail = scope.ci.salesRepresentativeId;
                                else scope.form.salesRepresentative.$setValidity('not_selected', false);
                            }
                        }
                    }
                }, true);

                scope.$watch('readonly', function (value) {
                    $timeout(function () {
                        scope.$apply();
                    });
                });

                scope.onContractDurationChange = function () {
                    scope.form.termContract.$setValidity('invalid_date', true);
                    if (scope.ci.termContractEnd) return;
                    if (scope.ci.termContract) {
                        scope.ci.termContract = new Date(scope.ci.termContract);
                        if (scope.ci.termContract.getTime() - scope.today.getTime() < 86400000) {
                            scope.form.termContract.$setValidity('invalid_date', false);
                        }
                    } else scope.form.termContract.$setValidity('invalid_date', false);
                };

                scope.getUser = function (val) {
                    scope.ci.salesRepresentativeEmail = null;
                    scope.form.salesRepresentative.$setValidity('not_selected', false);
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%')).then(
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
                                    });
                                } else {
                                    return [];
                                }
                            });

                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%')).then(
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

                scope.userSelected = function ($item) {
                    console.log(">>> ", $item.email);
                    scope.ci.salesRepresentativeEmail = $item.email;
                    scope.ci.salesRepresentative = $item.name;
                    scope.form.salesRepresentative.$setValidity('not_selected', true);
                };
            },
            templateUrl: './js/directives/PBX/customerInformation.html'
        };
    });
    module.directive('technicalSpecifications', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                ts: '=',
                form: '=',
                view: '=',
                readonly: '='
            },
            link: function (scope, element, attrs) {
                if (!scope.ts.intenationalCallAccess) scope.ts.intenationalCallAccess = "No";
                if (!scope.ts.pbxType) scope.ts.pbxType = "Цифровая АТС";
            },
            templateUrl: './js/directives/PBX/technicalSpecifications.html'
        };
    });
    module.directive('sipProtocol', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                sip: '=',
                form: '=',
                view: '=',
                readonly: '='
            },
            link: function (scope, element, attrs) {
                if (!scope.sip.vpnTuning) scope.sip.vpnTuning = "No";
            },
            templateUrl: './js/directives/PBX/sipProtocol.html'
        };
    });
    module.directive('e1Protocol', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                e1: '=',
                form: '=',
                view: '=',
                readonly: '='
            },
            link: function (scope, element, attrs) {
            },
            templateUrl: './js/directives/PBX/e1Protocol.html'
        };
    });
    module.directive('sipDirectProtocol', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                dir: '=',
                form: '=',
                view: '=',
                readonly: '='
            },
            link: function (scope, element, attrs) {
                if (!scope.dir.signalingPort) scope.dir.signalingPort = '5060';
                if (!scope.dir.minChannelCapacity) scope.dir.minChannelCapacity = 0;
                scope.minChannelCapacityChanged = function () {
                    scope.dir.minChannelCapacity = 0;
                    if (scope.dir.preferredCoding && scope.dir.sessionCount) {
                        scope.dir.minChannelCapacity = scope.dir.sessionCount * (scope.dir.preferredCoding == 'g711' ? 87.2 : 32.2);
                        scope.dir.minChannelCapacity = Math.floor(scope.dir.minChannelCapacity);
                    }
                }
                scope.floor = function (x) {
                    return Math.floor(x);
                }
            },
            templateUrl: './js/directives/PBX/sipDirectProtocol.html'
        };
    });
    module.directive('vpnTunnel', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {},
            link: function (scope, element, attrs) {
            },
            templateUrl: './js/directives/PBX/vpnTunnel.html'
        };
    });
    module.directive('pbxRichText', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                disabled: '=',
                minHeight: '=',
                processId: '=',
                taskId: '=',
                templates: '='
            },
            link: function (scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function (file, path, tmp) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/' + tmp + 'put/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        $http.put(response.data, file, {headers: {'Content-Type': undefined}}).then(
                            function () {
                                $http({
                                    method: 'GET',
                                    url: '/camunda/uploads/' + tmp + 'get/' + path,
                                    transformResponse: []
                                }).then(function (response) {
                                    var image = $('<img>').attr('src', response.data);
                                    element.summernote("insertNode", image[0]);
                                }, function (error) {
                                    console.log(error.data);
                                });
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }, function (error) {
                        console.log(error.data);
                    });
                };

                let toolbarList = [
                    ['style', ['style']],
                    ['font', ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript']],
                    ['fontname', ['fontsize', 'fontname']],
                    ['color', ['color']],
                    ['para', ['ul', 'ol', 'paragraph', 'height']],
                    ['table', ['table']],
                    ['insert', ['link', 'unlink', 'picture', 'video']],
                    ['view', ['fullscreen', 'codeview', 'help']],
                ];

                if (scope.templates && Object.keys(scope.templates).length) toolbarList[6][1].push('template');
                if (scope.disabled) toolbarList = [];

                element.summernote({
                    focus: false,
                    disableResizeEditor: true,
                    minHeight: scope.disabled ? 30 : (scope.minHeight ? scope.minHeight : 300),
                    callbacks: {
                        onChange: function (content) {
                            if (content === '<p><br></p>') content = '';
                            scope.data = content;
                            if (!scope.$$phase && !$rootScope.$$phase) {
                                scope.$apply();
                            }
                        },
                        onImageUpload: function (files) {
                            for (var file of files) {
                                if (scope.processId && scope.taskId) {
                                    uploadImage(file, scope.processId + '/' + scope.taskId + '/' + file.name, '');
                                } else {
                                    uploadImage(file, uuid + '/' + file.name, 'tmp/');
                                }
                            }
                        },
                        onMediaDelete: function (target) {
                            console.log("deleted => ", target);
                        }
                    },
                    toolbar: toolbarList,
                    template: {
                        path: '/summernote-templates',
                        list: scope.templates
                    },
                    popover: {
                        link: []
                    }
                });
                $('.note-statusbar').hide();
                $('.note-status-output').hide();
                $('.note-editor .link-dialog .checkbox').hide();

                if (scope.disabled) {
                    element.summernote('disable');
                    $('.note-editor > .note-editing-area').css('background', '#eee');
                    $('.note-editor > .note-editing-area > .note-editable').css('padding', '4px 10px 0 10px');
                }

                scope.$watch('data', function (value) {
                    if (value && value.length) {
                        if (value !== element.summernote('code')) {
                            // Workaround to force links open in new window
                            var html = document.createElement('div');
                            html.innerHTML = value;
                            html.querySelectorAll('a').forEach(function (link) {
                                $(link).attr('target', '_blank');
                            });
                            element.summernote('code', html.innerHTML);
                        }
                    }
                }, true);
            }
        };
    });
    module.directive('uatProcess', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                uatModel: '=',
                print: '=',
                bk: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/tmp/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                }
            },
            templateUrl: './js/directives/uatProcess.html'
        };
    }]);
    module.directive('demandGeneral', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                technical: '=',
                actualLaunch: '='
            },
            link: function (scope, element, attrs) {
                scope.datePickerMinDate = new Date();
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};

                        if (scope.data.plannedLaunch) scope.data.plannedLaunch = new Date(scope.data.plannedLaunch);
                        if (scope.data.actualLaunch) scope.data.actualLaunch = new Date(scope.data.actualLaunch);
                        if (!scope.data.technicalAnalysis) scope.data.technicalAnalysis = false;

                        if (!scope.data.demandOwner) {
                            scope.data.demandOwner = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name + "/profile").then(function (result) {
                                if (result.data && result.data.firstName && result.data.lastName && scope.data.demandOwner === $rootScope.authentication.name) {
                                    scope.data.demandOwner = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                $http.get('/camunda/catalogs/api/get/Demand/name/' + encodeURIComponent('Initiative type')).then(function (result) {
                    if (result && result.data) {
                        scope.initativeTypeOptions = result.data.data;
                    }
                });

                $http.get('/camunda/catalogs/api/get/Demand/name/' + encodeURIComponent('Demand type')).then(function (result) {
                    if (result && result.data) {
                        scope.demandTypeOptions = result.data.data;
                    }
                });

                $http.get('/camunda/catalogs/api/get/Demand/name/' + encodeURIComponent('Activity type')).then(function (result) {
                    if (result && result.data) {
                        scope.activityTypeOptions = result.data.data;
                    }
                });

                scope.getUser = function (val) {
                    scope.data.demandSupervisorId = null;
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%')).then(
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
                            //return usersByFirstName;
                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%')).then(
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

                scope.userSelected = function (item) {
                    scope.data.demandSupervisorId = item.id;
                    scope.data.demandSupervisor = item.name;
                };
            },
            templateUrl: './js/directives/demand/general.html'
        };
    });
    module.directive('demandDetails', function ($rootScope, $http, $sce, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                catalog: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function (scope, element, attrs) {
                scope.deliverableHidden = null;
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.productOffers) scope.data.productOffers = [];
                        if (!scope.data.deliverable) scope.data.deliverable = [];
                        scope.deliverableHidden = 'something';
                        if (!scope.data.deliverable.length) scope.deliverableHidden = null;
                        if (!scope.pdCollapsed) scope.pdCollapsed = [];
                    }
                }, true);

                scope.multiselectSettings = {
                    enableSearch: true,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v'
                };
                scope.deliverableOptions = [
                    {v: 'Charging logic in BSS'},
                    {v: 'USSD channel'},
                    {v: 'Web channel'},
                    {v: 'Custom Detalization'},
                    {v: 'TV cmpaign'},
                    {v: 'Billboards'}
                ];

                // ------ Product name -----

                scope.productNameChange = function () {
                    scope.data.productVersion = 1;
                    if (scope.catalog && scope.catalog.products && scope.catalog.products.length) {
                        var elt = scope.catalog.products.find(function (e) {
                            return e.name === scope.data.productName;
                        });
                        if (elt) scope.productNameSelected(elt);
                    }
                    scope.data.productOffers = [];
                };
                scope.productNameSelected = function (option) {
                    scope.data.productName = option.name;
                    scope.data.productVersion = option.version + 1;
                };

                scope.productOfferAdd = function () {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    if (!scope.data.productOffers) scope.data.productOffers = [];
                    scope.data.productOffers.push({
                        name: '',
                        description: '',
                        version: version
                    });
                    scope.pdCollapsed.push(false);
                };

                scope.productOfferDelete = function (index) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data.productOffers.splice(index, 1);
                        scope.pdCollapsed.splice(index, 1);
                    });
                };

                scope.productOfferChange = function (index) {
                    var version = '1';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].version = version;
                    let curProductName = scope.data.productOffers[index].name;
                    if (scope.catalog && scope.catalog.offers && scope.catalog.offers.length) {
                        var elt = scope.catalog.offers.find(function (e) {
                            return e.name === curProductName;
                        });
                        if (elt) scope.productOfferSelected(elt, index, true);
                    }
                };
                scope.productOfferSelected = function (option, index, local) {
                    var version = (option.version + 1) + '';
                    if (scope.data.productVersion) version = scope.data.productVersion + '.' + version;
                    scope.data.productOffers[index].name = option.name;
                    scope.data.productOffers[index].description = option.description;
                    scope.data.productOffers[index].version = version;
                    if (!local) scope.pdCollapsed[index] = !scope.pdCollapsed[index];
                };

                scope.togglePDCollapse = function (el, index) {
                    if (el.target.classList.contains('form-control') || el.target.classList.contains('disabled-element') || el.target.classList.contains('thelabel')) return;
                    scope.pdCollapsed[index] = !scope.pdCollapsed[index];
                };
            },
            templateUrl: './js/directives/demand/details.html'
        };
    });
    module.directive('demandTargetAudience', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function (scope, element, attrs) {
                scope.audienceHidden = null;
                scope.roumingHidden = null;
                scope.audienceAll = false;
                scope.roumingAll = false;
                scope.multiselectSettings = {
                    enableSearch: true,
                    showCheckAll: false,
                    showUncheckAll: false,
                    displayProp: 'v',
                    idProp: 'v',
                    externalIdProp: 'v'
                };

                scope.audienceMultiselectEvents = {
                    onItemSelect: function (item) {
                        if (item.v === 'All') this.audienceAll = true;
                        if (this.audienceAll) scope.data.audience = scope.data.audience.filter(function (e) {
                            return e.v === 'All';
                        });
                    },
                    onItemDeselect: function (item) {
                        if (item.v === 'All') this.audienceAll = false;
                    }
                };

                scope.roumingMultiselectEvents = {
                    onItemSelect: function (item) {
                        if (item.v === 'All') this.roumingAll = true;
                        if (this.roumingAll) scope.data.rouming = scope.data.rouming.filter(function (e) {
                            return e.v === 'All';
                        });
                    },
                    onItemDeselect: function (item) {
                        if (item.v === 'All') this.roumingAll = false;
                    }
                };

                scope.audienceOptions = [
                    {v: "All"},
                    {v: "B2B"},
                    {v: "B2C"},
                    {v: "CCD"},
                    {v: "CEO"},
                    {v: "CPD"},
                    {v: "FD"},
                    {v: "HR"},
                    {v: "LD"},
                    {v: "TD"},
                    {v: "NB"}
                ];
                scope.roumingOptions = [
                    {v: 'All'},
                    {v: 'Astana and Akmola region'},
                    {v: 'Almaty and Almaty region'},
                    {v: 'Taraz and Zhambyl region'},
                    {v: 'Kyzylorda and Kyzylorda region'},
                    {v: 'Shymkent and Turkestan region'},
                    {v: 'Oskemen and East-Kazakhstan region'},
                    {v: 'Kostanai and Kostanai region'},
                    {v: 'Pavlodar and Pavlodar region'},
                    {v: 'Petropavlovsk and North-Kazakhstan region'},
                    {v: 'Aktau and Mangystau region'},
                    {v: 'Aktobe and Aktobe region'},
                    {v: 'Uralsk and West-Kazakhstan region'},
                    {v: 'Karagandy and Karagangdy region'},
                    {v: 'Atyrau and Atyrau region'}
                ];
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.audience) scope.data.audience = [];
                        if (!scope.data.rouming) scope.data.rouming = [];
                        scope.audienceHidden = 'something';
                        if (!scope.data.audience.length) scope.audienceHidden = null;
                        scope.roumingHidden = 'something';
                        if (!scope.data.rouming.length) scope.roumingHidden = null;
                    }
                }, true);
            },
            templateUrl: './js/directives/demand/targetAudience.html'
        };
    });
    module.directive('demandFuncRequirements', function ($rootScope, $http, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                processId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        // if (!scope.data.businessCases) scope.data.businessCases = [];
                        if (!scope.data.useCases) scope.data.useCases = [];
                        if (!scope.useCaseCollapsed) scope.useCaseCollapsed = [];
                    }
                });

                scope.useCaseAdd = function () {
                    scope.data.useCases.push({name: '', description: ''});
                    scope.useCaseCollapsed.push(false);
                };

                scope.useCaseDelete = function (index) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data.useCases.splice(index, 1);
                        scope.useCaseCollapsed.splice(index, 1);
                    });
                };

                scope.toggleUseCaseCollapse = function (el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.useCaseCollapsed[index] = !scope.useCaseCollapsed[index];
                };

                scope.checkEnter = function (event) {
                    if (event.keyCode === 13) event.preventDefault();
                };
            },
            templateUrl: './js/directives/demand/functionalRequirements.html'
        };
    });
    module.directive('demandNonFuncRequirements', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                    }
                });
            },
            templateUrl: './js/directives/demand/nonFunctionalRequirements.html'
        };
    });
    module.directive('demandInterfaces', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                processId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                    }
                });
                scope.collapse = {
                    UIRequirements: false,
                    CRIRequirements: false,
                    SIRequirements: false
                };
                scope.toggleCollapse = function (section) {
                    scope.collapse[section] = !scope.collapse[section];
                }
            },
            templateUrl: './js/directives/demand/interfaces.html'
        };
    });
    module.directive('demandResources', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                showprice: '='
            },
            link: function (scope, el, attrs) {

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        if (!scope.collapsed || !(scope.collapsed instanceof Array)) scope.collapsed = [];

                        scope.countTotalSum();

                        if (!scope.responsible) {
                            scope.responsible = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name + "/profile").then(function (result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.responsible = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                scope.toggleCollapse = function (el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.collapsed[index] = !scope.collapsed[index];
                };

                scope.deleteItem = function (index) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data.splice(index, 1);
                        scope.collapsed.splice(index, 1);
                        scope.countTotalSum();
                    });
                };

                scope.addItem = function () {
                    scope.data.push({
                        department: null,
                        role: null,
                        description: null,
                        quantity: null,
                        hours: null,
                        period: null,
                        existing: null,
                        pprice: null,
                        summ: null,
                        responsible: null
                    });
                    scope.collapsed.push(false);
                };

                scope.calcSumm = function (index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].hours) return;
                    if (!scope.data[index].period) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].hours * scope.data[index].period * scope.data[index].pprice;
                    scope.countTotalSum();
                };

                scope.setResponsible = function (index) {
                    scope.data[index].responsible = {
                        id: $rootScope.authentication.name,
                        fio: scope.responsible
                    };
                    scope.calcSumm(index);
                };

                scope.countTotalSum = function () {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onEmployeeTypeChange = function (index) {
                    scope.data[index].department = null;
                    scope.onDepartmentChange(index);
                };

                scope.onDepartmentChange = function (index) {
                    scope.data[index].role = null;
                    scope.onRoleChange(index);
                };

                var setExpertGroupName = function (index, expertGroupId) {
                    if (!expertGroupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + expertGroupId).then(
                        function (result) {
                            if (result.data) scope.data[index].role.expertGroupName = result.data.name;
                        },
                        function (error) {
                            toasty.error(error.data);
                        }
                    );
                };

                scope.onRoleChange = function (index, option) {
                    var row = scope.data[index];
                    row.role = option;
                    row.description = null;
                    if (!row.emplType || !row.department) return;
                    option = scope.catalogs.$meta[row.emplType].$meta[row.department].$meta[option];
                    if (option && option.description) row.description = option.description;
                    if (option && option.expertGroupId) {
                        row.expertGroupId = option.expertGroupId;
                        setExpertGroupName(index, option.expertGroupId);
                    }
                };

                $http.get('/camunda/catalogs/api/get/Demand/Resources').then(function (result) {
                    if (result && result.data) {
                        scope.catalogs = result.data.data;
                    }
                });

            },
            templateUrl: './js/directives/demand/resources.html'
        };
    });
    module.directive('demandMaterials', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                editprice: '=',
                editexisting: '=',
                purchaserGroup: "="
            },
            link: function (scope, el, attrs) {

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        if (!scope.collapsed || !(scope.collapsed instanceof Array)) scope.collapsed = [];
                        scope.countTotalSum();

                        if (!scope.userFIO) {
                            scope.userFIO = $rootScope.authentication.name;
                            $http.get("/camunda/api/engine/engine/default/user/" + $rootScope.authentication.name + "/profile").then(function (result) {
                                if (result.data && result.data.firstName && result.data.lastName) {
                                    scope.userFIO = result.data.firstName + " " + result.data.lastName;
                                }
                            });
                        }
                    }
                });

                scope.toggleCollapse = function (el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.collapsed[index] = !scope.collapsed[index];
                };

                scope.deleteItem = function (index) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data.splice(index, 1);
                        scope.collapsed.splice(index, 1);
                        scope.countTotalSum();
                    });
                };

                scope.addItem = function () {
                    scope.data.push({
                        cat1: null,
                        cat2: null,
                        cat3: null,
                        specification: null,
                        expert: null,
                        purchaser: null,
                        quantity: null,
                        measure: null,
                        existing: null,
                        currency: null,
                        pprice: null,
                        summ: null
                    });
                    scope.collapsed.push(false);
                };

                scope.calcSumm = function (index) {
                    if (!scope.data[index].quantity) return;
                    if (!scope.data[index].pprice) return;
                    scope.data[index].summ = scope.data[index].quantity * scope.data[index].pprice;
                    scope.countTotalSum();
                };

                scope.countTotalSum = function () {
                    scope.totalSumm = 0;
                    for (var d of scope.data) {
                        scope.totalSumm += d.summ;
                    }
                };

                scope.onCat1Change = function (index, option) {
                    scope.data[index].cat1 = option;
                    scope.data[index].cat2 = null;
                    scope.data[index].cat3 = null;
                    scope.data[index].purchaser = null;
                    scope.data[index].expert = null;
                };

                scope.onCat2Change = function (index, option) {
                    scope.data[index].cat2 = option;
                    scope.data[index].cat3 = null;
                    option = scope.options.levels[scope.data[index].cat1][option];
                    scope.data[index].purchaser = option.purchaser;
                    scope.data[index].expert = option.expert;
                    setPurchaserGroupName(index, option.purchaser);
                    setExpertGroupName(index, option.expert);
                };

                scope.onCat3Change = function (index, option) {
                    scope.data[index].cat3 = option;
                };

                var setPurchaserGroupName = function (index, purchaser) {
                    if (!purchaser || !purchaser.groupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + purchaser.groupId).then(
                        function (result) {
                            if (result.data) scope.data[index].purchaser.groupName = result.data.name;
                        },
                        function (error) {
                            toasty.error(error.data);
                        }
                    );
                };

                scope.setPurchaser = function (index) {
                    scope.data[index].purchaser.id = $rootScope.authentication.name;
                    scope.data[index].purchaser.fio = scope.userFIO;
                    scope.calcSumm(index);
                };

                var setExpertGroupName = function (index, expert) {
                    if (!expert || !expert.groupId) return;

                    $http.get("/camunda/api/engine/engine/default/group/" + expert.groupId).then(
                        function (result) {
                            if (result.data) scope.data[index].expert.groupName = result.data.name;
                        },
                        function (error) {
                            toasty.error(error.data);
                        }
                    );
                };

                scope.setExpert = function (index) {
                    scope.data[index].expert.id = $rootScope.authentication.name;
                    scope.data[index].expert.fio = scope.userFIO;
                    scope.calcSumm(index);
                };

                scope.options = {
                    purchaseGroup: [
                        {v: 100, t: "100 - Инфраструктура: Радио и Инфраструктура"},
                        {v: 110, t: "110 - Инфраструктура: Information Technology and Support Services"},
                        {v: 120, t: "120 - Инфраструктура: Коммутация и передача данных"},
                        {v: 130, t: "130 - Инфраструктура: Information Technology and Support Services"},
                        {v: 131, t: "131 - Операционный закуп"},
                        {v: 140, t: "140 - Продукты и услуги: Терминалы и SIM карты"},
                        {v: 150, t: "150 - Продукты и услуги: Маркетинг"},
                        {v: 170, t: "170 - Продукты и услуги: корпоративные продукты и услуги"}
                    ],
                    "levels": {
                        "_list": [
                            "DEVICES",
                            "MOBILE PHONES",
                            "INFRASTRUCTURE & CIVIL WORKS",
                            "POWER MATERIALS",
                            "IT",
                            "RAN",
                            "PRODUCTION",
                            "PROFESSIONAL SERVICES",
                            "B2B PRODUCTS",
                            "LI (COPM)",
                            "SUPPORT",
                            "CREATIVE",
                            "MARKETING COMMUNICATION CHANNELS",
                            "REAL ESTATE& FACILITY MANAGEMENT",
                            "INSTALLATION MATERIALS",
                            "FLEET MANAGEMENT",
                            "LOGISTICS",
                            "BSS",
                            "OSS",
                            "CORE NETWORK",
                            "SECURITY",
                            "TRANSPORT NETWORK",
                            "EVENTS",
                            "TRAVEL",
                            "HR",
                            "IP ROUTERS/SWITCHES",
                            "PR",
                            "SIM& STRATCH CARDS",
                            "SW DEVELOPMENT",
                            "VAS"
                        ],
                        "DEVICES": {
                            "_list": [
                                "ACCESSORIES",
                                "AFTER SALES_DEVICES",
                                "MOBILE DATA COLLECTORS/TERMINALS",
                                "MOBILE ROUTERS",
                                "MPOS TERMINALS",
                                "SET-TO-BOX"
                            ],
                            "ACCESSORIES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_accessories"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "AFTER SALES_DEVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_after_sales_devices"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MOBILE DATA COLLECTORS/TERMINALS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_mobile_data_collectors_terminals"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MOBILE ROUTERS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_mobile_routers"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MPOS TERMINALS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_mpos_terminals"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SET-TO-BOX": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_set_to_box"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "MOBILE PHONES": {
                            "_list": [
                                "AFTER SALES_MOBILE PHONES",
                                "MOBILE PHONES"
                            ],
                            "AFTER SALES_MOBILE PHONES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_after_sales_mobile_phones"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MOBILE PHONES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_mobile_phones"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "INFRASTRUCTURE & CIVIL WORKS": {
                            "_list": [
                                "AIR CONDITIONING",
                                "ANTIVANDAL CONSTRUCTION",
                                "CAMUFLAGE ",
                                "CONTAINER",
                                "FENCES",
                                "INFRASTRUCTURE CONSTRUCTION SERVICES",
                                "PROJECT SERVICES",
                                "TOWER",
                                "TOWER SERVICES"
                            ],
                            "AIR CONDITIONING": {
                                "_list": [
                                    "AIR CONDITIONERS",
                                    "AIR CONDITIONERS SPARE PARTS",
                                    "AIR CONDITIONERS CONSUMABLES - FREON",
                                    "AIR CONDITIONERS TOOLS",
                                    "HEATERS"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_air_conditioning"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "ANTIVANDAL CONSTRUCTION": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_antivandal_construction"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "CAMUFLAGE ": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_camuflage"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "CONTAINER": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_container"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FENCES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_fences"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "INFRASTRUCTURE CONSTRUCTION SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_infrastructure_construction_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PROJECT SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_project_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TOWER": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tower"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TOWER SERVICES": {
                                "_list": [
                                    "TOWER INFRASTRUCTURE SERVICES",
                                    "TOWER MATERIALS - LAMP",
                                    "TOWER MATERIALS INSTALLATION SERVICES",
                                    "EXPERTIZE SERVICES"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tower_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "POWER MATERIALS": {
                            "_list": [
                                "ALTERNATIVE ENERGY SOURCES",
                                "BATTERIES",
                                "CIRCUIT BREAKER",
                                "COUNTERS",
                                "DC POWER SYSTEM",
                                "DC POWER SYSTEM INSTALLATION SERVICES",
                                "DG",
                                "DG INSTALLATION WORKS",
                                "INVERTOR",
                                "OTHER POWER MATERIALS (SOCKETS",
                                "STABILISATOR",
                                "UPS"
                            ],
                            "ALTERNATIVE ENERGY SOURCES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_other_power_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "BATTERIES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_batteries"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "CIRCUIT BREAKER": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_circuit_breaker"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "COUNTERS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_counters"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DC POWER SYSTEM": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_dc_power_system"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DC POWER SYSTEM INSTALLATION SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_dc_power_system_installation_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DG": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_dg"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DG INSTALLATION WORKS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_other_power_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "INVERTOR": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_invertor"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OTHER POWER MATERIALS (SOCKETS": {
                                "_list": [
                                    " RECLOSER"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_other_power_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "STABILISATOR": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_stabilisator"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "UPS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ups"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "IT": {
                            "_list": [
                                "ANALYTICS/STATISTICS/BIG DATA",
                                "DB LISENCES",
                                "ERP",
                                "HW",
                                "INFORMATION SECURITY SYSTEM",
                                "MOBILE FINACIAL SERVICES",
                                "NFVI",
                                "OMNI-CHANNEL (CONTACT CENTER)",
                                "PLATFORMS",
                                "WORKSPACE SW"
                            ],
                            "ANALYTICS/STATISTICS/BIG DATA": {
                                "_list": [
                                    "BI",
                                    "NPS"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_analytics_statistics_big_data"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DB LISENCES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_db_lisences"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "ERP": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_erp"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "HW": {
                                "_list": [
                                    "Workspace",
                                    "Servers",
                                    "Storages"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_hw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "INFORMATION SECURITY SYSTEM": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_information_security_system"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MOBILE FINACIAL SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_mobile_finacial_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "NFVI": {
                                "_list": [
                                    "Virtualization layer",
                                    "SDN"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_nfvi"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OMNI-CHANNEL (CONTACT CENTER)": {
                                "_list": [
                                    "IVR",
                                    "CHATBOT"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_omni_channel"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PLATFORMS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_platforms"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "WORKSPACE SW": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_workspace_sw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "RAN": {
                            "_list": [
                                "ANTENNAS",
                                "BSC/RNC HW",
                                "BSC/RNC SW (capacity licence",
                                "NETWORK MEASUREMENT",
                                "NETWORK PLANNING ",
                                "OPTIMIZATION SERVICES",
                                "RBS HW",
                                "RBS INSTAL MAT FROM VENDOR",
                                "RBS SPARE PARTS",
                                "RBS SW",
                                "ROLLOUT SERVICES"
                            ],
                            "ANTENNAS": {
                                "_list": [
                                    "ANTENNAS COMPONENTS - SPLITTER",
                                    "CABLE & ACCESSORIES",
                                    "CONNECTORS",
                                    "FEEDER"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_antennas"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "BSC/RNC HW": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_bsc_rnc_hw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "BSC/RNC SW (capacity licence": {
                                "_list": [
                                    " features"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_bsc_rnc_sw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "NETWORK MEASUREMENT": {
                                "_list": [
                                    "INSTALLATION&IMPLEMENTATION SERVICES",
                                    "MEASUREMENT HW",
                                    "MEASUREMENT TOOL HW&SW&LISENCES - DRIVE TESTS",
                                    "MEASUREMENT TOOL UPGRADE - DRIVE TESTS",
                                    "OTHER MEASUREMENT TOOLS"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_network_measurement"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "NETWORK PLANNING ": {
                                "_list": [
                                    "DIGITAL MAPS",
                                    "INSTALLATION&IMPLEMENTATION SERVICES",
                                    "PLANNING TOOL HW&SW&LISENCES",
                                    "PLANNING TOOL UPGRADE"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_network_planning"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OPTIMIZATION SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_optimization_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RBS HW": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rbs_hw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RBS INSTAL MAT FROM VENDOR": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rbs_instal_mat_from_vendor"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RBS SPARE PARTS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rbs_spare_parts"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RBS SW": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rbs_sw"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "ROLLOUT SERVICES": {
                                "_list": [
                                    "REVISION",
                                    "ROLLOUT",
                                    "BSC/RNC"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rollout_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "PRODUCTION": {
                            "_list": [
                                "Audio record",
                                "Branding",
                                "Commercial Print",
                                "Marketing production",
                                "Photo Video production",
                                "Souvenirs"
                            ],
                            "Audio record": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_audio_record"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Branding": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_branding"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Commercial Print": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_commercial_print"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Marketing production": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_marketing_production"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Photo Video production": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_photo_video_production"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Souvenirs": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_souvenirs"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "PROFESSIONAL SERVICES": {
                            "_list": [
                                "AUDIT",
                                "CONSULTANCY",
                                "FINANCIAL SERVICES",
                                "Insurances",
                                "Research",
                                "Stardardization & certification",
                                "TESTING SERVICES_OTHER"
                            ],
                            "AUDIT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_audit"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "CONSULTANCY": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_consultancy"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FINANCIAL SERVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_financial_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Insurances": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_insurances"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Research": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_research"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Stardardization & certification": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_stardardization_certification"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TESTING SERVICES_OTHER": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_testing_services_other"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "B2B PRODUCTS": {
                            "_list": [
                                "AUTOMONITORING",
                                "B2B products - COGS"
                            ],
                            "AUTOMONITORING": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_automonitoring"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "B2B products - COGS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_b2b_products_cogs"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "LI (COPM)": {
                            "_list": [
                                "BILLING",
                                "DATA",
                                "SMS",
                                "VOICE"
                            ],
                            "BILLING": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_billing"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "DATA": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_data"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SMS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sms"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "VOICE": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_voice"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "SUPPORT": {
                            "_list": [
                                "BILLING",
                                "CORE_SUPPORT",
                                "FIELD ",
                                "IT_SUPPORT",
                                "LI (SORM)",
                                "OSS",
                                "RAN",
                                "SECURITY",
                                "TR",
                                "VAS"
                            ],
                            "BILLING": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_billing_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "CORE_SUPPORT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_core_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FIELD ": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_field_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "IT_SUPPORT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_it_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "LI (SORM)": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_li_sorm_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OSS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_oss_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RAN": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ran_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SECURITY": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_securitys_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TR": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tr_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "VAS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_vas_support"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "CREATIVE": {
                            "_list": [
                                "Brand and product creative service",
                                "Web/Digital creative"
                            ],
                            "Brand and product creative service": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_brand_and_product_creative_service"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Web/Digital creative": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_web_digital_creative"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "MARKETING COMMUNICATION CHANNELS": {
                            "_list": [
                                "BTL",
                                "Digital",
                                "direct mail",
                                "Newspapers/Magazines",
                                "OOH",
                                "Radio",
                                "TV"
                            ],
                            "BTL": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_btl"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Digital": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_digital"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "direct mail": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_direct_mail"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Newspapers/Magazines": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_newspapers_magazines"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OOH": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ooh"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Radio": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_radio"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TV": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tv"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "REAL ESTATE& FACILITY MANAGEMENT": {
                            "_list": [
                                "Building maintenance",
                                "Construction & repair",
                                "Furniture",
                                "Office supply",
                                "Shop supply",
                                "Stationary"
                            ],
                            "Building maintenance": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_building_maintenance"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Construction & repair": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_construction_repair"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Furniture": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_furniture"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Office supply": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_office_supply"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Shop supply": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_shop_supply"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Stationary": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_stationary"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "INSTALLATION MATERIALS": {
                            "_list": [
                                "CABLE MATERIALS",
                                "FEATURED INSTALLATION MATERIALS",
                                "FIXING MATERIALS",
                                "HOSE & ACCESSORIES",
                                "ISOLATION MATERIALS",
                                "LADDERS",
                                "LOCKS",
                                "PIPES",
                                "SENSORS",
                                "TOOLS FOR CIVIL WORKS"
                            ],
                            "CABLE MATERIALS": {
                                "_list": [
                                    "CABLE",
                                    "CABLE LUG",
                                    "CABLE TIE&STRIP"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_cable_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FEATURED INSTALLATION MATERIALS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_featured_installation_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FIXING MATERIALS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_fixing_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "HOSE & ACCESSORIES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_hose_accessories"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "ISOLATION MATERIALS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_isolation_materials"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "LADDERS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ladders"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "LOCKS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_locks"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PIPES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_pipes"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SENSORS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sensors"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TOOLS FOR CIVIL WORKS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tools_for_civil_works"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "FLEET MANAGEMENT": {
                            "_list": [
                                "Car maitenance",
                                "Cars",
                                "Spare parts"
                            ],
                            "Car maitenance": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_car_maitenance"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Cars": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_cars"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Spare parts": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_spare_parts"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "LOGISTICS": {
                            "_list": [
                                "Courier services",
                                "Logistic services",
                                "Transportation",
                                "Warehousing"
                            ],
                            "Courier services": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_courier_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Logistic services": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_logistic_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Transportation": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_transportation"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Warehousing": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_warehousing"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "BSS": {
                            "_list": [
                                "CRM",
                                "TESTING_BSS"
                            ],
                            "CRM": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_crm"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TESTING_BSS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_testing_bss"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "OSS": {
                            "_list": [
                                "CS",
                                "IT",
                                "PS",
                                "RAN",
                                "TN"
                            ],
                            "CS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_cs"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "IT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_it_oss"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ps"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "RAN": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ran"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "TN": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_tn"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "CORE NETWORK": {
                            "_list": [
                                "CS CORE",
                                "PS CORE"
                            ],
                            "CS CORE": {
                                "_list": [
                                    "CUDB",
                                    "MSS",
                                    "MGW",
                                    "IMS"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_cs_core"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PS CORE": {
                                "_list": [
                                    "EPC (MME",
                                    "SGSN-GGSN ",
                                    "PCRF",
                                    "CGNAT/FIREWALL"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_ps_core"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "SECURITY": {
                            "_list": [
                                "DEVICES",
                                "FACILITY",
                                "FINANCIAL  ",
                                "IT",
                                "PHYSICAL"
                            ],
                            "DEVICES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_devices"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FACILITY": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_facility"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "FINANCIAL  ": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_financial"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "IT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_it_security"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "PHYSICAL": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_physical"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "TRANSPORT NETWORK": {
                            "_list": [
                                "DIGITAL CHANNELS RENT",
                                "INTERNET",
                                "MICROWAVE",
                                "OPTIC NETWORK",
                                "SATELLITE CHANNELS RENT"
                            ],
                            "DIGITAL CHANNELS RENT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_digital_channels_rent"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "INTERNET": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_internet"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "MICROWAVE": {
                                "_list": [
                                    "MW VENDOR HW",
                                    "MW VENDOR SW"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_microwave"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "OPTIC NETWORK": {
                                "_list": [
                                    "FOCL",
                                    "PTN HW",
                                    "OPTIC MATERIALS",
                                    "DWDM"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_optic_network"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SATELLITE CHANNELS RENT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_satellite_channels_rent"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "EVENTS": {
                            "_list": [
                                "Event activities ",
                                "Rent of premises and catering"
                            ],
                            "Event activities ": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_event_activities"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Rent of premises and catering": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_rent_of_premises_catering"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "TRAVEL": {
                            "_list": [
                                "Hotel services",
                                "Travel agency services"
                            ],
                            "Hotel services": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_hotel_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Travel agency services": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_travel_agency_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "HR": {
                            "_list": [
                                "HR services",
                                "Outstaff",
                                "Recruitment",
                                "Trainings"
                            ],
                            "HR services": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_hr_services"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Outstaff": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_outstaff"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Recruitment": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_recruitment"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Trainings": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_trainings"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "IP ROUTERS/SWITCHES": {
                            "_list": [
                                "L2 SWITCHES",
                                "L3 SWITCHES",
                                "ROUTERS"
                            ],
                            "L2 SWITCHES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_l2_switches"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "L3 SWITCHES": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_l3_switches"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "ROUTERS": {
                                "_list": [
                                    "DATA CENTRE",
                                    "CORE NETWORK"
                                ],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_routers"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "PR": {
                            "_list": [
                                "PR service",
                                "Sponsorship"
                            ],
                            "PR service": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_pr_service"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "Sponsorship": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sponsorship"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "SIM& STRATCH CARDS": {
                            "_list": [
                                "Scratch cards",
                                "SIM card packaging",
                                "SIM cards"
                            ],
                            "Scratch cards": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_scratch_cards"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SIM card packaging": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sim_card_packaging"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            },
                            "SIM cards": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sim_cards"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "SW DEVELOPMENT": {
                            "_list": [
                                "SW DEVELOPMENT"
                            ],
                            "SW DEVELOPMENT": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_sw_development"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        },
                        "VAS": {
                            "_list": [
                                "VAS"
                            ],
                            "VAS": {
                                "_list": [],
                                "purchaser": {
                                    "groupId": "demand_supportive_cpd_l2_vas"
                                },
                                "expert": {
                                    "groupId": null
                                }
                            }
                        }
                    }
                }
            },
            templateUrl: './js/directives/demand/materials.html'
        };
    });
    module.directive('demandSupportiveInputs', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                onitemselect: '=',
                onitemdeselect: '=',
                hintText: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data || !(scope.data instanceof Array)) scope.data = [];
                        for (var elt of scope.data) {
                            var opt = scope.optionList.find(function (e) {
                                return e.unit == elt.unit;
                            });
                            var ind = scope.optionList.indexOf(opt), ind1 = -1, ind2 = 1;
                            scope.optionList.splice(ind, 1);
                            for (var i = 0; i < scope.data.length; i++) {
                                ind1 = optionsCopy.findIndex(function (e) {
                                    return e.unit == scope.optionList[i].unit;
                                });
                                ind2 = optionsCopy.findIndex(function (e) {
                                    return e.unit == opt.unit;
                                });
                                if (ind1 > ind2) {
                                    scope.optionList.splice(i, 0, opt);
                                    break;
                                }
                            }
                            if (ind1 < ind2) scope.optionList.splice(scope.data.length - 1, 0, opt);
                        }
                    }
                });
                scope.multiselectEvents = {
                    onItemSelect: function (item) {
                        var elt = scope.data.find(function (e) {
                            return e.unit == item.unit;
                        });
                        var opt = scope.optionList.find(function (e) {
                            return e.unit == item.unit;
                        });
                        if (elt && opt) angular.copy(opt, elt);
                        if (scope.onitemselect) scope.onitemselect(elt);
                        var ind = scope.optionList.indexOf(opt), ind1 = -1, ind2 = 1;
                        scope.optionList.splice(ind, 1);
                        for (var i = 0; i < scope.data.length; i++) {
                            ind1 = optionsCopy.findIndex(function (e) {
                                return e.unit == scope.optionList[i].unit;
                            });
                            ind2 = optionsCopy.findIndex(function (e) {
                                return e.unit == opt.unit;
                            });
                            if (ind1 > ind2) {
                                scope.optionList.splice(i, 0, opt);
                                break;
                            }
                        }
                        if (ind1 < ind2) scope.optionList.splice(scope.data.length - 1, 0, opt);
                    },
                    onItemDeselect: function (item) {
                        var elt = scope.optionList.find(function (e) {
                            return e.unit == item.unit;
                        });
                        if (scope.onitemdeselect) scope.onitemdeselect(elt);
                        var ind = scope.optionList.indexOf(elt), ind1 = -1, ind2 = 1;
                        scope.optionList.splice(ind, 1);
                        for (var i = scope.data.length; i < scope.optionList.length; i++) {
                            ind1 = optionsCopy.findIndex(function (e) {
                                return e.unit == scope.optionList[i].unit;
                            });
                            ind2 = optionsCopy.findIndex(function (e) {
                                return e.unit == elt.unit;
                            });
                            if (ind1 > ind2) {
                                scope.optionList.splice(i, 0, elt);
                                break;
                            }
                        }
                        if (ind1 < ind2) scope.optionList.push(elt);
                    },
                    onDeselectAll: function () {
                        for (var item of scope.data) {
                            var elt = scope.optionList.find(function (e) {
                                return e.unit == item.unit;
                            });
                            if (scope.onitemdeselect) scope.onitemdeselect(elt);
                        }
                        scope.optionList = _.map(optionsCopy, _.clone);
                    }
                };
                scope.multiselectSettings = {
                    enableSearch: true,
                    showCheckAll: false,
                    showUncheckAll: true,
                    displayProp: 'unit',
                    idProp: 'unit',
                    externalIdProp: 'unit',
                    template: `{{getPropertyForObject(option, settings.displayProp)}} <span ng-if="option.form == 'HR' || option.form == 'CPD' || option.form == 'WH'" title="Необходимо заполнить форму для {{option.form == 'HR'?'ресурсов':'материалов'}} Supportive request form for {{option.form == 'WH'?'Centralized Procurement Department':option.unit}}" style="color: #682d86" class="glyphicon glyphicon-question-sign"></span>`
                };
                scope.optionList = [
                    {
                        form: "TD",
                        unit: "TD - Network Economics Unit",
                        groupId: "demand_supportive_td_network_economics_unit"
                    },
                    {form: "HR", unit: "Human Resources Department", groupId: "demand_supportive_hr"},
                    {form: "CPD", unit: "Centralized Procurement Department"},
                    {
                        form: "WH",
                        unit: "Warehouse and Logistics Section",
                        groupId: "demand_supportive_warehouse_and_logistics_section"
                    },
                    {
                        form: "BI",
                        unit: "CCD - Business Intelligence",
                        groupId: "demand_supportive_ccd_business_intelligence"
                    },
                    {
                        form: "BI",
                        unit: "B2C - Business Intelligence",
                        groupId: "demand_supportive_b2c_business_intelligence_"
                    },
                    {
                        form: "BI",
                        unit: "B2B - Business Intelligence",
                        groupId: "demand_supportive_b2b_business_intelligence"
                    },
                    {
                        form: "BI",
                        unit: "TD - Business Analytics Solutions Unit",
                        groupId: "demand_supportive_td_business_analytics_solutions_unit"
                    },
                    {
                        form: "LD",
                        unit: "LD - Government Relations Unit",
                        groupId: "demand_supportive_ld_government_relations_unit"
                    },
                    {
                        form: "LD",
                        unit: "LD - Contract Management Unit",
                        groupId: "demand_supportive_ld_contract_management_unit"
                    },
                    {
                        form: "LD",
                        unit: "LD - Commercial Expertise Unit",
                        groupId: "demand_supportive_ld_commercial_expertise_unit"
                    },
                    {
                        form: "LD",
                        unit: "LD - Litigation and Investigations Unit",
                        groupId: "demand_supportive_ld_litigation_and_investigations_unit"
                    },
                    {form: "Other", unit: "FD - Debt Control Unit", groupId: "demand_supportive_fd_debt_control_unit"},
                    {
                        form: "Other",
                        unit: "FD - Internal and Financial Control Unit",
                        groupId: "demand_supportive_fd_internal_and_financial_control_unit"
                    },
                    {
                        form: "Other",
                        unit: "FD - Scoring and Credit Control Unit",
                        groupId: "demand_supportive_fd_scoring_and_credit_control_unit"
                    },
                    {form: "Other", unit: "FD - Tax Unit", groupId: "demand_supportive_fd_tax_unit"},
                    {
                        form: "Other",
                        unit: "FD - Fraud Management and Revenue Assurance Section",
                        groupId: "demand_supportive_fd_fraud_and_revenue_assurance_section"
                    },
                    {
                        form: "Other",
                        unit: "FD - Real Estate Section",
                        groupId: "demand_supportive_fd_real_estate_section"
                    },
                    {
                        form: "Other",
                        unit: "FD - Physical and Economical Security Unit",
                        groupId: "demand_supportive_fd_physical_and_economical_security_unit"
                    },
                    {
                        form: "Other",
                        unit: "HR - Health and Safety Unit",
                        groupId: "demand_supportive_hr_health_and_safety_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Network Operations Unit",
                        groupId: "demand_supportive_td_network_operations_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Service Operations Unit",
                        groupId: "demand_supportive_td_service_operations_unit"
                    },
                    {form: "TD", unit: "TD - IT Operations Unit", groupId: "demand_supportive_td_it_operations_unit"},
                    {form: "TD", unit: "TD - Control Center Unit", groupId: "demand_supportive_td_control_center_unit"},
                    {
                        form: "TD",
                        unit: "TD - Operation Support Unit",
                        groupId: "demand_supportive_td_operation_support_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Consumer Solutions Project Management Unit",
                        groupId: "demand_supportive_td_consumer_solution_project_management_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Delivery Management Unit",
                        groupId: "demand_supportive_td_delivery_management_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Enterprise Solutions Project Management Unit",
                        groupId: "demand_supportive_td_enterprise_solution_project_management_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Site and Facility Management Unit",
                        groupId: "demand_supportive_td_site_and_facility_management_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Access Network Rollout Unit",
                        groupId: "demand_supportive_td_access_network_rollout_unit"
                    },
                    {form: "TD", unit: "TD - Access Network Unit", groupId: "demand_supportive_td_access_network_unit"},
                    {form: "TD", unit: "TD - Voice Network Unit", groupId: "demand_supportive_td_voice_network_unit"},
                    {form: "TD", unit: "TD - Data Network Unit", groupId: "demand_supportive_td_data_network_unit"},
                    {form: "TD", unit: "TD - VAS platforms Unit", groupId: "demand_supportive_td_vas_platforms_unit"},
                    {
                        form: "TD",
                        unit: "TD - Transmission Network Unit",
                        groupId: "demand_supportive_td_transmission_network_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Information Security Unit",
                        groupId: "demand_supportive_td_information_security_unit"
                    },
                    {form: "TD", unit: "TD - Enterprise IT Unit", groupId: "demand_supportive_td_enterprise_it_unit"},
                    {form: "TD", unit: "TD - IT Platforms Unit", groupId: "demand_supportive_td_it_platforms_unit"},
                    {
                        form: "TD",
                        unit: "TD - Billing and Charging Unit",
                        groupId: "demand_supportive_td_billing_and_charging_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - IT Architecture and Delivery Unit",
                        groupId: "demand_supportive_td_it_architecture_and_delivery_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Consumer Solutions Development Unit",
                        groupId: "demand_supportive_td_consumer_solutions_development_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Software Development Unit",
                        groupId: "demand_supportive_td_software_development_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Process Design and Automation Unit",
                        groupId: "demand_supportive_td_process_design_and_automation_unit"
                    },
                    {
                        form: "TD",
                        unit: "TD - Enterprise Solutions Development Unit",
                        groupId: "demand_supportive_td_enterprise_solutions_development_unit"
                    },
                    {
                        form: "Other",
                        unit: "B2C - Consumer Marketing Section",
                        groupId: "demand_supportive_b2c_consumer_marketing_section"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Enterprise Marketing Section",
                        groupId: "demand_supportive_b2b_enterprise_marketing_section"
                    },
                    {
                        form: "Other",
                        unit: "B2C - Interconnect and Roaming Section",
                        groupId: "demand_supportive_b2c_interconnect_and_roaming_section"
                    },
                    {form: "Other", unit: "FD - Reporting unit", groupId: "demand_supportive_fd_reporting_unit"},
                    {
                        form: "Other",
                        unit: "FD - Accounting Section",
                        groupId: "demand_supportive_fd_accounting_section"
                    },
                    {form: "Other", unit: "FD - Treasury Section", groupId: "demand_supportive_fd_treasury_section"},
                    {
                        form: "Other",
                        unit: "CCD - Customer Relations and Experience Section",
                        groupId: "demand_supportive_ccd_customer_relations_and_experience_section"
                    },
                    {
                        form: "Other",
                        unit: "CCD - Remote Sales and Service Section",
                        groupId: "demand_supportive_ccd_remote_sales_and_service_section"
                    },
                    {
                        form: "Other",
                        unit: "NB - Business Development Section",
                        groupId: "demand_supportive_nb_business_development_section"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Products Delivery Management Unit",
                        groupId: "demand_supportive_b2b_products_delivery_management_unit"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Large Accounts Development Section",
                        groupId: "demand_supportive_b2b_large_accounts_development_section"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Products Development Section",
                        groupId: "demand_supportive_b2b_products_development_section"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Small and Medium Accounts Development Section",
                        groupId: "demand_supportive_b2b_small_medium_accounts_development_section"
                    },
                    {
                        form: "Other",
                        unit: "B2B - Strategic Accounts Development Section",
                        groupId: "demand_supportive_b2b_strategic_accounts_development_section"
                    },
                    {
                        form: "Other",
                        unit: "CEO - Internal Audit Section",
                        groupId: "demand_supportive_ceo_internal_audit_section"
                    }
                ];

                var optionsCopy = _.map(scope.optionList, _.clone);
            },
            templateUrl: './js/directives/demand/supportiveInputs.html'
        };
    });
    module.directive('demandBusinessCase', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showBanchmark: '=',
                onExcelImport: '=',
                editableQualitative: '=',
            },
            link: function (scope, element, attrs) {

                scope.selects = {
                    strategicGoal: [
                        'Revenue growth',
                        'Cost optimisation',
                        'Business continuity',
                        'Regulatory',
                        'Regular expenses'
                    ],
                    strategyFit: [
                        'Values through superior network connectivity',
                        'Customer loyalty through convergence',
                        'Competitive operations',
                        'Growth in adjacencies',
                        'Regular expenses'
                    ],
                    businessPriority: [
                        'A - Delayed project realization would have no impact on the business processes.',
                        'B - The delayed project realization would affect the business processes.',
                        'C - The delayed project realization would stop the business processes.'
                    ],
                    opActivitiesImpact: [
                        'A - The delayed project realization would not affect the daily operational activities.',
                        'B - The delayed project realization would affect the daily operational activities.',
                        'C - The delayed project realization would significantly hinder important daily operational activities.'
                    ]
                };

                scope.onItemSelect = function (index, option) {
                    scope.data[index] = option;
                    scope.onChange();
                };

                function initData() {
                    scope.data = {
                        general: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: []
                        },
                        cashFlow: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: [],
                            revenuesTotal: {},
                            capexesTotal: {},
                            othersTotal: {},
                            incomeTotal: {}
                        },
                        accurals: {
                            revenues: [],
                            capexes: [],
                            others: [],
                            income: [],
                            revenuesTotal: {},
                            capexesTotal: {},
                            othersTotal: {},
                            incomeTotal: {}
                        },
                        firstYear: (new Date()).getFullYear() + 1,
                        benchmark: {
                            minPP: 0.8,
                            maxROI: 94,
                            maxNPV: 8205418520.33,
                            maxPL: 2420366789.82,
                            maxCF: 2420366789.82,
                            wacc: 13.6
                        },
                        irr: 0.0
                    };

                    scope.onChange();
                }

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) initData();
                        if (!scope.data.general) {
                            scope.data.general = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: []
                            };
                        }
                        if (!scope.data.cashFlow) {
                            scope.data.cashFlow = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: [],
                                revenuesTotal: {},
                                capexesTotal: {},
                                othersTotal: {},
                                incomeTotal: {}
                            };
                        }
                        if (!scope.data.accurals) {
                            scope.data.accurals = {
                                revenues: [],
                                capexes: [],
                                others: [],
                                income: [],
                                revenuesTotal: {},
                                capexesTotal: {},
                                othersTotal: {},
                                incomeTotal: {}
                            };
                        }
                        if (!scope.data.firstYear) scope.data.firstYear = (new Date()).getFullYear() + 1;
                        if (!scope.data.benchmark) {
                            scope.data.benchmark = {
                                minPP: 0.8,
                                maxROI: 94,
                                maxNPV: 8205418520.33,
                                maxPL: 2420366789.82,
                                maxCF: 2420366789.82,
                                wacc: 13.6
                            };
                        }
                        if (!scope.data.irr) scope.data.irr = 0.0;

                        scope.onChange();
                    }
                });

                var strategyFitPercentage = function () {
                    if (!scope.data.strategyFit) return 0;

                    if (scope.data.strategyFit.startsWith('Values')) return 1;
                    else if (scope.data.strategyFit.startsWith('Customer')) return 1;
                    else if (scope.data.strategyFit.startsWith('Competitive')) return 1;
                    else if (scope.data.strategyFit.startsWith('Growth')) return 0.8;
                    else return 0;
                };

                var businessPriorityPercentage = function () {
                    if (!scope.data.businessPriority) return 0;

                    if (scope.data.businessPriority.startsWith('C')) return 1;
                    else if (scope.data.businessPriority.startsWith('B')) return 0.5;
                    else return 0;
                };

                var operationalActivityPercentage = function () {
                    if (!scope.data.opActivitiesImpact) return 0;

                    if (scope.data.opActivitiesImpact.startsWith('C')) return 1;
                    else if (scope.data.opActivitiesImpact.startsWith('B')) return 0.5;
                    else return 0;
                };

                scope.xlsxSelected = function (el) {
                    if (scope.onExcelImport) scope.onExcelImport(el);
                    $('#loaderDiv').show();
                    var reader = new FileReader();
                    reader.onload = function (e) {
                        var wb = XLSX.read(e.target.result, {type: "binary"});
                        var ind = wb.SheetNames.indexOf('General summary');
                        if (ind === -1) ind = 1;
                        var sheet = XLSX.utils.sheet_to_json(wb.Sheets[wb.SheetNames[ind]], {
                            blankrows: true,
                            header: "A"
                        });
                        if (sheet) sheet.unshift({});
                        processSheet(sheet);
                        $(el).val('');
                        $('#loaderDiv').hide();
                    };
                    reader.onerror = function (e) {
                        $('#loaderDiv').hide();
                        console.log("reading file error:", e);
                    };
                    if (el && el.files[0]) reader.readAsBinaryString(el.files[0]);
                    else {
                        $(el).val('');
                        $('#loaderDiv').hide();
                    }
                };

                var processSheet = function (sheet) {

                    initData();

                    // Strategy Goal
                    if (sheet.length > 10 && sheet[10]['G'] && sheet[10]['G'] !== 'n/a') {
                        if (sheet[10]['G'].startsWith('Revenue')) scope.data.strategicGoal = 'Revenue growth';
                        else if (sheet[10]['G'].startsWith('Cost')) scope.data.strategicGoal = 'Cost optimisation';
                        else if (sheet[10]['G'].startsWith('Business')) scope.data.strategicGoal = 'Business continuity';
                        else if (sheet[10]['G'].startsWith('Regulatory')) scope.data.strategicGoal = 'Regulatory';
                        else if (sheet[10]['G'].startsWith('Regular')) scope.data.strategicGoal = 'Regular expenses';
                    }

                    // Strategy Fit
                    if (sheet.length > 23 && sheet[23]['G'] && sheet[23]['G'] !== 'n/a') {
                        if (sheet[23]['G'].startsWith('A'))
                            scope.data.strategyFit = 'Values through superior network connectivity';
                        else if (sheet[23]['G'].startsWith('B'))
                            scope.data.strategyFit = 'Customer loyalty through convergence';
                        else if (sheet[23]['G'].startsWith('C'))
                            scope.data.strategyFit = 'Competitive operations';
                        else if (sheet[23]['G'].startsWith('D'))
                            scope.data.strategyFit = 'Growth in adjacencies';
                        else
                            scope.data.strategyFit = 'Regular expenses';
                    }

                    if (sheet.length > 24 && sheet[24]['C'] && parseFloat(sheet[24]['C'])) {
                        scope.data.irr = parseFloat(sheet[24]['C']) * 100.0;
                    }

                    // Define impact on operational activities
                    if (sheet.length > 27 && sheet[27]['G'] && sheet[27]['G'] !== 'n/a') {
                        if (sheet[27]['G'][0] === 'A') scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact[0];
                        else if (sheet[27]['G'][0] === 'B') scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact[1];
                        else scope.data.opActivitiesImpact = scope.selects.opActivitiesImpact[2];
                    }

                    // Define business priority
                    if (sheet.length > 29 && sheet[29]['G'] && sheet[29]['G'] !== 'n/a') {
                        if (sheet[29]['G'][0] === 'A') scope.data.businessPriority = scope.selects.businessPriority[0];
                        else if (sheet[29]['G'][0] === 'B') scope.data.businessPriority = scope.selects.businessPriority[1];
                        else scope.data.businessPriority = scope.selects.businessPriority[2];
                    }

                    // TABLE
                    if (sheet.length > 39) {
                        scope.data.firstYear = Math.floor(sheet[37]['AF']);
                        for (var r = 39; r < sheet.length; r++) {

                            if (!sheet[r]['E'] || !sheet[r]['E'].length || sheet[r]['E'] === 'n/a') continue;

                            // CASH FLOW
                            var generalInfo = {
                                rocType: (!sheet[r]['E'] || sheet[r]['E'] === 'n/a') ? null : sheet[r]['E'],
                                rocName: (!sheet[r]['F'] || sheet[r]['F'] === 'n/a') ? null : sheet[r]['F'],
                                wbs: (!sheet[r]['AE'] || sheet[r]['AE'] === 'n/a') ? null : sheet[r]['AE'],
                                cost: (!sheet[r]['O'] || sheet[r]['O'] === 'n/a') ? null : sheet[r]['O'],
                                ktLineNum1: (!sheet[r]['P'] || sheet[r]['P'] === 'n/a') ? null : sheet[r]['P'],
                                ktLineNum2: (!sheet[r]['Q'] || sheet[r]['Q'] === 'n/a') ? null : sheet[r]['Q'],
                                ktLineName: (!sheet[r]['R'] || sheet[r]['R'] === 'n/a') ? null : sheet[r]['R'],
                                incremental: (!sheet[r]['S'] || sheet[r]['S'] === 'n/a') ? null : sheet[r]['S'],
                                purchasing: (!sheet[r]['U'] || sheet[r]['U'] === 'n/a') ? null : sheet[r]['U'],
                                vendorName: (!sheet[r]['V'] || sheet[r]['V'] === 'n/a') ? null : sheet[r]['V'],
                                vendorCode: (!sheet[r]['W'] || sheet[r]['W'] === 'n/a') ? null : sheet[r]['W'],
                                contract: (!sheet[r]['X'] || sheet[r]['X'] === 'n/a') ? null : sheet[r]['X'],
                                truCode: (!sheet[r]['Y'] || sheet[r]['Y'] === 'n/a') ? null : sheet[r]['Y'],
                                neededDate: (!sheet[r]['Z'] || sheet[r]['Z'] === 'n/a') ? null : sheet[r]['Z'],
                                requestDate: (!sheet[r]['AA'] || sheet[r]['AA'] === 'n/a') ? null : sheet[r]['AA'],
                                currency: (!sheet[r]['AB'] || sheet[r]['AB'] === 'n/a') ? null : sheet[r]['AB'],
                                depCode: (!sheet[r]['AC'] || sheet[r]['AC'] === 'n/a') ? null : sheet[r]['AC']
                            };
                            var cashFlowRow = {
                                year: {
                                    1: parseFloat(sheet[r]['AF']) ? parseFloat(sheet[r]['AF']) : 0.0,
                                    2: parseFloat(sheet[r]['AS']) ? parseFloat(sheet[r]['AS']) : 0.0,
                                    3: parseFloat(sheet[r]['AT']) ? parseFloat(sheet[r]['AT']) : 0.0,
                                    4: parseFloat(sheet[r]['AU']) ? parseFloat(sheet[r]['AU']) : 0.0,
                                    5: parseFloat(sheet[r]['AV']) ? parseFloat(sheet[r]['AV']) : 0.0
                                },
                                month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}
                            };
                            for (var c = 71; c < 83; c++) {
                                var cellVal = sheet[r]['A' + String.fromCharCode(c)];
                                cashFlowRow.month[1][scope.months[c - 71]] = parseFloat(cellVal) ? parseFloat(cellVal) : 0.0;
                            }
                            // ACCURALS
                            var accuralsRow = {
                                year: {
                                    1: parseFloat(sheet[r]['BS']) ? parseFloat(sheet[r]['BS']) : 0.0,
                                    2: parseFloat(sheet[r]['CF']) ? parseFloat(sheet[r]['CF']) : 0.0,
                                    3: parseFloat(sheet[r]['CG']) ? parseFloat(sheet[r]['CG']) : 0.0,
                                    4: parseFloat(sheet[r]['CH']) ? parseFloat(sheet[r]['CH']) : 0.0,
                                    5: parseFloat(sheet[r]['CI']) ? parseFloat(sheet[r]['CI']) : 0.0
                                },
                                month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}
                            };
                            for (var c = 84; c < 91; c++) {
                                var cellVal = sheet[r]['B' + String.fromCharCode(c)];
                                accuralsRow.month[1][scope.months[c - 84]] = parseFloat(cellVal) ? parseFloat(cellVal) : 0.0;
                            }
                            for (var c = 65; c < 70; c++) {
                                var cellVal = sheet[r]['C' + String.fromCharCode(c)];
                                accuralsRow.month[1][scope.months[c - 58]] = parseFloat(cellVal) ? parseFloat(cellVal) : 0.0;
                            }

                            var isFilled = false;

                            for (var i = 1; i < 6; i++) {
                                if (cashFlowRow.year[i] || accuralsRow.year[i]) {
                                    isFilled = true;
                                    break;
                                }
                            }

                            if (!isFilled) {
                                for (var i = 0; i < 12; i++) {
                                    if (cashFlowRow.month[1][scope.months[i]] || accuralsRow.month[1][scope.months[i]]) {
                                        isFilled = true;
                                        break;
                                    }
                                }
                            }

                            if (!isFilled) continue;

                            if (generalInfo.rocType.toLowerCase().startsWith('revenue')) {
                                scope.data.general.revenues.push(generalInfo);
                                scope.data.cashFlow.revenues.push(cashFlowRow);
                                scope.data.accurals.revenues.push(accuralsRow);
                            } else if (generalInfo.rocType.toLowerCase().startsWith('capex')) {
                                scope.data.general.capexes.push(generalInfo);
                                scope.data.cashFlow.capexes.push(cashFlowRow);
                                scope.data.accurals.capexes.push(accuralsRow);
                            } else if (generalInfo.rocType.toLowerCase().startsWith('other')) {
                                scope.data.general.others.push(generalInfo);
                                scope.data.cashFlow.others.push(cashFlowRow);
                                scope.data.accurals.others.push(accuralsRow);
                            }
                        }
                        scope.onChange();
                        scope.$apply();
                    }
                };

                scope.months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

                scope.collapse = {
                    cashFlow: false,
                    accurals: false
                };

                scope.toggleCollapse = function (name) {
                    scope.collapse[name] = !scope.collapse[name];
                };

                scope.addItem = function (name) {
                    scope.data.general[name].push({});
                    scope.data.cashFlow[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    scope.data.accurals[name].push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    if (!scope.data.general.income.length) {
                        scope.data.general.income.push({});
                        scope.data.cashFlow.income.push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                        scope.data.accurals.income.push({month: {1: {}, 2: {}, 3: {}, 4: {}, 5: {}}, year: {}});
                    }
                };
                scope.deleteItem = function (index, name) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data.general[name].splice(index, 1);
                        scope.data.cashFlow[name].splice(index, 1);
                        scope.data.accurals[name].splice(index, 1);
                        scope.onChange();
                    });
                };

                scope.onChange = function () {
                    $('#loaderDiv').show();
                    for (var table of ['cashFlow', 'accurals']) {
                        for (var name of ['revenues', 'capexes', 'others', 'income']) {
                            if (!scope.data[table][name]) continue;
                            scope.data[table][name + 'Total'] = {year: {}, month: {}};
                            for (var i = 1; i < 6; i++) {
                                scope.data[table][name + 'Total'].year[i] = 0;
                                scope.data[table][name + 'Total'].month[i] = {};
                                if (i === 1) {
                                    for (var m of scope.months)
                                        scope.data[table][name + 'Total'].month[i][m] = 0;
                                }
                            }
                            for (var i = 0; i < scope.data[table][name].length; i++) {
                                for (var y = 1; y < 6; y++) {
                                    var monthsTotal = null;
                                    for (var m in scope.data[table][name][i]['month'][y]) {
                                        if (monthsTotal === null) monthsTotal = 0;
                                        monthsTotal += scope.data[table][name][i]['month'][y][m];
                                        scope.data[table][name + 'Total'].month[y][m] += scope.data[table][name][i]['month'][y][m];
                                    }
                                    if (monthsTotal != null) scope.data[table][name][i]['year'][y] = monthsTotal;
                                    if (scope.data[table][name][i]['year'][y])
                                        scope.data[table][name + 'Total'].year[y] += scope.data[table][name][i]['year'][y];
                                }
                            }
                        }
                    }
                    calcQuantitative();
                };

                var calcQuantitative = function () {

                    // Discount factors
                    var discount = [0];
                    for (var i = 1; i < 6; i++) {
                        discount.push(1.0 / Math.pow(1 + (scope.data.benchmark.wacc / 100.0), i - 1));
                    }

                    // NPV
                    scope.data.npv = 0.0;
                    for (var i = 1; i < 6; i++) {
                        scope.data.npv += (scope.data.cashFlow.revenuesTotal.year[i]
                            + scope.data.cashFlow.capexesTotal.year[i]
                            + scope.data.cashFlow.othersTotal.year[i]
                            + scope.data.cashFlow.incomeTotal.year[i]) * discount[i];
                    }

                    // ROI
                    scope.data.roi = 0.0;
                    if (scope.data.npv > 0.0) {
                        var caps = 0;
                        for (var i = 1; i < 6; i++) {
                            caps += (scope.data.cashFlow.capexesTotal.year[i]
                                + scope.data.cashFlow.othersTotal.year[i]
                                + scope.data.cashFlow.incomeTotal.year[i]) * discount[i];
                        }
                        scope.data.roi = scope.data.npv / Math.abs(caps) * 100.0;
                    }

                    // Payback period
                    scope.data.paybackPeriod = 0.0;
                    var ppsum = 0.0;
                    for (var i = 1; i < 6; i++) {
                        ppsum += (scope.data.cashFlow.revenuesTotal.year[i]
                            + scope.data.cashFlow.othersTotal.year[i]
                            + scope.data.cashFlow.incomeTotal.year[i]
                            + scope.data.cashFlow.capexesTotal.year[i]) * discount[i];
                        if (ppsum > 0) {
                            scope.data.paybackPeriod = (i > 1 ? i : 0.0) + 1.0 - (ppsum / (ppsum - (scope.data.cashFlow.capexesTotal.year[i] * discount[i])));
                            break;
                        }
                    }

                    // P&L effect first year
                    scope.data.plEffect = (scope.data.accurals.revenuesTotal.year[1]
                        + scope.data.accurals.capexesTotal.year[1]
                        + scope.data.accurals.othersTotal.year[1]
                        + scope.data.accurals.incomeTotal.year[1]);

                    // CF effect first year
                    scope.data.cfEffect = (scope.data.cashFlow.revenuesTotal.year[1]
                        + scope.data.cashFlow.capexesTotal.year[1]
                        + scope.data.cashFlow.othersTotal.year[1]
                        + scope.data.cashFlow.incomeTotal.year[1]);

                    scope.calcScoring();
                };

                scope.calcScoring = function () {
                    // Strategy fit
                    scope.data.strategyFitScore = strategyFitPercentage() * 50;

                    // Risks / Opportunities / Business priority
                    scope.data.businessPriorityScore = ((businessPriorityPercentage() * 0.7) + (operationalActivityPercentage() * 0.3)) * 50;

                    // Quantitative score
                    scope.data.quantitativeScore = 0.0;
                    if (scope.data.npv > scope.data.benchmark.maxNPV) scope.data.quantitativeScore += 0.2;
                    else if (scope.data.benchmark.maxNPV) scope.data.quantitativeScore += scope.data.npv / scope.data.benchmark.maxNPV * 0.2;

                    if (scope.data.roi > 0.0) {
                        if (scope.data.roi > scope.data.benchmark.maxROI) scope.data.quantitativeScore += 0.2;
                        else if (scope.data.benchmark.maxROI) scope.data.quantitativeScore += scope.data.roi / scope.data.benchmark.maxROI * 0.2;
                    }

                    if (scope.data.paybackPeriod > 0.0) {
                        if (scope.data.paybackPeriod < scope.data.benchmark.minPP) scope.data.quantitativeScore += 0.2;
                        else if (scope.data.benchmark.minPP) scope.data.quantitativeScore += scope.data.benchmark.minPP / scope.data.paybackPeriod * 0.2;
                    }

                    if (scope.data.plEffect > scope.data.benchmark.maxPL) scope.data.quantitativeScore += 0.25;
                    else if (scope.data.benchmark.maxPL) scope.data.quantitativeScore += scope.data.plEffect / scope.data.benchmark.maxPL * 0.25;

                    if (scope.data.cfEffect > scope.data.benchmark.maxCF) scope.data.quantitativeScore += 0.15;
                    else if (scope.data.benchmark.maxCF) scope.data.quantitativeScore += scope.data.cfEffect / scope.data.benchmark.maxCF * 0.15;

                    scope.data.quantitativeScore *= 55;

                    // Qualitative score
                    scope.data.qualitativeScore = (scope.data.strategyFitScore + scope.data.businessPriorityScore) * 0.45;

                    // SCORE
                    scope.data.score = scope.data.qualitativeScore + scope.data.quantitativeScore;
                    $('#loaderDiv').hide();
                };

                scope.getUser = function (val) {
                    scope.data.networkEconomicsId = null;
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%')).then(
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
                            //return usersByFirstName;
                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%')).then(
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

                scope.userSelected = function (item) {
                    scope.data.networkEconomicsId = item.id;
                    scope.data.networkEconomics = item.name;
                };
            },
            templateUrl: './js/directives/demand/businessCase.html'
        };
    });

    module.directive('demandNumberFormat', function ($filter) {
        'use strict';

        return {
            restrict: 'A',
            require: '?ngModel',
            scope: {
                demandNumberFormat: '=',
                formatAddon: '='
            },
            link: function (scope, elem, attrs, ctrl) {
                if (!ctrl) return;

                ctrl.$formatters.unshift(function () {
                    var fractionSize = -1, view = ctrl.$modelValue;
                    if (!isNaN(scope.demandNumberFormat)) fractionSize = scope.demandNumberFormat;
                    if (fractionSize !== -1) view = $filter('number')(ctrl.$modelValue, fractionSize);
                    else view = $filter('number')(ctrl.$modelValue);
                    if (ctrl.$modelValue === 0.0) view = '0';
                    if (view && scope.formatAddon && scope.formatAddon.length) view += scope.formatAddon;
                    return view;
                });

                ctrl.$parsers.unshift(function (viewValue) {
                    var fractionSize = -1;
                    var plainNumber = viewValue.replace(/,/g, '');
                    if (scope.formatAddon) plainNumber = plainNumber.replace(scope.formatAddon, '');
                    if (!isNaN(scope.demandNumberFormat)) fractionSize = scope.demandNumberFormat;

                    plainNumber = parseFloat(plainNumber);
                    var view = plainNumber;
                    if (fractionSize !== -1) view = $filter('number')(plainNumber, fractionSize);
                    else view = $filter('number')(plainNumber);
                    if (plainNumber === 0.0) view = '0';
                    if (scope.formatAddon && scope.formatAddon.length) view += scope.formatAddon;
                    elem.val(view);

                    return plainNumber;
                });
            }
        };
    });
    module.directive('demandBusinessIntelligence', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                biname: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        // if (!scope.data.requestType) scope.data.requestType = 'Consulting';
                        // if (!scope.data.dataType) scope.data.dataType = 'Kcell';
                        // if (!scope.data.fromPeriod) scope.data.fromPeriod = new Date();
                        if (scope.data.fromPeriod) scope.data.fromPeriod = new Date(scope.data.fromPeriod);
                        // if (!scope.data.toPeriod) scope.data.toPeriod = new Date();
                        if (scope.data.toPeriod) scope.data.toPeriod = new Date(scope.data.toPeriod);
                    }
                });
            },
            templateUrl: './js/directives/demand/businessIntelligence.html'
        };
    });
    module.directive('demandDropdown', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                ngModel: '=',
                options: '=',
                onItemSelect: '=',
                searchField: '=',
                objectList: '=',
                displayProp: '=',
                titleProp: '=',
                index: '=',
                disabled: '=',
                nowrap: '=',
                fieldName: '=',
                isRequired: '=',
                toggleCallback: '='
            },
            link: function (scope, el, attrs) {

                //TODO: position ul-list by js (so it didn't depend on overflow)

                scope.$watch('ngModel', function (value) {
                    scope.theModel = scope.ngModel;
                }, true);

                scope.theModel = null;
                scope.isOpen = false;
                scope.searchVal = '';

                $(document).bind('click', function (e) {
                    if (el !== e.target && e.target.classList.contains('page-disabler') && scope.isOpen) {
                        scope.$apply(function () {
                            scope.isOpen = false;
                            if (scope.toggleCallback) scope.toggleCallback(scope.isOpen);
                        });
                    }
                });

                var setWidth = function () {
                    var element = el[0].querySelector('.list-group');
                    var curWidth = element.offsetWidth, scrollWidth;
                    element.style.width = 'auto';
                    scrollWidth = element.scrollWidth;
                    if (scrollWidth > curWidth) element.style.width = scrollWidth + 'px';
                    else element.style.width = curWidth + 'px';
                };

                scope.toggleSelect = function () {
                    scope.isOpen = !scope.isOpen;
                    scope.searchVal = '';

                    if (scope.isOpen) $timeout(setWidth);
                    if (scope.toggleCallback) scope.toggleCallback(scope.isOpen);
                };

                scope.selectOption = function (option) {
                    scope.toggleSelect();
                    scope.theModel = option;
                    if (scope.onItemSelect) scope.onItemSelect(scope.index, option);
                };

                scope.filterFunc = function (value, index, array) {
                    if (!scope.objectList) return (value + '').toLowerCase().includes(scope.searchVal.toLowerCase());
                    return (value[scope.displayProp] + '').toLowerCase().includes(scope.searchVal.toLowerCase());
                };
            },
            templateUrl: './js/directives/demand/demandDropdown.html'
        };
    }]);
    module.directive('demandSddFieldConstructor', function ($rootScope, $http, $timeout, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                section: '='
            },
            link: function (scope, element, attrs) {
                scope.selected = null;
                scope.sections = [
                    {id: 'funcRequirements', name: 'Functional requirements'},
                    {id: 'nonFuncRequirements', name: 'Non-Functional requirements'},
                    {id: 'solutionDesign', name: 'Solution design'},
                    {id: 'components', name: 'Components'}
                ];

                scope.leftIndex = {size: 0};
                scope.rightIndex = {size: 0};
                scope.customIndex = {size: 0};
                scope.onLeftSelect = function (index) {
                    // scope.rightIndex = {size: 0};
                    if (scope.leftIndex[index]) {
                        scope.leftIndex.size--;
                        scope.leftIndex[index] = false;
                    } else {
                        scope.leftIndex.size++;
                        scope.leftIndex[index] = true;
                    }
                };

                scope.onRightSelect = function (index, customField) {
                    // scope.leftIndex = {size: 0};
                    if (customField) {
                        if (scope.customIndex[index]) {
                            scope.customIndex.size--;
                            scope.customIndex[index] = false;
                        } else {
                            scope.customIndex.size++;
                            scope.customIndex[index] = true;
                        }
                    } else {
                        if (scope.rightIndex[index]) {
                            scope.rightIndex.size--;
                            scope.rightIndex[index] = false;
                        } else {
                            scope.rightIndex.size++;
                            scope.rightIndex[index] = true;
                        }
                    }
                };

                scope.moveRight = function () {
                    for (var i in scope.leftIndex) {
                        if (i === 'size') continue;
                        else if (i === '-1') scope.data[scope.sections[scope.section].id].customs.push({name: ''});
                        else scope.data[scope.sections[scope.section].id].fields[parseInt(i)].hidden = false;
                    }
                    scope.leftIndex = {size: 0};
                };

                scope.moveLeft = function () {
                    exModal.open({
                        scope: {
                            message: 'Are you sure you want to delete the field' + ((scope.rightIndex.size + scope.customIndex.size > 1) ? 's' : '') + '?',
                            cancel: 'No',
                            ok: 'Yes'
                        },
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        for (var i in scope.rightIndex) {
                            if (i === 'size') continue;
                            else scope.data[scope.sections[scope.section].id].fields[parseInt(i)].hidden = true;
                        }
                        for (var i in scope.customIndex) {
                            if (i === 'size') continue;
                            else scope.data[scope.sections[scope.section].id].customs.splice(parseInt(i), 1);
                        }
                        scope.leftIndex = {size: 0};
                        scope.customIndex = {size: 0};
                    });
                };

                scope.changeSection = function (index) {
                    scope.section = index;
                    scope.leftIndex = {size: 0};
                    scope.rightIndex = {size: 0};
                    scope.customIndex = {size: 0};
                };
            },
            templateUrl: './js/directives/demand/sddFieldConstructor.html'
        };
    });
    module.directive('demandSddSections', function ($rootScope, $http, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                section: '=',
                form: '=',
                view: '=',
                disabled: '=',
                required: '=',
                processId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                scope.sections = [
                    [
                        {from: 0, to: 1},
                        {from: 2, to: 5, name: 'Functional requirements', hidden: true},
                        {from: 6, to: 7}
                    ],
                    [
                        {from: 0, to: 8}
                    ],
                    [
                        {from: 0, to: 4, name: 'General architecture', hidden: true},
                        {from: 5, to: 15, name: 'Solution design', hidden: true}
                    ],
                    [
                        {from: 0, to: 2}
                    ]
                ];

                const fields = [
                    [
                        {name: 'User functions', hidden: true, value: ''},
                        {name: 'Administrative functions', hidden: true, value: ''},
                        {name: 'Product connection', hidden: true, value: ''},
                        {name: 'Product suspention', hidden: true, value: ''},
                        {name: 'Product renewal', hidden: true, value: ''},
                        {name: 'Product disconnection', hidden: true, value: ''},
                        {name: 'Charging', hidden: true, value: ''},
                        {name: 'Invoicing', hidden: true, value: ''}
                    ],
                    [
                        {name: 'Interfaces with the system', hidden: true, value: ''},
                        {name: 'Security', hidden: true, value: ''},
                        {name: 'Technologies', hidden: true, value: ''},
                        {name: 'Used equipment', hidden: true, value: ''},
                        {name: 'Technical support', hidden: true, value: ''},
                        {name: 'Service availability', hidden: true, value: ''},
                        {name: 'System recovery time', hidden: true, value: ''},
                        {name: 'Performance requirements', hidden: true, value: ''},
                        {name: 'Scalability requirements', hidden: true, value: ''}
                    ],
                    [
                        {name: 'Hardware architecture', hidden: true, value: ''},
                        {name: 'Software architecture', hidden: true, value: ''},
                        {name: 'Interaction architecture', hidden: true, value: ''},
                        {name: 'Integration scheme', hidden: true, value: ''},
                        {name: 'Network layout', hidden: true, value: ''},
                        {name: 'Use and Interaction scenarios', hidden: true, value: ''},
                        {name: 'Data model', hidden: true, value: ''},
                        {name: 'Data adaptation', hidden: true, value: ''},
                        {name: 'External access interfaces', hidden: true, value: ''},
                        {name: 'Internal access interfaces', hidden: true, value: ''},
                        {name: 'Information security', hidden: true, value: ''},
                        {name: 'Performance', hidden: true, value: ''},
                        {name: 'Data backup and security', hidden: true, value: ''},
                        {name: 'Logging', hidden: true, value: ''},
                        {name: 'Reporting', hidden: true, value: ''},
                        {name: 'Monitoring', hidden: true, value: ''}
                    ],
                    [
                        {name: 'Function on midlleware / Abstraction layer', hidden: true, value: ''},
                        {name: 'DNS settings', hidden: true, value: ''},
                        {name: 'Firewall settings', hidden: true, value: ''}
                    ]
                ];

                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.data.fields) scope.data.fields = fields[scope.section];
                        if (!scope.data.customs) scope.data.customs = [];

                        for (var r of scope.sections[scope.section]) {
                            if (r.name) {
                                r.hidden = true;
                                for (var i = r.from; i <= r.to; i++) {
                                    if (!scope.data.fields[i].hidden) {
                                        r.hidden = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }, true);
            },
            templateUrl: './js/directives/demand/sddSections.html'
        };
    });
    module.directive('demandSddPdf', function ($rootScope, $http, $sce) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                bKey: '=',
                dName: '=',
                dDescription: '=',
                date: '=',
                author: '='
            },
            link: function (scope, element, attrs) {
                scope.sections = [
                    {id: 'funcRequirements', name: 'Functional requirements'},
                    {id: 'nonFuncRequirements', name: 'Non-Functional requirements'},
                    {id: 'solutionDesign', name: 'Solution design'},
                    {id: 'components', name: 'Components'},
                ];
                scope.trustedHtml = function (html) {
                    return $sce.trustAsHtml(html);
                };
                scope.hasFields = function (section) {
                    if (!section) return false;
                    if (section.customs && section.customs.length) return true;
                    if (!section.fields || !section.fields.length) return false;
                    for (var f of section.fields) {
                        if (f && f.hidden === false) return true;
                    }
                    return false;
                };
            },
            templateUrl: './js/directives/demand/sddPdf.html'
        };
    });
    module.directive('demandTestCases', function ($rootScope, $http, exModal) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                useCases: '=',
                form: '=',
                view: '=',
                disabled: '=',
                status: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data) scope.data = {};
                        if (!scope.useCaseCollapsed) scope.useCaseCollapsed = [];
                    }
                });

                scope.testCaseAdd = function (index) {
                    if (!scope.data[index]) scope.data[index] = [];
                    scope.data[index].push({value: '', status: ''});
                };

                scope.testCaseDelete = function (uIndex, index) {
                    exModal.open({
                        templateUrl: './js/partials/confirmModal.html',
                        size: 'sm'
                    }).then(function () {
                        scope.data[uIndex].splice(index, 1);
                    });
                };

                scope.toggleUseCaseCollapse = function (el, index) {
                    if (el.target.classList.contains('not-collapsable') || $(el.target).parents('.not-collapsable').length) return;
                    scope.useCaseCollapsed[index] = !scope.useCaseCollapsed[index];
                };
            },
            templateUrl: './js/directives/demand/testCases.html'
        };
    });
    module.directive('demandUatPdf', function ($rootScope, $http, $sce) {
        return {
            restrict: 'E',
            scope: {
                uat: '=',
                bKey: '=',
                dName: '=',
                dOwner: '=',
                dDescription: '=',
                useCases: '=',
                testCases: '=',
                appList: '=',
                responsible: '=',
                comment: '=',
                attachments: '='
            },
            link: function (scope, element, attrs) {
                scope.date = new Date();
                setInterval(function () {
                    scope.date = new Date();
                }, 60 * 1000);
                scope.trustedHtml = function (html) {
                    return $sce.trustAsHtml(html);
                };
            },
            templateUrl: './js/directives/demand/uatPdf.html'
        };
    });
    module.directive('leasingDetail', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                leasingInfo: '='
            },
            link: function (scope, el, attrs) {
                // $http.get("/camunda/api/engine/engine/default/user/" + scope.bulksmsInfo.starter + "/profile").then(function (e) {
                //                 scope.bulksmsInfo.starter = (e.data.firstName ? e.data.firstName : "") + " " + (e.data.lastName ? e.data.lastName : "");
                //             });
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/leasing/leasingDetail.html'
        };
    }]);
    module.directive('leasingCandidate', ['$http', '$timeout', function ($http, $timeout) {
        return {
            require: '^form',
            restrict: 'E',
            scope: {
                leasingCandidate: '=',
                farEnd: '='
            },
            link: function (scope, el, attrs, formCtrl) {
                //console.log(formCtrl, 'formCtrl');
                scope.parentForm = formCtrl;
                scope.dictionary = {};
                scope.selectedIndex = -1;
                scope.defaultFarEndCard = false;
                scope.loadCurrentFarEnd = true;
                if (scope.leasingCandidate.cellAntenna) {
                    console.log('scope.leasingCandidate.cellAntenna')
                    console.log(scope.leasingCandidate.cellAntenna)
                    if (scope.leasingCandidate.cellAntenna.cn_du !== null && typeof scope.leasingCandidate.cellAntenna.cn_du == 'string') {
                        scope.leasingCandidate.cellAntenna.cn_du = [scope.leasingCandidate.cellAntenna.cn_du]
                    }
                    if (!scope.leasingCandidate.cellAntenna.hasOwnProperty('cn_du')) {
                        scope.leasingCandidate.cellAntenna.cn_du = [];
                    }
                }
                $http.get('/api/leasingCatalogs').then(
                    function (result) {
                        angular.extend(scope.dictionary, result.data);
                        console.log(`dictionary result:`);
                        console.log(result);
                        console.log('----------------------------------------');
                        console.log(`scope.dictionary:`);
                        console.log(scope.dictionary);
                        console.log('----------------------------------------');
                        scope.dictionary.legalTypeTitle = _.keyBy(scope.dictionary.legalType, 'id');
                        scope.dictionary.antennasList = scope.dictionary.antennas;
                        scope.dictionary.antennaType = scope.dictionary.antennaType;
                        scope.addressesList = scope.dictionary.addresses;
                        scope.oblastList = _.uniqBy(scope.dictionary.addresses, 'oblast').map((e, index) => {
                            return {"name": e.oblast, "id": index}
                        });
                        scope.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                        scope.currentFarEnd.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
                        scope.districtList = _.uniqBy(scope.dictionary.addresses, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.filteredDistricts = scope.districtList;
                        scope.filteredDistrictsInCAI = scope.districtList;
                        scope.alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');
                        console.log(scope);

                        scope.leasingCandidate.addressString = '';
                        scope.leasingCandidate.cellAntenna.addressString = '';
                        //tabs
                        scope.minTab = 0;
                        scope.maxTab = 3;
                        scope.leasingCandidate.cellAntenna.sectors[0].active = 'active';
                        scope.selectedSectorTab = 0;
                        scope.leasingCandidate.checkTPSTouched = false;
                        scope.leasingCandidate.checkTPSBTouched = false;
                        Object.values(scope.leasingCandidate.address).forEach((s, index) => {
                            scope.leasingCandidate.addressString += index > 0 ? ', ' + s : s
                        });

                        Object.values(scope.leasingCandidate.cellAntenna.address).forEach((s, index) => {
                            scope.leasingCandidate.cellAntenna.addressString += index > 0 ? ', ' + s : s
                        });
                        console.log(`asdasdsa: ${scope.leasingCandidate.cellAntenna.addressString} 12`);
                    },
                    function (error) {
                        console.log(error);
                    }
                );

                scope.$watch('leasingCandidate.farEndInformation', function (farEndInformation) {
                    if (farEndInformation && farEndInformation.length > 0 && scope.loadCurrentFarEnd) {
                        angular.forEach(farEndInformation, function (fe, i) {
                            if (fe.surveyDate) {
                                fe.surveyDate = new Date(fe.surveyDate);
                            }
                        });
                        scope.currentFarEnd = farEndInformation[0];
                        scope.currentFarEnd.priority = true;
                        scope.loadCurrentFarEnd = false;
                        scope.defaultFarEndCard = true;
                    }
                });

                scope.addressToString = function (address) {
                    let string = '';
                    if (address) {
                        string += `${address.cn_addr_oblast ? address.cn_addr_oblast + ', ' : ''}`;
                        string += `${address.cn_addr_district ? address.cn_addr_district + ', ' : ''}`;
                        string += `${address.cn_addr_city ? address.cn_addr_city + ', ' : ''}`;
                        string += `${address.cn_addr_street ? address.cn_addr_street + ', ' : ''}`;
                        string += `${address.cn_addr_building ? address.cn_addr_building + ', ' : ''}`;
                        string += `${address.cn_addr_cadastral_number ? address.cn_addr_cadastral_number + ', ' : ''}`;
                        string += `${address.cn_addr_note ? address.cn_addr_note + ', ' : ''}`;
                        //cn_addr_cadastral_number cn_addr_note
                        // Object.values(address).forEach((a,index) => {
                        // 	string += index > 0 ? ', ' + a : a
                        // })
                        // console.log(`string: ${string}`)
                    }
                    return string;
                };


                scope.cellAntennaTypeChanged = function (val, parent, index) {
                    const pIndex = parent.$index
                    if(scope.leasingCandidate.cellAntenna) {
                        if(scope.leasingCandidate.cellAntenna.sectors && scope.leasingCandidate.cellAntenna.sectors.length >0){
                            if(scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas && scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas.length >0) {
                                scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas[index].frequencyBand = parseInt(val.replace(/[^0-9.]/gi, ''));
                            }
                        }
                    }
                };

                scope.getRadians = function (val) {
                    const pi = 3.14159265358979;
                    return pi*val/180;
                }

                scope.latitudePattern = '(4([1-9]{1}.[0-9]{6}|0.([6-9]{1}[0-9]{5}|5([7-9]{1}[0-9]{4}|6(9[0-9]{3}|8[0-9]{3}))))|5([0-4]{1}.[0-9]{6}|5.([0-3]{1}[0-9]{5}|4([0-3]{1}[0-9]{4}|40000))))';
                scope.longitudePattern = '([5-7]{1}[0-9]{1}.[0-9]{6}|4([7-9]{1}.[0-9]{6}|6.([5-9]{1}[0-9]{5}|49([4-9]{1}|3)[0-9]{3}))|8([0-6]{1}.[0-9]{6}|7.([0-2]{1}[0-9]{5}|3(0[0-9]{4}|1([0-4]{1}[0-9]{3}|5000)))))';

                scope.autoSetAzimuth = function () {
                    const primaryFE = scope.leasingCandidate.farEndInformation && scope.leasingCandidate.farEndInformation.length >0 ? scope.leasingCandidate.farEndInformation[0] : null
                    const NE_address = scope.leasingCandidate.transmissionAntenna && scope.leasingCandidate.transmissionAntenna.address ? scope.leasingCandidate.transmissionAntenna.address : null
                    if (primaryFE && NE_address) {
                        if ( primaryFE.fe_longitude && primaryFE.fe_latitude && NE_address.latitude && NE_address.longitude) {
                            const pi = 3.14159265358979;
                            let fe_long = parseFloat(primaryFE.fe_longitude) //'77.507694')
                            let fe_lat = parseFloat(primaryFE.fe_latitude )//'52.648194')
                            let ne_long = parseFloat(NE_address.latitude) //'77.233417')
                            let ne_lat = parseFloat(NE_address.longitude) //'52.782889')
                            const ne_long_radians = scope.getRadians(ne_long)
                            const ne_lat_radians = scope.getRadians(ne_lat)
                            const fe_long_radians = scope.getRadians(fe_long)
                            const fe_lat_radians = scope.getRadians(fe_lat)
                            // Math.cos(x)
                            const cosNeLat = Math.cos(ne_lat_radians)
                            const cosFeLat = Math.cos(fe_lat_radians)
                            const sinNeLat = Math.sin(ne_lat_radians)
                            const sinFeLat = Math.sin(fe_lat_radians)
                            const deltaLong = Math.abs(ne_long_radians - fe_long_radians);
                            const cosDeltaLong = Math.cos(deltaLong)
                            const sinDeltaLongNE = Math.sin(fe_long_radians - ne_long_radians)
                            const sinDeltaLongFE = Math.sin(ne_long_radians - fe_long_radians)
                            const angleDelta = Math.acos(sinNeLat*sinFeLat + cosFeLat*cosNeLat*cosDeltaLong)
                            const angleDiff = Math.atan2(sinDeltaLongNE*cosFeLat, (cosNeLat*sinFeLat-cosFeLat*sinNeLat*cosDeltaLong) )
                            const neAzimuth = Math.round(180*(angleDiff % (2*pi))/pi*100)/100
                            scope.leasingCandidate.transmissionAntenna.azimuth = neAzimuth;
                        } else {
                            alert('Заполните корректно поля FE Longitude, FE Latitude, NE Longitude, NE Latitude')
                        }
                    }
                }


                scope.tabStepRight = function () {
                    if (scope.selectedSectorTab + 1 < scope.leasingCandidate.cellAntenna.sectors.length) {
                        scope.selectedSectorTab = scope.selectedSectorTab + 1;
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';

                        if (scope.selectedSectorTab === scope.maxTab) {
                            scope.minTab = scope.minTab + 1;
                            scope.maxTab = scope.maxTab + 1;
                        }
                    } else {
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
                    }
                }

                scope.tabStepLeft = function () {
                    if (scope.selectedSectorTab - 1 >= 0) {
                        scope.selectedSectorTab = scope.selectedSectorTab - 1;
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';

                        if (scope.selectedSectorTab + 1 === scope.minTab) {
                            scope.minTab = scope.minTab - 1;
                            scope.maxTab = scope.maxTab - 1;
                        }
                    } else if (scope.selectedSectorTab - 1 < 0) {
                        scope.leasingCandidate.cellAntenna.sectors[0].active = 'active';
                    } else {
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
                    }
                }
                scope.checkTPS = function (val) {
                    if (val === 'TPS installation required (TPS is missing)') {
                        // scope.leasingCandidate.powerSource.cn_tps_belongs = '';
                        // scope.leasingCandidate.powerSource.cn_tps_belongs_commentary = '';
                        // scope.leasingCandidate.powerSource.cn_tps_distance = '';
                        scope.leasingCandidate.checkTPSTouched = false;
                    } else {
                        scope.leasingCandidate.checkTPSTouched = true;
                    }
                };


                scope.checkTPSB = function (val) {
                    if (val === 'Other') {
                        scope.leasingCandidate.checkTPSBTouched = true;
                    } else {
                        // scope.leasingCandidate.powerSource.cn_tps_belongs_commentary = '';
                        scope.leasingCandidate.checkTPSBTouched = false;
                    }
                };

                scope.selectAntennaSector = function (index) {
                    scope.selectedSectorTab = index;
                }

                scope.openFarEndInformation = function (index) {
                    scope.currentFarEnd = scope.leasingCandidate.farEndInformation[index];
                    if (scope.currentFarEnd.surveyDate) {
                        scope.currentFarEnd.surveyDate = new Date(scope.currentFarEnd.surveyDate);
                    }
                    scope.defaultFarEndCard = true;
                };
                scope.checkCableLayingType = function ($index, val) {
                    if (val) scope.leasingCandidate.cableLayingTypeChecks += 1;
                    else scope.leasingCandidate.cableLayingTypeChecks -= 1;
                    scope.leasingCandidate.checkCableLayingTypeTouched = true;
                };
                scope.sortableOptions = {
                    disabled: true,
                    'ui-preserve-size': true,
                    stop: function (e, ui) {
                        scope.leasingCandidate.farEndInformation[0].priority = true;
                        angular.forEach(scope.leasingCandidate.farEndInformation, function (fe, i) {
                            if (i !== 0) fe.priority = false;
                        });
                    }
                };
                scope.$watch('leasingCandidate.checkAndApproveFETaskResult', function (resolution) {
                    if (resolution === 'approved' || resolution === 'rejected') {
                        scope.sortableOptions.disabled = true;
                    } else if (resolution == 'priorityChange') scope.sortableOptions.disabled = false;
                });
                scope.$watch('leasingCandidate.transmissionAntenna.antennaType', function (antennaType) {
                    scope.leasingCandidate.frequenciesByAntennaType = {};
                    scope.leasingCandidate.frequenciesByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                        return p.name === antennaType;
                    });
                }, true);
                scope.$watch('currentFarEnd.feAntennaType', function (antennaType) {
                    scope.leasingCandidate.frequenciesFeByAntennaType = {};
                    scope.leasingCandidate.frequenciesFeByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                        return p.name === antennaType;
                    })
                }, true);
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
                scope.defineByAntennaType = function (antennaType, type) {
                    if (antennaType) {
                        if (type === 'transmission') {
                            angular.forEach(scope.dictionary.antennaType, function (type) {
                                if (type.name === antennaType) {
                                    scope.leasingCandidate.transmissionAntenna.diameter = type.diameter;
                                    scope.leasingCandidate.transmissionAntenna.weight = type.weight;
                                }
                            });
                        } else {
                            angular.forEach(scope.dictionary.antennaType, function (type) {
                                if (type.name === antennaType) {
                                    scope.currentFarEnd.diameter = type.diameter;
                                    scope.currentFarEnd.weight = type.weight;
                                }
                            });

                        }
                    } else {
                        if (type === 'transmission') {
                            scope.leasingCandidate.transmissionAntenna.diameter = undefined;
                            scope.leasingCandidate.transmissionAntenna.weight = undefined;
                        } else {
                            scope.currentFarEnd.diameter = undefined;
                            scope.currentFarEnd.weight = undefined;
                        }
                    }
                };

                scope.selectIndex = function (index) {
                    if (scope.selectedIndex == index) {
                        scope.selectedIndex = undefined;
                    } else {
                        scope.selectedIndex = index;
                    }
                }
                scope.checkAzimuth = function (val, index) {
                    if (index > 0) {
                        if (!(val >= 0 && val <= 360)) scope.leasingCandidate.cellAntenna.selectedAntennas[index].azimuth = '';
                    } else {
                        if (!(val >= 0 && val <= 360)) scope.currentFarEnd.azimuth = '';
                    }
                };

                scope.filterDistrictAfterSelectObl = function (cn_addr_oblast) {
                    scope.currentFarEnd.address.cn_addr_oblast = cn_addr_oblast
                    scope.currentFarEnd.address.cn_addr_district = ''
                    scope.currentFarEnd.address.cn_addr_city = ''
                    if (scope.currentFarEnd.address.cn_addr_oblast && scope.currentFarEnd.address.cn_addr_oblast !== '') {
                        scope.currentFarEnd.filteredByOblast = scope.dictionary.addresses.filter(a => {
                            return a.oblast === scope.currentFarEnd.address.cn_addr_oblast
                        });
                        scope.currentFarEnd.filteredDistricts = _.uniqBy(scope.currentFarEnd.filteredByOblast, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.currentFarEnd.cityList = _.uniqBy(scope.currentFarEnd.filteredByOblast, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    } else {
                        scope.currentFarEnd.filteredDistricts = scope.districtList;
                        scope.currentFarEnd.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    }
                };

                scope.addressCitySelected = function ($item) {
                    scope.currentFarEnd.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    if (!scope.currentFarEnd.address.cn_addr_oblast) {
                        scope.currentFarEnd.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
                    }
                };

                scope.getCity = function (val) {
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.currentFarEnd.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                };

                scope.filterDistrictAfterSelectOblCN = function (cn_addr_oblast) {
                    scope.leasingCandidate.address.cn_addr_oblast = cn_addr_oblast
                    scope.leasingCandidate.address.cn_addr_district = ''
                    scope.leasingCandidate.address.cn_addr_city = ''
                    if (scope.leasingCandidate.address.cn_addr_oblast && scope.leasingCandidate.address.cn_addr_oblast !== '') {
                        scope.leasingCandidate.filteredByOblast = scope.dictionary.addresses.filter(a => {
                            return a.oblast === scope.leasingCandidate.address.cn_addr_oblast
                        });
                        scope.leasingCandidate.filteredDistricts = _.uniqBy(scope.leasingCandidate.filteredByOblast, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.leasingCandidate.cityList = _.uniqBy(scope.leasingCandidate.filteredByOblast, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    } else {
                        scope.leasingCandidate.filteredDistricts = scope.districtList;
                        scope.leasingCandidate.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    }
                };

                scope.addressCitySelectedCN = function ($item) {
                    scope.leasingCandidate.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    scope.leasingCandidate.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
                };

                scope.getCityCN = function (val) {
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.leasingCandidate.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                };

                scope.getCity = function (val) {
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.currentFarEnd.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                };

                scope.filterDistrictAfterSelectOblNE = function (cn_addr_oblast) {
                    scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_oblast = cn_addr_oblast
                    scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_district = ''
                    scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_city = ''
                    if (scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_oblast && scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_oblast !== '') {
                        scope.leasingCandidate.transmissionAntennatransmissionAntenna.filteredByOblast = scope.dictionary.addresses.filter(a => {
                            return a.oblast === scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_oblast
                        });
                        scope.leasingCandidate.transmissionAntennatransmissionAntenna.filteredDistricts = _.uniqBy(scope.leasingCandidate.transmissionAntennatransmissionAntenna.filteredByOblast, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.leasingCandidate.transmissionAntennatransmissionAntenna.cityList = _.uniqBy(scope.leasingCandidate.transmissionAntennatransmissionAntenna.filteredByOblast, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    } else {
                        scope.leasingCandidate.transmissionAntennatransmissionAntenna.filteredDistricts = scope.districtList;
                        scope.leasingCandidate.transmissionAntennatransmissionAntenna.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    }
                };

                scope.addressCitySelectedNE = function ($item) {
                    scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    scope.leasingCandidate.transmissionAntennatransmissionAntenna.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
                };

                scope.getCityNE = function (val) {
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.leasingCandidate.transmissionAntennatransmissionAntenna.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                };
            },
            templateUrl: './js/directives/leasing/leasingCandidate.html'
        };
    }]);
    module.directive('farEndInfo', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                farEnd: '='
            },
            link: function (scope, el, attrs) {
                scope.dictionary = {};
                $http.get('/api/leasingCatalogs').then(
                    function (result) {
                        angular.extend(scope.dictionary, result.data);
                        scope.dictionary.legalTypeTitle = _.keyBy(scope.dictionary.legalType, 'id');
                    },
                    function (error) {
                        console.log(error);
                    }
                );
            },
            templateUrl: './js/directives/leasing/farEndInfo.html'
        };
    }]);
    module.directive("limitTo", [function () {
        return {
            restrict: "A",
            link: function (scope, elem, attrs) {
                var limit = parseInt(attrs.limitTo);
                angular.element(elem).on("keypress", function (e) {
                    if (this.value.length == limit) e.preventDefault();
                });
            }
        }
    }]);
    module.directive('numbersOnly', [function () {
        return {
            restrict: "A",
            require: 'ngModel',
            link: function (scope, element, attr, ngModelCtrl) {
                function fromUser(text) {
                    if (text) {
                        var transformedInput = text.replace(/[^0-9]/g, '');

                        if (transformedInput !== text) {
                            ngModelCtrl.$setViewValue(transformedInput);
                            ngModelCtrl.$render();
                        }
                        return transformedInput;
                    }
                    return undefined;
                }

                ngModelCtrl.$parsers.push(fromUser);
            }
        };
    }]);
    module.directive('infoAftersales', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                aftersalesInfo: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/aftersales/infoAftersales.html'
        };
    }]);
    module.directive('aftersalesLegalInformation', function ($rootScope, $http, toasty) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
                pbxData: "="
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.termContract) scope.data.termContract = new Date();
                        else scope.data.termContract = new Date(scope.data.termContract);
                        if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.checkBIN = function () {
                    if (scope.data.BIN && scope.data.BIN.length === 12) {
                        $http({
                            method: 'GET',
                            url: '/camunda/aftersales/pbx/bin/' + scope.data.BIN,
                            transformResponse: []
                        }).then(
                            function (response) {
                                var result = JSON.parse(response.data);
                                if (result.clientPriority) scope.data.clientPriority = result.clientPriority;
                                if (!result.aftersales && result.legalInfo) parseFromPBX(JSON.parse(result.legalInfo));
                                $rootScope.$broadcast('aftersalesPBXBINCheck', result);
                            },
                            function (response) {
                                toasty.error(response.data);
                            });
                    } else toasty.error('BIN must have length 12!');
                };

                function parseFromPBX(li) {
                    if (!li) return;
                    if (li.legalName) scope.data.legalName = li.legalName;
                    if (li.companyRegistrationCity) scope.data.companyCity = li.companyRegistrationCity;
                    if (li.purchaseType) scope.data.purchaseType = li.purchaseType;
                    if (li.salesRepresentative) scope.data.salesRepr = li.salesRepresentative;
                    if (li.salesRepresentativeId) scope.data.salesReprId = li.salesRepresentativeId;
                    if (li.legalAddress) scope.data.legalAddress = li.legalAddress;
                    if (li.mailingAddress) scope.data.mailingAddress = li.mailingAddress;
                    if (li.email) scope.data.email = li.email;
                    if (li.companyRegistrationDate) scope.data.companyDate = new Date(li.companyRegistrationDate);
                    if (li.accountant) scope.data.accountant = li.accountant;
                    if (li.accountantNumber) scope.data.accountantNumber = li.accountantNumber;
                    if (li.ticName) scope.data.ticName = li.ticName;
                    if (li.mainTypeActivityCustomer) scope.data.mainType = li.mainTypeActivityCustomer;
                    if (li.providerName) scope.data.providerName = li.providerName;
                    if (li.providerBin) scope.data.providerBIN = li.providerBin;
                    if (li.vatCertificate) scope.data.vatCert = li.vatCertificate;
                    if (li.iban) scope.data.iban = li.iban;
                    if (li.bankName) scope.data.bankName = li.bankName;
                    if (li.swift) scope.data.swift = li.swift;
                    if (li.kbe) scope.data.kbe = li.kbe;
                    if (li.termContract) scope.data.termContract = new Date(li.termContract);
                    scope.data.termContractEnd = li.termContractEnd;
                    if (scope.data.termContractEnd === null || scope.data.termContractEnd === undefined) scope.data.termContractEnd = false;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    if (scope.pbxData.companyDate) scope.pbxData.companyDate = new Date(scope.pbxData.companyDate);
                    if (scope.pbxData.termContract) scope.pbxData.termContract = new Date(scope.pbxData.termContract);
                    scope.pbxData.fetched = true;
                }

                scope.onSalesReprChange = function () {
                    if (!scope.data.salesRepr || !scope.data.salesRepr.length) scope.form.liSalesRepr.$setValidity('not_selected', true);
                };

                scope.getUser = function (val) {
                    scope.data.salesReprId = null;
                    scope.form.liSalesRepr.$setValidity('not_selected', false);
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%')).then(
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

                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%')).then(
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

                scope.userSelected = function ($item) {
                    scope.data.salesReprId = $item.id;
                    scope.data.salesRepr = $item.name;
                    scope.form.liSalesRepr.$setValidity('not_selected', true);
                };
            },
            templateUrl: './js/directives/aftersalesPBX/legalInfo.html'
        };
    });
    module.directive('aftersalesTechnicalSpecifications', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showCpNew: '=',
                pbxData: '=',
                editConnPoint: '=',
                removeNumbers: '=',
                hiddenFields: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.iCallAccess) scope.data.iCallAccess = 'No';
                        if (!scope.data.removalRequired) scope.data.removalRequired = false;
                    }
                });

                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.$on("aftersalesPBXBINCheck", function (e, result) {
                    if (!result || result.aftersales) return;
                    if (result.techSpecs) parseFromPBX(JSON.parse(result.techSpecs));
                });

                scope.$on('tab-selected', function (e, tabName) {
                    if (tabName === 'techSpec') {
                        var pbxTmp = scope.data.pbxNumbers;
                        scope.data.pbxNumbers = 'this is because of tabset';
                        var removalTmp = scope.data.removalNumbers;
                        scope.data.removalNumbers = 'this is because of tabset';
                        $timeout(function () {
                            scope.data.pbxNumbers = pbxTmp;
                            scope.data.removalNumbers = removalTmp;
                        });
                    }
                });

                function parseFromPBX(ts) {
                    if (!ts) return;
                    if (ts.technicalPerson) scope.data.contactPerson = ts.technicalPerson;
                    if (ts.technicalNumber) scope.data.contactNumber = ts.technicalNumber;
                    if (ts.technicalEmail) scope.data.contactEmail = ts.technicalEmail;
                    if (ts.pbxType) scope.data.equipmentType = ts.pbxType;
                    if (ts.pbxVendor) scope.data.pbxVendor = ts.pbxVendor;
                    if (ts.pbxModel) scope.data.pbxModel = ts.pbxModel;
                    if (ts.pbxLocation) scope.data.pbxLocation = ts.pbxLocation;
                    if (ts.pbxCity) scope.data.equipmentCity = ts.pbxCity;
                    if (ts.pbxAddress) scope.data.equipmentAddress = ts.pbxAddress;
                    if (ts.virtualNumbersCount) scope.data.pbxQuantity = ts.virtualNumbersCount;
                    if (ts.pbxNumbers) scope.data.pbxNumbers = ts.pbxNumbers;
                    if (ts.inOutCallAccess) scope.data.callsAccess = ts.inOutCallAccess;
                    if (ts.intenationalCallAccess) scope.data.iCallAccess = ts.intenationalCallAccess;
                    if (ts.connectionType) scope.data.connectionType = ts.connectionType;
                    if (ts.connectionPoint) scope.data.connectionPoint = ts.connectionPoint;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesPBX/techSpecs.html'
        };
    });
    module.directive('aftersalesSipProtocol', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                showNewFields: '=',
                modifyConnection: '=',
                pbxData: "=",
                hiddenFields: "="
            },
            link: function (scope, element, attrs) {
                scope.$watch('pbxData', function (value) {
                    if (!scope.pbxData) scope.pbxData = {};
                });

                scope.$on("aftersalesPBXBINCheck", function (e, result) {
                    if (!result || result.aftersales) return;
                    if (result.sip) parseFromPBX(JSON.parse(result.sip));
                });

                scope.$on('tab-selected', function (e, tabName) {
                    if (tabName === 'techSpec') {
                        var tmp = scope.data.description;
                        scope.data.description = 'this is because of tabset';
                        $timeout(function () {
                            scope.data.description = tmp;
                        });
                    }
                });

                function parseFromPBX(sip) {
                    if (!sip) return;
                    if (sip.authorizationType) scope.data.authorizationType = sip.authorizationType;
                    if (sip.ipVoiceTraffic) scope.data.curPublicVoiceIP = sip.ipVoiceTraffic;
                    if (sip.ipSignaling) scope.data.curSignalingIP = sip.ipSignaling;
                    if (sip.transportLayerProtocol) scope.data.transProtocol = sip.transportLayerProtocol;
                    if (sip.signalingPort) scope.data.signalingPort = sip.signalingPort;
                    if (sip.sessionCount) scope.data.sessionsCount = sip.sessionCount;
                    if (sip.voiceTrafficPortStart) scope.data.voicePortStart = sip.voiceTrafficPortStart;
                    if (sip.voiceTrafficPortEnd) scope.data.voicePortEnd = sip.voiceTrafficPortEnd;
                    if (sip.preferredCoding) scope.data.coding = sip.preferredCoding;

                    scope.pbxData = JSON.parse(JSON.stringify(scope.data));
                    scope.pbxData.fetched = true;
                }
            },
            templateUrl: './js/directives/aftersalesPBX/sipProtocol.html'
        };
    });
    module.directive('aftersalesRichText', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                disabled: '=',
                minHeight: '=',
                processId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function (file, path, tmp) {
                    $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'put/' + path, transformResponse: []}).then(
                        function (data, status, headers, config) {
                            $http.put(data, file, {headers: {'Content-Type': undefined}}).then(
                                function () {
                                    $http({
                                        method: 'GET',
                                        url: '/camunda/uploads/' + tmp + 'get/' + path,
                                        transformResponse: []
                                    }).then(
                                        function (data, status, headers, config) {
                                            var image = $('<img>').attr('src', data);
                                            element.summernote("insertNode", image[0]);
                                        },
                                        function (data, status, headers, config) {
                                            console.log(data);
                                        }
                                    );
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        },
                        function (data, status, headers, config) {
                            console.log(data);
                        }
                    );
                };

                element.summernote({
                    focus: true,
                    disableResizeEditor: true,
                    minHeight: (scope.minHeight ? scope.minHeight : 300),
                    callbacks: {
                        onChange: function (content) {
                            scope.data = content;
                            if (!scope.$$phase && !$rootScope.$$phase) {
                                scope.$apply();
                            }
                        },
                        onImageUpload: function (files) {
                            for (var file of files) {
                                if (scope.processId && scope.taskId) {
                                    uploadImage(file, scope.processId + '/' + scope.taskId + '/' + file.name, '');
                                } else {
                                    uploadImage(file, uuid + '/' + file.name, 'tmp/');
                                }
                            }
                        },
                        onMediaDelete: function (target) {
                            console.log("deleted => ", target);
                        },
                        onPaste: function (event) {
                            // Workaround for copying tables from Microsoft Excel
                            $timeout(function () {
                                var html = document.createElement('div');
                                html.innerHTML = element.summernote('code');
                                html.querySelectorAll('table').forEach(function (table) {
                                    $(table).addClass('table-bordered');
                                });
                                element.summernote('code', html.innerHTML);
                            });
                        }
                    },
                    toolbar: [
                        ['style', ['style']],
                        ['font', ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript']],
                        ['fontname', ['fontsize', 'fontname']],
                        ['color', ['color']],
                        ['para', ['ul', 'ol', 'paragraph', 'height']],
                        ['table', ['table']],
                        ['insert', ['link', 'picture', 'video', 'template']],
                        ['view', ['fullscreen', 'codeview', 'help']],
                    ],
                    template: {
                        path: '/summernote-templates',
                        list: {
                            'resume': 'Resume',
                            'resume_cover_letter': 'Resume cover letter',
                            'business_letter': 'Business letter',
                            'flow': 'Flow',
                            'personal_letterhead': 'Personal letterhead',
                            'retrospect': 'Retrospect'
                        }
                    }
                });

                if (scope.disabled) element.summernote('disable');

                scope.$watch('data', function (value) {
                    if (value && value.length) {
                        if (value !== element.summernote('code')) {
                            element.summernote('code', value);
                        }
                    }
                }, true);
            }
        };
    });
    module.directive('infoAftersalesPbx', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                aftersalesInfo: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/aftersalesPBX/infoAftersalesPBX.html'
        };
    }]);
    module.directive('revolvingLegalInformation', function ($rootScope, $http, toasty) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: "=",
                fixed: "="
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.termContract) scope.data.termContract = new Date();
                        else scope.data.termContract = new Date(scope.data.termContract);
                        if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
                    }
                });

                scope.onSalesReprChange = function () {
                    if (!scope.data.salesRepr || !scope.data.salesRepr.length) scope.form.liSalesRepr.$setValidity('not_selected', true);
                };

                scope.getUser = function (val) {
                    scope.data.salesReprId = null;
                    scope.form.liSalesRepr.$setValidity('not_selected', false);
                    var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike=' + encodeURIComponent('%' + val + '%')).then(
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
                                    });
                                } else {
                                    return [];
                                }
                            });

                            return $http.get('/camunda/api/engine/engine/default/user?lastNameLike=' + encodeURIComponent('%' + val + '%')).then(
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
                                            });
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

                scope.userSelected = function ($item) {
                    scope.data.salesReprId = $item.id;
                    scope.data.salesRepr = $item.name;
                    scope.form.liSalesRepr.$setValidity('not_selected', true);
                };

                scope.$watch('data.BIN', function (clientBIN) {
                    if (clientBIN && clientBIN.length === 12) {
                        $http({
                            method: 'GET',
                            url: '/camunda/crm/client/bin/' + clientBIN,
                            transformResponse: []
                        }).then(function (response) {
                            var clientCRM = JSON.parse(response.data);
                            if (clientCRM.accountName) {
                                scope.data.legalName = clientCRM.accountName;

                                if (clientCRM.salesExecutiveUser && clientCRM.salesExecutiveUser.username) {
                                    scope.userSelected({id: clientCRM.salesExecutiveUser.email.replace('[', '').replace(']', '').split('|')[0]});
                                    $http.get("/camunda/api/engine/engine/default/user/" + scope.data.salesReprId + "/profile").then(function (userResponse) {
                                        scope.userSelected({
                                            id: scope.data.salesReprId,
                                            name: userResponse.data.firstName + ' ' + userResponse.data.lastName
                                        });
                                    });
                                }

                                if (clientCRM.city && clientCRM.city.nameRu) {
                                    scope.data.companyCity = clientCRM.city.nameRu;
                                }
                            }
                        });
                    }
                }, true);
            },
            templateUrl: './js/directives/revolvingNumbers/legalInfo.html'
        };
    });
    module.directive('revolvingTechnicalSpecifications', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '=',
                activeConnType: '=',
                callbackForwarding: '='
            },
            link: function (scope, element, attrs) {
                scope.$watch('data', function (value) {
                    if (value) {
                        if (!scope.data.connectionType) scope.data.connectionType = 'SIP over internet';
                        if (!scope.data.vpnRequired) scope.data.vpnRequired = false;
                    }
                });

                scope.$on('tab-selected', function (e, tabName) {
                    if (tabName === 'techSpec') {
                        var pbxTmp = scope.data.pbxNumbers;
                        scope.data.pbxNumbers = 'this is because of tabset';
                        var descriptionTmp = scope.data.description;
                        scope.data.description = 'this is because of tabset';
                        $timeout(function () {
                            scope.data.pbxNumbers = pbxTmp;
                            scope.data.description = descriptionTmp;
                        });
                    }
                });
            },
            templateUrl: './js/directives/revolvingNumbers/techSpecs.html'
        };
    });
    module.directive('revolvingSipProtocol', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function (scope, element, attrs) {
            },
            templateUrl: './js/directives/revolvingNumbers/sipProtocol.html'
        };
    });
    module.directive('revolvingDirectProtocol', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                form: '=',
                view: '=',
                disabled: '='
            },
            link: function (scope, element, attrs) {
                scope.onChannelCapacityChange = function () {
                    if (!scope.data.sessionsCount || !scope.data.coding) return;
                    if (scope.data.coding === 'g711') {
                        scope.data.channelCapacity = 87.2 * scope.data.sessionsCount / 1024;
                    } else {
                        scope.data.channelCapacity = 32.2 * scope.data.sessionsCount / 1024;
                    }
                };
            },
            templateUrl: './js/directives/revolvingNumbers/directProtocol.html'
        };
    });
    module.directive('pbxRevolvingNumbersInfo', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/revolvingNumbers/info.html'
        };
    }]);
    module.directive('demandProcess', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            link: function (scope, el, attrs) {
            },
            templateUrl: './js/directives/demandProcess.html'
        };
    }]);
    module.directive('bulksmsRichText', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                disabled: '=',
                minHeight: '=',
                processId: '=',
                taskId: '='
            },
            link: function (scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function (file, path, tmp) {
                    $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'put/' + path, transformResponse: []}).then(
                        function (data, status, headers, config) {
                            $http.put(data, file, {headers: {'Content-Type': undefined}}).then(
                                function () {
                                    $http({
                                        method: 'GET',
                                        url: '/camunda/uploads/' + tmp + 'get/' + path,
                                        transformResponse: []
                                    }).then(
                                        function (data, status, headers, config) {
                                            var image = $('<img>').attr('src', data);
                                            element.summernote("insertNode", image[0]);
                                        },
                                        function (data, status, headers, config) {
                                            console.log(data);
                                        }
                                    );
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        },
                        function (data, status, headers, config) {
                            console.log(data);
                        }
                    );
                };

                element.summernote({
                    focus: true,
                    disableResizeEditor: true,
                    minHeight: (scope.minHeight ? scope.minHeight : 300),
                    callbacks: {
                        onChange: function (content) {
                            scope.data = content;
                            if (!scope.$$phase && !$rootScope.$$phase) {
                                scope.$apply();
                            }
                        },
                        onImageUpload: function (files) {
                            for (var file of files) {
                                if (scope.processId && scope.taskId) {
                                    uploadImage(file, scope.processId + '/' + scope.taskId + '/' + file.name, '');
                                } else {
                                    uploadImage(file, uuid + '/' + file.name, 'tmp/');
                                }
                            }
                        },
                        onMediaDelete: function (target) {
                            console.log("deleted => ", target);
                        },
                        onPaste: function (event) {
                            // Workaround for copying tables from Microsoft Excel
                            $timeout(function () {
                                var html = document.createElement('div');
                                html.innerHTML = element.summernote('code');
                                html.querySelectorAll('table').forEach(function (table) {
                                    $(table).addClass('table-bordered');
                                });
                                element.summernote('code', html.innerHTML);
                            });
                        }
                    },
                    toolbar: [
                        ['style', ['style']],
                        ['font', ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript']],
                        ['fontname', ['fontsize', 'fontname']],
                        ['color', ['color']],
                        ['para', ['ul', 'ol', 'paragraph', 'height']],
                        ['table', ['table']],
                        ['insert', ['link', 'picture', 'video', 'template']],
                        ['view', ['fullscreen', 'codeview', 'help']],
                    ],
                    template: {
                        path: '/summernote-templates',
                        list: {
                            'resume': 'Resume',
                            'resume_cover_letter': 'Resume cover letter',
                            'business_letter': 'Business letter',
                            'flow': 'Flow',
                            'personal_letterhead': 'Personal letterhead',
                            'retrospect': 'Retrospect'
                        }
                    }
                });

                if (scope.disabled) element.summernote('disable');

                scope.$watch('data', function (value) {
                    if (value && value.length) {
                        if (value !== element.summernote('code')) {
                            element.summernote('code', value);
                        }
                    }
                }, true);
            }
        };
    });
    module.directive('selectPicker', function ($timeout) {
        return {
            restrict: 'A',
            //scope: false,
            //controller: '@',
            priority: 1000, // same as ng-controller
            link: function (scope, element, attrs) {
                function refresh() {
                    scope.$applyAsync(function () {
                        $('.selectpicker').selectpicker('refresh');
                    });
                }


                $(function () {
                    $('.selectpicker').on('show.bs.select', function () {

                        var dropdownList = $('.dropdown-menu'),
                            dropdownOffset = $(this).offset(),
                            offsetLeft = dropdownOffset.left,
                            dropdownWidth = dropdownList.width(),
                            docWidth = $(window).width(),

                            subDropdown = dropdownList.eq(1),
                            subDropdownWidth = subDropdown.width(),

                            isDropdownVisible = (offsetLeft + dropdownWidth <= docWidth),
                            isSubDropdownVisible = (offsetLeft + dropdownWidth + subDropdownWidth <= docWidth);

                        if (!isDropdownVisible || !isSubDropdownVisible) {
                            dropdownList.addClass('pull-right');
                        } else {
                            dropdownList.removeClass('pull-right');
                        }
                    });
                });


                $timeout(function () {
                    $('.selectpicker').selectpicker("");
                });
                if (attrs.ngModel) {
                    scope.$watch(attrs.ngModel, refresh, true);
                }
                if (attrs.selectModel) {
                    scope.$watch(attrs.selectModel, refresh, true);
                }
                if (attrs.ngDisabled) {
                    scope.$watch(attrs.ngDisabled, refresh, true);
                }
                if (attrs.ngRequired) {
                    scope.$watch(attrs.ngRequired, refresh, true);
                }


            }
        };
    });

    var myApp = angular.module('ui.bootstrap');

    function Decorate($provide, datepickerPopupConfig, datepickerConfig) {
        datepickerPopupConfig.showButtonBar = false;
        datepickerConfig.showCustomRangeLabel = true;
        datepickerConfig.startingDay = 1;
        $provide.decorator('daypickerDirective', function ($delegate, $rootScope) {
            var directive = $delegate[0];
            var holidays = $rootScope.holidays;
            var weekendWorking = $rootScope.weekendWorking;
            var holidaysForYear = $rootScope.holidaysForYear;
            var doNotMarkNextHoliday = $rootScope.doNotMarkNextHoliday;
            directive.templateUrl = './js/directives/customCalendar/dayPicker.html';
            var link = directive.link;
            directive.compile = function () {
                return function (scope, element, attrs, ngModelCtrl) {
                    link.apply(this, arguments);
                    //scope.weekends = holidays;

                    scope.$watchGroup(['rows'], function () {
                        var mark_next = 0;
                        angular.forEach(scope.rows, function (row) {
                            angular.forEach(row, function (day) {
                                var year = day.date.getFullYear();
                                var month = day.date.getMonth() + 1;
                                var thisDay = day.date.getDate();
                                var day_type = day.date.getDay();
                                if (holidays.includes(thisDay + '/' + month) || holidaysForYear.includes(thisDay + '/' + month + '/' + year)) {
                                    day.holiday = true;
                                }
                                if (mark_next > 0 && !day.holiday && !(day_type === 0 || day_type === 6)) {
                                    day.holiday = true;
                                    mark_next -= 1;
                                }
                                if (day.holiday && (day_type === 6 || day_type === 0) && !holidaysForYear.includes(thisDay + '/' + month + '/' + year) && !doNotMarkNextHoliday.includes(thisDay + '/' + month + '/' + year)) {
                                    mark_next += 1;
                                }
                                if (!day.holiday && (day_type === 6 || day_type === 0) &&
                                    !weekendWorking.includes(thisDay + '/' + month + '/' + year)) {
                                    day.weekDay = true;
                                }
                            });
                        });
                    });
                };
            };
            return $delegate;
        });

        $provide.decorator('datepickerPopupWrapDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/calendarPopUp.html';
            return $delegate;
        });
        $provide.decorator('monthpickerDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/monthPicker.html';
            return $delegate;
        });
        $provide.decorator('yearpickerDirective', function ($delegate) {
            var directive = $delegate[0];
            directive.templateUrl = './js/directives/customCalendar/yearPicker.html';
            return $delegate;
        });

    }

    myApp.config(['$provide', 'datepickerPopupConfig', "datepickerConfig", Decorate]);
    module.directive('calendarRange', function ($timeout, $parse, $rootScope) {
        return {
            restrict: 'A',
            //scope: false,
            //controller: '@',
            priority: 1000, // same as ng-controller
            link: function (scope, element, attrs) {
                var element_name = element[0].name;
                var startDate;
                var endDate;
                var holidays = $rootScope.holidays;
                var weekend_working = $rootScope.weekendWorking;
                var holidaysForYear = $rootScope.holidaysForYear;
                var doNotMarkNextHoliday = $rootScope.doNotMarkNextHoliday;
                var substitues = [];
                var substitude_done = [];
                for (var i = 0; i < holidays.length; i++) {
                    substitude_done.push(false);
                }

                let setMultipleDatePicker = () => {
                    $('input[name="' + element_name + '"]').daterangepicker({
                            startDate: startDate,
                            endDate: endDate,
                            autoUpdateInput: false,
                            linkedCalendars: false,
                            autoApply: true,
                            showDropdowns: true,
                            isCustomDate: function (date) {
                                var day_type = date.toDate().getDay();
                                var format_date = date.format('D/M');
                                var format_date_year = date.format('D/M/YYYY');
                                var next_day = "";
                                var with_year = "";
                                var return_class = "";
                                if ((day_type === 6 || day_type === 0) &&
                                    weekend_working.indexOf(format_date_year) === -1) {
                                    return_class = 'calendar-weekday ';
                                }

                                if (holidays.indexOf(format_date) > -1) {
                                    var days_to_jump = 1;
                                    if ((day_type === 6 || day_type === 0) && doNotMarkNextHoliday.indexOf(format_date_year) === -1) {
                                        if (day_type === 6) {
                                            days_to_jump += 1;
                                        }
                                        next_day = getNextDay(date, days_to_jump)[0];
                                        with_year = getNextDay(date, days_to_jump)[1];
                                        while (holidays.indexOf(next_day) !== -1 ||
                                        (!substitude_done[holidays.indexOf(format_date)] && substitues.indexOf(with_year) !== -1)) {
                                            days_to_jump += 1;
                                            next_day = getNextDay(date, days_to_jump)[0];
                                            with_year = getNextDay(date, days_to_jump)[1];
                                            //substitude_done[substitues.indexOf(with_year)] = false;
                                        }
                                        substitude_done[holidays.indexOf(format_date)] = true;
                                        if (substitues.indexOf(with_year) === -1) {
                                            substitues.push(with_year);
                                        }
                                    }
                                    return_class += 'calendar-holiday';
                                } else if (substitues.indexOf(date.format('D/M/YYYY')) > -1) {
                                    return_class += 'calendar-holiday';
                                }
                                if (holidaysForYear.indexOf(format_date_year) > -1) {
                                    return_class += 'calendar-holiday';
                                }
                                return return_class;

                            },
                            locale: {
                                format: 'DD.MM.YYYY',
                                cancelLabel: 'Clear',
                                firstDay: 1
                            }
                        }
                    );

                    function getNextDay(date, index) {
                        var tomorrow = new Date(date.toDate());
                        tomorrow.setDate(tomorrow.getDate() + index);
                        var month = tomorrow.getMonth() + 1;
                        var next_day = tomorrow.getDate();
                        var year = tomorrow.getFullYear();
                        var a = next_day + '/' + month;
                        var b = next_day + '/' + month + '/' + year;
                        return [a, b];
                    }

                    $('input[name="' + element_name + '"]').on('apply.daterangepicker', function (ev, picker) {

                        var x = $(this).attr('ng-model');
                        var model = $parse(x);
                        scope.$apply(function () {
                            model.assign(scope, picker.startDate.format('DD.MM.YY') + ' - ' + picker.endDate.format('DD.MM.YY'));
                        });
                    });


                    element.on('click', function (e) {
                        $('input[name="' + element_name + '"]').click();
                    });

                };
                $timeout(setMultipleDatePicker);


            }
        };
    });
    module.directive('infoAftersalesSearch', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/aftersales/afsearch.html'
        };
    }]);
    module.directive('summernoteRichText', function ($rootScope, $http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                data: '=',
                disabled: '=',
                minHeight: '=',
                processId: '=',
                taskId: '=',
                templates: '='
            },
            link: function (scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function (file, path, tmp) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/' + tmp + 'put/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        $http.put(response.data, file, {headers: {'Content-Type': undefined}}).then(
                            function () {
                                $http({
                                    method: 'GET',
                                    url: '/camunda/uploads/' + tmp + 'get/' + path,
                                    transformResponse: []
                                }).then(function (response) {
                                    var image = $('<img>').attr('src', response.data);
                                    element.summernote("insertNode", image[0]);
                                }, function (error) {
                                    console.log(error.data);
                                });
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }, function (error) {
                        console.log(error.data);
                    });
                };

                let toolbarList = [
                    ['style', ['style']],
                    ['font', ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript']],
                    ['fontname', ['fontsize', 'fontname']],
                    ['color', ['color']],
                    ['para', ['ul', 'ol', 'paragraph', 'height']],
                    ['table', ['table']],
                    ['insert', ['link', 'unlink', 'picture', 'video']],
                    ['view', ['fullscreen', 'codeview', 'help']],
                ];

                if (scope.templates && Object.keys(scope.templates).length) toolbarList[6][1].push('template');
                if (scope.disabled) toolbarList = [];

                element.summernote({
                    focus: false,
                    disableResizeEditor: true,
                    minHeight: scope.disabled ? 30 : (scope.minHeight ? scope.minHeight : 300),
                    callbacks: {
                        onChange: function (content) {
                            if (content === '<p><br></p>') content = '';
                            scope.data = content;
                            if (!scope.$$phase && !$rootScope.$$phase) {
                                scope.$apply();
                            }
                        },
                        onImageUpload: function (files) {
                            for (var file of files) {
                                if (scope.processId && scope.taskId) {
                                    uploadImage(file, scope.processId + '/' + scope.taskId + '/' + file.name, '');
                                } else {
                                    uploadImage(file, uuid + '/' + file.name, 'tmp/');
                                }
                            }
                        },
                        onMediaDelete: function (target) {
                            console.log("deleted => ", target);
                        }
                    },
                    toolbar: toolbarList,
                    template: {
                        path: '/summernote-templates',
                        list: scope.templates
                    },
                    popover: {
                        link: []
                    }
                });
                $('.note-statusbar').hide();
                $('.note-status-output').hide();
                $('.note-editor .link-dialog .checkbox').hide();

                if (scope.disabled) {
                    element.summernote('disable');
                    $('.note-editor > .note-editing-area').css('background', '#eee');
                    $('.note-editor > .note-editing-area > .note-editable').css('padding', '4px 10px 0 10px');
                }

                scope.$watch('data', function (value) {
                    if (value && value.length) {
                        if (value !== element.summernote('code')) {
                            // Workaround to force links open in new window
                            var html = document.createElement('div');
                            html.innerHTML = value;
                            html.querySelectorAll('a').forEach(function (link) {
                                $(link).attr('target', '_blank');
                            });
                            element.summernote('code', html.innerHTML);
                        }
                    }
                }, true);
            }
        };
    });
    module.directive('networkArchitectureSearch', ['$http', '$timeout', '$q', '$rootScope', 'exModal', 'toasty', function ($http, $timeout, $q, $rootScope, exModal, toasty) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                var baseUrl = '/camunda/api/engine/engine/default';
                var catalogs = {};
                scope.dismantleCatalogs = {};
                scope.leasingCatalogs = {};
                scope.isCustomField = false;

                $http.get($rootScope.getCatalogsHttpByName('catalogs')).then(
                    function (result) {
                        angular.extend(scope, result.data);
                        angular.extend(catalogs, result.data);

                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                $http.get($rootScope.getCatalogsHttpByName('dismantleCatalogs')).then(
                    function (result) {
                        angular.extend(scope.dismantleCatalogs, result.data);
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                $http.get($rootScope.getCatalogsHttpByName('leasingCatalogs')).then(
                    function (result) {
                        angular.extend(scope.leasingCatalogs, result.data);
                        scope.leasingCatalogs.rbsTypes = [];
                        angular.forEach(scope.leasingCatalogs.rbsType, function (rbs) {
                            if (scope.leasingCatalogs.rbsTypes.indexOf(rbs.rbsType) < 0) {
                                scope.leasingCatalogs.rbsTypes.push(rbs.rbsType);
                            }
                        });
                        scope.leasingCatalogs.legalTypeTitle = _.keyBy(scope.leasingCatalogs.legalType, 'id');
                        scope.leasingCatalogs.initiatorTitle = _.keyBy(scope.leasingCatalogs.initiators, 'id');
                        scope.leasingCatalogs.allUnionProjects = [];
                        try {
                            scope.leasingCatalogs.projects.forEach(pbi => {
                                scope.leasingCatalogs.allUnionProjects = _.uniqBy(scope.leasingCatalogs.allUnionProjects.concat(pbi.project.filter(p => {return (p.id !== null && p.id !== 'null')})), pr => {return pr.id + pr.name });
                            });
                            // console.log(scope.leasingCatalogs.allUnionProjects)
                        } catch (error) {
                            console.log(error)
                        }
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
                    },
                    leasing: {
                        title: "Roll-out", value: false
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
                    '4': 'LSE',
                    '5': 'Kcell_region'
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
                        if ((filtered.Revision || filtered.Invoice) && !filtered.leasing && !filtered.Dismantle && !filtered.Replacement) {
                            scope.RevisionOrMonthlyAct = true;
                        }
                        angular.forEach(filtered, function (process, key) {
                            scope.selectedProcessInstances.push(key);
                        });
                    }
                    if (scope.onlyProcessActive !== 'Revision') {
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
                    if (scope.onlyProcessActive !== 'Dismantle') {
                        scope.filter.dismantlingInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive !== 'Replacement') {
                        scope.filter.replacementInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive !== 'Revision' && scope.onlyProcessActive !== 'Dismantle' && scope.onlyProcessActive !== 'Replacement') {
                        scope.filter.requestedDateRange = undefined;
                        scope.filter.requestor = undefined;
                    }
                    if (scope.onlyProcessActive !== 'Invoice') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive === 'Invoice') {
                        // siteId&sitename are common filters except if Invoice selected
                        scope.filter.siteId = undefined;
                        scope.filter.sitename = undefined;
                    }
                    if (scope.onlyProcessActive !== 'leasing') {
                        scope.filter.leasingCandidateLegalType = undefined;
                        scope.filter.leasingRbsType = undefined;
                        scope.filter.leasingFarEndLegalType = undefined;
                        scope.filter.ncpId = undefined;
                        scope.filter.leasingCabinetType = undefined;
                        scope.filter.leasingKazakhtelecomBranch = undefined;
                        scope.filter.leasingSiteType = undefined;
                        scope.filter.leasingBand = undefined;
                        scope.filter.leasingContractType = undefined;
                        scope.filter.leasingInitiator = undefined;
                        scope.filter.leasingContractExecutor = undefined;
                        scope.filter.leasingProject = undefined;
                        scope.filter.bin = undefined;
                        scope.filter.leasingActivityId = undefined;
                        scope.filter.leasingReason = undefined;
                        scope.filter.leasingInstallationStatus = undefined;
                        scope.filter.leasingGeneralStatus = undefined;
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

                function downloadXML(process) {
                    $http.get(baseUrl + '/process-definition/key/' + process + '/xml')
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

                            if (process === 'Revision') {
                                var excludeTasks = [
                                    'signpr_by_center',
                                    'signpr_by_manager',
                                    'signpr_by_budgetowner',
                                    'signpr_by_cto',
                                    'signpr_by_cfo',
                                    'signpr_by_ceo',
                                    'please_provide_fa',
                                    'create_sloc_sap_enter_db',
                                    'provide_info_sloc_creation',
                                    'correct_jo_file',
                                    'get_sap_code_accountant_and_enter_form',
                                    'please_provide_fa',
                                    'correct_pr_file',
                                    'IntermediateThrowEvent_wait_po_pr_creation',
                                    'IntermediateThrowEvent_wait_pr_result',
                                    'IntermediateThrowEvent_wait_result_jo_file',
                                    'IntermediateThrowEvent_wait_fa_result'
                                ];

                                var userTasks = getUserTasks(xml);
                                var includedUserTasks = _.filter(userTasks, function (task) {
                                    return excludeTasks.indexOf(task.id) === -1;
                                });
                                scope.includedUserTasks = includedUserTasks;
                                var userTasksMap = _.keyBy(includedUserTasks, 'id');
                                scope.userTasksMap = userTasksMap;
                            } else if (process === 'sdr_srr_request') {
                                scope.dismantleUserTasks = getUserTasks(xml);
                            } else if (process === 'leasing') {
                                scope.leasingUserTasks = getUserTasks(xml);
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
                scope.InvoicePeriodYears = [currentDate.getFullYear(), currentDate.getFullYear() - 1, currentDate.getFullYear() - 2];
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
                        if (scope.onlyProcessActive === 'Revision' && scope.selectedCustomFields.length > 0) {
                            tbl = document.getElementById('customXlsxRevisionsTable');
                        }
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
                    angular.forEach(scope.selectedProcessInstances, function (item) {
                        if (item === 'Dismantle' || item === 'Replacement') {
                            if (selectedProcessInstances.indexOf('sdr_srr_request') === -1) {
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
                        activeActivityIdIn: [],
                        processInstanceBusinessKeyLike: '%-%',
                        processDefinitionKeyNotIn: excludeProcesses

                    }
                    if (scope.selectedProcessInstances.indexOf('Dismantle') !== -1 && scope.selectedProcessInstances.indexOf('Replacement') === -1) {
                        filter.variables.push({"name": "requestType", "operator": "eq", "value": "dismantle"});
                    }
                    if (scope.selectedProcessInstances.indexOf('Replacement') !== -1 && scope.selectedProcessInstances.indexOf('Dismantle') === -1) {
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
                            filter.processInstanceBusinessKeyLike = '%' + scope.filter.businessKey + '%';
                        }
                    }
                    if (scope.filter.workType) {
                        if (scope.onlyProcessActive === 'Invoice')
                            filter.variables.push({
                                "name": "workType",
                                "operator": "eq",
                                "value": scope.filter.workType
                            });
                        else if (scope.onlyProcessActive === 'Revision')
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
                            function (tasks) {
                                if (!filter.processInstanceIds) filter.processInstanceIds = _.map(tasks.data, 'processInstanceId');
                                else filter.processInstanceIds = filter.processInstanceIds.filter(value => -1 !== _.map(tasks.data, 'processInstanceId').indexOf(value));

                                asynCall1 = true;
                                if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                                    scope.lastSearchParamsRevision = filter;
                                    getProcessInstances(filter, 'processInstances');
                                    asynCall1 = false;
                                }
                            },
                            function (error) {
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
                        if (scope.onlyProcessActive === 'Revision') {
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
                        } else if (scope.onlyProcessActive === 'Dismantle') {
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
                        } else if (scope.onlyProcessActive === 'Replacement') {
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
                    if (scope.filter.participation && scope.onlyProcessActive === 'Revision') {
                        if (!scope.filter.requestor) {
                            toasty.error({title: "Error", msg: 'Please fill field Requestor!'});
                            return;
                        }

                        if (['participant', 'iamparticipant'].indexOf(scope.filter.participation) !== -1) {
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


                    if (scope.filter.activityId && scope.onlyProcessActive === 'Revision') {
                        filter.activeActivityIdIn.push(scope.filter.activityId);
                    }
                    if (scope.filter.dismantleActivityId && (scope.onlyProcessActive === 'Dismantle' || scope.onlyProcessActive === 'Replacement')) {
                        filter.activeActivityIdIn.push(scope.filter.dismantleActivityId);
                    }
                    if (scope.filter.mainContract && scope.filter.mainContract !== 'All') {
                        filter.variables.push({
                            "name": "mainContract",
                            "operator": "eq",
                            "value": scope.filter.mainContract
                        });
                    }
                    if (scope.onlyProcessActive === 'leasing') {
                        if (scope.filter.leasingCandidateLegalType) {
                            filter.variables.push({
                                "name": "leasingCandidateLegalType",
                                "operator": "eq",
                                "value": scope.filter.leasingCandidateLegalType
                            });
                        }
                        if (scope.filter.leasingRbsType) {
                            filter.variables.push({
                                "name": "rbsType",
                                "operator": "eq",
                                "value": scope.filter.leasingRbsType
                            });
                        }
                        if (scope.filter.leasingFarEndLegalType) {
                            filter.variables.push({
                                "name": "leasingFarEndLegalType",
                                "operator": "like",
                                "value": '%,' + scope.filter.leasingFarEndLegalType + ',%'
                            });
                        }
                        if (scope.filter.ncpId) {
                            filter.variables.push({"name": "ncpID", "operator": "eq", "value": scope.filter.ncpId});
                        }
                        if (scope.filter.leasingCabinetType) {
                            filter.variables.push({
                                "name": "plannedCabinetType",
                                "operator": "eq",
                                "value": scope.filter.leasingCabinetType
                            });
                        }
                        if (scope.filter.leasingKazakhtelecomBranch && scope.filter.leasingCandidateLegalType === 'national_kazakhtelecom') {
                            filter.variables.push({
                                "name": "branchKT",
                                "operator": "eq",
                                "value": scope.filter.leasingKazakhtelecomBranch
                            });
                        }
                        if (scope.filter.leasingSiteType) {
                            filter.variables.push({
                                "name": "siteTypeForSearch",
                                "operator": "eq",
                                "value": scope.filter.leasingSiteType
                            });
                        }
                        if (scope.filter.leasingBand) {
                            filter.variables.push({
                                "name": "bandsJoinedByComma",
                                "operator": "like",
                                "value": '%,' + scope.filter.leasingBand + ',%'
                            });
                        }
                        if (scope.filter.leasingContractType) {
                            filter.variables.push({
                                "name": "contractTypeJoinedByComma",
                                "operator": "like",
                                "value": '%,' + scope.filter.leasingContractType + ',%'
                            });
                        }
                        if (scope.filter.leasingInitiator) {
                            filter.variables.push({
                                "name": "initiatorForSearch",
                                "operator": "eq",
                                "value": scope.filter.leasingInitiator
                            });
                        }
                        if (scope.filter.leasingGeneralStatus) {
                            filter.variables.push({
                                "name": "generalStatus",
                                "operator": "eq",
                                "value": scope.filter.leasingGeneralStatus
                            });
                        }
                        if (scope.filter.leasingContractExecutor) {
                            filter.variables.push({
                                "name": "contractExecutorJoinedByComma",
                                "operator": "like",
                                "value": '%,' + scope.filter.leasingContractExecutor.name + ',%'
                            });
                        }
                        if (scope.filter.leasingProject) {
                            filter.variables.push({
                                "name": "projectForSearch",
                                "operator": "eq",
                                "value": scope.filter.leasingProject
                            });
                        }
                        if (scope.filter.leasingInstallationStatus) {
                            filter.variables.push({
                                "name": "installationStatus",
                                "operator": "eq",
                                "value": scope.filter.leasingInstallationStatus
                            });
                        }
                        if (scope.filter.bin) {
                            filter.variables.push({
                                "name": "contractBinsJoinedByComma",
                                "operator": "like",
                                "value": '%,' + scope.filter.bin + ',%'
                            });
                        }
                        if (scope.filter.contractsForSearch) {
                            filter.variables.push({
                                "name": "contractsForSearch",
                                "operator": "like",
                                "value": '%,' + scope.filter.contractsForSearch + ',%'
                            });
                        }
                        if (scope.filter.leasingActivityId) {
                            filter.activeActivityIdIn.push(scope.filter.leasingActivityId);
                        }
                        if (scope.filter.leasingReason) {
                            filter.variables.push({
                                "name": "reasonForSearch",
                                "operator": "eq",
                                "value": scope.filter.leasingReason
                            });
                        }
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
                    scope.filter.beginYear = scope.currentDate.getFullYear() - 1;
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
                    scope.filter.dismantlingInitiator = undefined;
                    scope.filter.replacementInitiator = undefined;
                    scope.filter.dismantleActivityId = undefined;

                    scope.filter.leasingCandidateLegalType = undefined;
                    scope.filter.leasingRbsType = undefined;
                    scope.filter.leasingFarEndLegalType = undefined;
                    scope.filter.ncpId = undefined;
                    scope.filter.leasingCabinetType = undefined;
                    scope.filter.leasingKazakhtelecomBranch = undefined;
                    scope.filter.leasingSiteType = undefined;
                    scope.filter.leasingBand = undefined;
                    scope.filter.leasingContractType = undefined;
                    scope.filter.leasingInitiator = undefined;
                    scope.filter.leasingContractExecutor = undefined;
                    scope.filter.leasingProject = undefined;
                    scope.filter.bin = undefined;
                    scope.filter.leasingActivityId = undefined;
                    scope.filter.leasingReason = undefined;
                    scope.filter.leasingInstallationStatus = undefined;
                    scope.filter.leasingGeneralStatus = undefined;
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

                            if (scope.selectedProcessInstances.indexOf('Dismantle') !== -1 || scope.selectedProcessInstances.indexOf('Replacement') !== -1) {
                                variables.push('requestType');
                            }
                            if (scope.selectedProcessInstances.indexOf('leasing') !== -1) {
                                variables.push('generalStatus');
                                variables.push('generalStatusUpdatedDate');
                                variables.push('installationStatusUpdatedDate');
                                if (scope.filter.leasingInitiator) {
                                    variables.push('initiator');
                                }
                                if (scope.filter.leasingProject) {
                                    variables.push('project');
                                }
                                if (scope.filter.leasingReason) {
                                    variables.push('reason');
                                }
                                if (scope.filter.ncpId) {
                                    variables.push('ncpID');
                                }
                                if (scope.filter.leasingSiteType) {
                                    variables.push('siteType');
                                }
                                if (scope.filter.leasingRbsType) {
                                    variables.push('rbsType');
                                }
                                if (scope.filter.leasingCabinetType) {
                                    variables.push('plannedCabinetType');
                                }
                                if (scope.filter.leasingBand) {
                                    variables.push('bands');
                                }
                                if (scope.filter.leasingInstallationStatus) {
                                    variables.push('installationStatus');
                                }
                                if (scope.filter.leasingCandidateLegalType) {
                                    variables.push('leasingCandidateLegalType');
                                }
                                if (scope.filter.leasingKazakhtelecomBranch && scope.filter.leasingCandidateLegalType === 'national_kazakhtelecom') {
                                    variables.push('branchKT');
                                }
                                if (scope.filter.leasingFarEndLegalType) {
                                    variables.push('farEndInformation');
                                }
                                if (scope.filter.leasingContractType || scope.filter.leasingContractExecutor || scope.filter.bin) {
                                    variables.push('contractInformations');
                                }
                            }
                            if (scope.selectedProcessInstances.indexOf('Revision') !== -1) {
                                variables.push('monthActNumber');
                                variables.push('invoiceRO1Number');
                                variables.push('invoiceRO2Number');
                                variables.push('invoiceRO3Number');
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

                scope.getPageInstances = function () {
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
                        if (process === 'Revision' && !scope.KWMSProcesses[process].downloaded) {
                            downloadXML('Revision');
                            scope.KWMSProcesses[process].downloaded = true;
                        } else if ((process === 'Dismantle' || process === 'Replacement') && !scope.KWMSProcesses[process].downloaded) {
                            downloadXML('sdr_srr_request');
                            scope.KWMSProcesses[process].downloaded = true;
                        } else if (process === 'leasing' && !scope.KWMSProcesses[process].downloaded) {
                            downloadXML('leasing');
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
                        size: (scope.jobModel.processDefinitionKey === 'Invoice' ? 'hg' : 'lg')
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
                            url: baseUrl + '/task?processInstanceBusinessKey=' + encodeURIComponent(businessKey),
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
                                                            openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        } else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls += 1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalDismantle(processDefinitionId, businessKey, index);
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
                            });
                    }
                };

                function openProcessCardModalDismantle(processDefinitionId, businessKey, index) {
                    exModal.open({
                        scope: {
                            dismantleInfo: scope.dismantleInfo,
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails: scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.dismantleCatalogs,
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
                            }
                        },
                        templateUrl: './js/partials/dismantleCardModal.html',
                        size: 'lg'
                    }).then(function (results) {
                    });
                }

                scope.toggleProcessViewReplacement = function (index, processDefinitionKey, processDefinitionId, businessKey) {
                    scope.showDiagramView = false;
                    scope.diagram = {};
                    if (scope.piIndex === index) {
                        scope.piIndex = undefined;
                    } else {
                        scope.piIndex = index;
                        scope.replacementInfo = {
                            processDefinitionKey: processDefinitionKey,
                            startTime: {value: scope.processInstances[index].startTime}
                        };
                        $http({
                            method: 'GET',
                            headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                            url: baseUrl + '/task?processInstanceBusinessKey=' + encodeURIComponent(businessKey),
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
                                                            openProcessCardModalReplacement(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        } else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls += 1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalReplacement(processDefinitionId, businessKey, index);
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
                                        openProcessCardModalReplacement(processDefinitionId, businessKey, index);
                                        asynCall1 = false;
                                    }
                                }
                                $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstances[index].id).then(
                                    function (result) {
                                        var workFiles = [];
                                        result.data.forEach(function (el) {
                                            scope.replacementInfo[el.name] = el;
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                scope.replacementInfo[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                scope.replacementInfo[el.name].value = JSON.parse(el.value);
                                            }
                                        });
                                        if (scope.replacementInfo.resolutions && scope.replacementInfo.resolutions.value) {
                                            $q.all(scope.replacementInfo.resolutions.value.map(function (resolution) {
                                                return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                            })).then(function (tasks) {
                                                tasks.forEach(function (e, index) {
                                                    if (e.data.length > 0) {
                                                        scope.replacementInfo.resolutions.value[index].taskName = e.data[0].name;
                                                        try {
                                                            scope.replacementInfo.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                        } catch (e) {
                                                            console.log(e);
                                                        }
                                                    }
                                                });
                                                asynCall2 = true;
                                                if (asynCall1 && asynCall2) {
                                                    openProcessCardModalReplacement(processDefinitionId, businessKey, index);
                                                    asynCall2 = false;
                                                } else console.log('asynCall 1 problem');
                                            });
                                        }
                                        scope.replacementInfo.tasks = processInstanceTasks;
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );

                            },
                            function (error) {
                                console.log(error.data);
                            });
                    }
                };

                function openProcessCardModalReplacement(processDefinitionId, businessKey, index) {
                    exModal.open({
                        scope: {
                            replacementInfo: scope.replacementInfo,
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails: scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.dismantleCatalogs,
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
                            }
                        },
                        templateUrl: './js/partials/replacementCardModal.html',
                        size: 'lg'
                    }).then(function (results) {
                    });
                }

                scope.putCurrentUserAsRequestor = function () {
                    if (scope.filter.participation === 'iaminitiator' || scope.filter.participation === 'iamparticipant') {
                        scope.filter.requestor = $rootScope.authentication.name;
                    }
                }

                // scope.$watch('filter.leasingInitiator', function (leasingInitiator) {
                //     if (leasingInitiator) {
                //         scope.projectsByInitiator = {};
                //         scope.reasonsByProject = [];
                //         scope.projectsByInitiator = _.find(scope.leasingCatalogs.projects, function (p) {
                //             return p.initiator === scope.leasingCatalogs.initiatorTitle[leasingInitiator].name;
                //         });
                //         scope.leasingCatalogs.projectTitle = _.keyBy(scope.projectsByInitiator.project, 'id');
                //     }
                // }, true);

                // scope.$watch('filter.leasingProject', function (leasingProject) {
                //     if (leasingProject) {
                //         scope.reasonsByProject = [];
                //         scope.reasonsByProject = _.filter(scope.leasingCatalogs.reasons, function (r) {
                //             if (_.find(r.project, function (p) {
                //                 return p === scope.leasingCatalogs.projectTitle[leasingProject].name;
                //             })) {
                //                 return true
                //             } else return false;
                //         });
                //     }
                // }, true);

                scope.toggleProcessViewLeasing = function (index, processDefinitionKey, processDefinitionId, businessKey) {
                    scope.showDiagramView = false;
                    scope.diagram = {};
                    if (scope.piIndex === index) {
                        scope.piIndex = undefined;
                    } else {
                        scope.piIndex = index;
                        scope.leasingInfo = {
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
                                        $http.get("/camunda/api/engine/engine/default/history/identity-link-log?type=assignee&taskId=" + e.id + "&operationType=add&sortBy=time&sortOrder=desc").then(function (result) {
                                            if(result.data.length > 0){
                                                try {
                                                   e.claimedDate = new Date(result.data[0].time);
                                                } catch(e){
                                                    console.log(e);
                                                }
                                            }

                                        });
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
                                                            openProcessCardModalLeasing(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        } else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls += 1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalLeasing(processDefinitionId, businessKey, index);
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
                                        openProcessCardModalLeasing(processDefinitionId, businessKey, index);
                                        asynCall1 = false;
                                    }
                                }
                                $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstances[index].id).then(
                                    function (result) {
                                        var files = [];
                                        var uploadRSDandVSDfilesFiles = [];
                                        var uploadTSDfileFiles = [];
                                        var createdRSDFile = [];
                                        result.data.forEach(function (el) {
                                            scope.leasingInfo[el.name] = el;
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                scope.leasingInfo[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                try {
                                                    scope.leasingInfo[el.name].value = JSON.parse(el.value);
                                                    if (el.name.toLowerCase().includes('file')) {
                                                        if (el.name === 'createdRSDFile') {
                                                            createdRSDFile = createdRSDFile.concat(scope.leasingInfo[el.name].value)
                                                        } else if (el.name === 'uploadRSDandVSDfilesFiles') {
                                                            uploadRSDandVSDfilesFiles = uploadRSDandVSDfilesFiles.concat(scope.leasingInfo[el.name].value)
                                                        } else if (el.name === 'uploadTSDfileFiles') {
                                                            uploadTSDfileFiles = uploadTSDfileFiles.concat(scope.leasingInfo[el.name].value)
                                                        } else {
                                                            files = files.concat(scope.leasingInfo[el.name].value);
                                                        }
                                                        console.log('>> TSD VSD RSD >>')
                                                        console.log(createdRSDFile)
                                                        console.log(uploadRSDandVSDfilesFiles)
                                                        console.log(uploadTSDfileFiles)
                                                        console.log('<< TSD VSD RSD <<')
                                                    }
                                                } catch (e) {
                                                    console.log(e);
                                                }
                                            }
                                        });
                                        scope.leasingInfo.files = files;
                                        scope.leasingInfo.createdRSDFile = createdRSDFile;
                                        scope.leasingInfo.uploadRSDandVSDfilesFiles = uploadRSDandVSDfilesFiles;
                                        scope.leasingInfo.uploadTSDfileFiles = uploadTSDfileFiles;
                                        if (scope.leasingInfo.resolutions && scope.leasingInfo.resolutions.value) {
                                            $q.all(scope.leasingInfo.resolutions.value.map(function (resolution) {
                                                return $http.get("/camunda/api/engine/engine/default/history/task?processInstanceId=" + resolution.processInstanceId + "&taskId=" + resolution.taskId);
                                            })).then(function (tasks) {
                                                tasks.forEach(function (e, index) {
                                                    if (e.data.length > 0) {
                                                        scope.leasingInfo.resolutions.value[index].taskName = e.data[0].name;
                                                        try {
                                                            scope.leasingInfo.resolutions.value[index].taskEndDate = new Date(e.data[0].endTime);
                                                        } catch (e) {
                                                            console.log(e);
                                                        }
                                                    }
                                                });
                                                asynCall2 = true;
                                                if (asynCall1 && asynCall2) {
                                                    openProcessCardModalLeasing(processDefinitionId, businessKey, index);
                                                    asynCall2 = false;
                                                } else console.log('asynCall 1 problem');
                                            });
                                        }
                                        scope.leasingInfo.tasks = processInstanceTasks;
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );

                            },
                            function (error) {
                                console.log(error.data);
                            });
                    }
                };

                function openProcessCardModalLeasing(processDefinitionId, businessKey, index) {
                    console.log(scope.leasingInfo);
                    exModal.open({
                        scope: {
                            leasingInfo: scope.leasingInfo,
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails: scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.leasingCatalogs,
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
                        },
                        templateUrl: './js/partials/leasingCardModal.html',
                        size: 'lg'
                    }).then(function (results) {
                    });
                }

                scope.setCustomField = function () {
                    scope.isCustomField = !scope.isCustomField;
                }
                scope.customFields = [
                    {name: "Region", id: "region", selected: false, order: 1},
                    {name: "Sitename", id: "sitename", selected: false, order: 2},
                    {name: "JR Number", id: "jrNumber", selected: false, order: 3},
                    {name: "Execution Time", id: "executionTime", selected: false, order: 4},
                    {name: "Current Activity", id: "currentActivity", selected: false, order: 5},
                    {name: "Contractor", id: "contractor", selected: false, order: 6},
                    {name: "Reason", id: "reason", selected: false, order: 7},
                    {name: "Requested By", id: "requestedby", selected: false, order: 8},
                    {name: "Current Assignee", id: "currentAssignee", selected: false, order: 9},
                    {name: "Requested Date", id: "requestedDate", selected: false, order: 10},
                    {name: "Validity Date", id: "validityDate", selected: false, order: 11},
                    {name: "Jobs List", id: "jobsList", selected: false, order: 12},
                    {name: "Montly Act #", id: "montlyAct", selected: false, order: 13}
                ];

                scope.selectedCustomFields = [];

                scope.selectCustomField = function () {
                    var tmp = [];
                    angular.forEach(scope.customFields, function (field) {
                        if (field.selected) {
                            field.selected = false;
                            scope.selectedCustomFields.push(field);
                        } else {
                            tmp.push(field);
                        }
                    });
                    scope.customFields = tmp;
                }

                scope.unSelectCustomField = function () {
                    var tmp = [];
                    angular.forEach(scope.selectedCustomFields, function (field) {
                        if (field.selected) {
                            field.selected = false;
                            scope.customFields.push(field);
                        } else {
                            tmp.push(field);
                        }
                    });
                    scope.selectedCustomFields = tmp;
                }

                scope.fillEmptyLines = function (length) {
                    return new Array(13 - length);
                }
            },
            templateUrl: './js/directives/search/networkArchitectureSearch.html'
        };
    }]);
    module.directive('deliveryPortalSearch', ['$http', '$rootScope', '$timeout', '$q', 'exModal', 'SearchCurrentSelectedProcessService',
        function ($http, $rootScope, $timeout, $q, exModal, SearchCurrentSelectedProcessService) {
            return {
                restrict: 'E',
                scope: false,
                link: function (scope, el, attrs) {
                    scope.$watch('tabs.DP', function (switcher) {
                        if (switcher) {
                            $timeout(function () {
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
                        //return scope.ExcellentExport.convert({anchor: 'xlsxClick',format: 'xlsx',filename: 'delivery-portal'}, [{name: 'Process Instances',from: {table: 'xlsxDeliveryPortalTable'}}]);

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
                        changeTransmitNumber: "Смена номера переадресации",
                        changeTariffPlan: "Изменение тарифного плана"
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
                    if (scope.DPProcessesCount === 1) {
                        angular.forEach(scope.DPProcesses, function (value, processKey) {
                            value.value = true;
                        });
                    } else if (scope.DPProcessesCount === 0) {
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
                        if (scope.onlyOneProcessActiveName === '') {
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
                            if ($('input[name="multipleDate"]').data('daterangepicker')) {
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
                        $timeout(function () {
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

                            if (filtered.PBX) scope.pbx = filtered.PBX;
                            else scope.pbx = false;


                        }


                        if (!scope.onlyOneProcessActive) {
                            scope.filterDP.activityId = undefined;
                        } else {
                            if (!(scope.onlyOneProcessActiveName === 'AftersalesPBX')) {
                                scope.filterDP.salesRepr = undefined;
                                scope.filterDP.ip = undefined;
                            } else if (!(scope.onlyOneProcessActiveName === 'revolvingNumbers')) {
                                scope.filterDP.callbackNumber = undefined;
                                scope.filterDP.callerID = undefined;
                            }
                        }

                        angular.forEach(filtered, function (process, key) {
                            scope.selectedProcessInstancesDP.push({name: key, value: key});
                        });
                        scope.filterDP.processDefinitionKey = scope.onlyOneProcessActiveName;

                        if (scope.aftersalesPBXorRevolving || !scope.freephoneOrBulkSms || filtered.PBX) {
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
                                                        console.log(asynCall1, asynCall4, asynCall5, asProcess);
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
                                                                        if (["disconnectProcess", "changeOfficialClientCompanyName", "changeContractNumber", "disconnectOperator", "connectOperator", "changeConnectionType", "changeIpNumber", "changeIdentifier", "changeSmsServiceType", "changeProvider", "changeTransmitNumber", "changeTariffPlan"].indexOf(el.name) > -1) {
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
                                showGroupDetails: scope.showGroupDetails,
                                getStatus: scope.getStatus,
                                showDiagram: scope.showDiagram,
                                showHistory: scope.showHistory,
                                startProcess: scope.startProcess,
                                afterSalesIvrSmsDefinitionId: scope.afterSalesIvrSmsDefinitionId,
                                disableAftersalesStart: scope.disableAftersalesStart,
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
                                downloadTemp: function (file) {
                                    $http({
                                        method: 'GET',
                                        url: '/camunda/uploads/tmp/get/' + file.path,
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
                        }).then(function (results) {
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
    module.directive('demandSearch', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: false,
            link: function (scope, el, attrs) {
                scope.$watch('tabs.DemandUAT', function (switcher) {
                    if (switcher) {
                        $timeout(function () {
                            $("[elastic-textarea]")[0].style.height = 'auto';
                            $("[elastic-textarea]")[0].style.height = ($("[elastic-textarea]")[0].scrollHeight) + 'px';
                        });
                    }
                });
            },
            templateUrl: './js/directives/search/demandSearch.html'
        };
    }]);
    module.directive('dismantleInfo', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                dismantleInfo: '='
            },
            link: function (scope, element, attrs) {
                scope.catalogs = {};
                $http.get($rootScope.getCatalogsHttpByName('dismantleCatalogs')).then(
                    function (result) {
                        angular.extend(scope.catalogs, result.data);
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/dismantleReplace/dismantleInfo.html'
        };
    });
    module.directive('replacementInfo', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {
                replacementInfo: '='
            },
            link: function (scope, element, attrs) {
                scope.catalogs = {};
                console.log(scope)
                $http.get($rootScope.getCatalogsHttpByName('dismantleCatalogs')).then(
                    function (result) {
                        angular.extend(scope.catalogs, result.data);
                    },
                    function (error) {
                        console.log(error.data);
                    }
                );
                scope.download = function (path) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/dismantleReplace/replacementInfo.html'
        };
    });
    module.directive('leasingFiles', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                leasingFiles: '='
            },
            link: function (scope, el, attrs) {
                scope.fileDownload = function (file) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + file.path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };
            },
            templateUrl: './js/directives/leasing/leasingFiles.html'
        };
    }]);
    module.directive('leasingAttachments', ['$http', '$timeout', function ($http, $timeout) {
        return {
            restrict: 'E',
            scope: {
                leasingFiles: '=',
                deletedFiles: '=',
                taskKey: '=',
                modify: '='
            },
            link: function (scope, el, attrs) {
                console.log(scope)
                scope.fileDownload = function (file) {
                    $http({
                        method: 'GET',
                        url: '/camunda/uploads/get/' + file.path,
                        transformResponse: []
                    }).then(function (response) {
                        document.getElementById('fileDownloadIframe').src = response.data;
                    }, function (error) {
                        console.log(error.data);
                    });
                };

                scope.clearFile = function (list, fileIndex) {
                    if (scope.deletedFiles) {
                        scope.deletedFiles.push(scope[list][fileIndex])
                    }
                    scope[list].splice(fileIndex, 1);
                };
            },
            templateUrl: './js/directives/leasing/leasingAttachments.html'
        };
    }]);
});
