<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.list.title" /></title>
  <link href="${resource(dir:'css',file:'DT_bootstrap.css')}" rel="stylesheet"/>
</head>

<content tag="title">
    <g:message code="server.page.edit.header" />
</content>

%{--
  - Copyright 2010 CollabNet, Inc. All rights reserved.
  --}%
<g:render template="/server/leftNav" />

<body>

<table class="table table-striped table-bordered table-condensed tablesorter" id="datatable">
  <thead>
    <tr>
      
      <g:sortableColumn property="name" title="${message(code:'logs.page.list.column.name')}"/>
      <g:sortableColumn property="date" title="${message(code:'logs.page.list.column.date')}"/>
      <g:sortableColumn property="size" title="${message(code:'logs.page.list.column.size')}"/>
    </tr>
  </thead>
  <tbody>
    <g:if test="${files.size() > 0}">

      <g:each in="${files}" status="i" var="file">
        <g:def var="fileName" value="${file.name}"/>

        <tr>
          <td><g:link action="show" params="[fileName : fileName]">${file.name}</g:link></td>
          <td><g:formatDate format="${logDateFormat}" date="${file.lastModified()}"/></td>
          <td><span title="${file.size}"><g:formatFileSize size="${file.size}"/></span></td>
        </tr>

      </g:each>

    </g:if>
    <g:else>
      <tr>
        <td colspan="3"><g:message code="logs.page.list.noFilesFound" /></td>
      </tr>
    </g:else>
  </tbody>
</table>

<g:javascript library="jquery.dataTables.min"/>
<g:javascript library="DT_bootstrap"/>
<g:javascript>
  /* Table initialisation */
  $(document).ready(function() {
    $('#datatable').dataTable( {
      "sDom": "<'row'<'span4'l><'pull-right'f>r>t<'row'<'span4'i><'pull-right'p>><'spacer'>",
      "sPaginationType": "bootstrap",
      "bStateSave": true,
      "oLanguage": {
        "sLengthMenu": "${message(code:'datatable.rowsPerPage')}",
        "oPaginate": {
            "sNext": "${message(code:'default.paginate.next')}",
            "sPrev": "${message(code:'default.paginate.prev')}"
        },
        "sSearch": "${message(code:'default.filter.label')}",
        "sZeroRecords": "${message(code:'default.search.noResults.message')}",
        "sEmptyTable": "${message(code:'default.search.noResults.message')}",
        "sInfo": "${message(code:'datatable.showing')}",
        "sInfoFiltered": " ${message(code:'datatable.filtered')}"
        },
      "aaSorting": [[ 0, "asc" ]],
      "aoColumns": [
                null,
                null,
                { "sType": "title-numeric" }  // sorts on title attribute, rather than text of the file size element
            ]
    } );
  } );
</g:javascript>
</body>
</html>
