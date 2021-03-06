# Projects

## Creating an example project

Authenticating as [ ](- "c:echo=switchUser('heinz')").

Current user's [projects](- "#projects=getProjects()")
should be [empty](- "c:assert-true=#projects.empty").

Upon [creating an example project](- "#project=createExampleProject()"),
its name should be equal to [Example Project](- "?=#project.name"),
its description should be equal to [Example Project Description](- "?=#project.description")
and its initial coefficient should be equal to [0.7](- "?=#project.initialCoefficient").

Upon [retrieving the created project by its ID](- "#retrievedProject=getProjectById(#project.id)"),
it shall be [the same as the created project](- "c:assert-true= #retrievedProject == #project").

The [current user's projects](- "#projects=getProjects()")
now should consist of [1](- "?=#projects.size") project,
which is [equal to the created one](- "c:assert-true= #projects[0] == #project").

When authenticated as [ ](- "c:echo=switchUser('peter')"), the current user
[should not have access](- "c:assert-true=403==tryGetProjectById(#project.id).statusCode")
to the created project. Their [projects](- "#projects2=getProjects()")
shall be [empty](- "c:assert-true=#projects2.empty").

## Adding users to the project

Authenticating as [ ](- "c:echo=switchUser('heinz')").

The user [ ](- "c:echo=describeUser('peter')")
is [added to the project](- "addToProject(#project.id, 'peter')").

Now, [ ](- "c:echo=switchUser('peter')") should
[have access to the project](- "c:assert-true=200==tryGetProjectById(#project.id).statusCode").

[It is not possible](- "c:assert-true=400==tryToAddNonExistentUserToProject(#project.id).statusCode")
to add a non-existing user to the project.

[It is not possible](- "c:assert-true=409==tryToAddExistingUserToProject(#project.id, 'peter').statusCode")
to add the same user to the project twice.

## Removing users from the project

Authenticating as [ ](- "c:echo=switchUser('heinz')").

The user [ ](- "c:echo=describeUser('peter')")
is [removed from the project](- "removeFromProject(#project.id, 'peter')").

When authenticated as [ ](- "c:echo=switchUser('peter')"), the current user
[should not have access](- "c:assert-true=403==tryGetProjectById(#project.id).statusCode")
to the created project. Their [projects](- "#projects2=getProjects()")
shall be [empty](- "c:assert-true=#projects2.empty").

### Non-admins cannot remove users from projects

Authenticating as [ ](- "c:echo=switchUser('heinz')").

The user [ ](- "c:echo=describeUser('peter')")
is [added to the project](- "addToProject(#project.id, 'peter')").

The user [ ](- "c:echo=describeUser('rick')")
is [added to the project](- "addToProject(#project.id, 'rick')").

When authenticated as [ ](- "c:echo=switchUser('rick')"), the current user
[cannot remove](- "c:assert-true=403==tryRemoveFromProject(#project.id, 'peter').statusCode")
other users. The current user also
[cannot set roles](- "c:assert-true=403==trySetRoleInProject(#project.id, 'peter', 'ADMIN').statusCode")
of other users.
