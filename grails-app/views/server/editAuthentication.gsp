<html>
  <head>
    <title>CollabNet Subversion Edge Authentication</title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

    <g:javascript>

  var pristineFieldValues = "${server.allowAnonymousReadAccess}"
    pristineFieldValues = pristineFieldValues + ":${server.fileLoginEnabled}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapEnabled}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapServerHost}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapSecurityLevel}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapServerPort}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapAuthBasedn}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapAuthBinddn}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapAuthBindPassword}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapLoginAttribute}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapSearchScope}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapFilter}"
    pristineFieldValues = pristineFieldValues + ":${server.ldapServerCertVerificationNeeded}"

        // updates a select for the given values.  Assumes that the options
        // should have both text and value set the same.
        function updateSelect(selectElem, values) {
            removeAllOptions(selectElem)
            for (var i = 0; i < values.length; i++) {
                addOption(selectElem, values[i], values[i])
            }
        }

        // add an option with the given text/value to the select element.
        function addOption(selectElem, text, value) {
            var opt = document.createElement('option');
	        opt.text = text
	        opt.value = value
            try {
                selectElem.add(opt, null) // standards compliant
            }
            catch(ex) {
                selectElem.add(opt) // IE only
            }
        }

        // remove all current options from the select element.
        function removeAllOptions(selectElem) {
            for (var i = selectElem.length - 1; i >= 0; i--) {
                selectElem.remove(i)
            }
        }
    
    function showHideLdapOptions() {
      if (document.forms[0].ldapEnabled.checked)
        document.getElementById('ldapDialog').style.display = 'block';
      else 
        document.getElementById('ldapDialog').style.display = 'none';
    }
    Event.observe(window, 'load', showHideLdapOptions, false);
  

    function warnForUnSavedData() {
        var userFieldValues = document.forms[0].allowAnonymousReadAccess.checked
        userFieldValues = userFieldValues + ":" + document.forms[0].fileLoginEnabled.checked
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapEnabled.checked
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapServerHost.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapSecurityLevel.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapServerPort.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapAuthBasedn.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapAuthBinddn.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapAuthBindPassword.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapLoginAttribute.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapSearchScope.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapFilter.value
        userFieldValues = userFieldValues + ":" + document.forms[0].ldapServerCertVerificationNeeded.checked
        if (userFieldValues == pristineFieldValues) {
            document.location.href = "edit";
            return true
        }
        var r=confirm("Your changes may not have been saved. Press Cancel if you want to remain in this page, Press OK to ignore this changes.");
        if (r==true) {
            document.location.href = "edit";
        } else {
            return false;
        }
    }

    </g:javascript>
    
  </head>
  <content tag="title">
    Administration
  </content>
  
  <g:render template="leftNav" />
  
  <body onLoad="javascript:showHideLdapOptions();">


    <div class="message">${result}</div>
    <g:if test="${!isConfigurable}">
    <div class="instructionText">
    <p>httpd.conf is missing directives which are needed for the management console to support configuration
    of the svn server.  Please add the following Include directives:
    <blockquote>
    <code>
      Include "${csvnConf}/csvn_main_httpd.conf"<br/>
      Include "${csvnConf}/svn_viewvc_httpd.conf"
    </code>
    </blockquote>
    </p>
    </div>
    </g:if>

  <g:set var="events">onclick="warnForUnSavedData()"</g:set>
  <g:render template="/common/tabs" 
      model="[tabs:[
      	[action:'edit', href:'#', events:events, label:'General'], 
      	[action:'editAuthentication', href:'#', label:'Authentication', active: true]
      	]]" />
  <g:form method="post">
      <g:hiddenField name="view" value="editAuthentication"/>

      <input type="hidden" name="id" value="${server.id}" />
      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="allowAnonymousReadAccess">Anonymous Access:</label>
        </td>
        <td valign="top" colspan="2"
            class="ItemDetailValue ${hasErrors(bean:server,field:'allowAnonymousReadAccess','errors')}">
          <g:checkBox name="allowAnonymousReadAccess" value="${server.allowAnonymousReadAccess}"/>
          <label for="allowAnonymousReadAccess">Allow read access to anonymous users</label>
        </td>
      </tr>
   <g:hasErrors bean="${server}" field="ldapEnabled">
      <tr>
        <td></td>
        <td colspan="2" class="errors">
           <ul><g:eachError bean="${server}" field="ldapEnabled">
             <li><g:message error="${it}"/></li>
           </g:eachError></ul>
        </td>
      </tr>
   </g:hasErrors>
      <tr>
        <td class="ItemDetailName">
          <label>Authentication Methods:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'fileLoginEnabled','errors')}">
          <g:checkBox name="fileLoginEnabled" value="${server.fileLoginEnabled}"/>
          <label for="fileLoginEnabled">Local authentication against an htpasswd file along with other providers</label>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapEnabled','errors')}">
          <g:checkBox name="ldapEnabled" value="${server.ldapEnabled}" onClick="javascript:showHideLdapOptions();"/>
         <label for="ldapEnabled">LDAP authentication against an LDAP server</label>
        </td>
      </tr>
      </table>
      <div id="ldapDialog">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Security Level</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapSecurityLevel','errors')}">
          <g:select from="${['NONE', 'SSL', 'TLS', 'STARTTLS']}" value="${fieldValue(bean:server,field:'ldapSecurityLevel')}" name="ldapSecurityLevel"></g:select>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Server Host:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerHost','errors')}">
          <input size="30" type="text" id="ldapServerHost" name="ldapServerHost" value="${fieldValue(bean:server,field:'ldapServerHost')}"/>
          <g:hasErrors bean="${server}" field="ldapServerHost">
              <ul><g:eachError bean="${server}" field="ldapServerHost">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
          </g:hasErrors>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Server Port:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerPort','errors')}">
          <input size="6" type="text" id="ldapServerPort" name="ldapServerPort" value="${fieldValue(bean:server,field:'ldapServerPort')}"/>
          <g:hasErrors bean="${server}" field="ldapServerPort">
              <ul><g:eachError bean="${server}" field="ldapServerPort">
                  <li><g:message error="${it}"/></li>
              </g:eachError></ul>
          </g:hasErrors>

        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Base DN:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapAuthBasedn','errors')}">
          <input size="30" type="text" id="ldapAuthBasedn" name="ldapAuthBasedn" value="${fieldValue(bean:server,field:'ldapAuthBasedn')}"/>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Bind DN:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapAuthBinddn','errors')}">
          <input size="30" type="text" id="ldapAuthBinddn" name="ldapAuthBinddn" value="${fieldValue(bean:server,field:'ldapAuthBinddn')}"/>
        </td>
        <td class="ItemDetailValue">
          <i><strong>Warning: </strong>Use this only if anonymous binds are not allowed</i>        
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Bind Password:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapAuthBindPassword','errors')}">
          <input size="30" type="password" id="ldapAuthBindPassword" name="ldapAuthBindPassword" value="${fieldValue(bean:server,field:'ldapAuthBindPassword')}"/>
        </td>
        <td class="ItemDetailValue">
          <i><strong>Warning: </strong>Use this only if anonymous binds are not allowed</i>        
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Login Attribute:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapLoginAttribute','errors')}">
          <input size="30" type="text" id="ldapLoginAttribute" name="ldapLoginAttribute" value="${fieldValue(bean:server,field:'ldapLoginAttribute')}"/>
        </td>
        <td class="ItemDetailValue">
          <i>The subversion server will match the given username with this attribute, Otherwise 'uid' attribute is matched</i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Search Scope:</label>
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:select from="${['sub', 'one']}" value="${fieldValue(bean:server,field:'ldapSearchScope')}" name="ldapSearchScope"></g:select>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Filter:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapFilter','errors')}">
          <input size="30" maxlength=8000 type="text" id="ldapFilter" name="ldapFilter" value="${fieldValue(bean:server,field:'ldapFilter')}"/>
        </td>
        <td class="ItemDetailValue">
          <i>The subversion server will apply this filter in the login search process</i>        
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name">LDAP Server Certificate<br> Verification:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerCertVerificationNeeded','errors')}">
          <g:checkBox name="ldapServerCertVerificationNeeded" value="${server.ldapServerCertVerificationNeeded}"/>
          Verify the certificate of the LDAP server
        </td>
      </tr>
      </table>
      </div>
      </td>
      </tr>
      <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
                <g:actionSubmit action="update" value="Save" class="Button"/>
            </div>
          </div>
        </td>
      </tr>
      </table>
      </g:form>             
  </body>
</html>
