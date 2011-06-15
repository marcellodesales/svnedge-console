<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<head>
    <meta name="layout" content="main" />
    <meta http-equiv="refresh" content="5">
    <title><g:message code="job.page.list.title"/></title>
</head>

<body>

<content tag="title">
  <g:message code="job.page.header"/> - ${replicaName}
</content>

<content tag="leftMenu">
<BR>
   <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <b>${message(code:'status.page.replica.master_hostname')}</b> ${svnMasterUrl}
   </div>
   <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <b>${message(code:'status.page.url.teamforge')}</b> ${ctfUrl}
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
      ${schCommand.id} ${schCommand.params.repoName ? "(" + schCommand.params.repoName.substring(schCommand.params.repoName.lastIndexOf("/") + 1, schCommand.params.repoName.length()) + ")" : ""}
     </td>
    </tr>
  </g:each>
    </tbody>
   </table>
<g:if test="${unprocessedCommands}">
<br />
   <table class="Container" id="blockedCommandsTable">
     <tbody>
     <tr class="ContainerHeader">
       <td colspan="2">
         <g:message code="job.page.list.blocked.header"/>
       </td>
     </tr>
  <g:set var="rowClass" value="OddRow"/>
  <g:each in="${unprocessedCommands}" status="i" var="cmd">
    <tr id="sch_${cmd.id}" class="${rowClass}}">
     <td>
      &nbsp; <g:set var="commandCode" value="${AbstractCommand.makeCodeName(cmd)}" />
      <img border="0" src="/csvn/images/replica/${commandCode}.png" alt"" />
      ${cmd.id} ${cmd.params.repoName ? "(" + cmd.params.repoName.substring(cmd.params.repoName.lastIndexOf("/") + 1, cmd.params.repoName.length()) + ")" : ""}
     </td>
    </tr>
    <g:set var="rowClass" value="${rowClass ==  'OddRow' ? 'EvenRow' : 'OddRow'}"/>
  </g:each>
    </tbody>
   </table>
   <small><g:message code="job.page.list.blocked.note"/></small>
</g:if>
</div>

</content>

<g:render template="/job/runningCommands" model="['tableName': 'longRunningCommandsTable',
 'runningCommands': longRunningCommands, 'maxNumber': maxLongRunning, 'shortRun': false]" />

<BR><BR>


<g:render template="/job/runningCommands" model="['tableName': 'shortRunningCommandsTable',
 'runningCommands': shortRunningCommands, 'maxNumber': maxShortRunning, 'shortRun': true]" />

</body>
