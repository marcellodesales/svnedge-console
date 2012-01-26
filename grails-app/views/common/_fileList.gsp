

<g:if test="${fileList?.size() > 0}">
  <g:form>
  <input type="hidden" name="id" value="${params.id}" />
  <table class="Container">
  <tbody>
    <tr class="ItemListHeader">
      <th><g:listViewSelectAll/></th>
      <g:sortableColumn property="name" titleKey="repository.page.fileList.filename" />
      <g:sortableColumn property="date" titleKey="repository.page.fileList.timestamp" />
      <g:sortableColumn property="size" titleKey="repository.page.fileList.fileSize" />
    </tr>
    <g:each in="${fileList}" status="i" var="file">
       <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
         <td><g:listViewSelectItem item="${file}" property="name"/></td>
         <td><a href="${createLink(action: 'downloadHookFile', id: params.id, params: [filename: file.name])}">${file.name}</a></td>

         <td><g:formatDate format="yyyy-MM-dd" date="${new java.util.Date(file.lastModified())}"/></td>
         <td><g:formatFileSize size="${file.length()}" /></td>
       </tr>
    </g:each>
                
    <tr class="ContainerFooter">
       <td colspan="4">
         <div class="AlignRight">
         <%=buttons%>
         </div>
       </td>
    </tr>
  </tbody>
  </table>
  </g:form>
</g:if>
<g:else>
    <p>${noFilesMessage}</p>
</g:else>
