<g:applyLayout name="repoDetail">
 <content tag="headSnippet">
    <g:render template="/common/listViewResources"/>
 </content>

 <content tag="tabContent">
    <g:form>
      <input type="hidden" name="id" value="${params.id}" />
    <g:if test="${dumpFileList.size() > 0}">
      <table class="Container">
      <tbody>
        <tr class="ItemListHeader">
          <th><g:listViewSelectAll/></th>
          <g:sortableColumn property="name" titleKey="repository.page.dumpFileList.filename" />
          <g:sortableColumn property="date" titleKey="repository.page.dumpFileList.timestamp" defaultOrder="desc"/>
          <g:sortableColumn property="size" titleKey="repository.page.dumpFileList.fileSize" />
        </tr>
        <g:each in="${dumpFileList}" status="i" var="file">
           <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
             <td><g:listViewSelectItem item="${file}" property="name"/></td>
             <td><a href="${createLink(action: 'downloadDumpFile', id: params.id, params: [filename: file.name])}">${file.name}</a></td>

             <td><g:formatDate format="yyyy-MM-dd" date="${new java.util.Date(file.lastModified())}"/></td>
             <td><g:formatFileSize size="${file.length()}" /></td>
           </tr>
        </g:each>
                    
        <tr class="ContainerFooter">
           <td colspan="4">
             <div class="AlignRight">
                 <g:listViewActionButton action="downloadDumpFile" minSelected="1" maxSelected="1">
                   <g:message code="repository.page.dumpFileList.button.download.label"/>
                 </g:listViewActionButton>
               <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_REPO">
                 <g:listViewActionButton action="deleteDumpFiles" minSelected="1" maxSelected="1"
                     confirmMessage="${message(code:'repository.page.dumpFileList.delete.confirmation')}">
                   <g:message code="default.button.delete.label"/>
                 </g:listViewActionButton>
               </g:ifAnyGranted>
             </div>

           </td>
        </tr>
      </tbody>
    </table>

    </g:if>
    <g:else>
        <p><g:message code="repository.page.dumpFileList.noFiles" /></p>
    </g:else>

      </g:form>
  </content>
</g:applyLayout>
