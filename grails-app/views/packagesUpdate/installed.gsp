<html>
  <head>
      <title>
        CollabNet Subversion Edge
      </title>
      <meta name="layout" content="main" />
      <g:javascript library="prototype" />

   <g:if test="${anyConnectionProblem || !packagesInfo || packagesInfo.size() == 0}">
      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
        djconfig="parseOnLoad: true, isDebug: false"></script>
      <script type="text/javascript">
        dojo.addOnLoad(function() {
           document.getElementById("reloadUpdates").disabled = true;
           document.getElementById("reloadAddOns").disabled = true;
        });
      </script>
   </g:if>

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
            <td >
              <div class="AlignRight">
                  <g:actionSubmit id="reloadUpdates" action="reloadUpdates" 
                                  value="Check for Updates" 
                                  class="Button"/>
                  <g:actionSubmit id="reloadAddOns" action="reloadAddOns" 
                                  value="Check for New Packages" 
                                  class="Button"/>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

    </g:form>

  </body>
</html>
