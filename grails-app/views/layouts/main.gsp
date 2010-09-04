<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <g:sslRedirect/>
    <title><g:layoutTitle default="CollabNet Subversion Edge" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
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
                 border="0" alt="Home"/></g:link></td>
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
                    <g:link controller="user" action="showSelf">
                    <g:loggedInUserInfo field="realUserName"/>&nbsp;(<g:loggedInUsername/>)
                    </g:link>
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

    <!-- TeamForge buttonbar begin -->
      <table class="TopMenu">
      <tr class="ButtonRow">
        <%--
            Algorithm:
            set buttonMap (controllerName: buttonName)
            set iconMap (controllerName: iconUrl)
            set ordered buttonList (controllerName)
            for button in buttonList:
                set buttonClass = "Button"
                if ${controllerName} == button:
                    add SelectedLeft
                    set buttonClass = "Button Selected"
                add button (taking name from buttonMap, 
                         icon url from iconMap,
                         use buttonClass)
                if ${controllerName} == button:
                    add SelectedRight


             Note that "featureList" is a set of tokens
                in the page model identifying which buttons to show.
                See ApplicationFilters.groovy for the filter
                that creates the active feature list

        --%>

        <g:set var="controllerButtonMap"
               value="${[status: 'status',
                      repo: 'repo',
                      user: 'user',
                      role: 'user',
                      userCache: 'userCache',
                      statistics: 'statistics',
                      admin: 'admin',
                      server: 'admin',
                      log: 'admin',
                      packagesUpdate: 'admin',
                      setupTeamForge: 'admin',
                      ocn: 'ocn'
                      ]}" />

        <g:set var="buttonNameMap"
               value="${[status: message(code:'status.main.icon'),
                      repo: message(code:'repository.main.icon'),
                      user: message(code:'user.main.icon'),
                      userCache: message(code:'userCache.main.icon'),
                      statistics: message(code:'statistics.main.icon'),
                      admin: message(code:'server.main.icon'),
                      ocn: 'openCollabNet'
                      ]}" />

        <g:set var="buttonIconMap"
               value="${[status: 'project-homeicon.gif',
                      repo: 'project-scmicon.gif',
                      user: 'project-adminusers.gif',
                      userCache: 'project-tasksicon.gif',
                      statistics: 'project-reportsicon.gif',
                      admin: 'project-projectadminicon.gif',
                      ocn: 'project-ocnicon.gif']}"/>

        <td class="VerticalSeparatorNoBorder"></td>
        %{-- activate buttons in this order: 'activeButton' property in model, controllerName, or default (status)--}%
        <g:set var="selectedButton">status</g:set>
        <g:if test="${controllerButtonMap[activeButton]}">
            <g:set var="selectedButton"
                value="${controllerButtonMap[activeButton]}" />
        </g:if>
        <g:elseif test="${controllerButtonMap[controllerName]}">
            <g:set var="selectedButton" 
                value="${controllerButtonMap[controllerName]}" />
        </g:elseif>
        <g:each in="${featureList}">
          <g:set var="buttonClass">Button</g:set>
          <g:set var="isButtonSelected" 
              value="${(controllerButtonMap[controllerName] == controllerButtonMap[it]) || 
              (it == 'status' && selectedButton == 'status')}" />
          <g:if test="${isButtonSelected}">
            <g:set var="buttonClass">Button Selected</g:set>
            <td class="SelectedLeft"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="4" alt=""/></td>
          </g:if>

          <td class="${buttonClass}"
              onclick="window.location='${createLink(controller: it)}'; return false;"><a
              href="${createLink(controller: it )}" target="_top"><img
              src="${resource(dir:'images/project',file: buttonIconMap[controllerButtonMap[it]])}"
              width="25" height="20"
              border="0" alt=""/><br/>${buttonNameMap[controllerButtonMap[it]]}</a></td>
          <g:if test="${isButtonSelected}">
            <td class="SelectedRight"><img
                 src="${resource(dir:'images/misc',file:'pixel.gif')}"
                 border="0" height="1" width="4" alt=""/></td>
          </g:if>
          <td class="VerticalSeparator"></td>

        </g:each>
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
            <g:if test="${flash.message}">
                <div class="greenText">${flash.message}</div>
            </g:if>
            <g:if test="${flash.warn}">
                <div class="warningText">${flash.warn}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="errorMessage">${flash.error}</div>
            </g:if>
      </div>
      <div class="requestmessages"> 
            <g:if test="${request['message']}">
                <div class="greenText">${request['message']}</div>
            </g:if>
            <g:if test="${request['warn']}">
                <div class="warningText">${request['warn']}</div>
            </g:if>
            <g:if test="${request['error']}">
                <div class="errorMessage">${request['error']}</div>
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
                  
                  <g:if test="${pageProperty(name:'page.leftMenu')}">
                    <td class="CategoryListBody" width="10%">                         
                      <!-- *************  LEFT NAV STUFF GOES HERE *********** -->
                      <g:pageProperty name="page.leftMenu" />
                    </td>
                  </g:if>
      
                  <td class="ContainerBodyWithPaddedBorder" width="90%" >
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
        &#169; 2010 CollabNet. CollabNet is a registered trademark of CollabNet, Inc.
      </div>
    </div>
    <!-- TeamForge content-area end -->
  </body>
</html>
