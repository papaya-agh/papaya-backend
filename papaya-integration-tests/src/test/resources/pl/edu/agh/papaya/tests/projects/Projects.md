# Projects

Authenticating as [ ](- "c:echo=switchUser('heinz')").

Upon [creating an example project](- "#project=createExampleProject()"),
its name should be equal to [Example Project](- "?=#project.name"),
its description should be equal to [Example Project Description](- "?=#project.description"),
its initial coefficient should be equal to [0.7](- "?=#project.initialCoefficient").

Upon [retrieving the created project by its ID](- "#retrievedProject=getProjectById(#project.id)"),
it shall be [the same as the created project](- "c:assert-true= #retrievedProject == #project").

The [current user's projects](- "#projects=getProjects()"))
now should consist of [1](- "?=#projects.size") project,
[equal to the created one](- "c:assert-true= #projects[0] == #project").
