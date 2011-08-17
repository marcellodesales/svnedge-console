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
  table#CloudServices tr td {
    font-size: 1.27em;
    vertical-align: middle;
    text-align: center;
  }

  td.CloudServicesHeading, td.CloudServicesSubHeading {
    background-color: #006699;
    color: white;
    border-collapse: collapse;
    border: 1px solid #006699;
    border-top: 1px solid #006699;
    padding: 3px 5px;
    font-size: 1.50em;
    font-weight: bold;
  }

  td.CloudServicesSubHeading  {
    font-size: 1.27em;
    font-weight: normal;
  }

  td.CloudServicesSubHeading a, td.CloudServicesSubHeading a:visited {
    text-decoration: underline;
    color: white;
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
    <td class="CloudServicesSubHeading"><g:message code="setupCloudServices.page.index.p1"/></td>
  </tr>
  <tr>
    <td class="ContainerBodyWithPaddedBorder">

      <table id="CloudServices">
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.backup"/></td>
          <td width="60%"><img style="padding: 10px" width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudBackup.png')}" border="0"/></td>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.backup.detail"/></td>
        </tr>
        <tr>
          <td colspan="3"><hr/></td>
        </tr>
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.migrate"/></td>
          <td width="60%"><img style="padding: 10px" width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudMigrate.png')}" border="0"/></td>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.migrate.detail"/></td>
        </tr>
        <tr>
          <td colspan="3"><hr/></td>
        </tr>
        <tr>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.extend"/></td>
          <td width="60%"><img style="padding: 10px" width="400" height="150" alt="" src="${resource(dir:'images/cloud',file:'cloudExtend.png')}" border="0"/></td>
          <td width="20%"><g:message code="setupCloudServices.page.index.service.extend.detail"/></td>
        </tr>
      </table>

    </td>
  </tr>
  <tr class="ContainerFooter">
    <td>
      <g:form method="post">
        <div class="AlignRight">
          <g:actionSubmit id="btnCloudServicesGetStarted"
                          value="${message(code:'setupCloudServices.page.index.button.continue')}"
                          controller="setupCloudServices" action="getStarted" class="Button"/>
        </div>
      </g:form>
    </td>
  </tr>
</table>

</body>
</html>
