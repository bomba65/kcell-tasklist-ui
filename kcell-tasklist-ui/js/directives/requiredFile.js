define(['./module'], function(module){
	'use strict';
	module.directive('requiredFile',function(){
		return {
			require:'ngModel',
			restrict: 'A',
			link: function(scope,el,attrs,ctrl){
				function updateValidity(fileRequired){
					var valid = (fileRequired && el.val() !== '') || (!fileRequired);
					ctrl.$setValidity('validFile', valid);
					ctrl.$setValidity('camVariableType', valid);
				}
				updateValidity(scope.$eval(attrs.requiredFile));
				scope.$watch(attrs.requiredFile, updateValidity);
				el.bind('change',function(){
					updateValidity(scope.$eval(attrs.requiredFile));
					scope.$apply(function(){
						ctrl.$setViewValue(el.val());
						ctrl.$render();
					});
				});
			}
		}
	});
});