<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.CommandState"%>
<%@page import="com.collabnet.svnedge.integration.command.AbstractCommand"%>
<head>
    <meta name="layout" content="main" />
    <title><g:message code="job.page.list.title"/></title>

      <script type="text/javascript" src="/csvn/plugins/cometd-0.1.5/dojo/dojo.js"
                djconfig="parseOnLoad: true, isDebug: false"></script>

      <g:set var="no_commands" value="${message(code:'status.page.status.replication.no_commands')}" />
      <g:set var="commands_running" value="${message(code:'status.page.status.replication.commands_running')}" />

      <script type="text/javascript">
      // http://rick.measham.id.au/javascript/hash.htm

	      function Hash(){
	          for( var i=0; i < arguments.length; i++ )
	              for( n in arguments[i] )
	                  if( arguments[i].hasOwnProperty(n) )
	                      this[n] = arguments[i][n];
	      }
	      Hash.prototype = new Object();
	
	      Hash.prototype.keys = function(){
	          var rv = [];
	          for( var n in this )
	              if( this.hasOwnProperty(n) )
	                  rv.push(n);
	          return rv;
	      }
	
	      Hash.prototype.containsKey = function() {
	          if (arguments[0] == null) {
	              return false
	          }
	          for( var n in this )
	              if( this.hasOwnProperty(n) ) {
	                  if (n == arguments[0])
	                      return true
	              }
	          return false
	      }
	
	      Hash.prototype.length = function(){
	          return this.keys().length();
	      }
	
	      Hash.prototype.values = function(){
	          var rv = [];
	          for( var n in this )
	              if( this.hasOwnProperty(n) )
	                  rv.push(this[n]);
	          return rv;
	      }
	
	      Hash.prototype.slice = function(){
	          var rv = [];
	          for( var i = 0; i < arguments.length; i++ )
	              rv.push(
	                  ( this.hasOwnProperty( arguments[i] ) )
	                      ? this[arguments[i]]
	                      : undefined
	              );
	          return rv;
	      }
	
	      Hash.prototype.concat = function(){
	          for( var i = 0; i < arguments.length; i++ )
	              for( var n in arguments[i] )
	                  if( arguments[i].hasOwnProperty(n) )
	                      this[n] = arguments[i][n];
	          return this;
        }

        var passedResults = new Hash()

        /**
         * Author: Marcello de Sales (mdesales@collab.net)
         * Date: May 18, 2011
         */
        dojo.require('dojox.cometd')
        dojo.require("dijit.Dialog")

        /** The counter cometd channel. */
        var statusCounterChannel = "/replica/status/counter"
        /** The counter cometd channel. */
        var commandsStateChannel = "/replica/status/all"

        /** The current number of commands at the time the page loads. */
        var currentNumberCommands = ${totalCommandsRunning}

        var existingCommandIds = new Array()

        var idleString = "${message(code: 'job.page.list.row.job_idle')}"

        function makeCommandStatusIcon(element, status) {
            var img = new Image()
            img.src = "/csvn/images/replica/command_" + status + ".png"
            img.border = "0"
            element.appendChild(img)
        }

        function makeCommandIcon(commandCode) {
            var img = new Image()
            img.src = "/csvn/images/replica/" + commandCode + ".png"
            img.border = "0"
            return img
        }

        function appendCommandIcon(col, command) {
            var cmdIconImage = makeCommandIcon(command.code)
            col.appendChild(cmdIconImage)
        }

        function updateScheduledCommands(command) {
            var tableId = "scheduledCommandsTable"
            var table = dojo.byId(tableId)
            // move all commands one line down.
            for (i = table.rows.length - 2; i > 2 ; i--) {
                table.rows[i + 1] = table.rows[i]
            }

            var row = table.insertRow(1);
            row.setAttribute('id', "sch_" + command.id)
            // inverting the color
            if ((table.rows.length-1) % 2 == 0) {
                row.setAttribute('class', "EvenRow")
            } else {
                row.setAttribute('class', "OddRow")
            }

            var col = row.insertCell(0)
            col.appendChild(document.createTextNode("\u00a0\u00a0"))
            appendCommandIcon(col, command)
            col.appendChild(document.createTextNode(" "))
            col.appendChild(document.createTextNode(command.id))
        }

        /**
         * Removes a command from the scheduled table.
         */
        function removeFromScheduled(command) {
            var row = dojo.byId("sch_" + command.id)
            if (row != null) {
                row.parentNode.removeChild(row)
            }
        }

        /**
         * Adding a new command row to the existing table.
         */
        function updateRunningCommands(tableId, command) {
            var table = dojo.byId(tableId)
            // move all commands one line down. Last command disappear.
            var bottomPosition = table.rows.length - 2 // table header and columns description

            for (i = bottomPosition; i > 1 ; i--) {
                var currentRow = table.rows[i]
                var nextRow = table.rows[i + 1]
                if (currentRow.getAttribute("id") == null && nextRow.getAttribute("id") == null) {
                    if (i == 1) {
                        // First command to be added.
                        var rowClass = currentRow.getAttribute("class")
                        var rowIndex = currentRow.cells[0].innerHTML
                        table.deleteRow(i) // currentRow
                        var newRow = table.insertRow(i)
                        newRow.setAttribute('class', rowClass)

                        var indexCol = newRow.insertCell(0)
                        indexCol.innerHTML = rowIndex

                        nextRow.insertCell(1)
                        nextRow.insertCell(2)
                        nextRow.insertCell(3)
                        nextRow.insertCell(4)
                        continue

                    } else {
                        continue
                    }

                } else if (currentRow.getAttribute("id") != null && nextRow.getAttribute("id") == null) {
                    // remove the idle row and place a new one, keeping its formatting.
                    var rowClass = nextRow.getAttribute("class")
                    var rowIndex = nextRow.cells[0].innerHTML
                    table.deleteRow(i+1) // nextRow
                    nextRow = table.insertRow(i+1)
                    nextRow.setAttribute('class', rowClass)

                    var indexCol = nextRow.insertCell(0)
                    indexCol.innerHTML = rowIndex

                    nextRow.insertCell(1)
                    nextRow.insertCell(2)
                    nextRow.insertCell(3)
                    nextRow.insertCell(4)

                }
                // move all the cells/columns from current to next
                nextRow.setAttribute("id", currentRow.getAttribute("id"))
                for (j = 1; j < table.rows[i].cells.length; j++) {
                    nextRow.cells[j].innerHTML = currentRow.cells[j].innerHTML
                }
            }

            var row = table.rows[2]
            row.setAttribute("id", "run_" + command.id)

            row.cells[1].innerHTML = "<a target='" + command.id + "' href='/csvn/log/show?fileName=/temp/" + command.id + ".log&view=tail'>" + command.id + "</a>"

            var codeCol = row.cells[2]
            codeCol.innerHTML = ""
            appendCommandIcon(codeCol, command)
            codeCol.appendChild(document.createTextNode(" "))
            if (command.params.repoName != null) {
                codeCol.appendChild(document.createTextNode(command.params.repoName))
                row.cells[3].innerHTML = "-"

            } else {
                codeCol.appendChild(document.createTextNode(command.code))
                row.cells[3].innerHTML = command.params
            }
            row.cells[4].innerHTML = command.startedAt
        }

        /**
         * If there are commands running, then print the number and 
         */
        function updateUiCommandsRunning(numberOfCommands, spinner) {
            if (numberOfCommands > 0) {
                dojo.byId(spinner).style.display = ''
                dojo.byId('commandsCount').innerHTML = numberOfCommands + " ${no_commands}"

            } else {
                dojo.byId(spinner).style.display = 'none'
                dojo.byId('commandsCount').innerHTML = "${commands_running}"
            }
        }

        /**
         * Highlight the terminated command in green or red in case it passed or failed,
         * respectively, but not removing it until it has been reported.
         */
        function highlightTerminatedCommand(command) {
            if (command.state == "TERMINATED") {
                var row = dojo.byId("run_" + command.id)
                if (row == null) {
                    return
                }
                if (command.succeeded == "true") {
                    row.style.backgroundColor = "#99D6AD"

                } else if (command.succeeded == "false") {
                    row.style.backgroundColor = "#FFB2B2"
                }
                passedResults["run_" + command.id] = command.succeeded
            }
        }

        /**
         * Creates a new idle row in the given index position with the index value.
         */
        function makeIdleRow(table, index, value, rowClass) {
            var idleRow = table.insertRow(index)
            idleRow.setAttribute('class', rowClass)

            var indexCell = idleRow.insertCell(0)
            indexCell.innerHTML = value //same for UI and table

            var idleCell = idleRow.insertCell(1)
            idleCell.innerHTML = "<b>" + idleString + "</b>"
            idleCell.setAttribute("colspan", "4")
            idleCell.setAttribute("align", "center")

            return idleRow
        }

        /**
         * Removes a reported command from the list of commands.
         */
        function removeReportedCommand(command) {
            if (command.state == "REPORTED") {
                var row = dojo.byId("run_" + command.id)
                if (row == null) {
                    return
                }
                var missingIndex = new Number(row.cells[0].innerHTML)
                var table = row.parentNode
                table.removeChild(row)

                var bottom = table.rows.length
                if (missingIndex == bottom) {
                    // Since it is the bottom, create idle row
                    // get the class of the previous one, as the deleted one changed its background
                    var rowClass = table.rows[bottom - 1].getAttribute("class") == "OddRow" ? "EvenRow" : "OddRow"
                    makeIdleRow(table, missingIndex - 1, missingIndex, lastRow)

                } else if (missingIndex < bottom) {
                    var currentRow = table.rows[missingIndex - 1]
                    var missingRowClass = currentRow.getAttribute("class") == "OddRow" ? "EvenRow" : "OddRow"
                    if (currentRow.getAttribute("id") == null) {
                        // next is idle, then create another idle.
                        makeIdleRow(table, missingIndex - 1, missingIndex, missingRowClass)

                    } else {
                        // we are adding a command row in order to get the next ones be pushed up
                        var missingRow = table.insertRow(missingIndex - 1)
                        missingRow.setAttribute('class', missingRowClass)

                        var indexCol = missingRow.insertCell(0)
                        indexCol.innerHTML = missingIndex // the UI same as table index

                        missingRow.insertCell(1)
                        missingRow.insertCell(2)
                        missingRow.insertCell(3)
                        missingRow.insertCell(4)

                        // move all of command up until an idle row is found
                        var lastDuplicateIndex = null
                        var lastDuplicateId
                        for (i = missingIndex; i < table.rows.length; i++) {
                            if (table.rows[i].getAttribute("id") == null) {
                                // the next was an idle row
                                lastDuplicateIndex = i - 1
                                break

                            } else {
                                lastDuplicateId = table.rows[i].getAttribute("id")
                                // move all the cells/columns from current to next, but the index column
                                table.rows[i-1].setAttribute("id", table.rows[i].getAttribute("id"))
                                if (passedResults.containsKey(lastDuplicateId)) {
                                    if (passedResults[lastDuplicateId] == "true") {
                                        table.rows[i-1].style.backgroundColor = "#99D6AD"

                                    } else {
                                        table.rows[i-1].style.backgroundColor = "#FFB2B2"
                                    }
                                }
                                for (j = 1; j < table.rows[i].cells.length; j++) {
                                    table.rows[i-1].cells[j].innerHTML = table.rows[i].cells[j].innerHTML
                                }
                            }
                        }
                        // remove the last command, as it is duplicate from the one pushed up
                        var row = table.rows[lastDuplicateIndex]
                        var deletedRowClass = row.getAttribute("class")
                        table.deleteRow(lastDuplicateIndex)

                        var row = makeIdleRow(table, lastDuplicateIndex, lastDuplicateIndex + 1, deletedRowClass)
                        if (passedResults.containsKey(lastDuplicateId)) {
                            if (passedResults[lastDuplicateId] == "true") {
                                row.style.backgroundColor = "#99D6AD"
                            } else {
                                row.style.backgroundColor = "#FFB2B2"
                            }
                        }
                    }
                }
            }
        }

        /**
         * Function instance that is used to initialize upgrade process. Namely,
         * the dojox.cometd component, as well as the major UI objects for the 
         * initial status.
         */
        var init = function() {
            dojox.cometd.init('/csvn/plugins/cometd-0.1.5/cometd')
            dojox.cometd.subscribe(statusCounterChannel, onMessage)

            updateUiCommandsRunning(currentNumberCommands, "spinner_long")
            updateUiCommandsRunning(currentNumberCommands, "spinner_short")

            showMessage()
        };
        dojo.addOnLoad(init)

        /**
         * Function instance that is used to finish the upgrade process. It
         * finishes the dojox.cometd components.
         */
        var destroy = function() {
            dojox.cometd.unsubscribe(statusCounterChannel)
            dojox.cometd.disconnect()
        };
        dojo.addOnUnload(destroy)

        /**
         * Callback function for the cometd client that receives the messages
         * from the subscribed channels.
         * @param m is the message object proxied by the cometd server from 
         * one of the subscribed channels.
         * @see init()
         */
        function onMessage(m) {
            var c = m.channel;
            var o = eval('(' + m.data + ')')
            if (c == statusCounterChannel) {
                updateUiCommandsRunning(o.total)

            } else if (c == commandsStateChannel) {
                //if (o.state != "REPORTED") 
                addUpdateCommandToTable(o)
            }
        }

        function addNewCommand(o) {
            if (o.state == "SCHEDULED") {
                updateScheduledCommands(o)

            } else if (o.state == "RUNNING") {
                removeFromScheduled(o)
                if (o.type == "long") {
                    updateRunningCommands("longRunningCommandsTable", o)

                } else {
                    updateRunningCommands("shortRunningCommandsTable", o)
                }
            }
        }

        function addScheduledCommand() {
            o = {
                    "id": "cmdexec2002", 
                    "code": "copyRevprops",
                    "state": "SCHEDULED"}
            addNewCommand(o)
        }

        function addRunningCommand() {
            o = {
                    "id": "cmdexec2002", 
                    "code": "copyRevprops",
                    "state": "RUNNING",
                    "type": "short",
                    "params": {"repoName": "repo2"},
                    "startedAt":"22"
                }
            addNewCommand(o)
        }

        function getCheckedValue(radioObj) {
            if(!radioObj) {
                return ""
            }
            var radioLength = radioObj.length
            if (radioLength == null) {
                return radioObj.checked ? radioObj.value : ""
            }
            for(var i = 0; i < radioLength; i++) {
                if(radioObj[i].checked) {
                    return radioObj[i].value;
                }
            }
            return "";
        }

        function hightlightCommand(cmdId, result) {
            result = getCheckedValue(result)
            o = {
                    "id": cmdId, 
                    "code": "copyRevprops",
                    "state": "TERMINATED",
                    "succeeded": result,
                    "type": "short"
            }
            highlightTerminatedCommand(o)
        }

        function removeCommand(cmdId) {
            o = {
                    "id": cmdId, 
                    "code": "copyRevprops",
                    "state": "REPORTED",
                    "type": "short"
            }
            removeReportedCommand(o)
        }

    </script>

