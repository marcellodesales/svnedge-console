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
<g:form method="post" name="bkupForm">
  <input type="hidden" name="id" value="${repositoryInstance?.id}"/>
  <table class="ItemDetailContainer">
    <tr>
      <td class="ContainerBodyWithPaddedBorder">
        <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="type"><g:message code="repository.page.bkupSchedule.type"/></label>
            </td>
            <td valign="top" class="value ItemDetailValue">
              <g:set var="isCloud" value="${params.type == 'cloud' || dump.cloud}"/>
              <g:set var="isDumpDelta" value="${params.type == 'dump_delta' || dump.deltas}"/>
              <g:set var="isHotcopy" value="${params.type == 'hotcopy' || dump.hotcopy}"/>
              <g:set var="isNone" value="${params.type == 'none'}"/>
              <select id="type" name="type" class="scheduleElement">
                <option value="cloud" <g:if test="${isCloud}">selected="selected"</g:if>><g:message
                        code="repository.page.bkupSchedule.type.cloud"/></option>
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
                <span id="cloudRegister" class="TextRequired" style="display: none;">
                  <img width="15" height="15" alt="Warning" align="bottom"
                       src="${resource(dir: 'images/icons', file: 'icon_warning_sml.gif')}" border="0"/>
                  <g:message code="repository.page.bkupSchedule.cloud.not.configured"
                             args="${[createLink(controller: 'setupCloudServices', action: 'index')]}"/>
                </span>
              </g:if>
            </td>
          </tr>

          <tr id="whenRow">
            <td class="ItemDetailName">
              <label for="frequency"><g:message code="repository.page.bkupSchedule.period"/></label>
            </td>
            <td valign="top" class="value ItemDetailValue">
              <table>
                <tr>
                  <td>
                    <div>
                      <label>
                        <g:radio id="frequency_h" name="schedule.frequency" value="HOURLY"
                                 checked="${dump.schedule.frequency == SchedulerBean.Frequency.HOURLY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.hourly"/>
                      </label>
                    </div>

                    <div>
                      <label>
                        <g:radio id="frequency_d" name="schedule.frequency" value="DAILY"
                                 checked="${dump.schedule.frequency == SchedulerBean.Frequency.DAILY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.daily"/>
                      </label>
                    </div>

                    <div>
                      <label>
                        <g:radio id="frequency_w" name="schedule.frequency" value="WEEKLY"
                                 checked="${dump.schedule.frequency != SchedulerBean.Frequency.HOURLY && dump.schedule.frequency != SchedulerBean.Frequency.DAILY}"
                                 class="scheduleElement"/>
                        <g:message code="repository.page.bkupSchedule.period.weekly"/>
                      </label>
                    </div>
                  </td>
                  <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                  <td>

                    <table class="ItemDetailContainer">
                      <tr>
                        <td class="ItemDetailName">
                          <g:message code="repository.page.bkupSchedule.startTime"/>&nbsp;&nbsp;
                        </td>
                        <td valign="top" class="value ItemDetailValue">
                          <g:set var="hours"
                                 value="${(0..23).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                          <g:set var="minutes"
                                 value="${(0..59).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                          <span id="time">
                            <g:select id="startHour" name="schedule.hour" from="${hours}"
                                      value="${formatNumber(number: dump.schedule.hour, minIntegerDigits: 2)}"
                                      class="scheduleElement"/>&nbsp;:&nbsp;<g:select id="startMinute"
                                                                                      name="schedule.minute"
                                                                                      from="${minutes}"
                                                                                      value="${formatNumber(number: dump.schedule.minute, minIntegerDigits: 2)}"
                                                                                      class="scheduleElement"/>&nbsp;:&nbsp;00
                          </span>
                        </td>
                      </tr>
                      <tr id="dayOfWeekRow">
                        <g:set var="daysOfWeek"
                               value="${[message(code: 'default.dayOfWeek.sunday'), message(code: 'default.dayOfWeek.monday'), message(code: 'default.dayOfWeek.tuesday'), message(code: 'default.dayOfWeek.wednesday'), message(code: 'default.dayOfWeek.thursday'), message(code: 'default.dayOfWeek.friday'), message(code: 'default.dayOfWeek.saturday')]}"/>
                        <td class="ItemDetailName"><g:message code="repository.page.bkupSchedule.dayOfWeek"/></td>
                        <td valign="top" class="value ItemDetailValue">
                          <select id="dayOfWeek" name="schedule.dayOfWeek" class="scheduleElement">
                            <g:each status="i" var="day" in="${daysOfWeek}">
                              <option value="${i + 1}" <g:if
                                      test="${dump.schedule.dayOfWeek == i + 1}">selected="selected"</g:if>>${day}</option>
                            </g:each>
                          </select>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <tr id="keepRow">
            <td class="ItemDetailName">
              <label for="keep"><g:message code="repository.page.bkupSchedule.numberToKeep"/></label>
            </td>
            <td valign="top" class="value ItemDetailValue">
              <input type="text" size="3" name="numberToKeep" id="numberToKeep" value="${dump.numberToKeep}"
                     class="scheduleFormElement"/>
              &nbsp;&nbsp;<g:message code="repository.page.bkupSchedule.numberToKeep.all"/>
            </td>
          </tr>
          <g:if test="${repositoryInstance && (flash['nameAdjustmentRequired' + repositoryInstance.id] || repositoryInstance.cloudName && repositoryInstance.cloudName != repositoryInstance.name)}">
            <tr id="cloudNameRow">
              <td class="ItemDetailName">
                <label for="${'cloudName' + repositoryInstance.id}"><g:message
                        code="repository.page.bkupSchedule.cloudName"/></label>
              </td>
              <td valign="top" class="value ItemDetailValue">
                <g:if test="${flash['nameAdjustmentRequired' + repositoryInstance.id]}">
                  <input type="text" name="${'cloudName' + repositoryInstance.id}"
                         value="${params['cloudName' + repositoryInstance.id] ?: repositoryInstance.cloudName ?: repositoryInstance.name}"
                         size="30"/>
                </g:if>
                <g:elseif
                        test="${repositoryInstance.cloudName && repositoryInstance.cloudName != repositoryInstance.name}">${repositoryInstance.cloudName}</g:elseif>
              </td>
            </tr>
          </g:if>

        </table>
        <g:if test="${!repositoryInstance}">
          <br/>
          <table class="Container">
            <tbody>
            <tr class="ItemListHeader">
              <th><g:listViewSelectAll/></th>
              <g:sortableColumn property="repoName" titleKey="repository.page.bkupSchedule.job.repoName"
                                defaultOrder="asc"/>
              <g:sortableColumn property="type" titleKey="repository.page.bkupSchedule.job.type"/>
              <g:sortableColumn property="scheduledFor" titleKey="repository.page.bkupSchedule.job.scheduledFor"/>
              <g:sortableColumn property="keepNumber" titleKey="repository.page.bkupSchedule.job.keepNumber"/>
            </tr>
            <g:each in="${repoBackupJobList}" status="i" var="job">
              <tr class="${(i % 2) == 0 ? 'EvenRow' : 'OddRow'}">
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
                <td colspan="5"><p><g:message code="repository.page.list.noRepos"/></p></td>
              </tr>
            </g:if>
          </table>
        </g:if>
      </td>
    </tr>
    <tr class="ContainerFooter">
      <td>
        <div class="AlignRight">
          <g:if test="${!repositoryInstance}">
            <g:listViewActionButton action="updateBkupSchedule" minSelected="1">
              <g:message code="repository.page.bkupSchedule.job.setSchedule"/>
            </g:listViewActionButton>
          </g:if>
          <g:else>
            <g:actionSubmit action="updateBkupSchedule" value="${message(code:'default.button.save.label')}"
                            class="Button"/>
          </g:else>
        </div>
      </td>
    </tr>
  </table>
