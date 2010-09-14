<%@ page import="com.collabnet.svnedge.console.security.Role" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title><g:message code="role.page.edit.title"/></title>
</head>
<content tag="title">
  <g:message code="role.page.header"/>
</content>

<g:render template="../user/leftNav" />

<body>
<g:hasErrors bean="${roleInstance}">
    <div class="errors">
      <g:renderErrors bean="${roleInstance}" as="list"/>
    </div>
  </g:hasErrors>
    <table class="Container"> 
        <tr class="ContainerHeader">
            <td colspan="2"><g:message code="role.page.edit.title"/></td>
        </tr> 
  <g:form method="post">
    <g:hiddenField name="id" value="${roleInstance?.id}"/>
    <g:hiddenField name="version" value="${roleInstance?.version}"/>
        <tr class="prop">
          <td valign="top" class="name">
            <label><g:message code="role.authority.label"/></label>
          </td>
          <td width="100%" valign="top" class="value ${hasErrors(bean: roleInstance, field: 'authority', 'errors')}">
            ${fieldValue(bean: roleInstance, field: "authority")}
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name">
            <label for="description"><g:message code="role.description.label"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: roleInstance, field: 'description', 'errors')}">
            <g:textArea name="description" value="${roleInstance?.description}" />
          </td>
        </tr>

        <tr class="prop">
          <td valign="top" class="name" style="white-space: nowrap;">
            <label for="people"><g:message code="role.people.label"/></label>
          </td>
          <td valign="top" class="value ${hasErrors(bean: roleInstance, field: 'people', 'errors')}">
            <g:select name="people" from="${userList}" multiple="yes" optionKey="id" optionValue="username" size="5" value="${roleInstance?.people}"/>
            <i><g:message code="role.page.edit.warning.selfedit"/></i> 
          </td>
        </tr>
        <tr class="ContainerFooter">
          <td colspan="2">
             <div class="AlignRight">
               <g:actionSubmit class="Button save" action="update" value="${message(code: 'default.button.update.label')}"/>
             </div>
          </td>
        </tr>
        </tbody>
      </table>
  </g:form>
</body>
</html>
