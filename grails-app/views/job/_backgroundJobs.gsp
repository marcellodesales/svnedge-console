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



<table class="Container">
  <thead>
    <tr class="ContainerHeader">
      <td colspan="3">
        ${heading}
      </td>
      <td>
        <g:set var="imageRunning" value="none" />
        <g:if test="${itemList?.size() > 0}">
           <g:set var="imageRunning" value="" />
        </g:if>
      </td>
    </tr>
     <tr class="ItemListHeader">
       <td width="18">#</td>
       <td width="15%">${message(code: 'job.page.list.column.id')}</td>
       <td>${message(code: 'job.page.list.column.code')}</td>
       <td width="20%">${message(code: 'job.page.list.column.started_at')}</td>
    </tr>
  </thead>
  <tbody id="${tableName}">
    <g:each in="${itemList}" var="job" status="i">

    <g:if test="${job.started && !job.finished}">
      <tr id="run_${job.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
    </g:if>
    <g:elseif test="${job.finished}">
      <tr id="run_${job.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}" style="background-color : #FFB2B2;">
    </g:elseif>

       <td>${i+1}</td>
       <td>
         <g:if test="${!job.started || job.finished}">
         ${job.id}
         </g:if>
         <g:elseif test="${job.started && !job.finished}">
            <a target="${job.id}" href="${job.url}">${job.id}</a>
         </g:elseif>
       </td>

       <td>
         ${job.description}
       </td>
       <td>
        <g:formatDate format="${logDateFormat}"
             date="${job.started}"/>
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
