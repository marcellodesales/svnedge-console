<html>
  <head>
    <title>CollabNet Subversion Edge <g:message code="setupTeamForge.page.index.title" /></title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />
  </head>
  <content tag="title">
    <g:message code="setupTeamForge.page.leftNav.header" />
  </content>

  <g:render template="/server/leftNav" />

  <body>


    <g:set var="tabArray" value="${[[active:true, label: message(code:'setupTeamForge.page.tabs.index', args:[1])]]}" />
    <g:set var="tabArray" value="${tabArray << [label: ' ... ', args:[2]]}" />
 <%--
    <g:if test="${isFreshInstall}">
      <g:set var="tabArray" value="${tabArray << [action:'ctfInfo', label: message(code:'setupTeamForge.page.tabs.confirm', args:[2])]}" />
    </g:if>
    <g:else>
      <g:set var="tabArray" value="${tabArray << [action:'ctfInfo', label: message(code:'setupTeamForge.page.tabs.ctfInfo', args:[2])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.ctfProject', args:[3])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.ctfUsers', args:[4])]}" />
      <g:set var="tabArray" value="${tabArray << [label: message(code:'setupTeamForge.page.tabs.confirm', args:[5])]}" />
    </g:else>
 --%>
    <g:render template="/common/tabs" model="${[tabs: tabArray]}" />
 <table class="ItemDetailContainer">
  <tr>
   <td class="ContainerBodyWithPaddedBorder">

    <a href="http://www.open.collab.net/products/ctf/">
     <img style="float:right; padding: 10px" width="520" height="367" alt="" 
          src="${resource(dir:'images/about',file:'ctf.gif')}" border="0"/>
    </a>
    <br/><br/>
    <p>
    <strong><g:message code="setupTeamForge.page.index.almTitle" /></strong>
    </p>
    <br/>
    <p>
    <a href="http://www.open.collab.net/products/ctf/">CollabNet TeamForge</a> 
       <g:message code="setupTeamForge.page.index.p1" />
    </p>
    <p>CollabNet Subversion Edge <g:message code="setupTeamForge.page.index.p2" />
    </p>
    <p>
      <g:if test="${isFreshInstall}">
        <g:message code="setupTeamForge.page.index.p3.freshConversion" />
      </g:if>
      <g:else>
        <g:message code="setupTeamForge.page.index.p3.complete" />
      </g:else>
    </p>
   </td>
   </tr>
     <tr class="ContainerFooter">
       <td >
         <g:form method="post">
         <div class="AlignRight">
             <g:actionSubmit id="btnCtfMode" value="${message(code:'setupTeamForge.page.index.button.ctfMode')}" 
                 controller="setupTeamForge" action="ctfInfo" class="Button"
                 />
             <input type="button" id="btnReplicaMode" value="${message(code:'setupTeamForge.page.index.button.replicaMode')}"
                 onclick="document.location.href='${createLink(controller: 'setupReplica', action:'ctfInfo')}'; return false"
                 class="Button"/>
         </div>
         </g:form>
       </td>
     </tr>
</table>

  </body>
</html>
