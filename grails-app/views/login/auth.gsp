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
        <h2><g:message code="login.page.auth.title" /></h2>
        <form action='${postUrl}' method='post' id='loginForm'>
          <fieldset>
            <div class="clearfix">
              <input type="text" name="j_username" id="j_username" placeholder="<g:message code="login.page.auth.username.label" />" />
            </div>
            <div class="clearfix">
              <input type="password" name="j_password" id="j_password" placeholder="<g:message code="login.page.auth.password.label" />" />
            </div>
            <button class="btn btn-primary" type="submit"><g:message code="login.page.auth.button.submit" /></button>
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
