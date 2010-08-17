<html>
  <head>
    <title>CollabNet Subversion Edge Configuration</title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

    <g:javascript>
    
    var addrInterfaceMap = []
    <g:each in="${addrInterfaceMap}">
        addrInterfaceMap["${it.key}"] = [
        <g:each var="iface" in="${it.value}">
            "${iface}", 
	    </g:each>
	    ]
    </g:each>
    var pristineFieldValues = "${fieldValue(bean:server,field:'hostname')}"
    pristineFieldValues = pristineFieldValues + ":${server.port}"
    // backslashes in windows paths are converted to forward slash for purposes of this
    // field-dirtiness check
    pristineFieldValues = pristineFieldValues
            + ":${fieldValue(bean:server,field:'repoParentDir').toString().replaceAll("\\\\","/")}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'ipAddress')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'netInterface')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'adminName')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'adminEmail')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'adminAltContact')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'useSsl')}"
    pristineFieldValues = pristineFieldValues + ":${fieldValue(bean:server,field:'defaultStart')}"

        function updateInterface(addrSelect) {
            var val = addrSelect.value
            var options = addrInterfaceMap[val]
            var selectElement = document.getElementById("netInterface")
            removeAllOptions(selectElement)
            for (var i = 0; i < options.length; i++) {
                addOption(selectElement, options[i], options[i])
            }
        }
    
        // update select boxes with new options for change in NetworkInterface
        function updateNetInt(e) {
           var result = eval("(" + e.responseText + ")")
           var ipAddresses = result.ipAddresses
           updateIpAddresses(ipAddresses)
        }

        // update the select for ipaddresses
        function updateIpAddresses(ipAddresses) {
            var ipSelect = document.getElementById("ipaddress")
            updateSelect(ipSelect, ipAddresses)
        }

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

        function warnForUnSavedData() {
            var userFieldValues = document.forms[0].hostname.value
            userFieldValues = userFieldValues + ":" + document.forms[0].port.value
            // backslashes in windows paths are converted to forward slash for purposes of this
            // field-dirtiness check
            userFieldValues = userFieldValues + ":" + document.forms[0].repoParentDir.value.replace(/\\/g, "/")
            userFieldValues = userFieldValues + ":" + document.forms[0].ipAddress.value
            userFieldValues = userFieldValues + ":" + document.forms[0].netInterface.value
            userFieldValues = userFieldValues + ":" + document.forms[0].adminName.value
            userFieldValues = userFieldValues + ":" + document.forms[0].adminEmail.value
            userFieldValues = userFieldValues + ":" + document.forms[0].adminAltContact.value
            userFieldValues = userFieldValues + ":" + document.forms[0].useSsl.checked
            userFieldValues = userFieldValues + ":" + document.forms[0].defaultStart.checked

            if (userFieldValues == pristineFieldValues) {
                document.location.href = "editAuthentication";
                return true
            }
            var r=confirm("Your changes may not have been saved. Press Cancel if you want to remain in this page, Press OK to ignore this changes.");
            if (r==true) {
                document.location.href = "editAuthentication";
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

  <body>
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
<g:if test="${standardPortInstructions}">
<div class="instructionText">
    <i>Standard ports require additional setup.</i>
    <p>Two options exist to allow use of port 80 or 443.  
    Only one or the other is necessary and both require root privileges to setup. 
</p>
<ul>
<li>Start httpd under sudo. <a id="toggleSudo" href="#" 
  onclick="var el = $('sudoInstructions'); el.toggle(); if (el.visible()) { this.update('Hide'); } else { this.update('Show commands'); } return false;">Show commands</a>
<div id="sudoInstructions" style="border: 1px;">
<p>
The svn server can be started with root privileges allowing it to bind to the port, after which the 
server will reduce its privileges.  To use this method and allow starting and stopping the server from 
the management console, setup sudo for the httpd binary to work without a password.
</p>
<ul>
<li><code>/usr/sbin/visudo</code></li>
<li>Add the following line to the end of the file:<br/><br/>
<code>${console_user}    ALL=(ALL) NOPASSWD: ${csvnHome}/bin/httpd</code>
<br/>
</li>
</ul>
</div>
</li>
<li>Use bind helper application. <a id="toggleBind" href="#" 
  onclick="var el = $('bindInstructions'); el.toggle(); if (el.visible()) { this.update('Hide'); } else { this.update('Show commands'); } return false;">Show commands</a>
<div id="bindInstructions" style="border: 1px;">
<p>httpd_bind is a small application included with 
CollabNet Subversion Edge to allow the server access to the standard ports 
without the server itself running with elevated privileges.  In order for 
it to work, httpd_bind must be owned by root and have its suid bit set such as shown by the 
commands below.  <em>These must be executed as root or sudo.</em>
</p>
<blockquote>
<code>chown root:${httpd_group} ${csvnHome}/lib/httpd_bind/httpd_bind
<br/>
chmod u+s ${csvnHome}/lib/httpd_bind/httpd_bind</code>
</blockquote>
</div>
</li>
</ul>
<script type="text/javascript">
$('sudoInstructions').hide();
$('bindInstructions').hide();
</script>
<!-- Don't show this until isDefaultPortsAllowed accounts for it
<p>Solaris offers another option which can be used to give non-root 
users access to ports less than 1024.</p>
<blockquote>
<code>
/usr/sbin/usermod -K defaultpriv=basic,net_privaddr ${console_user}
</code>
</blockquote>
-->
    </div>
</g:if>
  
  <g:set var="events">onclick="warnForUnSavedData()"</g:set>
  <g:set var="tabArray" value="${[[action:'edit', href:'#', label:'General', active: true]]}" />
  <g:if test="${!isManagedMode}">
    <g:set var="tabArray" value="${tabArray << [action:'editAuthentication', href:'#', events:events, label:'Authentication']}" />
  </g:if>  
    <g:render template="/common/tabs" model="${[tabs: tabArray]}" />
  <g:form method="post" onSubmit="javascript:check();">
      <g:hiddenField name="view" value="edit"/>
  
      <input type="hidden" name="id" value="${server.id}" />
      <table class="ItemDetailContainer">
      <tr>
        <td class="ContainerBodyWithPaddedBorder">
      <table class="ItemDetailContainer">
      <tr>
        <td class="ItemDetailName">
          <label for="hostname">Hostname:</label>
        </td>
        <td valign="top" class="value ${hasErrors(bean:server,field:'hostname','errors')}">
          <input size="30" type="text" id="hostname" name="hostname" 
              value="${fieldValue(bean:server,field:'hostname')}"/>
        </td>
        <td class="ItemDetailValue"><i>The fully qualified hostname including domain.</i></td>
      </tr>
    <g:hasErrors bean="${server}" field="hostname">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" valign="top" class="errors">
          <ul><g:eachError bean="${server}" field="hostname">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>

      <tr>
        <td class="ItemDetailName">
          <label for="port">Port:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'port','errors')}">
          <input size="6" type="text" id="port" name="port" 
              value="${fieldValue(bean:server,field:'port').replace(',','')}"/>
        </td>
        <td class="ItemDetailValue">
            <g:if test="${standardPortInstructions}">
                <i>Standard ports require additional setup.</i>
            </g:if>
            <g:else>
            <g:if test="${(server.useSsl && server.port != 443) || server.port != 80}">
                <i>Standard ports may require additional setup.</i>
            </g:if>
            </g:else>
         </td>
      </tr> 
    <g:hasErrors bean="${server}" field="port">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" valign="top" class="errors">
          <ul><g:eachError bean="${server}" field="port">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>
    
      <tr>
        <td class="ItemDetailName">
          <label for="repoParentDir">Repository Parent Directory:</label>
        </td>
        <td class="value ${hasErrors(bean:server,field:'repoParentDir','errors')}">
          <input size="30" type="text" id="repoParentDir" name="repoParentDir" 
              value="${fieldValue(bean:server,field:'repoParentDir')}"/>
        </td>
        <td class="ItemDetailValue"><i>Parent directory that includes all repositories.</i></td>
      </tr> 
    <g:hasErrors bean="${server}" field="repoParentDir">
      <tr>
        <td>&nbsp;</td>
        <td colspan="2" width="100%" valign="top" class="errors">
          <ul><g:eachError bean="${server}" field="repoParentDir">
              <li><g:message error="${it}"/></li>
          </g:eachError></ul>
        </td>
      </tr>
    </g:hasErrors>
      
      <tr>
      	  <td class="ItemDetailName">
              <label for="ipAddress">IP Address:</label>
          </td>
          <td valign="top" class="value">
              <select name="ipAddress" id="ipAddress" onchange="updateInterface(this)">
                  <optgroup label="IPv4">
                  <g:each var="addr" in="${ipv4Addresses.collect{ it.getHostAddress() }}">
                    <g:set var="isSelected" value=""/>
                    <g:if test="${server.ipAddress == addr}">
                      <g:set var="isSelected"> selected="selected"</g:set>
                    </g:if>
					<option value="${addr}"${isSelected}>${addr}</option>
				  </g:each>
				  </optgroup>
                  <optgroup label="IPv6">
                  <g:each var="addr" in="${ipv6Addresses.collect{ it.getHostAddress() }}">
                    <g:set var="isSelected" value=""/>
                    <g:if test="${server.ipAddress == addr}">
                      <g:set var="isSelected"> selected="selected"</g:set>
                    </g:if>
					<option value="${addr}"${isSelected}>${addr}</option>
				  </g:each>
				  </optgroup>
			  </select>
          </td>
      </tr>
      <tr>
          <td class="ItemDetailName">
              <label for="interface">Network Interface:</label>
          </td>
          <td class="value">
             <g:select name="netInterface" from="${networkInterfaces}" 
                 value="${server.netInterface}"/>
             <script type="text/javascript">updateInterface(document.getElementById('ipAddress'))</script>
          </td>
      </tr>
      <tr>
        <td class="ItemDetailName">
              <label for="adminName">Administrator:</label>
          </td>
          <td colspan="2" class="ItemDetailValue">
              <!-- Widget should eventually change to person picker (See TeamForge) -->
              <input name="adminName" type="text" 
                  value="${server.adminName}"/>
              <g:hasErrors bean="${server}" field="adminName">
                 <ul><g:eachError bean="${server}" field="adminName">
                      <li><g:message error="${it}"/></li>
                  </g:eachError></ul>
              </g:hasErrors>
          </td>
      </tr>
      <!-- The following 2 table rows should be removed after updating UI to person picker -->
      <tr>
          <td class="ItemDetailName">
              <label for="adminEmail">Administrator Email:</label>
          </td>
          <td class="value errors" colspan="2">
              <input name="adminEmail" type="text" 
                  value="${server.adminEmail}"/>
              <g:hasErrors bean="${server}" field="adminEmail">
                  <ul><g:eachError bean="${server}" field="adminEmail">
                      <li><g:message error="${it}"/></li>
                  </g:eachError></ul>
              </g:hasErrors>
          </td>
      </tr>
      <tr>
          <td class="ItemDetailName">
              <label for="adminAltContact">Administrator Alternative Contact:</label>
          </td>
          <td valign="top" class="ItemDetailValue" colspan="2">
              <input name="adminAltContact" type="text" 
                  value="${server.adminAltContact}"/>
              <g:hasErrors bean="${server}" field="adminAltContact">
                  <ul><g:eachError bean="${server}" field="adminAltContact">
                      <li><g:message error="${it}"/></li>
                  </g:eachError></ul>
              </g:hasErrors>
          </td>
      </tr>      
      <tr>
         <td class="ItemDetailName">
          <label for="name">Apache Encryption:</label>
         </td>
        <td class="ItemDetailValue ${hasErrors(bean:server,field:'useSsl','errors')}" colspan="2">
          <g:checkBox name="useSsl" value="${server.useSsl}"/>
          Subversion Server should serve via https
        </td>
      </tr> 
      <tr>
        <td class="ItemDetailName">
          <label for="defaultStart">Startup Setting:</label>
        </td>
        <td colspan="2" class="ItemDetailValue ${hasErrors(bean:server,field:'defaultStart','errors')}">
          <g:checkBox name="defaultStart" value="${server.defaultStart}"/>
          Start Subversion server when the management console is started 
        </td>
      </tr>
     
      </table>
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
