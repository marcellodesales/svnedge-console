<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge Logs</title>
</head>

<content tag="title">
    Administration
</content>

%{--
  - Copyright 2010 CollabNet, Inc. All rights reserved.
  --}%
<g:render template="/server/leftNav" />

<body>

  <g:render template="/common/tabs"
      model="[tabs:[
        [action:'list', label:'Available Files', active: false],
        [action:'configure', label:'Configure', active: true]
        ]]" />

  <g:form>

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">
          <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="consoleLevel">Console Log Level:</label>
            </td>
            <td valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'consoleLevel','errors')}">
                 <select class="inputfield" name="consoleLevel" id="consoleLevel">
                    <g:each in="${consoleLevels}" var="level">
                        <option value="${level}" <g:if test="${level == logConfigurationCommand.consoleLevel}">SELECTED</g:if>>${level}</option>
                    </g:each>
                 </select>
            </td>
            <td class="ItemDetailValue"><i>The log level for the Subversion Edge Console</i></td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="apacheLevel">Subversion Server Log Level:</label>
            </td>
            <td valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'apacheLevel','errors')}">
                 <select class="inputfield" name="apacheLevel" id="apacheLevel">
                    <g:each in="${apacheLevels}" var="level">
                        <option value="${level}" <g:if test="${level == logConfigurationCommand.apacheLevel}">SELECTED</g:if>>${level}</option>
                    </g:each>
                 </select>
            </td>
            <td class="ItemDetailValue"><i>The log level for the Apache/Subversion server</i></td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="pruneLogFileOlderThan">Delete log files older than:</label>
            </td>
            <td nowrap valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'pruneLogsOlderThan','errors')}">
              <input name="pruneLogsOlderThan" id="pruneLogFileOlderThan" type="text" size="3"
                value="${logConfigurationCommand.pruneLogsOlderThan}"/> days.
            </td>
            <td class="ItemDetailValue"><i>Value '0' indicates no deletion.</i></td>
          </tr>
          <g:hasErrors bean="${logConfigurationCommand}" field="pruneLogsOlderThan">
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <ul>
                <li><g:message code="logConfiguration.pruneDays"/></li>
              </ul>
            </td>
          </tr>
          </g:hasErrors>

          </table>
        </td>
      </tr>
        <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
                <g:actionSubmit action="saveConfiguration" value="Save" class="Button"/>
            </div>
          </div>
        </td>
      </tr>
     </table>
  </g:form>



</body>
</html>
