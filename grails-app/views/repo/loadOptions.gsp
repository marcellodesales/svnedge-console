<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.load.title" /></title>
        <g:javascript library="prototype" />
    </head>

<g:render template="leftNav" />

<content tag="title">
     <g:message code="repository.page.load.title" />
</content>

    <body>
<div>


    <table class="ItemDetailContainer">
      <thead>
          <tr class="ContainerHeader">
             <td colspan="5"><g:message code="repository.page.load.title" /></td>
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
                <tr class="prop">
                  <td valign="top" class="name ItemDetailName"><g:message code="repository.page.show.uuid" /></td>
                  <td valign="top" class="value ItemDetailValue">${repoUUID}</td>
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </tbody>
    </table>

    <g:uploadForm action="loadFileUpload">
    <table id="dumpOptionsTable" class="ItemDetailContainer">
      <thead>
        <tr class="ContainerHeader">
            <td colspan="5"><g:message code="repository.page.load.subtitle" /></td>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td class="ContainerBodyWithPaddedBorder">
            <table id="loadOptionsInnerTable" class="ItemDetailContainer">
              <tbody>
                <tr>
                  <td class="ItemDetailName">
                    <label for="dumpFile"><g:message code="repository.page.load.fileupload.label" /></label>
                  </td>
                  <td valign="top" class="value">
                    <input name="dumpFile" id="dumpFile" type="file"/>
                  </td>
                  <td class="ItemDetailValue"><g:message code="repository.page.load.fileupload.tip" /></td>
                </tr>

                <tr>
                  <td class="ItemDetailName">
                    <label for="ignoreUuid"><g:message code="repository.page.load.ignoreUuid.label" /></label>
                  </td>
                  <td class="value">
                    <g:checkBox name="ignoreUuid" id="ignoreUuid" value="${params.ignoreUuid}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.load.ignoreUuid.tip" />
                  </td>
                </tr>

                <tr class="ContainerFooter">
                  <td colspan="5">
                    <div class="AlignRight">
                        <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                        <span><g:submitButton name="cancelButton" value="${message(code:'default.confirmation.cancel')}" class="Button"/></span>
                        <span><g:submitButton name="dumpButton" value="${message(code:'repository.page.load.button.label')}" class="Button"/></span>
                    </div>
                  </td>
                </tr>
             </tbody>
           </table> <!-- loadOptionsInnerTable -->
         </td>
       </tr>
    </tbody>
  </table> <!-- loadOptionsTable -->
  </g:uploadForm>                

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
    //$('filter').onchange = filterHandler

    function loadHandler() {
        //filterHandler();
    }
    Event.observe(window, 'load', loadHandler);    

  </g:javascript>


</body>
</html>
