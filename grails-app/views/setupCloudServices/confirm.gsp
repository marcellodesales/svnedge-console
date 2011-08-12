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
  <title>CollabNet Subversion Edge

    <g:message code="setupCloudServices.page.confirmation.title"/></title>
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
    <td colspan="2"><g:message code="setupCloudServices.page.confirmation.title"/></td>
  </tr>
  <tr>
    <td class="ContainerBodyWithPaddedBorder">
      <p><g:message code="setupCloudServices.page.confirmation.p1"/></p>

      <g:render template="nextSteps"/>
    </td>
  </tr>
  <tr class="ContainerFooter">
    <td>

    </td>
  </tr>
</table>

</body>
</html>
