<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repository.page.list.header.title /></title>
    </head>

<g:render template="leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>
    
    <body>
    <table class="Container">
    <tbody>

    <g:if test="${repositoryInstanceList.size() > 0}">
        <tr class="ContainerHeader">
          <td colspan="4"><g:message code="repository.page.list.header" /></td>
        </tr>
        <tr class="ItemListHeader">
           <g:sortableColumn property="name" title="Name" />

      <g:if test="${isReplica}">
              <g:sortableColumn property="lastSyncTime" title="${message(code:'repository.page.list.replica.lastSyncTime')}" />
              <g:sortableColumn property="lastSyncRev" title="${message(code:'repository.page.list.replica.lastSyncRevision')}" />
              <g:sortableColumn property="enabled" title="${message(code:'repository.page.list.replica.enabled')}" />
              <g:sortableColumn property="status" title="${message(code:'repository.page.list.replica.status')}" />
      </g:if> 
                               <th><g:message code="repository.page.list.checkout_command" /></th>
      <g:if test="${!isReplica}">
              <g:sortableColumn property="permissionsOk" title="${message(code:'repository.page.list.status')}" />
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
              <th><g:message code="repository.page.list.edit" /></th>
        </g:ifAnyGranted>
      </g:if>
                        </tr>
                    <tbody>
                    <g:each in="${repositoryInstanceList}" status="i" var="repositoryInstance">
<g:if test="${isReplica}">
  <g:set var="repoName" value="${repositoryInstance.repo.name}"/>
</g:if>
<g:else>
  <g:set var="repoName" value="${repositoryInstance.name}"/>
</g:else>
                        <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
                            <g:set var="viewvcURL" value="${server.viewvcURL(repoName)}"/>
                            <g:if test="${viewvcURL}">
                              <td><a href="${viewvcURL}" target="_blank">${repoName}</a></td>
                            </g:if>
                            <g:else>
                              <td>${repoName}</td>
                            </g:else>

      <g:if test="${isReplica}">
                <g:if test="${repositoryInstance.lastSyncTime > 0}">
                  <td><g:formatDate date="${new Date(repositoryInstance.lastSyncTime)}"
                                    format="yyyy-MM-dd HH:mm:ss z"/></td>
                </g:if>
                <g:else>
                  <td><g:message code="repository.page.list.replica.notUdated" /></td>
                </g:else>
                <td>${fieldValue(bean:repositoryInstance, field:'lastSyncRev')}</td>
                <td>${fieldValue(bean:repositoryInstance, field:'enabled')}</td>
                <td>
                  ${fieldValue(bean:repositoryInstance, field:'status')}
                </td>
      </g:if>

                <td>svn co ${server.svnURL()}${repoName} ${repoName} --username=<g:loggedInUsername/></td>
      <g:if test="${!isReplica}">
                  <td>
                    <g:if test="${repositoryInstance.permissionsOk}">
                      <span style="color:green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                    </g:if>
                    <g:else>
                      <span style="color:red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
                    </g:else>

                  </td>

      <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
                            <td><g:link action="show" id="${repositoryInstance.id}"><g:message code="repository.page.list.instance.edit" /></g:link></td>
      </g:ifAnyGranted>
      </g:if> 
                        </tr>
                    </g:each>
                    <tr class="ContainerFooter">
                       <td colspan="4">
                       <div class="paginateButtons">
                <g:paginate total="${repositoryInstanceTotal}" />
            </div>
                       </td>
                    </tr>
            </div>
        </div>
    </g:if>

    <g:else>
      <g:if test="${isReplica}">
        <p><g:message code="repository.page.list.replica.noRepos" /></p>
      </g:if>
      <g:else>
        <p><g:message code="repository.page.list.noRepos" /></p>
      </g:else>
    </g:else>

       </tbody>
      </table>
    </body>
</html>
