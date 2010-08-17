<html>
  <head>
      <title>
        CollabNet Subversion Edge
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
    Software Updates
  </content>

  <g:render template="/server/leftNav" />

  <body>

    <g:render template="/packagesUpdate/packagesInfoTable" />

    <g:form method="post">

      <table class="Container">
        <tbody>
          <tr class="ContainerFooter">
            <td>
              <div class="AlignRight">
                  <g:actionSubmit id="reloadButton" action="reloadAddOns" 
                                  value="Reload New Packages"
                                  class="Button"/>
                  <g:actionSubmit id="installButton" action="installAddOns" 
                                  value="Install New Packages"
                                  class="Button"
                                  onclick="return confirm('The console needs ' +
                                   'to be restarted after installing new ' +
                                   'packages. Continue?')"
                               />
              </div>
            </td>
          </tr>
        </tbody>
      </table>

    </g:form>

  </body>
</html>
