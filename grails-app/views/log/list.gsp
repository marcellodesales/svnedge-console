<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.list.title" /></title>
</head>

<content tag="title">
    <g:message code="server.page.edit.header" />
</content>

%{--
  - Copyright 2010 CollabNet, Inc. All rights reserved.
  --}%
<g:render template="/server/leftNav" />

<body>

  <g:set var="tabArray" value="${[[active: true, label: message(code:'logs.page.tabs.available')]]}" />
  <g:set var="tabArray" value="${tabArray << [action:'configure', label: message(code:'logs.page.tabs.settings')]}" />
  <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

  <table class="Container">
    <tbody>
    <tr class="ItemListHeader">
      
      <g:sortableColumn property="name" title="${message(code:'logs.page.list.column.name')}"/>
      <g:sortableColumn property="date" title="${message(code:'logs.page.list.column.date')}"/>
      <g:sortableColumn property="size" title="${message(code:'logs.page.list.column.size')}"/>
    </tr>

    <g:if test="${files.size() > 0}">

      <g:each in="${files}" status="i" var="file">
        <g:def var="fileName" value="${file.name}"/>

        <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
          <td><g:link action="show" params="[fileName : fileName]">${file.name}</g:link></td>
          <td><g:formatDate format="${logDateFormat}" date="${file.lastModified()}"/></td>
          <td><g:formatFileSize size="${file.size}"/></td>
        </tr>

      </g:each>

    </g:if>
    <g:else>
      <tr class="ItemListNoData">
        <td colspan="3"><g:message code="logs.page.list.noFilesFound" />.</td>
      </tr>
    </g:else>

    <tr class="ContainerFooter">
      <td colspan="5">
      </td>
    </tr>
    </tbody>
  </table>
</body>
</html>
