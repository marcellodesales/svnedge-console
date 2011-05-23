<%@page import="com.collabnet.svnedge.integration.command.ShortRunningCommand"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>

<table class="Container" id="${tableName}">
  <tbody>
    <tr class="ContainerHeader">
      <td colspan="4">
       <g:if test="${shortRun}">
        <g:message code="job.page.list.short_running.header"/>
       </g:if>
       <g:if test="${!shortRun}">
        <g:message code="job.page.list.long_running.header"/>
       </g:if>
      </td>
      <td>
        <g:set var="imageRunning" value="none" />
        <g:if test="${runningCommands.size() > 0}">
           <g:set var="imageRunning" value="" />
        </g:if>
        <img src="/csvn/images/replica/commands_updating_spinner.gif" 
             id="spinner_${shortRun ? "short" : "long"}" style="display:'${imageRunning}';">
      </td>
    </tr>
     <tr class="ItemListHeader">
       <td width="18">#</td>
       <td>${message(code: 'job.page.list.column.id')}</td>
       <td>${message(code: 'job.page.list.column.code')}</td>
       <td>${message(code: 'job.page.list.column.properties')}</td>
       <td>${message(code: 'job.page.list.column.started_at')}</td>
    </tr>
  </thead>
  <tbody>
   <g:each in="${(0..maxNumber-1)}" var="i">
    <g:if test="${i < runningCommands.size()}">
     <g:set var="command" value="${runningCommands.get(i)}" />
      <tr id="run_${command.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
       <td>${i+1}</td>
       <g:set var="commandCode" value="${AbstractCommand.makeCodeName(command)}" />
       <td width="15%"><a target="${command.id}" href="/csvn/log/show?fileName=/temp/${command.id}.log&view=tail">${command.id}</a></td>
       <td width="20%">
         <img border="0" src="/csvn/images/replica/${commandCode}.png"> 
         <g:if test="${!command.params.repoName}">
           ${commandCode}
         </g:if>
         <g:if test="${command.params.repoName}">
           ${command.params.repoName}
         </g:if>
       </td>
       <td>
         <g:if test="${shortRun && !command.params.repoName}">
          ${command.params}
         </g:if>
         <g:if test="${shortRun && command.params.repoName}">
          -
         </g:if>
         <g:if test="${!shortRun}">
          create + svn sync
         </g:if>
       </td>
       <td width="20%">
        <g:formatDate format="${logDateFormat}" 
             date="${new Date(command.stateTransitions.get(CommandState.RUNNING))}"/>
       </td>
      </tr> 
    </g:if>
    <g:if test="${i >= runningCommands.size()}">
      <tr class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
       <td>${i+1}</td>
       <td colspan="4" align="center"><b>${message(code: 'job.page.list.row.job_idle')}</b></td>
      </tr>
     </g:if>
     </tr>
   </g:each>
  </tbody>
 </table>