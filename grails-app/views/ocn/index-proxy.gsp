<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge <g:message code="ocn.page.proxy.title" /></title>
  </head>
  <body>

    <iframe id="ocnContent" name="ocnContent" frameborder="0" width="100%" height="780"
        <g:ifAnyGranted role='ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_SYSTEM,ROLE_ADMIN_USERS'>      
            src="http://tab.open.collab.net/nonav/csvn.html">
        </g:ifAnyGranted>
        <g:ifNotGranted role='ROLE_ADMIN,ROLE_ADMIN_REPO,ROLE_ADMIN_SYSTEM,ROLE_ADMIN_USERS'>      
            src="http://tab.open.collab.net/nonav/csvn.html">
        </g:ifNotGranted>
        <p><g:message code="ocn.page.proxy.iframe.error" /></p>
    </iframe>
  </body>
</html>
