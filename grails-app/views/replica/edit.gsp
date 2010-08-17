<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Edit Replica</title>
  </head>
  <body>

    <content tag="title">
      CollabNet Subversion Edge Administration &gt; Edit Replica Properties
    </content>

    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
      <div class="leftDescription">
        <ul>
          <g:each in="${fields}" var="field">
            <li>${field['desc']}</li>
          </g:each>
        </ul>
      </div>
    </content>

    <div class="body">
      <g:hasErrors bean="${replicaInstance}">
        <div class="errors">
          <g:renderErrors bean="${replicaInstance}" as="list" />
        </div>
      </g:hasErrors>
      <g:form method="post" >
        <input type="hidden" name="id" value="${replicaInstance?.id}" />
        <input type="hidden" name="version"
               value="${replicaInstance?.version}" />
        <div class="dialog">
          <table class="ItemDetailContainer">
            <tbody>
              <tr class="ContainerHeader">
                <td colspan="2">Replica Properties</td>
              </tr>

              <g:each status="i" in="${fields}" var="field">
              
              <tr class="${ (i % 2) == 0? 'EvenRow' : 'OddRow'}">
                <td valign="top" class="name">
                  <label for="${field['field']}">${field['title']}:</label>
                </td>
                <td valign="top"
                    class="value ${hasErrors(bean:replicaInstance,
                           field: field['field'],'errors')}">
                  <input type="text" id="${field['field']}"
                         name="${field['field']}"
                         value="${fieldValue(bean:replicaInstance,
                                field:field['field'])}" />
                </td>
              </tr> 
              </g:each>
             
            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" value="Update"
          /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
