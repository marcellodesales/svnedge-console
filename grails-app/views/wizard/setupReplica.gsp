<%@ page import="com.collabnet.svnedge.replica.manager.Master" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="wizard" />
    <title>Welcome to the CollabNet Subversion Edge Configuration Wizard</title>
    <g:javascript library="prototype"/>
    <g:javascript>

    </g:javascript>
  </head>
  <body>
    <content tag="title">
      CollabNet Subversion Edge Configuration Wizard &gt; Setting up this Replica Host
    </content>
    
    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
      <div class="leftDescription">
        Please fill out the following information about
        the Replica:
        <ul>
          <li>User credential cache expiration rate</li>
          <li>How often to upload errors to the Master</li>
          <li>How often to synchronize with the master SVN repositories</li>
        </ul>
        The current values were used during the bootstrap
        of this Replica host. In case you need to change
        them, please consult the system administrator.
      </div>

    </content>
    
    <div class="body">
      <g:form method="post" >
        <input type="hidden" name="id" value="${replicaInstance?.id}" />
        <input type="hidden" name="version"
               value="${replicaInstance?.version}"/>
        <g:hasErrors bean="${masterInstance}">
          <div class="errors">
            <g:renderErrors bean="${masterInstance}" as="list" />
          </div>
        </g:hasErrors>
        <div class="dialog">
          <table>
            <tbody>
              <tr>
                <td>
                  
                  <table class="ItemDetailContainer">
                    <tbody>
                      <tr class="ContainerHeader">
                        <td colspan="4">Replica Host identification</td>
                      </tr>


                      <tr class="prop, OddRow">
                        <td valign="top" class="name" width="160">
                          <label for="name"><strong>
                          Name</strong></label>
                        </td>
                        <td valign="top">
                          <input type="text" name="name" value="${replicaInstance.name}" />
                          <g:hasErrors bean="${replicaInstance}" 
                             field="name">
                            <ul><g:eachError bean="${replicaInstance}" 
                               field="name">
                              <li><g:message error="${it}"/>
                              </li>
                            </g:eachError></ul>
                          </g:hasErrors>
                        </td>
                        <td>The name used to refer to this replica.
                        </td>
                      </tr> 

                      <tr class="prop, EvenRow">
                        <td valign="top" class="name">
                          <label for="replicaLocation"><strong>
                          Location:</strong></label>
                        </td>
                        <td valign="top">
                          <input type="text" name="locationName" value="${replicaInstance.locationName}" />
                          <g:hasErrors bean="${replicaInstance}" 
                             field="locationName">
                            <ul><g:eachError bean="${replicaInstance}" 
                               field="locationName">
                              <li><g:message error="${it}"/></li>
                            </g:eachError></ul>
                          </g:hasErrors>
                        </td>
                        <td>The name of the physical location of this
                        Replica
                        </td>
                      </tr>

                      <tr class="prop, OddRow">
                        <td valign="top" class="name" width="160">
                          <label for="replicaLocationGeo"><strong>
                          Geo Location</strong></label>
                        </td>
                        <td valign="top">
                          <label for="latitude">Latitude:</label>
                          <input name="latitude" type="text" size="10" value="${replicaInstance.latitude}" />&nbsp;
                          <label for="longitude">Longitude:</label>
                          <input name="longitude" type="text" size="10" value="${replicaInstance.longitude}"/>
                          <g:hasErrors bean="${replicaInstance}" 
                             field="latitude">
                            <ul><g:eachError bean="${replicaInstance}" 
                               field="latitude">
                              <li><g:message error="${it}"/></li>
                            </g:eachError></ul>
                          </g:hasErrors>
                          <g:hasErrors bean="${replicaInstance}" 
                             field="longitude">
                            <ul><g:eachError bean="${replicaInstance}" 
                               field="longitude">
                              <li><g:message error="${it}"/></li>
                            </g:eachError></ul>
                          </g:hasErrors>
                        </td>
                        <td>The latitude and longitude of the Replica.
                        </td>
                      </tr> 

                    </tbody>
                  </table>
                </td>
              </tr>
              <tr>
                <td>
                  
                  <table class="ItemDetailContainer">
                    <tbody>
                      <tr class="ContainerHeader">
                        <td colspan="3">Svn Replica Artifacts</td>
                      </tr>
                      <tr class="prop, OddRow">
                        <td valign="top" class="name" width="160">
                          <label for="svnReplicationLocation"><strong>
                          Repository Location</strong></label>
                        </td>
                        <td valign="top">
                          <strong>${replicaRepositoryPath}</strong>
                        </td>
                        <td>The location where the replicated repositories will
                        be stored.
                        </td>
                      </tr> 

                      <tr class="prop, EvenRow">
                        <td valign="top" class="name">
                          <label for="svnSynchRate"><strong>Synchronization
                          Rate:</strong></label>
                        </td>
                        <td valign="top">
                          <strong>${svnSyncRate}</strong>
                        </td>
                        <td>This rate is the number of <b>minutes</b> to
                        synchronize this replica host with the defined Master
                        host. The synchronization status time stamp will be
                        saved in <strong>${svnSyncStatusFilePath}</strong>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
              <tr>
                <td>
                  
                  <table class="ItemDetailContainer">
                    <tbody>
                      <tr class="ContainerHeader">
                        <td colspan="3">Cache Management</td>
                      </tr>
                     
                      <tr class="prop, OddRow">
                        <td valign="top" class="name">
                          <label for="positiveExpirationRate"><strong>Positive
                              Expiration Rate:</strong></label>
                        </td>
                        <td valign="top">
                          <strong>${currentConfig.positiveExpirationRate}</strong>
                        </td>
                        <td>This rate is the number of <b>minutes</b> a
                        successful authentication and authorization cache will
                        live on this Replica.
                        </td>
                      </tr>

                      <tr class="prop, EvenRow">
                        <td valign="top" class="name">
                          <label for="negativeExpirationRate"><strong>Negative
                              Expiration Rate:</strong></label>
                        </td>
                        <td valign="top">
                          <strong>${currentConfig.negativeExpirationRate}
                          </strong>
                        </td>
                        <td>This rate is the number of <b>seconds</b> a
                        unsuccessful authentication and authorization cache 
                        will live on this Replica.
                        </td>
                      </tr>

                      <tr class="prop, OddRow">
                        <td valign="top" class="name">
                          <label for="cacheFlushPeriod"><strong>Cache
                              Flush Period:</strong></label>
                        </td>
                        <td valign="top">
                          <strong>${currentConfig.cacheFlushPeriod}</strong>
                        </td>
                        <td>This rate is the number of <b>hours</b> to flush
                        out the authentication and authorization caches on this
                        Replica.
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
              
              <tr>
                <td>
                  
                  <table class="ItemDetailContainer">
                    <tbody>
                      <tr class="ContainerHeader">
                        <td colspan="3">Collected Data Management</td>
                      </tr>
                      <tr class="prop, OddRow">
                        <td valign="top" class="name" width="160">
                          <label for="uploadErrorsRate"><strong>Upload Errors
                          Rate:</strong></label>
                        </td>
                        <td valign="top" width="50">
                          <strong>${uploadErrorRate}</strong>
                        </td>
                        <td>This rate is the number of <b>minutes</b> to
                        transfer existing errors to the Master management.
                        </td>
                      </tr> 
                    </tbody>
                  </table>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="buttons" align="right">
          <span class="button"><g:actionSubmit class="save"
            value="Confirm Replica" action="updateReplica" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
