<g:applyLayout name="repoDetail">
 <content tag="headSnippet">
   <g:javascript library="prototype" />
 </content>

 <content tag="tabContent">

<g:message code="" />

 <g:form method="post" name="bkupForm">  
  <input type="hidden" name="id" value="${repositoryInstance.id}" />
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
         <option value="dump"><g:message code="repository.page.bkupSchedule.type.fullDump" /></option>
         <option value="dump_delta"><g:message code="repository.page.bkupSchedule.type.fullDumpDelta" /></option>
         <option value="cloud"><g:message code="repository.page.bkupSchedule.type.cloud" /></option>
         <option value="none"><g:message code="repository.page.bkupSchedule.type.none" /></option>
         </select>
        </td>
      </tr>
      
      <tr id="whenRow">
        <td class="ItemDetailName">
          <label for="period"><g:message code="repository.page.bkupSchedule.period" /></label>
        </td>
        <td valign="top" class="value ItemDetailValue">
          <table>
            <tr>
              <td>
                <div>
                <label>
                  <g:radio id="period_h" name="period" value="h" checked="${params.period == 'h'}" />
                  <g:message code="repository.page.bkupSchedule.period.hourly" />
                </label>
                </div>
                <div>
                <label>
                  <g:radio id="period_d" name="period" value="d" checked="${params.period == 'd'}" />
                  <g:message code="repository.page.bkupSchedule.period.daily" />
                </label>
                </div>
                <div>
                <label>
                  <g:radio id="period_w" name="period" value="w" checked="${params.period != 'h' && params.period != 'd'}" />
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
                        <g:select id="startHour" name="startHour" from="${hours}" />&nbsp;:&nbsp;<g:select name="startMinute" from="${minutes}" />&nbsp;:&nbsp;00
                      </span>
                    </td>
                  </tr>
                  <tr id="dayOfWeekRow">
                    <g:set var="daysOfWeek" value="${[message(code: 'default.dayOfWeek.sunday'), message(code: 'default.dayOfWeek.monday'), message(code: 'default.dayOfWeek.tuesday'), message(code: 'default.dayOfWeek.wednesday'), message(code: 'default.dayOfWeek.thursday'), message(code: 'default.dayOfWeek.friday'), message(code: 'default.dayOfWeek.saturday')]}"/>
                    <td class="ItemDetailName"><g:message code="repository.page.bkupSchedule.dayOfWeek" /></td>
                    <td valign="top" class="value ItemDetailValue">
                      <g:select name="day" from="${daysOfWeek}" />
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
          <input type="text" size="3" name="keep" value="${params.keep}" />
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
      if ($('period_h').checked) {
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
      if ($('period_w').checked) {
          $('dayOfWeekRow').style.display = '';
      } else {
          $('dayOfWeekRow').style.display = 'none';
      }
  }
  function periodHandler() {
      displayTimeWidget();
      displayDayOfWeekWidget();
  }
  periodHandler();
  $('period_h').onchange = periodHandler;
  $('period_d').onchange = periodHandler;
  $('period_w').onchange = periodHandler;

</g:javascript>
          

  </content>
</g:applyLayout>
