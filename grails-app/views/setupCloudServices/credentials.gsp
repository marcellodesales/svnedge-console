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
  <title>CollabNet Subversion Edge </title>
  <meta name="layout" content="main"/>
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
  //-->
  </g:javascript>
</head>
<content tag="title"><g:message code="setupCloudServices.page.credentials.title"/></content>

<g:render template="/server/leftNav"/>
<body>
<g:form class="form-horizontal">

  <g:if test="${existingCredentials}">
    <div class="control-group required-field">
      <span class="control-label"><g:message code="setupCloudServices.page.signup.domain.label"/></span>
      <div class="controls readonly">${cmd.domain}</div>
    </div>
    <div class="control-group required-field">
      <span class="control-label"><g:message code="setupCloudServices.page.signup.username.label"/></span>
      <div class="controls readonly">${cmd.username}</div>
    </div>    
  </g:if>
  <g:else>
    <g:propTextField bean="${cmd}" field="domain" required="true" prefix="setupCloudServices.page.signup"/>
    <g:propTextField bean="${cmd}" field="username" required="true" prefix="setupCloudServices.page.signup"/>
  </g:else>

  <g:propControlsBody bean="${cmd}" field="password" required="true" prefix="setupCloudServices.page.signup">
    <g:passwordFieldWithChangeNotification name="password"
      value="${fieldValue(bean:cmd,field:'password')}"/>
  </g:propControlsBody>
  <div class="form-actions">  
          <g:actionSubmit id="btnCloudServicesValidate"
                          value="${message(code:'setupCloudServices.page.credentials.button.validate')}"
                          action="updateCredentials" class="btn btn-primary"/>
          <g:if test="${existingCredentials}">
            <g:actionSubmit id="btnCloudServicesRemove"
                            value="${message(code:'setupCloudServices.page.credentials.button.remove')}"
                            action="removeCredentials" class="btn" data-toggle="modal" data-target="#confirmDelete"/>
          </g:if>
  </div>
  
      <div id="confirmDelete" class="modal hide fade" style="display: none">
        <div class="modal-header">
          <a class="close" data-dismiss="modal">&times;</a>
          <h3><g:message code="default.confirmation.title"/></h3>
        </div>
        <div class="modal-body">
          <p><g:message code="setupCloudServices.page.credentials.button.remove.confirm"/></p>
        </div>
        <div class="modal-footer">
          <a href="#" class="btn btn-primary ok" 
             onclick="formSubmit($('#btnCloudServicesRemove').closest('form'), '/csvn/setupCloudServices/removeCredentials')">${message(code: 'default.confirmation.ok')}</a>
          <a href="#" class="btn cancel" data-dismiss="modal">${message(code: 'default.confirmation.cancel')}</a>
        </div>
      </div>  
</g:form>

<g:if test="${existingCredentials}">
<table>
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
