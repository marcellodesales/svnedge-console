<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="job.page.list.title"/></title>

      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
                djconfig="parseOnLoad: false, isDebug: false"></script>

      <g:javascript src="json.js" />
      <g:javascript src="hash.js" />
      <script type="text/javascript">
          /** The current number of commands at the time the page loads. */
          var currentNumberCommands = ${totalCommandsRunning}
          /** The total number of permits of short-running commands. */
          var sizeShortRunning = ${shortRunningCommands.size()}
          /** The total number of permits of long-running commands (svn initializations). */
          var sizeLongRunning = ${longRunningCommands.size()}
          /** A row that shows as idle. */
          var idleString = "${message(code: 'job.page.list.row.job_idle')}"
          /** The polling interval string when the value is changed */
          var pollingChangeString = "${message(code:'job.page.list.polling_interval', args:["x"])}"

          var cmdStrings = new Hash()
          cmdStrings["copyRevprops"] = "${message(code: 'job.page.list.copyRevprops', args:["x"])}"
          cmdStrings["replicaPropsUpdate"] = "${message(code: 'job.page.list.replicaPropsUpdate', args:["x"])}"
          cmdStrings["replicaUnregister"] = "${message(code: 'job.page.list.replicaUnregister')}"
          cmdStrings["repoSync"] = "${message(code: 'job.page.list.repoSync', args:["x"])}"
          cmdStrings["repoRemove"] = "${message(code: 'job.page.list.repoRemove', args:["x"])}"
          cmdStrings["repoAdd"] = "${message(code: 'job.page.list.repoAdd', args:["x"])}"

          /** The hash of commands with passed results. [cmdexec1001 = true] */
          var passedResults = new Hash()
          /** The hash of parameters. [cmdexec1001 = true] */
          var paramsIndex = new Hash()
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
     <div style="float: left;"><img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
     </div> <div id="pollingIntervalString" style="float: left;">
        ${message(code:'job.page.list.polling_interval', args:[commandPollRate])}
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
     </td>
    </tr>
  </g:each>
    </tbody>
   </table>
</div>

</content>

<g:render template="/job/runningCommands" model="['tableName': 'longRunningCommandsTable',
 'runningCommands': longRunningCommands, 'maxNumber': maxLongRunning, 'shortRun': false]" />

<BR><BR>


<g:render template="/job/runningCommands" model="['tableName': 'shortRunningCommandsTable',
 'runningCommands': shortRunningCommands, 'maxNumber': maxShortRunning, 'shortRun': true]" />

</body>
