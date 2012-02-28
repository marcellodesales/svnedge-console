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
    <div class="alert alert-block alert-info">
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
    
  <g:form class="form-horizontal" method="post" name="serverForm">
      <g:hiddenField name="view" value="editAuthentication"/>
      <g:hiddenField name="id" value="${server.id}" />
      
      <fieldset>
        <legend><g:message code="admin.page.leftNav.auth" /></legend>
        <g:propCheckBox bean="${server}" field="allowAnonymousReadAccess" prefix="server"/>
        <g:propCheckBox bean="${server}" field="forceUsernameCase" prefix="server"/>

        <g:propControlsBody bean="${server}" field="ldapEnabled" prefix="server" labelCode="server.authenticationMethods.label">
            <g:checkBox name="fileLoginEnabled" value="${server.fileLoginEnabled}"/>
            <label class="checkbox inline withFor" for="fileLoginEnabled"><g:message code="server.fileLoginEnabled.label" /></label><br />
            <g:checkBox name="ldapEnabled" value="${server.ldapEnabled}" onClick="javascript:showHideLdapOptions();"/>
            <label class="checkbox inline withFor" for="ldapEnabled"><g:message code="server.ldapEnabled.label" /></label>
        </g:propControlsBody>

      <div id="ldapDialog">
      
        <g:propControlsBody bean="${server}" field="ldapSecurityLevel" prefix="server" required="true">
          <g:select from="${['NONE', 'SSL', 'TLS', 'STARTTLS']}" value="${fieldValue(bean:server,field:'ldapSecurityLevel')}" name="ldapSecurityLevel"></g:select>
        </g:propControlsBody>
      
        <g:propTextField bean="${server}" field="ldapServerHost" prefix="server" required="true"/>
        <g:propTextField bean="${server}" field="ldapServerPort" prefix="server" required="true" sizeClass="small" integer="true"/>
        <g:propTextField bean="${server}" field="ldapAuthBasedn" prefix="server" sizeClass="xxlarge"/>
        <g:propTextField bean="${server}" field="ldapAuthBinddn" prefix="server" sizeClass="xxlarge"/>
        <g:propTextField bean="${server}" field="ldapAuthBindPassword" prefix="server"/>
        <g:propTextField bean="${server}" field="ldapLoginAttribute" prefix="server"/>
        
        <g:propControlsBody bean="${server}" field="ldapSearchScope" prefix="server">
            <g:select from="${['sub', 'one']}" value="${fieldValue(bean:server,field:'ldapSearchScope')}" name="ldapSearchScope"></g:select>
        </g:propControlsBody>
        
        <g:propTextField bean="${server}" field="ldapFilter" prefix="server" sizeClass="xxlarge" maxlength="8000"/>

        <g:propCheckBox bean="${server}" field="ldapServerCertVerificationNeeded" prefix="server"/>
        <g:propCheckBox bean="${server}" field="ldapEnabledConsole" prefix="server"/>
        <g:propTextField bean="${server}" field="authHelperPort" prefix="server" sizeClass="small" integer="true"/>

      </fieldset>
      <div class="form-actions">
        <g:actionSubmit action="update" value="${message(code:'server.page.editAuthentication.button.save')}" class="btn btn-primary"/>
        <button type="reset" class="btn"><g:message code="default.button.cancel.label" /></button>
      </div>
    </g:form>             
  </body>
</html>
