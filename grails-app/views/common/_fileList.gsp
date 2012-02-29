

<g:if test="${fileList?.size() > 0}">
  <g:form>
  <input type="hidden" name="id" value="${params.id}" />
  <table class="table table-striped table-bordered table-condensed">
  <thead>
    <tr class="ItemListHeader">
      <th><g:if test="${!radioStyle}"><g:listViewSelectAll/></g:if></th>
      <g:sortableColumn property="name" titleKey="repository.page.fileList.filename" />
      <g:sortableColumn property="date" titleKey="repository.page.fileList.timestamp" />
      <g:sortableColumn property="size" titleKey="repository.page.fileList.fileSize" />
    </tr>
  </thead>
  <tbody>  
    <g:each in="${fileList}" status="i" var="file">
       <tr>
         <td><g:listViewSelectItem item="${file}" property="name" radioStyle="${radioStyle}"/></td>
         <td><a href="${createLink(action: linkAction, id: params.id, params: [filename: file.name])}">${file.name}</a></td>

         <td><g:formatDate format="yyyy-MM-dd" date="${new java.util.Date(file.lastModified())}"/></td>
         <td><g:formatFileSize size="${file.length()}" /></td>
       </tr>
    </g:each>
  </tbody>
  </table>

  <div class="pull-right">
    <%=buttons%>
  </div>
  </g:form>
</g:if>
<g:else>
    <p>${noFilesMessage}</p>
</g:else>
