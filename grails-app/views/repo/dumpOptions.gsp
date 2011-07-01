<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.dump.title" /></title>
        <g:javascript library="prototype" />
    </head>

<g:render template="leftNav" />

<content tag="title">
     <g:message code="repository.page.leftnav.title" />
</content>

    <body>
<div>


    <table class="ItemDetailContainer">
      <thead>
          <tr class="ContainerHeader">
             <td colspan="5"><g:message code="repository.page.dump.title" /></td>
          </tr>
      </thead>
      <tbody>
        <tr>
          <td class="ContainerBodyWithPaddedBorder">
            <table class="ItemDetailContainer">
              <tbody>
                <tr class="prop">
                  <td valign="top" class="name ItemDetailName"><g:message code="repository.page.show.name" /></td>
                  <td valign="top" class="value ItemDetailValue">
                    ${fieldValue(bean:repositoryInstance, field:'name')}
                  </td>
                </tr>
                <tr class="prop">
                  <td valign="top" class="name ItemDetailName"><g:message code="repository.page.show.status" /></td>
                  <td valign="top" class="value ItemDetailValue"><g:if test="${repositoryInstance.permissionsOk}">
                    <span style="color: green"><g:message code="repository.page.list.instance.permission.ok" /></span>
                    </g:if> <g:else>
                      <span style="color: red"><g:message code="repository.page.list.instance.permission.needFix" /></span>
                    </g:else>
                  </td>
                </tr>

                <tr class="prop">
                  <td valign="top" class="name ItemDetailName"><g:message code="repository.page.dump.headRevision" /></td>
                  <td valign="top" class="value ItemDetailValue">${headRev}</td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </tbody>
    </table>

    <g:form action="createDumpFile">
    <table id="dumpOptionsTable" class="ItemDetailContainer">
      <thead>
        <tr class="ContainerHeader">
            <td colspan="5"><g:message code="repository.page.dump.subtitle" /></td>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td class="ContainerBodyWithPaddedBorder">
            <table id="dumpOptionsInnerTable" class="ItemDetailContainer">
              <tbody>
                <tr>
                  <td class="ItemDetailName">
                    <label for="revisionRange"><g:message code="repository.page.dump.revisionRange.label" /></label>
                  </td>
                  <td valign="top" class="value">
                    <input name="revisionRange" id="revisionRange" type="text" value="${dump.revisionRange}"/>
                  </td>
                  <td class="ItemDetailValue"><g:message code="repository.page.dump.revisionRange.tip" /></td>
                </tr>
                <g:hasErrors bean="${dump}" field="revisionRange">
                  <tr>
                    <td>&nbsp;</td>
                    <td colspan="2" width="100%" valign="top" class="errors">
                      <ul><g:eachError bean="${dump}" field="revisionRange">
                        <li><g:message error="${it}" encodeAs="HTML"/></li>
                      </g:eachError></ul>
                    </td>
                  </tr>
                </g:hasErrors>

                <tr>
                  <td class="ItemDetailName">
                    <label for="incremental"><g:message code="repository.page.dump.incremental.label" /></label>
                  </td>
                  <td class="value">
                    <g:checkBox name="incremental" id="incremental" value="${dump.incremental}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.dump.incremental.tip" />
                  </td>
                </tr>

                <tr>
                  <td class="ItemDetailName">
                    <label for="deltas"><g:message code="repository.page.dump.deltas.label" /></label>
                  </td>
                  <td class="value">
                    <g:checkBox name="deltas" id="deltas" value="${dump.deltas}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.dump.deltas.tip" />
                  </td>
                </tr>

                <tr>
                  <td class="ItemDetailName">
                    <label for="compress"><g:message code="repository.page.dump.compress.label" /></label>
                  </td>
                  <td class="value">
                    <g:checkBox name="compress" id="compress" value="${dump.compress}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.dump.compress.tip" />
                  </td>
                </tr>

                <tr>
                  <td class="ItemDetailName">
                    <label for="filter"><g:message code="repository.page.dump.filter.label" /></label>
                  </td>
                  <td class="value">
                    <g:checkBox name="filter" id="filter" value="${dump.filter}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.dump.filter.tip" />
                  </td>
                </tr>
                
                <tr>
                 <td><div id="filterOptionsSpacer">&nbsp;</div></td>
                 <td colspan="2">
                   <div  id="filterOptions">
                   <table id="filterOptionsTable" class="ItemDetailContainer">
                     <tbody>
                       <tr>
                         <td class="ItemDetailName">
                           <label for="includePath"><g:message code="repository.page.dump.filter.include.label" /></label>
                         </td>
                         <td valign="top" class="value">
                           <input name="includePath" id="includePath" type="text" size="60" value="${dump.includePath}"/>
                         </td>
                         <td class="ItemDetailValue"><g:message code="repository.page.dump.filter.include.tip" /></td>
                       </tr>
                       <tr>
                         <td class="ItemDetailName">
                           <label for="excludePath"><g:message code="repository.page.dump.filter.exclude.label" /></label>
                         </td>
                         <td valign="top" class="value">
                           <input name="excludePath" id="excludePath" type="text" size="60" value="${dump.excludePath}"/>
                         </td>
                         <td class="ItemDetailValue"><g:message code="repository.page.dump.filter.exclude.tip" /></td>
                       </tr>
                       <tr>
                         <td class="ItemDetailName"><label for="dropEmptyRevs"><g:message
                           code="repository.page.dump.filter.drop-empty-revs.label" /></label></td>
                         <td colspan="2" class="value ItemDetailValue">
                           <g:checkBox name="dropEmptyRevs" id="dropEmptyRevs" value="${dump.dropEmptyRevs}" /> <g:message
                             code="repository.page.dump.filter.drop-empty-revs.tip" /></td>
                       </tr>
                       <tr>
                         <td class="ItemDetailName"><label for="renumberRevs"><g:message
                           code="repository.page.dump.filter.renumber-revs.label" /></label></td>
                         <td colspan="2" class="value ItemDetailValue">
                           <g:checkBox name="renumberRevs" id="renumberRevs" value="${dump.renumberRevs}" /> <g:message
                             code="repository.page.dump.filter.renumber-revs.tip" /></td>
                       </tr>
                       <tr>
                         <td class="ItemDetailName">
                           <label for="preserveRevprops"><g:message code="repository.page.dump.filter.preserve-revprops.label" /></label>
                         </td>
                         <td colspan="2" class="value ItemDetailValue">
                           <g:checkBox name="preserveRevprops" id="preserveRevprops" value="${dump.preserveRevprops}"/>
                           <g:message code="repository.page.dump.filter.preserve-revprops.tip" />
                         </td>
                       </tr>
                       <tr>
                         <td class="ItemDetailName">
                           <label for="skipMissingMergeSources"><g:message code="repository.page.dump.filter.skip-missing-merge-sources.label" /></label>
                         </td>
                         <td colspan="2" class="value ItemDetailValue">
                           <g:checkBox name="skipMissingMergeSources" id="skipMissingMergeSources" value="${dump.skipMissingMergeSources}"/>
                           <g:message code="repository.page.dump.filter.skip-missing-merge-sources.tip" />
                         </td>
                       </tr>
                     </tbody>
                   </table> <!-- filterOptionsTable -->
                   </div>
                 </td>
                </tr>  
                <tr class="ContainerFooter">
                  <td colspan="5">
                    <div class="AlignRight">
                        <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                        <span><g:submitButton name="cancelButton" value="${message(code:'default.confirmation.cancel')}" class="Button"/></span>
                        <span><g:submitButton name="dumpButton" value="${message(code:'repository.page.dump.button.dump')}" class="Button"/></span>
                    </div>
                  </td>
                </tr>
             </tbody>
           </table> <!-- dumpOptionsInnerTable -->
         </td>
       </tr>
    </tbody>
  </table> <!-- dumpOptionsTable -->
  </g:form>                

