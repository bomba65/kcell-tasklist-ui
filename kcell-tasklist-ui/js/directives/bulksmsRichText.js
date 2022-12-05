define(['./module', 'summernote', 'summernote-ext-template'], function(module){
    'use strict';
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
            link: function(scope, element, attrs) {
                var uuid = new Date().getTime();
                var uploadImage = function(file, path, tmp) {
                    $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'put/' + path, transformResponse: [] }).then(
                        function(data, status, headers, config) {
                            $http.put(data, file, {headers: {'Content-Type': undefined}}).then(
                                function () {
                                    $http({method: 'GET', url: '/camunda/uploads/' + tmp + 'get/' + path, transformResponse: [] }).then(
                                        function(data, status, headers, config) {
                                            var image = $('<img>').attr('src', data);
                                            element.summernote("insertNode", image[0]);
                                        },
                                        function(data, status, headers, config) { console.log(data); }
                                    );
                                },
                                function (error) {
                                    console.log(error.data);
                                }
                            );
                        },
                        function(data, status, headers, config) { console.log(data);}
                    );
                };

                element.summernote({
                    focus: true,
                    disableResizeEditor: true,
                    minHeight: (scope.minHeight?scope.minHeight:300),
                    callbacks: {
                        onChange: function(content) {
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
                        },
                        onPaste: function(event) {
                            // Workaround for copying tables from Microsoft Excel
                            $timeout(function() {
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
});