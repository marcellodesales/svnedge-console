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

<%@ page import="com.collabnet.svnedge.console.SchedulerBean" %>
<g:form class="form-horizontal" method="post" name="bkupForm">
  <input type="hidden" name="id" value="${repositoryInstance?.id}"/>

<div class="row-fluid">
  <div  class="span9" id="scheduler">
    <div class="control-group required-field">
      <label class="control-label"
          for="type"><g:message code="repository.page.bkupSchedule.type"/></label>
      <div class="controls">
              <g:set var="isCloud" value="${params.type == 'cloud' || dump.cloud}"/>
              <g:set var="isDumpDelta" value="${params.type == 'dump_delta' || dump.deltas}"/>
              <g:set var="isHotcopy" value="${params.type == 'hotcopy' || dump.hotcopy}"/>
              <g:set var="isNone" value="${params.type == 'none'}"/>
              <select id="type" name="type" class="scheduleElement">
                <g:if test="${cloudEnabled}">
                  <option value="cloud" <g:if test="${isCloud}">selected="selected"</g:if>><g:message
                          code="repository.page.bkupSchedule.type.cloud"/></option>
                </g:if>
                <option value="dump" <g:if
                        test="${!isCloud && !isDumpDelta && !isHotcopy && !isNone}">selected="selected"</g:if>><g:message
                        code="repository.page.bkupSchedule.type.fullDump"/></option>
                <option value="dump_delta" <g:if test="${isDumpDelta}">selected="selected"</g:if>><g:message
                        code="repository.page.bkupSchedule.type.fullDumpDelta"/></option>
                <option value="hotcopy" <g:if test="${isHotcopy}">selected="selected"</g:if>><g:message
                        code="repository.page.bkupSchedule.type.hotcopy"/></option>
                <option value="none" <g:if test="${isNone}">selected="selected"</g:if>><g:message
                        code="repository.page.bkupSchedule.type.none"/></option>
              </select>
              <g:if test="${cloudRegistrationRequired}">
                <span id="cloudRegister" class="help-inline" style="display: none;">
                  <img width="15" height="15" alt="Warning" align="bottom"
                       src="${resource(dir: 'images/icons', file: 'icon_warning_sml.gif')}" border="0"/>
                  <g:message code="repository.page.bkupSchedule.cloud.not.configured"
                             args="${[createLink(controller: 'setupCloudServices', action: 'index')]}"/>
                </span>
              </g:if>
      </div>
    </div>

    <div class="control-group required-field" id="whenRow">
      <label class="control-label"
          for="frequency"><g:message code="repository.page.bkupSchedule.period"/></label>
      <div class="controls">
                    <div style="display: inline-block;">
                      <label class="radio">
                        <g:radio id="frequency_h" name="schedule.frequency" value="HOURLY"
                                 checked="${dump.schedule.frequency == SchedulerBean.Frequency.HOURLY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.hourly"/>
                      </label>
                      <label class="radio">
                        <g:radio id="frequency_d" name="schedule.frequency" value="DAILY"
                                 checked="${dump.schedule.frequency == SchedulerBean.Frequency.DAILY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.daily"/>
                      </label>
                      <label class="radio">
                        <g:radio id="frequency_w" name="schedule.frequency" value="WEEKLY"
                                 checked="${dump.schedule.frequency != SchedulerBean.Frequency.HOURLY && dump.schedule.frequency != SchedulerBean.Frequency.DAILY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.weekly"/>
                      </label>
                    </div>
                    <table style="display: inline-block; margin-left: 20px; vertical-align: top;">
                      <tr>
                        <td>
                          <g:message code="repository.page.bkupSchedule.startTime"/>&nbsp;&nbsp;
                        </td>
                        <td>
                          <g:set var="hours"
                                 value="${(0..23).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                          <g:set var="minutes"
                                 value="${(0..59).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                          <span id="time">
                            <g:select id="startHour" name="schedule.hour" from="${hours}"
                                      value="${formatNumber(number: dump.schedule.hour, minIntegerDigits: 2)}"
                                      class="scheduleElement autoWidth"/>&nbsp;:&nbsp;<g:select id="startMinute"
                                                                                      name="schedule.minute"
                                                                                      from="${minutes}"
                                                                                      value="${formatNumber(number: dump.schedule.minute, minIntegerDigits: 2)}"
                                                                                      class="scheduleElement autoWidth"/>&nbsp;:&nbsp;00
                          </span>
                        </td>
                      </tr>
                      <tr id="dayOfWeekRow">
                        <g:set var="daysOfWeek"
                               value="${[message(code: 'default.dayOfWeek.sunday'), message(code: 'default.dayOfWeek.monday'), message(code: 'default.dayOfWeek.tuesday'), message(code: 'default.dayOfWeek.wednesday'), message(code: 'default.dayOfWeek.thursday'), message(code: 'default.dayOfWeek.friday'), message(code: 'default.dayOfWeek.saturday')]}"/>
                        <td><g:message code="repository.page.bkupSchedule.dayOfWeek"/></td>
                        <td>
                          <select id="dayOfWeek" name="schedule.dayOfWeek" class="scheduleElement autoWidth">
                            <g:each status="i" var="day" in="${daysOfWeek}">
                              <option value="${i + 1}" <g:if
                                      test="${dump.schedule.dayOfWeek == i + 1}">selected="selected"</g:if>>${day}</option>
                            </g:each>
                          </select>
                        </td>
                      </tr>
                    </table>
      </div>
    </div>
              
    <div class="control-group required-field" id="keepRow">
      <label class="control-label"
          for="keep"><g:message code="repository.page.bkupSchedule.numberToKeep"/></label>
      <div class="controls">
        <input type="text" name="numberToKeep" id="numberToKeep" value="${dump.numberToKeep}"
             class="scheduleFormElement input-mini"/>
        <div class="help-block"><g:message code="repository.page.bkupSchedule.numberToKeep.all"/></div>
      </div>
    </div>

    <g:if test="${repositoryInstance && (flash['nameAdjustmentRequired' + repositoryInstance.id] || repositoryInstance.cloudName && repositoryInstance.cloudName != repositoryInstance.name)}">
    <div class="control-group required-field" id="cloudNameRow">
      <label class="control-label"
          for="${'cloudName' + repositoryInstance.id}"><g:message code="repository.page.bkupSchedule.cloudName"/></label>
      <div class="controls">
                <g:if test="${flash['nameAdjustmentRequired' + repositoryInstance.id]}">
                  <input type="text" name="${'cloudName' + repositoryInstance.id}"
                         value="${params['cloudName' + repositoryInstance.id] ?: repositoryInstance.cloudName ?: repositoryInstance.name}"/>
                </g:if>
                <g:elseif
                        test="${repositoryInstance.cloudName && repositoryInstance.cloudName != repositoryInstance.name}">${repositoryInstance.cloudName}</g:elseif>
      </div>
    </div>
    </g:if>
    
    <g:if test="${repositoryInstance}">
      <div class="form-actions">
        <g:actionSubmit action="updateBkupSchedule" value="${message(code:'default.button.save.label')}"
                            class="btn btn-primary"/>
      </div>
    </g:if>
  </div>
            <g:if test="${cloudEnabled}">
              <div class="span3" style="vertical-align: top; text-align: center" id="cloudInfo">
                <a target="_blank" href="${helpBaseUrl}/index.jsp?topic=/csvn/action/movetocncloud.html"><img 
                        width="200" height="75" alt="" src="${resource(dir:'images/cloud',file:'cloudBackup.png')}" border="0"/><br/>
                <g:message code="repository.page.bkupSchedule.help.link.label"/></a>
              </div>
            </g:if>
