<html>
<head>
  %{--
  - CollabNet Subversion Edge
  - Copyright (C) 2010, CollabNet Inc. All rights reserved.
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

  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
</head>
<content tag="title">
  <g:message code="setupTeamForge.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>

<body>

<g:set var="tabArray" value="${[[action:'index', label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}"/>
<g:set var="tabArray" value="${tabArray << [action:'ctfInfo', label: message(code:'setupReplica.page.tabs.ctfInfo', args:[2])]}"/>
<g:set var="tabArray" value="${tabArray << [active:true, label: message(code:'setupReplica.page.tabs.replicaInfo', args:[3])]}"/>
<g:set var="tabArray" value="${tabArray << [label: message(code:'setupReplica.page.tabs.confirm', args:[4])]}"/>


<g:render template="/common/tabs" model="${[tabs: tabArray]}"/>

<g:form method="post">

  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <p>
          <g:message code="setupReplica.page.replicaSetup.p1"/>
        </p>

        <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="svnMasterURL"><g:message code="setupReplica.page.replicaSetup.svnMasterURL.label"/></label>
            </td>
            <td valign="top" class="value" colspan="2">
              <select name="masterExternalSystemId">
                <g:each in="${integrationServers}" var="scmServer">
                    <option value="${scmServer.id}">${scmServer.title}</option>
                </g:each>
              </select>
            </td>
            <td></td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="svnMasterURL">
                <ul><g:eachError bean="${cmd}" field="svnMasterURL">
                  <li><g:message error="${it}"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="name"><g:message code="setupReplica.page.replicaSetup.name.label"/></label>
            </td>
            <td class="value ${hasErrors(bean: cmd, field: 'name', 'errors')}">
              <g:textField name="name" size="40"/>
            </td>
            <td>
              <em><g:message code="setupReplica.page.replicaSetup.name.label.tip"/></em>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="name">
                <ul><g:eachError bean="${cmd}" field="name">
                  <li><g:message error="${it}"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="description"><g:message code="setupReplica.page.replicaSetup.description.label"/></label>
            </td>
            <td class="value ${hasErrors(bean: cmd, field: 'description', 'errors')}">
              <textarea name="description" id="description" rows="5" cols="50">${cmd?.description}</textarea>
            </td>
            <td>
              <em><g:message code="setupReplica.page.replicaSetup.description.label.tip"/></em>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="description">
                <ul><g:eachError bean="${cmd}" field="description">
                  <li><g:message error="${it}"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="message"><g:message code="setupReplica.page.replicaSetup.message.label"/></label>
            </td>
            <td class="value ${hasErrors(bean: cmd, field: 'message', 'errors')}">
              <textarea name="message" id="message" rows="5" cols="50">${cmd?.message}</textarea>
            </td>
            <td>
              <em><g:message code="setupReplica.page.replicaSetup.message.label.tip"/></em>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="message">
                <ul><g:eachError bean="${cmd}" field="message">
                  <li><g:message error="${it}"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>

        </table>

    <tr class="ContainerFooter">
      <td colspan="3">
        <div class="AlignRight">
          <g:actionSubmit action="confirm" value="${message(code:'setupTeamForge.page.ctfInfo.button.continue')}" class="Button"/>
        </div>
      </td>
    </tr>

  </table>
  </td>
  </tr>
</g:form>

</body>
</html>
  
