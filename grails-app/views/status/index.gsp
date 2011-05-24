<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge</title>

  <g:if test="${isReplicaMode}">
      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
                djconfig="parseOnLoad: false, isDebug: false"></script>

      <g:set var="no_commands" value="${message(code:'status.page.status.replication.no_commands')}" />
      <g:set var="commands_running" value="${message(code:'status.page.status.replication.commands_running')}" />

      <script type="text/javascript">
        /**
         * Author: Marcello de Sales (mdesales@collab.net)
         * Date: May 16, 2011
         */
        dojo.require('dojox.cometd');
        dojo.require("dijit.Dialog");

        /** The counter cometd channel. */
        var statusCounterChannel = "/replica/status/counter";

        /** The current number of commands at the time the page loads. */
        var currentNumberCommands = ${replicaCommandsSize}

        /**
         * If there are commands running, then print the number and 
         */
        function updateUiCommandsRunning(numberOfCommands) {
            if (numberOfCommands > 0) {
                dojo.byId('spinner').src = '/csvn/images/replica/commands_updating_spinner.gif';
                dojo.byId('commandsCount').innerHTML = "${commands_running}" + numberOfCommands;

            } else {
                dojo.byId('spinner').src = '/csvn/images/fping_up.gif';
                dojo.byId('commandsCount').innerHTML = "${no_commands}";
            }
        }

        /**
         * Function instance that is used to initialize upgrade process. Namely,
         * the dojox.cometd component, as well as the major UI objects for the 
         * initial status.
         */
        var init = function() {
            dojox.cometd.init('/csvn/plugins/cometd-0.1.5/cometd');
            dojox.cometd.subscribe(statusCounterChannel, onMessage);

            updateUiCommandsRunning(currentNumberCommands)
        };
        dojo.addOnLoad(init);

        /**
         * Function instance that is used to finish the upgrade process. It
         * finishes the dojox.cometd components.
         */
        var destroy = function() {
            dojox.cometd.unsubscribe(statusCounterChannel);
            dojox.cometd.disconnect();
        };
        dojo.addOnUnload(destroy);

        /**
         * Callback function for the cometd client that receives the messages
         * from the subscribed channels.
         * @param m is the message object proxied by the cometd server from 
         * one of the subscribed channels.
         * @see init()
         */
        function onMessage(m) {
            var c = m.channel;
            var o = eval('('+m.data+')')
            if (c == statusCounterChannel) {
                updateUiCommandsRunning(o.total)
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
        <strong><g:message code="status.page.replica.name" /></strong> ${currentReplica.name}</a>
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
             <div id="commandsCount">${replicaCommandsSize == 0 ? no_commands : (commands_running + " " +replicaCommandsSize)}</div>
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
