<html>
  <head>
  <title>
    CollabNet Subversion Edge <g:message code="packagesUpdate.page.available.title" />
  </title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
        djconfig="parseOnLoad: true, isDebug: false"></script>

      <script type="text/javascript">
        dojo.addOnLoad(function() {

           <g:if test="${anyConnectionProblem || !packagesInfo || packagesInfo.size() == 0}">
           document.getElementById("installButton").disabled = true;
           </g:if>

           <g:if test="${!anyConnectionProblem && packagesInfo && packagesInfo.size() > 0}">
           document.getElementById("reloadButton").disabled = true;
           </g:if>
        });
      </script>

  </head>
  <content tag="title">
    <g:message code="packagesUpdate.page.leftNav.header" />
  </content>

  <g:render template="/server/leftNav" />

  <body>

    <g:render template="/packagesUpdate/packagesInfoTable" />

    <g:form method="post">

              <div class="pull-right">
                  <g:actionSubmit id="reloadButton" action="reloadUpdates" 
                                  value="${message(code:'packagesUpdate.page.available.button.reload')}" 
                                  class="btn"/>
                  <g:set var="confirmMsg" value="${message(code:'packagesUpdate.available.install.confirmation')}" />
                  <g:actionSubmit id="installButton" action="installUpdates" 
                                  value="${message(code:'packagesUpdate.page.available.button.install')}" 
                                  class="btn btn-primary"
                                  onclick="return confirm('${confirmMsg}')"
                               />
              </div>

    </g:form>

  </body>
</html>
