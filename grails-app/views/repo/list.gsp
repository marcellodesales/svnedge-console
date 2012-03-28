<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>CollabNet Subversion Edge <g:message code=repository.page.list.header.title /></title>
        <link href="${resource(dir:'css',file:'DT_bootstrap.css')}" rel="stylesheet"/>
        <g:set var="adminView" value="${false}"/>
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_HOOKS">
          <g:set var="adminView" value="${true}"/>
        </g:ifAnyGranted>  
    </head>

<g:render template="leftNav" />

<content tag="title">
   <g:message code="repository.page.leftnav.title" />
</content>

<body>

<g:form>
    <table id="datatable" class="table table-striped table-bordered table-condensed tablesorter"></table>
    <script type="text/javascript">
    /* Data set */
    var aDataSet = [
    <g:each in="${repositoryInstanceList}" status="i" var="repositoryInstance">
      <g:if test="${adminView}">
        ['${repositoryInstance.id}',
          '${repositoryInstance.name}',
          '',
          '${repositoryInstance.id}|${(repositoryInstance.permissionsOk) ? message(code: "repository.page.list.instance.permission.ok") : message(code: "repository.page.list.instance.permission.needFix") }'
        ]<g:if test="${i < (repositoryInstanceList.size() - 1)}">,</g:if>
      </g:if> 
      <g:else>
        ['${repositoryInstance.name}', '']<g:if test="${i < (repositoryInstanceList.size() - 1)}">,</g:if>
       </g:else> 
    </g:each>
    ];
    </script>

<g:if test="${adminView}">
  <div class="pull-right">
  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
    <g:if test="${!isManaged}">
      <g:listViewActionButton action="create" minSelected="0" maxSelected="0"><g:message code="default.button.create.label" /></g:listViewActionButton>
    </g:if>
   <g:listViewActionButton action="discover" minSelected="0" maxSelected="0"><g:message code="repository.page.list.button.discover.label" /></g:listViewActionButton>
  </g:ifAnyGranted>
   <g:listViewActionButton action="show" minSelected="1" maxSelected="1"><g:message code="default.button.show.label" /></g:listViewActionButton>
  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
   <g:listViewActionButton action="dumpOptions" minSelected="1" maxSelected="1">
     <g:message code="repository.page.list.button.dump.label"/>
   </g:listViewActionButton>
   <g:listViewActionButton action="loadOptions" minSelected="1" maxSelected="1">
     <g:message code="repository.page.list.button.load.label"/>
   </g:listViewActionButton>
   <g:if test="${!isManaged}">
     <g:listViewActionButton action="deleteMultiple" minSelected="1" maxSelected="1"
                             confirmMessage="${message(code:'repository.page.list.delete.confirmation')}"
                             confirmByTypingThis="${message(code:'default.confirmation.typeThis')}">
       <g:message code="default.button.delete.label"/>
     </g:listViewActionButton>
   </g:if>
   </g:ifAnyGranted>
  </div>
</g:if>
</g:form>

<g:javascript library="jquery.dataTables.min"/>
<g:javascript library="DT_bootstrap"/>
<g:javascript>
  /* Table initialisation */
  $(document).ready(function() {
    var dt = $('#datatable').dataTable( {
      "aaData": aDataSet,
      "sDom": "<'row'<'span4'l><'pull-right'f>r>t<'row'<'span4'i><'pull-right'p>><'spacer'>",
      "sPaginationType": "bootstrap",
      "bStateSave": true,
      "fnStateSave": tableState.save('datatable'),
      "fnStateLoad": tableState.load('datatable'),
      "oLanguage": {
        "sLengthMenu": "${message(code:'datatable.rowsPerPage')}",
        "oPaginate": {
            "sNext": "${message(code:'default.paginate.next')}",
            "sPrevious": "${message(code:'default.paginate.prev')}"
        },
        "sSearch": "${message(code:'default.filter.label')}",
        "sZeroRecords": "${message(code:'default.search.noResults.message')}",
        "sEmptyTable": "${(isReplica) ? message(code:'repository.page.list.replica.noRepos') : message(code:'repository.page.list.noRepos')}",
        "sInfo": "${message(code:'datatable.showing')}",
        "sInfoEmpty": "${message(code:'datatable.showing.empty')}",
        "sInfoFiltered": " ${message(code:'datatable.filtered')}"
      },
      <g:if test="${adminView}">  
      "aaSorting": [[ 1, "asc" ]],
      "aoColumns": [
        {"sTitle": "", 
         "bSortable": false,
         "fnRender": function (oObj, sVal) {
           var template = '<input type="checkbox" class="listViewSelectItem radio" id="listViewItem_ID" name="listViewItem_ID"/>'
           return template.replace(/ID/g, sVal);
         }
        },
        {"sTitle": "${message(code:'repository.page.list.name')}",
         "fnRender": function ( oObj, sVal ) {
           <g:if test="${isManaged}">
           var template = 'REPO';
           </g:if>
           <g:else>
           var template = '<a href="${server.viewvcURL("REPO")}" target="_blank">REPO</a>';
           </g:else>
           return template.replace(/REPO/g, sVal);
         }
        },
        {"sTitle": "${message(code:'repository.page.list.checkout_command')}",
         "fnRender": function (oObj, sVal) {
           var template = 'svn co ${server.svnURL()}REPO REPO --username=<g:loggedInUsername/>';
           return template.replace(/REPO/g, $(oObj.aData[1]).text());
         }
        },
        {"sTitle": "${message(code:'repository.page.list.status')}",
         "bSortable": false ,
         "fnRender": function(oObj, sVal) {
           var template = '<g:link action="show" id="REPOID">MSG</g:link>';
           return template.replace("REPOID", sVal.split("|")[0]).replace("MSG", sVal.split("|")[1]);
         }
        }
      ]
      </g:if>
      <g:else>  
      "aaSorting": [[ 0, "asc" ]],
      "aoColumns": [
        {"sTitle": "${message(code:'repository.page.list.name')}",
         "fnRender": function ( oObj, sVal ) {
           var template = '<a href="${server.viewvcURL("REPO")}" target="_blank">REPO</a>';
           return template.replace(/REPO/g, sVal);
         }
        },
        {"sTitle": "${message(code:'repository.page.list.checkout_command')}",
         "fnRender": function (oObj, sVal) {
           var template = 'svn co ${server.svnURL()}REPO REPO --username=<g:loggedInUsername/>';
           return template.replace(/REPO/g, $(oObj.aData[0]).text());
         }
        }
      ]
      </g:else>
    } );
    
    // limit filter to column 1 only (the repo name)
    filterElement= $('#datatable_filter').find("input")
  	filterElement.keyup( function () {
        dt.fnFilter(filterElement.attr("value"), 1);
        applyCheckboxObserver();
        updateActionButtons();
    } );
  } );
</g:javascript>
<g:javascript library="listView-3.0.0"/>

</body>
</html>
