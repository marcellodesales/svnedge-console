<%@ page import="com.collabnet.svnedge.replica.manager.Master" %>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Update Master Host</title>
  </head>
  <body>
    <content tag="title">
      CollabNet Subversion Edge Administration &gt; Edit Master Properties
    </content>

    <!-- Following content goes in the left nav area -->
    <content tag="leftMenu">
      <div class="leftDescription">
        Please, fill out the following information about
        the Subversion hosted at a Master CEE/CTF, so that:
        <ul>
          <li>All SVN Repositories will be replicated here</li>
          <li>User Authentication is cached at this host</li>
          <li>User Authorization is cached at this host</li>
        </ul>
        The current values were used during the bootstrap
        of this Replica host. In case you need to change
        these values, please consult the system administrator
        to verify (Config.groovy).
      </div>
    </content>
    
    <div class="body">
      <g:form method="post" >
        <input type="hidden" name="id" value="${masterInstance?.id}" />
        <input type="hidden" name="onWizard" value="${params.onWizard}" />
        <input type="hidden" name="version" value="${masterInstance?.version}" />
        <g:if test="${flash.message}">
          <div class="message">${flash.message}</div>
        </g:if>
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
                        <td valign="top" class="name">
                          <label for="hostName"><strong>Host
                            Name:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'hostName','errors')}">
                          <input type="text" size="37" id="hostName"
                                 name="hostName"
                                 value="${fieldValue(bean:masterInstance,
                                        field:'hostName')}"/>
                        </td>
                        <td>The host address of the <br/>machine you want to
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
                        <td valign="top" class="name">
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
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'sslEnabled','errors')}">
                          <g:checkBox value="${masterInstance?.sslEnabled}"
                                      name="sslEnabled"></g:checkBox>
                        </td>
                        <td>Verify if the Master is connected through SSL
                        channel.  (That means, the connection is done with HTTP
                        or HTTPS protocol.)
                        </td>
                      </tr> 
                      
                      <tr class="prop, OddRow">
                        <td valign="top" class="name">
                          <label for="trustStorePassword"><strong>Trust Store
                            Password:</strong></label>
                        </td>
                        <td valign="top"
                            class="value ${hasErrors(bean:masterInstance,
                                   field:'trustStorePassword','errors')}">
                          <input type="password" size="15"
                                 id="trustStorePassword"
                                 name="trustStorePassword"
                                 value="${fieldValue(bean:masterInstance,
                                        field:'trustStorePassword')}"/>
                        </td>
                        <td>If SSL is enabled, enter the trust store password.
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
          <span class="button"><g:actionSubmit class="save" value="Update"
          /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
