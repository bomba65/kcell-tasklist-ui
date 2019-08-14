require.config({
     //  псевдонимы и пути используемых библиотек и плагинов
     waitSeconds : 60,
     map: {
     		'lodash/*': './node_modules/lodash/index'
     	}
     ,
     paths: {
         'domReady': './node_modules/requirejs-domready/domReady',
         'angular': './node_modules/angular/angular.min',
         'jquery': './node_modules/jquery/dist/jquery.min',
         'bootstrap': './node_modules/bootstrap/dist/js/bootstrap.min',
         'ngRoute': './node_modules/angular-route/angular-route.min',
         'ngCookies': './node_modules/angular-cookies/angular-cookies.min',
         'simpleApp': './simpleApp',
         'toasty' : './node_modules/angular-toasty/dist/angular-toasty.min',
         'translate' : './node_modules/angular-translate/dist/angular-translate.min',
         'deep-diff' : './node_modules/deep-diff/index',
         'angular-ui-bootstrap' : './node_modules/angular-ui-bootstrap/ui-bootstrap-tpls.min',
         'angular-translate-storage-local' : './node_modules/angular-translate-storage-local/angular-translate-storage-local.min',
         'angular-translate-storage-cookie' : './node_modules/angular-translate-storage-cookie/angular-translate-storage-cookie.min',
         'camundaSDK': './camunda/camunda-bpm-sdk-angular',
         'lodash': './lodash.min',
         'bpmn-viewer': './camunda/bpmn-viewer.min',
         'bpmn-navigated-viewer': './camunda/bpmn-navigated-viewer.min',
         'big-js': './node_modules/big.js/big.min',
         'xlsx': './node_modules/xlsx/dist/xlsx.full.min',
         'angular-ui-router': './node_modules/@uirouter/angularjs/release/angular-ui-router.min',
         'ng-file-upload': './node_modules/ng-file-upload/dist/ng-file-upload-all.min',
         'angular-local-storage': './node_modules/angular-local-storage/dist/angular-local-storage.min',
         'angularjs-dropdown-multiselect': './node_modules/angularjs-dropdown-multiselect/dist/angularjs-dropdown-multiselect.min',
         'summernote': './summernote/summernote.min',
         'summernote-ext-template': './summernote/summernote-ext-template',
         'moment': './node_modules/moment/min/moment.min',
         'daterangepicker': './daterangepicker/daterangepicker',
         'bootstrap-select': './node_modules/bootstrap-select/dist/js/bootstrap-select.min',
         'angular-toarrayfilter': './node_modules/angular-toarrayfilter/toArrayFilter',
         'html2canvas': './node_modules/html2canvas/dist/html2canvas.min',
         'pdfMake': './node_modules/pdfmake/build/pdfmake.min'
     },

     // angular не поддерживает AMD из коробки, поэтому экспортируем перменную angular в глобальную область
     shim: {
         'angular': {
            exports: 'angular',
            deps: ['jquery']
         },
         'ngRoute': ['angular'],
         'ngCookies': ['angular'],
         'toasty' : ['angular'],
         'camundaSDK' : ['angular'],
         'translate' : ['angular'],
         'angular-local-storage': ['angular'],
         'angular-ui-router': ['angular'],
         'angular-ui-bootstrap': ['angular'],
         'ng-file-upload': ['angular'],
         'summernote': ['bootstrap'],
         'summernote-ext-template': ['summernote'],
         'angularjs-dropdown-multiselect': ['angular'],
         'angular-translate-storage-local': ['translate'],
         'angular-translate-storage-cookie': ['translate'],
         'bootstrap':['jquery'],
         'bootstrap-select': {
             exports: 'bootstrap-select',
             deps:['jquery', 'bootstrap']
         },
         'angular-toarrayfilter':['angular']
     },
 
     // запустить приложение
     deps: ['./bootstrapAngular'],
     urlArgs: "bust=" + Math.floor((new Date()).getTime()/(24*60*60*1000))
});
