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

<%@page import="com.collabnet.svnedge.console.SchedulerBean"%>
<g:form method="post" name="bkupForm">
  <input type="hidden" name="id" value="${repositoryInstance?.id}" />
   <table class="ItemDetailContainer">
    <tr>
     <td class="ContainerBodyWithPaddedBorder">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="type"><g:message code="repository.page.bkupSchedule.type" /></label>
        </td>
        <td valign="top" class="value ItemDetailValue">
         <select id="type" name="type">
         <option value="dump" <g:if test="${params.type != 'none' && !dump.deltas}"> selected="selected"</g:if>><g:message code="repository.page.bkupSchedule.type.fullDump" /></option>
         <option value="dump_delta" <g:if test="${dump.deltas}"> selected="selected"</g:if>><g:message code="repository.page.bkupSchedule.type.fullDumpDelta"/></option>
         <option value="cloud" disabled="disabled"><g:message code="repository.page.bkupSchedule.type.cloud" /></option>
         <option value="none" <g:if test="${params.type == 'none'}"> selected="selected"</g:if>><g:message code="repository.page.bkupSchedule.type.none" /></option>
         </select>
        </td>
      </tr>

      <tr id="whenRow">
        <td class="ItemDetailName">
          <label for="frequency"><g:message code="repository.page.bkupSchedule.period" /></label>
        </td>
        <td valign="top" class="value ItemDetailValue">
          <table>
            <tr>
              <td>
                <div>
                <label>
                  <g:radio id="frequency_h" name="schedule.frequency" value="HOURLY" checked="${dump.schedule.frequency == SchedulerBean.Frequency.HOURLY}" />
                  <g:message code="repository.page.bkupSchedule.period.hourly" />
                </label>
                </div>
                <div>
                <label>
                  <g:radio id="frequency_d" name="schedule.frequency" value="DAILY" checked="${dump.schedule.frequency == SchedulerBean.Frequency.DAILY}" />
                  <g:message code="repository.page.bkupSchedule.period.daily" />
                </label>
                </div>
                <div>
                <label>
                  <g:radio id="frequency_w" name="schedule.frequency" value="WEEKLY" checked="${dump.schedule.frequency != SchedulerBean.Frequency.HOURLY && dump.schedule.frequency != SchedulerBean.Frequency.DAILY}" />
                  <g:message code="repository.page.bkupSchedule.period.weekly" />
                </label>
                </div>
              </td>
              <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
              <td>

                <table class="ItemDetailContainer">
                  <tr>
                    <td class="ItemDetailName">
                      <g:message code="repository.page.bkupSchedule.startTime" />&nbsp;&nbsp;
                    </td>
                    <td valign="top" class="value ItemDetailValue">
                      <g:set var="hours" value="${(0..23).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                      <g:set var="minutes" value="${(0..59).collect {formatNumber(number: it, minIntegerDigits: 2)}}"/>
                      <span id="time">
                        <g:select id="startHour" name="schedule.hour" from="${hours}" value="${formatNumber(number: dump.schedule.hour, minIntegerDigits: 2)}"/>&nbsp;:&nbsp;<g:select name="schedule.minute" from="${minutes}" value="${formatNumber(number: dump.schedule.minute, minIntegerDigits: 2)}"/>&nbsp;:&nbsp;00
                      </span>
                    </td>
                  </tr>
                  <tr id="dayOfWeekRow">
                    <g:set var="daysOfWeek" value="${[message(code: 'default.dayOfWeek.sunday'), message(code: 'default.dayOfWeek.monday'), message(code: 'default.dayOfWeek.tuesday'), message(code: 'default.dayOfWeek.wednesday'), message(code: 'default.dayOfWeek.thursday'), message(code: 'default.dayOfWeek.friday'), message(code: 'default.dayOfWeek.saturday')]}"/>
                    <td class="ItemDetailName"><g:message code="repository.page.bkupSchedule.dayOfWeek" /></td>
                    <td valign="top" class="value ItemDetailValue">
                      <select id="dayOfWeek" name="schedule.dayOfWeek">
                      <g:each status="i" var="day" in="${daysOfWeek}">
                        <option value="${i + 1}"<g:if test="${dump.schedule.dayOfWeek == i + 1}"> selected="selected"</g:if>>${day}</option>
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
          <label for="keep"><g:message code="repository.page.bkupSchedule.numberToKeep" /></label>
        </td>
        <td valign="top" class="value ItemDetailValue">
          <input type="text" size="3" name="numberToKeep" value="${dump.numberToKeep}" />
          &nbsp;&nbsp;<g:message code="repository.page.bkupSchedule.numberToKeep.all" />
        </td>
      </tr>
     </table>
    </td>
   </tr>
   <tr class="ContainerFooter">
     <td >
       <div class="AlignRight">
          <g:actionSubmit action="updateBkupSchedule" value="${message(code:'default.button.save.label')}" class="Button"/>
       </div>
     </td>
   </tr>
  </table>
</g:form>
<g:javascript>
  function typeHandler() {
      var typeSelect = $('type');
      if (typeSelect.value == 'dump' || typeSelect.value == 'dump_delta') {
          $('whenRow').style.display = '';
          $('keepRow').style.display = '';
      } else if (typeSelect.value == 'cloud') {
          $('whenRow').style.display = '';
          $('keepRow').style.display = 'none';
      } else {
          $('whenRow').style.display = 'none';
          $('keepRow').style.display = 'none';
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

</g:javascript>
