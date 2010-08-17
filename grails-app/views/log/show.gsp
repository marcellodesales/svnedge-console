<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="main"/>
  <title>Log File View</title>
</head>

<content tag="title">
    Administration
</content>

<g:render template="/server/leftNav" />

<body>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
 <table class="Container">
  <tbody>
    <tr class="ContainerHeader">
      <td>Server Logs: ${params.fileName}</td>    
    </tr>
    <g:if test="${file}">  
    <tr>
      <td>
        <!-- This is still in progress, need to look for grails ui component that does this gracefully  -->
        <div style="width: 800px; height: 300px; overflow: auto;">
<!-- Leave this left-justified so that spaces are not padded in the first line of the log -->        
<pre>
<% file.withReader { out << it } %>           
</pre>
        </div>
      </td>    
    </tr>
    </g:if>
  <g:else>
    <tr class="ItemListNoData">
        <td colspan="3">No results found.</td>
      </tr>
  </g:else>
   
  <tr class="ContainerFooter">
        <td >
          <div class="AlignRight">
              <div class="Button"><div class="Middle">                
                <g:link target="_blank" action="show" params="[fileName : file.name, rawView : true]" class="Button">View Raw Log File&#133;</g:link>
              </div></div>
              <div class="Button"><div class="Middle">
                <g:link action="list" class="Button">Return</g:link>
              </div></div>
            </div>
          </div>
        </td>
      </tr>
   </tbody>  
   </table>
</body>
</html>
