%{--
  - CollabNet Subversion Edge
  - Copyright (C) 2011, CollabNet Inc. All rights reserved.
  -
  - This program is free software: you can redistribute it and/or modify
  - it under the terms of the GNU Affero General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU Affero General Public License for more details.
  -
  - You should have received a copy of the GNU Affero General Public License
  - along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%

<%@ page import="org.springframework.scheduling.quartz.QuartzJobBean" %>

<table class="Container">
  <thead>
  <tr class="ContainerHeader">
    <td colspan="5">
      ${heading}
    </td>
    <td>
      <g:set var="imageRunning" value="none"/>
      <g:if test="${itemList?.size() > 0}">
        <g:set var="imageRunning" value=""/>
      </g:if>
    </td>
  </tr>
  <tr class="ItemListHeader">
    <td width="5%">#</td>
    <td width="15%">${message(code: 'job.page.list.column.id')}</td>
    <td width="50%">${message(code: 'job.page.list.column.description')}</td>
    <td width="10%">${message(code: 'job.page.list.column.scheduled')}</td>
    <td width="10%">${message(code: 'job.page.list.column.started_at')}</td>
    <td width="10%">${message(code: 'job.page.list.column.finished_at')}</td>
  </tr>
  </thead>
  <tbody id="${tableName}">
  <g:each in="${itemList}" var="jobCtx" status="i">

    <tr id="run_${jobCtx.jobDetail}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">

      <td>${i + 1}</td>
      <td>
        ${jobCtx.mergedJobDataMap.id}
      </td>
      <td>
        ${jobCtx.mergedJobDataMap.description}

        <g:if test="${view == 'scheduled' && jobCtx.mergedJobDataMap.urlConfigure}">
          <a href="${jobCtx.mergedJobDataMap.urlConfigure}">${message(code: 'job.page.list.jobConfigure')}</a>
        </g:if>
        <g:elseif test="${view == 'running' && jobCtx.mergedJobDataMap.urlProgress}">
          <a target="${jobCtx.mergedJobDataMap.id}" href="${jobCtx.mergedJobDataMap.urlProgress}">${message(code: 'job.page.list.jobProgress')}</a>
        </g:elseif>
        <g:elseif test="${view == 'finished' && jobCtx.mergedJobDataMap.urlResult}">
          <a href="${jobCtx.mergedJobDataMap.urlResult}">${message(code: 'job.page.list.jobResult')}</a>
        </g:elseif>

      </td>
      <td>
        <g:set var="scheduledTime" value="${view == 'scheduled' ? jobCtx.nextFireTime : jobCtx.scheduledFireTime}"/>
        <g:if test="${scheduledTime}">
          <g:formatDate format="${logDateFormat}" date="${scheduledTime}"/>
        </g:if>
        <g:else>
            -
        </g:else>
      </td>
      <td>
        <g:if test="${jobCtx.fireTime}">
          <g:formatDate format="${logDateFormat}" date="${jobCtx.fireTime}"/>
        </g:if>
        <g:else>
          -
        </g:else>
      </td>
      <td>
        <g:if test="${jobCtx.jobRunTime > -1}">
          <g:formatDate format="${logDateFormat}" date="${new Date(jobCtx.fireTime.time + jobCtx.jobRunTime)}"/>
        </g:if>
        <g:else>
          -
        </g:else>
      </td>
    </tr>
  </g:each>
  <g:if test="${!itemList}">
    <tr class="EvenRow}">
      <td>1</td>
      <td colspan="3" align="center"><b>${message(code: 'job.page.list.row.job_idle')}</b></td>
    </tr>
  </g:if>
  </tbody>
</table>
