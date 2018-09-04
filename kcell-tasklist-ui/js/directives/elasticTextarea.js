define(['./module'], function(module){
	'use strict';
	module.directive('elasticTextarea', ['$timeout', function($timeout) {
        return {
            restrict: 'A',
			require: 'ngModel',
            link: function(scope, el) {
                el[0].setAttribute('style', 'height:' + (el[0].scrollHeight) + 'px;overflow-y:hidden;');

                scope.$watch(function() {return el[0].value;}, function(value) {
                    el[0].style.height = 'auto';
                    el[0].style.height = (el[0].scrollHeight) + 'px';
                }, true);
            }
        };
    }]);
});