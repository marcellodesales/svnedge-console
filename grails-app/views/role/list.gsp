<%@ page import="com.collabnet.svnedge.console.security.Role" %>
<html>
<head>
  <meta name="layout" content="main"/>
  <title><g:message code="role.page.list.title"/></title>
</head>

<content tag="title">
  <g:message code="role.page.header"/>
</content>

<g:render template="../user/leftNav" />

<body>

    <table class="Container">
      <tbody>
      <tr class="ContainerHeader">
        <td colspan="3"><g:message code="role.page.list.header"/></td>
      </tr>
      <tr class="ItemListHeader">
        <g:sortableColumn property="authority" title="${message(code: 'role.page.list.column.authority')}"/>

        <g:sortableColumn property="description" title="${message(code: 'role.page.list.column.description')}"/>
      </tr>
      <tbody>
      <g:each in="${roleList}" status="i" var="roleInstance">
        <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">

          <td width="20%"><g:ifAnyGranted role="ROLE_ADMIN,${roleInstance.authority}"><g:link action="show" id="${roleInstance.id}">${fieldValue(bean: roleInstance, field: "authority")}</g:link></g:ifAnyGranted></td>

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