</div>

          
        <g:if test="${!repositoryInstance}">
          <br/>
          <table id="reposTable" class="table table-striped table-bordered table-condensed tablesorter">
          <thead>
            <tr>
              <th><g:listViewSelectAll/></th>
              <g:sortableColumn property="repoName" titleKey="repository.page.bkupSchedule.job.repoName"
                                defaultOrder="asc"/>
              <g:sortableColumn property="type" titleKey="repository.page.bkupSchedule.job.type"/>
              <g:sortableColumn property="scheduledFor" titleKey="repository.page.bkupSchedule.job.scheduledFor"/>
              <g:sortableColumn property="keepNumber" titleKey="repository.page.bkupSchedule.job.keepNumber"/>
            </tr>
          </thead>
          <tbody>
            <g:each in="${repoBackupJobList}" status="i" var="job">
              <tr>
                <td>
                  <g:listViewSelectItem item="${job}" property="repoId"
                                        onClick="setFormState({'type': '${job.typeCode}', 'numberToKeep': '${job.keepNumber}', 'scheduleFrequency': '${job.schedule?.frequency}', 'scheduleHour': '${job.schedule?.hour}', 'scheduleMinute': '${job.schedule?.minute}', 'scheduleDayOfWeek': '${job.schedule?.dayOfWeek}'})"/>
                </td>

                <td>${job.repoName}
                  <g:if test="${flash['nameAdjustmentRequired' + job.repoId]}">
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <label><g:message code="repository.page.bkupSchedule.cloudName"/>
                      <input type="text" name="${'cloudName' + job.repoId}"
                             value="${params['cloudName' + job.repoId] ?: job.cloudName ?: job.repoName}" size="30"/>
                    </label>
                  </g:if>
                  <g:elseif test="${job.cloudName && job.cloudName != job.repoName}">(${job.cloudName})</g:elseif>
                </td>
                <td>${job.type}
                  <g:if test="${cloudRegistrationRequired && job.typeCode == 'cloud'}">
                    <span class="TextRequired">
                      <g:message code="repository.page.bkupSchedule.cloud.activation.required"
                                 args="${[createLink(controller: 'setupCloudServices', action: 'index')]}"/>
                    </span>
                  </g:if>
                </td>
                <td>${job.scheduleFormatted}</td>
                <td>${job.keepNumber == 0 ? "ALL" : job.keepNumber}</td>
              </tr>
            </g:each>
            <g:if test="${!repoBackupJobList}">
              <tr>
                <td><p><g:message code="repository.page.list.noRepos"/></p></td>
              </tr>
            </g:if>
            </tbody>
          </table>

          <div class="pull-right">
            <g:listViewActionButton action="updateBkupSchedule" minSelected="1" primary="true">
              <g:message code="repository.page.bkupSchedule.job.setSchedule"/>
            </g:listViewActionButton>
          </div>
        </g:if>

  
