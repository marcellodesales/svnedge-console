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

      <li<g:if test="${controllerName == 'repo' && actionName.contains('Authorization')}"> class="active"</g:if>>
          <g:link controller="repo" action="showAuthorization"><g:message code="repository.page.leftnav.accessRules" /></g:link>
      </li>

      <li<g:if test="${controllerName == 'repo' && actionName == 'bkupScheduleMultiple'}"> class="active"</g:if>>
          <g:link controller="repo" action="bkupScheduleMultiple"><g:message code="repository.page.leftnav.backup" /></g:link>
      </li>

      <li<g:if test="${controllerName == 'repoTemplate'}"> class="active"</g:if>>
          <g:link controller="repoTemplate" action="list"><g:message code="repoTemplate.leftnav.manageRepoTemplates" /></g:link>
      </li>
      
    </g:ifAnyGranted>
  </g:else>
</content>
