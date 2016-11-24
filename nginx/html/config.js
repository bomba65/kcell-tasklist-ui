var camTasklistConf = {
  customScripts: {
    ngDeps: ['kcell.custom.module'],
    deps: ['job-request-module'],
    paths: {
      'job-request-module': '/customScripts/job-request'
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
