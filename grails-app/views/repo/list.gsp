<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repository.page.list.header.title /></title>
        <g:javascript library="listView"/>

    </head>

<g:render template="leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>

<body>

<g:form>
    <table id="reposTable" class="table table-striped table-bordered table-condensed tablesorter">
      <thead>
      <tr>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
          <th></th>
        </g:ifAnyGranted>
        <g:sortableColumn property="name" title="${message(code:'repository.page.list.name')}"/>
          <th><g:message code="repository.page.list.checkout_command"/></th>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
          <th><g:message code="repository.page.list.status"/></th>
        </g:ifAnyGranted>
      </tr>
      </thead>
      <tbody>
      <g:if test="${repositoryInstanceList.size() > 0}">
      <g:each in="${repositoryInstanceList}" status="i" var="repositoryInstance">
        <g:set var="repoName" value="${repositoryInstance.name}"/>
        <tr>
          <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
            <td><g:listViewSelectItem item="${repositoryInstance}" radioStyle="true"/></td>
          </g:ifAnyGranted>
          <g:set var="viewvcURL" value="${server.viewvcURL(repoName)}"/>
          <g:if test="${viewvcURL}">
            <td><a href="${viewvcURL}" target="_blank">${repoName}</a></td>
          </g:if>
          <g:else>
            <td>${repoName}</td>
          </g:else>
          <td>svn co ${server.svnURL()}${repoName} ${repoName} --username=<g:loggedInUsername/></td>
          <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
            <td>
              <g:set var="repoView" value="dumpFileList"/>
              <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_HOOKS">
                <g:set var="repoView" value="hooksList"/>
              </g:ifAnyGranted>            
              <g:link action="${repoView}" id="${repositoryInstance.id}">
                <g:if test="${repositoryInstance.permissionsOk}">
                  <g:message code="repository.page.list.instance.permission.ok"/>
                </g:if>
                <g:else>
                  <g:message code="repository.page.list.instance.permission.needFix"/>
                </g:else>
              </g:link>
            </td>
          </g:ifAnyGranted>
        </tr>
      </g:each>
      </g:if>
      <g:else>
        <g:set var="numCols" value="2"/>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
          <g:set var="numCols" value="4"/>
        </g:ifAnyGranted>
        <tr>
          <td colspan="${numCols}">
            <g:if test="${isReplica}">
              <p><g:message code="repository.page.list.replica.noRepos"/></p>
            </g:if>
            <g:else>
              <p><g:message code="repository.page.list.noRepos"/></p>
            </g:else>
          </td>
        </tr>
      </g:else>
      </tbody>
    </table>


  <g:pagination total="${repositoryInstanceTotal}"/>

  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
    <div class="pull-right">

    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
      <g:listViewActionButton action="create" minSelected="0" maxSelected="0"><g:message code="default.button.create.label" /></g:listViewActionButton>
      <g:listViewActionButton action="discover" minSelected="0" maxSelected="0"><g:message code="repository.page.list.button.discover.label" /></g:listViewActionButton>
    </g:ifAnyGranted>
      <g:listViewActionButton action="${repoView}" minSelected="1" maxSelected="1"><g:message code="default.button.show.label" /></g:listViewActionButton>
    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
      <g:listViewActionButton action="dumpOptions" minSelected="1" maxSelected="1">
        <g:message code="repository.page.list.button.dump.label"/>
      </g:listViewActionButton>
      <g:listViewActionButton action="loadOptions" minSelected="1" maxSelected="1">
        <g:message code="repository.page.list.button.load.label"/>
      </g:listViewActionButton>
      <g:listViewActionButton action="deleteMultiple" minSelected="1" maxSelected="1"
                              confirmMessage="${message(code:'repository.page.list.delete.confirmation')}"
                              confirmByTypingThis="${message(code:'default.confirmation.typeThis')}">
        <g:message code="default.button.delete.label"/>
      </g:listViewActionButton>
      </g:ifAnyGranted>
    </div>
  </g:ifAnyGranted>

</g:form>

</body>
</html>
