<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>CollabNet Subversion Edge</title>
  </head>
  <body>

    <content tag="title">
      <g:message code="status.page.certDetails.label" />
    </content>

  <g:form method="post">
    <div class="dialog">
      <table align="center" width="99%">
        <tbody>
          <tr><td>
            <table class="ItemDetailContainer" border="1">
              <tbody border=1>
                <tr class="prop, OddRow">
                  <td><strong><g:message code="status.page.hostname.label" /></strong></td>
                  <td>${certHostname}</td>
                </tr>
                <tr class="prop, EvenRow">
                  <td><strong><g:message code="status.page.certValidity.label" /></strong></td>
                  <td>${certValidity}</td>
                </tr>
                <tr class="prop, OddRow">
                  <td><strong><g:message code="status.page.certIssuer.label" /></strong></td>
                  <td>${certIssuer}</td>
                </tr>
                <tr class="prop, EvenRow">
                  <td><strong><g:message code="status.page.fingerPrint.label" /></strong></td>
                  <td>${certFingerPrint}</td>
                </tr>
                <tr class="prop, OddRow">
                  <td colspan="2"> 
                  <g:ifAnyGranted role="ROLE_ADMIN,ROLE_ADMIN_SYSTEM">
                    <div class="buttons" style="float: right">
                      <g:actionSubmit action="acceptCertificate" value="${message(code:'status.page.validate.button')}" class="Button"/>
                   </div>
                  </g:ifAnyGranted>
                  <input type="hidden" name="currentlyAcceptedFingerPrint" value="${certFingerPrint}">
                  </td>
                </tr>

              </tbody>
            </table>
          </td></tr>
        </tbody>
      </table>
    </div>
  </g:form>
  </body>
</html>
