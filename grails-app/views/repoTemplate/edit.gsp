<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repoTemplate.page.edit.header.title /></title>
    </head>

<g:render template="/repo/leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>

<body>
  <div class="body">
    <g:hasErrors bean="${repoTemplateInstance}">
      <div class="errors">
        <g:renderErrors bean="${repoTemplateInstance}" as="list" />
      </div>
    </g:hasErrors>
    <g:form method="post" >
      <g:hiddenField name="id" value="${repoTemplateInstance?.id}" />
      <g:hiddenField name="version" value="${repoTemplateInstance?.version}" />
      <div class="dialog">
        <table class="Container">
        <thead> 
          <tr class="ContainerHeader">
            <td colspan="3"><g:message code="repoTemplate.page.edit.header.title"/></td>        
          </tr>
        </thead>
        <tbody>
          <tr class="prop">
            <td valign="top" class="name">
              <label for="name"><g:message code="repoTemplate.name.label" default="Name" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: repoTemplateInstance, field: 'name', 'errors')}">
              <g:textField name="name" maxlength="120" value="${fieldValue(bean: repoTemplateInstance, field: 'name')}" size="80"/>
            </td>
            <td>&nbsp;</td>
          </tr>
          <g:hasErrors bean="${repoTemplateInstance}" field="name">
            <tr id="name_errors_row">
              <td>&nbsp;</td>
              <td colspan="2" width="100%" class="errors">
                <ul><g:eachError bean="${repoTemplateInstance}" field="name">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>

<!-- 
          <tr class="prop">
            <td valign="top" class="name">
              <label for="displayOrder"><g:message code="repoTemplate.displayOrder.label" default="displayOrder" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: repoTemplateInstance, field: 'displayOrder', 'errors')}">
              <g:textField name="displayOrder" value="${fieldValue(bean: repoTemplateInstance, field: 'displayOrder')}" size="3"/>
            </td>
            <td>&nbsp;</td>
          </tr>
-->
                         
          <tr class="prop">
            <td valign="top" class="name">
              <label for="active"><g:message code="repoTemplate.active.label" default="Active" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: repoTemplateInstance, field: 'active', 'errors')}">
              <g:checkBox name="active" value="${repoTemplateInstance?.active}" />
            </td>
            <td>&nbsp;</td>
          </tr>
          <tr class="ContainerFooter">
            <td colspan="3">
              <div class="AlignRight">
                <g:actionSubmit class="Button save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                <g:actionSubmit class="Button delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
              </div>
            </td>
          </tr>
        </tbody>
        </table>
      </div>

    </g:form>
  </div>
</body>
</html>
