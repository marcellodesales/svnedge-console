<html>
  <head>
    <g:if test="${isFreshInstall}">
        <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.ctfInfo.title.fresh" /></title>
    </g:if>
    <g:else>
        <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.ctfInfo.title.complete" /></title>
    </g:else>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />
  </head>
  <content tag="title">
    <g:message code="setupTeamForge.page.leftNav.header" />
  </content>

  <g:render template="/server/leftNav" />

  <body>

    <g:set var="tabArray" value="${[[action:'index', label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}" />
    <g:if test="${isFreshInstall}">
      <g:set var="tabArray" value="${tabArray << [active:true, label: message(code:'setupTeamForge.page.tabs.confirm', args:[2])]}" />
    </g:if>
    <g:else>
      <g:set var="tabArray" value="${tabArray << [active:true, label: message(code:'setupTeamForge.page.tabs.ctfInfo', args:[2])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.ctfProject', args:[3])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.ctfUsers', args:[4])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.confirm', args:[5])]}" />
    </g:else>
    <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

    <g:form method="post">

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

          <g:if test="${flash.errors}">
            <g:render template="errorList"/>
          </g:if> 
          <g:if test="${connectionErrors && errorCause}">
            <div class="errorMessage">
                <%=generalError%>
                <ul>
                    <li><g:message code="ctfConversion.form.ctfInfo.noconnection"/> <%=errorCause%></li>
                </ul>
            </div>
          </g:if>

          <p>
            <g:message code="setupTeamForge.page.ctfInfo.p1"/>
          </p>

      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="ctfURL"><g:message code="setupTeamForge.page.ctfInfo.ctfUrl.label"/></label>
        </td>
        <td valign="top" class="value">
          <input size="40" type="text" id="ctfURL" name="ctfURL" 
                        value="${fieldValue(bean:con, field:'ctfURL')}"/>
        </td>
        <td class="ItemDetailValue">
            <em><g:message code="setupTeamForge.page.ctfInfo.ctfUrl.label.tip"/></em>
        </td>
      </tr>
      <tr>
         <td></td>
         <td class="errors" colspan="2">
            <g:hasErrors bean="${con}" field="ctfURL">
              <ul><g:eachError bean="${con}" field="ctfURL">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </g:hasErrors>
         </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="ctfUsername"><g:message code="setupTeamForge.page.ctfInfo.ctfUsername.label"/></label>
        </td>
        <td class="value ${hasErrors(bean:con,field:'ctfUsername','errors')}">
          <input size="20" type="text" id="ctfUsername" name="ctfUsername" 
              value="${fieldValue(bean:con,field:'ctfUsername').replace(',','')}"/>
        </td>
        <td class="ItemDetailValue">
          <em><g:message code="setupTeamForge.page.ctfInfo.ctfUsername.label.tip"/></em>
         </td>
      </tr>
      <tr>
         <td></td>
         <td class="errors" colspan="2">
           <g:hasErrors bean="${con}" field="ctfUsername">
              <ul><g:eachError bean="${con}" field="ctfUsername">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
           </g:hasErrors>
         </td>
      </tr> 
      <tr>
        <td class="ItemDetailName">
          <label for="ctfPassword"><g:message code="setupTeamForge.page.ctfInfo.ctfPassword.label"/></label>
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
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
            </g:hasErrors>
         </td>
      </tr>
            <tr>
        <td class="ItemDetailName">
          <label for="serverKey"><g:message code="ctfConversionBean.serverKey.label" /></label>
        </td>
        <td valign="top" class="value ${hasErrors(bean: cmd, field: 'serverKey', 'errors')}">
          <input size="40" type="text" id="serverKey" name="serverKey" 
              value="${fieldValue(bean: cmd, field: 'serverKey')}"/>
          <g:if test="${con && con.requiresServerKey}">
            <div class="errorMessage">
              <g:message code="setupReplica.action.updateCredentials.invalidApiKey" />
            </div>
          </g:if>
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
      </table>
      
      <tr class="ContainerFooter">
        <td colspan="3">
          <div class="AlignRight">
            <g:if test="${isFreshInstall}">
                <g:actionSubmit action="convert" value="${message(code:'setupTeamForge.page.ctfInfo.button.convert')}" class="Button"/>
            </g:if>
            <g:else>
                <g:actionSubmit action="confirmCredentials" value="${message(code:'setupTeamForge.page.ctfInfo.button.continue')}" class="Button"/>
            </g:else>
          </div>
        </td>
      </tr>
      
      </td></tr></table>
      </g:form>

    </body>
</html>
  
