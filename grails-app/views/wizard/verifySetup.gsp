<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="wizard" />
    <title>Welcome to the CollabNet Subversion Edge Configuration Wizard</title>
  </head>
  <body>

    <content tag="title">
      CollabNet Subversion Edge Configuration Wizard &gt; Confirmation
    </content>

    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
      <div class="leftDescription">
        Please verify if all information is correct.
        <br/>
        This Replica will start the following Jobs:
        <ul>
          <li>Cache Flusher Job: flushes the cache periodically</li>
          <li>Upload Errors Job: sends the exceptions errors to the Master host
          periodically</li>
          <li>Svn Sync Job: synchronizes the replicated repositories</li>
          <li>Statistics Data Job: sends statistical information about the
          "health" of the Replica</li>
        </ul>
        If you need to make any changes, click in one
        of the sections and follow the instructions.
        <br/>
        <br/>
        After confirming these values, you will be taken
        to the regular Administration area for this Replica.
      </div>

    </content>
    
    <div class="body">
      <div class="dialog">
        <table>
          <tbody>
            <tr>
              <td>
                <strong>This replica will be activated 
                after the confirmation.</strong>
              </td>
            </tr>
            <tr>
              <td>
                
                <table class="ItemDetailContainer">
                  <tbody>
                    <tr class="ContainerHeader">
                      <td colspan="3">Master Host Basic Access</td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <strong>Host Name:</strong>
                      </td>
                      <td valign="top" width="190">
                        ${masterInstance?.hostName}
                      </td>
                      <td>The address of the machine you want to replicate.
                      </td>
                    </tr>
                    
                    <tr class="prop, EvenRow">
                      <td valign="top" class="name">
                        <label for="accessUsername"><strong>Access
                        Username:</strong></label>
                      </td>
                      <td valign="top">
                        ${masterInstance?.accessUsername}
                      </td>
                      <td>The username of the replica user, with full access to
                      execute the Web Services API. 
                      </td>
                    </tr>
                    
                    <tr class="prop, OddRow">
                      <td valign="top" class="name">
                        <label for="accessPassword"><strong>Access
                        Password:</strong></label>
                      </td>
                      <td valign="top">
                        ${masterInstance?.accessPassword}
                      </td>
                      <td>The password for the given username to access the 
                      replica through the Web Services API. 
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
                      <td colspan="3">Communication Security</td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <label for="securityLevel"><strong>Security
                        Level:</strong></label>
                      </td>
                      <td valign="top" width="100">
                        ${masterInstance?.securityLevel}
                      </td>
                      <td>The security level to access the Remote Master CEE.
                      ws-sec-min: Passwords are sent on-the-wire; ws-sec-ext:
                      Password is encrypted.
                      </td>
                    </tr> 
                    <tr class="prop, EvenRow">
                      <td valign="top" class="name">
                        <label for="sslEnabled"><strong>SSL
                        Enabled:</strong></label>
                      </td>
                      <td valign="top">
                        ${masterInstance?.sslEnabled == "true" ? "Yes" : "No"}
                      </td>
                      <td>Verify if the Master is connected through SSL
                      channel. That is, the communication is done through
                      HTTPS, with the trust store file and its accessing
                      password defined in the default configuration.
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
                      <td colspan="4">Replica Host identification</td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="120">
                        <label for="svnReplicationLocation"><strong>Replica
                        Hostname</strong></label>
                      </td>
                      <td valign="top">
                        <strong>${replicaInstance.hostname}</strong>
                      </td>
                      <td>The location where the replicated repositories will
                      be stored.
                      </td>
                    </tr> 

                    <tr class="prop, EvenRow">
                      <td valign="top" class="name">
                        <label for="svnSynchRate"><strong>Replica
                        Location:</strong></label>
                      </td>
                      <td valign="top">
                        <strong>${replicaInstance.getLocationName()}</strong>
                      </td>
                      <td>This is the name of the physical location of this
                      Replica
                      </td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name">
                        <label for="svnReplicationLocation"><strong>Geo
                        Location</strong></label>
                      </td>
                      <td valign="top">
                        <strong>${replicaInstance.getLatitude()},
                        ${replicaInstance.getLongitude()}</strong>
                      </td>
                      <td>The latitude and longitude of the replica.
                      
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
                      <td colspan="3">Svn Replicated Artifacts</td>
                    </tr>
                    <tr class="prop, OddRow">
                      <td valign="top" class="name" width="135">
                        <label for="svnReplicationLocation"><strong>Dir
                        Location</strong></label>
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
                      host. The synchronization status time stamp will be saved
                      at <strong>${svnSyncStatusFilePath}</strong>
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
                      <td valign="top" class="name" width="120">
                        <label for="uploadErrorsRate"><strong>Upload Errors
                        Rate:</strong></label>
                      </td>
                      <td valign="top" width="50">
                        <strong>${uploadErrorRate}</strong>
                      </td>
                      <td>This rate is the number of <b>minutes</b> to transfer
                      existing errors to the Master management.
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
        <g:form method="post" >
          <span class="button"><g:actionSubmit class="save"
          value="Confirm Setup" action="confirmSetup" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>
