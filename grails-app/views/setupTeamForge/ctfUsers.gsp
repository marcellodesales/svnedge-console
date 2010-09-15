<html>
  <head>
    <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.ctfUsers.title" /></title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

    <script type="text/javascript">
    function showHide() {
      if (document.getElementById('importUsers').checked) {
        document.getElementById('userList').style.display = 'block';
      } else {
        document.getElementById('userList').style.display = 'none';
      }
    }
    </script>

  </head>
  <content tag="title">
    <g:message code="setupTeamForge.page.leftNav.header" />
  </content>

  <g:render template="/server/leftNav" />

  <body>

   <g:set var="tabArray" value="${[[action:'index', label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}" />
   <g:set var="tabArray" value="${tabArray << [action:'ctfInfo', label: message(code:'setupTeamForge.page.tabs.ctfInfo', args:[2])]}" />
   <g:set var="tabArray" value="${tabArray << [action:'ctfProject', label: message(code:'setupTeamForge.page.tabs.ctfProject', args:[3])]}" />
   <g:set var="tabArray" value="${tabArray << [active: true, label: message(code:'setupTeamForge.page.tabs.ctfUsers', args:[4])]}" />
   <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.confirm', args:[5])]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

<g:form method="post">
 <table class="ItemDetailContainer">
 <tr>
   <td class="ContainerBodyWithPaddedBorder">
 <g:if test="${server.ldapEnabled}">
      <div class="warningText">
      <g:message code="setupTeamForge.page.ctfUsers.p1" />
      </div>
 </g:if>
 <g:else>
    <g:if test="${existingUsers.size() == 0 && csvnOnlyUsers.size() == 0}">
      <p><g:message code="setupTeamForge.page.ctfUsers.noUsers" /></p>
    </g:if>
    <g:elseif test="${existingUsers.size() > 0 && csvnOnlyUsers.size() == 0}">
        <p><g:message code="setupTeamForge.page.ctfUsers.managedUsers" /></p>
    </g:elseif>
    <g:else>
        <g:checkBox id="importUsers" name="importUsers" value="true" 
            checked="${wizardBean.importUsers}" onclick="showHide()" />
        <label for="importUsers"><g:message code="setupTeamForge.page.ctfUsers.importUsers.label" /></label>
        <div id="userList"<g:if test="${!wizardBean.importUsers}"> style="display: none;"</g:if>>
          <label><g:checkBox name="assignMembership" id="assignMembership"
                    value="true" checked="${wizardBean.assignMembership}"/>
            <g:message code="setupTeamForge.page.ctfUsers.importUsers.assignMembership" /></label>&nbsp;&nbsp;
            <em>
               <g:message code="setupTeamForge.page.ctfUsers.importUsers.addMembershipTo" args="${wizardBean.ctfProject ? [1, wizardBean.ctfProject] : [2]}"/>
            </em>
          <p>
          <g:if  test="${existingUsers.size() == 0}">
             <g:message code="setupTeamForge.page.ctfUsers.importUsers.noConflicts" />
          </g:if>
          <g:else>
             <g:message code="setupTeamForge.page.ctfUsers.importUsers.someExists" />
          </g:else>
          </p>
        <g:if  test="${existingUsers.size() > 0}">
         <table width="100%" border="1">
          <tr><th><g:message code="setupTeamForge.page.ctfUsers.column.existingUsers" /></th><th><g:message code="setupTeamForge.page.ctfUsers.column.toImport" /></th></tr>
          <tr>
          <td>
            <ul>
              <g:each var="user" in="${existingUsers}">
                <li>${user}</li>
              </g:each>
            </ul>
          </td>
          <td>
            <ul>
              <g:each var="user" in="${csvnOnlyUsers}">
                <li>${user}</li>
              </g:each>
            </ul>
          </td>
          </tr>
         </table>
        </g:if>
        </div>
    </g:else>
  </g:else>
   </td>
   </tr>
       <tr class="ContainerFooter">
         <td >
           <div class="AlignRight">
                 <g:actionSubmit action="updateUsers" value="${message(code:'setupTeamForge.page.ctfUsers.button.continue')}" class="Button"/>
             </div>
         </td>
       </tr>
 </table>
      </g:form>
    </body>
</html>
  
