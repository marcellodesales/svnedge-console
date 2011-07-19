<%@page import="com.collabnet.svnedge.domain.ServerMode; java.util.concurrent.TimeUnit"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<head>
    <meta name="layout" content="main" />
    <meta http-equiv="refresh" content="5">
    <title><g:message code="job.page.header"/></title>
</head>


<content tag="title">
  <g:message code="job.page.header"/>
</content>

<content tag="leftMenu">
  <div class="<g:if test="${actionName == 'list' || actionName == 'listReplica'}">ImageListParentSelectedNoTop</g:if><g:else>ImageListParent</g:else>">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <g:link controller="job" action="index"><g:message code="job.page.leftNav.list" /></g:link>
  </div>
</content>

<body>

<g:render template="/job/backgroundJobs" model="['view': 'running','tableName': 'backgroundJobsRunningTable',
 'heading': message(code:'job.page.list.backgroundActive.header'), 'itemList': backgroundJobsRunning, 'maxNumber': 1]" />

<BR><BR>

<g:render template="/job/backgroundJobs" model="['view': 'finished','tableName': 'backgroundJobsFinishedTable',
 'heading': message(code:'job.page.list.backgroundFinished.header'),'itemList': backgroundJobsFinished, 'maxNumber': 1]" />

<BR><BR>

<g:render template="/job/backgroundJobs" model="['view': 'scheduled', 'tableName': 'backgroundJobsScheduledTable',
 'heading': message(code:'job.page.list.backgroundScheduled.header'), 'itemList': backgroundJobsScheduled,
 'maxNumber': 1]" />

</content>
</body>
