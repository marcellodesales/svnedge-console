<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <g:sslRedirect/>
    <title><g:layoutTitle default="CollabNet Subversion Edge Setup Wizard" /></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'styles_new.css')}"
          type="text/css"/>
    <link rel="stylesheet" href="${resource(dir:'css',file:'svnedge.css')}"
          type="text/css"/>
    <link rel="shortcut icon"
          href="${resource(dir:'images/icons',file:'favicon.ico')}" />
    <g:layoutHead />
    <g:javascript library="application" />				
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
                border="0" alt=""/></g:link></td>
            </tr>
          </table>
        </td>
        <td align="right" valign="top">
            <table border="0" cellspacing="0" cellpadding="0"
                   class="mastHeadLink logonMenu">
              <tr class="mastHeadLink" valign="top">
                <td valign="middle">
                  <g:isNotLoggedIn>
                    <g:link controller="login">Login
                    </g:link>
                </g:isNotLoggedIn>
                <g:isLoggedIn>
                    Logged in as:&nbsp;
                    <g:loggedInUserInfo field="realUserName"/>&nbsp;
                    (<g:loggedInUsername/>)
                </td>
                <td nowrap="nowrap">
                    <img src="${resource(dir:'images/masthead',
                              file:'vertical_line.gif')}" width="1" height="19"
                         hspace="4" alt=""/>
                </td>
                <td valign="middle">
                    <g:link controller="logout">
                        LOGOUT
                    </g:link>
                </g:isLoggedIn>
                </td>
                <td nowrap="nowrap"><img src="${resource(dir:'images/masthead',
                file:'vertical_line.gif')}" width="1" height="19" hspace="4"
                alt=""/></td>

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

    <!-- SourceForge buttonbar begin -->

    <table class="TopMenu">
      <tr class="ButtonRow">

        <g:set var="buttonList" value="${['welcome', 'setupMaster', 'setupReplica']}" />

        <%-- TODO: read names from l10n props --%>
        <g:set var="buttonNameMap"
               value="${[welcome: 'Introduction', 
		      setupMaster: 'Master Setup',
                      setupReplica: 'Replica Setup']}" />

        <g:set var="buttonIconMap"
               value="${[welcome: 'project-wikiicon.gif',
                      setupMaster: 'project-trackericon.gif',
                      setupReplica: 'project-documentsicon.gif']}"/>

        <td class="VerticalSeparatorNoBorder"></td>
        <g:each in="${buttonList}">
          <g:set var="buttonClass">Button</g:set>
          <g:if test="${actionName == it}">
            <g:set var="buttonClass">Button Selected</g:set>
            <td class="SelectedLeft"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="4" alt=""/></td>
          </g:if>
          <td class="${buttonClass}"
              onclick="window.location='${createLink(controller:'wizard',
                       action: it)}'; return false;"><a
              href="${createLink(controller: it )}" target="_top"><img
              src="${resource(dir:'images/project',file: buttonIconMap[it])}"
              width="25" height="20"
              border="0" alt=""/><br/>${buttonNameMap[it]}</a></td>
          <g:if test="${actionName == it}">
            <td class="SelectedRight"></td>
          </g:if>
          <td class="VerticalSeparator"></td>


        </g:each>
        <td class="ButtonEnd"></td>
      </tr>
      <tr class="ShadowRow">
        <td colspan="2"></td>
        <td class="SelectedLeft"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="4" alt=""/></td>
        <td class="Selected"></td>
        <td class="SelectedRight"></td>
        <td colspan="20"></td>
      </tr>
    </table>
    <!-- SourceForge buttonbar end -->
    
    <!-- SourceForge content-area begin -->
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
                  <td class="CategoryListBody" width="30%">
                    <!-- *************  LEFT NAV STUFF GOES HERE ********** -->
                    <g:pageProperty name="page.leftMenu" />
                  </td>
      
                  <td class="ContainerBodyWithPaddedBorder" width="70%" >
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
          width="102" height="31" alt="Powered by CollabNet" border="0"/></a></div>
        &#169; 2010 CollabNet. CollabNet is a registered trademark of CollabNet,
        Inc.
      </div>
    </div>
    <!-- SourceForge content-area end -->
  </body>
</html>
