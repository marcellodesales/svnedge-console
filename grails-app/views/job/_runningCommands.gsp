<%@page import="com.collabnet.svnedge.integration.command.ShortRunningCommand"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>

<table class="Container">
  <thead>
    <g:if test="${shortRun}">
      <g:set var="numOfColumns" value="4" />
    </g:if>
    <g:else>
      <g:set var="numOfColumns" value="3" />
    </g:else>
    <tr class="ContainerHeader">
      <td colspan="${numOfColumns}">
       <g:if test="${shortRun}">
        <g:message code="job.page.list.short_running.header"/>
       </g:if>
       <g:else>
        <g:message code="job.page.list.long_running.header"/>
       </g:else>
      </td>
      <td>
        <g:set var="imageRunning" value="none" />
        <g:if test="${runningCommands.size() > 0}">
           <g:set var="imageRunning" value="" />
        </g:if>
      </td>
    </tr>
     <tr class="ItemListHeader">
       <td width="18">#</td>
       <td width="15%">${message(code: 'job.page.list.column.id')}</td>
  <g:if test="${shortRun}">
       <td width="20%">${message(code: 'job.page.list.column.code')}</td>
  </g:if>
  <g:else>
       <td>${message(code: 'job.page.list.column.code')}</td>
  </g:else>
  <g:if test="${shortRun}">
       <td>${message(code: 'job.page.list.column.properties')}</td>
  </g:if>
       <td width="20%">${message(code: 'job.page.list.column.started_at')}</td>
    </tr>
  </thead>
  <tbody id="${tableName}">
   <g:each in="${(0..maxNumber-1)}" var="i">
    <g:if test="${i < runningCommands.size()}">
     <g:set var="command" value="${runningCommands.get(i)}" />

    <g:if test="${command.state == CommandState.RUNNING}">
      <tr id="run_${command.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
    </g:if>
    <g:elseif test="${(command.state == CommandState.TERMINATED || command.state == CommandState.REPORTED) && command.succeeded}">
      <tr id="run_${command.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}" style="background-color : #99D6AD;">
    </g:elseif>
    <g:elseif test="${(command.state == CommandState.TERMINATED || command.state == CommandState.REPORTED) && !command.succeeded}">
      <tr id="run_${command.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}" style="background-color : #FFB2B2;">
    </g:elseif>

       <td>${i+1}</td>
       <g:set var="commandCode" value="${AbstractCommand.makeCodeName(command)}" />
       <g:set var="commandDesc" value="job.page.list.${commandCode}" />
         <g:if test="${command?.params?.repoName}">
           <g:set var="repoName" value="${command.params.repoName.substring(command.params.repoName.lastIndexOf("/") + 1)}" />
         </g:if>
       <td>
         <g:if test="${commandCode.contains('replica') || command.state == CommandState.REPORTED}">
            ${command.id}
         </g:if>
         <g:elseif test="${command.state == CommandState.RUNNING || command.state == CommandState.TERMINATED}">
            <a target="${command.id}" href="/csvn/log/show?fileName=/temp/${command.id}.log&view=tail">${command.id}</a>
         </g:elseif>
       </td>

       <td>
         <img border="0" src="/csvn/images/replica/${commandCode}.png"> 
         <g:if test="${command?.params?.repoName}">
           ${message(code: commandDesc, args:[command.params.repoName.substring(command.params.repoName.lastIndexOf("/") + 1, command.params.repoName.length())])}
         </g:if>
         <g:else>
           ${message(code: commandDesc)}
         </g:else>
       </td>
  <g:if test="${shortRun}">
       <td>
         <g:if test="${!command?.params?.repoName}">
           ${command.params}
         </g:if>
       </td>
  </g:if>
       <td>
        <g:formatDate format="${logDateFormat}"
             date="${new Date(command.getCurrentStateTransitionTime())}"/>
       </td>
      </tr> 
    </g:if>
    <g:else>
      <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
       <td>${i+1}</td>
       <td colspan="${numOfColumns}" align="center"><b>${message(code: 'job.page.list.row.job_idle')}</b></td>
      </tr>
    </g:else>
     </tr>
   </g:each>
  </tbody>
 </table>