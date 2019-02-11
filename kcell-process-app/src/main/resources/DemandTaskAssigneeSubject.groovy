def processName = delegateTask.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateTask.getProcessDefinitionId()).getName()
processName + " - " + delegateTask.execution.processBusinessKey + ", Activity " + delegateTask.getName() + " assigned"
