<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title><g:message code="repository.page.load.title" /></title>
        <g:javascript library="prototype" />
        
        <g:javascript>
/** Handle for the polling function */
var periodicUpdater

// instantiate the polling task on load
Event.observe(window, 'load', function() {
    $('loadButton').onclick = function() {
        $('uploadProgress').style.display = '';
        // re-animate IE
        setTimeout(function() { 
            $('uploadSpinner').src = '/csvn/images/spinner.gif';
        }, 100);
        return true;
    }
    
    $('loadFileUpload').onsubmit = function() {
        $('loadButton').disabled = true;
        periodicUpdater = new PeriodicalExecuter(fetchUploadProgress, 5);
        return true;
    }        
})

/** function to fetch replication info and update ui */
function fetchUploadProgress() {
    new Ajax.Request('/csvn/repo/uploadProgress', {
        method:'get',
        parameters: {uploadProgressKey:'${uploadProgressKey}', avoidCache: new Date().getTime()},
        requestHeaders: {Accept: 'text/json'},
        onSuccess: function(transport) {
            var responseData = transport.responseText.evalJSON(true);
            var percentComplete = responseData.uploadStats.percentComplete;
            $('percentComplete').innerHTML = percentComplete + '%';
        }
    })
}
        </g:javascript>
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

    <g:uploadForm action="loadFileUpload" name="loadFileUpload" params="${[uploadProgressKey: uploadProgressKey]}">
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
                <g:if test="${headRev > 0}">
                  <tr>
                    <td colspan="4">
                      <div class="instructionText"><g:message code="repository.page.load.not.empty.message"/></div>
                    </td>
                  <tr>
                </g:if>
                <tr>
                  <td class="ItemDetailName">
                    <label for="dumpFile"><g:message code="repository.page.load.fileupload.label" /></label>
                  </td>
                  <td valign="top" class="value">
                    <input style="float: left" name="dumpFile" id="dumpFile" type="file"/>
                  </td>
                  <td valign="top" class="value">                  
                        <span id="uploadProgress" style="display: none;">
                          <img id="uploadSpinner" class="spinner" align="middle"  src="/csvn/images/spinner.gif" /><g:message 
                              code="repository.page.load.uploading.ellipses"/>&nbsp;<span id="percentComplete"></span></span>
                   </td>
                  <td class="ItemDetailValue"><g:message code="repository.page.load.fileupload.tip" /> </td>
                </tr>
                 <g:if test="${headRev == 0}">
                  <tr>
                  <td class="ItemDetailName">
                    <label for="ignoreUuid"><g:message code="repository.page.load.ignoreUuid.label" /></label>
                  </td>
                  <td class="value" colspan="2">
                    <g:checkBox name="ignoreUuid" id="ignoreUuid" value="${params.ignoreUuid}"/>
                  </td>
                  <td class="ItemDetailValue">
                    <g:message code="repository.page.load.ignoreUuid.tip" />
                  </td>
                  </tr>
                 </g:if>

                <tr class="ContainerFooter">
                  <td colspan="5">
                    <div class="AlignRight">
                        <input type="hidden" name="id" value="${repositoryInstance?.id}" />
                        <span><g:submitButton name="cancelButton" value="${message(code:'default.confirmation.cancel')}" class="Button"/></span>
                        <span><g:submitButton id="loadButton" name="loadButton" value="${message(code:'repository.page.load.button.label')}" class="Button"/></span>
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
</body>
</html>
