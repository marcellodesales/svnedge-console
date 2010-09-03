<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge</title>
  </head>
  <body>

    <content tag="title">
      <g:message code="status.page.header.title" />
    </content>
    
    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
  <g:form method="post">
  <div>
    <g:if test="${ctfUrl}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.url.teamforge" />:</strong> <a href="${ctfUrl}" target="_blank">${ctfUrl}</a>
      </div>
    </g:if>
    <g:if test="${isStarted}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.hostname" />: </strong> ${server.hostname}
      </div>
      <div class="ImageListParent">
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
          <div class="buttons" style="float: right">
            <span class="button"><g:actionSubmit class="stop" value="${message(code:'status.page.subversion.stop')}"/></span>
          </div>
        </g:ifAnyGranted>
        <strong><g:message code="status.page.subversion" />: </strong>
        <img src="${resource(dir:'images', file:'fping_up.gif')}" width="16" height="16"
                         hspace="4" alt="<g:message code='status.page.subversion.on' />"/><g:message code="status.page.subversion.on" />
      </div>
     <g:if test="${!ctfUrl && server.viewvcURL()}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.url.repository" />:</strong> <a href="${server.svnURL()}" target="_blank">${server.svnURL()}</a>
      </div>
      <div class="ImageListParent"><strong><g:message code="status.page.url.repository.browse" />:</strong>
        <a href="${server.viewvcURL()}" target="_blank">${server.viewvcURL()}</a>
      </div>
     </g:if>
   </g:if>
   <g:else>
      <div class="ImageListParent">
        <strong><g:message code="status.page.hostname" />: </strong> ${server.hostname}
      </div>
      <div class="ImageListParent">
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
          <div class="buttons" style="float: right">
            <span class="button"><g:actionSubmit value="${message(code:'status.page.subversion.start')}"/></span>
          </div>
        </g:ifAnyGranted>
        <strong><g:message code="status.page.subversion" />: </strong>
        <img src="${resource(dir:'images', file:'fping_down.gif')}" width="16" height="16"
                         hspace="4" alt="<g:message code='status.page.subversion.off' />"/><g:message code="status.page.subversion.off" />
      </div>
    </g:else>
  <img src="${resource(dir:'images/misc', file:'pixel.gif')}" width="280" height="1" alt=""/>
    </div>
    </g:form>

    <g:if test="${server.replica}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.replica.name" />: </strong> ${currentReplica.getName()}
      </div>
      <div class="ImageListParent">
        <strong><g:message code="status.page.replica.location" />:</strong> ${currentReplica.getLocationName()}
      </div>
      <div class="ImageListParent">
        <strong><g:message code="status.page.replica.master_hostname" />:</strong> ${defaultMaster.hostName}
      </div>
      <div class="ImageListParent">
        <strong><g:message code="status.page.replica.master_ssl" />:</strong> 
        <g:if test="${defaultMaster.sslEnabled}">
            <g:message code="status.page.replica.master_ssl.yes" />
        </g:if>
        <g:else>
            <g:message code="status.page.replica.master_ssl.no" />
        </g:else>
      </div>
    </g:if>
    </content>

    <div class="dialog">
      <table align="center" width="99%">
        <tbody>
          <tr><td>
            <table class="ItemDetailContainer">
              <tbody>
                <tr class="ContainerHeader">
                  <td colspan="2"><g:message code="status.page.header.server" /></td>
                </tr>
            <g:if test="${softwareVersion}">
                <tr class="prop, OddRow">
                  <td class="ItemDetailName"><strong><g:message code="status.page.status.version.software" /></strong></td>
                  <td class="ItemDetailValue">${softwareVersion}</td>
                </tr>
            </g:if>
            <g:if test="${svnVersion}">
                <tr class="prop, OddRow">
                  <td class="ItemDetailName"><strong><g:message code="status.page.status.version.subversion" /></strong></td>
                  <td class="ItemDetailValue">${svnVersion}</td>
                </tr>
            </g:if>
                <g:each status="i" var="stat" in="${perfStats}">
                  <tr class="prop, ${i % 2 == 0 ? 'EvenRow' : 'OddRow'}">
                    <td class="ItemDetailName"><strong>${stat.label}</strong></td>
                    <td class="ItemDetailValue">${stat.value}</td>
                  </tr>
                </g:each>

              </tbody>
            </table>
          </td></tr>
        </tbody>
      </table>
    </div>
  </body>
</html>
