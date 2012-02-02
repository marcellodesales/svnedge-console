<html>
  <head>
    <title>CollabNet Subversion Edge <g:message code="server.page.editAuthentication.title" /></title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

    <g:set var="editAuthConfirmMessage" value="${message(code:'server.page.edit.authentication.confirm')}" />

    <g:javascript>

        var fieldsChanged = false;
        Event.observe(window, 'load', function() {
            // track field changes for "unsaved changes" alert
            var allInputs = Form.getElements("serverForm")
            allInputs.each(function(item){
                Event.observe(item, 'change', function(event) {
                    fieldsChanged = true;
                });
            })
            
            // toggle the console ldap auth with the general ldapenabled setting
            $('ldapEnabled').observe('change', function(event){
                $('ldapEnabledConsole').checked = $('ldapEnabled').checked
                
            })
        });

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
      if (!fieldsChanged) {
        return true
      }
      return confirm("${editAuthConfirmMessage}");
    }

    </g:javascript>
    
  </head>
  <content tag="title">
    <g:message code="server.page.edit.header" />
  </content>
  
  <g:render template="leftNav" />
  
  <body onLoad="javascript:showHideLdapOptions();">


    <div class="message">${result}</div>
    <g:if test="${!isConfigurable}">
    <div class="instructionText">
    <p><g:message code="server.page.edit.missingDirectives" />
    <blockquote>
    <code>
      Include "${csvnConf}/csvn_main_httpd.conf"<br/>
      Include "${csvnConf}/svn_viewvc_httpd.conf"
    </code>
    </blockquote>
    </p>
    </div>
    </g:if>
    <g:render template="tabs" model="${[view: 'editAuthentication']}" />
    
  <g:form method="post" name="serverForm">
      <g:hiddenField name="view" value="editAuthentication"/>

      <input type="hidden" name="id" value="${server.id}" />
      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">

      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="allowAnonymousReadAccess"><g:message code="server.allowAnonymousReadAccess.label" /></label>
        </td>
        <td valign="top" colspan="2"
            class="ItemDetailValue ${hasErrors(bean:server,field:'allowAnonymousReadAccess','errors')}">
          <g:checkBox name="allowAnonymousReadAccess" value="${server.allowAnonymousReadAccess}"/>
          <label for="allowAnonymousReadAccess"><g:message code="server.allowAnonymousReadAccess.label.tip" /></label>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="forceUsernameCase"><g:message code="server.forceUsernamecase.label" /></label>
        </td>
        <td valign="top" colspan="2"
            class="ItemDetailValue ${hasErrors(bean:server,field:'forceUsernameCase','errors')}">
          <g:checkBox name="forceUsernameCase" value="${server.forceUsernameCase}"/>
          <label for="forceUsernameCase"><g:message code="server.forceUsernameCase.label.tip" /></label>
        </td>
      </tr>
   <g:hasErrors bean="${server}" field="ldapEnabled">
      <tr>
        <td></td>
        <td colspan="2" class="errors">
           <ul><g:eachError bean="${server}" field="ldapEnabled">
             <li><g:message error="${it}" encodeAs="HTML"/></li>
           </g:eachError></ul>
        </td>
      </tr>
   </g:hasErrors>
      <tr>
        <td class="ItemDetailName">
          <label><g:message code="server.authenticationMethods.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'fileLoginEnabled','errors')}">
          <g:checkBox name="fileLoginEnabled" value="${server.fileLoginEnabled}"/>
          <label for="fileLoginEnabled"><g:message code="server.fileLoginEnabled.label" /></label>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapEnabled','errors')}">
          <g:checkBox name="ldapEnabled" value="${server.ldapEnabled}" onClick="javascript:showHideLdapOptions();"/>
         <label for="ldapEnabled"><g:message code="server.ldapEnabled.label" /></label>
        </td>
      </tr>
      </table>
      <div id="ldapDialog">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapSecurityLevel.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapSecurityLevel','errors')}">
          <g:select from="${['NONE', 'SSL', 'TLS', 'STARTTLS']}" value="${fieldValue(bean:server,field:'ldapSecurityLevel')}" name="ldapSecurityLevel"></g:select>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapServerHost.label" /></label>
        </td>
        <td class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerHost','errors')}">
          <input size="30" type="text" id="ldapServerHost" name="ldapServerHost" value="${fieldValue(bean:server,field:'ldapServerHost')}"/>
          <g:hasErrors bean="${server}" field="ldapServerHost">
              <ul><g:eachError bean="${server}" field="ldapServerHost">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
          </g:hasErrors>
        </td>
        <td class="ItemDetailValue">
          <i><strong><g:message code="server.page.editAuthentication.ldapServerHost.example" /></strong></i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapServerPort.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerPort','errors')}">
          <input size="6" type="text" id="ldapServerPort" name="ldapServerPort" 
                  value="${params.ldapServerPort ?: server.ldapServerPort}"/>
          <g:hasErrors bean="${server}" field="ldapServerPort">
              <ul><g:eachError bean="${server}" field="ldapServerPort">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
          </g:hasErrors>

        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapAuthBasedn.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapAuthBasedn','errors')}">
          <input size="30" type="text" id="ldapAuthBasedn" name="ldapAuthBasedn" value="${fieldValue(bean:server,field:'ldapAuthBasedn')}"/>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapAuthBinddn.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapAuthBinddn','errors')}">
          <input size="30" type="text" id="ldapAuthBinddn" name="ldapAuthBinddn" value="${fieldValue(bean:server,field:'ldapAuthBinddn')}"/>
        </td>
        <td class="ItemDetailValue">
          <i><strong><g:message code="general.warning" /></strong> <g:message code="server.page.editAuthentication.anonymBindsNotAllowed" /></i>
          <i><strong><g:message code="server.page.editAuthentication.anonymBindsNotAllowed.example" /></strong></i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapAuthBindPassword.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapAuthBindPassword','errors')}">
          <g:passwordFieldWithChangeNotification name="ldapAuthBindPassword" value="${fieldValue(bean:server,field:'ldapAuthBindPassword')}" size="30"/>
        </td>
        <td class="ItemDetailValue">
          <i><strong><g:message code="general.warning" /></strong> <g:message code="server.page.editAuthentication.anonymBindsNotAllowed" /></i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapLoginAttribute.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapLoginAttribute','errors')}">
          <input size="30" type="text" id="ldapLoginAttribute" name="ldapLoginAttribute" value="${fieldValue(bean:server,field:'ldapLoginAttribute')}"/>
        </td>
        <td class="ItemDetailValue">
          <i><g:message code="server.ldapLoginAttribute.label.tip" /></i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapSearchScope.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue">
          <g:select from="${['sub', 'one']}" value="${fieldValue(bean:server,field:'ldapSearchScope')}" name="ldapSearchScope"></g:select>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapFilter.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'ldapFilter','errors')}">
          <input size="30" maxlength=8000 type="text" id="ldapFilter" name="ldapFilter" value="${fieldValue(bean:server,field:'ldapFilter')}"/>
        </td>
        <td class="ItemDetailValue">
          <i><g:message code="server.ldapFilter.label.tip" /></i>
        </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
          <label for="name"><g:message code="server.ldapServerCertVerificationNeeded.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapServerCertVerificationNeeded','errors')}">
          <g:checkBox name="ldapServerCertVerificationNeeded" value="${server.ldapServerCertVerificationNeeded}"/>
          <g:message code="server.ldapServerCertVerificationNeeded.label.tip" />
        </td>
      </tr> 
      <tr>
        <td class="ItemDetailName">
          <label for="ldapEnabledConsole"><g:message code="server.ldapEnabledConsole.label" /></label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'ldapEnabledConsole','errors')}">
          <g:checkBox name="ldapEnabledConsole" value="${server.ldapEnabledConsole}"/>
          <g:message code="server.ldapEnabledConsole.label.tip" />
        </td>
      </tr>      
     <tr>
        <td class="ItemDetailName">
          <label for="authHelperPort"><g:message code="server.authHelperPort.label" /></label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'authHelperPort','errors')}">
          <input size="6" type="text" id="authHelperPort" name="authHelperPort" 
              value="${params.authHelperPort ?: server.authHelperPort}"/>
          <g:hasErrors bean="${server}" field="authHelperPort">
              <ul><g:eachError bean="${server}" field="authHelperPort">
                  <li><g:message error="${it}" encodeAs="HTML"/></li>
              </g:eachError></ul>
          </g:hasErrors>
        </td>
        <td class="ItemDetailValue">
          <i><g:message code="server.authHelperPort.label.tip" /></i>
        </td>
      </tr>  
      </table>
      </div>
      </td>
      </tr>
      <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
                <g:actionSubmit action="update" value="${message(code:'server.page.editAuthentication.button.save')}" class="Button"/>
            </div>
        </td>
      </tr>
      </table>
      </g:form>             
  </body>
</html>
