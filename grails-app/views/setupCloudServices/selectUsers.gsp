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
  <g:message code="setupCloudServices.page.selectUsers.title"/>
</content>

<g:render template="/server/leftNav"/>
<body>

<g:set var="tabArray"
       value="${[[active: true, action:'selectUsers', label: message(code:'setupCloudServices.page.tabs.selectUsers', args:[1])]]}"/>
<g:set var="tabArray"
       value="${tabArray << [label: message(code:'setupCloudServices.page.tabs.createLogins', args:[2])]}"/>
<g:render template="/common/tabs" model="${[tabs: tabArray]}"/>

<g:form>
  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder" colspan="2">
        <p>
          <g:message code="setupCloudServices.page.selectUsers.p1"/>
        </p>
        <table class="Container">
          <tbody>
          <tr class="ItemListHeader">
            <th><g:listViewSelectAll/></th>
            <g:sortableColumn property="username" titleKey="setupCloudServices.page.selectUsers.username"
                              defaultOrder="asc"/>
            <g:sortableColumn property="realUserName" titleKey="setupCloudServices.page.selectUsers.realUsername"/>
            <g:sortableColumn property="email" titleKey="setupCloudServices.page.selectUsers.emailAddress"/>
            <th><g:message code="setupCloudServices.page.selectUsers.matchingRemoteUser"/></th>
          </tr>
          <g:each in="${userList}" status="i" var="user">
            <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
              <td><g:listViewSelectItem item="${user}" property="userId" selected="${user.selectForMigration}"/></td>
              <td>${user.username}</td>
              <td>${user.realUserName}</td>
              <td>${user.email}</td>
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
      <td>
        <div class="paginateButtons">
          <g:paginate total="${userListTotal}" />
        </div>
      </td>
      <td>
        <div class="AlignRight">
          <g:listViewActionButton action="createUserLogins" minSelected="1">
            <g:message code="setupCloudServices.page.selectUsers.createLogins"/>
          </g:listViewActionButton>
        </div>
      </td>
    </tr>
  </table>
</g:form>

</body>
</html>
