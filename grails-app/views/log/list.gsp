<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge Logs</title>
</head>

<content tag="title">
    Administration
</content>

%{--
  - Copyright 2010 CollabNet, Inc. All rights reserved.
  --}%
<g:render template="/server/leftNav" />

<body>

  <g:render template="/common/tabs"
      model="[tabs:[
        [action:'list', label:'Available Files', active: true],
        [action:'configure', label:'Configure', active: false]
        ]]" />

  <table class="Container">
    <tbody>
    <tr class="ItemListHeader">
      
      <g:sortableColumn property="name" title="File Name"/>
      <g:sortableColumn property="date" title="Date"/>
      <g:sortableColumn property="size" title="Size"/>
    </tr>

    <g:if test="${files.size() > 0}">

      <g:each in="${files}" status="i" var="file">
        <g:def var="fileName" value="${file.name}"/>

        <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
          <td><g:link action="show" params="[fileName : fileName]">${file.name}</g:link></td>
          <td><%=new Date(file.lastModified()).format("yyyy-MM-dd HH:mm")%></td>
          <td><g:formatFileSize size="${file.size}"/></td>
        </tr>

      </g:each>

    </g:if>
    <g:else>
      <tr class="ItemListNoData">
        <td colspan="3">No results found.</td>
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
