define(['./../module'], function(module){
	'use strict';
	module.directive('customerInformation', function ($rootScope, $http) {
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
				scope.$watch('ci', function (value) {
					if (value) {
						if (!scope.ci.companyRegistrationDate) scope.ci.companyRegistrationDate = new Date();
						else scope.ci.companyRegistrationDate = new Date(scope.ci.companyRegistrationDate);

						if (scope.legal) {
							if (!scope.ci.termContract) scope.ci.termContract = new Date();
							else scope.ci.termContract = new Date(scope.ci.termContract);
						}
					}
				})


        scope.getUser = function(val) {
          scope.ci.salesRepresentativeId = null;
          var users = $http.get('/camunda/api/engine/engine/default/user?memberOfGroup=demand_uat_users&firstNameLike='+encodeURIComponent('%'+val+'%')).then(
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
                  })
                } else {
                  return [];
                }
              });

              return $http.get('/camunda/api/engine/engine/default/user?memberOfGroup=demand_uat_users&lastNameLike='+encodeURIComponent('%'+val+'%')).then(
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
          scope.ci.salesRepresentativeId = $item.id;
          scope.ci.salesRepresentative = $item.name;
        };
	    	},
			templateUrl: './js/directives/PBX/customerInformation.html'
		};
	});
});