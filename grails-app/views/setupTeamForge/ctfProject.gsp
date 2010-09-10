<html>
  <head>
    <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.ctfProject.title" /></title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />
  </head>
  <content tag="title">
    <g:message code="setupTeamForge.page.leftNav.header" />
  </content>

  <g:render template="/server/leftNav" />

  <body>

   <g:set var="tabArray" value="${[[action:'index', label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}" />
   <g:set var="tabArray" value="${tabArray << [action:'ctfInfo', label: message(code:'setupTeamForge.page.tabs.ctfInfo', args:[2])]}" />
   <g:set var="tabArray" value="${tabArray << [active: true, label: message(code:'setupTeamForge.page.tabs.ctfProject', args:[3])]}" />
   <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.ctfUsers', args:[4])]}" />
   <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.confirm', args:[5])]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

  <g:form method="post">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailContainerCell">

          <g:if test="${con.errors.hasGlobalErrors()}">
            <div class="errorMessage">
              <ul>
              <g:each in="${con.errors.globalErrors}">
                  <li><g:message error="${it}" /></li>
              </g:each>
              </ul>
            </div>
          </g:if>

          <p>
            <g:message code="setupTeamForge.page.ctfProject.p1" />.
          </p>

          <p>
            <g:message code="setupTeamForge.page.ctfProject.p2" />.
          </p>

      <table class="ItemDetailTable">
        <tr>
             <td class="ItemDetailName">
               <label for="ctfProject"><g:message code="setupTeamForge.page.ctfProject.name.label" />:</label>
             </td>
             <td class="value ${hasErrors(bean:con,field:'ctfProject','errors')}">
             <g:hiddenField name="projectType" id="projectTypeSingle" value="single"/>
               <input size="30" type="text" id="ctfProject" name="ctfProject" 
                  value="${fieldValue(bean:con, field:'ctfProject')}"/>
             </td>
             <td class="ItemDetailValue"><em><g:message code="setupTeamForge.page.ctfProject.name.label.tip" /></em></td>
        </tr>
        <tr>
         <td></td>
          <td class="errors" colspan="2">
             <g:hasErrors bean="${con}" field="ctfProject">
               <ul><g:eachError bean="${con}" field="ctfProject">
                  <li><g:message error="${it}"/></li>
               </g:eachError></ul>
             </g:hasErrors>
          </td>
        </tr>
        <g:if test="${invalidRepoNames.containsUpperCaseRepos}">
        <tr>
             <td class="ItemDetailName">
               <label for="lowercaseRepos"><g:message code="setupTeamForge.page.ctfProject.name.toLowerCase" />:</label>
             </td>
             <td class="value">
               <g:checkBox id="lowercaseRepos" name="lowercaseRepos"
                   value="${con.lowercaseRepos}"/>
             </td>
             <td class="ItemDetailValue"><em><g:message code="setupTeamForge.page.ctfProject.name.toLowerCase.tip" />.</em></td>
        </tr>
        </g:if>
        <g:if test="${invalidRepoNames.containsReposWithInvalidFirstChar}">
        <tr>
             <td class="ItemDetailName">
               <label for="repoPrefix"><g:message code="setupTeamForge.page.ctfProject.repoName.prefix.label" />:</label>
             </td>
             <td class="value ${hasErrors(bean:con,field:'repoPrefix','errors')}">
               <input size="10" type="text" id="repoPrefix" name="repoPrefix" 
                  value="${fieldValue(bean:con, field:'repoPrefix')}"/>
             </td>
             <td class="ItemDetailValue"><em><g:message code="setupTeamForge.page.ctfProject.repoName.prefix.label.tip" />.</em></td>
        </tr>
        <tr>
         <td></td>
          <td class="errors" colspan="2">
             <g:hasErrors bean="${con}" field="repoPrefix">
               <ul><g:eachError bean="${con}" field="repoPrefix">
                  <li><g:message error="${it}"/></li>
               </g:eachError></ul>
             </g:hasErrors>
          </td>
        </tr>
        </g:if>
      </table>
      </td>
      </tr>
      <tr class="ContainerFooter">
        <td>
          <div class="AlignRight">
                <g:actionSubmit action="updateProject" value="${message(code:'setupTeamForge.page.ctfProject.button.continue')}" class="Button"/>
          </div>
        </td>
      </tr>
    </table>
    </g:form>

    </body>
</html>
  
