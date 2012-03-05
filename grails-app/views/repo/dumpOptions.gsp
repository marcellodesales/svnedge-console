<html>
<head>
  <meta name="layout" content="main" />
</head>

<g:render template="leftNav" />

<content tag="title">
  <g:message code="repository.page.dump.title" />
</content>

<body>
  <div>
  <table>
              <tbody>
                <tr>
                  <td style="padding-right: 20px"><g:message code="repository.page.dump.repo.name" /></td>
                  <td>${repositoryInstance.name}</td>
                </tr>
                <tr>
                  <td><g:message code="repository.page.show.status" /></td>
                  <td><g:if test="${repositoryInstance.permissionsOk}">
                    <span style="color: green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                    </g:if> <g:else>
                      <span style="color: red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
                    </g:else>
                  </td>
                </tr>

                <tr>
                  <td><g:message code="repository.page.dump.headRevision" /></td>
                  <td>${headRev}</td>
                </tr>
              </tbody>
            </table>
  </div>

    <h3><g:message code="repository.page.dump.subtitle" /></h3>
    <g:form class="form-horizontal" action="createDumpFile">
      <input type="hidden" name="id" value="${repositoryInstance?.id}" />
      <g:propTextField bean="${dump}" field="revisionRange" prefix="repository.page.dump"/>
      <g:propCheckBox bean="${dump}" field="incremental" prefix="repository.page.dump"/>
      <g:propCheckBox bean="${dump}" field="compress" prefix="repository.page.dump"/>
      <g:propCheckBox bean="${dump}" field="filter" prefix="repository.page.dump"/>
      <div id="filterOptions">
      <fieldset>
        <legend><small><g:message code="repository.page.dump.filterOptions"/></small></legend>
        <g:propTextField bean="${dump}" field="includePath" prefix="repository.page.dump.filter" sizeClass="span6"/>
        <g:propTextField bean="${dump}" field="excludePath" prefix="repository.page.dump.filter" sizeClass="span6"/>
        <g:propCheckBox bean="${dump}" field="dropEmptyRevs" prefix="repository.page.dump.filter"/>
        <g:propCheckBox bean="${dump}" field="renumberRevs" prefix="repository.page.dump.filter"/>
        <g:propCheckBox bean="${dump}" field="preserveRevprops" prefix="repository.page.dump.filter"/>
        <g:propCheckBox bean="${dump}" field="skipMissingMergeSources" prefix="repository.page.dump.filter"/>
      </fieldset>
      </div>
      <div class="form-actions">    
        <g:submitButton name="dumpButton" value="${message(code:'repository.page.dump.button.dump')}" class="btn btn-primary"/>
        <g:submitButton name="cancelButton" value="${message(code:'default.confirmation.cancel')}" class="btn"/>
      </div>
  </g:form>                

<g:javascript>
    function filterHandler() {
      if ($('#filter').attr('checked')) {
        $('#filterOptions').show();
        $('#filterOptionsSpacer').show();
      } else {
        $('#filterOptions').hide();
        $('#filterOptionsSpacer').hide();
      }
    }
    $('#filter').change(filterHandler);

    function dropEmptyRevsHandler() {
        var renumberRevs = $('#renumberRevs');
        var revProps = $('#preserveRevprops');
        if ($('#dropEmptyRevs').attr('checked')) {
            renumberRevs.attr('disabled', false);
            revProps.attr('checked', false);
            revProps.attr('disabled', true);
        } else {
            revProps.attr('disabled', false);
            renumberRevs.attr('checked', false);
            renumberRevs.attr('disabled', true);
        }
    }
    $('#dropEmptyRevs').change(dropEmptyRevsHandler);

    function deltasHandler() {
        var filter = $('#filter');
        if ($('#deltas').attr('checked')) {
            filter.attr('checked', false);
            filter.attr('disabled', true);
        } else {
            filter.attr('disabled', false);
        }
        filterHandler();
    }
    $('#deltas').change(deltasHandler);

    function loadHandler() {
        // deltasHandler calls filterHandler
        deltasHandler();
        dropEmptyRevsHandler();
    }
    $(document).ready(loadHandler);
  </g:javascript>


</body>
</html>
