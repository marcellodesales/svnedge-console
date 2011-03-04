<head>
    <meta name="layout" content="main" />
    <title><g:message code="user.page.list.title"/></title>
</head>


<content tag="title">
  <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>
            <table class="Container">
              <tbody>
              <tr class="ContainerHeader">
                <td colspan="5"><g:message code="user.page.list.header"/></td>
              </tr>
               <tr class="ItemListHeader">
                    <g:sortableColumn property="username" title="${message(code: 'user.page.list.column.username')}" />
                    <g:sortableColumn property="realUserName" title="${message(code: 'user.page.list.column.realUserName')}" />
                    <g:sortableColumn property="description" title="${message(code: 'user.page.list.column.description')}" />
              </tr>
            </thead>
            <tbody>
            <g:each in="${userInstanceList}" status="i" var="person">
                <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
                    <td><g:link action="edit" id="${person.id}">${person.username}</g:link></td>
                    <td>${person.realUserName}</td>
                    <td>${person.description}</td>
                </tr>
            </g:each>
            <tr class="ContainerFooter">
                       <td colspan="5">
                       <div class="paginateButtons">
                <g:paginate total="${userInstanceTotal}" />
            </div>
                       </td>
                    </tr>
            </tbody>
            </table>
</body>
