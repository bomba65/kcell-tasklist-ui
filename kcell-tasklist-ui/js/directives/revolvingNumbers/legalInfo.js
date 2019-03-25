define(['./../module'], function(module){
  'use strict';
  module.directive('revolvingLegalInformation', function ($rootScope, $http, toasty) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        form: '=',
        view: '=',
        disabled: "=",
        fixed: "="
      },
      link: function(scope, element, attrs) {
        scope.$watch('data', function (value) {
          if (value) {
            if (!scope.data.companyDate) scope.data.companyDate = new Date();
            else scope.data.companyDate = new Date(scope.data.companyDate);
            if (!scope.data.termContract) scope.data.termContract = new Date();
            else scope.data.termContract = new Date(scope.data.termContract);
            if (!scope.data.clientPriority) scope.data.clientPriority = 'Normal';
          }
        });

        scope.onSalesReprChange = function() {
          if (!scope.data.salesRepr || !scope.data.salesRepr.length) scope.form.liSalesRepr.$setValidity('not_selected', true);
        };

        scope.getUser = function(val) {
          scope.data.salesReprId = null;
          scope.form.liSalesRepr.$setValidity('not_selected', false);
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
                      });
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
          scope.data.salesReprId = $item.id;
          scope.data.salesRepr = $item.name;
          scope.form.liSalesRepr.$setValidity('not_selected', true);
        };
      },
      templateUrl: './js/directives/revolvingNumbers/legalInfo.html'
    };
  });
});