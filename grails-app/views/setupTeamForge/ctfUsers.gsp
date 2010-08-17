<html>
  <head>
    <title>CollabNet TeamForge Integration</title>
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
    CollabNet TeamForge Integration
  </content>

  <g:render template="/server/leftNav" />

  <body>

   <g:render template="/common/tabs"
       model="[tabs:[
         [action:'index', label:'1. Introduction'],
         [action:'ctfInfo', label:'2. TeamForge Credentials'],
         [action:'ctfProject',label:'3. TeamForge Project'],
         [active: true, label:'4. TeamForge Users'],
         [label:'5. Convert to TeamForge mode']
         ]]" />

<g:form method="post">
 <table class="ItemDetailContainer">
 <tr>
   <td class="ContainerBodyWithPaddedBorder">
 <g:if test="${server.ldapEnabled}">
      <div class="warningText">
      This CollabNet Subversion instance is configured to use LDAP.  The conversion process will not import
      users which are stored in LDAP.
      </div>
 </g:if>
 <g:else>
    <g:if test="${existingUsers.size() == 0 && csvnOnlyUsers.size() == 0}">
      <p>There are no internally managed users.</p>
    </g:if>
    <g:elseif test="${existingUsers.size() > 0 && csvnOnlyUsers.size() == 0}">
        <p>CollabNet TeamForge already contains matching users for all the
        users in CollabNet Subversion.
        No users from CollabNet Subversion will be imported.</p>
    </g:elseif>
    <g:else>
        <g:checkBox id="importUsers" name="importUsers" value="true" 
            checked="${wizardBean.importUsers}" onclick="showHide()" />
        <label for="importUsers"> Import users into CollabNet TeamForge</label>
        <div id="userList"<g:if test="${!wizardBean.importUsers}"> style="display: none;"</g:if>>
          <label><g:checkBox name="assignMembership" id="assignMembership"
                    value="true" checked="${wizardBean.assignMembership}"/>
                 Assign membership</label>&nbsp;&nbsp;
                 <em>Adds the imported users as members in the 
                  <g:if test="${wizardBean.ctfProject}">'${wizardBean.ctfProject}' project</g:if><g:else>project(s)</g:else>
                    created to house the imported repositories
                </em>
          <p>
          <g:if  test="${existingUsers.size() == 0}">
            There are no conflicting usernames between CollabNet 
            TeamForge and CollabNet Subversion. All users from CollabNet 
            Subversion will be imported. Users will need to use "Forgot 
            Your Password" link to receive a ticket to set their 
            new password.
          </g:if>
          <g:else>
            Some of the users in CollabNet Subversion already exist in 
            TeamForge.  Others will be imported into CollabNet TeamForge 
            during the conversion. The imported users will need to use 
            "Forgot Your Password" link to receive a ticket to set their 
            new password.
          </g:else>
          </p>
        <g:if  test="${existingUsers.size() > 0}">
         <table width="100%" border="1">
          <tr><th>Existing TeamForge users</th><th>Users to be imported</th></tr>
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
                 <g:actionSubmit action="updateUsers" value="Continue" class="Button"/>
             </div>
         </td>
       </tr>
 </table>
      </g:form>
    </body>
</html>
  
