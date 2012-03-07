<head>
    <meta name="layout" content="main" />
  <link href="${resource(dir:'css',file:'DT_bootstrap.css')}" rel="stylesheet"/>

</head>


<content tag="title">
  <g:message code="user.page.header"/>
</content>

<g:render template="leftNav" />

<body>
            <table class="table table-striped table-bordered table-condensed tablesorter" id="datatable">
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
      "aaSorting": [[ 0, "asc" ]]
      
    } );
  } );
</g:javascript>

</body>
