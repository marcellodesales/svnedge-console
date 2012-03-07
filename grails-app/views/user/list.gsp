<head>
    <meta name="layout" content="main" />
</head>


<content tag="title">
  <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>
            <table class="table table-striped table-bordered table-condensed tablesorter">
              <thead>
               <tr>
                    <g:sortableColumn property="username" title="${message(code: 'user.page.list.column.username')}" />
                    <g:sortableColumn property="realUserName" title="${message(code: 'user.page.list.column.realUserName')}" />
                    <g:sortableColumn property="description" title="${message(code: 'user.page.list.column.description')}" />
              </tr>
            </thead>
            <tbody>
            <g:each in="${userInstanceList}" status="i" var="person">
                <tr>
                    <td><g:link action="edit" id="${person.id}">${person.username}</g:link></td>
                    <td>${person.realUserName}</td>
                    <td>${person.description}</td>
                </tr>
            </g:each>
            </tbody>
            </table>
<g:form>
  <p class="pull-right">
    <g:listViewActionButton action="create" minSelected="0" maxSelected="0" primary="true"><g:message code="default.button.create.label" /></g:listViewActionButton>
  </p>
</g:form>

<g:pagination total="${userInstanceTotal}" />
</body>
