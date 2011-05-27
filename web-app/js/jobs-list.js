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
    if (o.totalCommands > 0) {
        dojo.byId('commandsCount').innerHTML = commands_running + " " + o.totalCommands

    } else {
        dojo.byId('commandsCount').innerHTML = no_commands
    }
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
function createCommandRow(table, index, printIndex, classRow) {
    var newRow = table.insertRow(index)
    newRow.setAttribute('class', classRow)

    var indexCol = newRow.insertCell(0)
    indexCol.innerHTML = printIndex

    newRow.insertCell(1)
    newRow.insertCell(2)
    newRow.insertCell(3)
    newRow.insertCell(4)

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
 */
function updateRunningCommands(tableId, command) {
    var table = dojo.byId(tableId)
    // move all commands one line down. Last command disappear.
    var bottomPosition = table.rows.length - 2 // table header and columns description

    for (i = bottomPosition; i > 1 ; i--) {
        var currentRow = table.rows[i]
        var nextRow = table.rows[i + 1]
        if (currentRow.getAttribute("id") == null && nextRow.getAttribute("id") == null) {
            if (i == 2) {
                // First command to be added.
                var rowClass = currentRow.getAttribute("class")
                var rowIndex = currentRow.cells[0].innerHTML
                table.deleteRow(i) // currentRow

                createCommandRow(table, i, rowIndex, rowClass)
                break

            } else {
                continue
            }

        } else if (currentRow.getAttribute("id") != null && nextRow.getAttribute("id") == null) {
            // remove the idle row and place a new one, keeping its formatting.
            var rowClass = nextRow.getAttribute("class")
            var rowIndex = nextRow.cells[0].innerHTML
            table.deleteRow(i+1) // nextRow

            var nextRow = createCommandRow(table, i+1, rowIndex, rowClass)
        }
        // move all the cells/columns from current to next
        nextRow.setAttribute("id", currentRow.getAttribute("id"))
        for (j = 1; j < table.rows[i].cells.length; j++) {
            nextRow.cells[j].innerHTML = currentRow.cells[j].innerHTML
        }
        // if the current row had a highlight, then move it down
        removePossibleHighlight(currentRow, nextRow)
    }

    // add the new command in the new row.
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
 * Highlight the terminated command in green or red in case it passed or failed,
 * respectively, but not removing it until it has been reported.
 */
function highlightTerminatedCommand(command) {
    if (command.state == "TERMINATED") {
        var row = dojo.byId("run_" + command.id)
        if (row == null) {
            return
        }
        if (new Boolean(command.succeeded)) {
            row.style.backgroundColor = "#99D6AD"

        } else {
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

                        // move the ID
                        table.rows[i-1].setAttribute("id", table.rows[i].getAttribute("id"))

                        // if the current row had a highlight, then move it up
                        removePossibleHighlight(table.rows[i], table.rows[i-1])

                        // move all the cells/columns from current to next, but the index column
                        for (j = 1; j < table.rows[i].cells.length; j++) {
                            table.rows[i-1].cells[j].innerHTML = table.rows[i].cells[j].innerHTML
                        }
                    }
                }
                // remove the last command, as it is duplicate from the one pushed up
                var rowDeleted = table.rows[lastDuplicateIndex]
                var deletedRowClass = row.getAttribute("class")
                table.deleteRow(lastDuplicateIndex)

                var row = makeIdleRow(table, lastDuplicateIndex, lastDuplicateIndex + 1, deletedRowClass)

                // if the current row had a highlight, then move it up
                removePossibleHighlight(table.rows[i], table.rows[i-1])

                if (passedResults.containsKey(lastDuplicateId)) {
                    if (passedResults[lastDuplicateId] == "true") {
                        row.style.backgroundColor = "#99D6AD"
                    } else {
                        row.style.backgroundColor = "#FFB2B2"
                    }
                    table.rows[lastDuplicateIndex + 1].style.backgroundColor = null
                }
            }
        }
    }
}