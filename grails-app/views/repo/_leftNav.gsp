<content tag="leftMenu">
  <g:if test="${isReplica}">
    <li class="ImageListParentSelectedNoTop">
          <g:message code="repository.page.replica.hosted" />
    </li>
  </g:if>
  <g:else>
    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">

      <li class="${(controllerName == 'repo' && actionName == 'list') ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
            <g:link controller="repo" action="list"><g:message code="repository.page.leftnav.list" /></g:link>
      </li>

      <li class="${controllerName == 'repo' && actionName == 'discover' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <g:link controller="repo" action="discover"><g:message code="repository.page.leftnav.discover" /></g:link>
      </li>


      <li class="${controllerName == 'repo' && actionName == 'create' || controllerName == 'repo' && actionName == 'save' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <g:link controller="repo" action="create"><g:message code="repository.page.leftnav.new" /></g:link>
      </li>


      <li class="${controllerName == 'repo' && actionName.contains('Authorization') ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <g:link controller="repo" action="showAuthorization"><g:message code="repository.page.leftnav.accessRules" /></g:link>
      </li>

      <li class="${controllerName == 'repo' && actionName == 'bkupScheduleMultiple' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <g:link controller="repo" action="bkupScheduleMultiple"><g:message code="repository.page.leftnav.backup" /></g:link>
      </li>

      <g:set var="isNewTemplate" value="${controllerName == 'repoTemplate' && (actionName == 'create' || actionName == 'save')}"/>
      <li class="${controllerName == 'repoTemplate' && !isNewTemplate ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <g:link controller="repoTemplate" action="list"><g:message code="repoTemplate.leftnav.manageRepoTemplates" /></g:link>
      </li>

          
          <g:if test="${controllerName == 'repoTemplate'}">
            <li class="${isNewTemplate ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <g:link controller="repoTemplate" action="create"><g:message code="repoTemplate.leftNav.create.new" /></g:link>
            </li>
          </g:if>

    </g:ifAnyGranted>
  </g:else>
</content>
