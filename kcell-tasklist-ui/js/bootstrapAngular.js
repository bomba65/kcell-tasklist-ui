define([
	'require',
	'angular',
	'app',
	'ngRoute',
	'ngCookies'
], function(require, ng){
	'use strict';
	require(['domReady!'], function(document){
		ng.bootstrap(document, ['app']);
	});
});