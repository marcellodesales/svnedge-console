<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.show.title" args="${[params.fileName]}" encodeAs="HTML"/></title>
  <g:javascript library="prototype" />
  <g:javascript library="application" />
  <g:javascript>


    Event.observe(window, 'load', function() {
        $('tailButton').observe('click', toggleLogStreaming)

        // allow initial state of tailing
        if ('${params.view}' == 'tail') {
            toggleLogStreaming()
        }
    })

    var logStreamer;
    var tailLog = false
    function toggleLogStreaming() {
      tailLog = !tailLog;
      if (tailLog) {
          logStreamer  = new LogStreamer('${params.fileName}', '${fileSizeBytes}', $('fileContent'), $('fileContentDiv'))
          logStreamer.start();
          $('tailButton').update("<g:message code="logs.page.show.button.tailStop"/>");
      }
      else {
          logStreamer.stop();
          $('tailButton').update("<g:message code="logs.page.show.button.tail"/>");
      }
    }

</g:javascript>
</head>

<content tag="title">
   <g:message code="server.page.edit.header" />
</content>

<g:render template="/server/leftNav" />

<body>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
 <table class="Container">
  <tbody>
    <tr class="ContainerHeader">
      <td><g:message code="logs.page.show.header.fileName" /> ${params.fileName} &nbsp;<g:message code="logs.page.show.header.size" /> ${fileSize} &nbsp;<g:message code="logs.page.show.header.lastModification" /> ${fileModification}</td>
    </tr>
    <g:if test="${file}">
    <tr>
      <td>
        <!-- This is still in progress, need to look for grails ui component that does this gracefully  -->
        <div style="width: 800px; height: 300px; overflow: auto;" id="fileContentDiv">
<!-- Leave this left-justified so that spaces are not padded in the first line of the log -->
<pre id="fileContent">
<%
if (params.highlight) {
   file.withReader { reader ->
	  String line
      boolean found = false
	  while ( (line = reader.readLine() ) != null ) {
        if (!found && line.contains(params.highlight)) {
            line = "<a name='loc'> </a><BR>"  + line
            found = true
        }
        line = line.replace(params.highlight, "<span style='background-color: #FFFF00'>${params.highlight}</span>")
	    out << line + "<BR/>"
     }
   }
} else {
    file.withReader { reader ->
       String line
       while ( (line = reader.readLine() ) != null ) {
         out << StringEscapeUtils.escapeHtml(line) + "\n"
      }
    }
}
%>
</pre>
        </div>
      </td>
    </tr>
    </g:if>
  <g:else>
    <tr class="ItemListNoData">
        <td colspan="3"><g:message code="logs.page.show.header.fileNotFound" args="${[params.fileName]}" encodeAs="HTML"/></td>
      </tr>
  </g:else>
   
  <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
              <div class="Button"><div class="Middle">
                <g:link id="tailButton" url="#" class="Button" onclick="return false"><g:message code="logs.page.show.button.tail" /></g:link>
              </div></div>
              <div class="Button"><div class="Middle">                
                <g:link target="_blank" action="show" params="[fileName : file.name, view : 'raw']" class="Button"><g:message code="logs.page.show.button.viewRaw" /> &#133;</g:link>
              </div></div>
              <div class="Button"><div class="Middle">
                <g:link action="list" class="Button"><g:message code="logs.page.show.button.return" /></g:link>
              </div></div>
            </div>
        </td>
      </tr>
   </tbody>  
   </table>
</body>
</html>
