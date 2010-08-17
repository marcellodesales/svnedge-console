<html>
  <head>
    <title>CollabNet TeamForge Integration</title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />
  </head>
  <content tag="title">
    CollabNet TeamForge Integration
  </content>

  <g:render template="/server/leftNav" />

  <body>

    <g:if test="${isFreshInstall}">
      <g:render template="/common/tabs"
          model="[tabs:[
            [action:'index', label:'1. Introduction'],
            [active:true, label:'2. Convert to TeamForge mode']]]" />
    </g:if>
    <g:else>
      <g:render template="/common/tabs"
          model="[tabs:[
            [action:'index', label:'1. Introduction'],
            [active:true, label:'2. TeamForge Credentials'],
            [label:'3. TeamForge Project'],
            [label:'4. TeamForge Users'],
            [label:'5. Convert to TeamForge mode']
            ]]" />
    </g:else>

    <g:form method="post">

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

          <g:if test="${connectionErrors && errorCause}">
            <div class="errorMessage">
                ${generalError}.
                <ul>
                    <li><g:message code="ctfConversion.form.ctfInfo.noconnection"/>: ${errorCause}</li>
                </ul>
            </div>
          </g:if>

          <p>
            Credentials for a CollabNet TeamForge Site Administrator are needed in 
            order to add a new SCM integration server. 
          </p>

      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="ctfURL">TeamForge server URL:</label>
        </td>
        <td valign="top" class="value">
          <input size="40" type="text" id="ctfURL" name="ctfURL" 
                        value="${fieldValue(bean:con, field:'ctfURL')}"/>
        </td>
        <td class="ItemDetailValue">
            <em>Base URL including protocol and hostname</em>
        </td>
      </tr>
      <tr>
         <td></td>
         <td class="errors" colspan="2">
            <g:hasErrors bean="${con}" field="ctfURL">
              <ul><g:eachError bean="${con}" field="ctfURL">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
            </g:hasErrors>
         </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfUsername">TeamForge administrator username:</label>
        </td>
        <td class="value ${hasErrors(bean:con,field:'ctfUsername','errors')}">
          <input size="20" type="text" id="ctfUsername" name="ctfUsername" 
              value="${fieldValue(bean:con,field:'ctfUsername').replace(',','')}"/>
        </td>
        <td class="ItemDetailValue">
          <em>Account must have permission to add new scm integration servers</em>
         </td>
      </tr>
      <tr>
         <td></td>
         <td class="errors" colspan="2">
           <g:hasErrors bean="${con}" field="ctfUsername">
              <ul><g:eachError bean="${con}" field="ctfUsername">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
           </g:hasErrors>
         </td>
      </tr> 
      <tr>
        <td class="ItemDetailName">
          <label for="ctfPassword">TeamForge administrator password:</label>
        </td>
        <td class="value ${hasErrors(bean:con,field:'ctfPassword','errors')}">
          <input size="20" type="password" id="ctfPassword" name="ctfPassword" 
              value="${fieldValue(bean:con,field:'ctfPassword')}"/>
        </td>
        <td></td>
      </tr> 
      <tr>
         <td></td>
         <td class="errors" colspan="2">
            <g:hasErrors bean="${con}" field="ctfPassword">
              <ul><g:eachError bean="${con}" field="ctfPassword">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
            </g:hasErrors>
         </td>
      </tr>
      <g:if test="${con && con.requiresServerKey}">
        <g:render template="serverKeyField"/>
      </g:if>

      </table>
      
      <tr class="ContainerFooter">
        <td colspan="3">
          <div class="AlignRight">
            <g:if test="${isFreshInstall}">
                <g:actionSubmit action="convert" value="Convert" class="Button"/>
            </g:if>
            <g:else>
                <g:actionSubmit action="confirmCredentials" value="Continue" class="Button"/>
            </g:else>
          </div>
        </td>
      </tr>
      
      </td></tr></table>
      </g:form>

    </body>
</html>
  
