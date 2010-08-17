<%@ page import="com.collabnet.svnedge.console.security.Role" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}"/>
  <title><g:message code="default.show.label" args="[entityName]"/></title>
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

  <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir: '/images/icons', file: 'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="role" action="list">Role List</g:link>
  </div>

</content>
<body>

<div class="body">
  <h1><g:message code="default.show.label" args="[entityName]" default="Show Role"/></h1>

  <div class="dialog">
    <table>
      <tbody>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.id.label" default="Id"/></td>

        <td valign="top" class="value">${fieldValue(bean: roleInstance, field: "id")}</td>

      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.authority.label" default="Authority"/></td>

        <td valign="top" class="value">${fieldValue(bean: roleInstance, field: "authority")}</td>

      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.description.label" default="Description"/></td>

        <td valign="top" class="value">${fieldValue(bean: roleInstance, field: "description")}</td>

      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.people.label" default="Users Having Role"/>:</td>

        <td valign="top" style="text-align: left;" class="value">
          <ul>
            <g:each in="${roleInstance.people}" var="p">
              <li><g:link controller="user" action="show" id="${p.id}">${p.realUserName}</g:link> (${p.username})</li>
            </g:each>
          </ul>
        </td>

      </tr>

      </tbody>
    </table>
  </div>
  <div class="buttons">
    <g:form>
      <g:hiddenField name="id" value="${roleInstance?.id}"/>
      <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
    </g:form>
  </div>
</div>
</body>
</html>