</head>

<body>

<content tag="title">
  <g:message code="job.page.header"/>
</content>

<content tag="leftMenu">
<BR>
  <form name="ids">
   <input type="text" id="cmdId" value="cmdexec2002">
   Passed? 
   <input type="radio" name="succeeded" value="true" checked="checked"> Yes
   <input type="radio" name="succeeded" value="false"> No
   <BR>
   <input type="button" value="Schedule" onclick="addScheduledCommand()">
   <input type="button" value="Running" onclick="addRunningCommand()">
   <input type="button" value="Terminated" onclick="hightlightCommand(document.ids.cmdId.value, document.ids.succeeded)">
   <input type="button" value="Reported" onclick="removeCommand(document.ids.cmdId.value)">
  </form>

   <div class="ImageListParent">
    <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
    <b>Master Server:</b> cu064.cloud.sp.collab.net:18080
   </div>
   <div class="ImageListParent" id="commandsCount">
     <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
      ${totalCommandsRunning == 0 ? "No" : (totalCommandsRunning)} command(s) running.
   </div>
   <div class="ImageListParent">
     <img width="9" hspace="5" height="9" src="${resource(dir:'/images/icons',file:'big_bullet.gif')}" alt="&bull;"/>
     Polling at every ${commandPollRate} seconds.
   </div>

