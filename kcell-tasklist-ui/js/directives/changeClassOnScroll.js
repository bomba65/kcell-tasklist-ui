define(['./module'], function(module){
	'use strict';
	module.directive('changeClassOnScroll', function ($window) {
		return {
			restrict: 'A',
			scope: {
				scrollElementId: "@",
				addClass: "@",
				scrollClass: "@"
			},
			link: function(scope, element, attrs) {
				scope.sticky = document.getElementById(scope.addClass).offsetTop;
				try {
					angular.element(document.querySelector("#" + scope.scrollElementId)).bind("scroll", function() {
						var offset = document.querySelector("#" + scope.scrollElementId).scrollTop;

						if (offset > scope.sticky) {
							element.addClass(scope.scrollClass);
							angular.element(document.querySelector("#sticky-header")).addClass('sticky-header');
							angular.element(document.querySelector("#taskElement")).addClass('taskElement');
						} else {
							element.removeClass(scope.scrollClass);
							angular.element(document.querySelector("#sticky-header")).removeClass('sticky-header');
							angular.element(document.querySelector("#taskElement")).removeClass('taskElement');
						}
					});
				} catch (e){
					console.log(e);
				}
			}
		};
	});
});