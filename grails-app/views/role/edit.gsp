<%@ page import="com.collabnet.svnedge.console.security.Role" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}"/>
  <title><g:message code="default.edit.label" args="[entityName]"/></title>
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
<g:hasErrors bean="${roleInstance}">
    <div class="errors">
      <g:renderErrors bean="${roleInstance}" as="list"/>
    </div>
  </g:hasErrors>
    <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2"><g:message code="default.edit.label" args="[entityName]" default="Edit Role"/></td>        
        </tr> 
  <g:form method="post">
    <g:hiddenField name="id" value="${roleInstance?.id}"/>
    <g:hiddenField name="version" value="${roleInstance?.version}"/>
        <tr class="prop">
          <td valign="top" class="name">
            <label><g:message code="role.authority.label" default="Authority"/>:</label>
          </td>
          <td width="100%" valign="top" class="value ${hasErrors(bean: roleInstance, field: 'authority', 'errors')}">
            ${fieldValue(bean: roleInstance, field: "authority")}
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="description"><g:message code="role.description.label" default="Description"/>:</label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: roleInstance, field: 'description', 'errors')}">
            <g:textArea name="description" value="${roleInstance?.description}" />
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name" style="white-space: nowrap;">
            <label for="people"><g:message code="role.people.label" default="Users Having Role"/>:</label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: roleInstance, field: 'people', 'errors')}">
            <g:select name="people" from="${userList}" multiple="yes" optionKey="id" optionValue="username" size="5" value="${roleInstance?.people}"/>
            <i><strong>Note:</strong> You cannot add or remove yourself from a Role so the logged-in account is not shown.</i> 
          </td>
        </tr>
        <tr class="ContainerFooter">
          <td colspan="2">
             <div class="AlignRight">
               <g:actionSubmit class="Button save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}"/>
             </div>
          </td>
        </tr>
        </tbody>
      </table>
  </g:form>
</body>
</html>
