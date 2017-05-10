define(['./module'], function(module){
	'use strict';
	module.directive('trackChange', function(){
		return {
			restrict: 'A',
			priority: 10000,
			link: function(scope, element, attrs, ctrl) {
				var paths = scope.myModelExpression.split('.')
	          				.reduce((a, b) => [...a, [...a[a.length - 1], b]], [[]])
							.map(e => e.join('.'))
							.reverse()
							.filter((e, i) => i && e.length);

				element.on('blur keyup change', function(){
					var [originalPath, original] = paths
								.map(e => [e, scope.$parent.$eval(e + "._original") || {}])
								.find(e => e[1]);
					if (original) {
						var fieldPath = scope.myModelExpression.slice(originalPath.length + 1);
						var originalValue = scope.$parent.$eval(fieldPath, original);
						if (originalValue === scope.myModelValue) {
							if(element.attr('type') === 'checkbox'){
								element.parent().removeClass('track-change-unequal');
							} else {
								element.removeClass("track-change-unequal");
							}
						} else {
							if(element.attr('type') === 'checkbox'){
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
});