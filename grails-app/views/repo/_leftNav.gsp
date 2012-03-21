<%@ page import="com.collabnet.svnedge.domain.Server; com.collabnet.svnedge.domain.ServerMode" %>
<g:set var="isManaged" value="${Server.server.mode == ServerMode.MANAGED}"/>
<g:set var="isReplica" value="${Server.server.mode == ServerMode.REPLICA}"/>
<content tag="leftMenu">
  <g:if test="${isReplica}">
    <li class="active"><g:message code="repository.page.replica.hosted" /></li>
  </g:if>
  <g:else>
      <li class="nav-header">
          <g:message code="repository.main.icon" />
      </li>

      <li<g:if test="${(controllerName == 'repo' && actionName == 'list')}"> class="active"</g:if>>
          <g:link controller="repo" action="list"><g:message code="repository.page.leftnav.list" /></g:link>
      </li>

    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">

      <g:if test="${!isManaged && !isReplica}">
      <li<g:if test="${controllerName == 'repo' && actionName.contains('Authorization')}"> class="active"</g:if>>
          <g:link controller="repo" action="showAuthorization"><g:message code="repository.page.leftnav.accessRules" /></g:link>
      </li>
      </g:if>

      <li<g:if test="${controllerName == 'repo' && actionName == 'bkupScheduleMultiple'}"> class="active"</g:if>>
          <g:link controller="repo" action="bkupScheduleMultiple"><g:message code="repository.page.leftnav.backup" /></g:link>
      </li>

      <g:if test="${!isManaged && !isReplica}">
      <li<g:if test="${controllerName == 'repoTemplate'}"> class="active"</g:if>>
          <g:link controller="repoTemplate" action="list"><g:message code="repoTemplate.leftnav.manageRepoTemplates" /></g:link>
      </li>
      </g:if>
      
    </g:ifAnyGranted>
  </g:else>
</content>
