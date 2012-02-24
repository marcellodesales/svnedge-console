<g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_USERS">

<content tag="leftMenu">

  <li<g:if test="${controllerName == 'user' && actionName == 'list'}"> class="active"</g:if>>
    <g:link controller="user" action="list"><g:message code="user.page.leftnav.list"/></g:link>
  </li>

  <li<g:if test="${controllerName == 'role' && actionName == 'list'}"> class="active"</g:if>>
    <g:link controller="role" action="list"><g:message code="role.page.leftnav.list"/></g:link>
  </li>

</content>

</g:ifAnyGranted>