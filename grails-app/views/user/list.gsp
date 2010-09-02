<head>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge Users</title>
</head>


<content tag="title">
    Users
</content>
<content tag="leftMenu">


  <div class="ImageListParentSelectedNoTop">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link action="list">User List</g:link>
  </div>

  <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link action="create">New User</g:link>
  </div>

  <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="role" action="list">Role List</g:link>
  </div>

</content>

<body>
            <table class="Container">
              <tbody>
              <tr class="ContainerHeader">
                <td colspan="5">User List</td>
              </tr>
               <tr class="ItemListHeader">
                    <g:sortableColumn property="username" title="Login Name" />
                    <g:sortableColumn property="realUserName" title="Full Name" />
                    <g:sortableColumn property="description" title="Description" />
              </tr>
            </thead>
            <tbody>
            <g:each in="${userInstanceList}" status="i" var="person">
                <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
                    <td><g:link action="edit" id="${person.id}">${person.username?.encodeAsHTML()}</g:link></td>
                    <td>${person.realUserName?.encodeAsHTML()}</td>
                    <td>${person.description?.encodeAsHTML()}</td>
                </tr>
            </g:each>
            <tr class="ContainerFooter">
                       <td colspan="5">
                       <div class="paginateButtons">
                <g:paginate total="${com.collabnet.svnedge.console.security.User.count()}" />
            </div>
                       </td>
                    </tr>
            </tbody>
            </table>
</body>
