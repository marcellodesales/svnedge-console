<%@ page import="com.collabnet.svnedge.domain.MailConfiguration" %>
<%@ page import="com.collabnet.svnedge.domain.MailAuthMethod" %>
<%@ page import="com.collabnet.svnedge.domain.MailSecurityMethod" %>
<html>
<head>
  <title>CollabNet Subversion Edge <g:message code="server.page.editMail.title" /></title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="main" />
  <g:javascript library="prototype" />
</head>
<content tag="title">
  <g:message code="server.page.editMail.title" />
</content>

<g:render template="leftNav" />

<body>
  <g:render template="tabs" model="${[view: 'editMail']}" />
  
<p><g:message code="server.page.editMail.intro" args="${[server.adminEmail]}"/></p>

<div class="dialog">            
  <g:form method="post" onsubmit="javascript:check();" name="serverForm" action="updateMail">
    <g:hiddenField name="view" value="editMail"/>
    <g:hiddenField name="id" value="${config?.id}" />
    <g:hiddenField name="version" value="${config?.version}" />
    
    <div id="enabledContainer">
      <g:checkBox name="enabled" value="${config?.enabled}" />
      <label for="enabled"><g:message code="mailConfiguration.enabled.label" /></label>
      <g:javascript>
        $('enabled').onclick = toggleConfigFields;
        
        function toggleConfigFields() {
            var isEnabled = $('enabled').checked;
            $$('.requireEnabled').each(function(item) {
                item.disabled = !isEnabled;
            })
        }
      </g:javascript>
    </div>

    <br/>
    <div id="mailServerDialog">
      <p><g:message code="server.page.editMail.configureSMTP"/></p>
      <table>
        <tbody>
          <tr>
            <td class="ItemDetailName">
              <label for="serverName"><g:message code="mailConfiguration.serverName.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'serverName', 'errors')}">
              <g:textField name="serverName" value="${fieldValue(bean: config, field: 'serverName')}" size="30" class="requireEnabled"/>
            </td>
            <td class="ItemDetailValue"><i><g:message code="mailConfiguration.serverName.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="serverName">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="serverName">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>
                        
          <tr>
            <td class="ItemDetailName">
              <label for="port"><g:message code="mailConfiguration.port.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'port', 'errors')}">
              <g:textField name="port" value="${fieldValue(bean: config, field: 'port').replace(',','')}" class="requireEnabled"/>
            </td>
             <td class="ItemDetailValue"><i><g:message code="mailConfiguration.port.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="port">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="port">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="authUser"><g:message code="mailConfiguration.authUser.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'authUser', 'errors')}">
              <g:textField name="authUser" value="${fieldValue(bean: config, field: 'authUser')}" class="requireEnabled requireAuthMethod"/>
            </td>
            <td class="ItemDetailValue"><i><g:message code="mailConfiguration.authUser.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="authUser">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="authUser">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="authPass"><g:message code="mailConfiguration.authPass.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'authPass', 'errors')}">
              <g:passwordField name="authPass" value="${fieldValue(bean: config, field: 'authPass')}" class="requireEnabled requireAuthMethod"/>
            </td>
            <td class="ItemDetailValue"><i><g:message code="mailConfiguration.authPass.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="authPass">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="authPass">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="securityMethod"><g:message code="mailConfiguration.securityMethod.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'securityMethod', 'errors')}">
              <g:select name="securityMethod" from="${com.collabnet.svnedge.domain.MailSecurityMethod?.values()}" value="${config?.securityMethod}" class="requireEnabled"/>
            </td>
            <td class="ItemDetailValue"><i><g:message code="mailConfiguration.securityMethod.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="securityMethod">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="securityMethod">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="fromAddress"><g:message code="mailConfiguration.fromAddress.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean: config, field: 'fromAddress', 'errors')}">
              <g:textField name="fromAddress" value="${fieldValue(bean: config, field: 'fromAddress')}" class="requireEnabled requireAuthMethod"/>
            </td>
            <td class="ItemDetailValue"><i><g:message code="mailConfiguration.fromAddress.label.tip" /></i></td>
          </tr>
        <g:hasErrors bean="${config}" field="fromAddress">
          <tr>
            <td>&nbsp;</td>
            <td colspan="2" width="100%" valign="top" class="errors">
              <ul><g:eachError bean="${config}" field="fromAddress">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </td>
          </tr>
        </g:hasErrors>

      <tr class="ContainerFooter">
        <td colspan="3">
          <div class="AlignRight">
                <g:actionSubmit action="testMail" value="${message(code:'server.page.editMail.button.testSettings')}" class="Button requireEnabled"/>
                <g:actionSubmit action="updateMail" value="${message(code:'server.page.edit.button.save')}" class="Button"/>
          </div>
        </td>
      </tr>
                        
    </tbody>
  </table>
</div>
</g:form>
</div>
<g:javascript>
toggleConfigFields();
</g:javascript>
</body>
</html>
