<html>
  <head>
      <meta name="layout" content="main" />
  </head>
  <content tag="title"><g:message code="setupTeamForge.page.index.title" /></content>

  <g:render template="/server/leftNav" />

  <body>

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
         <g:form method="post">
         <div class="form-actions">
             <g:actionSubmit id="btnCtfMode" value="${message(code:'setupTeamForge.page.index.button.ctfMode')}" 
                 controller="setupTeamForge" action="ctfInfo" class="btn"
                 />
             <input type="button" id="btnReplicaMode" value="${message(code:'setupTeamForge.page.index.button.replicaMode')}"
                 onclick="document.location.href='${createLink(controller: 'setupReplica', action:'ctfInfo')}'; return false"
                 class="btn"/>
         </div>
         </g:form>
  </body>
</html>
