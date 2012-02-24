<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Scheduled Jobs Administration</title>
  </head>
  <body>
    <content tag="title">
      Scheduled Jobs Administration
    </content>
    
    <g:render template="/server/leftNav"/>

      <div class="well">
        The list of schedule jobs can be seen in the form.
        Here's the list of operations that can be done.
        <ul>
          <li><strong>Pause All:</strong> stops all jobs from running</li>
          <li><strong>Resume All:</strong> restart all paused jobs</li>
        </ul>
        <strong>Current Summary from Quartz:</strong>
        <p>
          ${summary}
        </p>
      </div>
    </div>
    <div class="body">
      <div class="dialog">
        <table class="ItemDetailContainer" align="center" width="99%">
                <tbody>
                  <tr class="ContainerHeader">
                    <td colspan="2">Update Jobs Scheduler</td>
                  </tr>
                  <tr class="prop, OddRow">
                    <td align="center">
                      <g:if test="${anyJobsRunning}">
                        <g:form method="post">
                          <input type="hidden" name="operation"
                                 value="pauseAll"/>
                          <g:actionSubmit class="save" value="Pause All"
                                          action="updateJobs" />
                        </g:form>
                      </g:if>
                      <g:else>
                        <input type="button" value="Pause All"
                               disabled="value"/>
                      </g:else>
                    </td>
                    <td valign="top" class="name" align="center">
                      <g:if test="${anyJobsPaused}">
                        <g:form method="post">
                          <input type="hidden" name="operation"
                                 value="resumeAll"/>
                          <g:actionSubmit class="save" value="Resume All"
                                          action="updateJobs" />
                        </g:form>
                      </g:if>
                      <g:else>
                        <input type="button" value="Resume All"
                               disabled="value"/>
                      </g:else>
                    </td>
                  </tr> 
                </tbody>
        </table>
      </div>

<br/>
<hr/>
<br/>

      <g:render template="/jobsAdmin/currentJobsTableSummary" 
                model="['groupTriggers':groupTriggers]" />

    </div>
  </body>
</html>
