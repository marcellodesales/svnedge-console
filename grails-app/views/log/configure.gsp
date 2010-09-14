<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.configure.title" /></title>
</head>

<content tag="title">
   <g:message code="server.page.edit.header" />
</content>

<g:render template="/server/leftNav" />

<body>

  <g:set var="tabArray" value="${[[action:'list', label: message(code:'logs.page.tabs.available')]]}" />
  <g:set var="tabArray" value="${tabArray << [active: true, label: message(code:'logs.page.tabs.settings')]}" />
  <g:render template="/common/tabs" model="${[tabs: tabArray]}" />

  <g:form>

      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">
          <table class="ItemDetailContainer">
          <tr>
            <td class="ItemDetailName">
              <label for="consoleLevel"><g:message code="logConfigurationCommand.consoleLevel.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'consoleLevel','errors')}">
                 <select class="inputfield" name="consoleLevel" id="consoleLevel">
                    <g:each in="${consoleLevels}" var="level">
                        <option value="${level}" <g:if test="${level == logConfigurationCommand.consoleLevel}">SELECTED</g:if>>${level}</option>
                    </g:each>
                 </select>
            </td>
            <td class="ItemDetailValue"><i><g:message code="logConfigurationCommand.consoleLevel.label.tip" /></i></td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="apacheLevel"><g:message code="logConfigurationCommand.apacheLevel.label" /></label>
            </td>
            <td valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'apacheLevel','errors')}">
                 <select class="inputfield" name="apacheLevel" id="apacheLevel">
                    <g:each in="${apacheLevels}" var="level">
                        <option value="${level}" <g:if test="${level == logConfigurationCommand.apacheLevel}">SELECTED</g:if>>${level}</option>
                    </g:each>
                 </select>
            </td>
            <td class="ItemDetailValue"><i><g:message code="logConfigurationCommand.apacheLevel.label.tip" /></i></td>
          </tr>
          <tr>
            <td class="ItemDetailName">
              <label for="pruneLogFileOlderThan"><g:message code="logConfigurationCommand.pruneLogFileOlderThan.label" /></label>
            </td>
            <td nowrap valign="top" class="value ${hasErrors(bean:logConfigurationCommand,field:'pruneLogsOlderThan','errors')}">
              <input name="pruneLogsOlderThan" id="pruneLogFileOlderThan" type="text" size="3"
                value="${logConfigurationCommand.pruneLogsOlderThan}"/> <g:message code="logConfigurationCommand.pruneLogFileOlderThan.days" />
            </td>
            <td class="ItemDetailValue"><i><g:message code="logConfigurationCommand.pruneLogFileOlderThan.label.tip" /></i></td>
          </tr>
          <g:hasErrors bean="${logConfigurationCommand}" field="pruneLogsOlderThan">
          <tr>
            <td></td>
            <td class="errors" colspan="2">
              <ul>
                <li><g:message code="logConfigurationCommand.pruneLogFileOlderThan.error"/></li>
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
                <g:actionSubmit action="saveConfiguration" value="${message(code:'logs.page.configure.button.save')}" class="Button"/>
            </div>
        </td>
      </tr>
     </table>
  </g:form>



</body>
</html>
