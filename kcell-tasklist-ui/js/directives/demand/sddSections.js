define(['./../module'], function(module){
  'use strict';
  module.directive('demandSddSections', function ($rootScope, $http, exModal) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        section: '=',
        form: '=',
        view: '=',
        disabled: '=',
        required: '=',
        processId: '=',
        taskId: '='
      },
      link: function(scope, element, attrs) {
        scope.sections = [
          [
            {from: 0, to: 1},
            {from: 2, to: 5, name: 'Functional requirements', hidden: true},
            {from: 6, to: 7}
          ],
          [
            {from: 0, to: 8}
          ],
          [
            {from: 0, to: 4, name: 'General architecture', hidden: true},
            {from: 5, to: 15, name: 'Solution design', hidden: true}
          ],
          [
            {from: 0, to: 2}
          ]
        ];

        const fields = [
          [
            {name: 'User functions', hidden: true, value: ''},
            {name: 'Administrative functions', hidden: true, value: ''},
            {name: 'Product connection', hidden: true, value: ''},
            {name: 'Product suspention', hidden: true, value: ''},
            {name: 'Product renewal', hidden: true, value: ''},
            {name: 'Product disconnection', hidden: true, value: ''},
            {name: 'Charging', hidden: true, value: ''},
            {name: 'Invoicing', hidden: true, value: ''}
          ],
          [
            {name: 'Interfaces with the system', hidden: true, value: ''},
            {name: 'Security', hidden: true, value: ''},
            {name: 'Technologies', hidden: true, value: ''},
            {name: 'Used equipment', hidden: true, value: ''},
            {name: 'Technical support', hidden: true, value: ''},
            {name: 'Service availability', hidden: true, value: ''},
            {name: 'System recovery time', hidden: true, value: ''},
            {name: 'Performance requirements', hidden: true, value: ''},
            {name: 'Scalability requirements', hidden: true, value: ''}
          ],
          [
            {name: 'Hardware architecture', hidden: true, value: ''},
            {name: 'Software architecture', hidden: true, value: ''},
            {name: 'Interaction architecture', hidden: true, value: ''},
            {name: 'Integration scheme', hidden: true, value: ''},
            {name: 'Network layout', hidden: true, value: ''},
            {name: 'Use and Interaction scenarios', hidden: true, value: ''},
            {name: 'Data model', hidden: true, value: ''},
            {name: 'Data adaptation', hidden: true, value: ''},
            {name: 'External access interfaces', hidden: true, value: ''},
            {name: 'Internal access interfaces', hidden: true, value: ''},
            {name: 'Information security', hidden: true, value: ''},
            {name: 'Performance', hidden: true, value: ''},
            {name: 'Data backup and security', hidden: true, value: ''},
            {name: 'Logging', hidden: true, value: ''},
            {name: 'Reporting', hidden: true, value: ''},
            {name: 'Monitoring', hidden: true, value: ''}
          ],
          [
            {name: 'Function on midlleware / Abstraction layer', hidden: true, value: ''},
            {name: 'DNS settings', hidden: true, value: ''},
            {name: 'Firewall settings', hidden: true, value: ''}
          ]
        ];

        scope.$watch('data', function(value) {
          if (value) {
            if (!scope.data) scope.data = {};
            if (!scope.data.fields) scope.data.fields = fields[scope.section];
            if (!scope.data.customs) scope.data.customs = [];

            for (var r of scope.sections[scope.section]) {
              if (r.name) {
                r.hidden = true;
                for (var i = r.from; i <= r.to; i++) {
                  if (!scope.data.fields[i].hidden) {
                    r.hidden = false;
                    break;
                  }
                }
              }
            }
          }
        }, true);
      },
      templateUrl: './js/directives/demand/sddSections.html'
    };
  });
});
