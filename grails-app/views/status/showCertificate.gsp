<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge</title>
  </head>
  <body>

    <content tag="title">
      <g:message code="status.page.showCertificate.label" />
    </content>

  <g:form method="post">
  
  <p><g:message code="showCertificate.page.p1" /></p>
  <code>
  ${svnCommand}
  </code>
  <br />
  <br />
  <p><g:message code="showCertificate.page.p2" /></p>
  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
    <g:actionSubmit action="index" value="${message(code:'showCertificate.page.validateCertViaSvn.button')}" class="btn btn-primary"/>
  </g:ifAnyGranted>
  <br />
  <br />
  <br />
  <p><g:message code="showCertificate.page.alternate" /></p>
  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
    <g:actionSubmit action="acceptCertificate" value="${message(code:'showCertificate.page.skipValidation.button')}" class="btn"/>
  </g:ifAnyGranted>
      
  </g:form>
  </body>
</html>
