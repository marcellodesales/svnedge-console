<%@ page import="com.collabnet.svnedge.domain.RepoTemplate" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repoTemplate.page.create.header.title /></title>
        <g:javascript library="prototype"/>
    </head>

<g:render template="/repo/leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>
    
  <body>
    <div class="body">
      <g:uploadForm>
      <div class="dialog">
        <table class="Container">
        <thead> 
          <tr class="ContainerHeader">
            <td colspan="2"><g:message code="repoTemplate.page.create.header.title"/></td>        
          </tr>
        </thead>
        <tbody>
          <tr class="prop">
            <td valign="top" class="name">
              <label for="name"><g:message code="repoTemplate.name.label" default="Name" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: repoTemplateInstance, field: 'name', 'errors')}">
              <g:textField name="name" maxlength="120" value="${repoTemplateInstance?.name}" size="80"/>
              <g:javascript>$('name').focus()</g:javascript>
            </td>
          </tr>
          <g:hasErrors bean="${repoTemplateInstance}" field="name">
            <tr id="name_errors_row">
              <td>&nbsp;</td>
              <td width="100%" class="errors">
                <ul><g:eachError bean="${repoTemplateInstance}" field="name">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>
          
          <tr class="prop">
            <td valign="top" class="name">
              <label for="templateUpload"><g:message code="repoTemplate.templateUpload.label" /></label>
            </td>
            <td valign="top" class="value">
               <input type="file" name="templateUpload" />
               <div><g:message code="repoTemplate.templateUpload.description" /></div>
            </td>
          </tr>
          <g:hasErrors bean="${repoTemplateInstance}" field="location">
            <tr id="location_errors_row">
              <td>&nbsp;</td>
              <td width="100%" class="errors">
                <ul><g:eachError bean="${repoTemplateInstance}" field="location">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>

<!-- 
          <tr class="prop">
            <td valign="top" class="name">
              <label for="displayFirst"><g:message code="repoTemplate.displayFirst.label" /></label>
            </td>
            <td valign="top" class="value">
              <g:checkBox name="displayFirst" value="${params.displayFirst}" />
            </td>
          </tr>
 -->                        
          <tr class="ContainerFooter">
            <td colspan="2">
              <div class="AlignRight">
                <g:actionSubmit action="save" class="Button save" value="${message(code: 'default.button.create.label')}" />
              </div>
            </td>
          </tr>
      </tbody>
      </table>
    </div>
    </g:uploadForm>
  </div>
</body>
</html>
