<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>CollabNet Subversion Edge</title>
    <meta name="layout" content="main" />
  </head>

  <content tag="title">
    CollabNet Subversion Edge Administration
  </content>

  <content tag="leftMenu">
    <div class="leftDescription">
      Main administration area with most of the functionalities of CollabNet Subversion Edge:
      <ul>
        <li>Configure the subversion server</li>
        <g:if test="${server.replica}">
          <li>Manage the Remote Master's parameters</li>
          <li>Manage this current Replica's parameters</li>
        </g:if>
        <li>Manage any Scheduled Jobs</li>
        <li>View the current list of errors, if any</li>
        <li>View current statistical data</li>
      </ul>
    </div>
  </content>

  <body>
    <div class="body">
      <div class="dialog">
        <table align="center" width="99%">
          <tbody>
            <tr>
              <td>
                
                <table class="ItemDetailContainer">
                  <tbody>
                    <tr class="ContainerHeader">
                      <td colspan="2">General Configuration</td>
                    </tr>
                    <tr class="prop, EvenRow">
                      <td valign="top" class="name" width="120">
                        <strong><g:link controller="server" action="edit"
                                        params="[id:1]">Update server:</g:link>
                        </strong>
                      </td>
  
                      <td valign="top">
                        Change the properties of the Subversion server
                      </td>
                    </tr>
        <g:if test="${server.replica}">
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <strong><g:link controller="master" action="edit"
                                        params="[id:1]">Update Master:</g:link>
                        </strong>
                      </td>
                      <td valign="top">
                        Change the properties of the default Master
                      </td>
                    </tr>
                    
                    <tr class="prop, EvenRow">
                      <td valign="top" class="name">
                        <strong><g:link controller="replica" action="edit"
                                        params="[id:1]">Update Replica:</g:link>
                        </strong>
                      </td>
                      <td valign="top">
                        Change the properties of this replica
                      </td>
                    </tr>
        </g:if>
                  </tbody>
                </table>
              </td>
            </tr>
            
            <tr>
              <td>
                
                <table class="ItemDetailContainer">
                  <tbody>
                    <tr class="ContainerHeader">
                      <td colspan="2">Activities</td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <strong><g:link controller="jobsAdmin"
                                        action="index">Scheduled Jobs:</g:link>
                        </strong>
                      </td>
                      <td valign="top">
                        Manage the scheduled jobs running on this replica
                      </td>
                    </tr>
                    
                    <tr class="prop, EvenRow">
                      <td valign="top" class="name">
                        <strong>Errors Manager:</strong>
                      </td>
                      <td valign="top">
                        View the current errors that to be sent to the Master
                        host
                      </td>
                    </tr>
                    
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <strong>Statistical Data:</strong>
                      </td>
                      <td valign="top">
                        View statistical data collected on this Replica
                      </td>
                    </tr>
                  </tbody>
                </table>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>
