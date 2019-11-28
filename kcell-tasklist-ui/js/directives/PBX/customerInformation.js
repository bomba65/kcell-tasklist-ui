define(['./../module'], function(module){
	'use strict';
	module.directive('customerInformation', function ($rootScope, $http, $timeout) {
		return {
			restrict: 'E',
			scope: {
				ci: '=',
				form: '=',
				view: '=',
				readonly: "=",
				legal: "=",
				start: "="
			},
			link: function(scope, element, attrs) {
			  scope.today = new Date();
			  scope.today.setHours(0, 0, 0, 0);
			  scope.tomorrow = new Date(scope.today);
			  scope.tomorrow.setTime(scope.today.getTime() + 86400000);
                scope.ci.companyRegistrationDate = new Date();

				scope.$watch('ci', function (value) {
					if (value) {

            if (scope.ci.termContract) scope.ci.termContract = new Date(scope.ci.termContract);

            if (scope.ci.termContractEnd === undefined) scope.ci.termContractEnd = true;

            if (scope.form) {
              scope.form.salesRepresentative.$setValidity('not_selected', true);
              if (!scope.ci.salesRepresentativeEmail) {
                if (scope.ci.salesRepresentativeId) scope.ci.salesRepresentativeEmail = scope.ci.salesRepresentativeId;
                else scope.form.salesRepresentative.$setValidity('not_selected', false);
              }
            }
					}
				}, true);

				scope.$watch('readonly', function (value) {
				  $timeout(function() {
            scope.$apply();
          });
        });

				scope.onContractDurationChange = function() {
          scope.form.termContract.$setValidity('invalid_date', true);
				  if (scope.ci.termContractEnd) return;
				  if (scope.ci.termContract) {
				    scope.ci.termContract = new Date(scope.ci.termContract);
				    if (scope.ci.termContract.getTime() - scope.today.getTime() < 86400000) {
              scope.form.termContract.$setValidity('invalid_date', false);
            }
          } else scope.form.termContract.$setValidity('invalid_date', false);
        };

        scope.getUser = function(val) {
          scope.ci.salesRepresentativeEmail = null;
          scope.form.salesRepresentative.$setValidity('not_selected', false);
          var users = $http.get('/camunda/api/engine/engine/default/user?firstNameLike='+encodeURIComponent('%'+val+'%')).then(
            function(response){
              var usersByFirstName = _.flatMap(response.data, function(s){
                if(s.id){
                  return s.id.split(',').map(function(user){
                    return {
                      id: s.id,
                      email: (s.email?s.email.substring(s.email.lastIndexOf('/')+1):s.email),
                      firstName: s.firstName,
                      lastName: s.lastName,
                      name: s.firstName + ' ' + s.lastName
                    };
                  });
                } else {
                  return [];
                }
              });

              return $http.get('/camunda/api/engine/engine/default/user?lastNameLike='+encodeURIComponent('%'+val+'%')).then(
                function(response){
                  var usersByLastName = _.flatMap(response.data, function(s){
                    if(s.id){
                      return s.id.split(',').map(function(user){
                        return {
                          id: s.id,
                          email: s.email.substring(s.email.lastIndexOf('/')+1),
                          firstName: s.firstName,
                          lastName: s.lastName,
                          name: s.firstName + ' ' + s.lastName
                        };
                      })
                    } else {
                      return [];
                    }
                  });
                  return _.unionWith(usersByFirstName, usersByLastName, _.isEqual);
                }
              );
            }
          );
          return users;
        };

        scope.userSelected = function($item){
          console.log(">>> ", $item.email);
          scope.ci.salesRepresentativeEmail = $item.email;
          scope.ci.salesRepresentative = $item.name;
          scope.form.salesRepresentative.$setValidity('not_selected', true);
        };
	    	},
			templateUrl: './js/directives/PBX/customerInformation.html'
		};
	});
});
