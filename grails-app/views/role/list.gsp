<%@ page import="com.collabnet.svnedge.console.security.Role" %>
<html>
<head>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge Roles</title>
</head>

<content tag="title">
  Roles
</content>
<content tag="leftMenu">

  <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir: '/images/icons', file: 'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="user" action="list">User List</g:link>
  </div>

  <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir: '/images/icons', file: 'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="user" action="create">New User</g:link>
  </div>

  <div class="ImageListParentSelectedNoTop">
    <img width="9" hspace="5" height="9" src="${resource(dir: '/images/icons', file: 'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="role" action="list">Role List</g:link>
  </div>

</content>
<body>

    <table class="Container">
      <tbody>
      <tr class="ContainerHeader">
        <td colspan="3">Role List</td>
      </tr>
      <tr class="ItemListHeader">
        <g:sortableColumn property="authority" title="${message(code: 'role.authority.label', default: 'Authority')}"/>

        <g:sortableColumn property="description" title="${message(code: 'role.description.label', default: 'Description')}"/>
      </tr>
      <tbody>
      <g:each in="${roleList}" status="i" var="roleInstance">
        <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">

          <td width="20%"><g:ifAnyGranted role="ROLE_ADMIN,${roleInstance.authority}"><g:link action="edit" id="${roleInstance.id}">${fieldValue(bean: roleInstance, field: "authority")}</g:link></g:ifAnyGranted></td>

          <td>${fieldValue(bean: roleInstance, field: "description")}</td>
        </tr>
      </g:each>
      <tr class="ContainerFooter">
                       <td colspan="4">
                       <div class="paginateButtons">
                <g:paginate total="${roleTotal}" />
            </div>
                       </td>
                    </tr>
      </tbody>
    </table>
</body>
</html>
