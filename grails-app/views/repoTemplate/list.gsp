<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repoTemplate.page.list.header.title /></title>
        <g:render template="/common/listViewResources"/>

    </head>

<g:render template="/repo/leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>


<body>
<!-- 
  <div class="nav">
    <span class="menuButton"><g:link class="create" action="create"><g:message code="repoTemplate.page.create.header.title" /></g:link></span>
  </div>
-->
  <div class="body">
    <g:if test="${repoTemplateInstanceList.size() > 0}">
    <div class="list">
      <table class="Container">
      <thead>
        <tr class="ContainerHeader">
          <td colspan="3"><g:message code="repoTemplate.page.list.header.title" /></td>
        </tr>
        <tr class="ItemListHeader">
          <!-- <th><g:listViewSelectAll/></th>
          <g:sortableColumn property="name" title="${message(code: 'repoTemplate.name.label', default: 'Name')}" />
          <g:sortableColumn property="active" title="${message(code: 'repoTemplate.active.label', default: 'Active')}" />  -->
          <th><g:message code="repoTemplate.name.label"/></th>
          <th><g:message code="repoTemplate.active.label"/></th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${repoTemplateInstanceList}" status="i" var="repoTemplateInstance">
          <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
            <!-- <td><g:listViewSelectItem item="${repoTemplateInstance}"/></td> -->
            <td><g:link action="edit" id="${repoTemplateInstance.id}">${fieldValue(bean: repoTemplateInstance, field: "name")}</g:link></td>
            <!--  <td>${fieldValue(bean: repoTemplateInstance, field: "displayOrder")}</td>  -->                        
            <td><g:formatBoolean boolean="${repoTemplateInstance.active}" /></td>
          </tr>
        </g:each>
      </tbody>
      </table>
    </div>
    <div class="paginateButtons">
      <g:paginate total="${repoTemplateInstanceTotal}" />
    </div>
    </g:if>
    <g:else>
      <div><p><g:message code="repoTemplate.page.list.empty"/></p></div>
    </g:else>
  </div>
</body>
</html>
