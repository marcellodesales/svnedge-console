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

<%@ page import="com.collabnet.svnedge.domain.ServerMode" %>
<html>
<head>
  <meta name="layout" content="main"/>
  <g:javascript library="prototype"/>
</head>
<content tag="title">
  <g:message code="setupTeamForge.page.leftNav.header"/>
</content>

<g:render template="/server/leftNav"/>

<body>

   <g:set var="tabArray" value="${[[controller: 'server', action: 'editIntegration', label: message(code:'server.page.editIntegration.tab.convert')]]}" />
   <g:set var="tabArray" value="${ tabArray << [active: true, controller: 'setupReplica', action: 'editCredentials', label: message(code:'server.page.editIntegration.tab.edit')]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

<g:form method="post">

  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <p>
          <g:if test="${isReplica}">
            <g:message code="setupReplica.page.editCredentials.p1"/>
          </g:if>
        </p>

        <table class="ItemDetailContainer">
         <g:if test="${isReplica}">
          <tr>
            <td class="ItemDetailName">
              <label for="ctfURL"><g:message code="setupReplica.page.ctfInfo.ctfURL.label"/></label>
            </td>
            <td valign="top" class="value">
              ${cmd.ctfURL}
              <g:hiddenField name="ctfURL" value="${cmd.ctfURL}"/>
            </td>
            <td>
              <em><g:message code="setupReplica.page.ctfInfo.ctfURL.label.tip"/></em>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="ctfURL">
                <ul><g:eachError bean="${cmd}" field="ctfURL">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="ctfUsername"><g:message code="setupReplica.page.ctfInfo.ctfUsername.label"/></label>
            </td>
            <td class="value ${hasErrors(bean: cmd, field: 'ctfUsername', 'errors')}">
              <g:textField name="ctfUsername" value="${cmd.ctfUsername}" size="20"/> 
            </td>
            <td>
              <em><g:message code="setupReplica.page.ctfInfo.ctfUsername.label.tip"/></em>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="ctfUsername">
                <ul><g:eachError bean="${cmd}" field="ctfUsername">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="ctfPassword"><g:message code="setupReplica.page.ctfInfo.ctfPassword.label"/></label>
            </td>
            <td class="value ${hasErrors(bean: cmd, field: 'ctfPassword', 'errors')}">
              <g:passwordField name="ctfPassword" value="${cmd.ctfPassword}" size="20"/>
            </td>
            <td>
            </td>
          </tr>
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="ctfPassword">
                <ul><g:eachError bean="${cmd}" field="ctfPassword">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>
     </g:if>
      <tr>
        <td class="ItemDetailName">
          <label for="serverKey"><g:message code="ctfConversionBean.serverKey.label" /></label>
        </td>
        <td valign="top" class="value ${hasErrors(bean: cmd, field: 'serverKey', 'errors')}">
          <input size="40" type="text" id="serverKey" name="serverKey" 
              value="${fieldValue(bean: cmd, field: 'serverKey')}"/>
        </td>
        <td class="ItemDetailValue"><em><g:message code="ctfConversionBean.serverKey.error.missing" /></em>
          <div>
            <g:message code="setupReplica.page.apiKey.description" />
            <ul>
              <li><g:message code="setupReplica.page.apiKey.hosted" /></li>
              <li><g:message code="setupReplica.page.apiKey.property" /></li>
            </ul>
          </div>
        </td>
      </tr> 
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <g:hasErrors bean="${cmd}" field="serverKey">
                <ul><g:eachError bean="${cmd}" field="serverKey">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
                </g:eachError></ul>
              </g:hasErrors>
            </td>
          </tr>

        </table>
      </td>
    </tr>

    <tr class="ContainerFooter">
      <td colspan="3">
        <div class="AlignRight">
        
          <g:actionSubmit action="updateCredentials" value="${message(code:'setupTeamForge.page.ctfInfo.button.continue')}" class="Button"/>
        </div>
      </td>
    </tr>

  </table>
</g:form>

</body>
</html>
  
