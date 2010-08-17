<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>CollabNet Subversion Edge Replication: View Cache</title>
    <meta name="layout" content="main" />
    <meta http-equiv="refresh" content="${uiRefreshRate}" />
  </head>

  <content tag="title">
    CollabNet Subversion Edge User Cache Info
  </content>

  <body>
    <div class="message">
      The current cache returned for the domain ${domainName}.
    </div>
    <div>
      Update Authentication Cache:
      <g:form controller="userCache" action="authUser" method="post">   
        Username: <input type="text" name="username" class="is_required"/>
        Password: <input type="text" name="password" class="is_required"/> 
        <input type="submit" value="Verify User/Password"/>
      </g:form>
    </div>

    <g:if test="${authCache}">
      <table class="ItemListTable">
        <tr class="ItemListHeader">
          <td>Username</td>
          <td>Password</td>
          <td>Is Valid?</td>
          <td>Expires On</td>
          <td>Authorization Details</td>
        </tr>
        <g:each var="authKey" in="${authCache.keySet()}" status="rowNumber">
          <tr class="${ (rowNumber % 2) == 0 ? 'a' : 'b'}">
            <td>${authKey.getUsername()}</td><td>${authKey.getPassword()}</td>
            <td>${authCache.get(authKey).responseValue}</td>
            <td>${authCache.get(authKey).getHumanReadableExpiration()}</td>
            <td>
              <g:if test="${authCache.get(authKey).responseValue}">
                <g:form controller="userCache" action="oauthUser" method="post">
                  <input type="hidden" name="username"
                         value="${authKey.getUsername()}"/>
                  <input type="submit"
                         value="Authorize ${authKey.getUsername()}"/>
                  on project: <input type="text" name="projectName" size="10"
                                     class="is_required"/>
                  for action <input type="text" name="actionName"
                                    class="is_required"/>
                </g:form>
                <g:each var="oauthKey" in="${oauthCache.keySet()}"
                        status="oathrowNumber">
                  <g:if test="${oauthKey.containsUsername(authKey.username)}">
                    <li><b>Key:</b> ${oauthKey}<BR>
                    &nbsp;&nbsp;&nbsp;&nbsp;<b>Value</b>:
                    ${oauthCache.get(oauthKey)}
                  </g:if>
                </g:each>
              </g:if>
              <g:else>
                No Action Allowed: User not successfully authenticated!
              </g:else>
            </td>
          </tr>
        </g:each>
      </table>
    </g:if>
    <g:else>
      <div class="warning">
        The Users Cache is empty.
      </div>
    </g:else>
  </body>
</html>
