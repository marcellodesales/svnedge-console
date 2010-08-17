    <g:render template="/common/tabs"
        model="[tabs:[
            [action:'available', label:'Updates'],
            [action:'addOns', label:'New Packages'],
            [action:'installed', label:'Installed Packages']]]" />

    <table class="Container">
      <tbody>
        <tr><td colspan="5"><strong>Packages repository:</strong>
            ${imageOriginUrl} <g:if test="${proxyToOriginURL}"><strong> through
                the proxy server </strong>${proxyToOriginURL}
               </g:if></td></tr>

    <g:if test="${(!anyConnectionProblem && packagesInfo && packagesInfo.size() > 0) || 
                  (actionName == 'installed' && packagesInfo && packagesInfo.size() > 0)}">
        <tr class="ItemListHeader">
          <g:sortableColumn property="summary" title="Name"/>
          <g:sortableColumn property="publishedDate" title="Published Date"/>
          <g:sortableColumn property="release" title="Release"/>
          <g:sortableColumn property="branch" title="Build"/>
          <g:sortableColumn property="size" title="Size"/>
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
                No Software Updates Available.
            </g:if>
            <g:else>
                <g:if test="${actionName == 'addOns'}"> 
                    No New Packages Available.
                </g:if>
                <g:else>
                    Installed packages list not available!
                </g:else>
            </g:else>
        </td>
      </tr>
    </g:else>

      </tbody>
    </table>