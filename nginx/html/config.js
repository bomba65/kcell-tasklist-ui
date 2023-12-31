var camTasklistConf = {
  customScripts: {
    ngDeps: ['kcell.custom.module', 'ngAnimate','xeditable', 'pattern.restrict.custom.module'],
    deps: ['kcell-custom-module', 'ng-animate','x-editable', 'pattern-restrict-module', 'lodash', 'deep-diff'],
    paths: {
      'kcell-custom-module': '/customScripts/job-request',
      'pattern-restrict-module': '/customScripts/cadastral',
      'ng-animate': '//ajax.googleapis.com/ajax/libs/angularjs/1.2.29/angular-animate',
      'x-editable': '/customScripts/xeditable.min',
      'lodash': '/customScripts/lodash.min',
      'deep-diff': '/customScripts/deep-diff.min'
    }
  },

  "shortcuts": {
    "claimTask": {
      "key": "ctrl+alt+c",
      "description": "claims the currently selected task"
    },
    "focusFilter": {
      "key": "ctrl+shift+f",
      "description": "set the focus to the first filter in the list"
    },
    "focusList": {
      "key": "ctrl+alt+l",
      "description": "sets the focus to the first task in the list"
    },
    "focusForm": {
      "key": "ctrl+alt+f",
      "description": "sets the focus to the first input field in a task form"
    },
    "startProcess": {
      "key": "ctrl+alt+p",
      "description": "opens the start process modal dialog"
    },
  }
};
