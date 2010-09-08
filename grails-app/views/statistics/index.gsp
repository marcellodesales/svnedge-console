<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge <g:message code="statistics.page.index.title" /></title>
    </head>
    

      <content tag="title">
        <g:message code="statistics.page.title" /> 
      </content>
    
      <!-- Following content goes in the left nav area -->
      <content tag="leftMenu">
        <div class="leftDescription">
        <ul class="category">
        <g:each status="i" var="data" in="${statData}">
          <%-- hide empty stat group categories (eg, replica-only categories in standalone mode --%>
          <g:if test="${data.statgroups}">
          <li>${data.category}</li>
            <ul class="group">
            <g:each status="j" var="group" in="${data.statgroups}">
              <li>${group.statgroup}</li>
                <ul class="graph">
                <g:each status="k" var="graph" in="${group.graphs}">
                  <li><a href="#" 
                         onclick="setCurrentGraph('${graph.graphData}');
                                  updateGraph();return false;">
                         ${graph.graphName}
                  </a></li>
                </g:each>
                </ul>
            </g:each>
            </ul>
          </g:if>
        </g:each>
        </ul>
        </div>

      </content>

      <g:render template="chart" />
  
</body>
</html>
