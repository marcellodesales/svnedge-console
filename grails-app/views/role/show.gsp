<%@ page import="com.collabnet.svnedge.domain.Role" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title><g:message code="role.page.show.title"/></title>
</head>
<content tag="title">
  <g:message code="role.page.header"/>
</content>

<g:render template="../user/leftNav" />

<body>

<div class="body">
  <h1><g:message code="role.page.show.title"/></h1>

  <div class="dialog">
    <table>
      <tbody>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.authority.label"/></td>

        <td valign="top" class="value">${fieldValue(bean: roleInstance, field: "authority")}</td>

      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.description.label"/></td>

        <td valign="top" class="value">${fieldValue(bean: roleInstance, field: "description")}</td>

      </tr>

      <tr class="prop">
        <td valign="top" class="name"><g:message code="role.people.label"/></td>

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
      <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label')}"/></span>
    </g:form>
  </div>
</div>
</body>
</html>
