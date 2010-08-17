<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge Repositories</title>
    </head>

<g:render template="leftNav" />

<content tag="title">
        Repositories
</content>
    
    <body>
    <table class="Container">
    <tbody>

    <g:if test="${repositoryInstanceList.size() > 0}">
        <tr class="ContainerHeader">
          <td colspan="4">Repository List</td>
        </tr>
        <tr class="ItemListHeader"> 
    
                               <g:sortableColumn property="name" title="Name" />

      <g:if test="${isReplica}">
              <g:sortableColumn property="lastSyncTime" title="Last Sync Time" />
              <g:sortableColumn property="lastSyncRev" title="Last Sync Revision" />
              <g:sortableColumn property="enabled" title="Enabled" />
              <g:sortableColumn property="status" title="Status" />
      </g:if> 
                               <th>Checkout command</th>
      <g:if test="${!isReplica}">
              <g:sortableColumn property="permissionsOk" title="Status" />
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
              <th>Edit</th>
        </g:ifAnyGranted>
      </g:if>
                        </tr>
                    </thead>
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
                                    format="yyyy-MM-dd HH:mm"/></td>
                </g:if>
                <g:else>
                  <td>Not yet updated.</td>
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
                      <span style="color:green">OK</span>
                    </g:if>
                    <g:else>
                      <span style="color:red">May Need Permissions Fix</span>
                    </g:else>

                  </td>

      <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
                            <td><g:link action="show" id="${repositoryInstance.id}">Edit</g:link></td>
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
                    </tbody>
                    
                </table>
            </div>
            
        </div>
    </g:if>
    <g:else>
      <g:if test="${isReplica}">
        <p>There are no repositories yet. Replication is initiated from the master.</p>  
      </g:if>
      <g:else>
        <p>There are no repositories yet.  You may create a new repository
        using the navigation link.</p>  
      </g:else>
    </g:else>
    </body>
</html>
