<content tag="leftMenu">
  <div class="<g:if test="${['edit','editAuthentication', 'editProxy', 'updateProxy'].contains(actionName)}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="server" action="edit"><g:message code="server.page.leftNav.settings" /></g:link>
  </div>

  <div class="<g:if test="${controllerName == 'log'}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="log" action="list" params="[sort : 'date', order: 'desc']"><g:message code="server.page.leftNav.logs" /></g:link>
  </div>

  <div class="<g:if test="${controllerName == 'packagesUpdate'}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="packagesUpdate" action="available"><g:message code="server.page.leftNav.updates" /></g:link>
  </div>

<g:if test="${!isManagedMode}">
  <div class="<g:if test="${controllerName == 'setupTeamForge' || controllerName == 'setupReplica'}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="setupTeamForge" action="index">CollabNet TeamForge</g:link>
  </div>
</g:if>
<g:else>
  <div class="<g:if test="${['editIntegration','revert','editCredentials','updateCredentials'].contains(actionName) }">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="server" action="editIntegration"><g:message code="server.page.leftNav.toStandalone" /></g:link>
  </div>
</g:else>

  <div class="<g:if test="${controllerName == 'setupCloudServices'}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="setupCloudServices" action="index"><g:message code="server.page.leftNav.cloudServices" /></g:link>
  </div>
</content>
