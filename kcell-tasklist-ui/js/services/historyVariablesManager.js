define(['./module'], function(module){
  'use strict';
  return module.service('historyVariablesManager', ['$http', function($http) {
    return function(camForm, taskDefKey) {
      function getAttachmentsChanges(lastAttachments, attachments) {
        var attachmentsChanges = {added: [], removed: []};
        var el;
        for (var i = 0; i < attachments.length; i++) {
          el = lastAttachments.find(function (e) {
            return e.path === attachments[i].path;
          });
          if (!el) attachmentsChanges.added.push(attachments[i]);
        }
        for (var i = 0; i < lastAttachments.length; i++) {
          el = attachments.find(function (e) {
            return e.path === lastAttachments[i].path;
          });
          if (!el) attachmentsChanges.removed.push(lastAttachments[i]);
        }
        return attachmentsChanges;
      }
      return {
        getAttachmentsChanges: getAttachmentsChanges,
        createVariables: function(hasAttachments) {
          camForm.variableManager.createVariable({
            name: taskDefKey + 'TaskResult',
            type: 'String',
            value: ''
          });
          camForm.variableManager.createVariable({
            name: taskDefKey + 'TaskComment',
            type: 'String',
            value: ''
          });
          if (hasAttachments) {
            camForm.variableManager.createVariable({
              name: taskDefKey + 'TaskAttachments',
              type: 'Json',
              value: {}
            });
          }
        },
        setVariables: function(resolution, comment, lastAttachments, attachments) {
          camForm.variableManager.variableValue(taskDefKey + 'TaskResult', resolution);
          camForm.variableManager.variableValue(taskDefKey + 'TaskComment', comment);
          if (lastAttachments && attachments) {
            var attachmentsChanges = getAttachmentsChanges(lastAttachments, attachments);
            camForm.variableManager.variableValue(taskDefKey + 'TaskAttachments', attachmentsChanges);
          }
        }
      };
    };
  }]);
});
