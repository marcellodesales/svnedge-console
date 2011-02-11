<html>
  <head>
    <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.confirm.title" /></title>
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
   <g:set var="tabArray" value="${tabArray << [action:'ctfProject', label: message(code:'setupTeamForge.page.tabs.ctfProject', args:[3])]}" />
   <g:set var="tabArray" value="${tabArray << [action:'ctfUsers', label: message(code:'setupTeamForge.page.tabs.ctfUsers', args:[4])]}" />
   <g:set var="tabArray" value="${tabArray << [active: true, label: message(code:'setupTeamForge.page.tabs.confirm', args:[5])]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

   <g:form method="post">
   <table class="ItemDetailContainer">
     <tr>
      <td class="ContainerBodyWithPaddedBorder">

        <g:if test="${flash.errors}">
          <g:render template="errorList"/>
        </g:if>
        <g:else>
          <p><strong><g:message code="setupTeamForge.page.confirm.ready" /></strong> <g:message code="setupTeamForge.page.confirm.ready.tip" /></p>
        </g:else>

      <table class="ItemDetailTable">
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.server" />
        </td>
        <td colspan="2" class="ItemDetailValue">${wizardBean.ctfURL}
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.tfVersion" />
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.appVersion == wizardBean.apiVersion}">
            ${wizardBean.apiVersion}
          </g:if>
          <g:else>
            ${wizardBean.appVersion} (API: ${wizardBean.apiVersion})
          </g:else>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.project" />
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.isProjectPerRepo}">
             <g:message code="setupTeamForge.page.confirm.sameReposImported" />
          </g:if>
          <g:else>
          ${wizardBean.ctfProject}
          </g:else>
        </td>
      </tr>
    <g:if test="${wizardBean.lowercaseRepos || wizardBean.repoPrefix}">
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.repositories" />
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.lowercaseRepos}">
            <div><g:message code="setupTeamForge.page.confirm.repositoriesConverted" /></div>
          </g:if>
          <g:if test="${wizardBean.repoPrefix}">
            <div><g:message code="setupTeamForge.page.confirm.repositoriesPrefixed" args="${[wizardBean.repoPrefix]}" encodeAs="HTML"/>
            </div>
          </g:if>
        </td>
      </tr>
    </g:if>
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.users" />
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.importUsers}">
             <g:message code="setupTeamForge.page.confirm.users.imported" />
          </g:if>
          <g:else>
             <g:message code="setupTeamForge.page.confirm.users.noUsersimported" />
          </g:else>
        </td>
      </tr>
    <g:if test="${wizardBean.importUsers}">
      <tr>
        <td class="ItemDetailName"><g:message code="setupTeamForge.page.confirm.membership" />
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:if test="${wizardBean.assignMembership}">
             <g:message code="setupTeamForge.page.confirm.membership.giveMembership" />
          </g:if>
          <g:else>
             <g:message code="setupTeamForge.page.confirm.membership.giveLater" />
          </g:else>
        </td>
      </tr>
    </g:if>
    <g:if test="${wizardBean && wizardBean.requiresServerKey}">
       <g:render template="serverKeyField" model="${[con:wizardBean]}"/>
    </g:if>

          </table>
        </td>
      </tr>
      <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
                <g:actionSubmit action="convert" value="${message(code:'setupTeamForge.page.confirm.button.confirm')}" class="Button"/>
          </div>
        </td>
      </tr>
      </table>
      </g:form>

    </body>
</html>
  
