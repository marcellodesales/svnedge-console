<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.show.title" /></title>
    </head>

<g:render template="leftNav" />

<content tag="title">
     <g:message code="repository.page.leftnav.title" />
</content>

    <body>

<g:if test="${!repositoryInstance.permissionsOk}">
<div class="instructionText">
    <i><g:message code="repository.page.show.filePermissionInfo" /></i>
    <p>
       <g:message code="repository.page.show.permission.p1" args="${[CollabNet Subversion Edge]}" />
    </p>
    <p>
      <g:message code="repository.page.show.permission.p2" />
    </p>
    <code>sudo chown -R ${svnUser}:${svnGroup} ${repoPath}</code>
    <p>
      <g:message code="repository.page.show.permission.p3" />
    </p>
 </div>
</g:if>


        <table class="Container">
    <tbody>
    <tr class="ContainerHeader">
      <td colspan="2"><g:message code="repository.page.show.header" /></td>    
    </tr>

                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.name" /></td>

                            <td valign="top" class="value" width="100%">${fieldValue(bean:repositoryInstance, field:'name')}</td>

                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="repository.page.show.status" /></td>

                            <td valign="top" class="value">
                              <g:if test="${repositoryInstance.permissionsOk}">
                                <span style="color:green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                              </g:if>
                              <g:else>
                                <span style="color:red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
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
                    <span class="button"><g:actionSubmit class="updatePermissions" value="${message(code:'repository.page.show.button.validate') }" action="updatePermissions"/></span>
                    </g:if>
                    <%--
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                    --%>
                </g:form>
            </div>
        </div>
    </body>
</html>
