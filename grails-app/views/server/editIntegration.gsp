<html>
  <head>
    <title>CollabNet Subversion Edge Configuration</title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

  </head>
  <content tag="title">
    Integration Administration
  </content>

  <g:render template="leftNav" />

  <body>

      <g:render template="/common/tabs"
          model="[tabs:[
            [active:true, label:'Convert to Standalone mode']]]" />

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

           <p>This Subversion Edge server is currently being managed by the TeamForge server 
           <strong><i>${ctfServerBaseUrl}</i></strong>, and therefore:</p>
            <ul>
                <li>All users' credentials, roles and access permissions are managed by TeamForge;</li>
                <li>All repositories are managed from TeamForge, although they reside on this server.
            </ul>

            <p>After reverting to Standalone mode:</p>
            <ul>
               <li>Local user authentication is used.</li>
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
          <label for="ctfURL">TeamForge server URL:</label>
        </td>
        <td valign="top" class="value" colspan="2">${ctfServerBaseUrl}</td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfUsername">TeamForge administrator username:</label>
        </td>
        <td class="value ${hasErrors(bean:ctfCredentials,field:'ctfUsername','errors')}">
          <input size="20" type="text" id="ctfUsername" name="ctfUsername" 
              value="${fieldValue(bean:ctfCredentials,field:'ctfUsername').replace(',','')}"/>
        </td>
        <td class="ItemDetailValue">
          <em>Account must have permission to remove integration servers</em>
         </td>
      </tr>
   <g:hasErrors bean="${ctfCredentials}" field="ctfUsername">
      <tr>
         <td></td>
         <td class="errors" colspan="2">
              <ul><g:eachError bean="${ctfCredentials}" field="ctfUsername">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
         </td>
      </tr>
   </g:hasErrors>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfPassword">TeamForge administrator password:</label>
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
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
         </td>
      </tr>
   </g:hasErrors>
      <tr><td colspan="3">&nbsp;</td></tr>
      <tr class="ContainerFooter">
        <td colspan="3">
          <div class="AlignRight">
                <g:actionSubmit action="revert" value="Convert" class="Button"/>
          </div>
        </td>
      </tr>
     </table>
  </g:form>

 </td></tr></table>
  </body>
</html>