</g:form>
<g:javascript>
  function typeHandler() {
    var typeSelect = $('#type');
    var typeSelectValue = typeSelect.val();
    if (typeSelectValue == 'dump' || typeSelectValue == 'dump_delta' ||
        typeSelectValue == 'hotcopy') {
      $('#whenRow').show();
      $('#keepRow').show();
      $('#cloudRegister').hide();
      $('#cloudNameRow').hide;
    } else if (typeSelectValue == 'cloud') {
      $('#whenRow').show();
      $('#keepRow').hide();
      $('#cloudRegister').show();
      $('#cloudNameRow').show();
    } else {
      $('#whenRow').hide();
      $('#keepRow').hide();
      $('#cloudRegister').hide();
      $('#cloudNameRow').hide();
    }
  }
  typeHandler();
  $('#type').change(typeHandler);

  var hourSelect = $('#startHour');
  var hourOptions = [];
  var hourSelectOptions = $('#startHour option');
  for (var i = 0; i < hourSelectOptions.length; i++) {
    hourOptions[i] = hourSelectOptions[i];
  }
  function displayTimeWidget() {
    if ($('#frequency_h').attr('checked')) {
      hourSelect.empty();
      //hourSelect.append('<option value="' + hourOptions[0] + '"/>')
      hourSelect.append(hourOptions[0]);
      hourSelect.attr('disabled', true);
    } else {
      var hours = $('#startHour option');
      if (hours.length == 1) {
        hours.attr('selected', true);
        hourSelect.empty()
        for (var i = 1; i < hourOptions.length; i++) {
          hourSelect.append(hourOptions[i]);
        }
        hourSelect.prepend(hourOptions[0]);
      }
      hourSelect.attr('disabled', false);
    }
  }
  function displayDayOfWeekWidget() {
    if ($('#frequency_w').attr('checked')) {
      $('#dayOfWeekRow').show();
    } else {
      $('#dayOfWeekRow').hide();
    }
  }
  function frequencyHandler() {
    displayTimeWidget();
    displayDayOfWeekWidget();
  }
  frequencyHandler();
  $('#frequency_h').change(frequencyHandler);
  $('#frequency_d').change(frequencyHandler);
  $('#frequency_w').change(frequencyHandler);

  var allowScheduleFormStateClobber = true;
  function setFormState(state) {

    // only change form if we are allowing changes and values are provided
    if (!allowScheduleFormStateClobber || state.type == 'null') {
      return;
    }

    // after this, disallow picking up changes from the existing jobs
    allowScheduleFormStateClobber = false

    $("#type").val(state.type);

    var hour = state.scheduleHour.length == 1 ? '0' + state.scheduleHour : '' + state.scheduleHour;
    $('#startHour').val(hour);

    var minute = state.scheduleMinute.length == 1 ? '0' + state.scheduleMinute : '' + state.scheduleMinute;
    $('#startMinute').val(minute);

    $('#dayOfWeek').val(state.scheduleDayOfWeek);
    $('#numberToKeep').val(state.numberToKeep);
    if (state.scheduleFrequency == "HOURLY") {
      $('#frequency_h').attr('checked', true);
    }
    else if (state.scheduleFrequency == "DAILY") {
      $('#frequency_d').attr('checked', true);
    }
    else if (state.scheduleFrequency == "WEEKLY") {
        $('#frequency_w').attr('checked', true);
      }
    typeHandler()
    frequencyHandler()
  }

  // add observer to disable state changes after the form is manipulated
  $(document).ready(function() {
    $(".scheduleElement").click(function() {
        allowScheduleFormStateClobber = false;
    });
  });
</g:javascript>
