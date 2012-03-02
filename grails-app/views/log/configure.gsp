<html>
<head>
  <meta name="layout" content="main"/>
</head>

<content tag="title"><g:message code="logs.page.configure.title" /></content>

<g:render template="/server/leftNav" />

<body>
  <g:form class="form-horizontal">
    <g:propControlsBody bean="${logConfigurationCommand}" field="consoleLevel" prefix="logConfigurationCommand">
      <select class="inputfield" name="consoleLevel" id="consoleLevel">
        <g:each in="${consoleLevels}" var="level">
          <option value="${level}" <g:if test="${level == logConfigurationCommand.consoleLevel}">selected="selected"</g:if>>${level}</option>
        </g:each>
      </select>
    </g:propControlsBody>
    
    <g:propControlsBody bean="${logConfigurationCommand}" field="apacheLevel" prefix="logConfigurationCommand">
      <select class="inputfield" name="apacheLevel" id="apacheLevel">
        <g:each in="${apacheLevels}" var="level">
          <option value="${level}" <g:if test="${level == logConfigurationCommand.apacheLevel}">selected="selected"</g:if>>${level}</option>
        </g:each>
      </select>
    </g:propControlsBody>
    
    <g:propTextField bean="${logConfigurationCommand}" field="pruneLogsOlderThan" prefix="logConfigurationCommand" sizeClass="mini"/>
    <div class="form-actions">
      <g:actionSubmit action="saveConfiguration" value="${message(code:'logs.page.configure.button.save')}" class="btn btn-primary"/>
      <button type="reset" class="btn"><g:message code="default.button.cancel.label" /></button>
    </div>
  </g:form>
</body>
</html>
