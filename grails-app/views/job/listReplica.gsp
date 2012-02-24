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

<g:render template="/server/leftNav"/>

<div class="well">
<br/>
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
<br/>
<br/>

<div>
  <table class="table table-striped table-bordered table-condensed" id="scheduledCommandsTable">
    <thead>
     <tr>
       <th>
         <g:message code="job.page.list.scheduled.header"/>
       </th>
     </tr>
    </thead>
    <tbody>
  <g:each in="${scheduledCommands}" status="i" var="schCommand">
    <tr id="sch_${schCommand.id}">
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
   <table class="table table-striped table-bordered table-condensed" id="blockedCommandsTable">
     <thead>
     <tr>
       <th>
         <g:message code="job.page.list.blocked.header"/>
       </th>
     </tr>
     </thead>
     <tbody>
  <g:each in="${unprocessedCommands}" status="i" var="cmd">
    <tr id="sch_${cmd.id}">
     <td>
      &nbsp; <g:set var="commandCode" value="${AbstractCommand.makeCodeName(cmd)}" />
      <img border="0" src="/csvn/images/replica/${commandCode}.png" alt="" />
      ${cmd.id} ${cmd.params.repoName ? "(" + cmd.params.repoName.substring(cmd.params.repoName.lastIndexOf("/") + 1, cmd.params.repoName.length()) + ")" : ""}
     </td>
    </tr>
  </g:each>
    </tbody>
   </table>
   <small><g:message code="job.page.list.blocked.note"/></small>
</g:if>
</div>

</div>

<g:render template="/job/replicaCommands" model="['tableName': 'longRunningCommandsTable',
 'runningCommands': longRunningCommands, 'maxNumber': maxLongRunning, 'shortRun': false]" />

<g:render template="/job/replicaCommands" model="['tableName': 'shortRunningCommandsTable',
 'runningCommands': shortRunningCommands, 'maxNumber': maxShortRunning, 'shortRun': true]" />

</body>
