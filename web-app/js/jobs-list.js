/**
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
 * 
 * Author: Marcello de Sales (mdesales@collab.net)
 * Date: May 18, 2011
 */
dojo.require('dojox.cometd')
dojo.require("dijit.Dialog")

/** The counter cometd channel. */
var commandsStateChannel = "/csvn-replica/commands-states"

/**
 * Function instance that is used to initialize upgrade process. Namely,
 * the dojox.cometd component, as well as the major UI objects for the 
 * initial status.
 */
var init = function() {
    dojox.cometd.init('/csvn/plugins/cometd-0.1.5/cometd')
    dojox.cometd.subscribe(commandsStateChannel, onStateMessageCallbackHandler)
};
dojo.addOnLoad(init)

/**
 * Function instance that is used to finish the upgrade process. It
 * finishes the dojox.cometd components.
 */
var destroy = function() {
    dojox.cometd.unsubscribe(commandsStateChannel)
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
function onStateMessageCallbackHandler(m) {
    var o = eval('(' + m.data + ')')
    updateCommands(o)
}

/**
 * Utility method that waits for the server to restart at every
 * 5 seconds.
 */
function updateCommands(cmd) {
    if (cmd.state == "SCHEDULED") {
        updateScheduledCommands(cmd)

    } else if (cmd.state == "RUNNING") {
        removeFromScheduled(cmd)
        if (cmd.type == "long") {
            updateRunningCommands("longRunningCommandsTable", cmd)

        } else {
            updateRunningCommands("shortRunningCommandsTable", cmd)
        }

    } else if (cmd.state == "TERMINATED") {
        highlightTerminatedCommand(cmd)

    } else if (cmd.state == "REPORTED") {
        removeReportedCommand(cmd)
    }
}

/**
 * Appends a command icon to the given element.
 */
function appendCommandIcon(element, command) {
    var cmdIconImage = new Image()
    cmdIconImage.src = "/csvn/images/replica/" + command.code + ".png"
    cmdIconImage.border = "0"
    element.appendChild(cmdIconImage)
}

/**
 * Updates the table of scheduled commands with the given command.
 */
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
 * Creates a new row with the columns for a command in the given index, printing the given
 * printIndex with the given classRow.
 */
function createCommandRow(table, index) {
    var rowClass = table.rows[index].getAttribute("class")
    table.deleteRow(index)

    var newRow = table.insertRow(index)
    newRow.setAttribute('class', rowClass)

    var indexCol = newRow.insertCell(0)
    indexCol.innerHTML = index + 1

    newRow.insertCell(1)
    newRow.insertCell(2)
    newRow.insertCell(3)
    if (table.getAttribute("id") == "shortRunningCommandsTable") {
        newRow.insertCell(4)
    }
    return newRow
}

/**
 * Removes the possible highlight of a removed row.
 */
function removePossibleHighlight(fromRow, toRow) {
    if (passedResults.containsKey(fromRow.getAttribute("id"))) {
        if (passedResults[fromRow.getAttribute("id")] == "true") {
            toRow.style.backgroundColor = "#99D6AD"

        } else {
            toRow.style.backgroundColor = "#FFB2B2"
        }
        fromRow.style.backgroundColor = null
    }
}

/**
 * Adding a new command row to the existing table.
 * @param tableBodyId is the Id of the TBody tag holding the rows.
 */
function updateRunningCommands(tableBodyId, command) {
    var table = dojo.byId(tableBodyId)
    // move all commands one line down.
    var row = null
    if (table.rows.length >= 2) {
        var bottomPosition = table.rows.length - 2
        for (i = bottomPosition; i >= 0 ; i--) {
            var currentRow = table.rows[i]
            var nextRow = table.rows[i + 1]
            if (currentRow.getAttribute("id") == null && nextRow.getAttribute("id") == null) {
                if (i == 0) {
                    // First command to be added.
                    row = createCommandRow(table, i)
                    break

                } else continue

            } else if (currentRow.getAttribute("id") != null && nextRow.getAttribute("id") == null) {
                // remove the idle row and place a new one, keeping its formatting.
                nextRow = createCommandRow(table, i + 1)
            }
            // move all the cells/columns from current to next
            nextRow.setAttribute("id", currentRow.getAttribute("id"))
            for (j = 1; j < table.rows[i].cells.length; j++) {
                nextRow.cells[j].innerHTML = currentRow.cells[j].innerHTML
            }
            // if the current row had a highlight, then move it down
            removePossibleHighlight(currentRow, nextRow)
        }

    } else {
        // Table had only one row.
        row = createCommandRow(table, 0)
    }

    // add the ID column
    row.setAttribute("id", "run_" + command.id)
    row.cells[1].innerHTML = "<a target='" + command.id + "' href='/csvn/log/show?fileName=/temp/" + command.id + ".log&view=tail'>" + command.id + "</a>"

    // add the command code column
    var codeCol = row.cells[2]
    codeCol.innerHTML = ""
    appendCommandIcon(codeCol, command)
    codeCol.appendChild(document.createTextNode(" "))
    if (command.params.repoName != null) {
        var index = command.params.repoName.lastIndexOf("/")
        if (index == -1) {
            index = command.params.repoName.lastIndexOf("\\")
        }
        var repo = command.params.repoName.substring(index + 1, command.params.repoName.length)
        var repoStringElement = document.createTextNode(cmdStrings[command.code].replace(
            "&quot;x&quot;", "'" + repo + "'"))
        codeCol.appendChild(repoStringElement)

    } else {
        var cmdDescStringElement = document.createTextNode(cmdStrings[command.code])
        codeCol.appendChild(cmdDescStringElement)
        var parameters = command.params.toJSONString().replace(/,/gi, " , ")
        if (parameters == null || parameters.length == 0) {
            row.cells[3].innerHTML = "-"
        }  else {
            row.cells[3].innerHTML = parameters
        }
    }
    if (table.getAttribute("id") == "shortRunningCommandsTable") {
        row.cells[4].innerHTML = command.startedAt

    } else {
        row.cells[3].innerHTML = command.startedAt
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
        var succ = new String(command.succeeded)
        if (succ == "true") {
            row.style.backgroundColor = "#99D6AD"

        } else {
            row.style.backgroundColor = "#FFB2B2"
        }
        passedResults["run_" + command.id] = command.succeeded
        paramsIndex[command.id] = command.params
    }
}

/**
 * Creates a new idle row in the given index position with the index value.
 */
function makeIdleRow(table, index) {
    var row = table.rows[index]
    var rowClass = row.getAttribute("class")
    table.removeChild(row)

    var idleRow = table.insertRow(index)
    idleRow.setAttribute('class', rowClass)

    var indexCell = idleRow.insertCell(0)
    indexCell.innerHTML = index + 1 //same for UI and table

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
        missingIndex--

        var table = row.parentNode // table actually is a tbody

        if (table.rows.length == 1) {
            // Since it is the bottom, create idle row
            makeIdleRow(table, 0)

        } else if (missingIndex < table.rows.length - 1) {
            // place an idle row
            var idleRow = makeIdleRow(table, missingIndex)

            // if there are more commands below it, push all commands up
            for (i = missingIndex + 1; i < table.rows.length; i++) {
                var nextRow = table.rows[i] // the current after the deletion
                if (nextRow.getAttribute("id") == null) {
                    break
                }
                table.rows[i - 1] = nextRow
                table.rows[i] = idleRow
                table.rows[i - 1].cells[0].innerHTML = i
                table.rows[i].cells[0].innerHTML = i + 1
            }
        }
        if (command.code == "replicaPropsUpdate") {
            var params = paramsIndex[command.id]
            if (params == null) {
                return
            }
            if (params.commandPollPeriod != null) {
                dojo.byId("pollingIntervalString").innerHTML = pollingChangeString.replace("x", params.commandPollPeriod)
            }
            var newMaxLong = parseInt(params.commandConcurrencyLong)
            if (newMaxLong != null) {
                var longRunningTable = dojo.byId("longRunningCommandsTable")
                resizeTable(longRunningTable, newMaxLong)
            }
            var newMaxShort = parseInt(params.commandConcurrencyShort)
            if (newMaxShort != null) {
                var shortRunningTable = dojo.byId("shortRunningCommandsTable")
                resizeTable(shortRunningTable, newMaxShort)
            }
        }
        paramsIndex[command.id] = null
    }

    /**
     * Resizes the the given table to the given new size.
     * @param table
     * @param newSize
     */
    function resizeTable(table, newSize) {
        if (newSize > table.rows.length) {
            var diff = newSize - table.rows.length
            var newRowClass = table.rows[table.rows.length - 1].getAttribute('class')
            for (i = 0; i < diff; i++) {
                newRowClass = newRowClass == "EvenRow" ?  "OddRow" : "EvenRow"

                var idleRow = table.insertRow(table.rows.length)
                idleRow.setAttribute('class', newRowClass)

                var indexCell = idleRow.insertCell(0)
                indexCell.innerHTML = table.rows.length

                var idleCell = idleRow.insertCell(1)
                idleCell.innerHTML = "<b>" + idleString + "</b>"
                idleCell.setAttribute("colspan", "4")
                idleCell.setAttribute("align", "center")
            }

        } else if (newSize < table.rows.length) {
            var diff = table.rows.length - newSize
            for (i = 0; i < diff; i++) {
                table.deleteRow(table.rows.length - 1)
            }
        }
    }
}