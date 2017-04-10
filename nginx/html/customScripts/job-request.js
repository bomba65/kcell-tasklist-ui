'use strict';
define('kcell-custom-module', ['angular'], function (angular) {
  var customModule = angular.module('kcell.custom.module', []);

  customModule
  .directive('jobRequest', function () {
    return {
      restrict: 'E',
      scope: {
        jobModel: '='
      },
      templateUrl: '/customScripts/jobRequest.html'
    };
  })
  .directive('trackChange', function(){
  	console.log("hello");
    return {
      restrict: 'A',
      priority: 10000,
	  link: function(scope, element, attrs, ctrl) {
      	var paths = scope.myModelExpression
        	.split('.')
          .reduce((a, b) => [...a, [...a[a.length - 1], b]], [[]])
          .map(e => e.join('.'))
          .reverse()
          .filter((e, i) => i && e.length);
        
      	element.on('blur keyup change', function(){
      		console.log(scope);
        	var [originalPath, original] = paths
          	.map(e => [e, scope.$parent.$eval(e + "._original") || {}])
            .find(e => e[1]);
          if (original) {
          	var fieldPath = scope.myModelExpression
            	.slice(originalPath.length + 1);
            var originalValue = scope.$parent.$eval(fieldPath, original);
            console.log(originalValue, scope.myModelValue);
            if (originalValue === scope.myModelValue) {
            	console.log("Equal");
            	if(element.attr('type') === 'checkbox'){
            		element.parent().removeClass('track-change-unequal');
            	} else {
            		element.removeClass("track-change-unequal");
            	}
            } else {
            	console.log("Unequal");

            	if(element.attr('type') === 'checkbox'){
            		element.parent().addClass('track-change-unequal');
            	} else {
            		element.addClass("track-change-unequal");
            	}
            }
          }
        	console.log();
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
  return customModule;
});