define(['./module', 'camundaSDK', 'html2canvas', 'pdfMake'], function (module, CamSDK, html2canvas, pdfMake) {
    'use strict';
    return module.service('AuthenticationService', ['$rootScope', '$q', '$http', 'Uri', function ($rootScope, $q, $http, Uri) {
        function emit(event, a, b) {
            $rootScope.$broadcast(event, a, b);
        }

        function parse(response) {
            if (response.status !== 200) {
                return $q.reject(response);
            }
            var Authentication = function (data) {
                angular.extend(this, data);
            }
            var data = response.data;
            return new Authentication({
                name: data.userId,
                authorizedApps: data.authorizedApps
            });
        }

        function update(authentication) {
            $rootScope.authentication = authentication;
            if (authentication === null) {
                $rootScope.authUser = authentication;
            }
            emit('authentication.changed', authentication);
        }

        this.updateAuthentication = update;
        this.login = function (username, password) {
            var form = $.param({
                username: username,
                password: password
            });

            function success(authentication) {
                update(authentication);
                emit('authentication.login.success', authentication);
                return authentication;
            }

            function error(response) {
                emit('authentication.login.failure', response);
                return $q.reject(response);
            }

            return $http({
                method: 'POST',
                url: Uri.appUri('admin://auth/user/:engine/login/:appName'),
                data: form,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            }).then(parse).then(success, error);
        };
        this.logout = function () {
            function success(response) {
                update(null);
                emit('authentication.logout.success', response);
            }

            function error(response) {
                emit('authentication.logout.failure', response);
                return $q.reject(response);
            }

            return $http.post(Uri.appUri('admin://auth/user/:engine/logout')).then(success, error);
        };
        var authenticationPromise;
        $rootScope.$on('authentication.changed', function (e, authentication) {
            authenticationPromise = $q[authentication ? 'when' : 'reject'](authentication);
        });
        this.getAuthentication = function () {
            function success(authentication) {
                update(authentication);
                return authentication;
            }

            if (!authenticationPromise) {
                if ($rootScope.authentication) {
                    authenticationPromise = $q.when($rootScope.authentication);
                } else {
                    authenticationPromise = $http.get(Uri.appUri('admin://auth/user/:engine')).then(parse).then(success);
                }
            }
            return authenticationPromise;
        };
        $rootScope.$on('$routeChangeStart', function (event, next) {
            if (next.authentication) {
                if (!next.resolve) {
                    next.resolve = {};
                }
                if (!next.resolve.authentication) {
                    next.resolve.authentication = ['AuthenticationService', function (AuthenticationService) {
                        return AuthenticationService.getAuthentication().catch(function (response) {
                            if (next.authentication === 'optional') {
                                return null;
                            } else {
                                emit('authentication.login.required', next);
                                return $q.reject(response);
                            }
                        });
                    }];
                }
            }
        });
    }]).service('translateWithDefault', ['$translate', '$q', function ($translate, $q) {
        return function (translationObject) {
            var promises = Object.keys(translationObject).reduce(function (promises, key) {
                promises[key] = translateKey(key);
                return promises;
            }, {});
            return $q.all(promises);

            function translateKey(key) {
                return $translate(key).catch(function () {
                    return translationObject[key];
                });
            }
        };
    }]).service('debounce', ['$timeout', function ($timeout) {
        return function debounce(fn, wait) {
            var timer;
            var debounced = function () {
                var context = this, args = arguments;
                debounced.$loading = true;
                if (timer) {
                    $timeout.cancel(timer);
                }
                timer = $timeout(function () {
                    timer = null;
                    debounced.$loading = false;
                    fn.apply(context, args);
                }, wait);
            };
            return debounced;
        };
    }]).service('StartProcessService', ['$rootScope', 'toasty', '$timeout', '$location', 'exModal', '$http', '$window', function ($rootScope, toasty, $timeout, $location, exModal, $http, $window) {
        var camClient = new CamSDK.Client({
            mock: false,
            apiUri: '/camunda/api/engine/'
        });
        var baseUrl = '/camunda/api/engine/engine/default';
        return function (id) {
            $http.get(baseUrl + '/process-definition/' + id + '/startForm').then(
                function (startFormInfo) {
                    if (startFormInfo.data.key) {
                        var url = startFormInfo.data.key.replace('embedded:app:', startFormInfo.data.contextPath + '/');
                        exModal.open({
                            scope: {
                                processDefinitionId: id,
                                url: url,
                                view: {
                                    submitted: false
                                }
                            },
                            templateUrl: './js/partials/start-form.html',
                            controller: StartFormController,
                            size: 'lg'
                        }).then(function (results) {
                            $http.get(baseUrl + '/task?processInstanceId=' + results.id).then(
                                function (tasks) {
                                    var task = null;
                                    if (tasks.data.length > 0) {
                                        task = tasks.data[0];
                                    } else {
                                        task = results.data
                                    }

                                    if (task.assignee === $rootScope.authUser.id) {
                                        $rootScope.tryToOpen = task;
                                        if (id.indexOf('VPN_Port_manual') !== -1) {
                                            $window.open('/kcell-tasklist-ui/#/tasks/' + task.id, '_self');
                                        }
                                    }
                                    $rootScope.$broadcast('getTaskListEvent');
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        });
                    } else {
                        $http.post(baseUrl + '/process-definition/' + id + '/start', {}, {headers: {'Content-Type': 'application/json'}}).then(
                            function (results) {
                                $http.get(baseUrl + '/task?processInstanceId=' + results.id).then(
                                    function (tasks) {
                                        if (tasks.data.length > 0) {
                                            $rootScope.tryToOpen = tasks.data[0];
                                        } else {
                                            $rootScope.tryToOpen = results.data
                                        }
                                        $rootScope.$broadcast('getTaskListEvent');
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
                },
                function (error) {
                    console.log(error.data);
                }
            );

            StartFormController.$inject = ['scope'];

            function StartFormController(scope) {
                $timeout(function () {
                    new CamSDK.Form({
                        client: camClient,
                        formUrl: scope.url,
                        processDefinitionId: scope.processDefinitionId,
                        containerElement: $('#start-form-modal-body'),
                        done: scope.addStartFormButtion
                    });
                })
                scope.addStartFormButtion = function (err, camForm, evt) {
                    if (err) {
                        throw err;
                    }
                    var $submitBtn = $('<button type="submit" class="btn btn-primary">Start</button>').click(function (e) {
                        scope.view = {
                            submitted: true
                        };
                        $timeout(function () {
                            scope.$apply(function () {
                                if (scope.kcell_form.$valid) {
                                    $submitBtn.attr('disabled', true);
                                    if (scope.preSubmit) {
                                        scope.preSubmit().then(
                                            function (result) {
                                                ///////////////////
                                                camForm.submit(function (err, results) {
                                                    if (err) {
                                                        $submitBtn.removeAttr('disabled');
                                                        toasty.error({title: "Error", msg: err});
                                                        e.preventDefault();
                                                        throw err;
                                                    } else {
                                                        console.log('SEND 107');
                                                        toasty.success({
                                                            title: "Info",
                                                            msg: " Your form has been successfully processed"
                                                        });
                                                        $('#start-form-modal-body').html('');
                                                        scope.$close(results);

                                                    }
                                                });
                                                /*
                                                try {
                                                    camForm.submit(function (err) {
                                                        if (err) {
                                                            console.log('+++++++++++++++++++++++++++++++++');
                                                            $submitBtn.removeAttr('disabled');
                                                            toasty.error({title: "Could not complete task", msg: err});
                                                            e.preventDefault();
                                                            throw err;
                                                         } else {
                                                            console.log('---------------------------------');
                                                            scope.preSubmit = undefined;
                                                            $('#taskElement').html('');
                                                            scope.currentTask = undefined;
                                                            scope.$parent.getTaskList();
                                                            $location.search({});
                                                            scope.submitted = false;
                                                         }
                                                    });
                                                } catch(e){
                                                    console.log(e);
                                                }
                                                */
                                                ///////////////////
                                            },
                                            function (err) {
                                                $submitBtn.removeAttr('disabled');
                                                toasty.error({title: "Error", msg: err});
                                                e.preventDefault();
                                                throw err;
                                            }
                                        );
                                    } else {
                                        camForm.submit(function (err, results) {
                                            if (err) {
                                                $submitBtn.removeAttr('disabled');
                                                toasty.error({title: "Error", msg: err});
                                                e.preventDefault();
                                                throw err;
                                            } else {
                                                console.log('SEND 154');
                                                toasty.success({
                                                    title: "Info",
                                                    msg: " Your form has been successfully processed"
                                                });
                                                $('#start-form-modal-body').html('');
                                                scope.$close(results);
                                            }
                                        });
                                    }
                                } else {
                                    console.log(scope.kcell_form);
                                    toasty.error({title: "Error", msg: "Please fill required fields"});
                                }
                            })
                        });
                    });
                    $("#modal-footer").append($submitBtn);
                }
            }
        }
    }]).service('SearchCurrentSelectedProcessService', ['$rootScope', function ($rootScope) {
        return function (currentProcessInstance) {
            if (currentProcessInstance && Object.keys(currentProcessInstance).length > 0) {
                $rootScope.currentProcessInstance = currentProcessInstance;
            }
            return $rootScope.currentProcessInstance;
        };
    }]).service('disconnectSelectedProcessService', ['$rootScope', function ($rootScope) {
        return function (disconnect) {
            if (typeof disconnect !== "undefined") {
                $rootScope.disconnect = disconnect;
            }
            return $rootScope.disconnect;
        };
    }]).service('htmlToPdf', ['$http', function ($http) {
        return function (domElement, options) {
            return new Promise(function (resolve, reject) {

                if (!domElement) {
                    reject("No dom element provided");
                    return;
                }

                options = options || {};

                var PAGE_HEIGHT = 815;
                var PAGE_WIDTH = 580;

                html2canvas(domElement).then(function (canvas) {
                    var content = [];
                    var fullHeight = canvas.height;

                    var canvass = document.createElement('canvas');
                    canvass.width = canvas.width;
                    canvass.height = canvas.height;
                    var context = canvass.getContext('2d');

                    if (canvas.width > PAGE_WIDTH) {
                        var ratio = canvas.height / canvas.width;
                        fullHeight = canvas.height - ((canvas.width - PAGE_WIDTH) * ratio);
                    }

                    var totalPages = Math.ceil(fullHeight / PAGE_HEIGHT);
                    var height = canvas.height;
                    if (fullHeight > PAGE_HEIGHT) {
                        height = height / (fullHeight / PAGE_HEIGHT);
                    }

                    for (var page = 0; page < totalPages; page++) {
                        context.clearRect(0, 0, canvass.width, canvass.height);
                        context.drawImage(canvas, 0, page * height, canvas.width, height, 0, 0, canvas.width, height);

                        content.push({
                            image: canvass.toDataURL(),
                            width: PAGE_WIDTH
                        });
                    }
                    var docDefinition = {
                        pageSize: 'A4',
                        pageMargins: [10, 10],
                        content: content
                    };
                    var fileName = 'generated_file';
                    if (options.fileName) fileName = options.fileName;
                    fileName += '.pdf';

                    if (options.download) {
                        pdfMake.createPdf(docDefinition).download(fileName);
                    }

                    if (options.upload) {
                        pdfMake.createPdf(docDefinition).getBlob(function (blob) {

                            var uploadPath = 'generatedPDFs/' + fileName;
                            if (options.uploadPath) uploadPath = options.uploadPath;

                            var userName = 'htmlToPdfService';
                            if (options.userName) userName = options.userName;

                            var fileToUpload = {
                                name: fileName,
                                path: uploadPath,
                                created: new Date(),
                                createdBy: userName,
                            };
                            $http({
                                method: 'GET',
                                url: '/camunda/uploads/put/' + fileToUpload.path,
                                transformResponse: []
                            }).then(function (response) {
                                $http.put(response.data, blob, {headers: {'Content-Type': undefined}}).then(function () {
                                    resolve();
                                }).catch(function (error) {
                                    reject(error);
                                });
                            }).catch(function (error) {
                                reject(error);
                            });

                            resolve();
                        });
                    } else resolve();
                });
            });
        };
    }]).service('historyVariablesManager', ['$http', function ($http) {
        return function (camForm, taskDefKey) {
            function getAttachmentsChanges(lastAttachments, attachments) {
                var attachmentsChanges = {added: [], removed: []};
                var el;
                for (var i = 0; i < attachments.length; i++) {
                    el = lastAttachments.find(function (e) {
                        return e.path === attachments[i].path;
                    });
                    if (!el) attachmentsChanges.added.push(attachments[i]);
                }
                for (var i = 0; i < lastAttachments.length; i++) {
                    el = attachments.find(function (e) {
                        return e.path === lastAttachments[i].path;
                    });
                    if (!el) attachmentsChanges.removed.push(lastAttachments[i]);
                }
                return attachmentsChanges;
            }

            return {
                getAttachmentsChanges: getAttachmentsChanges,
                createVariables: function (hasAttachments) {
                    camForm.variableManager.createVariable({
                        name: taskDefKey + 'TaskResult',
                        type: 'String',
                        value: ''
                    });
                    camForm.variableManager.createVariable({
                        name: taskDefKey + 'TaskComment',
                        type: 'String',
                        value: ''
                    });
                    if (hasAttachments) {
                        camForm.variableManager.createVariable({
                            name: taskDefKey + 'TaskAttachments',
                            type: 'Json',
                            value: {}
                        });
                    }
                },
                setVariables: function (resolution, comment, lastAttachments, attachments) {
                    camForm.variableManager.variableValue(taskDefKey + 'TaskResult', resolution);
                    camForm.variableManager.variableValue(taskDefKey + 'TaskComment', comment);
                    if (lastAttachments && attachments) {
                        var attachmentsChanges = getAttachmentsChanges(lastAttachments, attachments);
                        camForm.variableManager.variableValue(taskDefKey + 'TaskAttachments', attachmentsChanges);
                    }
                }
            };
        };
    }]);
});