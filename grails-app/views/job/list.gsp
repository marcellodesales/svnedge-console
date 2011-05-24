<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="job.page.list.title"/></title>

      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
                djconfig="parseOnLoad: false, isDebug: false"></script>

      <g:set var="noCommands" value="${message(code:'status.page.status.replication.no_commands')}" />
      <g:set var="commandsRunning" value="${message(code:'status.page.status.replication.commands_running')}" />

      <g:javascript src="hash.js" />
      <script type="text/javascript">
            /** The current number of commands at the time the page loads. */
          var currentNumberCommands = ${totalCommandsRunning}
          var sizeShortRunning = ${shortRunningCommands.size()}
          var sizeLongRunning = ${longRunningCommands.size()}
          var no_commands = "${noCommands}"
          var commands_running = "${commandsRunning}"

          var idleString = "${message(code: 'job.page.list.row.job_idle')}"

          var passedResults = new Hash()
      </script>
      <g:javascript src="jobs-list.js" />
</head>

<body>

<content tag="title">
  <g:message code="job.page.header"/>
</content>

<content tag="leftMenu">
<BR>
   <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <b>${message(code:'status.page.replica.master_hostname')}</b> ${svnMasterUrl}
   </div>
   <div class="ImageListParent">
     <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
     ${message(code:'job.page.list.polling_interval', args:[commandPollRate])}
   </div>
   <div class="ImageListParent">
     <div style="float: left;"><img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
     </div> <div id="commandsCount" style="float: left;">
         <g:if test="${totalCommandsRunning == 0}">
            ${no_commands}
         </g:if>
         <g:if test="${totalCommandsRunning != 0}">
            ${commands_running} ${totalCommandsRunning}
         </g:if>
     </div>
   </div>
<BR>

<div class="ImageListParent">
   <table class="Container" id="scheduledCommandsTable">
     <tbody>
     <tr class="ContainerHeader">
       <td colspan="2">
         <g:message code="job.page.list.scheduled.header"/>
       </td>
     </tr>
  <g:each in="${scheduledCommands}" status="i" var="schCommand">
    <tr id="sch_${schCommand.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
     <td>
      &nbsp; <g:set var="commandCode" value="${AbstractCommand.makeCodeName(schCommand)}" />
      <img border="0" src="/csvn/images/replica/${commandCode}.png"> 
      ${schCommand.id}
      <g:if test="${schCommand.params.repoName}">
         (${schCommand.params.repoName})
      </g:if>
     </td>
    </tr>
  </g:each>
    </tbody>
   </table>
</div>

</content>

<g:render template="/job/runningCommands" model="['tableName': 'longRunningCommandsTable',
 'runningCommands': longRunningCommands, 'maxNumber': 5, 'shortRun': false]" />

<BR><BR>


<g:render template="/job/runningCommands" model="['tableName': 'shortRunningCommandsTable',
 'runningCommands': shortRunningCommands, 'maxNumber': 10, 'shortRun': true]" />

</body>
