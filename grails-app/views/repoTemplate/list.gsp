<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repoTemplate.page.list.header.title /></title>
        <g:javascript library="listView"/>
        <g:javascript library="prototype"/>
        <g:javascript library="prototype/dragdrop"/>
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
  <p>
    <g:message code="repoTemplate.page.list.header.title" />
  </p>
  <p>
    <g:message code="repoTemplate.page.list.sort.instructions"/>
  </p>

    <g:if test="${repoTemplateInstanceList.size() > 0}">
      <table id="reposTable" class="table table-striped table-bordered table-condensed tablesorter">
        <thead>
        <tr>
          <!-- <th><g:listViewSelectAll/></th>
          <g:sortableColumn property="name" title="${message(code: 'repoTemplate.name.label', default: 'Name')}" />
          <g:sortableColumn property="active" title="${message(code: 'repoTemplate.active.label', default: 'Active')}" />  -->
          <th><g:message code="repoTemplate.name.label"/></th>
          <th><g:message code="repoTemplate.active.label"/></th>
        </tr>
      </thead>
      <tbody id="templates">
        <g:each in="${repoTemplateInstanceList}" status="i" var="repoTemplateInstance">
          <tr id="repoTemplate_${repoTemplateInstance.id}" class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}" style="cursor: move">
            <!-- <td><g:listViewSelectItem item="${repoTemplateInstance}"/></td> -->
            <td><g:link action="edit" id="${repoTemplateInstance.id}">${repoTemplateInstance.name}</g:link></td>
            <td><g:formatBoolean boolean="${repoTemplateInstance.active}" /></td>
          </tr>
        </g:each>
      </tbody>
      </table>
      
      <g:form>
      <p class="pull-right">
        <g:listViewActionButton action="create" minSelected="0" maxSelected="0" primary="true"><g:message code="default.button.create.label" /></g:listViewActionButton>
      </p>
      </g:form>
      
      <g:javascript>
        function sendUpdatedOrder(container) {
            new Ajax.Request("/csvn/repoTemplate/updateListOrder", {
                method: "post", parameters: Sortable.serialize(container.id)
                });
            var rows = $('templates').childElements();
            for (var i = 0; i < rows.length; i++) {
                rows[i].className = (i % 2) == 0 ? 'EvenRow' : 'OddRow';
            }
        }
        Sortable.create('templates',{tag: 'tr', ghosting:false, onUpdate: sendUpdatedOrder})
      </g:javascript>
    </g:if>
    <g:else>
      <p><g:message code="repoTemplate.page.list.empty"/></p>
    </g:else>
</body>
</html>
