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
  <title>CollabNet Subversion Edge <g:message code="setupCloudServices.page.selectUsers.title"/></title>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
  <g:render template="/common/listViewResources"/>
</head>
<content tag="title">
  <g:message code="setupCloudServices.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>
<body>
<g:form>
  <table class="ItemDetailContainer">
    <tr class="ContainerHeader">
      <td><g:message code="setupCloudServices.page.selectUsers.title"/></td>
    </tr>
    <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <table class="Container">
          <tbody>
          <tr class="ItemListHeader">
            <th><g:listViewSelectAll/></th>
            <g:sortableColumn property="username" titleKey="setupCloudServices.page.selectUsers.username"
                              defaultOrder="asc"/>
            <g:sortableColumn property="realUsername" titleKey="setupCloudServices.page.selectUsers.realUsername"/>
            <g:sortableColumn property="emailAddress" titleKey="setupCloudServices.page.selectUsers.emailAddress"/>
            <g:sortableColumn property="matchingRemoteUser" titleKey="setupCloudServices.page.selectUsers.matchingRemoteUser"/>
          </tr>
          <g:each in="${userList}" status="i" var="user">
            <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
              <td><g:listViewSelectItem item="${user}" property="userId" selected="${user.selectForMigration}"/></td>
              <td>${user.username}</td>
              <td>${user.realUsername}</td>
              <td>${user.emailAddress}</td>
              <td>${user.matchingRemoteUser}</td>
            </tr>
          </g:each>
          <g:if test="${!userList}">
            <tr>
              <td colspan="5"><p><g:message code="setupCloudServices.page.selectUsers.noUsers"/></p></td>
            </tr>
          </g:if>
        </table>

      </td>
    </tr>
    <tr class="ContainerFooter">
      <td colspan="3">
        <div class="AlignRight">
          <g:listViewActionButton action="createUserLogins" minSelected="1" >
             <g:message code="setupCloudServices.page.selectUsers.createLogins"/>
           </g:listViewActionButton>
        </div>
      </td>
    </tr>
  </table>
</g:form>


</body>
</html>
