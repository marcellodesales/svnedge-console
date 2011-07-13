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
    <td colspan="4">
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
    <td width="18">#</td>
    <td width="15%">${message(code: 'job.page.list.column.id')}</td>
    <td>${message(code: 'job.page.list.column.code')}</td>
    <td width="20%">${message(code: 'job.page.list.column.started_at')}</td>
    <td width="20%">${message(code: 'job.page.list.column.finished_at')}</td>
  </tr>
  </thead>
  <tbody id="${tableName}">
  <g:each in="${itemList}" var="jobCtx" status="i">

    <tr id="run_${jobCtx.jobDetail}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">

      <td>${i + 1}</td>
      <td>
        <g:if test="${jobCtx.jobRunTime > -1}">
          ${jobCtx.mergedJobDataMap.id}
        </g:if>
        <g:elseif test="${jobCtx.jobRunTime == -1}">
          <a target="${jobCtx.mergedJobDataMap.id}" href="${jobCtx.mergedJobDataMap.url}">${mergedJobDataMap.id}</a>
        </g:elseif>
      </td>
      <td>
        ${jobCtx.mergedJobDataMap.description}
      </td>
      <td>
        <g:formatDate format="${logDateFormat}" date="${jobCtx.fireTime}"/>
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