<BR>

<div class="ImageListParent">
   <table class="Container" id="scheduledCommandsTable">
     <tbody>
     <tr class="ContainerHeader">
       <td colspan="2">
         <g:message code="job.page.list.scheduled.header"/>
       </td>
     </tr>
  <g:each in="${scheduledCommands}" status="i" var="schCommand">
    <tr id="sch_${schCommand.id}" class="${(i % 2) == 0 ? 'OddRow' : 'EvenRow'}">
     <td>
      &nbsp; <g:set var="commandCode" value="${AbstractCommand.makeCodeName(schCommand)}" />
      <img border="0" src="/csvn/images/replica/${commandCode}.png"> 
      ${schCommand.id}
      <g:if test="${schCommand.params.repoName}">
         (${schCommand.params.repoName})
      </g:if>
     </td>
    </tr>
  </g:each>
    </tbody>
   </table>
</div>

</content>

<g:render template="/job/runningCommands" model="['tableName': 'longRunningCommandsTable',
 'runningCommands': longRunningCommands, 'maxNumber': 5, 'shortRun': false]" />

<BR><BR>


<g:render template="/job/runningCommands" model="['tableName': 'shortRunningCommandsTable',
 'runningCommands': shortRunningCommands, 'maxNumber': 10, 'shortRun': true]" />

</body>
