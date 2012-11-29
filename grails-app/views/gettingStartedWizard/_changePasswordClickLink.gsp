<p><g:message code="wizard.GettingStarted.ChangePassword.recommend"/></p>
<p><g:message code="wizard.GettingStarted.ChangePassword.clickUsernameToEdit"/></p>
<g:javascript>
  function highlightUsername() {
    var userLink = $('#loggedInUser :visible');
    if (userLink.length > 0) {
      var options = { trigger: 'manual', placement: 'bottom'};
      options.title = '<g:message code="wizard.GettingStarted.ChangePassword.clickHere"/>';
      userLink.tooltip(options);
      setInterval(function() { if ($('.btn-navbar').is(':hidden')) userLink.tooltip('toggle'); }, 1500);
    }
  }
  
  $(document).ready(highlightUsername);
</g:javascript>

