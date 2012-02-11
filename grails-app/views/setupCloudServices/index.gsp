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
  <title>CollabNet Subversion Edge <g:message code="setupCloudServices.page.index.title"/></title>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
  <g:javascript>
  <!--
  // PRELOADING IMAGES
    var freeBackupButton = new Image();
    freeBackupButton.src="${resource(dir:'images/cloud',file:'freeBackupButton.png')}";
    var freeTrialButton = new Image();
    freeTrialButton.src="${resource(dir:'images/cloud',file:'freeTrialButton.png')}";
    var freeBackupButtonPressed = new Image();
    freeBackupButtonPressed.src="${resource(dir:'images/cloud',file:'freeBackupButton-pressed.png')}";
    var freeTrialButtonPressed = new Image();
    freeTrialButtonPressed.src="${resource(dir:'images/cloud',file:'freeTrialButton-pressed.png')}";
  //-->
  </g:javascript>
  <style>

  td.CloudServicesHeading, td.CloudServicesSubHeading {
    border-collapse: collapse;
    padding: 3px 20px;
    font-size: 1.75em;
    font-weight: bold;
    color: #69c;
  }

  td.CloudServicesSubHeading  {
    font-size: 1.27em;
    font-weight: normal;
    color: black;
    padding: 3px 20px;
  }

  td.CloudServicesBody {
    border-spacing: 0;
    padding: 4px;
  }

  p#GetStartedPrompt {
    font-size: 1.50em;
    vertical-align: top;
    text-align: left;
    padding: 4px;
  }

  table#ServiceList tr td {
    font-size: 1.50em;
    vertical-align: middle;
    text-align: center;
  }

  p.ServiceDetail {
    font-size: .96em;
    vertical-align: top;
    text-align: center;
  }

  </style>
</head>
<content tag="title">
  <g:message code="setupCloudServices.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>

<body>
<table class="ItemDetailContainer">
  <tr>
    <td class="CloudServicesHeading">
      <g:message code="setupCloudServices.page.index.title"/>
    </td>
  </tr>
  <tr>
    <td class="CloudServicesBody">

      <table id="ServiceList">
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.backup"/></td>
          <td width="60%"><img width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudBackup.png')}" border="0"/></td>
          <td width="20%"><p class="ServiceDetail"><g:message code="setupCloudServices.page.index.service.backup.detail"/></p>
            <g:link controller="setupCloudServices" action="getStarted"
                      onmousedown="\$('freeBackup').src=freeBackupButtonPressed.src"
                      onmouseup="\$('freeBackup').src=freeBackupButton.src">
              <img id="freeBackup" align="right" alt="${message(code:'setupCloudServices.page.index.button.continue')}" src="${resource(dir:'images/cloud',file:'freeBackupButton.png')}" border="0"
                      />
<!-- 
              foo<img align="right" alt="${message(code:'setupCloudServices.page.index.button.continue')}" src="${resource(dir:'images/misc',file:'pixel.gif')}" width="252" height="52" border="0"/>
             -->
             </g:link>
          </td>
        </tr>
        <tr>
          <td colspan="3"><hr/></td>
        </tr>
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.migrate"/></td>
          <td width="60%"><img width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudMigrate.png')}" border="0"/></td>
          <td width="20%"><p class="ServiceDetail"><g:message code="setupCloudServices.page.index.service.migrate.detail"/></p>
            <g:link url="https://app.codesion.com/ajax#signup?mode=demo&source=svnedge" 
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

</body>
</html>
