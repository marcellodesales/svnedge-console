   <g:set var="tabArray" value="${[[action:'available', label: message(code:'packagesUpdate.page.tabs.updates')]]}" />
   <g:set var="tabArray" value="${tabArray << [action:'addOns', label: message(code:'packagesUpdate.page.tabs.addOns')]}" />
   <g:set var="tabArray" value="${tabArray << [action:'installed', label: message(code:'packagesUpdate.page.tabs.installed')]}" />
   <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

    <table class="Container">
      <tbody>
        <tr><td colspan="5"><strong><g:message code="packagesUpdate.page.table.repository" />:</strong>
            ${imageOriginUrl} <g:if test="${proxyToOriginURL}"><strong> <g:message code="packagesUpdate.page.table.throughProxy" /> </strong>${proxyToOriginURL}
               </g:if></td></tr>

    <g:if test="${(!anyConnectionProblem && packagesInfo && packagesInfo.size() > 0) || 
                  (actionName == 'installed' && packagesInfo && packagesInfo.size() > 0)}">
        <tr class="ItemListHeader">
          <g:sortableColumn property="summary" title="${message(code:'packagesUpdate.page.table.column.summary')}"/>
          <g:sortableColumn property="publishedDate" title="${message(code:'packagesUpdate.page.table.column.publishedDate')}"/>
          <g:sortableColumn property="release" title="${message(code:'packagesUpdate.page.table.column.release')}"/>
          <g:sortableColumn property="branch" title="${message(code:'packagesUpdate.page.table.column.branch')}"/>
          <g:sortableColumn property="size" title="${message(code:'packagesUpdate.page.table.column.size')}"/>
        </tr>
      <g:each var="packageInfo" in="${packagesInfo}" status="rowNumber">

        <tr class="${(rowNumber % 2) == 0 ? 'OddRow' : 'EvenRow'}">
          <td width="60%"><b>${packageInfo.summary}</b><BR>
                 ${packageInfo.description}
          </td>
          <td width="17%"><g:formatDate date="${packageInfo.publishedDate}" 
                format="yyyy-MM-dd HH:mm:ss Z"/>
          </td>
          <td width="6%">${packageInfo.release}</td>
          <td width="6%">${packageInfo.branch}</td>
          <td width="6%">${packageInfo.sizeInMB} MB</td>
        </tr>

      </g:each>
    </g:if>
    <g:else>
      <tr class="ItemListNoData">
        <td colspan="3">
            <g:if test="${actionName == 'available'}"> 
                <g:message code="packagesUpdate.page.table.noUpdatesAvilable" />.
            </g:if>
            <g:else>
                <g:if test="${actionName == 'addOns'}"> 
                    <g:message code="packagesUpdate.page.table.noNewPackagesAvilable" />.
                </g:if>
                <g:else>
                    <g:message code="packagesUpdate.page.table.noPackagesInstalled" />
                </g:else>
            </g:else>
        </td>
      </tr>
    </g:else>

      </tbody>
    </table>