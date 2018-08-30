define(['./module', 'simditor'], function(module){
	'use strict';
	module.directive('richText', ['$http', '$timeout', function ($http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				editorid: '=',
				htmlcode: '=',
				disabled: '=',
				onvalchange: '='
			},
			link: function(scope, el, attrs) {
				Simditor.locale = 'en-US';
				var editor;
				if (!scope.editorid || !scope.editorid.length) scope.editorid = 'simditor';

				if (!scope.disabled) {
					$timeout(function () {
						editor = new Simditor({
							textarea: $('#'+scope.editorid),
							toolbar: ['title', 'bold', 'italic', 'underline', 'strikethrough', 'fontScale', 'color', '|', 'ol', 'ul', 'blockquote', 'code', 'table', '|', 'link', 'hr', '|', 'indent', 'outdent', 'alignment'],
						});
						editor.on('valuechanged', function() {
							if (scope.onvalchange)
								return scope.onvalchange(editor.getValue());
						});

						scope.$watch('htmlcode', function (value) {
							if (value && value.length) {
								editor.setValue(value);
								editor.sync();
							}
						}, true);
					});
				}
			},
			templateUrl: './js/directives/richText.html'
		};
	}]);
});