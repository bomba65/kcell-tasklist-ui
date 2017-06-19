require.config({
     //  псевдонимы и пути используемых библиотек и плагинов
     map: {
     		'lodash/*': './node_modules/lodash/index'
     	}
     ,
     paths: {
         'domReady': './node_modules/requirejs-domready/domReady',
         'angular': './node_modules/angular/angular',
         'jquery': './node_modules/jquery/dist/jquery',
         'bootstrap': './node_modules/dist/js/bootstrap',
         'ngRoute': './node_modules/angular-route/angular-route',
         'ngCookies': './node_modules/angular-cookies/angular-cookies',
         'simpleApp': './simpleApp',
         'toasty' : './node_modules/angular-toasty/dist/angular-toasty',
         'translate' : './node_modules/angular-translate/dist/angular-translate',
         'deep-diff' : './node_modules/deep-diff/index',
         'angular-ui-bootstrap' : './node_modules/angular-ui-bootstrap/ui-bootstrap-tpls',
         'angular-translate-storage-local' : './node_modules/angular-translate-storage-local/angular-translate-storage-local.min',
         'angular-translate-storage-cookie' : './node_modules/angular-translate-storage-cookie/angular-translate-storage-cookie.min',
         'camundaSDK': './camunda/camunda-bpm-sdk-angular',
         'lodash': './lodash.min',
         'bpmn-viewer': './camunda/bpmn-viewer',
         'bpmn-navigated-viewer': './camunda/bpmn-navigated-viewer'
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
         'angular-ui-bootstrap': ['angular'],
         'angular-translate-storage-local': ['translate'],
         'angular-translate-storage-cookie': ['translate']
     },
 
     // запустить приложение
     deps: ['./bootstrapAngular'],
     urlArgs: "bust=" + (new Date()).getTime()
});