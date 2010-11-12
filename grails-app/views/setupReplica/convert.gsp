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

<g:set var="tabArray" value="${[[label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}"/>
<g:set var="tabArray" value="${tabArray << [label: message(code:'setupReplica.page.tabs.ctfInfo', args:[2])]}"/>
<g:set var="tabArray" value="${tabArray << [label: message(code:'setupReplica.page.tabs.replicaInfo', args:[3])]}"/>
<g:set var="tabArray" value="${tabArray << [label: message(code:'setupReplica.page.tabs.confirm', args:[4])]}"/>
<g:set var="tabArray" value="${tabArray << [active: true, label: message(code:'setupReplica.page.tabs.convert', args:[5])]}"/>


<g:render template="/common/tabs" model="${[tabs: tabArray]}"/>

<g:form method="post">

  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <p>
          <g:message code="setupReplica.page.convert.p1"/>
        </p>

        <div class="dialog">
          <table align="center" width="99%">
            <tbody>
            <tr><td>
              <table class="ItemDetailContainer">
                <tbody>
                <tr class="ContainerHeader">
                  <td colspan="2"><g:message code="status.page.header.server"/></td>
                </tr>
                
                <tr class="prop, OddRow">
                  <td class="ItemDetailName"><strong><g:message code="setupReplica.page.confirm.ctfURL.label"/></strong></td>
                  <td class="ItemDetailValue">${ctfURL}</td>
                </tr>
                
                <tr class="prop, EvenRow">
                   <td class="ItemDetailName"><strong><g:message code="setupReplica.page.ctfInfo.ctfUsername.label"/></strong></td>
                   <td class="ItemDetailValue">${ctfUsername}</td>
                 </tr>
                  
                
                <tr class="prop, OddRow">
                  <td class="ItemDetailName"><strong><g:message code="setupReplica.page.confirm.svnMasterURL.label"/></strong></td>
                  <td class="ItemDetailValue">${svnMasterURL}</td>
                </tr>
                
                <tr class="prop, EvenRow">
                  <td class="ItemDetailName"><strong><g:message code="setupReplica.page.confirm.svnReplicaCheckout.label"/></strong></td>
                  <td class="ItemDetailValue">${svnReplicaCheckout}</td>
                </tr>
                
                </tbody>
              </table>
            </td></tr>
            </tbody>
          </table>
        </div>


    
  </table>
  </td>
  </tr>
</g:form>

</body>
</html>
  
