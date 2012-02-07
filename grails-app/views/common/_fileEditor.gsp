%{--
  - CollabNet Subversion Edge
  - Copyright (C) 2012, CollabNet Inc. All rights reserved.
  -
  - This program is free software: you can redistribute it and/or modify
  - it under the terms of the GNU Affero General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU Affero General Public License for more details.
  -  
  - You should have received a copy of the GNU Affero General Public License
  - along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%

<g:form method="post">
  <g:hiddenField name="fileId" value="${fileId}"/>
  <table class="Container">
    <tr class="ContainerHeader">
      <td>${heading}</td>
    </tr>
    <tr>
      <td><g:message code="default.fileEditor.lockMessage" /></td>
    </tr>
    <tr class="prop">
      <td width="100%" valign="top" class="value">
        <textarea id="fileContent" name="fileContent" rows="25" cols="80" style="width: 99%">${fileContent}</textarea>
      </td>
    </tr>
    <tr class="ContainerFooter">
      <td>
        <div class="AlignRight">
          <g:actionSubmit id="cancel_button" action="${cancelAction}" class="Button cancel" value="${message(code:'default.confirmation.cancel')}"/>
          <g:actionSubmit id="save_button" action="${saveAction}" class="Button save" value="${message(code:'default.button.save.label')}"/>
        </div>
      </td>
    </tr>
  </table>
</g:form>
<g:if test="${ajaxCancelUrl}">
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
      new Ajax.Request('${ajaxCancelUrl}', {
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
</g:if>
