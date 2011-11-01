<content tag="leftMenu">
  <div class="leftDescription">
  <g:if test="${isReplica}">
    <div class="ImageListParentSelectedNoTop">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:message code="repository.page.replica.hosted" />
    </div>
  </g:if>
  <g:else>
    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">

      <div class="${(controllerName == 'repo' && actionName == 'list') ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
            <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
            <g:link controller="repo" action="list"><g:message code="repository.page.leftnav.list" /></g:link>
      </div>

      <div class="${controllerName == 'repo' && actionName == 'discover' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link controller="repo" action="discover"><g:message code="repository.page.leftnav.discover" /></g:link>
      </div>


      <div class="${controllerName == 'repo' && actionName == 'create' || controllerName == 'repo' && actionName == 'save' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link controller="repo" action="create"><g:message code="repository.page.leftnav.new" /></g:link>
      </div>


      <div class="${controllerName == 'repo' && actionName == 'editAuthorization' || actionName == 'saveAuthorization' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link controller="repo" action="editAuthorization"><g:message code="repository.page.leftnav.accessRules" /></g:link>
      </div>

      <div class="${controllerName == 'repo' && actionName == 'bkupScheduleMultiple' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link controller="repo" action="bkupScheduleMultiple"><g:message code="repository.page.leftnav.backup" /></g:link>
      </div>

      <g:set var="isNewTemplate" value="${controllerName == 'repoTemplate' && (actionName == 'create' || actionName == 'save')}"/>
      <div class="${controllerName == 'repoTemplate' && !isNewTemplate ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link controller="repoTemplate" action="list"><g:message code="repoTemplate.leftnav.manageRepoTemplates" /></g:link>
      </div>

          
          <g:if test="${controllerName == 'repoTemplate'}">
            <div class="${isNewTemplate ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
              &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
              <g:link controller="repoTemplate" action="create"><g:message code="repoTemplate.leftNav.create.new" /></g:link>
            </div>
          </g:if>

    </g:ifAnyGranted>
  </g:else>
  </div>
</content>