</g:form>
<g:javascript>
  function typeHandler() {
    var typeSelect = $('type');
    if (typeSelect.value == 'dump' || typeSelect.value == 'dump_delta' ||
        typeSelect.value == 'hotcopy') {
      $('whenRow').style.display = '';
      $('keepRow').style.display = '';
      if ($('cloudRegister')) {
        $('cloudRegister').style.display = 'none';
      }
      if ($('cloudNameRow')) {
        $('cloudNameRow').style.display = 'none';
      }
    } else if (typeSelect.value == 'cloud') {
      $('whenRow').style.display = '';
      $('keepRow').style.display = 'none';
      if ($('cloudRegister')) {
        $('cloudRegister').style.display = '';
      }
      if ($('cloudNameRow')) {
        $('cloudNameRow').style.display = '';
      }
    } else {
      $('whenRow').style.display = 'none';
      $('keepRow').style.display = 'none';
      if ($('cloudRegister')) {
        $('cloudRegister').style.display = 'none';
      }
      if ($('cloudNameRow')) {
        $('cloudNameRow').style.display = 'none';
      }
    }
  }
  typeHandler();
  $('type').onchange = typeHandler;

  var hourSelect = $('startHour');
  var hourOptions = [];
  for (var i = 0; i < hourSelect.options.length; i++) {
    hourOptions[i] = hourSelect.options[i];
  }
  function displayTimeWidget() {
    if ($('frequency_h').checked) {
      for (var i = hourSelect.options.length - 1; i > 0; i--) {
        hourSelect.remove(i);
      }
      hourSelect.disabled = true;
    } else {
      if (hourSelect.options.length == 1) {
        for (var i = 1; i < hourOptions.length; i++) {
          hourSelect.add(hourOptions[i], null);
        }
      }
      hourSelect.disabled = false;
    }
  }
  function displayDayOfWeekWidget() {
    if ($('frequency_w').checked) {
      $('dayOfWeekRow').style.display = '';
    } else {
      $('dayOfWeekRow').style.display = 'none';
    }
  }
  function frequencyHandler() {
    displayTimeWidget();
    displayDayOfWeekWidget();
  }
  frequencyHandler();
  $('frequency_h').onchange = frequencyHandler;
  $('frequency_d').onchange = frequencyHandler;
  $('frequency_w').onchange = frequencyHandler;

  var allowScheduleFormStateClobber = true;
  function setFormState(state) {

    // only change form if we are allowing changes and values are provided
    if (!allowScheduleFormStateClobber || state.type == 'null') {
      return;
    }

    // after this, disallow picking up changes from the existing jobs
    allowScheduleFormStateClobber = false

    $("type").value = state.type

    var hour = state.scheduleHour.length == 1 ? '0' + state.scheduleHour : '' + state.scheduleHour;
    $('startHour').value = hour;

    var minute = state.scheduleMinute.length == 1 ? '0' + state.scheduleMinute : '' + state.scheduleMinute;
    $('startMinute').value = minute;

    $('dayOfWeek').value = state.scheduleDayOfWeek
    $('numberToKeep').value = state.numberToKeep
    if (state.scheduleFrequency == "HOURLY") {
      $('frequency_h').checked = "true"
    }
    else if (state.scheduleFrequency == "DAILY") {
      $('frequency_d').checked = "true"
    }
    else if (state.scheduleFrequency == "WEEKLY") {
        $('frequency_w').checked = "true"
      }
    typeHandler()
    frequencyHandler()
  }

  // add observer to disable state changes after the form is manipulated
  Event.observe(window, 'load', function() {
    var allScheduleFormElements = $$("input.scheduleElement").concat($$("select.scheduleElement"));
    allScheduleFormElements.each(function(item) {
      Event.observe(item, 'click', function() {
        allowScheduleFormStateClobber = false
      });
    })
  })
</g:javascript>
