<p><g:message code="wizard.GettingStarted.ChangePassword.recommend"/></p>
<p><g:message code="wizard.GettingStarted.ChangePassword.clickUsernameToEdit"/></p>
<g:javascript>
  function highlightUsername() {
    var options = { trigger: 'manual', placement: 'bottom'};
    options.title = '<g:message code="wizard.GettingStarted.ChangePassword.clickHere"/>';
    var userLinks = $('#loggedInUser, .short-user-menu');
    userLinks.tooltip(options);
    if (userLinks.length > 0) {
      setInterval(function() {
        var userLink = $('#loggedInUser');
        if (userLink.is(':visible')) {
          $('.short-user-menu').tooltip('hide');
          userLink.tooltip('toggle');
        } else {
          $('#loggedInUser').tooltip('hide');
          $('.short-user-menu').tooltip('toggle');
        } 
      }, 1500);
    }
  }
  
  $(document).ready(highlightUsername);
</g:javascript>

