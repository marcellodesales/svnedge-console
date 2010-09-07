<g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">

<content tag="leftMenu">

  <div class="${controllerName == 'user' && actionName == 'list' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="user" action="list"><g:message code="user.page.leftnav.list"/></g:link>
  </div>

  <div class="${controllerName == 'user' && actionName == 'create' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="user" action="create"><g:message code="user.page.leftnav.create"/></g:link>
  </div>

  <div class="${controllerName == 'role' && actionName == 'list' ? 'ImageListParentSelectedNoTop' : 'ImageListParent' }">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="role" action="list"><g:message code="role.page.leftnav.list"/></g:link>
  </div>

</content>

</g:ifAnyGranted>