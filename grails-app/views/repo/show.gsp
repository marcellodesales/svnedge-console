<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Repository</title>
    </head>

<g:render template="leftNav" />

<content tag="title">
        Repositories
</content>

    <body>

<g:if test="${!repositoryInstance.permissionsOk}">
<div class="instructionText">
    <i>File Permissions Update.</i>
    <p>
    CollabNet Subversion Edge requires that repository files and directories be writable, which may not be the case
    with imported repositories.
    </p>
    <p>
      To correct for this repository, run the following command in the terminal:
    </p>
    <code>sudo chown -R ${svnUser}:${svnGroup} ${repoPath}</code>
    <p>
      Afterwards, click "Validate Permissions" to remove this warning.
    </p>
 </div>
</g:if>


        <table class="Container">
    <tbody>
    <tr class="ContainerHeader">
      <td colspan="2">Show Repository</td>    
    </tr>

                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Name:</td>

                            <td valign="top" class="value" width="100%">${fieldValue(bean:repositoryInstance, field:'name')}</td>

                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">Status:</td>

                            <td valign="top" class="value">
                              <g:if test="${repositoryInstance.permissionsOk}">
                                <span style="color:green">OK</span>
                              </g:if>
                              <g:else>
                                <span style="color:red">May Need Permissions Fix</span>
                              </g:else>
                             </td>
                        </tr>
                        <tr>
                            <td colspan="2">
                                <g:render template="../statistics/chart"/>
                            </td>
                        </tr>
                    
                    </tbody>
                </table>
    

            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                    <g:if test="${!repositoryInstance.permissionsOk}">
                    <span class="button"><g:actionSubmit class="updatePermissions" value="Validate Permissions" action="updatePermissions"/></span>
                    </g:if>
                    <%--
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                    --%>
                </g:form>
            </div>
        </div>
    </body>
</html>
