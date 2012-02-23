<%@ page import="com.collabnet.svnedge.domain.MailConfiguration" %>
<%@ page import="com.collabnet.svnedge.domain.MailAuthMethod" %>
<%@ page import="com.collabnet.svnedge.domain.MailSecurityMethod" %>
<html>
<head>
  <title>CollabNet Subversion Edge <g:message code="server.page.editMail.title" /></title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="layout" content="main" />
  <g:javascript library="prototype" />
  <g:javascript>

Event.observe(window, 'load', function() {
    if ($('testButton')) {
        $('testButton').observe('click', testMailHandler);
    }
});


function testMailHandler() {
    $('testButton').value = "Testing...";
    $('saveButton').disabled = true;
    window.setTimeout("$('testButton').disabled = true", 500);
}

function fetchResult() {
    var repeatTestMailResult = true;
    new Ajax.Request('/csvn/server/testMailResult?iefix=' + new Date(), {
        method:'get',
        asynchronous: false,
        requestHeaders: {Accept: 'text/json'},
        onSuccess: function(transport) {
            repeatTestMailResult = false;
            var responseData = transport.responseText.evalJSON(true);
            var result = responseData.result;
            var msg;
            if (result == 'STILL_RUNNING') {
                msg = '<g:message code="server.action.testMail.stillRunning"/>';
                repeatTestMailResult = true;
            } else if (result == 'NOT_RUNNING') {
                resetPage();
                msg = '<g:message code="server.action.testMail.notRunning"/>';
            } else if (result == 'SUCCESS') {
                resetPage();
                $('requestmessages').innerHTML = '<div class="alert alert-success">' +
                        '<g:message code="server.action.testMail.success"
                            args="${[server.adminEmail]}"/></div>';
            } else if (result == 'FAILED') {
                resetPage();
                msg = responseData.errorMessage;
            } else {
                // shouldn't get here
                alert("Result = " + result);
            }
            if (msg != null) {
                $('requestmessages').innerHTML = '<div class="alert">' +
                        msg + '</div>';
               
            }
        }
    });
    if (repeatTestMailResult) {
        window.setTimeout(fetchResult, 20000);
    }
}

function resetPage() {
    $('cancelTestButton').id = 'testButton';
    $('testButton').name = '_action_testMail';
    $('testButton').value = '${message(code:'server.page.editMail.button.testSettings')}';
    $('testButton').onclick = testMailHandler;
    $('spinner').hide();
}
  </g:javascript>
</head>
<content tag="title">
  <g:message code="server.page.editMail.title" />
</content>

<g:render template="leftNav" />

<body>
  <g:render template="tabs" model="${[view: 'editMail']}" />

  <g:form method="post" name="serverForm" action="updateMail">
    <div>
    <g:hiddenField name="view" value="editMail"/>
    <g:hiddenField name="id" value="${config?.id}" />
    <g:hiddenField name="version" value="${config?.version}" />

  <g:set var="testResult" value="${session['email.test.result']}"/>
  <g:if test="${testResult}">
      <g:if test="${testResult.done}">Test finished <!-- Should be handled by controller --></g:if>
      <g:elseif test="${testResult.cancelled}"><!-- Will be handled by controller, should not happen -->${message(code: 'server.action.testMail.cancelled')}</g:elseif>
      <g:else>
        <g:javascript>
            Event.observe(window, 'load', fetchResult);
        </g:javascript>
      </g:else>
    </div>
  </g:if>
  
<p><g:message code="server.page.editMail.intro" args="${[server.adminEmail]}"/></p>

    <div id="enabledContainer">
      <g:checkBox name="enabled" value="${config?.enabled}" disabled="${!config?.enabled && invalidAdminEmail}"/>
      <label for="enabled"><g:message code="mailConfiguration.enabled.label" /></label>
      <g:if test="${invalidAdminEmail}">
        <div class="alert"><g:message code="mailConfiguration.enabled.invalidAdminEmail" 
                                            args="${[createLink(action: 'edit')]}"/>
      </div>
      </g:if>
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
      <table class="ItemDetailContainer">
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
            <img id="spinner" class="spinner" src="/csvn/images/spinner-gray-bg.gif" alt="Testing connection..."/>
            <g:set var="saveDisabled" value=""/>
            <g:if test="${testResult}">
               <g:actionSubmit action="cancelTestMail" value="${message(code:'server.page.editMail.button.cancelTest')}" 
                       class="Button" id="cancelTestButton"/>
               <g:set var="saveDisabled" value="disabled='disabled'"/>
            </g:if>
            <g:else>
                <g:actionSubmit action="testMail" value="${message(code:'server.page.editMail.button.testSettings')}" 
                        class="Button requireEnabled" id="testButton"/>
                <g:javascript>
                  $('spinner').hide();
                </g:javascript>        
            </g:else>
                <g:actionSubmit action="updateMail" value="${message(code:'server.page.edit.button.save')}" 
                        class="Button" id="saveButton" ${saveDisabled} />
          </div>
        </td>
      </tr>
                        
    </tbody>
  </table>
</div>
</div>
</g:form>
<g:javascript>
toggleConfigFields();
</g:javascript>
</body>
</html>
