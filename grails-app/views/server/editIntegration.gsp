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
    <title>CollabNet Subversion Edge <g:message code="server.page.editIntegration.title" /></title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

  </head>
  <content tag="title">
    <g:message code="server.page.editIntegration.leftNav.header" />
  </content>

  <g:render template="leftNav" />

  <body>

   <g:set var="tabArray" value="${[[active:true, label: message(code:'server.page.editIntegration.tab.convert')]]}" />
   <g:set var="tabArray" value="${ tabArray << [controller: 'setupReplica', action: 'editCredentials', label: message(code:'server.page.editIntegration.tab.edit')]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

           <p>
            <g:message code="server.page.editIntegration.p1" args="${['<strong><i>' + ctfServerBaseUrl + '</i></strong>']}" />
           </p>
            <ul>
                <li><g:message code="server.page.editIntegration.bullet1" /></li>
                <li><g:message code="server.page.editIntegration.bullet2" />
            </ul>

            <p><g:message code="server.page.editIntegration.p3" /></p>
            <ul>
               <li><g:message code="server.page.editIntegration.bullet3" /></li>
            </ul>
      </td></tr>
      <tr>
        <td class="ItemDetailContainerCell">

          <g:if test="${formError}">
            <div class="errorMessage">
                ${formError}
                <g:if test="${errorCause}">
                    <ul>
                        <li>${errorCause}</li>
                    </ul>
                </g:if>
            </div>
          </g:if>

    <g:form method="post" action="revert">

      <table class="ItemDetailContainer">
      <tr><td colspan="3">&nbsp;</td></tr>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfURL"><g:message code="server.page.editIntegration.ctfUrl.label" /></label>
        </td>
        <td valign="top" class="value" colspan="2">${ctfServerBaseUrl}</td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfUsername"><g:message code="server.page.editIntegration.ctfUsername.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:ctfCredentials,field:'ctfUsername','errors')}">
          <input size="20" type="text" id="ctfUsername" name="ctfUsername" 
              value="${fieldValue(bean:ctfCredentials,field:'ctfUsername').replace(',','')}"/>
        </td>
        <td class="ItemDetailValue">
          <em><g:message code="server.page.editIntegration.ctfUsername.label.tip" /></em>
         </td>
      </tr>
   <g:hasErrors bean="${ctfCredentials}" field="ctfUsername">
      <tr>
         <td></td>
         <td class="errors" colspan="2">
              <ul><g:eachError bean="${ctfCredentials}" field="ctfUsername">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
         </td>
      </tr>
   </g:hasErrors>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfPassword"><g:message code="server.page.editIntegration.ctfPassword.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:ctfCredentials,field:'ctfPassword','errors')}">
          <input size="20" type="password" id="ctfPassword" name="ctfPassword" 
              value="${fieldValue(bean:ctfCredentials,field:'ctfPassword')}"/>
        </td>
        <td></td>
      </tr>
   <g:hasErrors bean="${ctfCredentials}" field="ctfPassword">
      <tr>
         <td></td>
         <td class="errors" colspan="2">
              <ul><g:eachError bean="${ctfCredentials}" field="ctfPassword">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
         </td>
      </tr>
   </g:hasErrors>
      <tr><td colspan="3">&nbsp;</td></tr>
      <tr class="ContainerFooter">
        <td colspan="3">
          <div class="AlignRight">
                <g:actionSubmit action="revert" value="${message(code:'server.page.editIntegration.button.convert')}" class="Button"/>
          </div>
        </td>
      </tr>
     </table>
  </g:form>

 </td></tr></table>
  </body>
</html>
