<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="wizard" />
    <title>Welcome to the CollabNet Subversion Edge Configuration Wizard</title>
  </head>
  <body>

    <content tag="title">
      CollabNet Subversion Edge Configuration Wizard &gt; Setting up the Master Host
    </content>

    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
      <div class="leftDescription">
        Please fill out the following information about
        the ${session?.masterBox} master server:
        <ul>
          <li>Master host communication information</li>
          <li>Security level to communicate with the Master</li>
        </ul>
        The current values were used during the bootstrap
        of this Replica host. In case you need to change
        them, please consult the system administrator.
      </div>

    </content>
    
    <div class="body">
      <g:form method="post" >
        <input type="hidden" name="id" value="${masterInstance?.id}" />
        <input type="hidden" name="version" value="${masterInstance?.version}"/>
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
                        <td colspan="3">Host Basic Access</td>
                      </tr>
                      <tr class="prop, OddRow">
                        <td valign="top" class="name" width="120">
                          <label for="hostName"><strong>Host
                          Name:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'hostName','errors')}">
                          <input type="text" size="30" id="hostName"
                                 name="hostName"
                                 value="${fieldValue(bean:masterInstance,
                                        field:'hostName')}"/>
                        </td>
                        <td>The host address of the machine you want to
                        replicate.
                        </td>
                      </tr>
                      
                      <tr class="prop, EvenRow">
                        <td valign="top" class="name">
                          <label for="accessUsername"><strong>Access
                          Username:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'accessUsername','errors')}">
                          <input type="text" size="12" id="accessUsername"
                                 name="accessUsername"
                                 value="${fieldValue(bean:masterInstance,
                                        field:'accessUsername')}"/>
                        </td>
                        <td>The username of the replica user, with full access
                        to execute the Web Services API.
                        </td>
                      </tr>
                      
                      <tr class="prop, OddRow">
                        <td valign="top" class="name">
                          <label for="accessPassword"><strong>Access
                          Password:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'accessPassword','errors')}">
                          <input type="password" size="12" id="accessPassword"
                                 name="accessPassword"
                                 value="${fieldValue(bean:masterInstance,
                                        field:'accessPassword')}"/>
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
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'securityLevel','errors')}">
                          <g:select
                              from="${com.collabnet.svnedge.ws.WsSecurityLevel?.values()}"
                              value="${masterInstance?.securityLevel}"
                              name="securityLevel" ></g:select>
                        </td>
                        <td>The security level to access the Remote Master
                        CEE.<br/> <em>ws-sec-min</em>: Password is sent
                        unencrypted<br/> <em>ws-sec-ext</em>: Password is sent
                        encrypted.
                        </td>
                      </tr> 
                      <tr class="prop, EvenRow">
                        <td valign="top" class="name">
                          <label for="sslEnabled"><strong>SSL
                          Enabled:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'sslEnabled','errors')}">
                          <g:checkBox name="sslEnabled"
                                      value="${masterInstance?.sslEnabled}"
                                      ></g:checkBox>
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
            </tbody>
          </table>
        </div>
        <div class="buttons" align="right">
          <span class="button"><g:actionSubmit class="save"
            value="Confirm Master" action="updateMaster" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
