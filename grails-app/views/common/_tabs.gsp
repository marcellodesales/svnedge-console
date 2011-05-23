<%-- If you want to add a tab, go to layouts/main.gsp and specify its properties there --%> 

<table cellspacing="0" cellpadding="0" border="0" class="TabTable">
  <tr>
  <g:if test="${tabs[0].action == actionName || tabs[0].active}">
    <td style="font-size: 0px;"><img width="9" height="21" alt="" 
        src="${resource(dir:'images/tabs',file:'first_active_tab.gif')}" /></td>  
  </g:if>
  <g:else>
  <td style="font-size: 0px;"><img width="9" height="21" alt="" 
      src="${resource(dir:'images/tabs',file:'first_inactive_tab.gif')}" /></td>  
  </g:else>
  <g:each in="${tabs}" status="i" var="tab">
    <g:if test="${tab.action == actionName || tab.active}">
      <g:set var="tabImage" value="tab_active_middle.gif"/>
      <g:set var="separatorImage" value="active_inactive_tab.gif"/>
    </g:if>
    <g:else>
      <g:set var="tabImage" value="tab_inactive_middle.gif"/>
      <g:if test="${i < (tabs.size() - 1)}">
        <g:set var="nextTab" value="${tabs[i+1]}"/>
        <g:if test="${nextTab.action == actionName || nextTab.active}">
          <g:set var="separatorImage" value="inactive_active_tab.gif"/>
        </g:if>
        <g:else>
          <g:set var="separatorImage" value="inactive_inactive_tab.gif"/>
        </g:else>
      </g:if>
    </g:else>
    <g:set var="tabLink" value="${tab.href}"/>
    <g:if test="${!tabLink && tab.controller}">
        <g:set var="tabLink" value="${createLink(controller: tab.controller, action: tab.action)}"/>
    </g:if>
    <g:if test="${!tabLink && !tab.controller}">
        <g:set var="tabLink" value="${tab.action}"/>
    </g:if>
    <td style="background: url(${resource(dir:'images/tabs',file:tabImage)})" width="0" 
      class="TabBody">
    <g:if test="${tabLink}">
      <a${tab.action ? ' id="' + tab.action + 'TabLink"' : ''} 
        ${tab.events ? tab.events : ''}  href="${tabLink}">
    </g:if>
      <%= tab.label.replaceAll(" ", "&nbsp;") %> 
    <g:if test="${tabLink}">
      </a>
    </g:if>
    </td>
    <g:if test="${i < tabs.size() - 1}">
      <td style="font-size: 0px;"><img width="19" height="21" alt="" 
          src="${resource(dir:'images/tabs',file:separatorImage)}" /></td>
    </g:if>
  </g:each>
  <g:if test="${tabs[tabs.size() - 1].action == actionName || tabs[tabs.size() - 1]?.active }">
    <g:set var="finaleImage" value="active_last_tab.gif"/>
  </g:if>
  <g:else>
    <g:set var="finaleImage" value="inactive_last_tab.gif"/>
  </g:else>
  <td style="font-size: 0px;"><img width="19" height="21" alt="" 
      src="${resource(dir:'images/tabs',file:finaleImage)}" /></td>
  <td style="background: url(${resource(dir:'images/tabs',file:'horiz_notab_background.gif')})" width="100%">&nbsp;</td>
  </tr>
  <tr class="TabHeader">
  <td nowrap="nowrap" colspan="${2 * tabs.size() + 2}" style="font-size: 1px;"><img width="1" height="1" alt="" 
      src="${resource(dir:'images/misc',file:'pixel.gif')}" /></td>
  </tr>
</table>
