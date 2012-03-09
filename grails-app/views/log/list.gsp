<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.list.title" /></title>
  <link href="${resource(dir:'css',file:'DT_bootstrap.css')}" rel="stylesheet"/>
</head>

<content tag="title">
    <g:message code="logs.page.list.title" />
</content>

%{--
  - Copyright 2010 CollabNet, Inc. All rights reserved.
  --}%
<g:render template="/server/leftNav" />

<body>
  <table class="table table-striped table-bordered table-condensed tablesorter" id="datatable"></table>
  <script type="text/javascript">
  /* Data set */
  var aDataSet = [
  <g:each in="${files}" status="i" var="file">
      ['<g:link action="show" params="[fileName : fileName]">${file.name}</g:link>',
        '<g:formatDate format="${logDateFormat}" date="${file.lastModified()}"/>',
        '<span title="${file.size}"><g:formatFileSize size="${file.size}"/></span>'],
  </g:each>
  ];
  </script>


<g:javascript library="jquery.dataTables.min"/>
<g:javascript library="DT_bootstrap"/>
<g:javascript>
  /* Table initialisation */
  $(document).ready(function() {
    $('#datatable').dataTable( {
      "aaData": aDataSet,
      "aoColumns": [
	    { "sTitle": "${message(code: 'logs.page.list.column.name')}" },
		{ "sTitle": "${message(code: 'logs.page.list.column.date')}" },
		{ "sTitle": "${message(code: 'logs.page.list.column.size')}",
		  "sType": "title-numeric" // sorts on title attribute, rather than text of the file size element
		}
	  ],
      "sDom": "<'row'<'span4'l><'pull-right'f>r>t<'row'<'span4'i><'pull-right'p>><'spacer'>",
      "sPaginationType": "bootstrap",
      "bStateSave": true,
      "oLanguage": {
        "sLengthMenu": "${message(code:'datatable.rowsPerPage')}",
        "oPaginate": {
            "sNext": "${message(code:'default.paginate.next')}",
            "sPrevious": "${message(code:'default.paginate.prev')}"
        },
        "sSearch": "${message(code:'default.filter.label')}",
        "sZeroRecords": "${message(code:'default.search.noResults.message')}",
        "sEmptyTable": "${message(code:'logs.page.list.noFilesFound')}",
        "sInfo": "${message(code:'datatable.showing')}",
        "sInfoEmpty": "${message(code:'datatable.showing.empty')}",
        "sInfoFiltered": " ${message(code:'datatable.filtered')}"
        },
      "aaSorting": [[ 0, "asc" ]]
    } );
  } );
</g:javascript>
</body>
</html>
