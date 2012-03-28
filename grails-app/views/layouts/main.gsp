<!DOCTYPE html>
<html lang="en">
  <head>
    <g:sslRedirect/>

    <g:if test="${pageProperty(name:'page.title')}">
        <g:set var="pageHeader"><g:pageProperty name="page.title" /></g:set>
    </g:if>    
    <title><g:layoutTitle default="CollabNet Subversion Edge ${pageHeader ?: 'Console'}" /></title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <meta name="description" content="Subversion Edge"/>
    <meta name="author" content="CollabNet"/>

    <link href="${resource(dir:'css',file:'bootstrap.css')}" rel="stylesheet"/>
    <style type="text/css">
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
        padding-bottom: 40px;
      }

      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
    <link href="${resource(dir:'css',file:'svnedge-3.0.0.css')}" rel="stylesheet"/>                                                                                                                                                                  
    <link href="${resource(dir:'css',file:'bootstrap-responsive.css')}" rel="stylesheet"/>
 
    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
      
    <link rel="shortcut icon" href="${resource(dir:'images/icons',file:'favicon.ico')}" />
          
    <!-- jquery lib is often needed before page html is rendered -->
    <g:javascript library="jquery-1.7.1.min"/>
    <g:javascript library="application-3.0.0" />
    <g:layoutHead />

  </head>
  <body ${pageProperty(name: 'body.onload', writeEntireProperty: true)}${pageProperty(name: 'body.onunload', writeEntireProperty: true)}>
    
    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container-fluid">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse"> 
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <g:link controller="status" class="brand"><img
               class="hidden-phone"
               src="${resource(dir:'images/masthead',file:'logo.png')}"
               alt="${message(code:'layout.page.home') }"/><img
               class="visible-phone"
               src="${resource(dir:'images/masthead',file:'small-logo.png')}"
               alt="${message(code:'layout.page.home') }"/></g:link>
          <div class="nav-collapse">
            <!-- buttons -->
            <ul class="nav">
            
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
               value="${[status: 'admin',
                      repo: 'repo',
                      repoTemplate: 'repo',
                      user: 'user',
                      role: 'user',
                      job: 'admin',
                      userCache: 'userCache',
                      statistics: 'admin',
                      admin: 'admin',
                      server: 'admin',
                      log: 'admin',
                      packagesUpdate: 'admin',
                      setupTeamForge: 'admin',
                      setupReplica: 'admin',
                      setupCloudServices: 'admin',
                      ocn: 'ocn'
                      ]}" />

        <g:set var="buttonNameMap"
               value="${[status: message(code:'status.main.icon'),
                      repo: message(code:'repository.main.icon'),
                      job: message(code:'job.main.icon'),
                      user: message(code:'user.main.icon'),
                      userCache: message(code:'userCache.main.icon'),
                      statistics: message(code:'statistics.main.icon'),
                      admin: message(code:'server.main.icon'),
                      ocn: message(code:'ocn.main.icon')
                      ]}" />

        <%-- activate buttons in this order: 'activeButton' property in model, controllerName, or default (status) --%>
        <g:set var="selectedButton">admin</g:set>
        <g:if test="${controllerButtonMap[activeButton]}">
            <g:set var="selectedButton"
                value="${controllerButtonMap[activeButton]}" />
        </g:if>
        <g:elseif test="${controllerButtonMap[controllerName]}">
            <g:set var="selectedButton" 
                value="${controllerButtonMap[controllerName]}" />
        </g:elseif>
        
        <g:if test="${!hideButtons}">
        <g:each in="${featureList}">
          <g:set var="isButtonSelected" 
              value="${(controllerButtonMap[controllerName] == controllerButtonMap[it]) || 
              (it == 'status' && selectedButton == 'status')}" />
          <li<g:if test="${isButtonSelected}"> class="active"</g:if>><a href="${createLink(controller: it )}" 
              target="_top">${buttonNameMap[controllerButtonMap[it]]}</a></li>
        </g:each>
        </g:if>
        
            </ul>
            <!-- buttons end -->
            <ul class="nav pull-right">
                <g:isNotLoggedIn>
                  <li><g:link controller="login"><g:message code="layout.page.login" /></g:link>
                </g:isNotLoggedIn>
                <g:isLoggedIn>
                    <li id="loggedInUser">
                    <g:link controller="user" action="showSelf">
                    <g:loggedInUserInfo field="realUserName"/>&nbsp;(<g:loggedInUsername/>)
                    </g:link>
                </li>
                <li class="divider-vertical"></li>
                <li><g:link controller="logout"><g:message code="layout.page.logout"/></g:link>
                </g:isLoggedIn>
              </li>
              <li class="divider-vertical"></li>
              <g:render template="/layouts/helpLink"/>
            </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
    <g:render template="/layouts/aboutModal"/>
    
    <!-- main content section begin -->
    <div class="container-fluid">    
      <div class="sessionmessages" id="sessionmessages">
        <!-- 
          <div class="alert alert-block">
          <h4 class="alert-heading">Updates Available</h4>
          There are new updates available for <a href="updates.html">download</a>.
          </div>
        -->
            <g:if test="${flash.message}">
                <div class="alert alert-success">${flash.message}</div>
            </g:if>
            <g:elseif test="${flash.unfiltered_message}">
                <div class="alert alert-success"><%=flash.unfiltered_message%></div>
            </g:elseif>
            <g:if test="${flash.warn}">
                <div class="alert">${flash.warn}</div>
            </g:if>
            <g:elseif test="${flash.unfiltered_warn}">
                <div class="alert"><%=flash.unfiltered_warn%></div>
            </g:elseif>
            <g:if test="${flash.error}">
                <div class="alert alert-error">${flash.error}</div>
            </g:if>
            <g:elseif test="${flash.unfiltered_error}">
                <div class="alert alert-error"><%=flash.unfiltered_error%></div>
            </g:elseif>
      </div>
      <div class="requestmessages" id="requestmessages"> 
            <g:if test="${request['message']}">
                <div class="alert alert-success">${request['message']}</div>
            </g:if>
            <g:elseif test="${request['unfiltered_message']}">
                <div class="alert alert-success"><%=request['unfiltered_message']%></div>
            </g:elseif>
            <g:if test="${request['warn']}">
                <div class="alert">${request['warn']}</div>
            </g:if>
            <g:elseif test="${request['unfiltered_warn']}">
                <div class="alert"><%=request['unfiltered_warn']%></div>
            </g:elseif>
            <g:if test="${request['error']}">
                <div class="alert alert-error">${request['error']}</div>
            </g:if>
            <g:elseif test="${request['unfiltered_error']}">
                <div class="alert alert-error"><%=request['unfiltered_error']%></div>
            </g:elseif>
      </div>
    </div> <!-- /container-fluid -->
 
    <div id="main" class="container-fluid">
      <div class="row-fluid">

        <g:set var="blocks" value="12"/>
        <g:if test="${pageProperty(name:'page.leftMenu')}">
          <g:set var="blocks" value="9"/>
          <!-- *************  LEFT NAV STUFF GOES HERE *********** -->
          <div class="span3">
            <div class="well sidebar-nav">
              <ul class="nav nav-list">
                <g:pageProperty name="page.leftMenu" />
              </ul>
            </div> <!--/.well -->

            <g:tipSelector>
            <div class="well hidden-phone">
              <span class="label label-info">Tip:</span>
              <%=tip%>
            </div> <!--/.well -->
            </g:tipSelector>

          </div> <!--/span3-->
        </g:if>
        <div class="span${blocks}">
          <g:if test="${pageHeader}">
            <div class="page-header"><h1>${pageHeader}</h1></div>
          </g:if>

          <div id="pageContent">
            <g:layoutBody />
          </div>
        </div><!-- /spanX -->
      </div><!-- /row-fluid -->
    </div><!-- /container-fluid #main-->
    <!-- main content section end -->
    <!-- ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <g:javascript library="jquery-tablesorter"/>
    <g:javascript library="bootstrap"/>
    <g:javascript library="load-image.min"/>
    <g:javascript library="bootstrap-image-gallery.min"/>
    <g:pageProperty name="page.bottomOfBody" />
  </body>
</html>
