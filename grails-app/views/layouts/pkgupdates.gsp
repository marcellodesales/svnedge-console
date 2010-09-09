<html>
  <head>
    <g:sslRedirect/>
    <title><g:layoutTitle default="CollabNet Subversion Edge" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'styles_new.css')}"
          type="text/css"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'svnedge.css')}"
          type="text/css"/>
    <link rel="shortcut icon"
          href="${resource(dir:'images/icons',file:'favicon.ico')}" />
    <g:layoutHead />
    <g:javascript library="application" />
  </head>

  <body class="tundra">
    
    <table width="100%" border="0" cellpadding="0" cellspacing="0"
           class="mastHeadBackground">
      <tr>
        <td>
          <table width="300" border="0" cellpadding="0" cellspacing="0"
                 class="mastHeadLink">
            <tr class="sitelogo">
              <td><g:link controller="status"><img
                src="${resource(dir:'images/masthead',file:'CSVN-Logo.png')}"
                border="0" alt="${message(code:'layout.page.home')}"/></g:link></td>
            </tr>
          </table>
        </td>
        <td align="right" valign="top">
            <table border="0" cellspacing="0" cellpadding="0"
                   class="mastHeadLink logonMenu">
              <tr class="mastHeadLink" valign="top">
                <td valign="middle">
                <g:isNotLoggedIn>
                    <g:link controller="login"><g:message code="layout.page.login" />
                    </g:link>
                </g:isNotLoggedIn>
                <g:isLoggedIn>
                    <g:message code="layout.page.loggedAs" />:&nbsp;
                    <g:loggedInUserInfo field="realUserName"/>&nbsp;
~                    (<g:loggedInUsername/>)
                </td>
                <td nowrap="nowrap">
                    <img src="${resource(dir:'images/masthead',
                              file:'vertical_line.gif')}" width="1" height="19"
                         hspace="4" alt=""/>
                </td>
                <td valign="middle">
                    <g:link controller="logout">
                        <g:message code="layout.page.logout" />
                    </g:link>
                </g:isLoggedIn>
                </td>
                <td nowrap="nowrap"><img src="${resource(dir:'images/masthead',
                file:'vertical_line.gif')}"
                width="1" height="19" hspace="4" alt=""/></td>

                <td nowrap="nowrap">&nbsp;&nbsp;&nbsp;<g:render template="/common/helpLink" model="['type' : 'img']"/></td>

                <td nowrap="nowrap" valign="middle">&nbsp;<g:render template="/common/helpLink" model="['type' : 'text']"/>&nbsp;&nbsp;</td>

              </tr>
            </table>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="black"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="1" alt=""/></td>
      </tr>
    </table>

    <div class="sectiontitle">&nbsp;</div>
    
    <!-- TeamForge content-area begin -->
    <div class="contentArea">
      <div class="sessionmessages"> 
            <g:if test="${flash.message}">
                <div class="greenText">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="errorMessage">${flash.error}</div>
            </g:if>
      </div>

      <div id="main">

        <table class="Container" id="tracker_summary_table_id">
          <!-- content header -->
          <tr class="ContainerHeader">
            <td>
              <!-- *****************  PAGE TITLE GOES HERE ************** -->
              <g:pageProperty name="page.title" />
            </td>
          </tr>
          <tr>
            <td class="ContainerBody">
              <table class="CategoryListTable">
                <tr>
                  <td class="ContainerBodyWithPaddedBorder" width="80%" >
                    <g:layoutBody />    
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
        &#169; 2010 CollabNet. CollabNet <g:message code="layout.page.trademark" /> CollabNet, Inc.
      </div>
    </div>
    <!-- TeamForge content-area end -->
  </body>
</html>
