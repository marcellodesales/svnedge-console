<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge</title>

  <g:if test="${isReplicaMode}">

      <g:javascript library="prototype" />
      <script type="text/javascript">

        /** Handle for the polling function */
        var periodicUpdater

        // instantiate the polling task on load
        Event.observe(window, 'load', function() {
            periodicUpdater = new PeriodicalExecuter(fetchReplicationData, 1)
        })

        /** function to fetch replication info and update ui */
        function fetchReplicationData() {

          new Ajax.Request('/csvn/status/replicationInfo', {
                  method:'get',
                  requestHeaders: {Accept: 'text/json'},
                  onSuccess: function(transport) {
                     responseData = transport.responseText.evalJSON(true);
                     numberOfCommands = responseData.relicaServerInfo.runningCmdsSize
                     updateUiCommandsRunning(numberOfCommands)
                  }
          })
        }

        /**
         * If there are commands running, then print the number and 
         */
        function updateUiCommandsRunning(numberOfCommands) {
            if (numberOfCommands > 0) {
                $('spinner').src = '/csvn/images/replica/commands_updating_spinner.gif';
                $('commandsCount').innerHTML = '<g:message code="status.page.status.replication.commands_running"/> ' + numberOfCommands;
            } else {
                $('spinner').src = '/csvn/images/fping_up.gif';
                $('commandsCount').innerHTML = '<g:message code="status.page.status.replication.no_commands"/>'; 
            }
        }
    </script>
   </g:if>

  </head>
  <body>

    <content tag="title">
      <g:message code="status.page.header.title" />
    </content>
    
    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
  <g:form method="post">
  <div>
    <g:if test="${isReplicaMode}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.replica.name" /></strong> ${currentReplica.name}
      </div>
      <g:if test="${currentReplica.svnMasterUrl}">
         <div class="ImageListParent">
           <strong><g:message code="status.page.replica.location" /></strong> ${currentReplica.svnMasterUrl}
         </div>
      </g:if>
    </g:if>
    <g:if test="${ctfUrl}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.url.teamforge" /></strong> <a href="${ctfUrl}" target="_blank">${ctfUrl}</a>
      </div>
    </g:if>
    <g:if test="${isStarted}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.hostname" /> </strong> ${server.hostname}
      </div>
      <div class="ImageListParent">
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
          <div class="buttons" style="float: right">
            <span class="button"><g:actionSubmit class="stop" value="${message(code:'status.page.subversion.stop')}" action="stop"/></span>
          </div>
        </g:ifAnyGranted>
        <strong><g:message code="status.page.subversion" /> </strong>
        <img src="${resource(dir:'images', file:'fping_up.gif')}" width="16" height="16"
                         hspace="4" alt="<g:message code='status.page.subversion.on' />"/><g:message code="status.page.subversion.on" />
      </div>
     <g:if test="${!ctfUrl && server.viewvcURL()}">
      <div class="ImageListParent">
        <strong><g:message code="status.page.url.repository" /></strong> <a href="${server.svnURL()}" target="_blank">${server.svnURL()}</a>
      </div>
      <div class="ImageListParent"><strong><g:message code="status.page.url.repository.browse" /></strong>
        <a href="${server.viewvcURL()}" target="_blank">${server.viewvcURL()}</a>
      </div>
     </g:if>
      <g:if test="${isReplicaMode}">
        <div class="ImageListParent"><strong><g:message code="status.page.status.replication.activity" /></strong>
        <g:set var="replicationStatusIcon" value="fping_up.gif" />
        <g:if test="${replicaCommandsSize > 0}">
            <g:set var="replicationStatusIcon" value="replica/commands_updating_spinner.gif" />
        </g:if>
        <img src="/csvn/images/${replicationStatusIcon}" id="spinner">
             <div id="commandsCount">
               <g:if test="${replicaCommandsSize == 0}">
                 <g:message code="status.page.status.replication.no_commands"/>
               </g:if>
               <g:else>
                 <g:message code="status.page.status.replication.commands_running"/> ${replicaCommandsSize}
               </g:else>
             </div>
        </div>
      </g:if>
   </g:if>
   <g:else>
      <div class="ImageListParent">
        <strong><g:message code="status.page.hostname" /> </strong> ${server.hostname}
      </div>
      <div class="ImageListParent">
        <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
          <div class="buttons" style="float: right">
            <span class="button"><g:actionSubmit value="${message(code:'status.page.subversion.start')}" action="start"/></span>
          </div>
        </g:ifAnyGranted>
        <strong><g:message code="status.page.subversion" /> </strong>
        <img src="${resource(dir:'images', file:'fping_down.gif')}" width="16" height="16"
                         hspace="4" alt="<g:message code='status.page.subversion.off' />"/><g:message code="status.page.subversion.off" />
      </div>
    </g:else>
  <img src="${resource(dir:'images/misc', file:'pixel.gif')}" width="280" height="1" alt=""/>
    </div>
    </g:form>

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
                <td class="ItemDetailValue">${stat.value ?: message(code:'status.page.status.noData')}</td>
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
