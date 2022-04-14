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
    module.directive('vpnTunnel', function ($rootScope, $http) {
        return {
            restrict: 'E',
            scope: {},
            link: function (scope, element, attrs) {
            },
            templateUrl: './js/directives/PBX/vpnTunnel.html'
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
    module.directive('leasingCandidate', ['$http', '$timeout', 'exModal', '$q', '$rootScope', function ($http, $timeout, exModal, $q, $rootScope) {
        return {
            require: '^form',
            restrict: 'E',
            scope: {
                leasingCandidate: '='
            },
            link: function (scope, el, attrs, formCtrl) {
                //console.log(formCtrl, 'formCtrl');
                scope.parentForm = formCtrl;
                scope.dictionary = {};
                scope.antennasList = [];
                scope.frequencyBand = [];
                scope.selectedIndex = -1;
                scope.defaultFarEndCard = false;
                scope.loadCurrentFarEnd = true;
                scope.leasingCandidate.frequenciesByAntennaType = [];
                scope.leasingCandidate.frequenciesFeByAntennaType = [];
                scope.leasingCandidate.farEndInformationDefault = [...scope.leasingCandidate.farEndInformation]
                if (scope.leasingCandidate.cellAntenna) {
                    if (scope.leasingCandidate.cellAntenna.cn_du !== null && typeof scope.leasingCandidate.cellAntenna.cn_du == 'string') {
                        scope.leasingCandidate.cellAntenna.cn_du = [scope.leasingCandidate.cellAntenna.cn_du]
                    }
                    if (!scope.leasingCandidate.cellAntenna.hasOwnProperty('cn_du')) {
                        scope.leasingCandidate.cellAntenna.cn_du = [];
                    }
                }
                for (let s of scope.leasingCandidate.cellAntenna.sectors) {
                    s.antennas.forEach( a => {
                        if (a.antennaType && typeof a.antennaType === 'string'){
                            return a.antennaType = {};
                        }
                    });
                }
                $http.get('/api/leasingCatalogs?version=3').then(
                    function (result) {
                        try{
                        angular.extend(scope.dictionary, result.data);

                        // var antennaTypePromise = $http.get($rootScope.catalogsServerUrl + '/camunda/catalogs/api/get/id/34').then(function(promiseResult){return promiseResult.data;});
                        // var trAntennaTypePromise = $http.get($rootScope.catalogsServerUrl + '/camunda/catalogs/api/get/id/20').then(function(promiseResult){return promiseResult.data;});
                        var newCatalogsPromise = $http.post('/camunda/catalogs/api/get/rolloutcatalogids', [14, 4, 13, 15, 21, 22, 30, 31, 32, 40, 59]).then(function(promiseResult){return promiseResult.data;});
                        var newBscRncsPromise = $http.get('/camunda/asset-management/bcs_rnc').then(function(promiseResult){return promiseResult.data;});
                        var trAntennaTypePromise = $http.get('/camunda/catalogs/api/get/id/20').then(function(promiseResult){return promiseResult.data;});
                        var newFrequenciesPromise = $http.get('/camunda/catalogs/api/get/id/17').then(function(promiseResult){return promiseResult.data;});
                        var antennaModelPromise = $http.get('/camunda/catalogs/api/get/id/19').then(function(promiseResult){return promiseResult.data;});
                         var antennaLocationsPromise = $http.get($rootScope.catalogsServerUrl + '/camunda/catalogs/api/get/id/64').then(function(promiseResult){return promiseResult.data;});
                        // $q.all([antennaTypePromise, trAntennaTypePromise, antennaModelPromise, antennaLocationsPromise, newCatalogsPromise]).then(function(allPromises) {
                        $q.all([newCatalogsPromise, newBscRncsPromise, trAntennaTypePromise, newFrequenciesPromise, antennaModelPromise, antennaLocationsPromise]).then(function(allPromises) {
                            // var antennaTypePromiseResult = allPromises[0];
                            // var trAntennaTypePromiseResult = allPromises[1];
                            // var antennaModelPromiseResult = allPromises[2];
                            var newCatalogsPromiseResult = allPromises[0];
                            var newBscRncsPromiseResult = allPromises[1];
                            var trAntennaTypePromiseResult = allPromises[2];
                            var newFrequenciesPromiseResult = allPromises[3];
                            var antennaModelPromiseResult = allPromises[4];
                            var antennaLocationsPromiseResult = allPromises[5];
                            var newBscRncs = [];
                            var newAntennas = [];
                            // var newAntennaTypes =  [];
                            // var newAntennaType =  [];
                             var newAntennaLocations =  [];
                            var newRbsLocation =  [];
                            var newLegalType =  [];
                            var newAddresses =  [];
                            var newAntennaType =  [];

                            scope.dictionary.constructionType = newCatalogsPromiseResult.construction_type;
                            scope.dictionary.transmissionType = newCatalogsPromiseResult.transmission_type;
                            scope.dictionary.equipmentType = newCatalogsPromiseResult.tr_equipment_type.filter( i => {return (i.hasOwnProperty('id') && i.id !== null)});
                            scope.dictionary.dUnit = newCatalogsPromiseResult.digital_unit_types;
                            scope.dictionary.FarEndConstructiontype = newCatalogsPromiseResult.fe_construction_type;
                            scope.dictionary.radioUnit = newCatalogsPromiseResult.radio_unit_types;
                            // scope.dictionary.legalType = newCatalogsPromiseResult.legal_type;
                            var newFrequencies =  [];

                            newFrequenciesPromiseResult.data.$list.forEach(function(e){
                                var tempItem = {
                                    frequency: e.value + 'GHz',
                                    diameters: []
                                };
                                if(e.TR_Antenna_Type){
                                    e.TR_Antenna_Type.value.forEach(function(v){
                                        trAntennaTypePromiseResult.data.$list.forEach(function(t){
                                            if(t.id === v){
                                                if(t['Antenna Diameter']) {
                                                    tempItem.diameters.push(t['Antenna Diameter'].value.replace(/([a-zA-Z ])/g, ""));
                                                }
                                            }
                                        });
                                    });
                                    newFrequencies.push(tempItem);
                                }
                            });
                            scope.dictionary.frequencies = newFrequencies;

                            newCatalogsPromiseResult.rbs_location.forEach(function(item){
                                newRbsLocation.push({
                                    "title": item.name,
                                    "id": item.id,
                                    "catalogsId": item.catalogsId
                                });
                            });

                            newBscRncsPromiseResult.forEach(function(item){
                                if (item.bsc_rnc_name && !item.bsc_rnc_name.includes('test') && !item.bsc_rnc_name.includes('test')) {
                                    newBscRncs.push({
                                        "name": item.bsc_rnc_name,
                                        "assetsid": item.id,
                                        "id": item.bscid
                                    });
                                }
                            });

                            newCatalogsPromiseResult.legal_type.forEach(function(item){
                                var oldLegalTypes = scope.dictionary.legalType;
                                var findedOldLegalType = oldLegalTypes.find(olp => {return olp.desc === item.name})
                                var newItem = {
                                    "desc": item.name,
                                    "udbid":item.id,
                                    "catalogsId": item.catalogsId
                                }
                                if(findedOldLegalType && findedOldLegalType.id) {
                                    newItem.id = findedOldLegalType.id;
                                }
                                newLegalType.push(newItem);
                            });

                            antennaModelPromiseResult.data.$list.forEach(function(item){
                                var tempItem = {
                                    "antenna": item.value,
                                    "description": ""
                                };
                                if(item.dimensions){
                                    tempItem.dimension = item.dimensions.value;
                                }
                                if(item.weight){
                                    tempItem.weight = item.weight.value;
                                }
                                if(item.udb_id){
                                    tempItem.idbid = item.udb_id.value;
                                }
                                if(item.id){
                                    tempItem.catalog_value_id = item.id.value;
                                }
                                newAntennas.push(tempItem);
                            });
                            // antennaTypePromiseResult.data.$list.forEach(function(item){
                            //     newAntennaTypes.push({
                            //         "name": item.value,
                            //         "id": item.id
                            //     });
                            // });
                            antennaLocationsPromiseResult.data.$list.forEach(function(item){
                                newAntennaLocations.push({
                                    "name": item.value,
                                    "id": item.id
                                });
                            });
                            // trAntennaTypePromiseResult.data.$list.forEach(function(item){
                            //     var tempItem = {
                            //         "name": item.name,
                            //         "frequencies": []
                            //     };
                            //     if(item['Antenna Diameter']) {
                            //         tempItem.diameter = item['Antenna Diameter'].value;
                            //     }
                            //     if(item['Weight']) {
                            //         tempItem.weight = item['Weight'].value.split(' ')[0];
                            //     }
                            //     if(item.udb_id){
                            //         tempItem.idbid = item.udb_id.value;
                            //     }
                            //     newAntennaType.push(tempItem);
                            // });

                            trAntennaTypePromiseResult.data.$list.forEach(function(item){
                                var tempItem = {
                                    "name": item.value,
                                    "frequencies": []
                                };
                                newFrequenciesPromiseResult.data.$list.forEach(function(e){
                                    if(e.TR_Antenna_Type){
                                        e.TR_Antenna_Type.value.forEach(function(v){
                                            if(item.id === v){
                                                tempItem.frequencies.push(e.value+'GHz');
                                            }
                                        });
                                    }
                                });

                                //if ($scope.dictionary.antennaType) {
                                //    const findedOldDictAntennaType = $scope.dictionary.antennaType.find(a => { return a.name === item.value})
                                //    if (findedOldDictAntennaType && findedOldDictAntennaType.hasOwnProperty('frequencies') && findedOldDictAntennaType.frequencies.length > 0) {
                                //        tempItem.frequencies = findedOldDictAntennaType.frequencies;
                                //    }
                                //}
                                if(item['Antenna Diameter']) {
                                    tempItem.diameter = item['Antenna Diameter'].value;
                                }
                                if(item['Weight']) {
                                    tempItem.weight = item['Weight'].value.split(' ')[0];
                                }
                                if(item.udb_id){
                                    tempItem.idbid = item.udb_id.value;
                                }
                                if(item.id){
                                    tempItem.catalog_value_id = item.id.value;
                                }
                                newAntennaType.push(tempItem);
                            });
                            scope.dictionary.antennaType = newAntennaType;

                            if(scope.leasingCandidate.transmissionAntenna && scope.leasingCandidate.transmissionAntenna.antennaType && scope.leasingCandidate.transmissionAntenna.antennaType !== null) {
                                scope.leasingCandidate.frequenciesByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                                    return p.name === scope.leasingCandidate.transmissionAntenna.antennaType;
                                })
                            }

                            var mappedD = _.keyBy(newCatalogsPromiseResult.district, o => o.catalogsId)
                            var mappedO = _.keyBy(newCatalogsPromiseResult.oblast, o => o.catalogsId)
                            newCatalogsPromiseResult.city_village.forEach(function(item){
                                var tempItem = {
                                    "city": item.name,
                                    "catalogsId": item.catalogsId
                                };
                                if(item['parent']) {
                                    // var findedDistrict = newCatalogsPromiseResult.district.find(function(d){ return d.catalogsId === item.parent.toString()});
                                    var findedDistrict = mappedD[item.parent.toString()];
                                    if(findedDistrict !== null ) {
                                        tempItem.district = findedDistrict.name
                                        tempItem.districtCatalogsId = findedDistrict.catalogsId
                                        if (findedDistrict.hasOwnProperty('parent')){
                                            // var findedOblast = newCatalogsPromiseResult.oblast.find(function(o){ return o.catalogsId === findedDistrict.parent.toString()});
                                            var findedOblast = mappedO[findedDistrict.parent.toString()];
                                            if (findedOblast !== null) {
                                                tempItem.oblast = findedOblast.name
                                                tempItem.oblastCatalogsId = findedOblast.catalogsId
                                            }
                                        }
                                    }
                                }
                                newAddresses.push(tempItem);
                            });
                            scope.antennasList = newAntennas;
                            scope.dictionary.antennas = newAntennas;
                            // scope.dictionary.antennaTypes = newAntennaTypes;
                            // scope.dictionary.antennaType = newAntennaType;
                            scope.dictionary.antennaLocation = newAntennaLocations;
                            scope.dictionary.rbsLocation = newRbsLocation;
                            scope.dictionary.legalType = newLegalType;

                            scope.dictionary.legalTypeTitle = _.keyBy(scope.dictionary.legalType, 'id');
                            scope.addressesList = newAddresses;
                            scope.dictionary.addresses = newAddresses;
                            scope.dictionary.BSC = newBscRncs;

                            scope.oblastList = _.uniqBy(scope.dictionary.addresses, 'oblast').map((e, index) => {
                                return {"name": e.oblast, "id": index}
                            });
                            scope.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                                return {"name": e.city, "id": index}
                            });
                            if(scope.currentFarEnd){
                                scope.currentFarEnd.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index} });
                                if(scope.currentFarEnd.feAntennaType && scope.currentFarEnd.feAntennaType !== null) {
                                    scope.leasingCandidate.frequenciesFeByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                                        return p.name === scope.currentFarEnd.feAntennaType;
                                    })
                                }
                            }
                            scope.districtList = _.uniqBy(scope.dictionary.addresses, 'district').map((e, index) => {
                                return {"name": e.district, "id": index}
                            });
                            scope.filteredDistricts = scope.districtList;
                            scope.filteredDistrictsInCAI = scope.districtList;
                            scope.alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');

                            scope.leasingCandidate.addressString = '';
                            scope.leasingCandidate.cellAntenna.addressString = '';
                            //tabs
                            scope.minTab = 0;
                            scope.maxTab = 3;

                            scope.selectedSectorTab = 0;
                            scope.leasingCandidate.cellAntenna.sectors[0].active = 'active';
                            scope.leasingCandidate.checkTPSTouched = false;
                            scope.leasingCandidate.checkTPSBTouched = false;
                            Object.values(scope.leasingCandidate.address).forEach((s, index) => {
                                if(scope.leasingCandidate) {
                                    scope.leasingCandidate.addressString += index > 0 ? ', ' + s : s
                                }
                            });

                            Object.values(scope.leasingCandidate.cellAntenna.address).forEach((s, index) => {
                                if(scope.leasingCandidate && scope.leasingCandidate.cellAntenna) {
                                    scope.leasingCandidate.cellAntenna.addressString += index > 0 ? ', ' + s : s
                                }
                            });
                            scope.frequencyBand = scope.dictionary.frequencyBand;

                                scope.catalogsFetched = true;
                                scope.openFarEndInformation(0);
                            });
                        }
                    catch(e){
                        console.log(e)
                    }
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

                scope.isError = function(block) {
                    try {
                        let error = false;
                        if(scope.kcell_form){
                            var keys = Object.keys(scope.kcell_form);
                            var fields = [];
                            if(block === 'candidateInformation') {
                                fields = ['siteName', 'latitude', 'longitude', 'constructionType', 'transmissionType', 'square', 'rbsLocation', 'radio', 'createNewCandidateSiteTaskComment', 'leasingCandidate', 'cn_addr_oblast', 'cn_addr_district', 'cn_addr_city', 'cn_addr_street', 'cn_addr_building', 'cn_addr_cadastral_number', 'cn_addr_note', 'cai_addr_oblast', 'cai_addr_district', 'cai_addr_street', 'cai_addr_building', 'cai_addr_cadastral_number', 'cai_addr_note', 'antennaName', 'quantity_', 'dimensions_', 'frequencyBand_', 'weight_', 'azimuth_', 'suspensionHeight_', 'check_', 'antennaLocation', 'cn_gsm900', 'cn_lte800', 'cn_dcs1800', 'cn_ret_lte800', 'cn_wcdma2100', 'cn_lte1800', 'cn_umts900', 'cn_ret_lte1800', 'cn_trx', 'cn_lte2100', 'cn_wcdma_carrier', 'cn_ret_lte2100', 'cn_gsm_antenna_quantity', 'cn_lte_antenna_quantity', 'cn_tilt_mech_gsm', 'cn_tilt_mech_lte', 'cn_tilt_electr_gsm', 'cn_tilt_electr_lte', 'cn_direction_gsm', 'cn_direction_lte', 'cn_height_gsm', 'cn_height_lte', 'cn_duplex_gsm', 'cn_diversity', 'cn_power_splitter', 'cn_hcu', 'cn_asc', 'cn_ret', 'cn_tma_gsm', 'cn_tcc', 'cn_gsm_range', 'selectedAntennas', 'antennasQuantity', 'address', 'legalType', 'RClegalName', 'legalAddress', 'telFax', 'firstLeaderName', 'firstLeaderPos', 'email', 'contactName', 'position', 'contractInfo', 'antennaType', 'antennaQuantity', 'frequencyBand', 'TRSuspensionHeight', 'TRazimuth', 'provideUs3Phase', 'cableLength', 'cableLayingType', 'agreeToReceiveMonthlyPayment', 'closestPublic04', 'closestPublic10', 'cn_tps', 'cn_tps_belongs', 'cn_tps_belongs_commentary', 'cn_tps_distance'];
                            } else if(block === 'address') {
                                fields = ['cn_addr_oblast' ,'cn_addr_district' ,'cn_addr_city' ,'cn_addr_street' ,'cn_addr_building' ,'cn_addr_cadastral_number' ,'cn_addr_note'];
                            } else if(block === 'cellAntennaInformation'){
                                fields = ['antennaLocation', 'antennaNameInSector_', 'azimuth_', 'cai_addr_building', 'cai_addr_cadastral_number', 'cai_addr_district', 'cai_addr_note', 'cai_addr_oblast', 'cai_addr_street', 'check_', 'cai_addr_city', 'cn_asc', 'cn_dcs1800_', 'cn_direction_gsm_', 'cn_direction_lte_', 'cn_diversity_', 'cn_duplex_gsm_', 'cn_gsm900_', 'cn_gsm_antenna_quantity_', 'cn_gsm_range_', 'cn_hcu_', 'cn_height_gsm_', 'cn_height_lte_', 'cn_lte1800_', 'cn_lte2100_', 'cn_lte800_', 'cn_lte_antenna_quantity_', 'cn_power_splitter_', 'cn_ret_', 'cn_ret_lte1800_', 'cn_ret_lte2100_', 'cn_ret_lte800_', 'cn_tcc_', 'cn_tilt_electr_gsm_', 'cn_tilt_electr_lte_', 'cn_tilt_mech_gsm_', 'cn_tilt_mech_lte_', 'cn_tma_gsm_', 'cn_trx_', 'cn_umts900_', 'cn_wcdma2100_', 'cn_wcdma_carrier_', 'dimensions_', 'dUnit', 'frequencyBand_', 'quantity_', 'radioUnit_', 'suspensionHeight_', 'weight_'];
                            } else if(block === 'renterCompany') {
                                fields = ['legalType', 'RClegalName', 'legalAddress', 'telFax', 'firstLeaderName', 'firstLeaderPos', 'email', 'contactName', 'position', 'contractInfo'];
                            } else if(block === 'nearEndInformation') {
                                fields = ['antennaType', 'antennaQuantity', 'ne_longitude', 'ne_latitude', 'frequencyBand', 'TRSuspensionHeight', 'TRazimuth', 'nei_addr_oblast', 'nei_addr_district', 'nei_addr_city', 'nei_addr_street', 'nei_addr_building', 'nei_addr_cadastral_number', 'nei_addr_note'];
                            } else if(block === 'powerSource') {
                                fields = ['agreeToReceiveMonthlyPayment', 'cableLength', 'closestPublic04', 'closestPublic10', 'cn_tps', 'cn_tps_belongs', 'cn_tps_belongs_commentary', 'cn_tps_distance', 'provideUs3Phase'];
                            } else if(block === 'farEndInformation') {
                                fields = ['farEndName', 'threeFarEndNotNecessaryCheck', 'threeFarEndNotNecessaryCheckComment', 'fe_form_contractInformation', 'fe_form_equipmentType', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_cn_addr_note', 'fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_n_addr_city', 'fe_form_farEndAddress', 'fe_form_farEndLegalType', 'fe_form_FeAntennasQuantity', 'fe_form_feAntennaType', 'fe_form_FeAzimuth', 'fe_form_FeComments', 'fe_form_FeConstructionType', 'fe_form_FeDiameter', 'fe_form_FeFrequencyBand', 'fe_form_FERC_ContractInformation', 'fe_form_FERC_Name', 'fe_form_FERC_Position', 'fe_form_FERCemail', 'fe_form_FERCFirstLeaderName', 'fe_form_FERCFirstLeaderPosition', 'fe_form_FERCLegalAddress', 'fe_form_FERClegalName', 'fe_form_FERCTelFax', 'fe_form_FeSquare', 'fe_form_FeSuspensionHeight', 'fe_form_FeWeight', 'fe_form_ResultsOfVisit', 'fe_form_surveyDate'];
                            } else if(block === 'farEndInformationAccordion') {
                                fields = ['fe_form_contractInformation', 'fe_form_equipmentType', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_cn_addr_note', 'fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_n_addr_city', 'fe_form_farEndAddress', 'fe_form_farEndLegalType', 'fe_form_FeAntennasQuantity', 'fe_form_feAntennaType', 'fe_form_FeAzimuth', 'fe_form_FeComments', 'fe_form_FeConstructionType', 'fe_form_FeDiameter', 'fe_form_FeFrequencyBand', 'fe_form_FERC_ContractInformation', 'fe_form_FERC_Name', 'fe_form_FERC_Position', 'fe_form_FERCemail', 'fe_form_FERCFirstLeaderName', 'fe_form_FERCFirstLeaderPosition', 'fe_form_FERCLegalAddress', 'fe_form_FERClegalName', 'fe_form_FERCTelFax', 'fe_form_FeSquare', 'fe_form_FeSuspensionHeight', 'fe_form_FeWeight', 'fe_form_ResultsOfVisit', 'fe_form_surveyDate'];
                            } else if(block === 'farEndAddress') {
                                fields = ['fe_form_FarEnd_cn_addr_oblast', 'fe_form_FarEnd_cn_addr_district', 'fe_form_FarEnd_n_addr_city', 'fe_form_FarEnd_cn_addr_street', 'fe_form_FarEnd_cn_addr_building', 'fe_form_FarEnd_cn_addr_cadastral_number', 'fe_form_FarEnd_cn_addr_note'];
                            } else if(block === 'nearEndAddress') {
                                fields = ['nei_addr_oblast', 'ne_longitude', 'ne_latitude', 'nei_addr_building', 'nei_addr_note', 'nei_addr_street'];
                            } else if(block === 'cellAntennaAddress') {
                                fields = ['cai_addr_oblast', 'cai_addr_district', 'cai_addr_building', 'cai_addr_note', 'cai_addr_street', 'cai_addr_street'];
                            }
                            keys.forEach(function (key) {
                                if (!key.startsWith('$')) {
                                    fields.forEach(function (field) {
                                        if(block === 'nearEndInformation'){
                                            if (key === field) {
                                                if (scope.kcell_form[key].$error && scope.kcell_form[key].$error !== {}) {
                                                    error = error || (scope.kcell_form[key].$error.required && (scope.kcell_form[key].$touched || (scope.kcell_form['$$parentForm'] && scope.kcell_form.$$parentForm['$submitted'])));
                                                }
                                            }
                                        } else {
                                            if (key.startsWith(field)) {
                                                if (scope.kcell_form[key].$error && scope.kcell_form[key].$error !== {}) {
                                                    error = error || (scope.kcell_form[key].$error.required && (scope.kcell_form[key].$touched || (scope.kcell_form['$$parentForm'] && scope.kcell_form.$$parentForm['$submitted'])));

                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                        return error;
                    } catch (e) {
                        console.log(e);
                    }
                    return false;
                }

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

                            function mod(n, m) {
                                return ((n % m) + m) % m;
                            }

                            const neAzimuth = Math.round(180*mod(angleDiff,(2*pi))/pi*100)/100
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
                    if(scope.currentFarEnd.feAntennaType && scope.currentFarEnd.feAntennaType !== null) {
                        scope.leasingCandidate.frequenciesFeByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                            return p.name === scope.currentFarEnd.feAntennaType;
                        })
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
                scope.$watch('leasingCandidate.checkAndApproveFETaskResult', function (resolution, oldValue) {
                    if (resolution === 'approved' || resolution === 'rejected') {
                        scope.sortableOptions.disabled = true;
                    } else if (resolution == 'priorityChange') {
                        scope.sortableOptions.disabled = false;
                    }
                    if (oldValue === 'priorityChange') {
                        exModal.open({
                            templateUrl: './js/partials/confirmModal.html',
                            size: 'sm'
                        }).then(function() {
                            scope.leasingCandidate.farEndInformation = [...scope.leasingCandidate.farEndInformationDefault]
                        }).catch(function(){
                            scope.leasingCandidate.checkAndApproveFETaskResult = 'priorityChange'
                        });
                    }
                });
                scope.$watch('leasingCandidate.transmissionAntenna.antennaType', function (antennaType) {
                    scope.leasingCandidate.frequenciesByAntennaType = [];
                    scope.leasingCandidate.frequenciesByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                        return p.name === antennaType;
                    });
                }, true);
                scope.$watch('currentFarEnd.feAntennaType', function (antennaType) {
                    scope.leasingCandidate.frequenciesFeByAntennaType = [];
                    scope.leasingCandidate.frequenciesFeByAntennaType = _.find(scope.dictionary.antennaType, function (p) {
                        return p.name === antennaType;
                    })
                }, true);
                scope.$watch('dictionary.antennaType', function (antennaType) {
                    scope.leasingCandidate.frequenciesByAntennaType = [];
                    scope.leasingCandidate.frequenciesByAntennaType = _.find(antennaType, function (p) {
                        return p.name === scope.leasingCandidate.transmissionAntenna.antennaType;
                    });

                    scope.leasingCandidate.frequenciesFeByAntennaType = [];
                    scope.leasingCandidate.frequenciesFeByAntennaType = _.find(antennaType, function (p) {
                        return p.name === scope.currentFarEnd.feAntennaType;
                    });
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
                                    scope.leasingCandidate.transmissionAntenna.udb_id = type.udb_id;
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
                    if(scope.currentFarEnd){
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
                    }
                };

                scope.siteAddressCitySelected = function ($item) {
                    scope.leasingCandidate.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    if (!scope.leasingCandidate.address.cn_addr_oblast || scope.leasingCandidate.address.cn_addr_oblast !== _.find(scope.dictionary.addresses, {'city': $item.name}).oblast) {
                        scope.leasingCandidate.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
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
                        scope.filteredDistricts = _.uniqBy(scope.leasingCandidate.filteredByOblast, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.cityList = _.uniqBy(scope.leasingCandidate.filteredByOblast, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    } else {
                        scope.filteredDistricts = scope.districtList;
                        scope.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    }
                };

                scope.filterDistrictAfterSelectOblInCAI = function (cn_addr_oblast) {
                    scope.leasingCandidate.cellAntenna.address.cn_addr_oblast = cn_addr_oblast
                    scope.leasingCandidate.cellAntenna.address.cn_addr_district = ''
                    scope.leasingCandidate.cellAntenna.address.cn_addr_city = ''
                    if(scope.leasingCandidate.cellAntenna.address.cn_addr_oblast && scope.leasingCandidate.cellAntenna.address.cn_addr_oblast !== ''){
                        scope.leasingCandidate.filteredByOblast = scope.dictionary.addresses.filter(a => {return a.oblast === scope.leasingCandidate.cellAntenna.address.cn_addr_oblast});
                        scope.leasingCandidate.filteredDistrictsInCAI = _.uniqBy(scope.filteredByOblast, 'district').map( (e, index) => { return {"name" : e.district, "id" : index} });
                        scope.leasingCandidate.cityList = _.uniqBy(scope.filteredByOblast, 'city').map( (e, index) => { return {"name" : e.city, "id" : index, "catalogsId" : e.catalogsId} });
                    } else {
                        scope.leasingCandidate.filteredDistrictsInCAI = scope.districtList;
                        scope.leasingCandidate.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index, "catalogsId" : e.catalogsId} });
                    }
                }

                scope.addressCitySelectedCN = function ($item) {
                    scope.leasingCandidate.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    scope.leasingCandidate.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
                };

                scope.getCityCN = function (val) {
                    scope.leasingCandidate.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map( (e, index) => { return {"name" : e.city, "id" : index, "catalogsId" : e.catalogsId} });
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.leasingCandidate.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                };

                scope.cellInfoAddressCitySelected = function ($item) {
                    if ($item.catalogsId) {
                        scope.leasingCandidate.cellAntenna.address.city_catalogs_id = $item.catalogsId;
                    }
                    let findByCity = {};
                    if (scope.leasingCandidate.cellAntenna.address.cn_addr_oblast && scope.leasingCandidate.cellAntenna.address.cn_addr_oblast !== null && scope.leasingCandidate.cellAntenna.address.cn_addr_oblast !== '') {
                        findByCity = _.find(scope.dictionary.addresses, {'city': $item.name, 'oblast' : scope.leasingCandidate.cellAntenna.address.cn_addr_oblast})
                    } else {
                        findByCity = _.find(scope.dictionary.addresses, {'city': $item.name})
                    }
                    scope.leasingCandidate.cellAntenna.address.cn_addr_district = findByCity ? findByCity.district : '';
                    if (!scope.leasingCandidate.cellAntenna.address.cn_addr_oblast || scope.leasingCandidate.cellAntenna.address.cn_addr_oblast !== findByCity.oblast ) {
                        scope.leasingCandidate.cellAntenna.address.cn_addr_oblast = findByCity ? findByCity.oblast : '';
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

                scope.filterDistrictAfterSelectOblNE = function (cn_addr_oblast) {
                    if(scope.leasingCandidate.transmissionAntenna){
                        scope.leasingCandidate.transmissionAntenna.address = {};
                    }
                    scope.leasingCandidate.transmissionAntenna.address.cn_addr_oblast = cn_addr_oblast
                    scope.leasingCandidate.transmissionAntenna.address.cn_addr_district = ''
                    scope.leasingCandidate.transmissionAntenna.address.cn_addr_city = ''
                    if (scope.leasingCandidate.transmissionAntenna.address.cn_addr_oblast && scope.leasingCandidate.transmissionAntenna.address.cn_addr_oblast !== '') {
                        scope.leasingCandidate.transmissionAntenna.filteredByOblast = scope.dictionary.addresses.filter(a => {
                            return a.oblast === scope.leasingCandidate.transmissionAntenna.address.cn_addr_oblast
                        });
                        scope.leasingCandidate.transmissionAntenna.filteredDistricts = _.uniqBy(scope.leasingCandidate.transmissionAntenna.filteredByOblast, 'district').map((e, index) => {
                            return {"name": e.district, "id": index}
                        });
                        scope.leasingCandidate.transmissionAntenna.cityList = _.uniqBy(scope.leasingCandidate.transmissionAntenna.filteredByOblast, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    } else {
                        scope.leasingCandidate.transmissionAntenna.filteredDistricts = scope.districtList;
                        scope.leasingCandidate.transmissionAntenna.cityList = _.uniqBy(scope.dictionary.addresses, 'city').map((e, index) => {
                            return {"name": e.city, "id": index}
                        });
                    }
                };

                scope.addressCitySelectedNE = function ($item) {
                    scope.leasingCandidate.transmissionAntenna.address.cn_addr_district = _.find(scope.dictionary.addresses, {'city': $item.name}).district;
                    scope.leasingCandidate.transmissionAntenna.address.cn_addr_oblast = _.find(scope.dictionary.addresses, {'city': $item.name}).oblast;
                };

                scope.getCityNE = function (val) {
                    if (val.length < 2) {
                        return []
                    }
                    return _.filter(scope.leasingCandidate.transmissionAntenna.cityList, function (o) {
                        return o.name.toLowerCase().includes(val.toLowerCase());
                    })
                }




                //NEW DIRECTIVE ITEMS
                //
                //
                // ;

                // scope.leasingCandidate.cellAntenna.sectors = [];
                // console.log('scope.kcell_form');
                // console.log(scope);
                scope.sector = {
                    "antennas":[{}],
                }

                scope.selectedAntennaSector = {};
                scope.selectedSectorTab = 0;

                //tabs
                scope.minTab = 0;
                scope.maxTab = 3;

                // scope.newSector = function () {
                //     let newS = JSON.parse(angular.toJson(scope.sector));
                //     scope.leasingCandidate.cellAntenna.sectors.push(newS);
                //     console.log(scope.leasingCandidate.cellAntenna.sectors)
                //     if (scope.selectedSectorTab+1 < scope.leasingCandidate.cellAntenna.sectors.length) {
                //         scope.selectedSectorTab = scope.leasingCandidate.cellAntenna.sectors.length - 1;
                //         scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';

                //         if (scope.selectedSectorTab === scope.maxTab) {
                //             scope.minTab= scope.minTab + 1;
                //             scope.maxTab= scope.maxTab + 1;
                //         }
                //     } else {
                //         scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
                //     }
                // }

                // if(!(scope.leasingCandidate.cellAntenna && scope.leasingCandidate.cellAntenna.sectors && scope.leasingCandidate.cellAntenna.sectors.length > 0)){
                //     scope.newSector();
                //     scope.newSector();
                //     scope.newSector();
                // }

                scope.selectAntennaSector = function (index) {
                    //if(scope.renterCompany && (scope.renterCompany.legalType === 'national_kazakhtelecom' || scope.renterCompany.legalType === 'national_kazteleradio' || scope.renterCompany.legalType === 'national_kazpost')){
                    //    scope.siteObjectType = 'national';
                    //} else {
                    //    scope.siteObjectType = 'other';
                    //}
                    scope.selectedSectorTab = index;
                    //scope.selectedAntennaSector = scope.cellAntenna.sectors[index];
                }

                scope.defineSiteObjectType = function () {
                    if(scope.leasingCandidate.renterCompany && (scope.leasingCandidate.renterCompany.legalType === 'national_kazakhtelecom' || scope.leasingCandidate.renterCompany.legalType === 'national_kazteleradio' || scope.leasingCandidate.renterCompany.legalType === 'national_kazpost')){
                        scope.siteObjectType = 'national';
                    } else {
                        scope.siteObjectType = 'other';
                    }
                    scope.legalType = scope.leasingCandidate.renterCompany?.legalType;
                }

                scope.antennaNameSelected = function (sector, antenna, a) {
                    const obj = scope.antennasList.find(b=>{ return b.antenna === a});
                    if(obj){
                        scope.leasingCandidate.cellAntenna.sectors[sector].antennas[antenna].dimension = obj.dimension;
                        scope.leasingCandidate.cellAntenna.sectors[sector].antennas[antenna].weight = obj.weight;
                        scope.leasingCandidate.cellAntenna.sectors[sector].antennas[antenna].udb_id = obj.idbid;
                        scope.leasingCandidate.cellAntenna.sectors[sector].antennas[antenna].catalog_value_id = obj.catalog_value_id;
                    }
                }

                scope.antennaLocationSelected = function (sector, antenna, a) {
                    const obj = scope.dictionary.antennaLocation.find(b=>{ return b.name === a});
                    if(obj){
                        scope.leasingCandidate.cellAntenna.sectors[sector].antennas[antenna].cn_antenna_loc_catalog_value_id = obj.id;
                    }
                }

                scope.filteredAntennaTypesBySiteType = [];
                if (scope.leasingCandidate.siteType) {
                    if (scope.leasingCandidate.siteType.id === "2" || scope.leasingCandidate.siteType.id === "1" || scope.leasingCandidate.siteType.id === "4"){
                        scope.filteredAntennaTypesBySiteType.push('G900')
                        scope.filteredAntennaTypesBySiteType.push('G1800')
                        scope.filteredAntennaTypesBySiteType.push('U900')
                        scope.filteredAntennaTypesBySiteType.push('U2100')
                        scope.filteredAntennaTypesBySiteType.push('L800')
                        scope.filteredAntennaTypesBySiteType.push('L1800')
                        scope.filteredAntennaTypesBySiteType.push('L2100')
                        scope.filteredAntennaTypesBySiteType.push('L2600')
                    } else {
                        if (scope.leasingCandidate.siteType.name.includes('2')){
                            scope.filteredAntennaTypesBySiteType.push('G900')
                            scope.filteredAntennaTypesBySiteType.push('G1800')
                        }
                        if (scope.leasingCandidate.siteType.name.includes('3')){
                            scope.filteredAntennaTypesBySiteType.push('U900')
                            scope.filteredAntennaTypesBySiteType.push('U2100')
                        }
                        if (scope.leasingCandidate.siteType.name.includes('4')){
                            scope.filteredAntennaTypesBySiteType.push('L800')
                            scope.filteredAntennaTypesBySiteType.push('L1800')
                            scope.filteredAntennaTypesBySiteType.push('L2100')
                            scope.filteredAntennaTypesBySiteType.push('L2600')
                        }
                    }
                }

                scope.checkAntennaBands = function (bandName, sectorIndex) {
                    if(scope.leasingCandidate.cellAntenna.sectors) {
                        const s = scope.leasingCandidate.cellAntenna.sectors[sectorIndex]

                        const finded = s.antennas.find( a => {
                            if (a.antennaType && a.antennaType[bandName]){
                                return a.antennaType[bandName]
                            }
                        });
                        if (finded) {
                            return false
                        }
                    }
                    return true
                }

                scope.changedAntennaQuantity = function (antennaIndex, sectorIndex) {
                    const a = scope.leasingCandidate.cellAntenna.sectors[sectorIndex].antennas[antennaIndex];
                    if (a.antennaType) {
                        Object.keys(a.antennaType).forEach( b => {
                            scope.countAntennaQuantity(b, sectorIndex)
                        })
                    }

                }

                scope.countAntennaQuantity = function (bandName, sectorIndex) {
                    // ant.antennaType[a]
                    const s = scope.leasingCandidate.cellAntenna.sectors[sectorIndex];
                    if ( !s.bands) {
                        s.bands = {};
                    }
                    if (!s.bands[bandName]) {
                        s.bands[bandName] = {};
                    }
                    const filtered = s.antennas.filter( a => { return a.antennaType[bandName]});
                    if (filtered.length > 0 && s) {
                        if ( !s.bands) {
                            s.bands = {};
                        }
                        if (!s.bands[bandName]) {
                            s.bands[bandName] = {};
                        }
                        s.bands[bandName].cn_gsm_antenna_quantity = 0 ;
                        filtered.forEach( a => {
                            console.log(a)
                            s.bands[bandName].cn_gsm_antenna_quantity += a.quantity ? parseInt(a.quantity) : 0;
                        })
                        // s.bands[bandName].cn_gsm_antenna_quantity = filtered.length;
                        s.bands[bandName].active = true;
                    } else {
                        if ( !s.bands) {
                            s.bands = {};
                        }
                        s.bands[bandName] = {};
                        s.bands[bandName].active = false;
                    }
                }

                scope.tabStepRight = function () {
                    if (scope.selectedSectorTab+1 < scope.leasingCandidate.cellAntenna.sectors.length) {
                        scope.selectedSectorTab = scope.selectedSectorTab + 1;
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';

                        if (scope.selectedSectorTab === scope.maxTab) {
                            scope.minTab= scope.minTab + 1;
                            scope.maxTab= scope.maxTab + 1;
                        }
                    } else {
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
                    }
                }

                scope.checkFrequency = function(val, index) {
                    var yes = false;
                    var temp = String(val);
                    angular.forEach(scope.frequencyBand, function (freq, i) {
                        if (freq.substring(0, temp.length) === temp) {
                            yes = true;
                            scope.frequencyBandValid = true;
                        }
                    });
                    if (!yes) {
                        scope.leasingCandidate.cellAntenna.selectedAntennas[index].frequencyBand = '';
                        scope.frequencyBandValid = false;
                    }
                };
                scope.cellAntennaTypeChanged = function(val, parent, index) {
                    const pIndex = parent.$index
                    if(scope.leasingCandidate.cellAntenna) {
                        if(scope.leasingCandidate.cellAntenna.sectors && scope.leasingCandidate.cellAntenna.sectors.length >0){
                            if(scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas && scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas.length >0) {
                                scope.leasingCandidate.cellAntenna.sectors[pIndex].antennas[index].frequencyBand = parseInt(val.replace(/[^0-9.]/gi, ''));
                            }
                        }
                    }
                }
                scope.checkAzimuth = function(val, index) {
                    if (!(val>=0 && val <=360)) scope.leasingCandidate.cellAntenna.selectedAntennas[index].azimuth = '';
                };

                scope.azimuthPattern = '([0-9]|[1-8][0-9]|9[0-9]|[12][0-9]{2}|3[0-5][0-9]|360)';

                scope.fromFirstToSecondWindow = function (indexes) {
                    if (indexes.length > 0) {
                        indexes.forEach(function (index) {
                            scope.leasingCandidate.cellAntenna.selectedAntennas.push(scope.antennasList[index]);
                            scope.antennasList.splice(index, 1);
                        });
                    }
                    scope.firstMultiselect = [];
                };

                scope.fromSecondToFirstWindow = function (indexes) {
                    if (indexes.length > 0) {
                        indexes.forEach(function (index) {
                            scope.antennasList.push(scope.antennasList[index]);
                            scope.leasingCandidate.cellAntenna.selectedAntennas.splice(index, 1);
                        });
                    }
                    scope.secondMultiselect = [];
                };

                scope.tabStepLeft = function () {
                    if (scope.selectedSectorTab-1 >= 0) {
                        scope.selectedSectorTab = scope.selectedSectorTab - 1;
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';

                        if (scope.selectedSectorTab+1 === scope.minTab) {
                            scope.minTab= scope.minTab - 1;
                            scope.maxTab= scope.maxTab - 1;
                        }
                    } else {
                        scope.leasingCandidate.cellAntenna.sectors[scope.selectedSectorTab].active = 'active';
                    }
                }
                ;
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
                // $http.get($rootScope.getCatalogsHttpByName('leasingCatalogs')).then(
                //     function (result) {
                //         angular.extend(scope.leasingCatalogs, result.data);
                //         scope.leasingCatalogs.rbsTypes = [];
                //         angular.forEach(scope.leasingCatalogs.rbsType, function (rbs) {
                //             if (scope.leasingCatalogs.rbsTypes.indexOf(rbs.rbsType) < 0) {
                //                 scope.leasingCatalogs.rbsTypes.push(rbs.rbsType);
                //             }
                //         });
                //         scope.leasingCatalogs.legalTypeTitle = _.keyBy(scope.leasingCatalogs.legalType, 'id');
                //         scope.leasingCatalogs.initiatorTitle = _.keyBy(scope.leasingCatalogs.initiators, 'id');
                //         scope.leasingCatalogs.allUnionProjects = [];
                //         try {
                //             scope.leasingCatalogs.projects.forEach(pbi => {
                //                 scope.leasingCatalogs.allUnionProjects = _.uniqBy(scope.leasingCatalogs.allUnionProjects.concat(pbi.project.filter(p => {return (p.id !== null && p.id !== 'null')})), pr => {return pr.id + pr.name });
                //             });
                //             // console.log(scope.leasingCatalogs.allUnionProjects)
                //         } catch (error) {
                //             console.log(error)
                //         }
                //     },
                //     function (error) {
                //         console.log(error.data);
                //     }
                // );
                $http.post('/camunda/catalogs/api/get/rolloutcatalogids', [2, 4, 5, 6, 7, 8, 9, 11, 12, 16, 22, 23, 60]).then(
                    function(res){
                        let newR = {};
                        for (let i = 0; i < res.data.regions.length ; i++ ) {
                            let r = res.data.regions[i];
                            switch(r.name) {
                                case 'Astana':
                                    newR['astana'] = r.name
                                    break;
                                case 'Almaty':
                                    newR['alm'] = r.name
                                    break;
                                case 'East':
                                    newR['east'] = r.name
                                    break;
                                case 'North & Central':
                                    newR['nc'] = r.name
                                    break;
                                case 'South':
                                    newR['south'] = r.name
                                    break;
                                case 'West':
                                    newR['west'] = r.name
                                    break;
                                default:
                                    console.log('not found region id')
                                    break;
                            }
                        }
                        scope.regionsMap = newR
                        scope.leasingCatalogs.initiators = res.data.ncp_initiator
                        scope.leasingCatalogs.allUnionProjects = res.data.ncp_projects.map(i => {
                            return {
                                id: i.catalogsId,
                                name: i.name,
                                initiator: i.initiator
                            }
                        })
                        scope.leasingCatalogs.reasons = res.data.ncp_reason.map(i => {
                            return {
                                id: i.id,
                                reason: i.name,
                                catalogsId: i.catalogsId
                            }
                        })
                        scope.leasingCatalogs.siteType = res.data.site_type.filter(i => i.id)
                        scope.leasingCatalogs.rbsCabinetTypes = res.data.rbs_cabinet_type.map(i => {
                            return {
                                ...i,
                                id: i.catalogsId
                            }
                        })
                        scope.leasingCatalogs.bands = res.data.ncp_bands.map(i => {
                            return {
                                ...i,
                                title: i.name
                            }
                        })
                        scope.leasingCatalogs.legalType = res.data.legal_type.map(i => {
                            return {
                                ...i,
                                codeName: scope.getLegalTypeCodeName(i.id)
                            }
                        })
                        scope.leasingCatalogs.contract = res.data.contract_type
                        scope.leasingCatalogs.contractExecuter = res.data.contract_executors
                        scope.leasingCatalogs.transmissionType = res.data.transmission_type
                        scope.leasingCatalogs.branchesKT = res.data.branches_kt.map(i => {
                            return {
                                ...i,
                                id: scope.getKTBranchId(i.name)
                            }
                        })
                        scope.leasingCatalogs.rbsTypes = res.data.rbs_model
                    },
                    function(error){
                        console.log(error);
                    }
                );

                scope.getLegalTypeCodeName = function (id) {
                    let legalType = [
                        {"id":"individual", "desc":"Физическое лицо", "udbid":"321"},
                        {"id":"too", "desc":"ТОО", "udbid":"122"},
                        {"id":"ao", "desc":"АО", "udbid":"161"},
                        {"id":"national_kazakhtelecom", "desc":"АО  \"Казахтелеком\"", "udbid":"211"},
                        {"id":"national_kazpost", "desc":"АО \"Казпочта\"", "udbid":"215"},
                        {"id":"national_kazteleradio", "desc":"АО \"Казтелерадио\"", "udbid":"214"},
                        {"id":"gu", "desc":"ГУ", "udbid":"381"},
                        {"id":"ip", "desc":"ИП", "udbid":"121"},
                        {"id":"ksk", "desc":"КСК", "udbid":"51"},
                        {"id":"nao", "desc":"НАО", "udbid":"382"},
                        {"id":"ooo", "desc":"ООО", "udbid":"383"},
                        {"id":"own_kcell", "desc":"Собственность Kcell", "udbid":"227"},
                        {"id":"tercom", "desc":"Терком", "udbid":"221"},
                        {"id":"esso", "desc":"ЭСО", "udbid":"216"},
                        {"id":"beeline", "desc":"Beeline", "udbid":"302"},
                        {"id":"tele2", "desc":"Tele2", "udbid":"341"},
                        {"id":"beeline", "desc":"Other", "udbid":"384"},
                        {"id":"kaz_trans_com", "desc":"АО \"KazTransCom\"", "udbid":"361"},
                        {"id":"lc_commerce", "desc":"ТОО \"LC Commerce\"", "udbid":"362"},
                        {"id":"vostoktelecom", "desc":"ТОО \"Востоктелеком\"", "udbid":"363"}
                    ]
                    return legalType.find(i => i.udbid === id).id
                }

                scope.getLegalTypeDesc = function (id) {
                    let legalType = [
                        {"id":"individual", "desc":"Физическое лицо", "udbid":"321"},
                        {"id":"too", "desc":"ТОО", "udbid":"122"},
                        {"id":"ao", "desc":"АО", "udbid":"161"},
                        {"id":"national_kazakhtelecom", "desc":"АО  \"Казахтелеком\"", "udbid":"211"},
                        {"id":"national_kazpost", "desc":"АО \"Казпочта\"", "udbid":"215"},
                        {"id":"national_kazteleradio", "desc":"АО \"Казтелерадио\"", "udbid":"214"},
                        {"id":"gu", "desc":"ГУ", "udbid":"381"},
                        {"id":"ip", "desc":"ИП", "udbid":"121"},
                        {"id":"ksk", "desc":"КСК", "udbid":"51"},
                        {"id":"nao", "desc":"НАО", "udbid":"382"},
                        {"id":"ooo", "desc":"ООО", "udbid":"383"},
                        {"id":"own_kcell", "desc":"Собственность Kcell", "udbid":"227"},
                        {"id":"tercom", "desc":"Терком", "udbid":"221"},
                        {"id":"esso", "desc":"ЭСО", "udbid":"216"},
                        {"id":"beeline", "desc":"Beeline", "udbid":"302"},
                        {"id":"tele2", "desc":"Tele2", "udbid":"341"},
                        {"id":"beeline", "desc":"Other", "udbid":"384"},
                        {"id":"kaz_trans_com", "desc":"АО \"KazTransCom\"", "udbid":"361"},
                        {"id":"lc_commerce", "desc":"ТОО \"LC Commerce\"", "udbid":"362"},
                        {"id":"vostoktelecom", "desc":"ТОО \"Востоктелеком\"", "udbid":"363"}
                    ]
                    return legalType.find(i => i.id === id).desc
                }

                scope.getKTBranchId = function (name) {
                    let branches = [
                        {"id":"ods", "name":"Объединение Дальняя Связь"},
                        {"id":"almaty", "name":"РДТ Алматытелеком"},
                        {"id":"north", "name":"Северная Региональная Дирекция Телекоммуникаций"},
                        {"id":"center", "name":"Центральная Региональная Дирекция Телекоммуникаций"},
                        {"id":"west", "name":"Западная Региональная Дирекция Телекоммуникаций"},
                        {"id":"east", "name":"Восточная Региональная Дирекция Телекоммуникаций"},
                        {"id":"south", "name":"Южная Региональная Дирекция Телекоммуникаций"},
                        {"id":"gtuust", "name":"ГЦУСТ"},
                        {"id":"dis", "name":"Дирекция информационных систем"}
                    ]
                    return branches.find(i => i.name === name).id
                }

                var processList = [];
                $http.get('/camunda/api/engine/engine/default/process-definition?latest=true').then(
                    function (response) {
                        angular.forEach(response.data, function (value) {
                            processList.push(value.key);
                        });
                    }
                );
                var allKWMSProcesses = {
                    Dismantle: {
                        title: "Dismantle", value: false
                    },
                    Replacement: {
                        title: "Replacement", value: false
                    },
                    Revision: {
                        title: "Revision", value: false
                    },
                    CreatePR: {
                        title: "PR Creation", value: false
                    },
                    Invoice: {
                        title: "Monthly Act", value: false
                    },
                    leasing: {
                        title: "Roll-out", value: false
                    },
                    'create-new-tsd': {
                        title: "Create new TSD", value: false
                    },
                    'change-tsd': {
                        title: "Change TSD", value: false
                    },
                    'tsd-processing': {
                        title: "TSD Processing", value: false
                    },
                    'cancel-tsd': {
                        title: "Cancel TSD", value: false
                    }
                };

                scope.isProcessVisible = function (process) {
                    if (process === 'Revision') {
                        if ($rootScope.hasGroup('search_revision') || $rootScope.hasGroup('infrastructure_revision_users')) {
                            return true;
                        } else
                            return false;
                    } else if (process === 'Invoice') {
                        if ($rootScope.hasGroup('search_monthlyact') || $rootScope.hasGroup('infrastructure_monthly_act_users')) {
                            return true;
                        } else
                            return false;
                    } else if (process === 'leasing') {
                        if ($rootScope.hasGroup('search_rollout') || $rootScope.hasGroup('infrastructure_leasing_users')) {
                            return true;
                        } else
                            return false;
                    } else
                        return true;
                }
                if($rootScope.hasGroup('search_revision') || $rootScope.hasGroup('infrastructure_revision_users')){
                    allKWMSProcesses.Revision = {
                        title: "Revision", value: false
                    }
                    allKWMSProcesses['Revision-power'] = {
                        title: "Revision Power", value: false
                    }
                }
                if($rootScope.hasGroup('search_monthlyact') || $rootScope.hasGroup('infrastructure_monthly_act_users')){
                    allKWMSProcesses.Invoice = {
                        title: "Monthly Act", value: false
                    }
                    allKWMSProcesses.monthlyAct = {
                        title: "Monthly Act Roll-out & Revision 2020", value: false
                    }
                }
                if($rootScope.hasGroup('search_rollout') || $rootScope.hasGroup('infrastructure_leasing_users')){
                    allKWMSProcesses.leasing = {
                        title: "Roll-out", value: false
                    }
                }

                scope.KWMSProcesses = {};
                scope.processDefinitionKeys = {
                    'create-new-tsd': 'Create new TSD',
                    'tsd-processing': 'TSD Processing',
                    'change-tsd':'Change TSD',
                    'cancel-tsd':'Cancel TSD'
                }
                // scope.regionsMap = {
                //     'alm': 'Almaty',
                //     'astana': 'Astana',
                //     'nc': 'North & Center',
                //     'east': 'East',
                //     'south': 'South',
                //     'west': 'West'
                // };
                scope.contractorShortName = {
                    '4': 'LSE',
                    '5': 'Kcell_region',
                    '6': 'Алта Телеком',
                    '7': 'Логиком',
                    '8': 'Arlan SI '
                };
                scope.reasonShortName = {
                    '1': 'P&O',
                    '2': 'TNU',
                    '3': 'S&FM',
                    '4': 'SAO',
                    '5': 'RO'
                };
                scope.processInstancesTotal = 0;
                scope.processInstancesPages = 0;
                scope.selectedProcessInstances = [];
                scope.shownPages = 0;
                scope.xlsxPreparedRevision = false;
                scope.xlsxPreparedTnu =false;
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
                    if ((filtered.Revision || filtered.Invoice || filtered.CreatePR) && !filtered.leasing && !filtered.Dismantle && !filtered.Replacement  && !filtered['create-new-tsd'] && !filtered['change-tsd'] && !filtered['tsd-processing'] && !filtered['cancel-tsd']) {
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
                        if ((filtered.Revision || filtered.Invoice || filtered.monthlyAct || filtered.CreatePR) && !filtered.leasing && !filtered.Dismantle && !filtered.Replacement && !filtered['create-new-tsd'] && !filtered['change-tsd'] && !filtered['tsd-processing'] && !filtered['cancel-tsd']) {
                            scope.RevisionOrMonthlyAct = true;
                        }
                        angular.forEach(filtered, function (process, key) {
                            scope.selectedProcessInstances.push(key);
                        });
                    }
                    if (scope.onlyProcessActive!=='Revision' && scope.onlyProcessActive!=='CreatePR' && scope.onlyProcessActive!=='create-new-tsd' && scope.onlyProcessActive!=='change-tsd' && scope.onlyProcessActive!=='tsd-processing' && scope.onlyProcessActive!=='cancel-tsd' && scope.onlyProcessActive!=='Revision-power') {
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
                        scope.filter.contractor = 'All';
                        scope.filter.participation = undefined;
                        scope.filter.currentAssignee = undefined;
                    }
                    if (scope.onlyProcessActive!=='Dismantle') {
                        scope.filter.dismantlingInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive!=='Replacement') {
                        scope.filter.replacementInitiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                    }
                    if (scope.onlyProcessActive!=='Revision' && scope.onlyProcessActive!=='Dismantle' && scope.onlyProcessActive!=='Replacement' && scope.onlyProcessActive!=='CreatePR' && scope.onlyProcessActive!=='create-new-tsd' && scope.onlyProcessActive!=='change-tsd' && scope.onlyProcessActive!=='tsd-processing' && scope.onlyProcessActive!=='cancel-tsd') {
                        scope.filter.requestedDateRange = undefined;
                        scope.filter.requestor = undefined;
                    }
                    if (scope.onlyProcessActive!=='Invoice' && scope.onlyProcessActive!=='monthlyAct') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive==='Invoice' || scope.onlyProcessActive==='monthlyAct') {
                        // siteId&sitename are common filters except if Invoice selected
                        scope.filter.siteId = undefined;
                        scope.filter.sitename = undefined;
                    }
                    if (scope.onlyProcessActive!=='create-new-tsd') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive!=='change-tsd') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive!=='tsd-processing') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if (scope.onlyProcessActive!=='cancel-tsd') {
                        scope.filter.initiator = undefined;
                        scope.filter.businessKeyFilterType = 'all';
                        scope.filter.businessKey = undefined;
                        scope.filter.period = undefined;
                        scope.filter.monthOfFormalPeriod = undefined;
                        scope.filter.yearOfFormalPeriod = undefined;

                    }
                    if(scope.onlyProcessActive!=='leasing'){
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
                    if(scope.onlyProcessActive!=='Revision-power'){
                        scope.filter.validityDateRange = undefined;
                        $(".calendar-range-readonly").each(function () {
                            if ($(this).data('daterangepicker')) {
                                $(this).data('daterangepicker').setStartDate(moment());
                                $(this).data('daterangepicker').setEndDate(moment());
                            }
                        });
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

                function downloadXML(process){
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

                            if(process === 'Revision' || process === 'CreatePR'){
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
                            } else if (process === 'create-new-tsd' || process === 'change-tsd' || process === 'tsd-processing' || process === 'cancel-tsd') {
                                scope.tsdTasks = getUserTasks(xml);
                            } else if (process === 'Revision-power') {
                                scope.revisionPowerUserTasks = getUserTasks(xml);
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

                scope.powerSiteSelected = function ($item) {
                    scope.filter.powerSitename = $item.site_name;
                };

                scope.powerSiteIdSelected = function ($item) {
                    scope.filter.powerSiteId = $item.name;
                };

                scope.getSite = function (val) {
                    return $http.get('/camunda/sites/name/contains/' + val + '?forRollout=true').then(
                        function (response) {
                            return response.data
                        }
                    );
                };
                scope.getSiteId = function (val) {
                    return $http.get('/camunda/sites/name/contains/'+val + '?forRollout=true').then(
                        function(response){
                            console.log(response)
                            response.data.forEach(function(e){
                                e.name = e.siteid;
                            });
                            return response.data;
                        });
                };

                scope.getSites = function(val) {
                    return $http.get('/camunda/sites/name/contains/'+val).then(
                        function(response){
                            console.log(response)
                            response.data.forEach(function(e){
                                e.name = e.site_name;
                            });
                            return response.data;
                        });
                };
                scope.nearEndSelected = function ($item) {
                    scope.filter.nearend = $item.site_name;
                };
                scope.checkSite = function(){
                    return $http.get('/camunda/tsd_mw?nearend_id=' + $scope.site).then(
                        function(response) {
                            $scope.result = response.data;
                        });
                }
                scope.getXlsxProcessInstances = function () {
                    var fileName = scope.onlyProcessActive.toLowerCase() + '-search-result.xlsx';
                    if (scope.xlsxPreparedRevision || scope.xlsxPreparedTnu) {
                        var tbl = document.getElementById('xlsxRevisionsTable');
                        if ((scope.onlyProcessActive === 'Revision' && scope.selectedCustomFields.length > 0)|| (scope.onlyProcessActive === 'Revision-power' && scope.selectedCustomFields.length > 0)) {
                            tbl = document.getElementById('customXlsxRevisionsTable');
                        }

                        var ws;

                        if (scope.onlyProcessActive === 'Revision-power') {
                            var arrId = {
                                'headers': [
                                    "Contract",
                                    "JR To",
                                    "Year",
                                    "Month",
                                    "Region",
                                    "Sitename",
                                    "JR No",
                                    "JR Type",
                                    "JR Reason",
                                    "Requested Date",
                                    "Initiator",
                                    "Validity Date",
                                    "Acceptance Date",
                                    "Delay",
                                    "Job Description",
                                    "Quantity",
                                    "Comments",
                                    "Process State",
                                    "Sum",
                                    "Monthly act #"
                                ],
                                'data': []
                            }
    
                            scope['xlsxProcessInstances'].forEach(function (el, i) {
                                arrId['data'].push({"Acceptance Date": el['jrAcceptanceDate'] !== undefined ? el['jrAcceptanceDate'] : ""})
                                arrId['data'][i]["Delay"] = el['delay']
                                arrId['data'][i]["Job Description"] = ""
                                arrId['data'][i]["Quantity"] = ""
                                el["jobs"].forEach(function (v, n) {
                                    arrId['data'][i]["Job Description"] += n + 1 + "." + v.num + "," + v.title + "," + v.materialUnit + "\n"
                                    if (el["jobs"].length !== n + 1) arrId['data'][i]["Quantity"] += v.quantity + ",\n"
                                    else arrId['data'][i]["Quantity"] += v.quantity
                                })
                                arrId['data'][i]["Comments"] = el['init_comment']
                                arrId['data'][i]["Process State"] = el['state']
                                arrId['data'][i]["Sum"] = el['worksTotalSum']

                                arrId['data'][i]["Contract"] = el['contract']['name']
                                arrId['data'][i]["JR To"] = el["job_to"]

                                var monthNames = ["January", "February", "March", "April", "May", "June",
                                    "July", "August", "September", "October", "November", "December"
                                ];
                                var d = new Date(el["startTime"])
                                arrId['data'][i]["Year"] = d.getFullYear()
                                arrId['data'][i]["Month"] = monthNames[d.getMonth()]

                                arrId['data'][i]["Region"] = el['siteRegionShow'] !== undefined ? el['siteRegionShow'] : ""
                                arrId['data'][i]["Sitename"] = el['Site_Name']
                                arrId['data'][i]["JR No"] = el["businessKey"]
                                arrId['data'][i]["JR Type"] = el["jrType"]["name"]
                                arrId['data'][i]["JR Reason"] = el["jrReason"]["name"]
                                arrId['data'][i]["Requested Date"] = el["jrOrderedDate"]
                                arrId['data'][i]["Initiator"] = el["initiatorFull"]["firstName"] + " " + el["initiatorFull"]["lastName"]
                                arrId['data'][i]["Validity Date"] = el["validityDate"]
                            })

                            ws = XLSX.utils.json_to_sheet(arrId['data'], {header: arrId['headers'], dateNF: 'DD.MM.YYYY'})
                        } else {
                            ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});
                        }

                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                        return XLSX.writeFile(wb, fileName);
                        // return XLSX.writeFile(wb, fileName, {});
                    } else {
                        getProcessInstances(scope.lastSearchParamsRevision, 'xlsxProcessInstances');
                        scope.xlsxPreparedRevision = true;
                    }

                }
                scope.getXlsxProcessInstancesTnu = function () {
                    var fileName = scope.onlyProcessActive.toLowerCase() + '-search-result.xlsx';
                    if ( scope.xlsxPreparedTnu) {
                        var tbl = document.getElementById('xlsxTnuTable');
                        if ((scope.onlyProcessActive === 'create-new-tsd' || scope.onlyProcessActive === 'change-tsd' || scope.onlyProcessActive === 'tsd-processing' || scope.onlyProcessActive === 'cancel-tsd') && scope.selectedCustomFields.length > 0) {
                            tbl = document.getElementById('customXlsxTnuTable');
                        }
                        var ws = XLSX.utils.table_to_sheet(tbl, {dateNF: 'DD.MM.YYYY'});
                        var wb = XLSX.utils.book_new();
                        XLSX.utils.book_append_sheet(wb, ws, 'New Sheet Name 1');
                        return XLSX.writeFile(wb, fileName);
                    } else {
                        getProcessInstances(scope.lastSearchParamsTnu, 'xlsxProcessInstances');
                        scope.xlsxPreparedTnu = true;
                    }

                }
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
                scope.populateSelectFromCatalog(['tnuProcessTypeList'], 122);
                scope.populateSelectFromCatalog(['regionList'], 5);

                scope.search = function (refreshPages) {
                    var asynCall1 = false;
                    var asynCall2 = false;
                    var asynCall3 = false;
                    var asynCall4 = false;
                    if (refreshPages) {
                        scope.filter.page = 1;
                        scope.piIndex = undefined;
                        scope.xlsxPreparedRevision = false;
                        scope.xlsxPreparedTnu =false;
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
                        processInstanceBusinessKeyLike: '%-%'

                    }
                    if(excludeProcesses.length > 0){
                        filter.processDefinitionKeyNotIn = excludeProcesses;
                    }

                    if(scope.selectedProcessInstances.indexOf('Dismantle')!==-1 && scope.selectedProcessInstances.indexOf('Replacement')===-1){
                        filter.variables.push({"name": "requestType", "operator": "eq", "value": "dismantle"});
                    }
                    if(scope.selectedProcessInstances.indexOf('Replacement')!==-1 && scope.selectedProcessInstances.indexOf('Dismantle')===-1){
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
                    if (scope.filter.tnuRegion && scope.filter.tnuRegion !== 'all') {
                        filter.variables.push({"name": "region", "operator": "eq", "value": scope.filter.tnuRegion});
                    }
                    if (scope.filter.siteId) {
                        filter.variables.push({"name": "siteName", "operator": "eq", "value": scope.filter.siteId});
                    }
                    if (scope.filter.sitename) {
                        filter.variables.push({"name": "site_name", "operator": "eq", "value": scope.filter.sitename});
                    }
                    if(scope.filter.nearend) {
                        filter.variables.push({"name": "site_name", "operator": "eq", "value": scope.filter.nearend});
                    }
                    if(scope.contractor) {
                        filter.variables.push({"name": "contractor", "operator": "eq", "value": scope.filter.contractor});
                    }

                    if (scope.filter.businessKey) {
                        if (scope.filter.businessKeyFilterType === 'eq') {
                            filter.processInstanceBusinessKey = scope.filter.businessKey;
                        } else {
                            filter.processInstanceBusinessKeyLike = '%'+scope.filter.businessKey+'%';
                        }
                    }
                    if (scope.filter.workType) {
                        if (scope.onlyProcessActive==='Invoice')
                            filter.variables.push({"name": "workType", "operator": "eq", "value": scope.filter.workType});
                        else if (scope.onlyProcessActive==='Revision' || scope.onlyProcessActive==='CreatePR')
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
                        if(scope.onlyProcessActive==='Revision' || scope.onlyProcessActive==='CreatePR' || scope.onlyProcessActive==='create-new-tsd' || scope.onlyProcessActive==='change-tsd' || scope.onlyProcessActive==='tsd-processing' || scope.onlyProcessActive==='cancel-tsd'){
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
                        } else if(scope.onlyProcessActive==='Dismantle'){
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
                        } else if(scope.onlyProcessActive==='Replacement'){
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
                    if (scope.filter.jrOrderedDate) {
                        var results = scope.convertStringToDate(scope.filter.jrOrderedDate);
                        if (results.length === 2) {
                            filter.variables.push({
                                "name": "jrOrderedDate",
                                "operator": "gteq",
                                "value": results[0]
                            });

                            filter.variables.push({
                                "name": "jrOrderedDate",
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
                    if (scope.filter.participation && (scope.onlyProcessActive==='Revision' || scope.onlyProcessActive==='Revision-power' || scope.onlyProcessActive==='CreatePR' || scope.onlyProcessActive==='create-new-tsd' || scope.onlyProcessActive==='change-tsd' || scope.onlyProcessActive==='tsd-processing' || scope.onlyProcessActive==='cancel-tsd')) {
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
                                        scope.lastSearchParamsRevision = filter;
                                        scope.lastSearchParamsTnu = filter;
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
                                    scope.lastSearchParamsRevision = filter;
                                    scope.lastSearchParamsTnu = filter;
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


                    if (scope.filter.activityId && (scope.onlyProcessActive==='Revision' || scope.onlyProcessActive==='Revision-power' || scope.onlyProcessActive==='CreatePR' || scope.onlyProcessActive==='create-new-tsd' || scope.onlyProcessActive==='change-tsd' || scope.onlyProcessActive==='tsd-processing' || scope.onlyProcessActive==='cancel-tsd')) {
                        filter.activeActivityIdIn.push(scope.filter.activityId);
                    }
                    if (scope.filter.dismantleActivityId && (scope.onlyProcessActive==='Dismantle' || scope.onlyProcessActive==='Replacement')) {
                        filter.activeActivityIdIn.push(scope.filter.dismantleActivityId);
                    }
                    if (scope.filter.mainContract && scope.filter.mainContract !== 'All') {
                        filter.variables.push({
                            "name": "mainContract",
                            "operator": "eq",
                            "value": scope.filter.mainContract
                        });
                    }
                    if (scope.filter.contractor && scope.filter.contractor !== 'All') {
                        filter.variables.push({
                            "name": "contractor",
                            "operator": "eq",
                            "value": Number(scope.filter.contractor)
                        });
                    }
                    if(scope.onlyProcessActive==='Revision-power') {
                        if(scope.filter.powerActivityId){
                            filter.activeActivityIdIn.push(scope.filter.powerActivityId);
                        }
                        if (scope.filter.powerRegion && scope.filter.powerRegion !== 'all') {
                            filter.variables.push({"name": "siteRegionShow", "operator": "eq", "value": scope.filter.powerRegion});
                        }
                        if (scope.filter.powerSiteId) {
                            filter.variables.push({"name": "Site", "operator": "eq", "value": scope.filter.siteId});
                        }
                        if (scope.filter.powerSitename) {
                            filter.variables.push({"name": "Site_Name", "operator": "eq", "value": scope.filter.powerSitename});
                        }
                        if(scope.filter.jrType) {
                            filter.variables.push({"name": "jrTypeName", "operator": "eq", "value": scope.filter.jrType});
                        }
                        if(scope.filter.jrReason) {
                            filter.variables.push({"name": "jrReasonName", "operator": "eq", "value": scope.filter.jrReason});
                        }
                        if(scope.filter.powerContractor) {
                            filter.variables.push({"name": "contractorName", "operator": "eq", "value": scope.filter.powerContractor});
                        }
                        if(scope.filter.powerContract) {
                            filter.variables.push({"name": "contractName", "operator": "eq", "value": scope.filter.powerContract});
                        }
                    }
                    if(scope.onlyProcessActive==='leasing'){
                        if(scope.filter.leasingCandidateLegalType){
                            filter.variables.push({"name": "leasingCandidateLegalType","operator": "eq","value": scope.filter.leasingCandidateLegalType});
                        }
                        if(scope.filter.leasingRbsType){
                            filter.variables.push({"name": "rbsType","operator": "eq","value": scope.filter.leasingRbsType});
                        }
                        if(scope.filter.leasingFarEndLegalType){
                            filter.variables.push({"name": "leasingFarEndLegalType","operator": "like","value": '%,' + scope.filter.leasingFarEndLegalType + ',%'});
                        }
                        if(scope.filter.ncpId){
                            filter.variables.push({"name": "ncpID","operator": "eq","value": scope.filter.ncpId});
                        }
                        if(scope.filter.leasingCabinetType){
                            filter.variables.push({"name": "plannedCabinetType","operator": "eq","value": scope.filter.leasingCabinetType});
                        }
                        if(scope.filter.leasingKazakhtelecomBranch && scope.filter.leasingCandidateLegalType==='national_kazakhtelecom'){
                            filter.variables.push({"name": "branchKT","operator": "eq","value": scope.filter.leasingKazakhtelecomBranch});
                        }
                        if(scope.filter.leasingSiteType){
                            filter.variables.push({"name": "siteTypeForSearch","operator": "eq","value": scope.filter.leasingSiteType});
                        }
                        if(scope.filter.leasingBand){
                            filter.variables.push({"name": "bandsJoinedByComma","operator": "like","value": '%,' + scope.filter.leasingBand + ',%'});
                        }
                        if(scope.filter.leasingContractType){
                            filter.variables.push({"name": "contractTypeJoinedByComma","operator": "like","value": '%,' + scope.filter.leasingContractType + ',%'});
                        }
                        if(scope.filter.leasingInitiator){
                            filter.variables.push({"name": "initiatorForSearch","operator": "eq","value": scope.filter.leasingInitiator});
                        }
                        if(scope.filter.leasingGeneralStatus){
                            filter.variables.push({"name": "generalStatus","operator": "eq","value": scope.filter.leasingGeneralStatus});
                        }
                        if(scope.filter.leasingContractExecutor){
                            filter.variables.push({"name": "contractExecutorJoinedByComma","operator": "like","value": '%,' + scope.filter.leasingContractExecutor.name + ',%'});
                        }
                        if(scope.filter.leasingProject){
                            filter.variables.push({"name": "projectForSearch","operator": "eq","value": scope.filter.leasingProject});
                        }
                        if(scope.filter.transmissionType){
                            filter.variables.push({"name": "transmissionTypeForSearch","operator": "eq","value": scope.filter.transmissionType});
                        }
                        if(scope.filter.leasingInstallationStatus){
                            filter.variables.push({"name": "installationStatus","operator": "eq","value": scope.filter.leasingInstallationStatus});
                        }
                        if(scope.filter.bin){
                            filter.variables.push({"name": "contractBinsJoinedByComma","operator": "like","value": '%,' + scope.filter.bin + ',%'});
                        }
                        if(scope.filter.leasingActivityId){
                            filter.activeActivityIdIn.push(scope.filter.leasingActivityId);
                        }
                        if(scope.filter.leasingReason){
                            filter.variables.push({"name": "reasonForSearch","operator": "eq","value": scope.filter.leasingReason});
                        }
                    }
                    asynCall3 = true;
                    if (asynCall1 && asynCall2 && asynCall3 && asynCall4) {
                        scope.lastSearchParamsRevision = filter;
                        scope.lastSearchParamsTnu = filter;
                        getProcessInstances(filter, 'processInstances');
                        asynCall3 = false;
                    }
                };

                scope.clearFilters = function () {
                    scope.filter.region = 'all';
                    scope.filter.tnuRegion = 'all';
                    scope.filter.initiator = undefined;
                    scope.filter.period = undefined;
                    scope.filter.siteId = undefined;
                    scope.filter.sitename = undefined;
                    scope.filter.nearend = undefined;
                    scope.filter.businessKey = undefined;
                    scope.filter.workType = undefined;
                    scope.filter.participation = undefined;
                    scope.filter.currentAssignee = undefined;
                    scope.filter.beginYear = scope.currentDate.getFullYear()-1;
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
                    scope.filter.contractor = 'All';
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

                    scope.filter.powerContract = undefined;
                    scope.filter.powerContractor = undefined;
                    scope.filter.jrType = undefined;
                    scope.filter.jrReason = undefined;
                    scope.filter.jrOrderedDate = undefined;
                    scope.filter.powerActivityId = undefined;
                    scope.filter.powerRegion = 'all';

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
                            // console.log(result)
                            scope[processInstances] = result.data;
                            var variables = ['siteRegion', 'site_name', 'contractor', 'reason', 'requestedDate','validityDate', 'jobWorks', 'explanation', 'workType'];

                            if(scope.selectedProcessInstances.indexOf('Dismantle')!==-1 || scope.selectedProcessInstances.indexOf('Replacement')!==-1){
                                variables.push('requestType');
                            }
                            if(scope.selectedProcessInstances.indexOf('create-new-tsd')!==-1 || scope.selectedProcessInstances.indexOf('change-tsd')!==-1 || scope.selectedProcessInstances.indexOf('tsd-processing')!==-1 || scope.selectedProcessInstances.indexOf('cancel-tsd')!==-1){
                                variables.push('region');
                            }
                            if(scope.selectedProcessInstances.indexOf('leasing')!==-1){
                                variables.push('generalStatus');
                                variables.push('generalStatusUpdatedDate');
                                variables.push('installationStatusUpdatedDate');
                                if(scope.filter.leasingInitiator){
                                    variables.push('initiator');
                                }
                                if(scope.filter.leasingProject){
                                    variables.push('project');
                                }
                                if(scope.filter.leasingReason){
                                    variables.push('reason');
                                }
                                if(scope.filter.ncpId){
                                    variables.push('ncpID');
                                }
                                if(scope.filter.leasingSiteType){
                                    variables.push('siteType');
                                }
                                if(scope.filter.leasingRbsType){
                                    variables.push('rbsType');
                                }
                                if(scope.filter.leasingCabinetType){
                                    variables.push('plannedCabinetType');
                                }
                                if(scope.filter.leasingBand){
                                    variables.push('bands');
                                }
                                if(scope.filter.leasingInstallationStatus){
                                    variables.push('installationStatus');
                                }
                                if(scope.filter.leasingCandidateLegalType){
                                    variables.push('leasingCandidateLegalType');
                                }
                                if(scope.filter.leasingKazakhtelecomBranch && scope.filter.leasingCandidateLegalType==='national_kazakhtelecom'){
                                    variables.push('branchKT');
                                }
                                if(scope.filter.leasingFarEndLegalType){
                                    variables.push('farEndInformation');
                                }
                                if(scope.filter.leasingContractType || scope.filter.leasingContractExecutor || scope.filter.bin){
                                    variables.push('contractInformations');
                                }
                            }
                            if(scope.selectedProcessInstances.indexOf('Revision')!==-1 || scope.selectedProcessInstances.indexOf('CreatePR')!==-1){
                                variables.push('monthActNumber');
                                variables.push('invoiceRO1Number');
                                variables.push('invoiceRO2Number');
                                variables.push('invoiceRO3Number');
                            }
                                
                            if(scope.selectedProcessInstances.indexOf('Revision-power')!==-1) {
                                variables.push('contract');
                                variables.push('jrReason');
                                variables.push('initiatorFull');
                                variables.push('job_to');
                                variables.push('jrAcceptanceDate');
                                variables.push('jobs');
                                variables.push('init_comment');
                                variables.push('Site_Name');
                                variables.push('jrType');
                                variables.push('worksTotalSum');
                                variables.push('siteRegionShow');
                                variables.push('jrOrderedDate');
                            }

                            if (scope[processInstances].length > 0) {
                                angular.forEach(scope[processInstances], function (el) {
                                    // console.log(el)
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
                                                if (el.processDefinitionKey === 'Revision-power') {
                                                    let delay = 0;
                                                    if (el.jrAcceptanceDate) {
                                                        var acceptDate = new Date(el.jrAcceptanceDate);
                                                        var validDate = new Date(el.validityDate);
                                                        delay = Math.floor((acceptDate.getTime() - validDate.getTime()) / (1000 * 60 * 60 * 24));
                                                    } else {
                                                        var validDate = new Date(el.validityDate);
                                                        delay = Math.floor(((new Date().getTime()) - validDate.getTime()) / (1000 * 60 * 60 * 24));
                                                    }
                                                    el.delay = delay > 0 ? delay : 0;
                                                }
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
                                        // console.log("1111====")
                                        // console.log(tasks)
                                        angular.forEach(scope[processInstances], function (el) {
                                            // console.log("123====")
                                            // console.log(scope[processInstances])
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
                scope.toggleProcess = function (process) {
                    if (scope.KWMSProcesses[process].value) {
                        scope.KWMSProcesses[process].value = false;
                    } else {
                        scope.KWMSProcesses[process].value = true;
                        if ((process === 'Revision' || process === 'Revision-power' || process === 'CreatePR') && !scope.KWMSProcesses[process].downloaded){
                            downloadXML(process);
                            scope.KWMSProcesses[process].downloaded = true;
                            if (process === 'Revision-power') {
                                $http.get('/api/revisionPowerJobs').then(
                                    function(result) {
                                      scope.jrTypesList = result.data.jrTypesList;
                                      scope.jrReasonsList = result.data.jrReasonsList;
                                      scope.contractsList = result.data.contractsList;
                                      scope.contractorsList = result.data.contractorsList;
                                    }
                                  );
                            }
                        } else if((process === 'Dismantle' || process === 'Replacement') && !scope.KWMSProcesses[process].downloaded){
                            downloadXML('sdr_srr_request');
                            scope.KWMSProcesses[process].downloaded = true;
                        } else if(process === 'leasing' && !scope.KWMSProcesses[process].downloaded){
                            downloadXML('leasing');
                            scope.KWMSProcesses[process].downloaded = true;
                        } else if(process === 'create-new-tsd' || process === 'change-tsd' || process === 'tsd-processing' || process === 'cancel-tsd'){
                            downloadXML(process);
                        }
                    }
                };
                scope.toggleProcessViewRevision = function (index, processDefinitionKey, processDefinitionId, businessKey) {
                    console.log()
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
                                                try {
                                                    // TEMPORARY Revision card OPENER  ---- MUST BE RESOLVED ---- start
                                                    if(processDefinitionKey === 'Revision-power') {
                                                        console.log("opened process")
                                                        openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                                    }
                                                    // ---- end
                                                    if (taskResult.data._embedded && taskResult.data._embedded.group) {
                                                        e.group = taskResult.data._embedded.group[0].id;
                                                        groupasynCalls += 1;
                                                        if (groupasynCalls === maxGroupAsynCalls) {
                                                            asynCall1 = true;
                                                            if (asynCall1 && asynCall2) {
                                                                if (processDefinitionKey === 'CreatePR'){
                                                                    openProcessCardModalCreatePR(processDefinitionId, businessKey, index);
                                                                } else if (processDefinitionKey === 'Revision-power') {
                                                                    openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                                                } else {
                                                                    openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                                }

                                                                asynCall1 = false;
                                                            } else console.log('asynCall 2 problem');
                                                        } else {
                                                            console.log(groupasynCalls, maxGroupAsynCalls);
                                                        }
                                                    } else {
                                                        console.log('vtoroi', groupasynCalls, maxGroupAsynCalls);
                                                        console.log(processDefinitionKey)
                                                        groupasynCalls += 1;
                                                        if (groupasynCalls === maxGroupAsynCalls) {
                                                            asynCall1 = true;
                                                            if (asynCall1 && asynCall2) {
                                                                if (processDefinitionKey === 'CreatePR'){
                                                                    openProcessCardModalCreatePR(processDefinitionId, businessKey, index);
                                                                } else if (processDefinitionKey === 'Revision-power'){
                                                                    openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                                                } else {
                                                                    openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                                }
                                                                asynCall1 = false;
                                                            } else console.log('asynCall 2 problem');
                                                        } else {
                                                            console.log(groupasynCalls, maxGroupAsynCalls);

                                                        }
                                                    }
                                                    asynCall1 = false;
                                                } catch (err) {
                                                    console.log(err)
                                                }
                                                
                                            },
                                            function (error) {
                                                console.log(error.data);
                                            }
                                        );

                                    });

                                } else {
                                    try {
                                        asynCall1 = true;
                                        // TEMPORARY Revision card OPENER  ---- MUST BE RESOLVED ---- start
                                        if(processDefinitionKey === 'Revision-power') {
                                            console.log("closed process")
                                            openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                        }
                                        // ---- end
                                        if (asynCall1 && asynCall2) {
                                            if (processDefinitionKey === 'CreatePR'){
                                                openProcessCardModalCreatePR(processDefinitionId, businessKey, index);
                                            } else if (processDefinitionKey === 'Revision-power'){
                                                openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                            } else {
                                                openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                            }
                                            asynCall1 = false;
                                        }
                                        asynCall1 = false;
                                    } catch (err) {
                                        console.log(err)
                                    }
                                    
                                }
                                $http.get(baseUrl + '/history/variable-instance?deserializeValues=false&processInstanceId=' + scope.processInstances[index].id).then(
                                    function (result) {
                                        var workFiles = [];
                                        result.data.forEach(function (el) {
                                            scope.jobModel[el.name] = el;
                                            // console.log(el.name + ' - ' + el.value)
                                            if (el.type === 'File' || el.type === 'Bytes') {
                                                scope.jobModel[el.name].contentUrl = baseUrl + '/history/variable-instance/' + el.id + '/data';
                                            }
                                            if (el.type === 'Json') {
                                                scope.jobModel[el.name].value = JSON.parse(el.value);
                                            }
                                            if (el.name.startsWith('works_') && el.name.includes('_file_')) {
                                                workFiles.push(el);
                                            }
                                            // if (processDefinitionKey == 'createPR') {

                                            // }
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
                                                asynCall2 = true;
                                                try {
                                                    if (asynCall1 && asynCall2) {
                                                        if (processDefinitionKey === 'CreatePR'){
                                                            openProcessCardModalCreatePR(processDefinitionId, businessKey, index);
                                                        } else if (processDefinitionKey === 'Revision-power'){
                                                            openProcessCardModalRevisionPower(processDefinitionId, businessKey, index);
                                                        } else {
                                                            openProcessCardModalRevision(processDefinitionId, businessKey, index);
                                                        }
                                                        asynCall2 = false;
                                                    } else console.log('aynCall 1 problem');
                                                } catch(err) {
                                                    console.log(err)
                                                }
                                                
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
                    let checkStatus = scope.jobModel.tasks.findIndex(i => i.name === 'Attach scan copy of documents')
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
                            blockExportButtons: checkStatus > -1,
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
                            export: function(source) {
                                let url;
                                if (source === 'fin') {
                                    url = 'generateFin'
                                } else {
                                    url = 'generateNotFin'
                                }
                                $http({
                                    method: 'POST',
                                    url: '/camunda/monthlyactandrevision/' + url,
                                    headers: {
                                        'Content-type': 'application/json',
                                        'Accept': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
                                    },
                                    responseType: 'arraybuffer',
                                    data: {
                                        selectedRevisions: scope.jobModel.selectedRevisions.value,
                                        subcontractor: scope.jobModel.subcontractor.value,
                                        businessKey: businessKey,
                                        monthOfFormalPeriod: scope.jobModel.monthOfFormalPeriod.value,
                                        yearOfFormalPeriod: scope.jobModel.yearOfFormalPeriod.value
                                    }
                                }).then(function(response){
                                    const url = window.URL.createObjectURL(new Blob([response.data]));
                                    const link = document.createElement('a');
                                    link.href = url;
                                    let arr = businessKey.split('-');
                                    let fileName = 'file'
                                    if (arr.length === 3) {
                                        arr[1] = arr[1].substring(2)
                                        fileName = arr.join('-')
                                        if (source === 'fin') {
                                            fileName += '- Fin'
                                        } 
                                    }
                                    link.setAttribute('download', `${fileName}.docx`); //or any other extension
                                    document.body.appendChild(link);
                                    link.click();
                                    
                                }).catch((e) => {
                                    console.log(e)
                                })
                            },
                            compareDate: new Date('2019-02-05T06:00:00.000'),
                        },
                        templateUrl: './js/partials/processCardModal.html',
                        size: ((scope.jobModel.processDefinitionKey === 'Invoice' || scope.jobModel.processDefinitionKey === 'monthlyAct') ? 'hg' : 'lg')
                    }).then(function (results) {
                    });
                }

                function openProcessCardModalRevisionPower(processDefinitionId, businessKey, index) {
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
                        },
                        templateUrl: './js/partials/revisionPowerCardModal.html',
                        size: 'lg'
                    })
                }

                function openProcessCardModalCreatePR(processDefinitionId, businessKey, index) {
                    try{
                        scope.documentType = {1:"ZK73-02", 2:"ZK73-03", 3:"ZK73-04", 4:"ZK73-01"};

                        if(!(scope.jobModel.hasOwnProperty('sapFaList') && scope.jobModel.sapFaList !== null)) {
                            scope.jobModel.sapFaList = {
                                value : []
                            }
                        }

                        scope.capexWorks = ['1','2','3','4','5','8','10','11','12','14','15','16','17','19','20','22','23','25','26','28','29','31','32','34',
                            '35','36','38','42','45', '46', '47', '48', '49', '50', '54', '55', '56', '57', '60', '62', '65', '66', '71', '72',
                            '77', '78', '79', '80', '81', '86', '87', '88', '91', '94', '97', '100', '103', '104', '105', '106', '112', '113',
                            '114', '115', '122', '131', '134', '138', '141', '144', '147', '150', '151', '155', '156', '157', '158', '159', '160',
                            '161', '162', '165', '168', '169', '172', '173'];

                        scope.undefinedWorks = ['39', '40', '41', '43', '61', '63', '67', '68', '73', '74', '82', '84', '85', '89', '92', '95', '98', '101', '107',
                            '108', '116', '117', '118', '123', '125', '126', '127', '128', '129', '130', '132', '135', '137', '139', '142', '145',
                            '148', '152', '153', '154', '166'];

                        scope.opexWorks = ['6', '7', '9', '13', '18', '21', '24', '27', '30', '33', '37', '44', '51', '52', '53', '58', '59', '64', '69', '70', '75',
                            '76', '83', '90', '93', '96', '99', '102', '109', '110', '111', '119', '120', '121', '124', '133', '136', '140', '143',
                            '146', '149', '163', '164', '167', '170', '171'];

                        scope.subcontructorDirectory = {
                            "1": {
                                "code": "949",
                                "responsible": "GULZHAN IMANDOSOVA"
                            },
                            "2": {
                                "code": "950",
                                "responsible": "AIGERIM SATYBEKOVA"
                            },
                            "3": {
                                "code": "951",
                                "responsible": "AIGERIM SATYBEKOVA"
                            },
                            "4": {
                                "code": "10",
                                "responsible": "KEREMET IBRAGIMOVA"
                            }
                        }

                        scope.jobWorksValue = scope.jobModel.jobWorks.value;
                        scope.workDefinitionMap = scope.jobModel.hasOwnProperty("workDefinitionMap") ? scope.jobModel.workDefinitionMap.value : {};
                        scope.tnuSiteLocations = scope.jobModel.hasOwnProperty("tnuSiteLocations") ? scope.jobModel.tnuSiteLocations.value : {};
                        scope.workPrices = scope.jobModel.hasOwnProperty("workPrices") ? scope.jobModel.workPrices.value : {};
                        // console.log(scope.jobModel)
                        scope.jobWorksTotal = scope.jobModel.hasOwnProperty("jobWorksTotal") && scope.jobModel.jobWorksTotal.hasOwnProperty("value") ? scope.jobModel.workPrices.value : '';
                        scope.jobWorksValue.forEach(function(work){
                            if(scope.capexWorks.indexOf(work.sapServiceNumber)!==-1){
                                work.expenseType = 'CAPEX';
                            } else if(scope.opexWorks.indexOf(work.sapServiceNumber)!==-1){
                                work.expenseType = 'OPEX';
                            }
                        });
                        calculateExpense(true);
                        scope.jobWorksValueTemp = [];
                        scope.jobWorksValue.forEach(function(w, index) {
                            if(scope.jobModel.reason.value != '2' || (scope.jobModel.reason.value == '2' && w.relatedSites.length == 0)) {
                                w.r = {
                                    site_name: scope.jobModel.site_name.value
                                };
                                w.prItemText = 'installation service ' + w.r.site_name;
                                w.index = index;
                                w.uuid = uuidv4();
                                w.requestedDate = new Date(scope.jobModel.requestedDate.value);
                                w.deliveryDate = new Date(new Date().getFullYear(), 11, 31);
                                w.open = function($event){
                                    $event.preventDefault();
                                    $event.stopPropagation();
                                    this.requestedObjectDateOpened = true;
                                };
                                w.deliveryDateOpen = function($event){
                                    $event.preventDefault();
                                    $event.stopPropagation();
                                    this.deliveryDateOpened = true;
                                };
                                if(!w.price){
                                    w.price = {};
                                }
                                if(!w.price.unitWorkPricePerSite) {
                                    w.price.unitWorkPricePerSite = '0.0';
                                }
                                if(!w.price.unitWorkPricePlusTx) {
                                    w.price.unitWorkPricePlusTx = '0.0';
                                }
                                w.amountText = scope.jobModel.reason.value == '2' ? w.price.unitWorkPricePerSite.replace('.',',') : w.price.unitWorkPricePlusTx.replace('.',',');
                                w.headerNotes = '1.Purchase description: Revision works for site ' + scope.jobModel.site_name.value + ' JR# ' + scope.jobModel.jrNumber.value + ' dated ' + $filter('date')(w.requestedDate,'dd.MM.yyyy') + ' ' +
                                    '2.Budgeted or not: yes ' + w.wbsElement + ' ' +
                                    '3.Main project for Fintur: revision works ' +
                                    '4.Describe the need of this purchase for this year: necessary for revision works ' +
                                    '5.Contact person: ' + scope.subcontructorDirectory[scope.jobModel.reason.value].responsible + ' ' +
                                    '6. Vendor: Line System Engineering LLP ' +
                                    '7. Total sum: ' + scope.jobWorksTotal + '';
                                scope.jobWorksValueTemp.push(w);
                            } else {
                                w.relatedSites.forEach(function(r, rindex) {
                                    w.r = r;
                                    w.index = rindex + index;
                                    w.uuid = uuidv4();
                                    w.prItemText = 'installation service ' + w.r.site_name;

                                    w.requestedDate = new Date(scope.jobModel.requestedDate.value);
                                    w.deliveryDate = new Date(new Date().getFullYear(), 11, 31);
                                    w.open = function($event){
                                        $event.preventDefault();
                                        $event.stopPropagation();
                                        this.requestedObjectDateOpened = true;
                                    };
                                    w.deliveryDateOpen = function($event){
                                        $event.preventDefault();
                                        $event.stopPropagation();
                                        this.deliveryDateOpened = true;
                                    };
                                    if(!w.price){
                                        w.price = {};
                                    }
                                    if(!w.price.unitWorkPricePerSite) {
                                        w.price.unitWorkPricePerSite = '0.0';
                                    }
                                    if(!w.price.unitWorkPricePlusTx) {
                                        w.price.unitWorkPricePlusTx = '0.0';
                                    }
                                    w.amountText = scope.jobModel.reason.value == '2' ? w.price.unitWorkPricePerSite.replace('.',',') : w.price.unitWorkPricePlusTx.replace('.',',');
                                    w.headerNotes = '1.Purchase description: Revision works for site ' + w.r.site_name + ' JR# ' + scope.jobModel.jrNumber.value + ' dated ' + $filter('date')(w.requestedDate,'dd.MM.yyyy') + ' ' +
                                        '2.Budgeted or not: yes ' + w.wbsElement + ' ' +
                                        '3.Main project for Fintur: revision works ' +
                                        '4.Describe the need of this purchase for this year: necessary for revision works ' +
                                        '5.Contact person: ' + scope.subcontructorDirectory[scope.jobModel.reason.value].responsible + ' ' +
                                        '6. Vendor: Line System Engineering LLP ' +
                                        '7. Total sum: ' + scope.jobWorksTotal + '';
                                    scope.jobWorksValueTemp.push(angular.copy(w));
                                });
                            }
                        });
                        // console.log('scope->>>>')
                        // console.log(scope)
                        // console.log('<<<<-scope')
                    } catch(E) {
                        console.log(E);
                    }


                    function calculateExpense(isInit) {
                        if(isInit){
                            scope.jobWorksValue.forEach(function(work){
                                work.price = _.find(scope.workPrices, function(p){
                                    return work.sapServiceNumber === p.sapServiceNumber
                                });

                                if(work.expenseType==='CAPEX'){
                                    work.activityServiceNumber = 'DUMMY';
                                    if(scope.jobModel.reason.value === '2'){
                                        work.wbsElement = 'TN-0502-48-0236';
                                    } else {
                                        work.wbsElement = 'RN-0502-33-0236';
                                    }
                                    work.controllingArea = 'DUMMY';
                                } else if(work.expenseType==='OPEX'){
                                    if(scope.jobModel.reason.value === '2'){
                                        work.activityServiceNumber = '7016046';
                                    } else {
                                        work.activityServiceNumber = '7016045';
                                    }
                                    if(scope.jobModel.reason.value === '4'){
                                        work.wbsElement = '252-70160-1';
                                    } else {
                                        work.wbsElement = '251-70160-1';
                                    }
                                    work.controllingArea = '3020';
                                }
                                work.costCenter = '25510';

                                if(work.expenseType==='CAPEX'){
                                    if(scope.jobModel.reason.value === '2'){
                                        angular.forEach(work.relatedSites, function (rs) {
                                            angular.forEach(scope.jobModel.sapFaList.value, function (fa) {
                                                if(scope.workDefinitionMap !== null && fa.faClass == scope.workDefinitionMap[work.sapServiceNumber].faClass && fa.sloc == scope.tnuSiteLocations[rs.site_name].siteLocation){
                                                    scope.tnuSiteLocations[rs.site_name].work[work.sapServiceNumber].fixedAssetNumber = fa.faNumber;
                                                }
                                            });
                                        });
                                    } else {
                                        angular.forEach(scope.jobModel.sapFaList.value, function (fa) {
                                            if(scope.workDefinitionMap !== null && fa.faClass === scope.workDefinitionMap[work.sapServiceNumber].faClass && fa.sloc == scope.jobModel.sloc.value){
                                                work.fixedAssetNumber = fa.faNumber;
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            scope.jobWorksValueTemp.forEach(function(work){
                                work.price = _.find(scope.workPrices, function(p){
                                    return work.sapServiceNumber === p.sapServiceNumber
                                });

                                if(work.expenseType==='CAPEX'){
                                    work.activityServiceNumber = 'DUMMY';
                                    if(scope.jobModel.reason.value === '2'){
                                        work.wbsElement = 'TN-0502-48-0236';
                                    } else {
                                        work.wbsElement = 'RN-0502-33-0236';
                                    }
                                    work.controllingArea = 'DUMMY';
                                } else if(work.expenseType==='OPEX'){
                                    if(scope.jobModel.reason.value === '2'){
                                        work.activityServiceNumber = '7016046';
                                    } else {
                                        work.activityServiceNumber = '7016045';
                                    }
                                    if(scope.jobModel.reason.value === '4'){
                                        work.wbsElement = '252-70160-1';
                                    } else {
                                        work.wbsElement = '251-70160-1';
                                    }
                                    work.controllingArea = '3020';
                                }
                                work.costCenter = '25510';

                                if(work.expenseType==='CAPEX'){
                                    if(scope.jobModel.reason.value === '2'){
                                        angular.forEach(work.relatedSites, function (rs) {
                                            angular.forEach(scope.jobModel.sapFaList.value, function (fa) {
                                                if(fa.faClass == scope.workDefinitionMap[work.sapServiceNumber].faClass && fa.sloc == scope.tnuSiteLocations[rs.site_name].siteLocation){
                                                    scope.tnuSiteLocations[rs.site_name].work[work.sapServiceNumber].fixedAssetNumber = fa.faNumber;
                                                }
                                            });
                                        });
                                    } else {
                                        angular.forEach(scope.jobModel.sapFaList.value, function (fa) {
                                            if(fa.faClass === scope.workDefinitionMap[work.sapServiceNumber].faClass && fa.sloc == scope.jobModel.sloc.value){
                                                work.fixedAssetNumber = fa.faNumber;
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }

                    function uuidv4() {
                        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
                            return v.toString(16);
                        });
                    }

                    scope.jobDetailObject = '';
                    if (scope.jobModel.hasOwnProperty('jrNumber')){
                        if (scope.jobModel.jrNumber.hasOwnProperty('value')) {
                            if (scope.jobModel.jrNumber.value.hasOwnProperty('value')) {
                                scope.jobDetailObject = 'REVISION ' + scope.jobModel.jrNumber.value.value.toString()
                            } else {
                                scope.jobDetailObject = 'REVISION ' + scope.jobModel.jrNumber.value.toString()
                            }
                        } else {
                            scope.jobDetailObject = 'REVISION ' + scope.jobModel.jrNumber.toString()
                        }
                    }

                    // scope.jobDetailObject = 'REVISION ' + scope.jobModel.jrNumber.value;
                    exModal.open({
                        scope: {
                            jobModel: scope.jobModel,
                            // jobDetailObject: '',
                            getStatus: scope.getStatus,
                            jobDetailObject: scope.jobDetailObject,
                            showDiagram: scope.showDiagram,
                            //contractorsTitle: scope.contractors,
                            contractorsService: scope.contractorsService,
                            jobWorksValue: scope.jobWorksValue,
                            jobWorksValueTemp: scope.jobWorksValueTemp,
                            contractorsTitle: scope.contractorsTitle,
                            showHistory: scope.showHistory,
                            subcontructorDirectory: scope.subcontructorDirectory,
                            requestedObjectDate: new Date(scope.jobModel.requestedDate.value),
                            documentType: scope.documentType,
                            reasonsSubcontractorResponsible: scope.jobModel.reasonsSubcontractorResponsible,
                            workDefinitionMap: scope.workDefinitionMap,
                            tnuSiteLocations: scope.tnuSiteLocations,
                            // showHistory: scope.showHistory,
                            // showHistory: scope.showHistory,
                            // showHistory: scope.showHistory,
                            // showHistory: scope.showHistory,
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
                        templateUrl: './js/partials/createPrProcessCardModal.html',
                        size: ('hg')
                    }).then(function (results) {
                        // console.log(scope)
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
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalDismantle(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
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
                                            // console.log('!!!!!');

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
                            showDiagram:scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails:scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.dismantleCatalogs,
                            download: function(path) {
                                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
                                then(function(response) {
                                    document.getElementById('fileDownloadIframe').src = response.data;
                                }, function(error){
                                    console.log(error);
                                });
                            }
                        },
                        templateUrl: './js/partials/dismantleCardModal.html',
                        size: 'lg'
                    }).then(function(results){
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
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalReplacement(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalReplacement(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
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
                scope.toggleProcessViewTsd = function (index, processInstanceId, businessKey, startDate, processDefinitionKey, userId, processDefinitionId) {
                    var task = []
                    $http({
                        method: 'GET',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        url: baseUrl + '/task?processInstanceId=' + processInstanceId,
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
                                                url: baseUrl + '/task/' + e.processInstanceId
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
                                    $http({
                                        method: 'GET',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        url: baseUrl + '/history/user-operation?operationType=Claim&taskId=' + e.id
                                    }).then(
                                        function (taskLog) {
                                            console.log(taskLog);
                                            if(taskLog.data.length > 0) {
                                                e.claimDate = taskLog.data[0].timestamp;
                                            }
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );
                                    $http({
                                        method: 'GET',
                                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                                        url: baseUrl + '/history/user-operation?operationType=Assign&taskId=' + e.id
                                    }).then(
                                        function (taskLog) {
                                            console.log(taskLog);
                                            if(taskLog.data.length > 0) {
                                                e.assigneeDate = taskLog.data[0].timestamp;
                                            }
                                        },
                                        function (error) {
                                            console.log(error.data);
                                        }
                                    );
                                });
                                task = processInstanceTasks;
                            } else {
                                var activities = [];
                                $http.get(baseUrl + '/process-instance/' + processInstanceId + '/activity-instances').then(
                                    function (result) {
                                        _.forEach(result.data.childActivityInstances, function (firstLevel) {
                                            if (firstLevel.activityType === 'subProcess') {
                                                _.forEach(firstLevel.childActivityInstances, function (secondLevel) {
                                                    if (secondLevel.activityType !== 'userTask' && secondLevel.activityType !== 'multiInstanceBody') {
                                                        activities.push(secondLevel);
                                                    }
                                                });
                                            } else if (firstLevel.activityType !== 'userTask' && firstLevel.activityType !== 'multiInstanceBody') {
                                                activities.push(firstLevel);
                                            }
                                        });
                                    },
                                    function (error) {
                                        console.log(error.data);
                                    }
                                );
                                task = activities;
                            }
                            $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                                processDefinitionKey: processDefinitionKey,
                                processInstanceId: processInstanceId,
                            }).then(function (result) {
                                let vars = {};
                                result.data.forEach(function (v) {
                                    vars[v.name] = v.value;
                                });
                                if (processDefinitionKey === 'create-new-tsd') {
                                    openCreateTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task)
                                } else {
                                    openTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task)
                                }
                            },
                            function (error) {
                                console.log(error.data);
                            });
                    });
                };
                function openProcessCardModalReplacement(processDefinitionId, businessKey, index) {
                    exModal.open({
                        scope: {
                            replacementInfo: scope.replacementInfo,
                            showDiagram:scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails:scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.dismantleCatalogs,
                            download: function(path) {
                                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
                                then(function(response) {
                                    document.getElementById('fileDownloadIframe').src = response.data;
                                }, function(error){
                                    console.log(error);
                                });
                            }
                        },
                        templateUrl: './js/partials/replacementCardModal.html',
                        size: 'lg'
                    }).then(function(results){
                    });
                }
                scope.putCurrentUserAsRequestor = function(){
                    if(scope.filter.participation === 'iaminitiator' || scope.filter.participation === 'iamparticipant'){
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
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalLeasing(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
                                                    } else {
                                                        console.log(groupasynCalls, maxGroupAsynCalls);

                                                    }
                                                } else {
                                                    groupasynCalls+=1;
                                                    if (groupasynCalls === maxGroupAsynCalls) {
                                                        asynCall1 = true;
                                                        if (asynCall1 && asynCall2) {
                                                            openProcessCardModalLeasing(processDefinitionId, businessKey, index);
                                                            asynCall1 = false;
                                                        }  else console.log('asynCall 2 problem');
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
                                                } catch(e) {
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
                async function openProcessCardModalLeasing(processDefinitionId, businessKey, index) {
                    console.log(scope.leasingInfo);
                    scope.leasingInfo.computedAntennaName = ''
                    scope.leasingInfo.computedDimensions = ''
                    scope.leasingInfo.computedWeight = ''
                    scope.leasingInfo.computedQuantity = ''
                    scope.leasingInfo.computedLocations = ''
                    scope.leasingInfo.computedAzimuth = ''
                    scope.leasingInfo.computedSuspension = ''
                    scope.leasingInfo.computedRadioUnits = ''
                    scope.leasingInfo.computedCableLaying = ''
                    scope.leasingInfo.computedDuTypes = ''
                    scope.leasingInfo.computedLegalType = ''
                    if (scope.leasingInfo.renterCompany) {
                        if (scope.leasingInfo.renterCompany.value.legalTypeCatalogId) {
                            const response = await $http.post('/camunda/catalogs/api/get/rolloutcatalogids', [22])
                            scope.leasingInfo.computedLegalType = response.data.legal_type.find(el => el.catalogsId == scope.leasingInfo.renterCompany.value.legalTypeCatalogId).name
                        } else {
                                // to old processes, old dict
                            scope.leasingInfo.computedLegalType = scope.getLegalTypeDesc(scope.leasingInfo.renterCompany.value.legalType)
                        }
                    }

                    if(scope.leasingInfo.cellAntenna && scope.leasingInfo.cellAntenna.type === 'Json') {
                        if(scope.leasingInfo.cellAntenna.value.sectors.length) {
                            scope.leasingInfo.computedAntennaName = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                i.antennas.forEach(j => {
                                    arr.push(j.antennaName)
                                })
                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedDimensions = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                i.antennas.forEach(j => {
                                    arr.push(j.dimension)
                                })
                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedWeight = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                i.antennas.forEach(j => {
                                    arr.push(j.weight)
                                })
                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedQuantity = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = 0;
                                i.antennas.forEach(j => {
                                    arr += +j.quantity;
                                })
                                return arr
                            }).flat(Infinity).join(', ')
                            scope.leasingInfo.computedLocations = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                i.antennas.forEach(j => {
                                    arr.push(j.cn_antenna_loc)
                                })
                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedAntennaTypes = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                i.antennas.forEach(j => {
                                    for(let [key, value] of Object.entries(j.antennaType)) {
                                        if (value) {
                                            arr.push(key)
                                        }
                                    }
                                })
                                return arr
                            }).flat(Infinity).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedAzimuth = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                if(i.antennas.length > 0) {
                                    arr.push(i.antennas[0].azimuth)
                                }

                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedSuspension = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                if(i.antennas.length > 0) {
                                    arr.push(i.antennas[0].suspensionHeight)
                                }

                                return arr
                            }).flat(Infinity).join('/ ')
                            scope.leasingInfo.computedRadioUnits = scope.leasingInfo.cellAntenna.value.sectors.map(i => {
                                let arr = [];
                                for(let [key, value] of Object.entries(i.bands)) {
                                    console.log(value.hasOwnProperty('cn_radio_unit'))
                                    if (value.active && value.hasOwnProperty('cn_radio_unit')) {
                                        arr.push(value.cn_radio_unit.name)
                                    }
                                }

                                return arr
                            }).flat(Infinity).join('/ ')
                        }

                        if (scope.leasingInfo.cellAntenna.value.cn_du) {
                            scope.leasingInfo.computedDuTypes = scope.leasingInfo.cellAntenna.value.cn_du.map(i => i.name).join('/ ')
                        }
                    }

                    if(scope.leasingInfo.powerSource && scope.leasingInfo.powerSource.type === 'Json') {
                        if (scope.leasingInfo.powerSource.value.cableLayingType && Array.isArray(scope.leasingInfo.powerSource.value.cableLayingType)) {
                            scope.leasingInfo.computedCableLaying = scope.leasingInfo.powerSource.value.cableLayingType.map(i => {
                                let arr = [];
                                if (i.value) {
                                    arr.push(i.title)
                                }

                                return arr
                            }).flat(Infinity).join('/ ')
                        }

                    }
                    exModal.open({
                        scope: {
                            leasingInfo: scope.leasingInfo,
                            showDiagram:scope.showDiagram,
                            showHistory: scope.showHistory,
                            hasGroup: scope.hasGroup,
                            showGroupDetails:scope.showGroupDetails,
                            processDefinitionId: processDefinitionId,
                            piIndex: scope.piIndex,
                            $index: index,
                            businessKey: businessKey,
                            catalogs: scope.leasingCatalogs,
                            tabs: {
                                candidate: true,
                                transmission: false,
                                radio: false,
                                power: false,
                            },
                            download: function(path) {
                                $http({method: 'GET', url: '/camunda/uploads/get/' + path, transformResponse: [] }).
                                then(function(response) {
                                    document.getElementById('fileDownloadIframe').src = response.data;
                                }, function(error){
                                    console.log(error);
                                });
                            },
                            addressToString: function (address) {
                                let string = [];
                                let array = [
                                    'cn_addr_oblast',
                                    'cn_addr_district',
                                    'cn_addr_city',
                                    'cn_addr_street',
                                    'cn_addr_building',
                                    'cn_addr_cadastral_number',
                                    'cn_addr_note',
                                ]
                                if (address) {
                                    array.forEach(i => {
                                        for(let [key, value] of Object.entries(address)) {
                                            if (key === i && value) {
                                                string.push(value)
                                            }
                                        }
                                    })
                                }
                                return string.join(', ');
                            }
                        },
                        templateUrl: './js/partials/leasingCardModal.html',
                        size: 'hg'
                    }).then(function(results){
                    });
                }
                scope.setCustomField = function(){
                    scope.isCustomField = !scope.isCustomField;
                }
                scope.customFields = [
                    {name: "Region", id: "region", selected: false, order: 1},
                    {name: "Sitename", id: "sitename", selected: false, order: 2},
                    {name: "JR Number", id: "jrNumber", selected: false, order:3},
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

                scope.selectCustomField = function(){
                    var tmp= [];
                    angular.forEach(scope.customFields, function(field){
                        if(field.selected){
                            field.selected = false;
                            scope.selectedCustomFields.push(field);
                        } else {
                            tmp.push(field);
                        }
                    });
                    scope.customFields = tmp;
                }

                scope.unSelectCustomField = function(){
                    var tmp= [];
                    angular.forEach(scope.selectedCustomFields, function(field){
                        if(field.selected){
                            field.selected = false;
                            scope.customFields.push(field);
                        } else {
                            tmp.push(field);
                        }
                    });
                    scope.selectedCustomFields = tmp;
                }

                scope.fillEmptyLines = function(length){
                    return new Array(13-length);
                }
                scope.populateSelectFromCatalogId = function (container, id) {
                    $http.get('/camunda/catalogs/api/get/id/' + id).then(function (result) {
                        if (result && result.data) {
                            scope[container] = _.groupBy(result.data.data.$list, 'id');
                        }
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


                scope.showProcessInfo = function(index, processInstanceId, businessKey, startDate, processDefinitionKey, userId, processDefinitionId) {
                    var task = []
                    $http({
                        method: 'GET',
                        headers: {'Accept': 'application/hal+json, application/json; q=0.5'},
                        url: baseUrl + '/task?processInstanceId=' + processInstanceId,
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
                                                url: baseUrl + '/task/' + e.processInstanceId
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
                                task = processInstanceTasks;
                            }
                            $http.post(baseUrl + '/history/variable-instance?deserializeValues=false', {
                                processDefinitionKey: processDefinitionKey,
                                processInstanceId: processInstanceId,
                            }).then(function (result) {
                                    let vars = {};
                                    result.data.forEach(function (v) {
                                        vars[v.name] = v.value;
                                    });
                                    if (processDefinitionKey === 'create-new-tsd') {
                                        openCreateTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task)
                                    } else {
                                        openTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task)
                                    }
                                },
                                function (error) {
                                    console.log(error.data);
                                });
                        })
                }
                function openTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task) {
                    // var oldTsd = JSON.parse(vars.oldTsd);
                    var selectedTsd = JSON.parse(vars.selectedTsd);
                    var resolutions = JSON.parse(vars.resolutions);
                    var eLicenseNumber = vars.eLicenseNumber;
                    var eLicenseDate = vars.eLicenseDate;
                    var rfsPermitionNumber = vars.rfsPermitionNumber;
                    var rfsPermitionDate = vars.rfsPermitionDate;
                    exModal.open({
                        scope: {
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            profiles: scope.profiles,
                            resolutions,
                            task,
                            processDefinitionId,
                            selectedTsd,
                            businessKey,
                            startDate,
                            userId,
                            eLicenseNumber,
                            eLicenseDate,
                            rfsPermitionNumber,
                            rfsPermitionDate
                        },
                        templateUrl: './js/partials/tsdProcessCardModal.html',
                        size: 'hg'
                    }).then(function (results) {
                    });
                }

                function openCreateTsdProcessCardModal(businessKey, startDate, userId, vars, processDefinitionId, task) {
                    var resolutions = JSON.parse(vars.resolutions);
                    var eLicenseNumber = vars.eLicenseNumber;
                    var eLicenseDate = vars.eLicenseDate;
                    var rfsPermitionNumber = vars.rfsPermitionNumber;
                    var rfsPermitionDate = vars.rfsPermitionDate;
                    exModal.open({
                        scope: {
                            showDiagram: scope.showDiagram,
                            showHistory: scope.showHistory,
                            profiles: scope.profiles,
                            construction_type_id: scope.construction_type_id,
                            antenna_diameter_id: scope.antenna_diameter_id,
                            protection_mode_id: scope.protection_mode_id,
                            capacity: scope.capacity,
                            rau_subband: scope.rau_subband,
                            link_type_id: scope.link_type_id,
                            polarization_id: scope.polarization_id,
                            region: scope.region,
                            resolutions,
                            task,
                            processDefinitionId,
                            vars,
                            businessKey,
                            startDate,
                            userId,
                            eLicenseNumber,
                            eLicenseDate,
                            rfsPermitionNumber,
                            rfsPermitionDate
                        },
                        templateUrl: './js/partials/tsdCreateProcessCardModal.html',
                        size: 'hg'
                    }).then(function (results) {
                    });
                }

            },
            templateUrl: './js/directives/search/networkArchitectureSearch.html'
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
