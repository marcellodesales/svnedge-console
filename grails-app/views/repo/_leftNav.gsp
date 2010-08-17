<content tag="leftMenu">
  <div class="leftDescription">
  <g:if test="${isReplica}">
    <div class="ImageListParentSelectedNoTop">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          Repositories hosted on this replica
    </div>
  </g:if>
  <g:else>
    <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">

      <div class="${actionName == 'list' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
            <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
            <g:link action="list">Repository List</g:link>
      </div>

      <div class="${actionName == 'discover' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="discover">Discover Repositories</g:link>
      </div>


      <div class="${actionName == 'create' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="create">New Repository</g:link>
      </div>


      <div class="${actionName == 'editAuthorization' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
          <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
          <g:link action="editAuthorization">Access Rules</g:link>
      </div>

    </g:ifAnyGranted>
  </g:else>
  </div>
</content>
