
// Opera 8.0+
var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;

// Chrome 1+
var isChrome = !!window.chrome; //&& !!window.chrome.webstore;

// Blink engine detection
var isBlink = (isChrome || isOpera) && !!window.CSS;
function showUseChrome(){
	var x = document.getElementById("showUseChrome");
	if(x){
		x.style.display = "block";
	} else {
		alert("Please use Chrome Browser");
	}
}
if(isBlink){
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
} else {
	showUseChrome();
}
