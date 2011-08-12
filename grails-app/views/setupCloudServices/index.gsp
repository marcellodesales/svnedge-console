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
</head>
<content tag="title">
  <g:message code="setupCloudServices.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>

<body>
<table class="ItemDetailContainer">
  <tr class="ContainerHeader">
    <td colspan="2"><g:message code="setupCloudServices.page.index.title"/></td>
  </tr>
  <tr>
    <td class="ContainerBodyWithPaddedBorder">
      <p><g:message code="setupCloudServices.page.index.p1"/></p>

      <p><g:message code="setupCloudServices.page.index.p2"/></p>

      <p><g:message code="setupCloudServices.page.index.services.header"/>
      <ul>
        <li><g:message code="setupCloudServices.page.index.service.1"/></li>
        <li><g:message code="setupCloudServices.page.index.service.2"/></li>
      </ul>
    </p>
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
