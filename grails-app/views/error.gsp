<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <g:sslRedirect/>
    <title>CollabNet Subversion Edge: Error</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" href="${resource(dir:'css',file:'styles_new.css')}"
          type="text/css"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'svnedge.css')}"
          type="text/css"/>
    <link rel="shortcut icon"
          href="${resource(dir:'images/icons',file:'favicon.ico')}" />
    <g:javascript library="application" />                

    <style type="text/css">
      .message {
        border: 1px solid black;
        padding: 5px;
        background-color:#E9E9E9;
      }
      .stack {
        border: 1px solid black;
        padding: 5px;
        overflow:auto;
        height: 300px;
      }
      .snippet {
        padding: 5px;
        background-color:white;
        border:1px solid black;
        margin:3px;
        font-family:courier;
      }
    </style>
  </head>

  <body ${pageProperty(name: 'body.onload', writeEntireProperty: true)}${pageProperty(name: 'body.onunload', writeEntireProperty: true)}>
    <table width="100%" border="0" cellpadding="0" cellspacing="0"
           class="mastHeadBackground">
      <tr>
        <td>
          <table width="300" border="0" cellpadding="0" cellspacing="0"
                 class="mastHeadLink">
            <tr class="sitelogo">
              <td><g:link controller="status"><img 
                 src="${resource(dir:'images/masthead',file:'CSVN-Logo.png')}"
                 border="0" alt="${message(code:'layout.page.home') }"/></g:link></td>
            </tr>
          </table>
        </td>
        <td align="right" valign="top">
        </td>
      </tr>
      <tr>
        <td colspan="2" class="black"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="1" alt=""/></td>
      </tr>
    </table>

    <div class="sectiontitle">&nbsp;</div>
    
      <!-- TeamForge buttonbar begin -->
      <table class="TopMenu">
      <tr class="ButtonRow">
          <td class="VerticalSeparatorNoBorder"></td>
          <td class="Button"
              onclick="window.location='${createLink(controller: 'status')}'; return false;"><a
              href="${createLink(controller: 'status')}" target="_top"><img
              src="${resource(dir:'images/project', file: 'project-homeicon.gif')}"
              width="25" height="20"
              border="0" alt=""/><br/><g:message code="status.main.icon"/></a></td>
          <td class="VerticalSeparator"></td>
                  <td class="ButtonEnd"></td>
          
      </tr>
      <tr class="ShadowRow">
        <td colspan="2"></td>
        <td class="SelectedLeft"></td>
        <td class="Selected"></td>
        <td class="SelectedRight"></td>
        <td colspan="20"></td>
      </tr>
    </table>
    <!-- TeamForge buttonbar end -->
    
    <!-- TeamForge content-area begin -->
    <div class="contentArea">
      <div class="sessionmessages"> 
         <div class="errorMessage"><g:message code="error.alertMessage"/></div>
      </div>

      <div id="main">
        <table class="Container" id="tracker_summary_table_id">
          <!-- content header -->
          <tr class="ContainerHeader">
            <td>
              <!-- *****************  PAGE TITLE GOES HERE ************** -->
              Error
            </td>
          </tr>
          <tr>
            <td class="ContainerBody">
              <table class="CategoryListTable">
                <tr>
                  <td class="ContainerBodyWithPaddedBorder" width="80%" >
                    <!-- More content -->
                    <p><g:message code="error.contactAdmin"/></p> 
                    <p><g:message code="error.submitErrorReport" args="${['https://ctf.open.collab.net/sf/discussion/do/listTopics/projects.svnedge/discussion.user_questions', 
                        exception?.message?.replace('?', 'QMark').replace('&', 'AND').encodeAsHTML().replace('&', '_')]}"/>
                    <br />
                    <!-- \" , '${}'      -->
                    
                    <g:message code="error.showDetails" args="${['setDisplayMode(\'errorDetails\', \'block\')']}"/> 
                    <a href="#" onclick="setDisplayMode('errorDetails', 'block')">[ <g:message code="error.showDetailsLink"/> ]</a>
                    </p>
                    <div id="errorDetails" style="display: none;">
                        <a style="float: right" href="#" onclick="setDisplayMode('errorDetails', 'none')"><small>[ <g:message code="error.hideDetails"/> ]</small></a><h2>Details</h2>
    <div class="message">
      <strong>Error ${request.'javax.servlet.error.status_code'}:</strong>
      ${request.'javax.servlet.error.message'}<br/>
      <strong>Servlet:</strong>
      ${request.'javax.servlet.error.servlet_name'}<br/>
      <strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
      <g:if test="${exception}">
        <input type="hidden" name="ExceptionMessage" value=""/>
        <strong>Exception Message:</strong>
        ${exception.message} <br />
        <strong>Caused by:</strong>
        ${exception.cause?.message} <br />
        <strong>Class:</strong> ${exception.className} <br />
        <strong>At Line:</strong> [${exception.lineNumber}] <br />
        <g:if test="${exception.codeSnippet}">
        <strong>Code Snippet:</strong><br />
        <div class="snippet">
          <g:each var="cs" in="${exception.codeSnippet}">
            ${cs}<br />
          </g:each>
        </div>
        </g:if>
      </g:if>
    </div>
    <g:if test="${exception}">
     <h2>Stack Trace</h2>
      <div class="stack">
      <pre>
<g:each in="${exception.stackTraceLines}">${it}</g:each>
      </pre>
      </div>
    </g:if>
                    </div>
                    <!-- More content end -->
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </div>
      <!-- main content section end -->

      <br/><br/>
      <div id="footer">
        <div id="poweredbylogo"><a href="http://www.collab.net/?cid=csvnedgeL" target="collabnet">
        <img src="${resource(dir:'images/about',file:'poweredbylogo.gif')}"
        width="102" height="31" alt="${message(code:'layout.page.poweredBy') }" border="0"/></a></div>
        &#169; 2011 <g:message code="layout.page.trademark" />
      </div>
    </div>
    <!-- TeamForge content-area end -->
<script type="text/javascript">
function setDisplayMode(id, mode) {
  document.getElementById(id).style.display = mode; 
  return false;
}
</script>
  </body>
</html>