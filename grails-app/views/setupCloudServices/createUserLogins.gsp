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
  <g:javascript library="application"/>

  <script type="text/javascript">

    var usernameAvailableMessages = {
        prompt: '<g:message code="setupCloudServices.login.available.prompt"/>',
        checking: '<img src="/csvn/images/spinner-green.gif" alt="spinner" align="top"/> <g:message code="setupCloudServices.login.available.checking"/>',
        available: '<img src="/csvn/images/ok.png" alt="ok icon" align="top"/> <g:message code="setupCloudServices.login.available.yes"/>',
        notAvailable: '<img src="/csvn/images/attention.png" alt="problem icon" align="top"/> <g:message code="setupCloudServices.login.available.no"/>'
    }

    var usernameAvailabilityCheckers = []
    Event.observe(window, 'load', function() {
      $$('input.CheckForLoginUniqueness').each (function(s) {
        var usernameField = s
        var usernameMsgElement = s.next()
        var checker = new CloudLoginAvailabilityChecker(usernameField, usernameMsgElement)
        usernameAvailabilityCheckers.push(checker)
        checker.onSuccess = updateActionButtons
        checker.onFailure = updateActionButtons

        checker.doAjaxRequest(checker)
        Event.observe(usernameField, 'keydown', function(e) {
          checker.keypressHandler()
        })
      })
      updateActionButtons()
    })

  function updateActionButtons() {
    var enableActions = true
    for (var i=0; i < usernameAvailabilityCheckers.length; i++) {
      if (usernameAvailabilityCheckers[i].loginAvailable != true) {
        enableActions = false
        break
      }
    }
    $$('input.listViewAction').each(function(s) {
       s.disabled = !enableActions
    })
  }

  </script>
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
            <th><g:message code="setupCloudServices.page.selectUsers.username"/></th>
            <th><g:message code="setupCloudServices.page.selectUsers.realUsername"/></th>
            <th><g:message code="setupCloudServices.page.selectUsers.emailAddress"/></th>
            <th><g:message code="setupCloudServices.page.selectUsers.proposedLogin"/></th>
          </tr>
          <g:each in="${userList}" status="i" var="user">
            <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
              <td>${user.username}</td>
              <td>${user.realUserName}</td>
              <td>${user.email}</td>
              <td>
                <input size="40" type="text" id="username_${user.id}" name="username_${user.id}"
                  value="${fieldValue(bean: user, field: 'username')}"
                  class="CheckForLoginUniqueness"/>
                <span class="usernameUniqueneMessage" id="usernameUniqueMessage_${user.id}"></span>
              </td>
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
          <g:actionSubmit id="btnMigrateUsers"
                value="${message(code:'setupCloudServices.page.selectUsers.migrate')}"
                controller="setupCloudServices" action="migrateUsers" class="Button listViewAction"
                disabled="disabled"/>
        </div>
      </td>
    </tr>
  </table>
</g:form>


</body>
</html>
