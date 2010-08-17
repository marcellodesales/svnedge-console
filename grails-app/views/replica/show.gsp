<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Show Replica</title>
  </head>
  <body>
    
    <div class="body">
      <h1>Show Replica</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="dialog">
        <table>
          <tbody>
            <tr class="prop">
              <td valign="top" class="name">Id:</td>
              
              <td valign="top" class="value">${fieldValue(bean:replicaInstance,
                                                          field:'id')}</td>
            </tr>
            
            <g:each in="${fields}" var="field">
              <tr class="prop">
                <td valign="top" class="name">${field['title']}:</td>
              
                <td valign="top" class="value">
                  ${fieldValue(bean:replicaInstance,
                               field:field['field'])}
                </td>
            </tr>
            
            </g:each>
          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <input type="hidden" name="id" value="${replicaInstance?.id}" />
          <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>
