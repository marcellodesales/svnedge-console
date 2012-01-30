<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title><g:message code="repository.page.editAuthorization.title" /></title>
      <g:javascript library="prototype" />
    
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

<g:form action="saveAuthorization" method="post">
<table class="Container">
<tr class="ContainerHeader">
    <td colspan="2"><g:message code="repository.page.editAuthorization.header" /></td>
</tr>

<tr>
  <td colspan="2"><g:message code="repository.page.editAuthorization.lockMessage" /></td>
</tr>


    <tr class="prop">

        <td width="100%" valign="top" class="value">
            <textarea id="accessRules" name="accessRules" rows="25" cols="80" style="width: 100%">${fieldValue(bean:authRulesCommand,field:'accessRules')}</textarea>
        </td>
    </tr>

    <tr class="ContainerFooter">
        <td colspan="2">
            <div class="AlignRight">
                <g:actionSubmit id="cancel_button" action="cancelEditAuthorization" class="Button cancel" value="${message(code:'default.confirmation.cancel')}"/>
                <input id="save_button" class="Button save" type="submit" value="${message(code:'repository.page.editAuthorization.button.save')}"/>
            </div>

        </td>
    </tr>
</table>
</g:form>
<g:javascript>
var isAjaxCancel = true;
function stopAjaxCancel() {
    isAjaxCancel = false;
}
$('save_button').onclick = stopAjaxCancel;
$('cancel_button').onclick = stopAjaxCancel;

<!-- Safari wants a synchronous call for this to work, other browsers seem fine to send the request and move on -->
window.onbeforeunload = function() {
  if (isAjaxCancel) {
    new Ajax.Request('/csvn/repo/cancelEditAuthorization', {
        asynchronous:false,
        method:'get',
        requestHeaders: {Accept: 'text/html'},
        onSuccess: function(transport) {
          // just send the request, don't need the result
        }
    });
  }
}
</g:javascript>

</body>
</html>
