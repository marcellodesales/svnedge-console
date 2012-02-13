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

  p.ServiceDetail, ol.ServiceDetail {
    font-size: .96em;
    vertical-align: top;
    text-align: left;
  }

  p.ServiceDetail {
    margin-left: 1em;
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
      <g:message code="setupCloudServices.page.confirmation.title"/>
    </td>
  </tr>
  <tr>
    <td class="CloudServicesSubHeading"><g:message code="setupCloudServices.page.confirmation.p1"/>
    </td>
  </tr>
  <tr>
    <td class="CloudServicesSubHeading">
    <ol>
      <li><g:message code="setupCloudServices.page.confirmation.service.backup.detail.step1"/></li>
      <li><g:message code="setupCloudServices.page.confirmation.service.backup.detail.step2"/></li>
      <li><g:message code="setupCloudServices.page.confirmation.service.backup.detail.step3"/></li>
    </ol>
    </td>
  </tr>
  <tr>
    <td class="CloudServicesBody">

      <table id="ServiceList" width="100%">
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.backup"/></td>
          <td width="60%"><img width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudBackup.png')}" border="0"/></td>
          <td width="20%"><p class="ServiceDetail"><g:message code="setupCloudServices.page.confirmation.service.backup.detail"/></p></td>
        </tr>
      </table>

    </td>
  </tr>
  <tr class="ContainerFooter">
    <td>
        <div class="AlignLeft">
          <p id="GetStartedPrompt"><g:message code="setupCloudServices.page.confirmation.getStarted.prompt"/></p>
        </div>
    </td>
  </tr>
</table>

</body>
</html>
