<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repository.page.list.header.title /></title>
        <g:render template="/common/listViewResources"/>

    </head>

<g:render template="leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>

<body>

<g:form>
  <g:if test="${repositoryInstanceList.size() > 0}">
    <table id="reposTable" class="table table-striped table-bordered table-condensed tablesorter">
      <thead>
      <tr>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
          <th><g:listViewSelectAll/></th>
        </g:ifAnyGranted>
        <g:sortableColumn property="name" title="${message(code:'repository.page.list.name')}"/>
          <th><g:message code="repository.page.list.checkout_command"/></th>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
          <th><g:message code="repository.page.list.status"/></th>
        </g:ifAnyGranted>
      </tr>
      </thead>
      <tbody>
      <g:each in="${repositoryInstanceList}" status="i" var="repositoryInstance">
        <g:set var="repoName" value="${repositoryInstance.name}"/>
        <tr>
          <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
            <td><g:listViewSelectItem item="${repositoryInstance}"/></td>
          </g:ifAnyGranted>
          <g:set var="viewvcURL" value="${server.viewvcURL(repoName)}"/>
          <g:if test="${viewvcURL}">
            <td><a href="${viewvcURL}" target="_blank">${repoName}</a></td>
          </g:if>
          <g:else>
            <td>${repoName}</td>
          </g:else>
          <td>svn co ${server.svnURL()}${repoName} ${repoName} --username=<g:loggedInUsername/></td>
          <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
            <td>
              <g:link action="dumpFileList" id="${repositoryInstance.id}">
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
      </tbody>
    </table>

    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
    <p class="pull-right">
      <g:listViewActionButton action="deleteMultiple" minSelected="1" maxSelected="1"
                              confirmMessage="${message(code:'repository.page.list.delete.confirmation')}"
                              confirmByTypingThis="${message(code:'default.confirmation.typeThis')}">
        <g:message code="default.button.delete.label"/>
      </g:listViewActionButton>
      <g:listViewActionButton action="dumpOptions" minSelected="1" maxSelected="1">
        <g:message code="repository.page.list.button.dump.label"/>
      </g:listViewActionButton>
      <g:listViewActionButton action="loadOptions" minSelected="1" maxSelected="1">
        <g:message code="repository.page.list.button.load.label"/>
      </g:listViewActionButton>
    </p>
    </g:ifAnyGranted>

    <g:pagination total="${repositoryInstanceTotal}"/>
    

  </g:if>

  <g:else>
    <g:if test="${isReplica}">
      <p><g:message code="repository.page.list.replica.noRepos"/></p>
    </g:if>
    <g:else>
      <p><g:message code="repository.page.list.noRepos"/></p>
    </g:else>
  </g:else>

</g:form>

</body>
</html>
