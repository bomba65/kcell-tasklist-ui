'use strict';
define('kcell-custom-module', ['angular'], function (angular) {
  var customModule = angular.module('kcell.custom.module', []);

  customModule
  .factory('exModal', function ($http, $controller, $q, $rootScope, $compile, $templateCache, $timeout) {
    var service = {};
    service.open = openModal;
    return service;

    function openModal(options) {
      var modalResultDeferred = $q.defer();
      $http.get(options.templateUrl, {
        cache : $templateCache
      }).then(function(result) {
        var modalInstance = {
          close : function(result) {
            modalResultDeferred.resolve(result);
            destroy();
          },
          dismiss : function(reason) {
            if(options.warnOnClose){
              if (confirm('Вы уверены что хотите закрыть модальное окно?')) {
                modalResultDeferred.reject(reason);
                destroy();
              }
            }else{
              modalResultDeferred.reject(reason);
              destroy();
            }
          },
        };
        var modalScope = $rootScope.$new();
        if (options.scope) {
          angular.forEach(options.scope, function(obj, key) {
            modalScope[key] = angular.copy(obj);
          });
        }
        modalScope.$close = modalInstance.close;
        modalScope.$dismiss = modalInstance.dismiss;

        if (options.controller) {
          $controller(options.controller, {
            $scope : modalScope,
            scope : modalScope,
            modalInstance : modalInstance
          });
        }
        var html = result.data;
        var modalWindow = $('<div class="modal fade ex-modal" style="display: block;" tabindex="-1"></div>');
        var modalDialog = null;
        if(options.myclass){
          modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content" style="background-color: rgb(250, 250, 250);"></div></div>');
        }else{
          modalDialog = $('<div class="modal-dialog modal-' + options.size + '"><div class="modal-content"></div></div>');
        }

        modalWindow.append(modalDialog);

        var backdrop = $('<div class="modal-backdrop fade"></div>');
        modalWindow.find('div[class="modal-content"]').html(html);

        var compiled = $compile(modalWindow)(modalScope);

        $('body').append(compiled).append(backdrop);

        $timeout(function(){
          modalWindow.addClass('in');
          backdrop.addClass('in');
          $('body').addClass('modal-open');
          angular.element('.ex-modal').focus();
          if(options.warnOnClose){
            $(window).bind('beforeunload', function(){return 'Внимание';});
          }
        });

        function destroy() {
          modalWindow.removeClass('in');
          backdrop.removeClass('in');
          $('body').removeClass('modal-open');
          $(window).unbind('beforeunload');

          $timeout(function(){
            modalScope.$destroy();
            modalWindow.remove();
            backdrop.remove();
            compiled.remove();
          }, 500);
        }
      });
      return modalResultDeferred.promise;
    }
  })
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
  })
  .directive('requiredFile',function(){
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
  return customModule;
});