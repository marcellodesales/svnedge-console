<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>Replication Status</title>
    <meta name="layout" content="main" />
  </head>
  <body>
    <h1>Replication Status</h1>

    <g:if test="${masterTimestamp}">
      <div class="message">
        The notifcations returned from the master:
        <ul>
          <li>Master timestamp: ${masterTimestamp}</li>
        </ul>
      </div>
    </g:if>
    <g:else>
      <div class="warning">
        No notifications returned from the master.
      </div>
    </g:else>

  </body>
</html>
