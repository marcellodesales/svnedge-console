<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>CollabNet Subversion Edge <g:message code="logs.page.show.title" args="${[params.fileName]}" encodeAs="HTML"/></title>
  <g:javascript library="application" />
  <g:javascript>


    $(function() {
        $('#tailButton').on('click', toggleLogStreaming)
        $('#tailSpinner').hide()

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
          logStreamer  = new LogStreamer('${params.fileName}', '${fileSizeBytes}', $('#fileContent'), $('#fileContentDiv'), "\n\n** <g:message code="logs.page.show.tail.error"/> **\n")
          logStreamer.start();
          $('#tailButton').text("<g:message code="logs.page.show.button.tailStop"/>");
          $('#tailSpinner').show()
      }
      else {
          logStreamer.stop();
          $('#tailButton').text("<g:message code="logs.page.show.button.tail"/>");
          $('#tailSpinner').hide()
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

<div id="fileName">
  <g:message code="logs.page.show.header.fileName" /> ${params.fileName} &nbsp;<g:message code="logs.page.show.header.size" /> ${fileSize} &nbsp;<g:message code="logs.page.show.header.lastModification" /> ${fileModification}</td>
</div>

<g:if test="${file}">
  <div style="overflow: auto;" id="fileContentDiv">
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

</g:if>
<g:else>
  <g:message code="logs.page.show.header.fileNotFound" args="${[params.fileName]}" encodeAs="HTML"/>

</g:else>

<div class="pull-right">
  <img id="tailSpinner" class="spinner" src="/csvn/images/spinner-gray-bg.gif" alt="Tailing.."/>
  <g:link id="tailButton" url="#" class="btn" onclick="return false"><g:message code="logs.page.show.button.tail" /></g:link>
  <g:link target="_blank" action="show" params="[fileName : file.name, view : 'raw']" class="btn"><g:message code="logs.page.show.button.viewRaw" /> &#133;</g:link>
  <g:link action="list" class="btn"><g:message code="logs.page.show.button.return" /></g:link>
</div>

<g:javascript>
  function resizeFileViewer() {
    $("#fileContentDiv").css('height', '' + Math.round(.8 * $(window).height()));
  }

  $(document).ready(function() {
    resizeFileViewer();
    $(window).bind('resize', resizeFileViewer);
  });
</g:javascript>


</body>
</html>
