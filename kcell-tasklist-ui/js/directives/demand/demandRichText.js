define(['./../module', 'summernote', 'summernote-ext-template'], function(module){
    'use strict';
    module.directive('demandRichText', function ($rootScope, $http, $timeout) {
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
            link: function(scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function(file, path, tmp) {
                    $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'put/' + path, transformResponse: [] }).then(function(response) {
                        $http.put(response.data, file, {headers: {'Content-Type': undefined}}).then(
                            function () {
                                $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'get/' + path, transformResponse: [] }).then(function(response) {
                                    var image = $('<img>').attr('src', response.data);
                                    element.summernote("insertNode", image[0]);
                                }, function(error){
                                    console.log(error.data);
                                });
                            },
                            function (error) {
                                console.log(error.data);
                            }
                        );
                    }, function(error){
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
                    minHeight: scope.disabled?30:(scope.minHeight?scope.minHeight:300),
                    callbacks: {
                        onChange: function(content) {
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
                        onMediaDelete: function(target) {
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
});