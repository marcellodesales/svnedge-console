<head>
  <meta name='layout' content='login' />
  <title>CollabNet Subversion Edge <g:message code="login.page.auth.title" /></title>
</head>
  <content tag="title">
    <g:message code="login.page.auth.title" />
  </content>
<body>

  <div class="content">
    <div class="row">
      <div class="login-form">
        <form action='${postUrl}' method='post' id='loginForm'>
          <fieldset>
            <div class="clearfix">
              <label class="control-label"><g:message code="login.page.auth.username.label"/>
              <input type="text" name="j_username" id="j_username"/></label>
            </div>
            <div class="clearfix">
              <label class="control-label"><g:message code="login.page.auth.password.label"/>
              <input type="password" name="j_password" id="j_password"/>
            </div>
            <input class="btn btn-primary" value="<g:message code="login.page.auth.button.submit" />" type="submit"></input>
          </fieldset>
        </form>
      </div>
    </div>
  </div>
  <script type="text/javascript">
     <!--
         (function(){
         document.forms['loginForm'].elements['j_username'].focus();
         })();
     // -->
   </script>
 </body>
