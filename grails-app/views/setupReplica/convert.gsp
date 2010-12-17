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
                <tr>
                  <td class="ItemDetailName"><g:message code="setupReplica.page.confirm.ctfURL.label"/></td>
                  <td class="ItemDetailValue">${ctfURL}</td>
                </tr>
                
                <tr>
                   <td class="ItemDetailName"><g:message code="setupReplica.page.ctfInfo.ctfUsername.label"/></td>
                   <td class="ItemDetailValue">${ctfUsername}</td>
                 </tr>

                <tr>
                  <td class="ItemDetailName"><g:message code="setupReplica.page.confirm.svnReplicaCheckout.label"/></td>
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
  
