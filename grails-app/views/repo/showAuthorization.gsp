<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="repository.page.editAuthorization.title" /></title>
</head>

<content tag="title">
    <g:message code="repository.page.leftnav.title" />
</content>

<g:render template="leftNav"/>

<body>
<g:hasErrors bean="${authRulesCommand?.errors}">
    <div class="errors">
        <g:renderErrors as="list"/>
    </div>
</g:hasErrors>

  <g:if test="${lock}">
    <div class="warningText"><g:message code="repository.page.showAuthorization.isLocked" args="${[lockOwner.realUserName, lock.createdOn]}"/></div>
  </g:if>

<table class="Container">
  <tbody>
  <tr class="ContainerHeader">
    <td colspan="2"><g:message code="repository.page.editAuthorization.header"/></td>
  </tr>

  <tr class="prop">
    <td width="100%" valign="top" class="value">
      <pre>${accessRules}</pre>
    </td>
  </tr>

  <tr class="ContainerFooter">
    <td colspan="2">
      <g:form action="editAuthorization" method="get">
        <div class="AlignRight">
          <input class="Button" type="submit" value="${message(code: 'default.button.edit.label')}" <g:if test="${lock}">disabled="disabled"</g:if> />
        </div>
      </g:form>
    </td>
  </tr>
  </tbody>
</table>

</body>
</html>
