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

      <div class="${actionName == 'list' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
            <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
            <g:link action="list"><g:message code="repository.page.leftnav.list" /></g:link>
      </div>

      <div class="${actionName == 'discover' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="discover"><g:message code="repository.page.leftnav.discover" /></g:link>
      </div>


      <div class="${actionName == 'create' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="create"><g:message code="repository.page.leftnav.new" /></g:link>
      </div>


      <div class="${actionName == 'editAuthorization' || actionName == 'saveAuthorization' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="editAuthorization"><g:message code="repository.page.leftnav.accessRules" /></g:link>
      </div>

    </g:ifAnyGranted>
  </g:else>
  </div>
</content>
