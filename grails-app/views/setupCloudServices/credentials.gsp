%{--
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
<html>
<head>
  <title>CollabNet Subversion Edge <g:message code="setupCloudServices.page.credentials.title"/></title>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
  <g:javascript library="window"/>
  <g:javascript library="prototype/effects"/>
  <g:javascript library="window_effects"/>
  <link rel="stylesheet" href="${resource(dir:'js/themes',file:'default.css')}" type="text/css"/>
  <link rel="stylesheet" href="${resource(dir:'js/themes',file:'lighting.css')}" type="text/css"/>
  <style type="text/css">
  
  td.CloudServicesBody {
    border-spacing: 0;
    padding: 4px;
  }

  table#ServiceList tr td {
    font-size: 1.50em;
    vertical-align: middle;
    text-align: center;
  }

  p.ServiceDetail, ol.ServiceDetail {
    font-size: .96em;
    vertical-align: top;
    text-align: left;
  }

  p.ServiceDetail {
    margin-left: 1em;
  }
  
  </style>
  <g:javascript>
  <!--
  // PRELOADING IMAGES
  var freeTrialButton = new Image();
  freeTrialButton.src="${resource(dir:'images/cloud',file:'freeTrialButton.png')}";
  var freeTrialButtonPressed = new Image();
  freeTrialButtonPressed.src="${resource(dir:'images/cloud',file:'freeTrialButton-pressed.png')}";

  // javascript for confirmation dialog box to remove credentials
  var i18n = {
    _confirmOkLabel: "${message(code:'default.confirmation.ok')}",
    _confirmCancelLabel: "${message(code:'default.confirmation.cancel')}",
    _message: "${message(code:'setupCloudServices.page.credentials.button.remove.confirm')}"
  }

  Event.observe(window, 'load', function() {
    $('btnCloudServicesRemove').observe('click', function(e) {
      // stop this button click from submitting form
      Event.stop(e)
      // confirm dialog, with callback functions for "ok" and "cancel"
      dialog(i18n,
        function() {
          // OkHandler. On "ok", submit the form
          // Submits the form with the original button properties transferred to a hidden field,
          // to simulate the button click and thereby activate Grails dispatcher
          var s = Event.element(e)
          var action = new Element('input', { type: 'hidden',  name: s.readAttribute('name'), value: s.readAttribute('value') });
          var theForm = s.up('form');
          theForm.appendChild(action);
          theForm.submit();
        },
        function() {
          // CancelHandler. On cancel, do nothing
          return
        })
    })
  })
  //-->
  </g:javascript>
</head>
<content tag="title">
  <g:message code="setupCloudServices.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>
<body>
<g:form>
  <table class="ItemDetailContainer">
    <tr class="ContainerHeader">
      <td><g:message code="setupCloudServices.page.credentials.title"/></td>
    </tr>
    <tr>
      <td class="ContainerBodyWithPaddedBorder">
        <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="domain"><g:message code="setupCloudServices.page.signup.domain.label"/></label>
            </td>
            <td valign="top">
              <g:if test="${!existingCredentials}">
                <input size="40" type="text" id="domain" name="domain"
                       value="${fieldValue(bean: cmd, field: 'domain')}"/>
              </g:if>
              <g:else>
                <b>${fieldValue(bean: cmd, field: 'domain')}</b>
              </g:else>

            </td>
            <td></td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="domain">
                <ul><g:eachError bean="${cmd}" field="domain">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="username"><g:message
                      code="setupCloudServices.page.signup.username.label"/></label>
            </td>
            <td valign="top">
              <g:if test="${!existingCredentials}">
                <input size="40" type="text" id="username" name="username"
                       value="${fieldValue(bean: cmd, field: 'username')}"/>
              </g:if>
              <g:else>
                <b>${fieldValue(bean: cmd, field: 'username')}</b>
              </g:else>
            </td>
            <td></td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="username">
                <ul><g:eachError bean="${cmd}" field="username">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="password"><g:message
                      code="setupCloudServices.page.signup.password.label"/></label>
            </td>
            <td valign="top">
              <g:passwordFieldWithChangeNotification name="password"
                                                     value="${fieldValue(bean:cmd,field:'password')}"
                                                     size="40"/>
            </td>
            <td></td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="password">
                <ul><g:eachError bean="${cmd}" field="password">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr class="ContainerFooter">
      <td>
        <div class="AlignRight">
          <g:if test="${existingCredentials}">
            <g:actionSubmit id="btnCloudServicesRemove"
                            value="${message(code:'setupCloudServices.page.credentials.button.remove')}"
                            action="removeCredentials" class="Button"/>
          </g:if>
          <g:actionSubmit id="btnCloudServicesValidate"
                          value="${message(code:'setupCloudServices.page.credentials.button.validate')}"
                          action="updateCredentials" class="Button"/>

        </div>
      </td>
    </tr>
  </table>
</g:form>

<g:if test="${existingCredentials}">
<table class="ItemDetailContainer">
  <tr>
    <td class="CloudServicesBody">

      <table id="ServiceList" width="100%">

        <tr>
          <td colspan="3"><hr/></td>
        </tr>
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.backup"/></td>
          <td width="60%"><img width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudBackup.png')}" border="0"/></td>
          <td width="20%">
            <p class="ServiceDetail"><g:message code="setupCloudServices.page.confirmation.nextSteps.1"/></p>
            <p class="ServiceDetail"><g:message code="setupCloudServices.page.confirmation.nextSteps.2"/></p>
          </td>
        </tr>
        <tr>
          <td colspan="3"><hr/></td>
        </tr>
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.migrate"/></td>
          <td width="60%"><img width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudMigrate.png')}" border="0"/></td>
          <td width="20%"><p class="ServiceDetail"><g:message code="setupCloudServices.page.index.service.migrate.detail"/></p>
            <g:link url="https://app.codesion.com/ajax#signup?mode=demo&source=svnedge" target="_blank"
                      onmousedown="\$('freeTrial').src=freeTrialButtonPressed.src"
                      onmouseup="\$('freeTrial').src=freeTrialButton.src">
              <img id="freeTrial" align="right" alt="${message(code:'setupCloudServices.page.index.button.moveToCloud')}" src="${resource(dir:'images/cloud',file:'freeTrialButton.png')}" border="0"/>
            </g:link>
          </td>
        </tr>
        
      </table>
    </td>
  </tr>
</table>
</g:if>

</body>
</html>
