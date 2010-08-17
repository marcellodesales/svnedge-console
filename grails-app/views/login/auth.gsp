<head>
  <meta name='layout' content='login' />
  <title>CollabNet Subversion Edge Login</title>
</head>
  <content tag="title">
    Log In
  </content>
<body>
  <%--<g:if test='${flash.message}'>
    <div class='login_message'>${flash.message}</div>
  </g:if>--%>
  <form action='${postUrl}' method='POST' id='loginForm' class='cssform'>
  <table cellpadding="4" width="100%">
    <tbody>
      <tr>
        <td width="10%"><label for='j_username'>User Name</label></td>
        <td>
          <input type='text' class='text_' name='j_username' id='j_username' size="35" value='${request.remoteUser}' />
        </td>
      </tr>
      <tr>
        <td width="10%"><label for='j_password'>Password</label></td>
        <td>
          <input type='password' class='text_' name='j_password' id='j_password' size="35"/>
        </td>
      </tr>
      
    </tbody></table>
    <tr class="ContainerFooter">
   <td colspan="2">
    <div class="AlignRight">
      <input type='submit' value='Login' class="Button"/>
    </div>
   </td>
   </tr>
</form>

   <script type='text/javascript'>
     <!--
         (function(){
         document.forms['loginForm'].elements['j_username'].focus();
         })();
     // -->
   </script>
 </body>