</div>  

<g:javascript>
    function filterHandler() {
      if ($('filter').checked) {
        $('filterOptions').style.display = 'block';
        $('filterOptionsSpacer').style.display = 'block';
      } else {
        $('filterOptions').style.display = 'none';
        $('filterOptionsSpacer').style.display = 'none';
      }
    }
    $('filter').onchange = filterHandler

    function dropEmptyRevsHandler() {
        var renumberRevs = $('renumberRevs');
        var revProps = $('preserveRevprops');
        if ($('dropEmptyRevs').checked) {
            renumberRevs.disabled = false;
            revProps.checked = false;
            revProps.disabled = true;
        } else {
            revProps.disabled = false;
            renumberRevs.checked = false;
            renumberRevs.disabled = true;
        }
    }
    $('dropEmptyRevs').onchange = dropEmptyRevsHandler;

    function deltasHandler() {
        var filter = $('filter');
        if ($('deltas').checked) {
            filter.checked = false;
            filter.disabled = true;
        } else {
            filter.disabled = false;
        }
        filterHandler();
    }
    $('deltas').onchange = deltasHandler;

    function loadHandler() {
        deltasHandler();
        filterHandler();
        dropEmptyRevsHandler();
    }
    Event.observe(window, 'load', loadHandler);    

  </g:javascript>


</body>
</html>
