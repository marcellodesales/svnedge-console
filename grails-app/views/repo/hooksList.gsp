<g:applyLayout name="repoDetail">
 <content tag="headSnippet">
    <g:render template="/common/listViewResources"/>
 </content>

 <g:set var="listViewButtons">
    <g:listViewActionButton action="createHook" minSelected="0" maxSelected="0">
      <g:message code="default.button.create.label"/>
    </g:listViewActionButton>
    <g:listViewActionButton action="editHook" minSelected="1" maxSelected="1">
      <g:message code="default.button.edit.label"/>
    </g:listViewActionButton>
    <g:listViewActionButton action="copyHook" minSelected="1" maxSelected="1">
      <g:message code="repository.page.fileList.button.copy.label"/>
    </g:listViewActionButton>
    <g:listViewActionButton action="renameHook" minSelected="1" maxSelected="1">
      <g:message code="repository.page.fileList.button.rename.label"/>
    </g:listViewActionButton>
    <g:listViewActionButton action="downloadDumpFile" minSelected="1" maxSelected="1">
      <g:message code="repository.page.fileList.button.download.label"/>
    </g:listViewActionButton>
    <g:listViewActionButton action="deleteDumpFiles" minSelected="1" maxSelected="1"
        confirmMessage="${message(code:'repository.page.fileList.delete.confirmation')}">
      <g:message code="default.button.delete.label"/>
    </g:listViewActionButton>
 </g:set>

 <content tag="tabContent">
   <g:render template="/common/fileList" 
     model="${[fileList: hooksList, buttons: listViewButtons, 
               noFilesMessage: message(code: 'repository.page.hooksFileList.noFiles')]}" />
 </content>
 
</g:applyLayout>
