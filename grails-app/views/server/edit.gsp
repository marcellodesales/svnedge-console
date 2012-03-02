<%@ page import="com.collabnet.svnedge.domain.ServerMode" %>
<html>
  <head>
      <meta name="layout" content="main" />

    <g:set var="editAuthConfirmMessage" value="${message(code:'server.page.edit.authentication.confirm')}" />

    <g:javascript>
    /* <![CDATA[ */
    var addrInterfaceMap = []
    <g:each in="${addrInterfaceMap}">
        addrInterfaceMap["${it.key}"] = [
        <g:each var="iface" in="${it.value}">
            "${iface}", 
	    </g:each>
	    ]
    </g:each>
        
        $(document).ready(function() {
            // toggle standard server ports with useSsl field
            $("#useSsl").click(function(event) {
                var sslChkbox = $("#useSsl");
                var port = $("#port");
                if (sslChkbox.attr('checked') && port.val() == '80') {
                    port.val('443');
                }
                else if (!sslChkbox.attr('checked') && port.val() == '443') {
                    port.val('80');
                }
           });
        });

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

      /* ]]> */
    </g:javascript>
    
  </head>
  <content tag="title"><g:message code="admin.page.leftNav.settings" /></content>

  <g:render template="leftNav" />

  <body>
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
<g:if test="${privatePortInstructions}">
<div class="alert alert-block alert-info">
    <g:if test ="${isStandardPort}">
      <i><g:message code="server.page.edit.standardPorts.header" /></i>
    </g:if>
    <g:else>
      <i><g:message code="server.page.edit.privatePorts.header" /></i>
    </g:else>
    <g:if test="${isSolaris}">
      <p><g:message code="server.page.edit.solarisStandardPorts.instructions" /></p>
      <ul>
        <li><g:message code="server.page.edit.solarisStandardPorts.instructions1" args="${[console_user, params.port ?: server.port]}" /></li>
        <li><g:message code="server.page.edit.solarisStandardPorts.instructions2" args="${[console_user]}" /></li>
        <li><g:message code="server.page.edit.solarisStandardPorts.instructions3" /></li>
        <li><g:message code="server.page.edit.solarisStandardPorts.instructions4" /></li>
      </ul>
      <g:if test ="${isStandardPort}">
        <p><g:message code="server.page.edit.solarisStandardPorts.altInstructions" /></p>
      </g:if>
      <g:else>
        <p><g:message code="server.page.edit.solarisPrivatePorts.altInstructions" /></p>
      </g:else>
    </g:if>
    <g:elseif test ="${isStandardPort}">
      <p><g:message code="server.page.edit.standardPorts.instructions" /></p>
    </g:elseif>
    <g:else>
      <p><g:message code="server.page.edit.privatePorts.instructions" /></p>
    </g:else>
<ul>
<g:if test ="${isStandardPort}"> 
    <li><g:message code="server.page.edit.httpdBind" /> <a id="toggleBind" href="#"
      onclick="var el = $('#bindInstructions'); el.toggle(); if (!el.is(':hidden')) { $(this).text('<g:message code="general.hide" />'); } else { $(this).text('<g:message code="server.page.edit.showCommands" />'); } return false;"> <g:message code="server.page.edit.showCommands" /></a>
    <div id="bindInstructions" style="border: 1px;">
    <p><g:message code="server.page.edit.httpdBind.instructions" /> <em><g:message code="server.page.edit.httpdBind.asRoot" /></em>
    </p>
    <blockquote>
    <code>chown root:${httpd_group} ${csvnHome}/lib/httpd_bind/httpd_bind
    <br/>
    chmod u+s ${csvnHome}/lib/httpd_bind/httpd_bind</code>
    </blockquote>
    </div>
    </li>
</g:if>
<li><g:message code="server.page.edit.httpd.asSudo" /> <a id="toggleSudo" href="#" 
  onclick="var el = $('#sudoInstructions'); el.toggle(); if (!el.is(':hidden')) { $(this).text('<g:message code="general.hide" />'); } else { $(this).text('<g:message code="server.page.edit.showCommands" />'); } return false;"> <g:message code="server.page.edit.showCommands" /></a>
<div id="sudoInstructions" style="border: 1px;">
<p>
<g:message code="server.page.edit.httpd.asSudo.instruction" />
</p>
<ul>
<li><g:message code="server.page.edit.httpd.asSudo.command" args="${['<code>/usr/sbin/visudo</code>']}" /><br/><br/>
<code>Defaults env_keep += "PYTHONPATH"<br/>
${console_user}    ALL=(ALL) NOPASSWD: ${csvnHome}/bin/httpd</code>
</li>
</ul>
</div>
</li>
</ul>
<script type="text/javascript">
$('#sudoInstructions').hide();
$('#bindInstructions').hide();
</script>
    </div>
</g:if>

  <g:form class="form-horizontal" method="post" name="serverForm" id="serverForm">
      <g:hiddenField name="view" value="edit"/>
      <g:hiddenField name="id" value="${server.id}" />

      <fieldset>            
        <g:propTextField bean="${server}" field="hostname" required="true" prefix="server"/>

        <g:set var='portTip' value=""/>
        <g:if test="${privatePortInstructions}">
          <g:set var='portTip' value="server.port.label.tip"/>
        </g:if>
        <g:else>
          <g:if test="${(server.useSsl && server.port != 443) || server.port != 80}">
            <g:set var='portTip' value="server.port.label.tip.standardPorts"/>
          </g:if>
        </g:else>
        <g:propTextField bean="${server}" field="port" required="true" 
            integer="true" sizeClass="small" prefix="server" tipCode="${portTip}"/>

        <g:propTextField bean="${server}" field="repoParentDir" required="true" 
            sizeClass="span6" prefix="server"/>

        <g:propTextField bean="${server}" field="dumpDir" required="true" 
            sizeClass="span6" prefix="server"/>

        <g:propControlsBody bean="${server}" field="ipAddress" prefix="server">
            <select class="select-medium" name="ipAddress" id="ipAddress" onchange="updateInterface(this)">
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
        </g:propControlsBody>

        <g:propControlsBody bean="${server}" field="netInterface" prefix="server">
            <g:select name="netInterface" id="netInterface" from="${networkInterfaces}" 
                  value="${server.netInterface}"/>
            <script type="text/javascript">updateInterface(document.getElementById('ipAddress'))</script>
        </g:propControlsBody>
      

        <g:propTextField bean="${server}" field="adminName" prefix="server"/>
        <g:propTextField bean="${server}" field="adminEmail" required="true" prefix="server"/>
        <g:propTextField bean="${server}" field="adminAltContact" prefix="server"/>

        <g:if test="${server.mode == ServerMode.REPLICA}">
          <g:propCheckBox bean="${server}" field="useHttpV2" prefix="server"/>
        </g:if>
      
        <g:propCheckBox bean="${server}" field="useSsl" prefix="server"/>
        <g:propCheckBox bean="${server}" field="useSslConsole" prefix="server"/>
        <g:propCheckBox bean="${server}" field="defaultStart" prefix="server"/>

      </fieldset>
      <div class="form-actions">
        <g:actionSubmit action="update" value="${message(code:'server.page.edit.button.save')}" class="btn btn-primary"/>
        <button type="reset" class="btn"><g:message code="default.button.cancel.label" /></button>
      </div>
    </g:form>             
  </body>
</html>
