<html>
<head>
  <title>CollabNet Subversion Edge %{--
  - CollabNet Subversion Edge
  - Copyright (C) 2011, CollabNet Inc. All rights reserved.
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

    <g:message code="server.page.editProxy.title"/></title>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>

  <g:set var="editAuthConfirmMessage" value="${message(code:'server.page.edit.authentication.confirm')}"/>

  <g:javascript>

    var fieldsChanged = false;
    Event.observe(window, 'load', function() {
        // track field changes for "unsaved changes" alert
        var allInputs = Form.getElements("serverForm")
        allInputs.each(function(item){
            Event.observe(item, 'change', function(event) {
                fieldsChanged = true;
            });
        })
    });
  

    function warnForUnSavedData() {
      if (!fieldsChanged) {
        return true
      }
      return confirm("${editAuthConfirmMessage}");
    }

  </g:javascript>

</head>
<content tag="title">
  <g:message code="server.page.edit.header"/>
</content>

<g:render template="leftNav"/>

<body>
<g:set var="events" value="onclick='return warnForUnSavedData()'"/>
<g:set var="tabArray"
       value="${[[action: 'edit', events: events, label: message(code:'server.page.edit.tabs.general')]]}"/>
<g:if test="${!isManagedMode}">
  <g:set var="tabArray"
         value="${tabArray << [action: 'editAuthentication', events: events, label: message(code:'server.page.edit.tabs.authentication')]}"/>
</g:if>
<g:set var="tabArray"
       value="${tabArray << [action: 'editProxy', active: true, label: message(code:'server.page.edit.tabs.proxy')]}"/>
<g:render template="/common/tabs" model="${[tabs: tabArray]}"/>

<g:form method="post" name="serverForm" action="updateProxy">
  <g:hiddenField name="view" value="editProxy"/>

  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder">
        <p><g:message code="server.page.editProxy.message"/></p>
        <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="httpProxyHost"><g:message code="networkConfiguration.httpProxyHost.label"/></label>
            </td>
            <td valign="top" colspan="2"
                class="ItemDetailValue ${hasErrors(bean: networkConfig, field: 'httpProxyHost', 'errors')}">
              <input size="30" type="text" id="httpProxyHost" name="httpProxyHost"
                     value="${fieldValue(bean: networkConfig, field: 'httpProxyHost')}"/>
            </td>
          </tr>
          <g:hasErrors bean="${networkConfig}" field="httpProxyHost">
            <tr>
              <td></td>
              <td colspan="2" class="errors">
                <ul><g:eachError bean="${networkConfig}" field="httpProxyHost">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="httpProxyPort"><g:message code="networkConfiguration.httpProxyPort.label"/></label>
            </td>
            <td valign="top" colspan="2"
                class="ItemDetailValue ${hasErrors(bean: networkConfig, field: 'httpProxyPort', 'errors')}">
              <input size="6" type="text" id="httpProxyPort" name="httpProxyPort"
                     value="${fieldValue(bean: networkConfig, field: 'httpProxyPort')}"/>
            </td>
          </tr>
          <g:hasErrors bean="${networkConfig}" field="httpProxyPort">
            <tr>
              <td></td>
              <td colspan="2" class="errors">
                <ul><g:eachError bean="${networkConfig}" field="httpProxyPort">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="httpProxyUsername"><g:message code="networkConfiguration.httpProxyUsername.label"/></label>
            </td>
            <td valign="top" colspan="2"
                class="ItemDetailValue ${hasErrors(bean: networkConfig, field: 'httpProxyUsername', 'errors')}">
              <input size="30" type="text" id="httpProxyUsername" name="httpProxyUsername"
                     value="${fieldValue(bean: networkConfig, field: 'httpProxyUsername')}"/>
            </td>
          </tr>
          <g:hasErrors bean="${networkConfig}" field="httpProxyUsername">
            <tr>
              <td></td>
              <td colspan="2" class="errors">
                <ul><g:eachError bean="${networkConfig}" field="httpProxyUsername">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>

          <tr>
            <td class="ItemDetailName">
              <label for="httpProxyPassword"><g:message code="networkConfiguration.httpProxyPassword.label"/></label>
            </td>
            <td valign="top" colspan="2"
                class="ItemDetailValue ${hasErrors(bean: networkConfig, field: 'httpProxyPassword', 'errors')}">
              <input size="30" type="password" id="httpProxyPassword" name="httpProxyPassword"
                     value="${fieldValue(bean: networkConfig, field: 'httpProxyPassword')}"/>
            </td>
          </tr>
          <g:hasErrors bean="${networkConfig}" field="httpProxyPassword">
            <tr>
              <td></td>
              <td colspan="2" class="errors">
                <ul><g:eachError bean="${networkConfig}" field="httpProxyPassword">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </td>
            </tr>
          </g:hasErrors>
        </table>
        
      </td>
    </tr>
    <tr class="ContainerFooter">
      <td>
        <div class="AlignRight">
          <g:actionSubmit action="removeProxy" value="${message(code:'server.page.editProxy.button.clear')}"
                          class="Button"/>
          <g:actionSubmit action="updateProxy" value="${message(code:'server.page.editAuthentication.button.save')}"
                          class="Button"/>
        </div>
      </td>
    </tr>
  </table>
</g:form>
</body>
</html>
